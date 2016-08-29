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
 * This class is representing one architecture style, that is a parameterized
 * Architecture. From this architecture, given the operands, we can instantiate
 * one Architecture.
 */
public class ArchitectureStyle extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The set of all coordinators in the Architecture Style */
	private Set<String> coordinators;

	/* The cardinality of each coordinator */
	private Map<String, Integer> coordCardinalities;

	/* The cardinalities of the ports in the coordinator */
	private Map<String, List<String>> coordPortCardinalities;

	/* The set of all parameter operands in the Architecture Style */
	private Set<String> operands;

	/* The set of all ports in the Architecture Style */
	private Set<String> ports;

	/* The set of all connectors in the Architecture Style */
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

		/* Get coordinator cardinalities */
		this.coordCardinalities = this.parseCoordCardinalities(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_CARDINALITY_PARAM), delim1, delim2);

		/* Get coordinator port cardinalities */
		this.coordPortCardinalities = this.parseCoordPortsCardinalities(
				this.archEntityConfigFile.getParameters().get(ConstantFields.COORD_PORTS_CARDINALITY_PARAM), delim1,
				delim2);

		/* Get all operands */
		this.operands = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.OPERANDS_PARAM), delim1)));

		/* Get all ports */
		this.ports = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.archEntityConfigFile.getParameters().get(ConstantFields.PORTS_PARAM), delim1)));

		/* Get all connector tuples */
		this.connectorTuples = this.parseConnectors(
				this.archEntityConfigFile.getParameters().get(ConstantFields.CONNECTORS_PARAM), delim1, delim2);
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
	 * @return the coordinator cardinalities
	 */
	public Map<String, Integer> getCoordCardinalities() {
		return coordCardinalities;
	}

	/**
	 * @return the coordinator ports cardinalities
	 */
	public Map<String, List<String>> getCoordPortCardinalities() {
		return coordPortCardinalities;
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

			Set<String> coordinators = architectureStyle.getCoordinators();
			Map<String, Integer> coordCardinalities = architectureStyle.getCoordCardinalities();
			Map<String, List<String>> coordPortCardinalities = architectureStyle.getCoordPortCardinalities();
			Set<String> operands = architectureStyle.getOperands();
			Set<String> ports = architectureStyle.getPorts();
			List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

			/* Iterate coordinators */
			System.out.println("Coordinators are: ");
			for (String s : coordinators) {
				System.out.println("\t" + s);
			}

			/* Iterate coordinator cardinalities */
			System.out.println("Coordinator cardinalities are: ");
			for (Map.Entry<String, Integer> card : coordCardinalities.entrySet()) {
				System.out.println("\t" + card.getKey() + " : " + card.getValue());
			}

			/* Iterate coordinator port cardinalities */
			System.out.println("Coordinator port cardinalities are: ");
			for (Map.Entry<String, List<String>> card : coordPortCardinalities.entrySet()) {
				System.out.print("\t" + card.getKey() + " ");
				for(String s : card.getValue()){
					System.out.print(s + " ");
				}
				
				System.out.println();
			}

			/* Iterate operands */
			System.out.println("Operands are: ");
			for (String s : operands) {
				System.out.println("\t" + s);
			}

			/* Iterate ports */
			System.out.println("Ports are: ");
			for (String s : ports) {
				System.out.println("\t" + s);
			}

			/* Iterate connector tuples */
			System.out.println("Connector tuples: ");
			for (ConnectorTuple c : connectorTuples) {
				System.out.println("\t" + c.toString());
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
