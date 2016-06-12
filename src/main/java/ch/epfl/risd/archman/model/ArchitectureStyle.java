package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.ConnectorNotFoundException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.extractor.ExtractorImpl;
import ch.epfl.risd.archman.extractor.InspectArchitecture;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;
import ujf.verimag.bip.Core.Modules.impl.SystemImpl;

/**
 * This class is representing one architecture style, that is a parameterized
 * Architecture. From this architecture, given the operands, we can instantiate
 * one Architecture.
 */
public class ArchitectureStyle {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/*
	 * This is the model of the BIP file, where the information for the
	 * architecture style is stored
	 */
	private BIPFileModel bipFileModel;

	/*
	 * This argument contains (key, value) pairs of the parameters of the
	 * architecture style, like the list of all coordinators, list of ports,
	 * list of connectors.
	 */
	private Hashtable<String, String> parameters;

	/* The set of all coordinators in the Architecture Style */
	private Set<String> coordinators;

	/* The set of all parameter operands in the Architecture Style */
	private Set<String> operands;

	/* The set of all ports in the Architecture Style */
	private Set<String> ports;

	/* The set of all connectors in the Architecture Style */
	private List<ConnectorTuple> connectors;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method reads the Architecture Extractor configuration file, parse
	 * it, and stores the parameters in the hash table.
	 */
	private void readParameters(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException {
		/* Instantiate the hash table */
		parameters = new Hashtable<String, String>();

		/* Flag for existence of the PATH parameter in the configuration file */
		boolean hasPath = false;

		/*
		 * Flag for existence of the COORDINATORS parameter in the configuration
		 * file
		 */
		boolean hasCoordinators = false;

		/*
		 * Flag for existence of the OPERANDS parameter in the configuration
		 * file
		 */
		boolean hasOperands = false;

		/* Flag for existence PORTS parameter in the configuration file */
		boolean hasPorts = false;

		/*
		 * Flag for existence CONNECTORS parameter in the configuration file
		 */
		boolean hasConnectors = false;

		/* Get the absolute path to the configuration file */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Reading and parsing the configuration file */
		Scanner scanner = new Scanner(new File(absolutePath));
		
		while (scanner.hasNext()) {
			String[] tokens = scanner.nextLine().split(":");

			/* No more than one colon in a line exception */
			if (tokens.length > 2) {
				throw new ConfigurationFileException("More than one colon (:) in the line");
			}

			/* Check for PATH parameter */
			if (tokens[0].equals(ConstantFields.PATH_PARAM)) {
				hasPath = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the PATH parameter is missing");
				}
			}

			/* Check for COORDINATORS parameter */
			if (tokens[0].equals(ConstantFields.COORDINATORS_PARAM)) {
				hasCoordinators = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the COORDINATORS parameter is missing");
				}
			}

			/* Check for OPERANDS parameter */
			if (tokens[0].equals(ConstantFields.OPERANDS_PARAM)) {
				hasOperands = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the OPERANDS parameter is missing");
				}
			}

			/* Check for PORTS parameter */
			if (tokens[0].equals(ConstantFields.PORTS_PARAM)) {
				hasPorts = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the PORTS parameter is missing");
				}
			}

			/* Check for CONNECTORS parameter */
			if (tokens[0].equals(ConstantFields.CONNECTORS_PARAM)) {
				hasConnectors = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the CONNECTORS parameter is missing");
				}
			}

			/* Put the parameter in the hash table */
			parameters.put(tokens[0], tokens[1]);
		}

		/* If there is not some of the mandatory parameters */
		if (!hasPath) {
			throw new ConfigurationFileException("PATH parameter is missing");
		}

		if (!hasCoordinators) {
			throw new ConfigurationFileException("COORDINATORS parameter is missing");
		}

		if (!hasOperands) {
			throw new ConfigurationFileException("OPERANDS parameter is missing");
		}

		if (!hasPorts) {
			throw new ConfigurationFileException("PORTS parameter is missing");
		}

		if (!hasConnectors) {
			throw new ConfigurationFileException("INTERACTIONS parameter is missing");
		}
	}

	/**
	 * This method extracts the parameters from the hash table, after loading
	 * them
	 * 
	 * @throws ConfigurationFileException
	 */
	private void parseParameters() throws ConfigurationFileException {

		/* Concatenated string of all coordinator components */
		String allCoordinators = this.parameters.get(ConstantFields.COORDINATORS_PARAM);
		/* Concatenated string of all parameter operands in the Architecture */
		String allOperands = this.parameters.get(ConstantFields.OPERANDS_PARAM);
		/* Concatenated string of all ports in the architecture */
		String allPorts = this.parameters.get(ConstantFields.PORTS_PARAM);
		/* Concatenated string of all interactions in the architecture */
		String allConnectors = this.parameters.get(ConstantFields.CONNECTORS_PARAM);

		/* Get all coordinators */
		this.coordinators = this.parse(allCoordinators);

		/* Get all operands */
		this.operands = this.parse(allOperands);

		/* Get all ports */
		this.ports = this.parse(allPorts);

		/* Get all interactions */
		this.connectors = this.parseConnectors(allConnectors);
	}

	/**
	 * This method splits the concatenated string with a specified delimiter and
	 * return the tokens as a list
	 * 
	 * @param ConcatenatedString
	 *            - The concatenated string for splitting
	 * @return The list containing the tokens from the concatenated string
	 */
	private Set<String> parse(String concatenatedString) {
		/* Split the string */
		String[] tokens = concatenatedString.split(",");

		/* The resulting set */
		Set<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(tokens));

		return result;
	}

	/**
	 * This method parses the concatenated string representing the connectors
	 * 
	 * @param concatenatedString
	 *            - the string representing the coordinators
	 * @return the list of all connector tuples
	 * @throws ConfigurationFileException
	 */
	private List<ConnectorTuple> parseConnectors(String concatenatedString) throws ConfigurationFileException {
		/* Instantiate the resulting list of connector tuples */
		List<ConnectorTuple> result = new ArrayList<ConnectorTuple>();
		/* Split the concatenated string */
		String[] tuples = concatenatedString.split(",");

		/* Iterate tokens */
		for (String tupleString : tuples) {
			/* Split the tuple string */
			String[] tokens = tupleString.substring(1, tupleString.length() - 1).split(" ");
			/* Get the connector instance name */
			String connectorInstanceName = tokens[0];
			/* Instantiate empty list of port tuples */
			List<PortTuple> portTuples = new LinkedList<PortTuple>();

			if ((tokens.length - 1) % 3 != 0) {
				throw new ConfigurationFileException("Missing information in one of the connector tuples");
			}

			/* Parse the port tuples */
			for (int i = 0; i < tokens.length - 1; i += 3) {
				/* Get the port instance name */
				String portInstanceName = tokens[i + 1];
				/* Get the multiplicity */
				String multiplicity = tokens[i + 2];
				/* Get the degree */
				String degree = tokens[i + 3];

				/* Split the port instance name */
				String componentName = (portInstanceName.split("\\."))[0];

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

	private void validateArchitectureStyle() throws ArchitectureExtractorException {

		/* Instantiate new inspector */
		InspectArchitecture inspector = new ExtractorImpl(this.bipFileModel);

		validateCoordinators(inspector);
		validatePorts(inspector);
		validateOperands(inspector);
		validateConnectors(inspector);
	}

	private void validateCoordinators(InspectArchitecture inspector)
			throws ComponentNotFoundException, ArchitectureExtractorException {

		/* Iterate the set of coordinators in the Architecture Style */
		for (String s : this.coordinators) {
			if (!inspector.componentExists(s)) {
				throw new ComponentNotFoundException("The component with name " + s + "does not exist in the BIP file");
			}
		}

	}

	private void validatePorts(InspectArchitecture inspector)
			throws PortNotFoundException, ArchitectureExtractorException {
		/* Iterate the set of ports in the Architecture Ports */
		for (String s : this.ports) {
			String[] tokens = s.split("\\.");
			String portName = tokens[1];
			String componentName = tokens[0];
			if (!inspector.portExists(portName, componentName)) {
				throw new PortNotFoundException("The port with name " + portName + " in the component with name "
						+ componentName + " does not exist");
			}
		}
	}

	private void validateOperands(InspectArchitecture inspector)
			throws ComponentNotFoundException, ArchitectureExtractorException {
		/* Iterate the set of operands */
		for (String s : this.operands) {
			if (!inspector.componentExists(s)) {
				throw new ComponentNotFoundException(
						"The component with name " + s + " does not exist in the BIP file");
			}
		}
	}

	private void validateConnectors(InspectArchitecture inspector) throws ArchitectureExtractorException {
		/* Iterate the connector tuples */
		for (ConnectorTuple tuple : this.connectors) {
			if (!inspector.connectorExists(tuple.getConnectorInstanceName())) {
				throw new ConnectorNotFoundException("The connector instance with name "
						+ tuple.getConnectorInstanceName() + " does not exist in the BIP file");
			}
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, for reading an Architecture Style from
	 * configuration file
	 * 
	 * @param pathToConfFile
	 *            - The path to the configuration file
	 * @throws ConfigurationFileException
	 * @throws FileNotFoundException
	 * @throws ArchitectureExtractorException
	 */
	public ArchitectureStyle(String pathToConfFile)
			throws FileNotFoundException, ConfigurationFileException, ArchitectureExtractorException {

		/* Read the parameters from the configuration file */
		this.readParameters(pathToConfFile);

		/* After reading the parameters, parse parameters */
		this.parseParameters();

		/* Parse the BIP file model */
		this.bipFileModel = new BIPFileModel(this.parameters.get(ConstantFields.PATH_PARAM));

		/* Validate the Architecture style */
		validateArchitectureStyle();
	}

	/**
	 * @return the BIP file model of the Architecture Style
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

	/**
	 * @return the parameters of the Architecture style
	 */
	public Hashtable<String, String> getParameters() {
		return parameters;
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
	public List<ConnectorTuple> getConnectors() {
		return connectors;
	}

	/* Test the methods provided here (passed) */
	public static void main(String[] args) {

		String path1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
		String path2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf.txt";
		String path3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf.txt";

		try {
			ArchitectureStyle architectureStyle = new ArchitectureStyle(path1);

			Set<String> coordinators = architectureStyle.getCoordinators();
			Set<String> operands = architectureStyle.getOperands();
			Set<String> ports = architectureStyle.getPorts();
			List<ConnectorTuple> connectorTuples = architectureStyle.getConnectors();

			/* Iterate coordinators */
			System.out.println("Coordinators are: ");
			for (String s : coordinators) {
				System.out.println("\t" + s);
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
