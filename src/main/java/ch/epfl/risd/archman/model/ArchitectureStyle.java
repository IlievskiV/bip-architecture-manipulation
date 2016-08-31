package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.ConnectorNotFoundException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.helper.HelperMethods;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;

/**
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureStyle extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The set of all coordinators in the style */
	private Set<String> coordinators;

	/* The set of all parameter operands in the style */
	private Set<String> operands;

	/* The set of all ports in the style */
	private Set<String> ports;

	/* Mapping of the coordinators in the style */
	protected List<ComponentMapping> coordinatorsMapping;

	/* The set of all connectors in the style */
	private List<ConnectorTuple> connectorTuples;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method extracts the parameters from the hash table, after loading
	 * them
	 * 
	 * @throws ConfigurationFileException
	 */
	protected void parseParameters() throws ConfigurationFileException {
		/* The delimiter to split the string */
		String delim1 = ",";
		String delim2 = " ";

		/* Get all coordinators */
		this.coordinators = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORDINATORS_PARAM), delim1)));

		/* Get all operands */
		this.operands = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.OPERANDS_PARAM), delim1)));

		/* Get all ports */
		this.ports = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.PORTS_PARAM), delim1)));

		/* Get all connector tuples */
		this.connectorTuples = this.parseConnectors(
				this.archEntityConfigFile.getParameters().get(ConstantFields.CONNECTORS_PARAM), delim1, delim2);

		/* Split the concatenated string of cardinalities */
		List<String[]> coordCardinalities = HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_CARDINALITY_PARAM), delim1, delim2);

		/* Split the concatenated string of port cardinalities */
		List<String[]> cardinalities = HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_PORTS_CARDINALITY_PARAM), delim1,
				delim2);

		this.coordinatorsMapping = new LinkedList<ComponentMapping>();

		/* Map of the cardinalities for each component */
		Map<String, Integer> cardinalityMap = new HashMap<String, Integer>();
		for (String[] tokens : coordCardinalities) {
			cardinalityMap.put(tokens[0], Integer.parseInt(tokens[1]));
		}

		/* List of ports in each coordinator that have to be mapped */
		Map<String, List<String>> portsToMap = new HashMap<String, List<String>>();
		/* The list of cardinality of each port */
		Map<String, List<String>> portsCardinalities = new HashMap<String, List<String>>();

		for (String[] tokens : cardinalities) {
			/* The port to map */
			String portToMap = tokens[0];
			/* The name of the coordinator where it belongs */
			String coordName = portToMap.split("\\.")[0];

			/* Build first map */
			List<String> tempList1;
			if (!portsToMap.containsKey(coordName)) {
				tempList1 = new LinkedList<String>();
			} else {
				tempList1 = portsToMap.get(coordName);
			}

			tempList1.add(portToMap);
			portsToMap.put(coordName, tempList1);

			/* Build second map */
			List<String> tempList2 = new LinkedList<String>();
			for (int i = 1; i < tokens.length; i++) {
				tempList2.add(tokens[i]);
			}
			portsCardinalities.put(portToMap, tempList2);
		}

		Set<String> coordinators = cardinalityMap.keySet();
		for (String coord : coordinators) {
			String componentToMap = coord;
			int cardinalityValue = cardinalityMap.get(coord);
			List<String> ports = portsToMap.get(coord);

			List<List<String>> c = new LinkedList<List<String>>();

			for (String p : ports) {
				c.add(portsCardinalities.get(p));
			}

			this.coordinatorsMapping.add(new ComponentMapping(componentToMap, cardinalityValue, ports, c));
		}
	}

	/**
	 * This method parses the concatenated string representing coordinator
	 * cardinalities in the style.
	 * 
	 * @param concatenatedString
	 *            - the string representing coordinator cardinalities
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return the map of coordinator cardinalities
	 */
	private Map<String, Integer> parseCoordCardinalities(String concatenatedString, String delim1, String delim2) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		/* Split the concatenated string */
		List<String[]> cardinalities = HelperMethods.splitConcatenatedString(concatenatedString, delim1, delim2);

		/* Iterate over them */
		for (String[] carinality : cardinalities) {
			/* Add in the map */
			result.put(carinality[0], Integer.parseInt(carinality[1]));
		}

		return result;
	}

	/**
	 * This method parses the concatenated string representing coordinator ports
	 * cardinalities in the style
	 * 
	 * @param concatenatedString
	 *            - the string representing coordinator port cardinalities
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return the map of coordinator port cardinalities as interval
	 */
	private Map<String, List<String>> parseCoordPortsCardinalities(String concatenatedString, String delim1,
			String delim2) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		/* Split the concatenated string */
		List<String[]> cardinalities = HelperMethods.splitConcatenatedString(concatenatedString, delim1, delim2);

		for (String[] cardinality : cardinalities) {
			/* Parse the interval */
			List<String> interval = new LinkedList<String>();
			for (int i = 1; i < cardinality.length; i++) {
				interval.add(cardinality[i]);
			}

			result.put(cardinality[0], interval);
		}

		return result;
	}

	/**
	 * This method parses the concatenated string representing the connector
	 * tuples in the style
	 * 
	 * @param concatenatedString
	 *            - the string representing the coordinators
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return the list of all connector tuples
	 * @throws ConfigurationFileException
	 */
	private List<ConnectorTuple> parseConnectors(String concatenatedString, String delim1, String delim2)
			throws ConfigurationFileException {

		/* Instantiate the resulting list of connector tuples */
		List<ConnectorTuple> result = new ArrayList<ConnectorTuple>();

		/* Split the concatenated string */
		List<String[]> tuples = HelperMethods.splitConcatenatedString(concatenatedString, delim1, delim2);

		/* Iterate tokens */
		for (String[] tupleString : tuples) {
			/* Get the connector instance name */
			String connectorInstanceName = tupleString[0];
			/* Instantiate empty list of port tuples */
			List<PortTuple> portTuples = new LinkedList<PortTuple>();

			if ((tupleString.length - 1) % 3 != 0) {
				throw new ConfigurationFileException("Missing information in one of the connector tuples");
			}

			/* Parse the port tuples */
			for (int i = 0; i < tupleString.length - 1; i += 3) {
				/* Get the port instance name */
				String portInstanceName = tupleString[i + 1];
				/* Get the multiplicity */
				String multiplicity = tupleString[i + 2];
				/* Get the degree */
				String degree = tupleString[i + 3];

				/* Split the port instance name */
				String componentName = (portInstanceName.split("\\."))[0];

				/* Type of the tuple */
				if (this.coordinators.contains(componentName)) {
					portTuples.add(
							new PortTuple(portInstanceName, multiplicity, degree, PortTupleType.COORDINATOR_TUPLE));
				} else if (this.operands.contains(componentName)) {
					portTuples.add(new PortTuple(portInstanceName, multiplicity, degree, PortTupleType.OPERAND_TUPLE));
				} else {
					System.out.println("Something is wrong");
				}
			}

			/* Create the connector tuple */
			result.add(new ConnectorTuple(connectorInstanceName, portTuples));
		}

		return result;
	}

	@Override
	protected void validate() throws ComponentNotFoundException, ArchitectureExtractorException {
		/* Validate coordinators */
		this.validateComponents(this.coordinators);
		/* Validate operands */
		this.validateComponents(this.operands);
		/* Validate ports */
		this.validatePorts(this.ports);
		/* Validate connectors */
		this.validateConnectors(this.connectorTuples);
	}

	private void validateConnectors(List<ConnectorTuple> connectorTuples) throws ArchitectureExtractorException {
		/* Iterate the connector tuples */
		for (ConnectorTuple tuple : connectorTuples) {
			if (!BIPChecker.connectorExists(this.bipFileModel, tuple.getConnectorInstanceName())) {
				throw new ConnectorNotFoundException("The connector instance with name "
						+ tuple.getConnectorInstanceName() + " does not exist in the BIP file");
			}
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, when the configuration file is given.
	 * 
	 * @param pathToConfFile
	 *            - The path to the configuration file
	 * @throws ConfigurationFileException
	 * @throws FileNotFoundException
	 * @throws ArchitectureExtractorException
	 */
	public ArchitectureStyle(String pathToConfFile)
			throws FileNotFoundException, ConfigurationFileException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(pathToConfFile, ConstantFields.architectureStyleRequiredParams);
	}

	/**
	 * Constructor for this class, when the path to the BIP file is relative
	 * with respect the root folder of this project
	 * 
	 * @param prefix
	 * @param relativePath
	 * @throws FileNotFoundException
	 * @throws ConfigurationFileException
	 * @throws ComponentNotFoundException
	 * @throws ArchitectureExtractorException
	 */
	public ArchitectureStyle(String prefixToBip, String pathToConfFile) throws FileNotFoundException,
			ConfigurationFileException, ComponentNotFoundException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(prefixToBip, pathToConfFile, ConstantFields.architectureStyleRequiredParams);
	}

	/**
	 * @return the coordinators of the Architecture Style
	 */
	public Set<String> getCoordinators() {
		return coordinators;
	}

	/**
	 * @return the operands of the Architecture Style
	 */
	public Set<String> getOperands() {
		return operands;
	}

	/**
	 * @return the ports of the Architecture Style
	 */
	public Set<String> getPorts() {
		return ports;
	}

	/**
	 * @return the connector tuples of the Architecture Style
	 */
	public List<ConnectorTuple> getConnectorsTuples() {
		return connectorTuples;
	}

	/* Test the methods provided here (passed) */
	public static void main(String[] args) {

		String path1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
		String path2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf.txt";
		String path3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf.txt";

		try {
			ArchitectureStyle architectureStyle = new ArchitectureStyle(path1);

			for (ComponentMapping cm : architectureStyle.coordinatorsMapping) {
				System.out.println("The component to map is: " + cm.componentToMap);
				System.out.println("\t With cardinality name: " + cm.cardinalityTerm.name + " and cardinality value: "
						+ cm.cardinalityTerm.value);
				System.out.println("\t The mapping components are: ");
				for (String s : cm.mappedComponents) {
					System.out.println("\t\t " + s);
				}

				System.out.println("\t The mapping of ports: ");
				for (PortMapping pm : cm.portMappings) {
					System.out.println("\t\t The name of the port to be mapped: " + pm.portToMap);
					System.out.println("\t\t The mapping ports are: ");
					for (int i = 0; i < pm.mappedPorts.size(); i++) {
						if (pm.cardinalityTerms.get(i).isCalculated) {
							StringBuilder sb = new StringBuilder();
							for (String s : pm.mappedPorts.get(i)) {
								sb.append(s).append(" ");
							}
							System.out.println("\t\t\t " + sb.toString() + " with cardinality name: "
									+ pm.cardinalityTerms.get(i).name + " and cardinality value: "
									+ pm.cardinalityTerms.get(i).value);

						} else {
							System.out.println("\t\t\t The port is not known, the cardinality is variable");
						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigurationFileException e) {
			e.printStackTrace();
		} catch (ArchitectureExtractorException e) {
			e.printStackTrace();
		}
	}
}
