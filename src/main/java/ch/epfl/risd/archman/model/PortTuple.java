package ch.epfl.risd.archman.model;

/**
 * This class is representing one port tuple in one connector tuple. It contains
 * the name of the port instance (in format C.b, where the C is the name of the
 * component, and b is the name of port instantiated in that component), the
 * multiplicity and the degree of the port instance.
 */
public class PortTuple {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* enumeration for declaring the port tuple type */
	public enum PortTupleType {
		COORDINATOR_TUPLE, OPERAND_TUPLE
	}

	/* The name of the port instance */
	private String portInstanceName;

	/* The multiplicity of the port */
	private String multiplicity;

	/* The degree of the port */
	private String degree;

	/* The type of the tuple */
	private PortTupleType type;

	/* Flag indicating whether the multiplicity is calculated or not */
	private boolean isMultiplicityCalculated;

	/* Flag indicating whether the degree is calculated or not */
	private boolean isDegreeCalculated;

	/* The calculated multiplicity */
	private int calculatedMultiplicity;

	/* The calculated degree */
	private int calculatedDegree;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method checks whether the provided string is number or not
	 * 
	 * @param str
	 *            - The string for checking
	 * @return true if the string is number, false otherwise
	 */
	private static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor
	 * 
	 * @param portInstanceName
	 *            - The name of the port instance
	 * @param multiplicity
	 *            - The multiplicity of the port
	 * @param degree
	 *            - The degree of the port
	 */
	public PortTuple(String portInstanceName, String multiplicity, String degree, PortTupleType type) {
		this.portInstanceName = portInstanceName;
		this.multiplicity = multiplicity;
		this.degree = degree;
		this.type = type;

		/* Check whether the multiplicity is parametric or not */
		if (isNumeric(this.multiplicity)) {
			this.calculatedMultiplicity = Integer.parseInt(this.multiplicity);
			this.isMultiplicityCalculated = true;
		} else {
			this.isMultiplicityCalculated = false;
		}

		/* Check whether the degree is parametric or not */
		if (isNumeric(this.degree)) {
			this.calculatedDegree = Integer.parseInt(this.degree);
			this.isDegreeCalculated = true;
		} else {
			this.isDegreeCalculated = false;
		}

	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof PortTuple) {
			/* Cast the object to Port Tuple */
			PortTuple temp = ((PortTuple) obj);

			/* If the three conditions hold */
			if (this.portInstanceName.equals(temp.portInstanceName) && this.multiplicity.equals(temp.multiplicity)
					&& this.degree.equals(temp.degree)) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Port instance name: " + this.portInstanceName + " Multiplicity:" + this.multiplicity + " Degree:"
				+ this.degree + " Type: "
				+ ((this.type == PortTupleType.COORDINATOR_TUPLE) ? "Coordinator tuple" : "Operand tuple");
	}

	/**
	 * @return the name of the port instance
	 */
	public String getPortInstanceName() {
		return portInstanceName;
	}

	/**
	 * @return the multiplicity of the port
	 */
	public String getMultiplicity() {
		return multiplicity;
	}

	/**
	 * @return the degree of the port
	 */
	public String getDegree() {
		return degree;
	}

	/**
	 * @return true if the multiplicity is calculated, false otherwise
	 */
	public boolean isMultiplicityCalculated() {
		return isMultiplicityCalculated;
	}

	/**
	 * Set the value of the flag for calculated multiplicity
	 * 
	 * @param isMultiplicityCalculated
	 *            - The value of flag
	 */
	public void setMultiplicityCalculated(boolean isMultiplicityCalculated) {
		this.isMultiplicityCalculated = isMultiplicityCalculated;
	}

	/**
	 * @return true if the degree is calculated, false otherwise
	 */
	public boolean isDegreeCalculated() {
		return isDegreeCalculated;
	}

	/**
	 * Set the value of the flag for calculated degree
	 * 
	 * @param isDegreeCalculated
	 *            - The value of the flag
	 */
	public void setDegreeCalculated(boolean isDegreeCalculated) {
		this.isDegreeCalculated = isDegreeCalculated;
	}

	/**
	 * @return the tuple of the Port Tuple
	 */
	public PortTupleType getType() {
		return type;
	}

	/**
	 * Set the value of the multiplicity
	 * 
	 * @param calculatedMultiplicity
	 *            - The value of the multiplicity
	 */
	public void setCalculatedMultiplicity(int calculatedMultiplicity) {
		this.calculatedMultiplicity = calculatedMultiplicity;
		this.isMultiplicityCalculated = true;
	}

	/**
	 * Set the value of the degree
	 * 
	 * @param calculatedDegree
	 *            - The value of the degree
	 */
	public void setCalculatedDegree(int calculatedDegree) {
		this.calculatedDegree = calculatedDegree;
		this.isDegreeCalculated = true;
	}

	/**
	 * @return the calculated multiplicity
	 */
	public int getCalculatedMultiplicity() {
		return calculatedMultiplicity;
	}

	/**
	 * @return the calculated degree
	 */
	public int getCalculatedDegree() {
		return calculatedDegree;
	}

}
