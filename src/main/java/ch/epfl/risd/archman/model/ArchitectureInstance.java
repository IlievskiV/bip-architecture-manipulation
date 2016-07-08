package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import BIPTransformation.TransformationFunction;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;

/**
 * This class will represent one instance of the architecture when we will
 * substitute the parameter operands, with the exact operands.
 */
public class ArchitectureInstance extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* List of coordinators for this Architecture Instance */
	private List<String> coordinators;

	/* List of operands for this Architecture Instance */
	private List<String> operands;

	/* List of ports for this Architecture Instance */
	private List<String> ports;

	/* List of interactions for this Architecture Instance */
	private List<String> interactions;

	/* List of terms in the predicate */
	private String characteristicPredicate;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	@Override
	protected void readParameters(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException {
		/* Instantiate the hash table */
		parameters = new Hashtable<String, String>();

		/* Existence of the PATH parameter in the configuration file */
		boolean hasPath = false;

		/* Existence of the COORDINATORS parameter in the configuration file */
		boolean hasCoordinators = false;

		/* Existence of the OPERANDS parameter in the configuration file */
		boolean hasOperands = false;

		/* Existence of the PORTS parameter in the configuration file */
		boolean hasPorts = false;

		/* Existence of the INTERACTIONS parameter in the configuration file */
		boolean hasInteractions = false;

		/* Get the absolute path to the configuration file */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Reading and parsing the configuration file */
		Scanner scanner = new Scanner(new File(absolutePath));

		while (scanner.hasNext()) {
			/* Take the current line and split it where the semicolon is */
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

			/* Check for PORTS parameter */
			if (tokens[0].equals(ConstantFields.INTERACTIONS_PARAM)) {
				hasInteractions = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the INTERACTIONS parameter is missing");
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

		if (!hasInteractions) {
			throw new ConfigurationFileException("INTERACTIONS parameter is missing");
		}
	}

	@Override
	protected void parseParameters() throws ConfigurationFileException {
		/* Concatenated string of all coordinator components */
		String allCoordinators = this.parameters.get(ConstantFields.COORDINATORS_PARAM);
		/* Concatenated string of all parameter operands */
		String allOperands = this.parameters.get(ConstantFields.OPERANDS_PARAM);
		/* Concatenated string of all ports */
		String allPorts = this.parameters.get(ConstantFields.PORTS_PARAM);
		/* Concatenated string of all interactions */
		String allInteractions = this.parameters.get(ConstantFields.INTERACTIONS_PARAM);

		String delim = ",";

		/* Get all coordinators */
		this.coordinators = (List<String>) this.parseConcatenatedString(allCoordinators, delim);

		/* Get all operands */
		this.operands = (List<String>) this.parseConcatenatedString(allOperands, delim);

		/* Get all ports */
		this.ports = (List<String>) this.parseConcatenatedString(allPorts, delim);

		/* Get all interactions */
		this.interactions = (List<String>) this.parseConcatenatedString(allInteractions, delim);
	}

	@Override
	protected void validate() {

	}

	/**
	 * Method to initialize the parameters
	 */
	protected void initializeParameters() {
		parameters.put(ConstantFields.PATH_PARAM, "");
		parameters.put(ConstantFields.COORDINATORS_PARAM, "");
		parameters.put(ConstantFields.OPERANDS_PARAM, "");
		parameters.put(ConstantFields.PORTS_PARAM, "");
		parameters.put(ConstantFields.INTERACTIONS_PARAM, "");
	}

	protected List<String> parseConcatenatedString(String concatenatedString, String delim) {
		/* Split the string */
		String[] tokens = concatenatedString.split(delim);

		/* The resulting list */
		List<String> result = new LinkedList<String>();
		result.addAll(Arrays.asList(tokens));

		return result;
	}

	/**
	 * Method for calculating the characteristic predicate of this Architecture
	 * Instance, if the set of interactions is already given
	 * 
	 * @return the list of terms in the characteristic predicate
	 */
	protected String calculateCharacteristicPredicate() {
		/* List of predicate terms */
		List<String> predicateTerms = new LinkedList<String>();

		/* Iterate the interactions */
		for (String interaction : interactions) {
			/* Connector as in the Algebra of Connectors */
			ch.epfl.risd.ac.model.ConnectorNode connectorNode = ch.epfl.risd.ac.model.Connector.FromString(interaction);
			ch.epfl.risd.ac.model.Connector connector = new ch.epfl.risd.ac.model.Connector(new HashSet(),
					connectorNode);
			/* Transform it to the Causal Tree */
			ch.epfl.risd.ac.model.CausalTree causalTree = connector.toCausalTree();
			/* Get the possible interactions */
			List<String> generatedInteractions = causalTree.getInteractions();

			/* Iterate over the generated interactions */
			for (String genInt : generatedInteractions) {
				/* The current term in the predicate */
				StringBuilder predicateTerm = new StringBuilder();

				/* Iterate over the ports */
				for (String port : ports) {
					/* If the generated interaction contains the port */
					if (genInt.contains(port)) {
						predicateTerm.append(port);
					} else {
						predicateTerm.append("!" + port);
					}
					/* Append AND */
					predicateTerm.append("&");
				}

				/* Cut the last & */
				predicateTerm.setLength(predicateTerm.length() - 1);
				/* Add the predicate term in the final predicate */
				predicateTerms.add(predicateTerm.toString());
			}
		}

		/* The resulting characteristic predicate */
		StringBuilder predicate = new StringBuilder();

		/* Iterate over predicate terms */
		for (String pt : predicateTerms) {
			predicate.append(pt);
			predicate.append("|");
		}

		/* Cut the last | */
		predicate.setLength(predicate.length() - 1);

		return predicate.toString();
	}

	/**
	 * Method for calculating the set of interactions, if the characteristic
	 * predicate is given. We assume that the characteristic predicate in the
	 * Disjunctive Normal Form, i.e. disjunctions of conjunctions
	 * 
	 * @return
	 */
	protected List<String> calculateInteractions() {
		/* The resulting set */
		List<String> result = new LinkedList<String>();

		/* The characteristic predicate in terms of boolean algebra */
		Expression<String> boolPredicate = RuleSet.simplify(ExprParser.parse(characteristicPredicate));

		/* Due to the assumption, cast it to disjunction */
		Expression<String>[] expressions = ((Or<String>) boolPredicate).expressions;

		/* Iterate over the expressions in the disjunction */
		for (int i = 0; i < expressions.length; i++) {
			/* Due to the assumption, the sub-expressions are conjunctions */
			Expression<String>[] subExpressions = ((And<String>) expressions[i]).expressions;
			/* The resulting interaction */
			StringBuilder interaction = new StringBuilder();

			/* Iterate over the sub expressions */
			for (int j = 0; j < subExpressions.length; j++) {
				/* It is variable or negation */
				if (subExpressions[j] instanceof Variable<?>) {
					/* Append the port in the interaction */
					interaction.append(((Variable<String>) subExpressions[j]).getValue());
				}
			}
			/* Add the interaction */
			result.add(interaction.toString());
			System.out.println(interaction.toString());
		}

		return result;
	}

	/**
	 * Method to add new value in the parameters
	 * 
	 * @param key
	 *            - the key to the value
	 * @param value
	 *            - the value of the parameter
	 */
	protected void addToParameters(String key, String value) {
		if (this.parameters.get(key).equals("")) {
			this.parameters.put(key, value);
		} else {
			this.parameters.put(key, this.parameters.get(key) + "," + value);
		}
	}

	protected void removeFromParameters(String key, String value) {
		/* Get the string of parameters */
		String params = this.parameters.get((String) key);

		/* String Builder for the result */
		StringBuilder sb = new StringBuilder();

		/* Split to tokens */
		String[] tokens = params.split(",");

		/* Iterate over them */
		for (String t : tokens) {
			if (!t.equals(value)) {
				sb.append(t).append(",");
			}
		}

		/* Cut the last comma */
		sb.setLength(sb.length() - 1);

		/* Update parameters */
		this.parameters.put(key, sb.toString());

	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, creates an empty Architecture Instance, where
	 * the lists of ports, operands and interactions are empty. Only the BIP
	 * file where the architecture will be generated as a BIP code is specified.
	 * 
	 * @param systemName
	 *            - the name of the BIP system
	 * @param rootTypeName
	 *            - the name of the root type in the BIP system
	 * @param rootInstanceName
	 *            - the name of the root component in the BIP system
	 */
	public ArchitectureInstance(String systemName, String rootTypeName, String rootInstanceName) {
		/* Create empty BIP file model */
		this.bipFileModel = new BIPFileModel(systemName, rootTypeName, rootInstanceName);
		/* Instantiate the parameters */
		this.parameters = new Hashtable<String, String>();
		/* Initialize parameters */
		this.initializeParameters();
		/* Instantiate the list of coordinators */
		this.coordinators = new LinkedList<String>();
		/* Instantiate the list of operands */
		this.operands = new LinkedList<String>();
		/* Instantiate the list of ports */
		this.ports = new LinkedList<String>();
		/* Instantiate the list of interactions */
		this.interactions = new LinkedList<String>();
		/* Empty characteristic predicate */
		this.characteristicPredicate = new String();
	}

	public ArchitectureInstance(BIPFileModel bipFileModel) {
		/* Assign the BIP file */
		this.bipFileModel = bipFileModel;
		/* Instantiate the parameters */
		this.parameters = new Hashtable<String, String>();
		/* Initialize parameters */
		this.initializeParameters();
		/* Instantiate the list of coordinators */
		this.coordinators = new LinkedList<String>();
		/* Instantiate the list of operands */
		this.operands = new LinkedList<String>();
		/* Instantiate the list of ports */
		this.ports = new LinkedList<String>();
		/* Instantiate the list of interactions */
		this.interactions = new LinkedList<String>();
		/* Empty characteristic predicate */
		this.characteristicPredicate = new String();
	}

	/**
	 * Constructor for this class, when all fields of the architecture instance
	 * are already given, except the characteristic predicate
	 * 
	 * @param bipFileModel
	 *            - the BIP file where the architecture is written
	 * @param parameters
	 *            - the map of parameters in the configuration file
	 * @param operands
	 *            - the list of operands
	 * @param ports
	 *            - the list of ports
	 * @param interactions
	 *            - the list of interactions
	 */
	public ArchitectureInstance(BIPFileModel bipFileModel, Hashtable<String, String> parameters,
			List<String> coordinators, List<String> operands, List<String> ports, List<String> interactions) {
		/* Assign the references */
		this.bipFileModel = bipFileModel;
		this.parameters = parameters;
		this.coordinators = coordinators;
		this.operands = operands;
		this.ports = ports;
		this.interactions = interactions;
		this.characteristicPredicate = this.calculateCharacteristicPredicate();
	}

	public ArchitectureInstance(String pathToBIPFile, String pathtoConfFile, List<String> coordinators,
			List<String> operands, List<String> ports, String characteristicPredicate) {

		this.characteristicPredicate = characteristicPredicate;
		this.interactions = this.calculateInteractions();

	}

	/**
	 * Constructor for this class, creates an architecture instance with the
	 * informations given in the configuration file.
	 * 
	 * @param pathToConfFile
	 *            - the path to the configuration file
	 * @throws FileNotFoundException
	 * @throws ConfigurationFileException
	 */
	public ArchitectureInstance(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException {
		/* Read the parameters from the configuration file */
		this.readParameters(pathToConfFile);

		/* After reading the parameters, parse parameters */
		this.parseParameters();

		/* Calculate the characteristic predicate */
		this.characteristicPredicate = this.calculateCharacteristicPredicate();

		/* Parse the BIP file model */
		this.bipFileModel = new BIPFileModel(this.parameters.get(ConstantFields.PATH_PARAM));

		/* Validate the instance */
		validate();
	}

	/**
	 * Method to add the coordinator instance name to both, the list of
	 * coordinators and the parameters
	 * 
	 * @param coordinatorInstanceName
	 *            - the name of the coordinator component instance
	 */
	public void addCoordinator(String coordinatorInstanceName) {
		/* Add to the list */
		this.coordinators.add(coordinatorInstanceName);
		/* Add the parameter */
		this.addToParameters(ConstantFields.COORDINATORS_PARAM, coordinatorInstanceName);
	}

	/**
	 * Method to add the operand instance name to both, the list of operands and
	 * the parameters
	 * 
	 * @param operandInstanceName
	 *            - the name of the operand component instance
	 */
	public void addOperand(String operandInstanceName) {
		/* Add to the list */
		this.operands.add(operandInstanceName);
		/* Add the parameter */
		this.addToParameters(ConstantFields.OPERANDS_PARAM, operandInstanceName);
	}

	/**
	 * Method to add the port instance name to both, the list of ports and the
	 * parameters
	 * 
	 * @param portInstanceName
	 *            - the name of the port instance
	 */
	public void addPort(String portInstanceName) {
		/* Add to the list */
		this.ports.add(portInstanceName);
		/* Add the parameter */
		this.addToParameters(ConstantFields.PORTS_PARAM, portInstanceName);
	}

	public void removePort(String portInstanceName) throws PortNotFoundException {

		for (String p : ports) {
			System.out.println(p);
		}

		if (ports.indexOf(portInstanceName) != -1) {
			this.ports.remove(ports.indexOf(portInstanceName));
			this.removeFromParameters(ConstantFields.PORTS_PARAM, portInstanceName);

		} else {
			throw new PortNotFoundException(
					"Port with name " + portInstanceName + " does not exist in the configuration file");
		}

	}

	/**
	 * Method to add an interaction to both, the list of interactions and the
	 * parameters
	 * 
	 * @param interactionName
	 *            - the name of the port instance
	 */
	public void addInteraction(String interactionName) {
		/* Add to the list */
		this.interactions.add(interactionName);
		/* Add the parameter */
		this.addToParameters(ConstantFields.INTERACTIONS_PARAM, interactionName);
	}

	/**
	 * Method to generate the resulting BIP file
	 * 
	 * @param pathToBIPFile
	 *            - absolute or relative path, where the BIP file should be
	 *            written
	 * @throws FileNotFoundException
	 */
	public void generateBipFile(String pathToBIPFile) throws FileNotFoundException {
		/* Get the absolute path */
		String absolutePath = new File(pathToBIPFile).getAbsolutePath();

		/* Add the absolute path in the parameters */
		this.setPathToBipFile(absolutePath);

		/* Write the generated code in the file */
		TransformationFunction.CreateBIPFile(absolutePath, this.bipFileModel.getSystem());
	}

	/**
	 * Method to generate the resulting configuration file
	 * 
	 * @param pathToConfFile
	 *            - relative or absolute path to the configuration file
	 * @throws IOException
	 */
	public void generateConfigurationFile(String pathToConfFile) throws IOException {
		/* Get the absolute path */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Create the file */
		File confFile = new File(absolutePath);

		if (!confFile.exists()) {
			confFile.createNewFile();
		}

		PrintWriter printer = null;

		try {
			printer = new PrintWriter(confFile);

			/* Print the path to the BIP file */
			printer.println(ConstantFields.PATH_PARAM + ":" + this.parameters.get((String) ConstantFields.PATH_PARAM));

			/* Print the coordinators */
			printer.println(ConstantFields.COORDINATORS_PARAM + ":"
					+ this.parameters.get((String) ConstantFields.COORDINATORS_PARAM));

			/* Print the operands */
			printer.println(
					ConstantFields.OPERANDS_PARAM + ":" + this.parameters.get((String) ConstantFields.OPERANDS_PARAM));

			/* Print the ports */
			printer.println(
					ConstantFields.PORTS_PARAM + ":" + this.parameters.get((String) ConstantFields.PORTS_PARAM));

			/* Print the interactions */
			printer.println(ConstantFields.INTERACTIONS_PARAM + ":"
					+ this.parameters.get((String) ConstantFields.INTERACTIONS_PARAM));
		} finally {
			if (printer != null) {
				printer.flush();
				printer.close();
			}
		}
	}

	/**
	 * @return the characteristic predicate for this Architecture Instance
	 */
	public String getCharacteristicPredicate() {
		return characteristicPredicate;
	}

	/**
	 * @return the list of coordinators for this Architecture Instance
	 */
	public List<String> getCoordinators() {
		return coordinators;
	}

	/**
	 * @return the list of operands of the Architecture Instance
	 */
	public List<String> getOperands() {
		return operands;
	}

	/**
	 * @return the list of ports of the Architecture Instance
	 */
	public List<String> getPorts() {
		return ports;
	}

	/**
	 * @return the list of interactions of the Architecture Instance
	 */
	public List<String> getInteractions() {
		return interactions;
	}
}
