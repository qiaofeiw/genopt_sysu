package pgenopt.algorithm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import pgenopt.PGenOpt;
import pgenopt.lang.OptimizerException;
import pgenopt.math.LinAlg;
import pgenopt.math.Point;
import pgenopt.util.util;

/**
 * Abstract class with model Particle Swarm Optimization algorithm.
 * <P>
 * 
 * <P>
 * <I>This project was carried out at:</I>
 * <UL>
 * <LI><A HREF="http://www.lbl.gov"> Lawrence Berkeley National Laboratory
 * (LBNL)</A>, <A HREF="http://simulationresearch.lbl.gov"> Simulation Research
 * Group</A>,
 * </UL>
 * </LI> <I>and supported by</I>
 * <UL>
 * <LI>the <A HREF="http://www.energy.gov"> U.S. Department of Energy (DOE)</A>,
 * <LI>the <A HREF="http://www.satw.ch"> Swiss Academy of Engineering Sciences
 * (SATW)</A>,
 * <LI>the Swiss National Energy Fund (NEFF), and
 * <LI>the <A HREF="http://www.snf.ch"> Swiss National Science Foundation
 * (SNSF)</A>
 * </UL>
 * </LI>
 * <P>
 * 
 * GenOpt Copyright (c) 1998-2008, The Regents of the University of California,
 * through Lawrence Berkeley National Laboratory (subject to receipt of any
 * required approvals from the U.S. Dept. of Energy). All rights reserved.
 * 
 * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
 * 
 * @version GenOpt(R) 2.1.0 (May 29, 2008)
 *          <P>
 */

/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * (2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * (3) Neither the name of the University of California, Lawrence Berkeley
 * National Laboratory, U.S. Dept. of Energy nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * You are under no obligation whatsoever to provide any bug fixes, patches, or
 * upgrades to the features, functionality or performance of the source code
 * ("Enhancements") to anyone; however, if you choose to make your Enhancements
 * available either publicly, or directly to Lawrence Berkeley National
 * Laboratory, without imposing a separate written license agreement for such
 * Enhancements, then you hereby grant the following license: a non-exclusive,
 * royalty-free perpetual license to install, use, modify, prepare derivative
 * works, incorporate into other computer software, distribute, and sublicense
 * such enhancements or derivative works thereof, in binary and source code
 * form.
 */

abstract public class ModelPSO extends Optimizer {
	/** Constant to denote the <I>gbest</I> neighborhood topology */
	private static final int GBEST = 0;
	/** Constant to denote the <I>lbest</I> neighborhood topology */
	private static final int LBEST = 1;
	/** Constant to denote the <I>von Neumann</I> neighborhood topology */
	private static final int VONNEUMANN = 2;

	/**
	 * Constructor
	 * 
	 * @param genOptData
	 *            a reference to the GenOpt object.<BR>
	 *            <B>Note:</B> the object is used as a reference. Hence, the
	 *            data of GenOpt are modified by this Class.
	 * @exception OptimizerException
	 *                if an OptimizerException occurs or if the user required to
	 *                stop GenOpt
	 * @exception SimulationInputException
	 *                if an error in writing the simulation input file occurs
	 * @exception NoSuchMethodException
	 *                if a method that should be invoked could not be found
	 * @exception IllegalAccessException
	 *                if an invoked method enforces Java language access control
	 *                and the underlying method is inaccessible
	 * @exception InvocationTargetException
	 *                if an invoked method throws an exception
	 * @exception Exception
	 *                if an I/O error in the simulation input file occurs
	 */
	public ModelPSO(PGenOpt genOptData) throws OptimizerException, IOException,
			Exception {

		super(genOptData);

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

		final String[] neiVals = { "gbest", "lbest", "vonNeumann" };
		final String neiTop = getInputValueString("NeighborhoodTopology",
				"lbest");
		NeiHooTop = -1;
		if (neiTop.equals("gbest"))
			NeiHooTop = GBEST;
		else if (neiTop.equals("lbest"))
			NeiHooTop = LBEST;
		else if (neiTop.equals("vonNeumann"))
			NeiHooTop = VONNEUMANN;
		assert (NeiHooTop != -1);

		NeiHooSiz = getInputValueInteger("NeighborhoodSize", 10);

		NumPar = getInputValueInteger("NumPar", 50);
		if (NeiHooTop == GBEST) {
			NeiHooSiz = NumPar / 2;
		} else if (NeiHooTop == VONNEUMANN) {
			double o = (double) (NumPar);
			o = StrictMath.sqrt(o);
			int nSq = (int) (StrictMath.ceil(o));
			NumPar = nSq * nSq;
		}

		NumGen = getInputValueInteger("NumGen", 60000);

		final long seed = (long) getInputValueInteger("Seed",
				(int) System.currentTimeMillis());

		CogAcc = getInputValueDouble("CognitiveAcceleration", 2.05);

		SocAcc = getInputValueDouble("SocialAcceleration", 2.05);

		MaxVelGaiCon = getInputValueDouble("MaxVelocityGainContinuous", 0.5);

		MaxVelDis = getInputValueDouble("MaxVelocityDiscrete", 4);

		// ///////////////////////////////////////////////////
		RanGen = new Random(seed);

		LenBitStr = new int[dimDis];
	}

	/**
	 * Runs the optimization process until a termination criteria is satisfied
	 * 
	 * @return <CODE>-1</CODE> if the maximum number of iteration is exceeded <BR>
	 *         <CODE>+1</CODE> if the required accuracy is reached
	 * @exception SimulationInputException
	 *                if an error in writing the simulation input file occurs
	 * @exception NoSuchMethodException
	 *                if a method that should be invoked could not be found
	 * @exception IllegalAccessException
	 *                if an invoked method enforces Java language access control
	 *                and the underlying method is inaccessible
	 * @exception InvocationTargetException
	 *                if an invoked method throws an exception
	 */
	public int run() {

		int retFla;
		// //////////////////////////////
		// initialize
		// current population
		if (data.isInitialContinuousParameter()) {
			NumPar = data.getInitialPoint().length;
			initializeCurrentPopulation(data.getInitialPoint());
		} else {
			randomInitializeCurrentPopulation();
		}
		// local best
		LocBes = new Point[NumPar];
		// global best
		GloBes = new Point[NumPar];

		System.arraycopy(CurPop, 0, LocBes, 0, NumPar);
		// neighborhood of each particle
		initializeNeighborhoodIndices();
		// //////////////////////////////
		// particle velocity, continuous and discrete parameter
		VelCon = new double[NumPar][dimCon];
		VelDis = new double[NumPar][dimDis][];
		for (int i = 0; i < NumPar; i++) {
			for (int j = 0; j < dimCon; j++)
				VelCon[i][j] = 0;
		}

		for (int i = 0; i < NumPar; i++) {
			for (int j = 0; j < dimDis; j++) {
				VelDis[i][j] = new double[LenBitStr[j]];
				for (int k = 0; k < LenBitStr[j]; k++)
					VelDis[i][j][k] = 0;
			}
		}
		// maximum velocities used for clamping
		// - continuous parameter
		VelMaxCon = new double[dimCon];
		for (int i = 0; i < dimCon; i++) {
			VelMaxCon[i] = (MaxVelGaiCon > 0) ? MaxVelGaiCon * (ub[i] - lb[i])
					: Double.MAX_VALUE;
		}
		// //////////////////////////////
		// optimization loop
		IGen = 0;
		boolean iterate = true;

		do {
			IGen++;
			for (int iP = 0; iP < NumPar; iP++) {
				// evaluate cost function
				updateF(CurPop[iP]);
			}
			if (IGen >= NumGen)
				iterate = false;
			else {
				// //////////////////////////////
				// get local best
				if (IGen == 1) // first generation
					System.arraycopy(CurPop, 0, LocBes, 0, NumPar);

				updateLocalBest();
				updateGlobalBest();
				updateVelocity();
				updateParticleLocation();
				reportMinimum(GloBes);

			}
		} while (iterate);
		for (int i = 0; i < GloBes.length; i++) {
			System.out.println(LocBes[i].getF(0));
		}
		reportMinimum(GloBes);
		retFla = 1;
		return retFla;
	}

	/**
	 * Updates the particle location
	 */
	private void updateParticleLocation() {
		for (int iP = 0; iP < NumPar; iP++) {
			// continuous parameter
			if (dimCon > 0) {
				CurPop[iP].setX(LinAlg.add(CurPop[iP].getX(), VelCon[iP]));
				// set to feasible point
				for (int i = 0; i < dimCon; i++) {
					final double xFea = Optimizer.setToFeasibleCoordinate(
							CurPop[iP].getX(i), lb[i], ub[i]);
					CurPop[iP].setX(i, xFea);
				}
			}
			// discrete parameter
			if (dimDis > 0) {
				for (int iV = 0; iV < dimDis; iV++) { // loop over variables
					int[] bs = new int[LenBitStr[iV]];
					for (int iB = 0; iB < LenBitStr[iV]; iB++) { // loop over
																	// bit
																	// stream
						final double ranDis = RanGen.nextDouble();
						final double s = 1. / (1. + StrictMath
								.exp(-VelDis[iP][iV][iB]));
						bs[iB] = (ranDis >= s) ? 0 : 1;
					}
					CurPop[iP].setGrayBinaryString(iV, bs);
				}
			} // end loop for discrete variables
		} // end loop for over all particles
	}

	/**
	 * Updates the particle velocity
	 */
	abstract protected void updateVelocity();

	/**
	 * Clamps the velocity
	 * 
	 * @param velocity
	 *            particle velocity
	 * @param max
	 *            maximum absolute value of the velocity. If
	 *            <code>max <= 0</code> then the value of <code>velocity</code>
	 *            is returned.
	 * @return velocity clamp to admissible interval
	 */
	protected static final double clampVelocity(final double velocity,
			final double max) {
		if (max > 0) {
			if (velocity > 0)
				return StrictMath.min(velocity, max);
			else
				return StrictMath.max(velocity, -max);
		} else
			return velocity;
	}

	/**
	 * Updates the global best particles based on the local best particles
	 */
	private void updateGlobalBest() {
		for (int iP = 0; iP < NumPar; iP++) {
			int igb = NeiHooInd[iP][0]; // index to global best
			// loop over all neighbors
			for (int iN = 1; iN < NeiHooInd[iP].length; iN++) {
				if (LocBes[igb].getF(0) > LocBes[NeiHooInd[iP][iN]].getF(0))
					igb = NeiHooInd[iP][iN];
			}
			GloBes[iP] = (Point) LocBes[igb].clone();
		}
	}

	/**
	 * Updates the local best particles based on the current population
	 */
	private final void updateLocalBest() {
		for (int i = 0; i < NumPar; i++) {
			if (LocBes[i].getF(0) > CurPop[i].getF(0)) {
				LocBes[i] = (Point) CurPop[i].clone();
			}
		}
	}

	private void randomInitializeCurrentPopulation() {
		double[][] pop = new double[NumPar][dimCon];
		double xmax = f[0].getMax();
		double xmin = f[0].getMin();
		CurPop = new Point[NumPar];

		for (int i = 0; i < NumPar; i++) {
			CurPop[i] = new Point(dimCon, 0, 1);
			for (int j = 0; j < dimCon; j++) {
				final double r = RanGen.nextDouble();
				final double x = xmin + r * (xmax - xmin);
				pop[i][j] = x;
			}
			CurPop[i].setX(pop[i]);
		}

	}

	/**
	 * Initializes the current population
	 */
	private void initializeCurrentPopulation(Point[] points) {
		CurPop = new Point[NumPar];
		int i;
		for (i = 0; i < NumPar; i++) {
			CurPop[i] = (Point) points[i];
		}
	}

	/**
	 * Initializes the current population
	 */
	private void initializeCurrentPopulation() {
		CurPop = new Point[NumPar];
		CurPop[0] = new Point(dimCon, dimDis, dimF);
		// initialize first point as in input file
		CurPop[0].setX(getX());
		CurPop[0].setIndex(getIndex());

		double[] dDom = new double[dimCon];
		for (int i = 0; i < dimCon; i++) {
			dDom[i] = ub[i] - lb[i];
		}

		for (int iP = 1; iP < NumPar; iP++)
			CurPop[iP] = new Point(dimCon, dimDis, dimF);

		// set maximum value for all variables
		for (int iV = 0; iV < dimDis; iV++) {
			final int maxVal = getLengthDiscrete(iV) - 1; // -1 because if
															// length=2, can
															// have values 0 and
															// 1
			for (int iP = 0; iP < NumPar; iP++)
				CurPop[iP].setMaximumIndex(iV, maxVal);
		}

		// get length of binary stream
		for (int j = 0; j < dimDis; j++)
			LenBitStr[j] = CurPop[0].getGrayBinaryStringLength(j);

		// initialize other parameters randomly
		for (int iP = 1; iP < NumPar; iP++) {
			// continuous parameter
			for (int iV = 0; iV < dimCon; iV++) {
				final double r = RanGen.nextDouble();
				final double x = getL(iV) + r * dDom[iV];
				CurPop[iP].setX(iV, x);
			}
			// discrete parameter
			if (dimDis > 0) {
				for (int iV = 0; iV < dimDis; iV++) {
					final int r = RanGen.nextInt(getLengthDiscrete(iV));
					CurPop[iP].setIndex(iV, r);
				}
			}
		}
	}

	/**
	 * Initializes the neighborhood indices for each particle
	 */
	private void initializeNeighborhoodIndices() {
		NeiHooInd = new int[NumPar][];
		for (int iP = 0; iP < NumPar; iP++) {
			int[] r;
			switch (NeiHooTop) {
			case LBEST:
				r = new int[2 * NeiHooSiz + 1];
				int k = 0;
				for (int j = iP - NeiHooSiz; j <= iP + NeiHooSiz; j++, k++) {
					r[k] = j;
					// flip index
					while (r[k] < 0)
						r[k] += NumPar;
					while (r[k] >= NumPar)
						r[k] -= NumPar;
				}
				break;
			case GBEST:
				r = new int[NumPar];
				for (int j = 0; j < NumPar; j++)
					r[j] = j;
				break;
			case VONNEUMANN:
				r = new int[5];
				final int offSet = (int) (StrictMath.rint(StrictMath
						.sqrt((double) NumPar)));
				assert (offSet * offSet) == NumPar;
				final int i0 = iP / offSet;
				final int j0 = iP - i0 * offSet;
				r[0] = _getVonNeumannIndex(i0 - 1, j0, offSet);
				r[1] = _getVonNeumannIndex(i0, j0 - 1, offSet);
				r[2] = iP;
				r[3] = _getVonNeumannIndex(i0, j0 + 1, offSet);
				r[4] = _getVonNeumannIndex(i0 + 1, j0, offSet);
				// flip index
				for (int j = 0; j < r.length; j++) {
					while (r[j] < 0)
						r[j] += NumPar;
					while (r[j] >= NumPar)
						r[j] -= NumPar;
				}
				break;
			default:
				assert false : "Wrong value of neighborhood topology: NeiHooTop = "
						+ NeiHooTop;
				r = new int[0]; // to satisfy the compiler
			}
			NeiHooInd[iP] = r;
		}
	}

	/**
	 * Computes the index of a point used in the von Neumann neighborhood
	 * 
	 * @param i
	 *            zero-based row index
	 * @param j
	 *            zero-based column index
	 * @param offSet
	 *            off set of index
	 * @return the zero-based particle index
	 */
	private final int _getVonNeumannIndex(final int i, final int j,
			final int offSet) {
		int k = i;
		while (k < 0)
			k += offSet;
		while (k >= offSet)
			k -= offSet;
		int l = j;
		while (l < 0)
			l += offSet;
		while (l >= offSet)
			l -= offSet;
		return k * offSet + l;
	}

	/** Neighborhood topology */
	private int NeiHooTop;
	/** Neighborhood size */
	private int NeiHooSiz;
	/** Index with neighborhood points for each particle */
	private int[][] NeiHooInd;
	/** The number of independent continuous variables */
	protected int dimCon;
	/** The number of independent discrete variables */
	protected int dimDis;
	/**
	 * Length of the bit string used to encode each component of the discrete
	 * parameters
	 */
	protected int[] LenBitStr;
	/** The number of function values */
	protected int dimF;
	/** The number of particles */
	protected int NumPar;
	/** The number of generations */
	protected int NumGen;
	/** Number of the current generation */
	protected int IGen;
	/** Random number generator */
	protected Random RanGen;
	/** Acceleration constant for cognitive component */
	protected double CogAcc;
	/** Acceleration constant for social component */
	protected double SocAcc;
	/** Gain for maximal velocity of continuous parameters */
	private double MaxVelGaiCon;
	/** Maximum velocity for discrete parameter */
	protected double MaxVelDis;
	/** Maximum velocity for each component of the continuous parameter */
	protected double[] VelMaxCon;
	/** Velocity of the continuous particles */
	protected double[][] VelCon;
	/** Velocity of the discrete particles */
	protected double[][][] VelDis;
	/** Current population */
	protected Point[] CurPop;
	/** Local best particles */
	protected Point[] LocBes;
	/** Global best particles */
	protected Point[] GloBes;
	/** the lower bound of variables */
	protected double[] lb;
	/** the upper bound of variables */
	protected double[] ub;
}
