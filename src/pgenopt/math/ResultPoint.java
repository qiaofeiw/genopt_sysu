package pgenopt.math;

public class ResultPoint extends Point implements Comparable {
	/**
	 * Constructor
	 * 
	 * @param x
	 *            a ResultPoint
	 */
	public ResultPoint(ResultPoint x) {
		super(x.getX(), x.getIndex(), x.getF(), x.getStepNumber(), x
				.getComment());
		setSimulationNumber(x.getSimulationNumber(),
				x.getMainIterationNumber(), x.getSubIterationNumber());
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 *            a point
	 */
	public ResultPoint(Point x) {
		super(x.getX(), x.getIndex(), x.getF(), x.getStepNumber(), x
				.getComment());
	}

	/**
	 * Constructor
	 * 
	 * @param dimensionXContinuous
	 *            the number of continuous independent variables
	 * @param dimensionXDiscrete
	 *            the number of discrete independent variables
	 * @param dimensionF
	 *            the number of function values
	 */
	public ResultPoint(int dimensionXContinuous, int dimensionXDiscrete,
			int dimensionF) {
		super(dimensionXContinuous, dimensionXDiscrete, dimensionF);
	}

	/**
	 * sets the simulation number
	 * 
	 * @param simNumber
	 *            the simulation number
	 * @param mainIterationNumber
	 *            the main iteration number
	 * @param subIterationNumber
	 *            the sub iteration number
	 */
	public void setSimulationNumber(int simNumber, int mainIterationNumber,
			int subIterationNumber) {
		rSimNum = simNumber;
		rMaiIteNum = mainIterationNumber;
		rSubIteNum = subIterationNumber;
	}

	/**
	 * clones the ResultPoint
	 * 
	 * @return the clone of the ResultPoint
	 */
	public Object clone() {
		ResultPoint r = new ResultPoint((Point) super.clone());
		r.setSimulationNumber(rSimNum, rMaiIteNum, rSubIteNum);
		return r;
	}

	/**
	 * Compares this object with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * 
	 * @param o
	 *            Object to be compared
	 */
	public int compareTo(Object o) {
		ResultPoint rp = (ResultPoint) o;
		if (getF(0) < rp.getF(0))
			return -1;
		else if (getF(0) == rp.getF(0))
			return 0;
		else
			return 1;
	}

	/**
	 * gets the run number
	 * 
	 * @return the run number
	 */
	public int getSimulationNumber() {
		return rSimNum;
	}

	/**
	 * gets the main iteration number
	 * 
	 * @return the main iteration number
	 */
	public int getMainIterationNumber() {
		return rMaiIteNum;
	}

	/**
	 * gets the sub iteration number
	 * 
	 * @return the sub iteration number
	 */
	public int getSubIterationNumber() {
		return rSubIteNum;
	}

	/** run number */
	protected int rSimNum;
	/** main iteration number */
	protected int rMaiIteNum;
	/** sub iteration number */
	protected int rSubIteNum;
}
