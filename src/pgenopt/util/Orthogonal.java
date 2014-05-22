package pgenopt.util;

import pgenopt.math.LinAlg;

public class Orthogonal {
	
	public static double[][] initialOrthogonalForm(int Q, int J) {
		int row = (int) Math.pow(Q, J);
		int N1 = (row-1)/(Q-1);
		int col = N1;
		double form[][] = new double[row][col];
		int i, j, k;
		
		double A1[][] = new double[row][N1];
		
		for (i = 1; i <= row; i++) {
			for (k = 1; k <= J; k++) {
				j=(int)((double)(Math.pow(Q, k-1) - 1)/(Q - 1) + 1);
				A1[i-1][j-1] = (int) (Math.floor(Math.abs((i-1)/Math.pow(Q, J-k))) % Q);
			}
		}
//		LinAlg.print(A1);
		
		for (k = 2; k <= J; k++) {
			j = (int)(Math.pow(Q, k-1) - 1) / (Q-1) + 1;
			for (int s = 1; s <= j-1; s++) {
				for (int t = 1; t <= Q - 1; t++) {
					double c[][] = LinAlg.multiplyCol(A1, s-1, t);
					double c1[] = LinAlg.add(c, A1, s-1, j-1);
					double c2[] = LinAlg.mod(c1, Q);
					A1 = LinAlg.setColumn(A1, c2, j+(s-1)*(Q-1)+t-1);
 				}
			}
		}
//		LinAlg.print(A1);
		
		for (i = 1; i <= row; i++) {
			for (j = 1; j <= N1; j++) {
				A1[i-1][j-1] = A1[i-1][j-1] + 1;
			}
		}
		
		return A1;
	}
	
	/** initial an Orthogonal Form of continuous
	 * @param Q
	 * @param J
	 * @return
	 */
	public static double[][] initialOrthogonalConForm(int Q, int J, double xmax, double xmin) {
		int row = (int) Math.pow(Q, J);
		int N1 = (row-1)/(Q-1);
		int col = N1;
		double form[][] = new double[row][col];
		int i, j, k;
		
		double A1[][] = new double[row][N1];
		
		for (i = 1; i <= row; i++) {
			for (k = 1; k <= J; k++) {
				j=(int)((double)(Math.pow(Q, k-1) - 1)/(Q - 1) + 1);
				A1[i-1][j-1] = (int) (Math.floor(Math.abs((i-1)/Math.pow(Q, J-k))) % Q);
			}
		}
//		LinAlg.print(A1);
		
		for (k = 2; k <= J; k++) {
			j = (int)(Math.pow(Q, k-1) - 1) / (Q-1) + 1;
			for (int s = 1; s <= j-1; s++) {
				for (int t = 1; t <= Q - 1; t++) {
					double c[][] = LinAlg.multiplyCol(A1, s-1, t);
					double c1[] = LinAlg.add(c, A1, s-1, j-1);
					double c2[] = LinAlg.mod(c1, Q);
					A1 = LinAlg.setColumn(A1, c2, j+(s-1)*(Q-1)+t-1);
 				}
			}
		}
//		LinAlg.print(A1);
		
		for (i = 1; i <= row; i++) {
			for (j = 1; j <= N1; j++) {
				A1[i-1][j-1] = A1[i-1][j-1] + 1;
			}
		}
//		LinAlg.print(A1);

		double g  = (xmax - xmin) / Q;
		double[][] l = new double[Q][N1];
		double[][] u = new double[Q][N1];
		double[][] a = new double[Q][N1];
		for (i = 1; i <= Q; i++) {
			for (j = 1; j <= N1; j++) {
				l[i-1][0] = xmin + (i-1)*g;
				u[i-1][0] = xmin +i*g;
				a[i-1][j-1] = l[i-1][0] + 0.5 * (u[i-1][0] - l[i-1][0]);
			}
		}
		
		for (i = 1; i <= row; i++) {
			for (j = 1; j <= N1; j++) {
				form[i-1][j-1] = a[(int)A1[i-1][j-1]-1][j-1];
			}
		}
//		LinAlg.print(form);

		return form;
	}

}
