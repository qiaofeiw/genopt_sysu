package pgenopt.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import pgenopt.PGenOpt;
import pgenopt.math.ContinuousParameter;
import pgenopt.math.LinAlg;
import pgenopt.math.Point;
import pgenopt.util.Orthogonal;
import pgenopt.util.util;

public class CWO extends Optimizer {

	public CWO(PGenOpt pgenopt) {
		super(pgenopt);
		dimCon = getDimensionContinuous();
		dimDis = getDimensionDiscrete();
		dimF = getDimensionF();
		double xmax = f[0].getMax();
		double xmin = f[0].getMin();
		lb = new double[dimCon];
		ub = new double[dimCon];
		for (int i = 0; i < dimCon; i++) {
			lb[i] = 1;
			ub[i] = 1;
		}
		lb = LinAlg.multiply(xmin, lb);
		ub = LinAlg.multiply(xmax, ub);
		RanGen = new Random(System.currentTimeMillis());
		
		NumGen = getInputValueInteger("NumGen", 300000);
		NumPar = getInputValueInteger("NumPar", 30);

		if (data.isInitialContinuousParameter()) {
			NumPar = data.getInitialPoint().length;
			initializeCurrentPosition(data.getInitialPoint());
		} else {
			initOrthogonalTable(Q, J, xmax, xmin);
			initializeCurrentPosition();
		}
	}

	@Override
	public int run() {
		int retFla;
		double ef;
		Point PX, PX_best;
		int nf; // current discussion
		ArrayList<Double> Pbest = new ArrayList<Double>();
		MM = new double[NumPar];
		V = new double[NumPar][dimCon];
		IGen = 0;
		while (IGen < NumGen) {
			ef = calef();
			nfmax = (int) Math.floor(1500 - 450 * (Math.pow(ef, 0.5) + ef));

			minPF_index = LinAlg.min(P_X);
			// minPF = P_X[minPF_index];
			PX = (Point) P_X[minPF_index].clone();

			PX_best = (Point) PX.clone();
			nf = 0;
			T = Ti;
			while (nf < nfmax) {
				double[] PXn = neighbour(PX);
				double PFn = f[0].compute(PXn);

				if (PFn < PX_best.getF(0)) {
					PX_best.setX(PXn.clone());
					PX_best.setF(0, PFn);

					if (PFn < P_X[minPF_index].getF(0)) {
						P_X[minPF_index].setX(PXn.clone());
						P_X[minPF_index].setF(0, PFn);
					}
				} else {
					nf++;
				}

				IGen++;
				Pbest.add(P_X[minPF_index].getF(0));
				if (prob(PX.getF(0), PFn, T) > RanGen.nextDouble()) {
					PX.setF(0, PFn);
					PX.setX(PXn);
				}
				T = (Ti - Tf) * (1 - (double)nf / nfmax) + Tf;
			}

			P_X[minPF_index] = (Point) PX_best.clone();
			reportMinimum(P_X);

			if (RanGen.nextDouble() < 0.0) {
				for (int i = 1; i <= NumPar; i++) {
					double d2 = (X[minF_index].getF(0) - P_X[i - 1].getF(0))
							/ (X[maxF_index].getF(0) - X[minF_index].getF(0));
					MM[i - 1] = Math.exp(d2);
				}

				MM[minF_index] = 30;

				FF = new double[dimCon][NumPar];
				for (int i = 1; i <= NumPar; i++) {
					for (int j = 1; j <= NumPar; j++) {
						double[] d = new double[dimCon];
						if ((i != j) && (i - 1 != minF_index)) {
							if (X[i - 1].getF(0) < P_X[j - 1].getF(0)) {
								for (int k = 1; k <= dimCon; k++) {
									d[k - 1] = X[i - 1].getX(k - 1)
											- P_X[j - 1].getX(k - 1);
								}
							} else {
								for (int k = 1; k <= dimCon; k++) {
									d[k - 1] = P_X[j - 1].getX(k - 1)
											- X[i - 1].getX(k - 1);
								}
							}
							for (int k = 1; k <= dimCon; k++) {
								if (d[k - 1] == 0) {
									continue;
								}
								FF[k - 1][i - 1] = FF[k - 1][i - 1] + G
										* MM[i - 1] * MM[j - 1] * d[k - 1];
							}
						}
					}
				}
				FF = LinAlg.transposition(FF);
				w = (Math.pow(ef, 0.5) + ef) * 0.25 + 0.4;

				for (int i = 1; i <= NumPar; i++) {
					if (i - 1 != minF_index) {
						for (int k = 1; k <= dimCon; k++) {
							double a = RanGen.nextGaussian();
							V[i - 1][k - 1] = w * V[i - 1][k - 1] + a
									* FF[i - 1][k - 1] / MM[i - 1];
							X[i - 1].setX(k - 1, X[i - 1].getX(k - 1)
									+ V[i - 1][k - 1]);
							if (X[i - 1].getX(k - 1) < lb[k - 1]) {
								X[i - 1].setX(k - 1, lb[k - 1]);
								V[i - 1][k - 1] = -0.5 * V[i - 1][k - 1];
							}
							if (X[i - 1].getX(k - 1) > ub[k - 1]) {
								X[i - 1].setX(k - 1, ub[k - 1]);
								V[i - 1][k - 1] = -0.5 * V[i - 1][k - 1];
							}
						}
					}
				}

				for (int i = 1; i <= NumPar; i++) {
					if (i - 1 != minF_index) {
						updateF(X[i - 1]);
						if (X[i - 1].getF(0) < P_X[i - 1].getF(0)) {
							P_X[i - 1] = (Point) X[i - 1].clone();
						}
					}
				}

				minF_index = LinAlg.min(X);
				maxF_index = LinAlg.max(X);

				for (int i = 0; i <= NumPar - 1; i++) {
					IGen++;
				}
				println(X[minF_index].getF(0) + "");
				reportMinimum(P_X);
			}
		}
//		for (int i = 0; i < NumPar; i++) {
//			X[i].setF(0, X[i].getF(0));sß
//		}
		reportMinimum(P_X);
		retFla = 1;
		return retFla;
	}
	
	public void updateF(Point x) {
		double[] fun = new double[dimF];
		for (int i = 0; i < dimF; i++) {
			fun[i] = f[i].compute(x.getX());
			isNaN(fun[i]);
		}
		x.setF(fun);
//		printPoint(x, IGen);
	}
	
	@Override
	protected void setResult() {
		data.setResult(X);
	}

	protected void initializeCurrentPosition() {
		int[] p = util.randomperm(row);
		P_X = new Point[NumPar];
		X = new Point[NumPar];
		int i, j;

		for (int iP = 0; iP < NumPar; iP++) {
			// P_X[iP] = new Point(dimCon, dimDis, dimF);
			X[iP] = new Point(dimCon, dimDis, dimF);
		}

		for (i = 1; i <= NumPar; i++) {
			for (j = 1; j <= dimCon; j++) {
				if (i < row + 1) {
					X[i - 1].setX(j - 1, OTable[p[i - 1]][j - 1]);
				} else {
					X[i - 1].setX(j - 1, 2.0);
				}
				// P_X[i-1].setX(j-1, X[i-1].getX(j-1));
			}
		}

		for (i = 1; i <= NumPar; i++) {
			updateF(X[i - 1]);
			// P_X[i-1].setF(0, X[i-1].getF(0));
			P_X[i - 1] = (Point) X[i - 1].clone();
		}
		minF_index = LinAlg.min(X);
		maxF_index = LinAlg.max(X);
	}
	
	protected void initializeCurrentPosition(Point[] points) {
		P_X = new Point[NumPar];
		X = new Point[NumPar];
		int i;
		for (i = 0; i < NumPar; i++) {
			X[i] =  points[i];
			P_X[i] = (Point) X[i].clone();
		}
		
		minF_index = LinAlg.min(X);
		maxF_index = LinAlg.max(X);
	}

	protected void initOrthogonalTable(int Q, int J, double xmax, double xmin) {
		if (OTable == null) {
			OTable = Orthogonal.initialOrthogonalConForm(Q, J, xmax, xmin);
			row = OTable.length;
			column = OTable[0].length;
		}
	}

	protected double calef() {
		int r = X.length;
		int c = X[0].getDimensionContinuous();
		Double[] D = new Double[r];
		double dk, ddk;

		for (int i = 1; i <= r; i++) {
			dk = 0;
			for (int j = 1; j <= r; j++) {
				ddk = 0;
				if (i != j) {
					for (int k = 1; k <= c; k++) {
						ddk = ddk
								+ Math.pow((X[i - 1].getX(k - 1) - X[j - 1]
										.getX(k - 1)), (double) 2);
					}
					ddk = Math.pow(ddk, 0.5);
				}
				dk += ddk;
			}
			D[i - 1] = dk / (r - 1);
		}
		int mind = LinAlg.min(D);
		int maxd = LinAlg.max(D);
		return (D[minF_index] - D[mind]) / (D[maxd] - D[mind]);

	}

	protected double[] neighbour(Point s) {
		double[] coeff = LinAlg.multiply(T / T0, LinAlg.subtract(ub, lb));
		double[] ds = new double[dimCon];
		int i;
		for (i = 1; i <= dimCon; i++) {
			ds[i - 1] = (0.5 - RanGen.nextGaussian()) * 2;
		}
		ds = LinAlg.multiply(coeff, ds);
		double[] sn = new double[dimCon];
		for (i = 1; i <= dimCon; i++) {
			sn[i - 1] = s.getX(i - 1) + ds[i - 1];
		}

		for (i = 1; i <= dimCon; i++) {
			if (sn[i - 1] > ub[i - 1] || sn[i - 1] < lb[i - 1]) {
				sn[i - 1] = lb[i - 1] + RanGen.nextGaussian()
						* (ub[i - 1] - lb[i - 1]);
			}
		}
		return sn;
	}

	protected double prob(double e, double en, double T) {
		double P = Math.exp(-(en - e) / T * T0 / jump);
		isNaN(P);
		return P;
	}

	protected double[][] OTable;
	protected int row;
	protected int column;
	protected int Q = 9;
	protected int J = 3;
	protected int N = (int) (Math.pow(Q, J) - 1) / (Q - 1);
	protected double[] MM;
	protected double[][] V;
	protected double[][] FF;
	protected double w = 0.7;
	protected double G = 0.01;
	protected double T0 = 100;
	protected double Ti = 10;
	protected double Tf = 0;
	protected double T = Ti;
	protected double jump = 10;
	protected int nfmax = 600;
	/** The number of function values */
	protected int dimF;
	/** The number of independent continuous variables */
	protected int dimCon;
	/** The number of independent discrete variables */
	protected int dimDis;
	/** The number of particles */
	protected int NumPar;
	/** Random number generator */
	protected Random RanGen;
	protected double[] lb;
	protected double[] ub;
	/** Current population */
	protected Point[] X;
	/** Local best particles */
	protected Point[] P_X;
	/** Local best index uu */
	protected int minPF_index;
	/** Global best particles */
	protected Point P_Best;
	/** best fitness index kk */
	protected int minF_index;
	/** worst fitness index pp */
	protected int maxF_index;

}
