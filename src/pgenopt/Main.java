package pgenopt;

import java.util.HashMap;

import lsgo_benchmark.F0;
import lsgo_benchmark.Function;

public class Main {

	private static HashMap<String, String> setting = new HashMap<String, String>();
	
	public static void main(String[] args) throws Exception {
		// Orthogonal.initialOrthogonalForm(9, 3, 32, -32);
		// for (int i = 0; i < 20; i++)
		// LinAlg.print(util.randomperm(20));
		// int[] perm = util.randomperm(20);
		// Integer[] Perm = new Integer[20];
		// for (int i = 0; i < perm.length; i++) {
		// Perm[i] = perm[i];
		// System.out.print(Perm[i] + " ");
		// }
		// System.out.println("\n"+Perm[LinAlg.min(Perm)]);
		Function[] f = new Function[1];
		setting.put("algorithm", "CWO");
		setting.put("NumGen", "3000000");
		setting.put("NumPar", "50");
		setting.put("NeighborhoodSize", "50");
		setting.put("Population Type", "MainPopulation");
		f[0] = new F0();
		PGenOpt genopt = new PGenOpt(f, setting);
		genopt.run();
//		Point[] result = genopt.getResultPoint();
//		Arff arff = util.convertToArff(result);
//		File data = arff.writeToFile("cwo.arff");
//		new ClusteringDemo("cwo.arff", null);
	}

}
