package pgenopt.ml.cluster;

import pgenopt.math.Point;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.XMeans;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

public class KmeansGrouping {

	SimpleKMeans clusterer;

	Instances data;
	
	final int k;

	public KmeansGrouping(String filename, int K) throws Exception {
		// load data
		data = DataSource.read(filename);
		data.setClassIndex(-1);
		k = K;
		//train clusterer
		clusterer = new SimpleKMeans();
		clusterer.setNumClusters(k);
		clusterer.buildClusterer(data);
		
	    // evaluate clusterer
	    ClusterEvaluation eval = new ClusterEvaluation();
	    eval.setClusterer(clusterer);
	    eval.evaluateClusterer(data);

	    // print results
	    System.out.println(eval.clusterResultsToString());
	    
	    for (int i = 0; i < data.numInstances(); i++) {
	    	int cluster = clusterer.clusterInstance(data.instance(i));
	        double[] dist = clusterer.distributionForInstance(data.instance(i));
	        System.out.print((i+1));
	        System.out.print(" - ");
	        System.out.print(cluster);
	        System.out.print(" - ");
	        System.out.print(Utils.arrayToString(dist));
	        System.out.println();
	    }
	}
	
	public int getNumOfCluster() {
		try {
			return clusterer.numberOfClusters();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public int[][] getGroup() throws Exception {
		int ng = clusterer.getNumClusters();
		int[][] group = new int[ng][];
		int[] csize = clusterer.getClusterSizes();
		
		for (int i = 0; i < ng; i++) {
			group[i] = new int[csize[i]];
			int k = 0;
			for (int j = 0; j < data.numInstances(); j++) {
				if (i == clusterer.clusterInstance(data.instance(j))) {
					group[i][k++] = j;
				}
			}
		}
		
		return group;
	}
	
}
