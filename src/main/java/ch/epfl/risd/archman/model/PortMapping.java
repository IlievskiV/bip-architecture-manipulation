package ch.epfl.risd.archman.model;

import java.util.Set;

/**
 * This class represents which port in one parameter operand component is mapped
 * to which set of ports in one of the mapped operand components. For example if
 * we have a parameter operand component named B with ports named "take" and
 * "release", and mapping operand components named B1 and B2, one instance of
 * this class will encapsulate the information to which ports in B1, the port
 * "take" in B is mapped.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class PortMapping {
	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The port to be mapped */
	protected String portToMap;

	/* The set of mapped ports */
	protected Set<String> mappedPorts;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/* Constructor */
	public PortMapping(String portToMap, Set<String> mappedPorts) {
		this.portToMap = portToMap;
		this.mappedPorts = mappedPorts;
	}

	public String getPortToMap() {
		return portToMap;
	}

	public Set<String> getMappedPorts() {
		return mappedPorts;
	}

}
