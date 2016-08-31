package ch.epfl.risd.archman.model;

/**
 * This class represents a name-value pair of the parameters and the variables
 * in the model, to be used in the SAT solver. For every term, we need a name,
 * the value might be known at the beginning, or it will be calculated .
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class NameValue {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The name of the term */
	protected String name;

	/* The value of the term */
	protected int value;

	/* Flag whether we have a value or not */
	protected boolean isCalculated;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, when only the name is known, the value have
	 * to be calculated
	 * 
	 * @param name
	 *            - the name of the term
	 */
	public NameValue(String name) {
		this.name = name;
		this.value = -1;
		this.isCalculated = false;
	}

	/**
	 * Constructor for this class, when the name and the value are known
	 * 
	 * @param name
	 *            - the name of the term
	 * @param value
	 *            - the value of the term
	 */
	public NameValue(String name, int value) {
		this.name = name;
		this.value = value;
		this.isCalculated = true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NameValue) {
			NameValue that = (NameValue) obj;

			if (this.name.equals(that.name) && this.value == that.value)
				return true;

			return false;
		}

		return false;
	}

	/* Getters and setters */

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		this.isCalculated = true;
	}

	public String getName() {
		return name;
	}

	public boolean isCalculated() {
		return isCalculated;
	}

}
