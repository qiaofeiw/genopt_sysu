package pgenopt.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lsgo_benchmark.F1;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import pgenopt.PGenOpt;
import pgenopt.format.Arff;
import pgenopt.math.LinAlg;
import pgenopt.math.Point;
import pgenopt.ml.cluster.KmeansGrouping;
import pgenopt.util.Orthogonal;
import pgenopt.util.util;

public class ParallelCWOCC {

	private static final int default_numPar = 100;

	private static final String default_numGen = "600";

	private static final int default_group_num = 10;

	private static final int default_group_size = 50;

	private static final int default_cycle = 5000;

	private static Random RanGen;

	private static Point context;

	private static lsgo_benchmark.Function[] f;

	private static HashMap<String, String> setting;

	private static final int index_pbest = 0;

	private static final int index_nbest = 1;

	private static final int index_oed = 2;

	public static void main(String[] args) throws Exception {
		int Q1 = 2, J1 = 10;
		int Q2 = 2, J2 = 5;
		double[][] OTable1;
		setting = new HashMap<String, String>();
		f = new lsgo_benchmark.Function[1];
		RanGen = new Random(System.currentTimeMillis());
		Point[][] population = new Point[default_group_num][];
		List<Point[]> group = new ArrayList<Point[]>();
		double[][] pop; // the global position
		int[][] group_map;
		Double[] valc; // current、previous fitness
		ArrayList<Double> bestval = new ArrayList<Double>();
		ArrayList<double[]> bestpop = new ArrayList<double[]>();
		int besti;

		f[0] = new F1();
		// OTable = initOrthogonalTable(Q, J, f[0].getMax(), f[0].getMin());
		final double[][] OTable2 = Orthogonal.initialOrthogonalForm(Q2, J2);
		int dim = f[0].getDimension();
		context = new Point(dim, 0, 1);
		// pop = initializeCurrentPopulation(OTable, f);
		pop = randomInitializeCurrentPopulation(f);
		Double[] OED_val = new Double[3];
		double[][] OED_pop = new double[3][dim];

		// evaluate
		valc = updateF(pop, f);
		besti = LinAlg.min(valc);
		bestval.add(new Double(valc[besti]));
		OED_pop[index_pbest] = pop[besti].clone();
		bestpop.add(OED_pop[index_pbest]);
		context.setX(pop[besti].clone());
		context.setF(0, valc[besti]);
		// choose which method to group;
		// group = randomGrouping(pop, default_group_size);
		group_map = motivateGrouping(pop, valc, f[0]);
		assert(group_map.length == default_group_num);
//		population[0] = new Point[default_numPar];
//		for (int i = 0; i < default_numPar; i++) {
//			population[0][i] = new Point(dim, 0, 1);
//		}
//		group.add(population[0]);
		for (int i = 0; i < default_group_num; i++) {
			population[i] = new Point[default_numPar];
			for (int j = 0; j < default_numPar; j++) {
				population[i][j] = new Point(dim, 0, 1);
			}
			group.add(population[i]);
		}

		// run the optimizer
		setting.put("algorithm", "CWOCC");
		setting.put("NumGen", default_numGen);
		int ng = group_map.length;
		for (int i = 0; i < ng; i++) {
			grouping(group.get(i), group_map[i], pop, valc);
		}

		JavaSparkContext ctx = new JavaSparkContext("local", "PSOCC",
				System.getenv("SPARK_HOME"),
				JavaSparkContext.jarOfClass(ParallelCWOCC.class));

		ArrayList<double[]> npop = new ArrayList<double[]>();
		//cc cycle
		for (int cycle = 0; cycle < default_cycle; cycle++) {
			JavaRDD<Point[]> rdd = ctx.parallelize(group, group.size());
			group = rdd.map(new Mapper()).collect();
			OED_pop[index_nbest] = combine(group, dim);
			npop.add(OED_pop[index_nbest]);
			double[][] obest = new double[OTable2.length][dim];
//			assert(OTable2[0].length == group_map.length);
			for (int i = 0; i < OTable2.length; i++) {
				for (int j = 0; j < group_map.length; j++) {
					if (OTable2[i][j] == 1.0) {
						assign(OED_pop[index_pbest], group_map[j], obest[i],
								group_map[j]);
					} else {
						assign(OED_pop[index_nbest], group_map[j], obest[i],
								group_map[j]);
					}
				}
			}
			Double[] oval = updateF(obest, f);

			// Factor Analysis
			double[][] factor = new double[Q2][default_group_num];
			for (int i = 0; i < default_group_num; i++) {
				int O = 0;
				int I = 0;
				for (int j = 0; j < OTable2.length; j++) {
					if (OTable2[j][i] == 1.0) {
						factor[0][i] += oval[j];
						O++;
					} else {
						factor[1][i] += oval[j];
						I++;
					}
				}
				factor[0][i] /= O;
				factor[1][i] /= I;
			}

			for (int i = 0; i < default_group_num; i++) {
				if (factor[0][i] < factor[1][i]) {
					assign(OED_pop[index_pbest], group_map[i], OED_pop[index_oed],
							group_map[i]);
				} else {
					assign(OED_pop[index_nbest], group_map[i], OED_pop[index_oed],
							group_map[i]);
				}
			}
			OED_val[index_pbest] = bestval.get(bestval.size() - 1);
			OED_val[index_nbest] = updateF(OED_pop[index_nbest], f[0]);
			OED_val[index_oed] = updateF(OED_pop[index_oed], f[0]);
			for (Double v : OED_val) {
				util.println(v.toString());
			}

			int min = LinAlg.min(OED_val);
			bestval.add(OED_val[min]);
			OED_pop[index_pbest] = Arrays.copyOf(OED_pop[min], OED_pop[min].length);
			bestpop.add(OED_pop[min]);
			context.setX(OED_pop[min]);
			context.setF(0, OED_val[min]);
			
			for (int i = 0; i < group.size(); i++) {
				int max = LinAlg.max(group.get(i));
				Point m = group.get(i)[max];
				int[] subidx = m.getSubIndex();
				double[] x = new double[subidx.length];
				for (int j = 0; j < subidx.length; j++) {
					x[j] = OED_pop[min][subidx[j]];
				}
				m.setX(x);
				m.setF(0, OED_val[min]);
			}
//			bestval.add(OED_val[min]);
//			bestpop.add(OED_pop[index_nbest]);
//			context.setX(OED_pop[index_nbest]);
//			context.setF(0, OED_val[index_nbest]);
		}
	}

	static class Mapper extends Function<Point[], Point[]> {

		@Override
		public Point[] call(Point[] population) throws Exception {
			// TODO Auto-generated method stub
			PGenOpt genopt = new PGenOpt(f, setting, population, (Point)context.clone());
			genopt.run();

			return genopt.getResultPoint();
		}

	}

	protected static double[][] randomInitializeCurrentPopulation(
			lsgo_benchmark.Function[] f) {
		int NumPar = default_numPar;
		int dimCon = f[0].getDimension();
		double[][] pop = new double[NumPar][dimCon];
		double xmax = f[0].getMax();
		double xmin = f[0].getMin();

		for (int i = 0; i < NumPar; i++) {
			for (int j = 0; j < dimCon; j++) {
				final double r = RanGen.nextDouble();
				final double x = xmin + r * (xmax - xmin);
				pop[i][j] = x;
			}
		}

		return pop;
	}

	protected static double[][] initializeCurrentPopulation(double[][] OTable,
			lsgo_benchmark.Function[] f) {
		int row = OTable.length;
		int column = OTable[0].length;
		int[] p = util.randomperm(row);
		int i, j;
		int NumPar = default_numPar;
		int dimCon = f[0].getDimension();
		double[][] pop = new double[NumPar][dimCon];

		for (i = 0; i < NumPar; i++) {
			for (j = 0; j < dimCon; j++) {
				if (i < row) {
					pop[i][j] = OTable[p[i]][j];
				} else {
					pop[i][j] = 2.0;
				}
			}
		}

		return pop;

	}

	public static double updateF(double[] pop, lsgo_benchmark.Function f) {
		double fun;
		fun = f.compute(pop);
		assert (Double.isNaN(fun));
		return fun;
	}

	public static Double[] updateF(double[][] pop, lsgo_benchmark.Function[] f) {
		Double[] fun = new Double[pop.length];
		for (int i = 0; i < pop.length; i++) {
			fun[i] = updateF(pop[i], f[0]);
		}
		return fun;
	}

	protected static double[][] initOrthogonalTable(int Q, int J, double xmax,
			double xmin) {
		double[][] OTable;
		OTable = Orthogonal.initialOrthogonalConForm(Q, J, xmax, xmin);
		return OTable;
	}

	protected static LinkedList<Point[]> initialGroup(Point[] point, Point best) {
		LinkedList<Point[]> list = new LinkedList<Point[]>();
		int dim = best.getDimensionContinuous();
		int subdim = default_group_size;
		int num = point.length;
		int ptr_start = 0;
		int i;

		do {
			if (dim > default_group_size)
				subdim = default_group_size;
			else
				subdim = dim;
			Point[] group = new Point[num];
			group[0] = new Point(subdim, 0, 1);
			int[] index = new int[subdim];
			for (i = 0; i < subdim; i++) {
				index[i] = i + ptr_start;
			}
			for (i = 1; i < num; i++) {
				System.arraycopy(group, 0, group, i, 1);
			}

			for (i = 0; i < num; i++) {
				double[] x = new double[subdim];
				System.arraycopy(point[i].getX(), ptr_start, x, 0, subdim);
				group[i].setX(x);
				group[i].setF(point[i].getF());
				group[i].setSubIndex(index);
			}
			list.add(group);
			ptr_start += subdim;
			dim = dim - subdim;
		} while (dim > 0);

		return list;
	}

	protected static void grouping(Point[] population, int[] group,
			double[][] pop, Double[] valc) {
		assert (population.length == group.length);
		int ng = population.length;
		int subdim = group.length;
		int i, j;
		for (i = 0; i < ng; i++) {
			double[] x = new double[subdim];
			for (j = 0; j < subdim; j++) {
				x[j] = pop[i][group[j]];
			}
			population[i].setX(x);
			population[i].setF(0, valc[i]);
			population[i].setSubIndex(group);
		}
	}

	protected static double[] combine(List<Point[]> population, int dim) {
		double[] nbest = new double[dim];
		for (int i = 0; i < population.size(); i++) {
			Point[] group = population.get(i);
			int min = LinAlg.min(group);
			assign(group[min].getX(), nbest, group[min].getSubIndex());
		}
		return nbest;
	}

	private static void assign(double[] src, double[] dest, int[] index) {
		for (int i = 0; i < index.length; i++) {
			for (int j = 0; j < dest.length; j++) {
				dest[index[i]] = src[i];
			}
		}
	}

	private static void assign(double[] src, int[] isrc, double[] dest,
			int[] idest) {
		for (int i = 0; i < idest.length; i++) {
			for (int j = 0; j < dest.length; j++) {
				dest[idest[i]] = src[isrc[i]];
			}
		}
	}

	protected static int[][] randomGrouping(double[][] pop, int subdim) {
		int dim = pop[0].length;
		int[][] subi = new int[(int) Math.ceil((double) dim / subdim)][subdim];

		int[] dim_rand = util.randomperm(dim);
		for (int i = 0; i < subi.length; i++) {
			System.arraycopy(dim_rand, i * subdim, subi[i], 0, subdim);
		}

		return subi;
	}

	protected static int[][] motivateGrouping(double[][] pop, Double[] val,
			lsgo_benchmark.Function f) throws Exception {
		int ng = pop.length;
		int dim = pop[0].length;
		double[][] delta = new double[ng][dim];
		double[][] copy = new double[ng][dim];

		for (int i = 0; i < ng; i++) {
			System.arraycopy(pop[i], 0, copy[i], 0, dim);
		}

		double d = RanGen.nextDouble();

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < ng; j++) {
				copy[j][i] += d;
				delta[j][i] = updateF(copy[j], f) - val[j];
			}
		}
		double[] avgd = new double[dim];
		for (int i = 0; i < dim; i++) {
			avgd[i] = LinAlg.sumColumn(delta, i) / ng;
		}

		Arff arff = util.convertToArff(avgd);
		String filename = "delta.arff";
		arff.writeToFile(filename);
		KmeansGrouping kmean = new KmeansGrouping(filename, default_group_num);

		return kmean.getGroup();
	}

	protected static void deltaGrouping(List<Point[]> current, List<Point[]> pre) {

	}

}
