package ch.epfl.risd.archman.model;

import java.util.Set;

/**
 * This class represents which port in one parameter component(coordinator or
 * coordinator) is mapped to which set of ports in only one particular component
 * of the mapped components (local port mapping). For example if we have a
 * parameter operand component named B with ports named "take" and "release",
 * and mapping operand components named B1 and B2, one instance of this class
 * will encapsulate the information to which ports only in B1 (not in B2), the
 * port "take" in B is mapped.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ComponentPortMapping {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The name of the mapped component */
	protected String mappedComponent;

	/* The set of ports to which it is mapped */
	protected Set<String> mappedPorts;

	/* The cardinality term */
	protected NameValue cardinalityTerm;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public ComponentPortMapping(String mappedComponent, Set<String> mappedPorts, NameValue cardinalityTerm) {
		this.mappedComponent = mappedComponent;
		this.mappedPorts = mappedPorts;
		this.cardinalityTerm = cardinalityTerm;
	}

	/* Getters */

	public String getMappedComponent() {
		return mappedComponent;
	}

	public Set<String> getMappedPorts() {
		return mappedPorts;
	}

	public NameValue getCardinalityTerm() {
		return cardinalityTerm;
	}

}
