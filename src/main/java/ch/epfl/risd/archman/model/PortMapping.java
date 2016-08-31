package ch.epfl.risd.archman.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents which port in one parameter component(coordinator or
 * coordinator) is mapped to which set of ports in one of the mapped components.
 * For example if we have a parameter operand component named B with ports named
 * "take" and "release", and mapping operand components named B1 and B2, one
 * instance of this class will encapsulate the information to which ports in B1,
 * the port "take" in B is mapped.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class PortMapping {
	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/
	
	
	public static final String PORT_CARD_DEFAULT_NAME_PREFIX = "card_port_";

	/* The port to be mapped */
	protected String portToMap;

	/* The list of set of mapped ports */
	/* One set for mapping in one separate component */
	protected List<Set<String>> mappedPorts;

	/* Name-Value pair for each mapping */
	protected List<NameValue> cardinalityTerms;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for the class, when the mapped ports are given
	 * 
	 * @param portToMap
	 *            - the name of the port to be mapped
	 * @param mappedPorts
	 *            - the set of ports to which the port is mapped
	 */
	public PortMapping(String portToMap, List<Set<String>> mappedPorts, List<NameValue> cardinalityTerms) {
		this.portToMap = portToMap;
		this.mappedPorts = mappedPorts;
		this.cardinalityTerms = cardinalityTerms;
	}
	
	
	/* Getters */
	
	public String getPortToMap() {
		return portToMap;
	}

	public List<Set<String>> getMappedPorts() {
		return mappedPorts;
	}

	public List<NameValue> getCardinalityTerms() {
		return cardinalityTerms;
	}

}
