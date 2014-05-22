package pgenopt.util;

import java.util.HashMap;
import java.util.Random;

import pgenopt.format.Arff;
import pgenopt.math.Point;

public class util {
	
	/**
	 * Line separator
	 */
	private final static String LS = System.getProperty("line.separator");
	
	private static Random randn = new Random(System.currentTimeMillis());
	
	public static int[] randomperm(int n) {
		int[] p = new int[n];
		
		//generate identity permutation
		int i;
		for (i = 0; i < n; i++) {
			p[i] = i;
		}
		
		for (i = 0; i < n; i++) {
			int j = randn.nextInt(i+1);
			p[i] = p[j];
			p[j] = i;
		}
		return p;
	}
	
	public static Arff convertToArff(Point[] points) {
		String relation = "point";
		HashMap<String, String> attributes = new HashMap<String, String>();
		String[] data = new String[points.length];
		
		for (int i = 0; i < points[0].getDimensionContinuous(); i++) {
			attributes.put("dim" + i, "NUMERIC");
		}
		
		for (int i = 0; i < points.length; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < points[0].getDimensionContinuous(); j++) {
				sb.append(points[i].getX(j));
				if (i != points.length) {
					sb.append(", ");
				}
			}
			data[i] = sb.toString();
		}
		
		return new Arff(relation, attributes, data);
	}
	
	public static Arff convertToArff(double[] value) {
		String relation = "point";
		HashMap<String, String> attributes = new HashMap<String, String>();
		String[] data = new String[value.length];
		
		attributes.put("dim0", "NUMERIC");
		
		for (int i = 0; i < value.length; i++) {
			data[i] = Double.toString(value[i]);
		}
		
		return new Arff(relation, attributes, data);
	}
	
	/**
	 * prints a message to the output device without finishing the line
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public static void print(String text) {
		System.out.print(text);
		return;
	}

	// /////////////////////////////////////////////////////////////////////
	/**
	 * prints a message to the output device, and then finishs the line
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public static void println(String text) {
		print(text + LS);
		return;
	}
	
	public static <T> void printArray(T[][] array) {
		for(int i=0;i<array.length;i++){  
	        for(int j=0;j<array[i].length;j++){  
	            System.out.print(array[i][j]+"  ");  
	        }  
	        System.out.println();  
	    }
	}
	
//	public static void printArray(String[][] array){  
//	    for(int i=0;i<array.length;i++){  
//	        for(int j=0;j<array[i].length;j++){  
//	            System.out.print(array[i][j]+"  ");  
//	        }  
//	        System.out.println();  
//	    }  
//	}  

	// /////////////////////////////////////////////////////////////////////
	/**
	 * prints an error to the output device
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public static void printError(String text) {
		System.err.print(text);
	}


	/**
	 * Prints a ResultPoint
	 * 
	 * @param rp
	 *            ResultPoint to be printed
	 * @param iteration
	 */
	public static void printPoint(Point p, int iteration) {
		String s = new String("");
		int dimF = p.getDimensionF();
		int dimCon = p.getDimensionContinuous();
		s += "\t" + iteration;
		// // step number
		// s += "\t" + p.getStepNumber();
		// function values, coordinates, and comment
		for (int i = 0; i < dimF; i++)
			s += "\t" + p.getF(i);
		for (int i = 0; i < dimCon; i++)
			// continuous parameters
			s += "\t" + p.getX(i);
		if (p.getComment() == null)
			p.setComment("");
		s += "\t" + p.getComment() + LS;
		println(s);
	}

}
