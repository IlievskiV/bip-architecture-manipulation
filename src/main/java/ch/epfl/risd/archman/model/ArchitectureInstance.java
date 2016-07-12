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
import java.util.Set;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import BIPTransformation.TransformationFunction;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.helper.HelperMethods;

/**
 * This class will represent one instance of the architecture when we will
 * substitute the parameter operands, with the exact operands.
 */
public class ArchitectureInstance extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* List of coordinators for this Architecture Instance */
	private Set<String> coordinators;

	/* List of operands for this Architecture Instance */
	private Set<String> operands;

	/* List of ports for this Architecture Instance */
	private Set<String> ports;

	/* List of interactions for this Architecture Instance */
	private Set<String> interactions;

	/* List of terms in the predicate */
	private String characteristicPredicate;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	@Override
	protected void parseParameters() throws ConfigurationFileException {
		/* The delimiter to split the string */
		String delim = ",";

		/* Get all coordinators */
		this.coordinators = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.confFileModel.getParameters().get(ConstantFields.COORDINATORS_PARAM), delim)));

		/* Get all operands */
		this.operands = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.confFileModel.getParameters().get(ConstantFields.OPERANDS_PARAM), delim)));

		/* Get all ports */
		this.ports = new HashSet<String>(Arrays.asList(HelperMethods
				.splitConcatenatedString(this.confFileModel.getParameters().get(ConstantFields.PORTS_PARAM), delim)));

		/* Get all interactions */
		this.interactions = new HashSet<String>(Arrays.asList(HelperMethods.splitConcatenatedString(
				this.confFileModel.getParameters().get(ConstantFields.INTERACTIONS_PARAM), delim)));
	}

	@Override
	protected void validate() throws ArchitectureExtractorException {
		/* Validate coordinators */
		this.validateComponents(this.coordinators);
		/* Validate operands */
		this.validateComponents(this.operands);
		/* Validate ports */
		this.validatePorts(this.ports);
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
		/* Call the super class constructor */
		super(systemName, rootTypeName, rootInstanceName, ConstantFields.architectureInstanceRequiredParams);
	}

	/**
	 * Constructor for this class, when the configuration file is given, where
	 * the path to the BIP file is absolute.
	 * 
	 * @param pathToConfFile
	 *            - absolute path to the configuration file
	 * @param emptyInteraction
	 *            - flag indicating whether the instance contains the empty
	 *            interaction
	 * 
	 * @throws FileNotFoundException
	 * @throws ConfigurationFileException
	 * @throws ArchitectureExtractorException
	 */
	public ArchitectureInstance(String pathToConfFile, boolean emptyInteraction)
			throws FileNotFoundException, ConfigurationFileException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(pathToConfFile, ConstantFields.architectureInstanceRequiredParams);

		if (emptyInteraction) {
			this.interactions.add("");
		}

		/* Calculate the characteristic predicate */
		this.characteristicPredicate = ArchitectureInstance.calculateCharacteristicPredicate(this.interactions,
				this.ports);

		/* Validate the instance */
		validate();
	}

	public ArchitectureInstance(String prefixToBip, String pathToConfFile, boolean emptyInteraction)
			throws ConfigurationFileException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(prefixToBip, pathToConfFile, ConstantFields.architectureInstanceRequiredParams);

		if (emptyInteraction) {
			this.interactions.add("");
		}

		/* Calculate the characteristic predicate */
		this.characteristicPredicate = ArchitectureInstance.calculateCharacteristicPredicate(this.interactions,
				this.ports);

		/* Validate the instance */
		validate();
	}

	/**
	 * Method for calculating the set of interactions, if the characteristic
	 * predicate is given. We assume that the characteristic predicate in the
	 * Disjunctive Normal Form, i.e. disjunctions of conjunctions
	 * 
	 * @return
	 */
	public static Set<String> calculateInteractions(String characteristicPredicate) {
		/* The resulting set */
		Set<String> result = new HashSet<String>();

		/* The characteristic predicate in terms of boolean algebra */
		Expression<String> boolPredicate = RuleSet.simplify(ExprParser.parse(characteristicPredicate));

		/* Due to the assumption, cast it to disjunction */
		Expression<String>[] expressions = ((Or<String>) boolPredicate).expressions;

		/* Iterate over the expressions in the disjunction */
		for (int i = 0; i < expressions.length; i++) {

			System.out.println("Expression: " + expressions[i]);

			/* Due to the assumption, the sub-expressions are conjunctions */
			Expression<String>[] subExpressions = ((And<String>) expressions[i]).expressions;
			/* The resulting interaction */
			StringBuilder interaction = new StringBuilder();

			/* Iterate over the sub expressions */
			for (int j = 0; j < subExpressions.length; j++) {
				/* It is variable or negation */
				if (subExpressions[j] instanceof Variable<?>) {
					/* Append the port in the interaction */
					interaction.append(((Variable<String>) subExpressions[j]).getValue()).append(" ");
				}
			}

			if (interaction.length() > 1) {
				interaction.setLength(interaction.length() - 1);
			}

			/* Add the interaction */
			result.add(interaction.toString());
			System.out.println(interaction.toString());
		}

		return result;
	}

	/**
	 * Method for calculating the characteristic predicate of this Architecture
	 * Instance, if the set of interactions is already given
	 * 
	 * @return the list of terms in the characteristic predicate
	 */
	public static String calculateCharacteristicPredicate(Set<String> interactions, Set<String> ports) {
		/* List of predicate terms */
		List<String> predicateTerms = new LinkedList<String>();

		/* Iterate the interactions */
		for (String interaction : interactions) {
			if (!interaction.equals("")) {
				/* Connector as in the Algebra of Connectors */
				ch.epfl.risd.ac.model.ConnectorNode connectorNode = ch.epfl.risd.ac.model.Connector
						.FromString(interaction);
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
			/* If we have the empty interaction */
			else {
				/* The current term in the predicate */
				StringBuilder predicateTerm = new StringBuilder();

				for (String port : ports) {
					predicateTerm.append("!" + port);
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

	/* Both instances have ports and predicates */
	public static Set<String> calculateInteractionsFromInstances(ArchitectureInstance instance1,
			ArchitectureInstance instance2) {

		/* Union of the ports */
		Set<String> union = new HashSet<String>();
		Set<String> portsInstance1 = instance1.getPorts();
		Set<String> portsInstance2 = instance2.getPorts();

		union.addAll(portsInstance1);
		union.addAll(portsInstance2);

		/* Map the ports, to not have dot */
		Hashtable<String, String> mapPorts = new Hashtable<String, String>();
		Hashtable<String, String> inverseMapPorts = new Hashtable<String, String>();
		String prefix = "b";
		int counter = 1;

		/* All ports are unique */
		for (String s : union) {
			mapPorts.put(s, prefix + String.valueOf(counter));
			inverseMapPorts.put(prefix + String.valueOf(counter), s);
			counter++;
		}

		/* Calculate new ports */
		Set<String> newPortInstance1 = new HashSet<String>();
		Set<String> newPortInstance2 = new HashSet<String>();

		for (String s : portsInstance1) {
			newPortInstance1.add(mapPorts.get(s));
		}

		for (String s : portsInstance2) {
			newPortInstance2.add(mapPorts.get(s));
		}

		/* Calculate new interactions */
		Set<String> interactionsInstance1 = instance1.getInteractions();
		Set<String> interactionsInstance2 = instance2.getInteractions();

		Set<String> newInteractions1 = new HashSet<String>();
		Set<String> newInteractions2 = new HashSet<String>();

		for (String interaction : interactionsInstance1) {
			String[] interactionPorts = interaction.split(" ");
			StringBuilder sb = new StringBuilder();

			for (String intPort : interactionPorts) {
				sb.append(mapPorts.get(intPort)).append(" ");
			}
			sb.setLength(sb.length() - 1);
			newInteractions1.add(sb.toString());
		}

		for (String interaction : interactionsInstance2) {
			String[] interactionPorts = interaction.split(" ");
			StringBuilder sb = new StringBuilder();

			for (String intPort : interactionPorts) {
				sb.append(mapPorts.get(intPort)).append(" ");
			}
			sb.setLength(sb.length() - 1);
			newInteractions2.add(sb.toString());
		}

		/* New characteristic predicates */
		String newPredicate1 = ArchitectureInstance.calculateCharacteristicPredicate(newInteractions1,
				newPortInstance1);
		String newPredicate2 = ArchitectureInstance.calculateCharacteristicPredicate(newInteractions2,
				newPortInstance2);

		/* Merge predicates */
		String mergedPredicate = "(" + newPredicate1 + ") & (" + newPredicate2 + ")";
		Expression<String> nonStandard = ExprParser.parse(mergedPredicate);
		Expression<String> dnf = RuleSet.toDNF(nonStandard);

		Expression<String>[] expressions = ((Or<String>) dnf).expressions;

		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < expressions.length; i++) {
			temp.append(expressions[i].toString().subSequence(1, expressions[i].toString().length() - 1));
			temp.append("|");
		}
		temp.setLength(temp.length() - 1);

		/* Calculate new interactions */
		Set<String> newInteractions = ArchitectureInstance.calculateInteractions(temp.toString());

		Set<String> resultInteractions = new HashSet<String>();

		for (String interaction : newInteractions) {

			if (interaction.equals("")) {
				resultInteractions.add("");
				continue;
			}

			String[] interactionPorts = interaction.split(" ");
			StringBuilder sb = new StringBuilder();

			for (String intPort : interactionPorts) {
				sb.append(inverseMapPorts.get(intPort));
				sb.append(" ");
			}
			sb.setLength(sb.length() - 1);
			resultInteractions.add(sb.toString());
		}

		return resultInteractions;
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
		this.confFileModel.addToParameters(ConstantFields.COORDINATORS_PARAM, coordinatorInstanceName);
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
		this.confFileModel.addToParameters(ConstantFields.OPERANDS_PARAM, operandInstanceName);
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
		this.confFileModel.addToParameters(ConstantFields.PORTS_PARAM, portInstanceName);
	}

	public void removePort(String portInstanceName) throws PortNotFoundException {

		for (String p : ports) {
			System.out.println(p);
		}

		if (ports.contains(portInstanceName)) {
			this.ports.remove(portInstanceName);
			this.confFileModel.removeFromParameters(ConstantFields.PORTS_PARAM, portInstanceName);

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
		this.confFileModel.addToParameters(ConstantFields.INTERACTIONS_PARAM, interactionName);
	}

	/**
	 * @return the characteristic predicate for this Architecture Instance
	 */
	public String getCharacteristicPredicate() {
		return characteristicPredicate;
	}

	public void setCharacteristicPredicate(String characteristicPredicate) {
		this.characteristicPredicate = characteristicPredicate;
	}

	/**
	 * @return the set of coordinators for this Architecture Instance
	 */
	public Set<String> getCoordinators() {
		return coordinators;
	}

	/**
	 * @return the set of operands of the Architecture Instance
	 */
	public Set<String> getOperands() {
		return operands;
	}

	/**
	 * @return the set of ports of the Architecture Instance
	 */
	public Set<String> getPorts() {
		return ports;
	}

	/**
	 * @return the set of interactions of the Architecture Instance
	 */
	public Set<String> getInteractions() {
		return interactions;
	}

	public static void main(String[] args) {
		try {
			ArchitectureInstance instance1 = new ArchitectureInstance("/home/vladimir/A12_conf.txt", true);
			ArchitectureInstance instance2 = new ArchitectureInstance("/home/vladimir/A13_conf.txt", true);

			Set<String> interactions = ArchitectureInstance.calculateInteractionsFromInstances(instance1, instance2);

			System.out.println(interactions.size());

			for (String i : interactions) {
				System.out.println("Interaction: " + i);
			}

		} catch (FileNotFoundException | ConfigurationFileException e) {
			e.printStackTrace();
		} catch (ArchitectureExtractorException e) {
			e.printStackTrace();
		}

	}
}
