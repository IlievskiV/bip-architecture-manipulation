package ch.epfl.risd.archman.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.risd.archman.helper.HelperMethods;

/**
 * This class is representing the mapping of one parameter component to a set of
 * components and the mapping of its ports.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ComponentMapping {

	public static final String COMP_CARD_DEFAULT_NAME_PREFIX = "card_comp_";

	/* Enumeration for the type of mapping */
	public enum ComponentMappingType {
		COORDINATOR_MAPPING_TYPE, OPERAND_MAPPING_TYPE;
	}

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* Coordinator or operand mapping */
	protected ComponentMappingType mappingType;

	/* The component to be mapped */
	protected String componentToMap;

	/* The set of components to be mapped */
	protected Set<String> mappedComponents;

	/* Name-Value pair for the cardinality */
	protected NameValue cardinalityTerm;

	/* The global mapping of every port */
	protected Map<String, GlobalPortMapping> globalPortMappings;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for the class, when the mapping is for an operand
	 * 
	 * @param componentToMap
	 *            - the name of the component to be mapped
	 * @param mappedComponents
	 *            - set of mapped components
	 * @param portsToMap
	 *            - the name of the ports in the same mapping component to be
	 *            mapped to ports in the other mapped components
	 * @param mappedPorts
	 *            - for each port, one list of sets of the mapped ports, such
	 *            that one set is representing ports in only one mapped
	 *            component
	 */
	public ComponentMapping(String componentToMap, Set<String> mappedComponents, List<String> portsToMap,
			List<List<Set<String>>> mappedPorts) {
		this.mappingType = ComponentMappingType.OPERAND_MAPPING_TYPE;
		this.componentToMap = componentToMap;
		this.mappedComponents = mappedComponents;
		this.cardinalityTerm = new NameValue(COMP_CARD_DEFAULT_NAME_PREFIX + componentToMap,
				this.mappedComponents.size());

		/* Generate port mappings */
		this.globalPortMappings = new HashMap<String, GlobalPortMapping>();

		for (int i = 0; i < portsToMap.size(); i++) {
			/* Take the current port to map */
			String currPortToMap = portsToMap.get(i);
			/* Take the mappings of the current port */
			List<Set<String>> currentMappedPorts = mappedPorts.get(i);

			/* Initialize the map of component port mappings */
			Map<String, ComponentPortMapping> componentPortMappings = new HashMap<String, ComponentPortMapping>();

			/* Iterate over the mappings */
			for (int j = 1; j <= currentMappedPorts.size(); j++) {

				/* Create the cardinality term */
				NameValue currCardinalityTerm = new NameValue(
						GlobalPortMapping.PORT_CARD_DEFAULT_NAME_PREFIX + currPortToMap + "_" + j,
						currentMappedPorts.get(j - 1).size());
				/* Get the name of the component where the ports map */
				String componentName = currentMappedPorts.get(j - 1).iterator().next().split("\\.")[0];

				componentPortMappings.put(componentName,
						new ComponentPortMapping(componentName, currentMappedPorts.get(j - 1), currCardinalityTerm));
			}

			/* Add new port mapping */
			this.globalPortMappings.put(currPortToMap, new GlobalPortMapping(currPortToMap, componentPortMappings));
		}
	}

	/**
	 * Constructor for the class, when the mapping is for a coordinator. A bit
	 * of explanation: For each port in the list "portsToMap", there is a list
	 * of cardinalities (one element in the list "cardinalities", i.e.
	 * portsToMap.size = cardinalities.size). The size of the list in one
	 * element of the list "cardinalities" equals the cardinality of the
	 * component(cardinalityTerm.value), i.e. the number of ports to which the
	 * corresponding port in the list "portsToMap" will map in each mapped
	 * component.
	 * 
	 * @param componentToMap
	 *            - the name of the component to map
	 * @param cardinalityTerm
	 *            - the cardinality term for the component
	 * @param portsToMap
	 *            - list of ports to be mapped
	 * @param cardinalities
	 *            - cardinalities for each of the ports to map in each of the
	 *            mapped components(list of list)
	 */
	public ComponentMapping(String componentToMap, int cardinalityValue, List<String> portsToMap,
			List<List<String>> cardinalities) {
		this.mappingType = ComponentMappingType.COORDINATOR_MAPPING_TYPE;
		this.componentToMap = componentToMap;
		this.cardinalityTerm = new NameValue(COMP_CARD_DEFAULT_NAME_PREFIX + "_" + componentToMap, cardinalityValue);

		/* Generate names of the mapping components */
		this.mappedComponents = new HashSet<String>();
		for (int i = 1; i <= cardinalityTerm.getValue(); i++) {
			mappedComponents.add(componentToMap + i);
		}

		/* portsToMap.size = cardinalities.size */
		/* cardinalities.get(i).size = cardinalityTerm.value */
		/* for each port to map, we have a list of cardinalities */
		/* for every mapping component */

		/* Generate port mappings */
		this.globalPortMappings = new HashMap<String, GlobalPortMapping>();
		for (int i = 0; i < portsToMap.size(); i++) {
			/* Take the current port to map */
			String currPortToMap = portsToMap.get(i);

			/* Initialize the list of mapped ports */
			Map<String, ComponentPortMapping> componentPortMapping = new HashMap<String, ComponentPortMapping>();

			/* Take and iterate over the cardinalities of the current port */
			List<String> cardinality = cardinalities.get(i);

			for (int j = 0; j < cardinality.size(); j++) {
				/* The set of ports to which the current port */
				/* Will map in the (j+1) component */
				Set<String> setOfMappedPorts = new HashSet<String>();
				String c = cardinality.get(j);

				/* The cardinality term */
				NameValue currCardinalityTerm;

				/* If the cardinality is given */
				/* We can generate the set of port names */
				if (HelperMethods.isNumeric(c)) {
					int value = Integer.parseInt(c);
					for (int k = 1; k <= value; k++) {
						/* Create the name of the port */
						setOfMappedPorts.add(componentToMap + (j + 1) + "." + currPortToMap.split("\\.")[1] + k);
					}
					/* Create cardinality term with value */
					currCardinalityTerm = new NameValue(
							GlobalPortMapping.PORT_CARD_DEFAULT_NAME_PREFIX + currPortToMap + "_" + (j + 1), value);

				}
				/* Otherwise, the set will be empty */
				else {
					/* Create cardinality term without value */
					currCardinalityTerm = new NameValue(c);
				}

				componentPortMapping.put(componentToMap + (j + 1),
						new ComponentPortMapping(componentToMap + (j + 1), setOfMappedPorts, currCardinalityTerm));
			}

			globalPortMappings.put(currPortToMap, new GlobalPortMapping(currPortToMap, componentPortMapping));
		}

	}

	/* Getters */

	public ComponentMappingType getMappingType() {
		return mappingType;
	}

	public String getComponentToMap() {
		return componentToMap;
	}

	public Set<String> getMappedComponents() {
		return mappedComponents;
	}

	public NameValue getCardinalityTerm() {
		return cardinalityTerm;
	}

	public Map<String, GlobalPortMapping> getGlobalPortMappings() {
		return globalPortMappings;
	}

}
