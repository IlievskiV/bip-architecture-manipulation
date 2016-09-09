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

import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.ConnectorNotFoundException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.exceptions.TestFailException;
import ch.epfl.risd.archman.helper.HelperMethods;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;
import ch.epfl.risd.archman.solver.ArchitectureStyleSolver;

/**
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureStyle extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The set of all coordinators in the style */
	protected Set<String> coordinators;

	/* The set of all parameter operands in the style */
	protected Set<String> operands;

	/* The set of all ports in the style */
	protected Set<String> ports;

	/* Mapping of the coordinators in the style */
	protected Map<String, ComponentMapping> coordinatorsMapping;

	/* The set of all connectors in the style */
	protected List<ConnectorTuple> connectorTuples;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	@Override
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

		/* Get all mappings */
		this.coordinatorsMapping = this.parseCoordinatorMappings(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_CARDINALITY_PARAM),
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_PORTS_CARDINALITY_PARAM), delim1,
				delim2);
	}

	/**
	 * Method for parsing the mapping of the coordinators
	 * 
	 * @param coordCardStr
	 *            - concatenated string of coordinators cardinalities
	 * @param coordPortCardStr
	 *            - concatenated string of coordinators ports cardinalities
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return
	 */
	private Map<String, ComponentMapping> parseCoordinatorMappings(String coordCardStr, String coordPortCardStr,
			String delim1, String delim2) {

		/* The resulting map */
		Map<String, ComponentMapping> result = new HashMap<String, ComponentMapping>();

		/* Split the concatenated string of coordinators cardinalities */
		List<String[]> coordCardinalities = HelperMethods.splitConcatenatedString(coordCardStr, delim1, delim2);

		/* Split the concatenated string of port cardinalities */
		List<String[]> portCardinalities = HelperMethods.splitConcatenatedString(coordPortCardStr, delim1, delim2);
		
		
		/* Map of the cardinalities for each component */
		Map<String, Integer> cardinalityMap = new HashMap<String, Integer>();
		for (String[] tokens : coordCardinalities) {
			cardinalityMap.put(tokens[0], Integer.parseInt(tokens[1]));
		}

		/* List of ports in each coordinator that have to be mapped */
		Map<String, List<String>> portsToMap = new HashMap<String, List<String>>();
		/* The list of cardinalities of each port that have to be mapped */
		Map<String, List<String>> portsCardinalities = new HashMap<String, List<String>>();

		for (String[] tokens : portCardinalities) {
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

		/* The key sets of all three maps are same (coordinators names) */
		Set<String> coordinators = cardinalityMap.keySet();
		for (String coordinator : coordinators) {

			/* The name of the component to map */
			String componentToMap = coordinator;
			int cardinalityValue = cardinalityMap.get(coordinator);
			List<String> ports = portsToMap.get(coordinator);
			List<List<String>> c = new LinkedList<List<String>>();

			for (String p : ports) {
				c.add(portsCardinalities.get(p));
			}

			result.put(componentToMap, new ComponentMapping(componentToMap, cardinalityValue, ports, c));
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

	public Map<String, ComponentMapping> getCoordinatorsMapping() {
		return coordinatorsMapping;
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

		String pathOp = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";

		try {
			ArchitectureStyle architectureStyle = new ArchitectureStyle(path1);
			ArchitectureOperands architectureOperands = new ArchitectureOperands(pathOp);

			ArchitectureStyleSolver.calculateVariables(architectureStyle, architectureOperands);

			for (String key1 : architectureStyle.coordinatorsMapping.keySet()) {

				ComponentMapping cm = architectureStyle.coordinatorsMapping.get(key1);

				System.out.println("The component to map is: " + cm.componentToMap);
				System.out.println("\t With cardinality name: " + cm.cardinalityTerm.name + " and cardinality value: "
						+ cm.cardinalityTerm.value);
				System.out.println("\t The mapping components are: ");
				for (String s : cm.mappedComponents) {
					System.out.println("\t\t " + s);
				}

				System.out.println("\t The mapping of ports: ");
				for (String key2 : cm.globalPortMappings.keySet()) {

					GlobalPortMapping pm = cm.globalPortMappings.get(key2);

					System.out.println("\t\t The name of the port to be mapped: " + pm.portToMap);
					System.out.println("\t\t The mapping ports are: ");
					int i = 0;
					for (String key3 : pm.componentPortMappings.keySet()) {
						System.out.println("\t\t\t From component named: " + key3);
						if (pm.componentPortMappings.get(key3).cardinalityTerm.isCalculated) {
							StringBuilder sb = new StringBuilder();
							for (String s : pm.componentPortMappings.get(key3).getMappedPorts()) {
								sb.append(s).append(" ");
							}
							System.out.println("\t\t\t " + sb.toString() + " with cardinality name: "
									+ pm.componentPortMappings.get(key3).cardinalityTerm.name
									+ " and cardinality value: "
									+ pm.componentPortMappings.get(key3).cardinalityTerm.value);

						} else {
							System.out.println("\t\t\t The port is not known, the cardinality is variable with name: "
									+ pm.componentPortMappings.get(key3).cardinalityTerm.name);
						}
						i++;
					}
				}
			}

			System.out.println("We have the following connectors: ");
			for (ConnectorTuple ct : architectureStyle.connectorTuples) {
				System.out.println("\t Connector instance name: " + ct.getConnectorInstanceName());
				System.out.println("\t The ports in this connector instance are: ");
				for (PortTuple pt : ct.getPortTuples()) {
					System.out.println(
							"\t\t Port instance name: " + pt.getPortInstanceName() + " with multiplicity term name: "
									+ pt.multiplicityTerm.getName() + " and multiplicity term value: "
									+ pt.multiplicityTerm.value + " and with degree term name: " + pt.degreeTerm.name
									+ " and degree term value: " + pt.degreeTerm.value);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigurationFileException e) {
			e.printStackTrace();
		} catch (ArchitectureExtractorException e) {
			e.printStackTrace();
		} catch (Z3Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TestFailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
