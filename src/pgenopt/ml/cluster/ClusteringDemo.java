package pgenopt.ml.cluster;

import pgenopt.math.Point;
import pgenopt.math.ResultPoint;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.XMeans;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

public class ClusteringDemo {
	
	Clusterer clusterer;
	
	Instances data;
	
	
	
	public ClusteringDemo(String filename, String[] args) throws Exception {
		// load data
		data = DataSource.read(filename);
		data.setClassIndex(- 1);
		
		// generate data for clusterer
		//filter (optional)
/*		
		Remove filter = new Remove();
	    filter.setAttributeIndices("" + (data.classIndex() + 1));
	    filter.setInputFormat(data);
	    Instances dataClusterer = Filter.useFilter(data, filter);
*/

		//train clusterer
		clusterer = new XMeans();
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
	
	public Point[] getClusterById(int id, Point[] points) {
		int num = 0;
		Point[] cluster = new Point[0];
		for (int i = 0; i < data.numInstances(); i++) {
			try {
				if (id == clusterer.clusterInstance(data.instance(i))) {
					Point[] pcluster = new Point[num];
					System.arraycopy(cluster, 0, pcluster, 0, num++);
					cluster = new Point[num];
					System.arraycopy(pcluster, 0, cluster, 0, num-1);
					cluster[num-1] = (Point) points[i].clone();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cluster;
	}

}
