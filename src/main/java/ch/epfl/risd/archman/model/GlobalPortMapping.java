package ch.epfl.risd.archman.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents which port in one parameter component(coordinator or
 * coordinator) is mapped to which set of ports in every of the mapped
 * components (global port mapping). For example if we have a parameter operand
 * component named B with ports named "take" and "release", and mapping operand
 * components named B1 and B2, one instance of this class will encapsulate the
 * information to which ports in B1 and B2, the port "take" in B is mapped.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class GlobalPortMapping {
	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	public static final String PORT_CARD_DEFAULT_NAME_PREFIX = "card_port_";

	/* The port to be mapped */
	protected String portToMap;

	/* Mapping of the port in each separate component */
	protected Map<String, ComponentPortMapping> componentPortMappings;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public GlobalPortMapping(String portToMap, Map<String, ComponentPortMapping> componentPortMappings) {
		this.portToMap = portToMap;
		this.componentPortMappings = componentPortMappings;
	}

	/* Getters */

	public String getPortToMap() {
		return portToMap;
	}

	public Map<String, ComponentPortMapping> getComponentPortMappings() {
		return componentPortMappings;
	}

}
