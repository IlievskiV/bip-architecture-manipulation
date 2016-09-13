package ch.epfl.risd.archman.model;

import ch.epfl.risd.archman.helper.HelperMethods;

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

	/* The prefix for the multiplicity term default name */
	public static final String MULT_DEFAULT_NAME_PREFIX = "mult_";

	/* The prefix for the degree term default name */
	public static final String DEG_DEFAULT_NAME_PREFIX = "deg_";

	/* enumeration for declaring the port tuple type */
	public enum PortTupleType {
		COORDINATOR_TUPLE, OPERAND_TUPLE
	}

	/* The name of the port instance */
	protected String portInstanceName;

	/* The term for the multiplicity */
	protected NameValue multiplicityTerm;

	/* The term for the degree */
	protected NameValue degreeTerm;

	/* The type of the tuple */
	protected PortTupleType type;

	/* Flag for the trigger port */
	protected boolean isTrigger;

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

		/* If the port is trigger */
		if (portInstanceName.charAt(portInstanceName.length() - 1) == '\'') {
			this.portInstanceName = portInstanceName.substring(0, portInstanceName.length() - 1);
			this.isTrigger = true;
		} else {
			this.portInstanceName = portInstanceName;
			this.isTrigger = false;
		}

		this.type = type;

		/* Check whether the multiplicity is constant or variable */
		if (HelperMethods.isNumeric(multiplicity)) {
			this.multiplicityTerm = new NameValue(MULT_DEFAULT_NAME_PREFIX + portInstanceName,
					Integer.parseInt(multiplicity));
		} else {
			this.multiplicityTerm = new NameValue(MULT_DEFAULT_NAME_PREFIX + portInstanceName);
		}

		/* Check whether the degree is parametric or not */
		if (HelperMethods.isNumeric(degree)) {
			this.degreeTerm = new NameValue(DEG_DEFAULT_NAME_PREFIX + portInstanceName, Integer.parseInt(degree));
		} else {
			this.degreeTerm = new NameValue(DEG_DEFAULT_NAME_PREFIX + portInstanceName);
		}

	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof PortTuple) {
			/* Cast the object to Port Tuple */
			PortTuple temp = ((PortTuple) obj);

			/* If the three conditions hold */
			if (this.portInstanceName.equals(temp.portInstanceName)
					&& this.multiplicityTerm.equals(temp.multiplicityTerm) && this.degreeTerm.equals(temp.degreeTerm)) {
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
		return "Port instance name: " + this.portInstanceName + " Multiplicity:" + this.multiplicityTerm.value
				+ " Degree:" + this.degreeTerm.value + " Type: "
				+ ((this.type == PortTupleType.COORDINATOR_TUPLE) ? "Coordinator tuple" : "Operand tuple");
	}

	public NameValue getMultiplicityTerm() {
		return this.multiplicityTerm;
	}

	public NameValue getDegreeTerm() {
		return this.degreeTerm;
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
		return this.multiplicityTerm.getName();
	}

	/**
	 * @return the degree of the port
	 */
	public String getDegree() {
		return this.degreeTerm.getName();
	}

	/**
	 * @return true if the multiplicity is calculated, false otherwise
	 */
	public boolean isMultiplicityCalculated() {
		return multiplicityTerm.isCalculated();
	}

	/**
	 * @return true if the degree is calculated, false otherwise
	 */
	public boolean isDegreeCalculated() {
		return degreeTerm.isCalculated();
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
		this.multiplicityTerm.setValue(calculatedMultiplicity);
	}

	/**
	 * Set the value of the degree
	 * 
	 * @param calculatedDegree
	 *            - The value of the degree
	 */
	public void setCalculatedDegree(int calculatedDegree) {
		this.degreeTerm.setValue(calculatedDegree);
	}

	/**
	 * @return the calculated multiplicity
	 */
	public int getCalculatedMultiplicity() {
		return this.multiplicityTerm.getValue();
	}

	/**
	 * @return the calculated degree
	 */
	public int getCalculatedDegree() {
		return this.degreeTerm.getValue();
	}

	public boolean isTrigger() {
		return isTrigger;
	}
	
	
	
}
