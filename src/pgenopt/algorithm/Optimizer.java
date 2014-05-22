package pgenopt.algorithm;

import java.io.IOException;
import java.util.HashMap;

import lsgo_benchmark.Function;
import pgenopt.PGenOpt;
import pgenopt.math.Point;

abstract public class Optimizer {

	/** System dependent line separator */
	protected final static String LS = System.getProperty("line.separator");

	protected Optimizer(PGenOpt pgenopt) {
		data = pgenopt;
		dimCon = data.conPar.length;
		dimDis = data.disPar.length;
		dimX = dimCon + dimDis;
		f = data.getFunction();
		dimF = f.length;
		configureFromSetting();
	}

	public int runOptimizer() {
		int retFla;
		retFla = run();
		setResult();
		return retFla;
	}

	abstract public int run();

	abstract protected void setResult();

	protected void configureFromSetting() {
		parameter = data.getSetting();
	}

	protected String getInputValueString(final String keyWord,
			final String defaultVal) {
		String val = parameter.get(keyWord);
		return val == null ? defaultVal : val;
	}

	protected int getInputValueInteger(final String keyWord,
			final int defaultVal) {
		String val = parameter.get(keyWord);
		return val == null ? defaultVal : Integer.parseInt(val);
	}

	protected double getInputValueDouble(final String keyWord,
			final double defaultVal) {
		String val = parameter.get(keyWord);
		return val == null ? defaultVal : Double.parseDouble(val);
	}

	/**
	 * Gets the lower bound <CODE>l[i]</CODE> of the continuous variable
	 * <CODE>x[i]</CODE>
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the lower bound of the variable
	 */
	public double getL(final int i) {
		assert (i >= 0 && i < dimCon) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.conPar[i].getMinimum();
	}

	/**
	 * Gets the upper bound <CODE>u[i]</CODE> of the continuous variable
	 * <CODE>x[i]</CODE>
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the upper bound of the variable
	 */
	public double getU(final int i) {
		assert (i >= 0 && i < dimCon) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.conPar[i].getMaximum();
	}

	/**
	 * Gets the kind of constraint that is imposed on the i-th continuous
	 * variable
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the kind of constraint that is imposed on the parameter.<BR>
	 *         <B>Possible constraints:</B><BR>
	 * 
	 *         <pre>
	 * 1: no under boundary, no upper boundary
	 * 2: under boundary,    no upper boundary
	 * 3: under boundary,    upper boundary
	 * 4: no under boundary, upper boundary
	 * </pre>
	 */
	public int getKindOfConstraint(final int i) {
		assert (i >= 0 && i < dimCon) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.conPar[i].getKindOfConstraint();
	}

	/**
	 * Gets the number of independent variables (sum of continuous and discrete)
	 * 
	 * @return the number of independent variables (sum of continuous and
	 *         discrete)
	 */
	public final int getDimensionX() {
		return dimX;
	}

	/**
	 * Gets the number of independent, continuous variables
	 * 
	 * @return the number of independent, continuous variables
	 */
	public final int getDimensionContinuous() {
		return dimCon;
	}

	public final int getDimensionSubPopulation() {
		return data.getInitialPoint()[0].getDimensionContinuous();
	}

	/**
	 * Gets the number of independent, continuous variables
	 * 
	 * @return the number of independent, continuous variables
	 */
	public final int getDimensionDiscrete() {
		return dimDis;
	}

	/**
	 * Gets the number of function values
	 * 
	 * @return the number of function values
	 */
	public final int getDimensionF() {
		return dimF;
	}
	
	/**
	 * Gets the value of <CODE>x[i]</CODE><BR>
	 * <B>Note:</B> <CODE>x[i]</CODE> might be in the transformed space
	 * depending on the value of <CODE>constraints</CODE>
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the value of the variable
	 */
	public double getX(final int i) {
		assert (i >= 0 && i < dimCon) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.conPar[i].getOriginalValue();
	}

	/**
	 * Gets the value (i.e., the index) of <CODE>x[i]</CODE><BR>
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the index of the variable
	 */
	public int getIndex(final int i) {
		assert (i >= 0 && i < dimDis) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.disPar[i].getIndex();
	}

	/**
	 * Gets the double representation of the <CODE>variableNumber</CODE>-th
	 * <I>discrete</I> parameter.<BR>
	 * 
	 * If the variable represents discrete <I>numerical</I> values, then the
	 * double value of the currently selected value is returned.<BR>
	 * If the variable represents different <I>string</I> values, then the
	 * currently selected index is returned.
	 * <P>
	 * 
	 * <B>Note:</B> Prior to calling this method, you need to call either
	 * {@link #setIndex(int, int) setIndex(int, int)} (for each component), or
	 * {@link #getF(Point) getF(Point)}, or {@link #increaseStepNumber(Point)
	 * increaseStepNumber(Point)}. Either of these methods update the index of
	 * the discrete parameter.
	 * 
	 * @param variableNumber
	 *            the number of the variable (zero-based counter)
	 * @return the double representation of the currently selected value
	 */
	public double getDiscreteValueDouble(final int variableNumber) {
		assert (variableNumber >= 0 && variableNumber < dimDis) : "Wrong argument. Received 'i="
				+ variableNumber + "'";
		return data.disPar[variableNumber].getValueDouble();
	}

	/**
	 * Gets the values of the continuous variable <CODE>x</CODE><BR>
	 * <B>Note:</B> <CODE>x</CODE> might be in the transformed space depending
	 * on the value of <CODE>constraints</CODE>
	 * 
	 * @return the vector of the independent variables
	 */
	public double[] getX() {
		double[] r = new double[dimCon];
		for (int i = 0; i < dimCon; i++)
			r[i] = getX(i);
		return r;
	}

	/**
	 * Gets the indices of the discrete variable <CODE>x</CODE><BR>
	 * 
	 * @return the vector of indices of the independent variables
	 */
	public int[] getIndex() {
		int[] r = new int[dimDis];
		for (int i = 0; i < dimDis; i++)
			r[i] = getIndex(i);
		return r;
	}

	/**
	 * Gets the number of elements of the i-th discrete variable
	 * 
	 * @param i
	 *            the number of the variable (zero-based counter)
	 * @return the number of elements of the i-th discrete variable
	 */
	public final int getLengthDiscrete(final int i) {
		assert (i >= 0 && i < dimDis) : "Wrong argument. Received 'i=" + i
				+ "'";
		return data.disPar[i].length();
	}

	/**
	 * Checks whether a point is feasible.
	 * 
	 * @param x
	 *            the point to be checked
	 * @return <CODE>true</CODE> if point is feasible, <CODE>false</CODE>
	 *         otherwise
	 */
	protected boolean isFeasible(final Point x) {
		for (int i = 0; i < dimCon; i++) {
			final double xi = x.getX(i);
			if (xi > getU(i) || xi < getL(i)) {
				return false;
			}
		}
		for (int i = 0; i < dimDis; i++) {
			final int xi = x.getIndex(i);
			if (xi >= getLengthDiscrete(i) || xi < 0)
				return false;
		}
		return true;
	}

	/**
	 * Restricts the value of <code>x</code> such that <code>l <= x <= u</code>.
	 * 
	 * This method recursively reassigning <code>x := 2 * l - x</code> if
	 * <code>x < l</code>, or <code>x := 2 * u - x</code> if <code>x < u</code>.
	 * If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
	 * 
	 * @param x
	 *            the independent paramter
	 * @param l
	 *            the lower bound
	 * @param u
	 *            the upper bound
	 * @return a feasible value of <code>x</code>, such that
	 *         <code>l <= x <= u</code>
	 */
	static public double setToFeasibleCoordinate(double x, double l, double u) {
		assert (l < u);
		double xPre;
		do {
			xPre = x;
			// update coordinate until we are feasible
			x = _setToFeasibleCoordinate(x, l, u);
		} while (xPre != x);
		return xPre;
	}

	/**
	 * Computes <code>x := 2 * l - x</code> if <code>x < l</code>, or
	 * <code>x := 2 * u - x</code> if <code>x < u</code>. If <code>x</code> is
	 * feasible, then it returns <code>x</code> unmodified.
	 * 
	 * @param x
	 *            the independent paramter
	 * @param l
	 *            the lower bound
	 * @param u
	 *            the upper bound
	 * @return a feasible value of <code>x</code>, such that
	 *         <code>l <= x <= u</code>
	 */
	static private double _setToFeasibleCoordinate(double x, double l, double u) {
		if (x < l)
			return 2. * l - x;
		else if (x > u)
			return 2. * u - x;
		else
			return x;
	}

	/**
	 * Restricts the value of <code>x</code> such that <code>l <= x <= u</code>.
	 * 
	 * This method recursively reassigning <code>x := 2 * l - x</code> if
	 * <code>x < l</code>, or <code>x := 2 * u - x</code> if <code>x < u</code>.
	 * If <code>x</code> is feasible, then it returns <code>x</code> unmodified.
	 * 
	 * @param x
	 *            the independent paramter
	 * @param l
	 *            the lower bound
	 * @param u
	 *            the upper bound
	 * @return a feasible value of <code>x</code>, such that
	 *         <code>l <= x <= u</code>
	 */
	static public int setToFeasibleCoordinate(int x, int l, int u) {
		assert (l < u);
		int xPre;
		do {
			xPre = x;
			// update coordinate until we are feasible
			x = _setToFeasibleCoordinate(x, l, u);
		} while (xPre != x);
		return xPre;
	}

	/**
	 * Computes <code>x := 2 * l - x</code> if <code>x < l</code>, or
	 * <code>x := 2 * u - x</code> if <code>x < u</code>. If <code>x</code> is
	 * feasible, then it returns <code>x</code> unmodified.
	 * 
	 * @param x
	 *            the independent paramter
	 * @param l
	 *            the lower bound
	 * @param u
	 *            the upper bound
	 * @return a feasible value of <code>x</code>, such that
	 *         <code>l <= x <= u</code>
	 */
	static private int _setToFeasibleCoordinate(int x, int l, int u) {
		if (x < l)
			return 2 * l - x;
		else if (x > u)
			return 2 * u - x;
		else
			return x;
	}

	/**
	 * update point x's fitness
	 * 
	 * @param x
	 *            the point to update
	 */
	protected abstract void updateF(Point x);

	public final void isNaN(double d) {
		if (Double.isNaN(d)) {
			throw new IllegalArgumentException(Double.toString(d));
		}
	}

	/**
	 * Prints a message to the output device, and then finishs the line<BR>
	 * <B>Note:</B> Use this method instead of
	 * <CODE>System.out.println(String)</CODE>, otherwise it won't be reported
	 * in the GUI
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public void println(final String text) {
		assert text != null : "Received 'null' as argument";
		data.println(text);
	}

	/**
	 * Prints a ResultPoint
	 * 
	 * @param rp
	 *            ResultPoint to be printed
	 * @param iteration
	 */
	public void printPoint(Point p, int iteration) {
		String s = new String("");
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

	protected void report(Point[] points) {

	}

	/**
	 * Reports the minimum point.<BR>
	 * This method gets the minimum point from the data base, calls the function
	 * report with an corresponding comment and reports the minimum point to the
	 * output device.
	 * 
	 * @exception IOException
	 *                if an I/O error in the optimization output files occurs
	 */
	protected void reportMinimum(Point[] points) {
		xMin = getMinimumPoint(points);
		final String mes = LS + LS + "Minimum: f(x*) = " + xMin.getF(0) + LS;
		println(mes);
	}

	/**
	 * Gets the minimum point.<BR>
	 * This method gets the minimum point from the data base.
	 * 
	 * @return the point with the lowest function value
	 * @exception IOException
	 *                if an I/O error in the optimization output files occurs
	 */
	public Point getMinimumPoint(Point[] points) {
		int i, min = 0;
		double fmin = points[0].getF(0);

		for (i = 1; i < points.length; i++) {
			if (points[i].getF(0) < fmin) {
				min = i;
				fmin = points[i].getF(0);
			}
		}
		return points[min];
	}
	
	public Point getMinimumPoint() {
		return xMin;
	}

	/** PGenOpt data */
	protected PGenOpt data;
	/** PGenOpt setting */
	HashMap<String, String> parameter;
	/** object function */
	protected Function f[];
	/** The number of generations */
	protected int NumGen;
	/** Number of the current generation */
	protected int IGen;
	/** The number of independent, continuous variables */
	private int dimCon;
	/** The number of independent, discrete variables */
	private int dimDis;
	/** The number of independent variables (sum of continuous and discrete) */
	private int dimX;
	/** The number of cost function values */
	private int dimF;
	/** The name of the function values */
	private String[] nameF;
	/** The minimum point */
	private Point xMin;
}
