package pgenopt;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

import lsgo_benchmark.Function;
import pgenopt.algorithm.Optimizer;
import pgenopt.lang.OptimizerException;
import pgenopt.math.ContinuousParameter;
import pgenopt.math.DiscreteParameter;
import pgenopt.math.Point;
import pgenopt.math.ResultPoint;
import scala.languageFeature.postfixOps;

public class PGenOpt {

	/**
	 * Line separator
	 */
	private final static String LS = System.getProperty("line.separator");

	private HashMap<String, String> setting;
	
	private Point[] resPoi;

	public PGenOpt(String optIniFileName) {
		super();
		f = new Function[0];
		instantiateFreePar();
	}

	public PGenOpt(Function[] function, HashMap<String, String> setting) {
		f = function;
		this.setting = setting;
		instantiateFreePar();
	}
	
	public PGenOpt(Function[] function, HashMap<String, String> setting, Point[] points) {
		f = function;
		this.setting = setting;
		isInitailPoint = true;
		ipoints = points;
		instantiateFreePar();
	}
	
	public PGenOpt(Function[] function, HashMap<String, String> setting, Point[] points, Point context) {
		f = function;
		this.setting = setting;
		isInitailPoint = true;
		ipoints = points;
		this.context = context;
		instantiateFreePar();
	}

	public Function[] getFunction() {
		return f;
	}

	private void initializeOptimization(String optIniFileName) {

	}

	public HashMap<String, String> getSetting() {
		return setting;
	}

	public void setSetting(HashMap<String, String> setting) {
		this.setting = setting;
	}

	/**
	 * runs the optimization method
	 * @throws OptimizerException 
	 */
	public void run() throws OptimizerException {
		try {
			int flag;
			flag = this.getOptimizer().runOptimizer();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Point[] getInitialPoint() {
		return ipoints;
	}
	
	public Point getContext() {
		return context;
	}
	
	public boolean isInitialContinuousParameter() {
		return isInitailPoint;
	}
	
	public Point[] getResultPoint() throws OptimizerException {
		if (resPoi == null) throw new OptimizerException("call this method after run()."); 
		return resPoi;
	}
	
	public void setResult(Point[] result) {
		resPoi = result;
	}

	/**
	 * gets a new instance of the Optimizer object
	 * @throws OptimizerException 
	 */
	private Optimizer getOptimizer() throws InstantiationException,
			ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, IOException, OptimizerException {
		final String maiAlg = this.getMainAlgorithm();

		final Class cl = Class.forName("pgenopt.algorithm." + maiAlg);
		Class[] arg = new Class[1];
		arg[0] = Class.forName("pgenopt.PGenOpt");
		try {
			final Constructor co = cl.getConstructor(arg);
			Object[] ob = new Object[1];
			ob[0] = this;
			final Optimizer o = (Optimizer) co.newInstance(ob);
			// o.goToEndOfCommandFile(); // to ensure that all the parameters
			// are parsed properly
			return o;
		} catch (NoSuchMethodException nsme) {
			final String em = new String(
					"Error in Optimization Code: Class '"
							+ maiAlg
							+ "' must have a constructor with argument 'pgenopt.PGenOpt'.");
			InstantiationException e = new InstantiationException(em);
			throw e;
		}
	}

	private String getMainAlgorithm() throws OptimizerException {
		String algorithm = setting.get("algorithm");
		if (algorithm ==  null) throw new OptimizerException("must specified algorithm");
		return algorithm;
	}
	
	public Point getBestPoint() throws Exception {
		Point best = this.getOptimizer().getMinimumPoint();
		if (best == null) {
			throw new OptimizerException("Error in getting minimum, did you call run?");
		}
		return best;
	}

	/**
	 * makes all instances of FreePar and input functions
	 */
	private void instantiateFreePar() {
		dimCon = f[0].getDimension();
		conPar = new ContinuousParameter[dimCon];
		for (int i = 0; i < dimCon; i++) {
			conPar[i] = new ContinuousParameter("x" + i, 0, f[0].getMax(),
					f[0].getMin(), step, 1);
		}
		dimDis = 0;
		disPar = new DiscreteParameter[dimDis];
	}
	
	// /////////////////////////////////////////////////////////////////////
	/**
	 * prints a message to the output device without finishing the line
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public void print(String text) {
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
	public void println(String text) {
		print(text + LS);
		return;
	}

	// /////////////////////////////////////////////////////////////////////
	/**
	 * prints an error to the output device
	 * 
	 * @param text
	 *            the text to be printed
	 */
	public void printError(String text) {
		System.err.print(text);
	}

	protected Function f[];
	/** dimension of the continuous parameters */
	protected int dimCon;
	/** dimension of the discrete parameters */
	protected int dimDis;
	/** is initial points */
	protected boolean isInitailPoint;
	/** initial points */
	protected Point[] ipoints;
	/** the best point in main population */
	protected Point context;
	/** continuous parameters */
	public ContinuousParameter conPar[];
	/** discrete parameters */
	public DiscreteParameter disPar[];
	/** step size */
	public double step;

}
