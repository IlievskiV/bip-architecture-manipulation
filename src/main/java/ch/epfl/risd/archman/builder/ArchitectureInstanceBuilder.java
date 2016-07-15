package ch.epfl.risd.archman.builder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionPortException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionStatesException;
import ch.epfl.risd.archman.exceptions.InvalidAtomTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidCompoundTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidStateNameException;
import ch.epfl.risd.archman.exceptions.InvalidVariableNameException;
import ch.epfl.risd.archman.exceptions.ListEmptyException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.factories.Factories;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ujf.verimag.bip.Core.ActionLanguage.Actions.AssignType;
import ujf.verimag.bip.Core.ActionLanguage.Actions.AssignmentAction;
import ujf.verimag.bip.Core.ActionLanguage.Actions.CompositeAction;
import ujf.verimag.bip.Core.ActionLanguage.Actions.IfAction;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BinaryExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BinaryOperator;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BooleanLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.DataParameterReference;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.DataReference;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.IntegerLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.RealLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.StringLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.UnaryExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.UnaryOperator;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.VariableReference;
import ujf.verimag.bip.Core.Behaviors.Action;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.Behavior;
import ujf.verimag.bip.Core.Behaviors.Binding;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.DefinitionBinding;
import ujf.verimag.bip.Core.Behaviors.Expression;
import ujf.verimag.bip.Core.Behaviors.PetriNet;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinition;
import ujf.verimag.bip.Core.Behaviors.PortDefinitionReference;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Behaviors.impl.DefinitionBindingImpl;
import ujf.verimag.bip.Core.Behaviors.impl.StateImpl;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.ExportBinding;
import ujf.verimag.bip.Core.Interactions.InnerPortReference;
import ujf.verimag.bip.Core.Interactions.Interaction;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.Part;
import ujf.verimag.bip.Core.Interactions.PartElementReference;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.PortParameterReference;
import ujf.verimag.bip.Core.Modules.OpaqueElement;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.ACFusion;
import ujf.verimag.bip.Core.PortExpressions.ACFusionNeutral;
import ujf.verimag.bip.Core.PortExpressions.ACTyping;
import ujf.verimag.bip.Core.PortExpressions.ACTypingKind;
import ujf.verimag.bip.Core.PortExpressions.ACUnion;
import ujf.verimag.bip.Core.PortExpressions.ACUnionNeutral;
import ujf.verimag.bip.Core.PortExpressions.AIExpression;
import ujf.verimag.bip.Core.PortExpressions.AISynchro;
import ujf.verimag.bip.Core.PortExpressions.AISynchroNeutral;
import ujf.verimag.bip.Core.PortExpressions.AIUnion;
import ujf.verimag.bip.Core.PortExpressions.AIUnionNeutral;
import ujf.verimag.bip.Core.PortExpressions.PortExpression;

/**
 * This class serves for building one Architecture Instance, given the
 * Architecture Style and Architecture Operands
 */
public class ArchitectureInstanceBuilder {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/
	public static final Object lock = new Object();

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method checks whether in the list of Variables there are duplicates,
	 * i.e. Variables with a same name.
	 * 
	 * @param variables
	 *            - a list of Variables for checking
	 * @return true if there are duplicates, false otherwise
	 */
	protected static boolean checkDuplicateVariables(List<Variable> variables) {

		/* Iterate all variables */
		for (int i = 0; i < variables.size(); i++) {
			for (int j = i + 1; j < variables.size(); j++) {
				/* If there are two variables with the same name */
				if (variables.get(i).getName().equals(variables.get(j).getName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * This method checks whether in the list of Ports there are duplicates,
	 * i.e. Ports with a same name.
	 * 
	 * @param ports
	 *            - a list of Ports for checking
	 * @return true if there are duplicates, false otherwise
	 */
	protected static boolean checkDuplicatePorts(List<Port> ports) {

		/* Iterate ports */
		for (int i = 0; i < ports.size(); i++) {
			for (int j = i + 1; j < ports.size(); j++) {
				/* If there are two ports with a same name */
				if (ports.get(i).getName().equals(ports.get(j).getName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * This method check whether in the list of States there are duplicates,
	 * i.e. States with a same name.
	 * 
	 * @param states
	 *            - a list of states for checking
	 * @return true if there are duplicates, false otherwise
	 */
	protected static boolean checkDuplicateStates(List<State> states) {

		/* Iterate all states */
		for (int i = 0; i < states.size(); i++) {
			for (int j = i + 1; j < states.size(); j++) {
				/* If there are two states with the same name */
				if (states.get(i).getName().equals(states.get(j).getName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * This method checks whether the ports labeling one transition are in the
	 * set of ports included in the atomic type
	 * 
	 * @param transition
	 *            - the transition for checking
	 * @param ports
	 *            - the set of ports
	 * @return true if the ports labeling the transition are in the set, false
	 *         otherwise
	 */
	protected static boolean checkTransitionPorts(Transition transition, List<Port> ports) {

		/* Get the Reference to the Port Definition */
		PortDefinitionReference reference = (PortDefinitionReference) transition.getTrigger();
		/* Get the Port Definition from the Reference */
		PortDefinition definition = reference.getTarget();
		/* Get the name of the Port */
		String portDefinitionName = definition.getName();

		/* Iterate Ports */
		for (Port p : ports) {
			/* Port types are same */
			if (p.getName().equals(portDefinitionName)) {
				return true;
			}
		}

		return false;

	}

	/**
	 * 
	 * @param transition
	 * @param states
	 * @return
	 */
	protected static boolean checkTransitionStates(Transition transition, List<State> states) {

		/* Whether the list of States includes all origin States */
		boolean containsOriginStates = false;
		/* Whether the list of States includes all destination States */
		boolean containsDestinationStates = false;

		/* Get all origin States of the Transition */
		List<State> originStates = transition.getOrigin();
		/* Get all target States of the Transition */
		List<State> destinationStates = transition.getDestination();

		/* The number of origin States */
		int numOriginStates = originStates.size();
		/* The number of destination States */
		int numDestinationStates = destinationStates.size();

		/* How much of the origin States there are in the list of all States */
		int countOriginStates = 0;
		/* How much of the dest. States there are in the list of all States */
		int countDestinationStates = 0;

		/* Check origin states */
		for (int i = 0; i < states.size(); i++) {
			for (int j = 0; j < originStates.size(); j++) {
				/* Same states */
				if (originStates.get(j).getName().equals(states.get(i).getName())) {
					countOriginStates++;
				}
			}
		}

		/* Check destination States */
		for (int i = 0; i < states.size(); i++) {
			for (int j = 0; j < destinationStates.size(); j++) {
				/* Same states */
				if (destinationStates.get(j).getName().equals(states.get(i).getName())) {
					countDestinationStates++;
				}
			}
		}

		/*
		 * The number of all origin States equals to the number of matched
		 * states
		 */
		if (countOriginStates == numOriginStates) {
			containsOriginStates = true;
		}

		/*
		 * If the number of all destination States equals to the number of
		 * matched states
		 */
		if (countDestinationStates == numDestinationStates) {
			containsDestinationStates = true;
		}

		return containsOriginStates && containsDestinationStates;
	}

	/**
	 * This method checks whether in the list of port parameters there are
	 * duplicates, i.e. port parameters with a same name
	 * 
	 * @param portParameters
	 *            - the port parameters to check
	 * @return true if there are duplicates, false otherwise
	 */
	protected static boolean checkDuplicatePortParameters(List<PortParameter> portParameters) {

		/* Iterate the list of Port Parameters */
		for (int i = 0; i < portParameters.size(); i++) {
			for (int j = i + 1; j < portParameters.size(); j++) {
				if (portParameters.get(i).getName().equals(portParameters.get(j).getName())) {
					return true;
				}
			}
		}

		return false;
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/****************************************************************************/
	/* ARCHITECTURE DEPENDENT METHODS */
	/***************************************************************************/

	public static Component createComponentInstance(ArchitectureInstance architectureInstance, String name,
			ComponentType type, CompoundType parent, boolean isCoordinator, boolean toInclude)
			throws ArchitectureExtractorException, InvalidComponentNameException {

		if (BIPChecker.componentExists(architectureInstance.getBipFileModel(), name)) {
			throw new InvalidComponentNameException(
					"Component with a name " + name + " already exists in the architecture");
		} else {
			/* Create the component */
			Component component = Factories.INTERACTIONS_FACTORY.createComponent();
			/* Set name for the component */
			component.setName(name);
			/* Set type for the component */
			component.setType(type);

			/* if the parent is null then the default parent is the root */
			if (parent == null) {
				component.setCompoundType(architectureInstance.getBipFileModel().getRootType());
			} else {
				/* Set the parent */
				component.setCompoundType(parent);
			}

			if (toInclude) {
				/*
				 * If the component is coordinator then add its name in the list
				 * of coordinators
				 */
				if (isCoordinator) {
					architectureInstance.addCoordinator(name);
				} else {
					architectureInstance.addOperand(name);
				}

				/* Add port instances in the configuration file */
				List<Port> allPorts = type.getPort();
				for (Port p : allPorts) {
					architectureInstance.addPort(name + "." + p.getName());
				}
			}

			return component;
		}
	}

	public static AtomType createAtomicType(ArchitectureInstance architectureInstance, String name, Behavior behavior,
			List<Port> ports, List<Variable> variables)
			throws ArchitectureExtractorException, InvalidAtomTypeNameException, InvalidVariableNameException,
			InvalidPortNameException, IllegalTransitionPortException {

		/* Check whether the Atom Type with the same name exists */
		if (BIPChecker.componentTypeExists(architectureInstance.getBipFileModel(), name)) {
			throw new InvalidAtomTypeNameException(
					"Atom Type with a name " + name + " already exists in the architecture");
		}

		/* Create atom type */
		AtomType atomType = Factories.BEHAVIORS_FACTORY.createAtomType();
		/* Set the name for the new type */
		atomType.setName(name);
		/* Set the system */
		atomType.setModule(architectureInstance.getBipFileModel().getSystem());

		/* Add all variables */
		if (variables != null) {
			/* Check for duplicates in the variables */
			if (checkDuplicateVariables(variables)) {
				throw new InvalidVariableNameException(
						"There are Variables in the Atom Type named " + name + " with the same name");
			}
			atomType.getVariable().addAll(variables);
		}

		/* Add all ports */
		if (ports != null) {
			if (checkDuplicatePorts(ports)) {
				throw new InvalidPortNameException(
						"There are Ports in the Atom Type named " + name + " with the same name");
			}
			atomType.getPort().addAll(ports);
		}

		/* Set type to ports and create port definitions */
		for (Port p : ports) {
			p.setComponentType(atomType);
			PortDefinition pd = ((DefinitionBinding) p.getBinding()).getDefinition();
			atomType.getPortDefinition().add(pd);
		}

		/* Cast the Behavior to Petri-Net */
		PetriNet petriNet = (PetriNet) behavior;
		/* Get the list of all transitions in the Petri-Net */
		List<Transition> transitions = petriNet.getTransition();

		/* Check the match of Port References for every transition */
		for (Transition t : transitions) {
			/* no match */
			if (!ArchitectureInstanceBuilder.checkTransitionPorts(t, ports)) {
				throw new IllegalTransitionPortException("Some transition in the Atom Type named " + name
						+ " is operating with Port not defined in the same Atom Type");
			}
		}

		/* Set the behavior of the new atom type */
		atomType.setBehavior(behavior);

		return atomType;
	}

	public static AtomType copyAtomicType(ArchitectureInstance architectureInstance, AtomType type)
			throws ArchitectureExtractorException {

		if (!BIPChecker.componentTypeExists(architectureInstance.getBipFileModel(), type)) {

			/* Create the empty atom type */
			AtomType copy = Factories.BEHAVIORS_FACTORY.createAtomType();

			/* Set the name of the copy atom type */
			copy.setName(type.getName());
			/*
			 * set the module as the architecture instance module, not as the
			 * original
			 */
			copy.setModule(architectureInstance.getBipFileModel().getSystem());

			/* Set variables */
			copy.getVariable().addAll(type.getVariable());

			/* Get all ports of the original atom type */
			List<Port> originalPorts = type.getPort();

			/* Instantiate empty list of ports */
			List<Port> copyPorts = new LinkedList<Port>();

			/* Instantiate empty list of port definitions */
			List<PortDefinition> copyPortDefinitions = new LinkedList<PortDefinition>();

			/* Iterate original ports */
			for (Port p : originalPorts) {
				/* Initialize the port type */
				PortType portType;

				/* If the port type does not exist */
				if (!BIPChecker.portTypeExists(architectureInstance.getBipFileModel(), p.getType())) {
					portType = ArchitectureInstanceBuilder.copyPortType(architectureInstance, p.getType());
				}
				/* If the port type exists */
				else {
					/* Get the port type */
					portType = BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(),
							p.getType().getName());
				}

				/* Add the port to the copy ports */
				Port newPort = ArchitectureInstanceBuilder.createPortInstance(p.getName(),
						((DefinitionBindingImpl) p.getBinding()).getDefinition().getName(), portType,
						((DefinitionBindingImpl) p.getBinding()).getDefinition().getExposedVariable());

				copyPorts.add(newPort);

				/* Get the port definition of the current port */
				PortDefinition pd = ((DefinitionBinding) p.getBinding()).getDefinition();

				/* Create new port definition but with different type */
				PortDefinition newPortDefinition = ArchitectureInstanceBuilder.createPortDefinition(pd.getName(),
						portType, pd.getExposedVariable());

				/* Add it to the list */
				copyPortDefinitions.add(newPortDefinition);
			}

			/* Set the behavior */
			copy.setBehavior(type.getBehavior());

			/* Add all ports */
			copy.getPort().addAll(copyPorts);

			/* Copy port definitions */
			copy.getPortDefinition().addAll(copyPortDefinitions);

			return copy;
		} else {
			return BIPExtractor.getAtomTypeByName(architectureInstance.getBipFileModel(), type.getName());
		}
	}

	/**
	 * 
	 * @param architectureInstance
	 * @param name
	 * @param subComponents
	 * @param connectors
	 * @param ports
	 * @return
	 * @throws ArchitectureExtractorException
	 * @throws InvalidCompoundTypeNameException
	 */
	public static CompoundType createCompoundType(ArchitectureInstance architectureInstance, String name,
			List<Component> subComponents, List<Connector> connectors, List<Port> ports)
			throws ArchitectureExtractorException, InvalidCompoundTypeNameException {

		/* Check whether the Compound Type with the same name exists */
		if (BIPChecker.componentTypeExists(architectureInstance.getBipFileModel(), name)) {
			throw new InvalidCompoundTypeNameException(
					"Compound Type with a name " + name + " already exists in the architecture");
		}

		/* Create new compound type */
		CompoundType compoundType = Factories.INTERACTIONS_FACTORY.createCompoundType();
		/* Set the name for the new compound type */
		compoundType.setName(name);
		/* Set the system */
		compoundType.setModule(architectureInstance.getBipFileModel().getSystem());
		/* Set the subcomponents */
		compoundType.getSubcomponent().addAll(subComponents);
		/* Set the connectors */
		compoundType.getConnector().addAll(connectors);
		/* Set the ports */
		compoundType.getPort().addAll(ports);

		return compoundType;
	}

	/**
	 * The method is not fully implemented
	 * 
	 * @param architectureInstance
	 * @param type
	 * @return
	 * @throws ArchitectureExtractorException
	 * @throws InvalidComponentNameException
	 * @throws InterruptedException
	 */
	public static CompoundType copyCompoundType(ArchitectureInstance architectureInstance, CompoundType type)
			throws ArchitectureExtractorException, InvalidComponentNameException, InterruptedException {

		if (!BIPChecker.componentTypeExists(architectureInstance.getBipFileModel(), type)) {
			/* Create new compound type */
			CompoundType copy = Factories.INTERACTIONS_FACTORY.createCompoundType();
			/* Set the name */
			copy.setName(type.getName());
			/* Set the system */
			copy.setModule(architectureInstance.getBipFileModel().getSystem());

			/* List of new ports */
			List<Port> newPorts = new LinkedList<Port>();

			/* Get the ports */
			List<Port> originalPorts = type.getPort();

			/* Iterate original ports */
			for (Port p : originalPorts) {
				/* Initialize the port type */
				PortType portType;

				/* If the port type does not exist */
				if (!BIPChecker.portTypeExists(architectureInstance.getBipFileModel(), p.getType())) {
					portType = ArchitectureInstanceBuilder.copyPortType(architectureInstance, p.getType());
				}
				/* If the port type exists */
				else {
					/* Get the port type */
					portType = BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(),
							p.getType().getName());
				}

				Binding portBinding = p.getBinding();
				Port newPort;

				if (portBinding instanceof DefinitionBinding) {
					/* Add the port to the copy ports */
					newPort = ArchitectureInstanceBuilder.createPortInstance(p.getName(),
							((DefinitionBinding) p.getBinding()).getDefinition().getName(), portType,
							((DefinitionBinding) p.getBinding()).getDefinition().getExposedVariable());
				} else {
					List<Variable> vars = new LinkedList<Variable>();
					/* Add the port to the copy ports */
					newPort = ArchitectureInstanceBuilder.createPortInstance(p.getName(),
							((ExportBinding) p.getBinding()).getOuterPort().getName(), portType, vars);
				}

				newPorts.add(newPort);
			}

			/* Add all ports */
			copy.getPort().addAll(newPorts);

			/* Get the subcomponents */
			List<Component> subComponents = type.getSubcomponent();
			/* Add the subcomponents */
			copy.getSubcomponent().addAll(subComponents);

			/* List of new connectors */
			List<Connector> newConnectors = new LinkedList<Connector>();

			/* Get all connectors */
			List<Connector> connectors = type.getConnector();

			/* Iterate over them */
			for (Connector c : connectors) {
				ConnectorType connectorType;

				if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), c.getType().getName())) {
					connectorType = ArchitectureInstanceBuilder.copyConnectorType(architectureInstance, c.getType());
				} else {
					connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
							c.getType().getName());
				}

				List<ActualPortParameter> app = new LinkedList<ActualPortParameter>();
				for (ActualPortParameter a : c.getActualPort()) {
					app.add((InnerPortReference) a);
				}

				/* Not sure if port parameters should copy */
				Connector newConnector = ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
						c.getName(), connectorType, type, app);

				newConnectors.add(newConnector);
			}

			/* Add new connectors */
			copy.getConnector().addAll(newConnectors);

			return copy;
		} else {
			return BIPExtractor.getCompoundTypeByName(architectureInstance.getBipFileModel(), type.getName());
		}
	}

	public static PortType createPortType(ArchitectureInstance architectureInstance, String name,
			List<DataParameter> dataParameters) throws ArchitectureExtractorException, InvalidPortTypeNameException {

		/* Check whether the Port Type with the same name exists */
		if (BIPChecker.portTypeExists(architectureInstance.getBipFileModel(), name)) {
			throw new InvalidPortTypeNameException(
					"Port Type with a name " + name + " already exists in the architecture");
		}

		/* Create the new port type */
		PortType portType = Factories.BEHAVIORS_FACTORY.createPortType();
		/* Set the name of the type */
		portType.setName(name);
		/* Set the system of the type */
		portType.setModule(architectureInstance.getBipFileModel().getSystem());

		/* Set the data parameters in the port */
		if (dataParameters != null) {
			portType.getDataParameter().addAll(dataParameters);
		}

		return portType;
	}

	public static Port createPortInstance(String innerName, String interfaceName, PortType type,
			List<Variable> variables) {

		/* First create Port Definition */
		PortDefinition portDefinition = ArchitectureInstanceBuilder.createPortDefinition(interfaceName, type,
				variables);
		/* Create Definition Binding */
		DefinitionBinding binding = ArchitectureInstanceBuilder.createDefinitionBinding(portDefinition);

		/* Create the new port */
		Port port = Factories.BEHAVIORS_FACTORY.createPort();
		/* Set the name of the port */
		port.setName(innerName);
		/* set the type of the port */
		port.setBinding(binding);
		/* Set the type of the port */
		port.setType(type);

		return port;
	}

	public static PortType copyPortType(ArchitectureInstance architectureInstance, PortType type)
			throws ArchitectureExtractorException {

		if (!BIPChecker.portTypeExists(architectureInstance.getBipFileModel(), type)) {

			System.out.println("Port Type Copied: " + type.getName());

			/* Create empty port type */
			PortType copy = Factories.BEHAVIORS_FACTORY.createPortType();
			/* Set the name of the port type */
			copy.setName(type.getName());
			/*
			 * Set the module of the type same as module of the architecture
			 * instance
			 */
			copy.setModule(architectureInstance.getBipFileModel().getSystem());

			/* Set data parameters */
			copy.getDataParameter().addAll(type.getDataParameter());

			return copy;
		} else {
			return BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), type.getName());
		}

	}

	/**
	 * Method for copying all port types in the the given list of port types
	 * 
	 * @param architectureInstance
	 * @param allPortTypes
	 * @throws ArchitectureExtractorException
	 */
	public static void copyAllPortTypes(ArchitectureInstance architectureInstance, List<PortType> allPortTypes)
			throws ArchitectureExtractorException {
		/* Iterate and copy */
		for (PortType type : allPortTypes) {
			ArchitectureInstanceBuilder.copyPortType(architectureInstance, type);
		}
	}

	public static ConnectorType createConnectorType(ArchitectureInstance architectureInstance, String connectorTypeName,
			List<PortParameter> portParameters, PortExpression interactionDefinition,
			List<InteractionSpecification> interactionSpecifications, Port port)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {

		/* Check whether the Connector Type with the same name exists */
		if (BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			throw new InvalidConnectorTypeNameException(
					"Connector Type with a name " + connectorTypeName + " already exists in the architecture");
		}

		/* Create new connector type */
		ConnectorType connectorType = Factories.INTERACTIONS_FACTORY.createConnectorType();
		/* Set module */
		connectorType.setModule(architectureInstance.getBipFileModel().getSystem());
		/* Set name */
		connectorType.setName(connectorTypeName);

		/* Add all Port Parameters as arguments */
		if (portParameters != null) {

			/* Check if some of the Port Parameters have the same name */
			if (checkDuplicatePortParameters(portParameters)) {
				throw new InvalidPortParameterNameException("In Connector Type named " + connectorTypeName
						+ " there are Port Parameters(arguments) with a same name");
			}
			connectorType.getPortParameter().clear();
			connectorType.getPortParameter().addAll(portParameters);
		}

		/* Add interaction definition */
		if (interactionDefinition != null) {
			connectorType.setDefinition(interactionDefinition);
		}

		/* Add interaction specification */
		if (interactionSpecifications != null) {
			connectorType.getInteractionSpecification().addAll(interactionSpecifications);
		}

		/* Set the port definition */
		if (port != null) {
			connectorType.setPort(port);
			connectorType.setPortDefinition(((DefinitionBinding) port.getBinding()).getDefinition());
		}

		return connectorType;
	}

	/**
	 * Suppose the interaction is flat
	 * 
	 * @param architectureInstance
	 * @param name
	 * @param type
	 * @param parent
	 * @param actualPortParameters
	 * @return
	 */
	public static Connector createConnectorInstance(ArchitectureInstance architectureInstance, String name,
			ConnectorType type, CompoundType parent, List<ActualPortParameter> actualPortParameters) {
		/* Create the new Connector */
		Connector connector = Factories.INTERACTIONS_FACTORY.createConnector();
		/* Set the name of the Connector */
		connector.setName(name);
		/* set the type of the Connector */
		connector.setType(type);
		/* Set the parent of the Connector */
		connector.setCompoundType(parent);
		/* Set the input ports */
		connector.getActualPort().addAll(actualPortParameters);

		System.err.println(actualPortParameters.size());

		/* Create the interaction */
		StringBuilder interaction = new StringBuilder();
		for (ActualPortParameter app : actualPortParameters) {
			interaction.append(((InnerPortReference) app).getTargetInstance().getTargetPart().getName() + "."
					+ ((InnerPortReference) app).getTargetPort().getName()).append(" ");
		}

		/* Set the interaction */
		interaction.setLength(interaction.length() - 1);
		architectureInstance.addInteraction(interaction.toString());

		return connector;
	}

	public static ConnectorType copyConnectorType(ArchitectureInstance architectureInstance, ConnectorType type)
			throws ArchitectureExtractorException {
		/* If the connector type does not exist */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), type)) {
			/* Create new connector type */
			ConnectorType copy = Factories.INTERACTIONS_FACTORY.createConnectorType();
			/* Copy the name */
			copy.setName(type.getName());
			/* Set the module same as this architecture instance */
			copy.setModule(architectureInstance.getBipFileModel().getSystem());

			/* Instantiate an empty list of port parameters */
			List<PortParameter> copyPortParameters = new LinkedList<PortParameter>();

			/* Get all port parameters */
			List<PortParameter> portParameters = type.getPortParameter();
			/* Iterate port parameters */
			for (PortParameter p : portParameters) {
				PortType portType;

				/* If the port type does not exist */
				if (!BIPChecker.portTypeExists(architectureInstance.getBipFileModel(), p.getType())) {
					/* copy the port type */
					portType = ArchitectureInstanceBuilder.copyPortType(architectureInstance, p.getType());
				}
				/* if the port type exists */
				else {
					/* Get the port type */
					portType = BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(),
							p.getType().getName());
				}

				copyPortParameters.add(ArchitectureInstanceBuilder.createPortParameter(portType, p.getName()));
			}

			/* copy the port parameters */
			copy.getPortParameter().addAll(copyPortParameters);
			/* copy the definition */
			copy.setDefinition(type.getDefinition());
			/* copy the interactions */
			copy.getInteractionSpecification().addAll(type.getInteractionSpecification());

			/* Copy the exported port if any */
			Port connectorTypeExportedPort = copy.getPort();
			PortType exportedPortType;

			/* If it exists */
			if (connectorTypeExportedPort != null) {
				if (!BIPChecker.portTypeExists(architectureInstance.getBipFileModel(),
						connectorTypeExportedPort.getType())) {
					/* If not exist copy it */
					exportedPortType = ArchitectureInstanceBuilder.copyPortType(architectureInstance,
							connectorTypeExportedPort.getType());
				} else {
					/* If exist extract it */
					exportedPortType = BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(),
							connectorTypeExportedPort.getType().getName());
				}

				/* Create the port instance */
				Port newExportedPort = ArchitectureInstanceBuilder
						.createPortInstance(connectorTypeExportedPort.getName(),
								((DefinitionBindingImpl) connectorTypeExportedPort.getBinding()).getDefinition()
										.getName(),
								exportedPortType, ((DefinitionBindingImpl) connectorTypeExportedPort.getBinding())
										.getDefinition().getExposedVariable());

				/* Set the new port instance */
				copy.setPort(newExportedPort);

				/* Get the port definition of the current port */
				PortDefinition pd = ((DefinitionBinding) newExportedPort.getBinding()).getDefinition();

				/* Create new port definition */
				PortDefinition newPortDefinition = ArchitectureInstanceBuilder.createPortDefinition(pd.getName(),
						exportedPortType, pd.getExposedVariable());
				/* Set the new port definition */
				copy.setPortDefinition(newPortDefinition);
			}

			return copy;
		} else {
			return BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(), type.getName());
		}
	}

	/****************************************************************************/
	/* ARCHITECTURE INDEPENDENT METHODS */
	/***************************************************************************/

	public static Behavior createBehavior(List<State> initialStates, Action initialAction, List<State> states,
			List<Transition> transitions)
			throws InvalidStateNameException, ListEmptyException, IllegalTransitionStatesException {
		/* Create the behavior */
		PetriNet net = (PetriNet) Factories.BEHAVIORS_FACTORY.createPetriNet();

		/* Add initial state */
		if (initialStates != null) {
			/* Check for duplicates in the initial states */
			if (checkDuplicateStates(initialStates)) {
				throw new InvalidStateNameException("There are Initial States with a same name");
			}

			/* If the list of the Initial States is empty */
			if (initialStates.isEmpty()) {
				throw new ListEmptyException("The List of Initial States is empty");
			}

			net.getInitialState().addAll(initialStates);
		} else {
			/* If the list of all Initial States is not defined */
			throw new NullPointerException("The list of Initial States is not defined(it is null)");
		}

		/* add initial action */
		if (initialAction != null) {
			net.setInitialization(initialAction);
		}

		/* add all states */
		if (states != null) {
			/* Check for duplicates in the States */
			if (checkDuplicateStates(states)) {
				throw new InvalidStateNameException("There are States with a same name");
			}

			/* If the list of States is empty */
			if (states.isEmpty()) {
				throw new ListEmptyException("The List States is empty");
			}

			net.getState().addAll(states);
		} else {
			/* If the list of all States is not defined */
			throw new NullPointerException("The list States is not defined(it is null)");
		}

		/* Add Transitions */
		if (transitions != null) {

			/* Create a List of all initial States and other States */
			List<State> allStates = new LinkedList<State>();
			allStates.addAll(initialStates);
			allStates.addAll(states);

			/* Check every Transition */
			for (Transition transition : transitions) {
				if (!checkTransitionStates(transition, allStates)) {
					throw new IllegalTransitionStatesException(
							"The Transition is operating with States, not defined in the corresponding Atom Type");
				}
			}

			net.getTransition().addAll(transitions);
		}

		return net;
	}

	public static DataParameter createDataParameter(String name, OpaqueElement type) {
		/* Create the new Data Parameter */
		DataParameter dataParameter = Factories.BEHAVIORS_FACTORY.createDataParameter();
		/* Set the name of the new data parameter */
		dataParameter.setName(name);
		/* Set the type */
		dataParameter.setType(type);

		return dataParameter;
	}

	public static PortDefinition createPortDefinition(String interfaceName, PortType type, List<Variable> variables) {
		/* First create Port Definition */
		PortDefinition portDefinition = Factories.BEHAVIORS_FACTORY.createPortDefinition();
		/* set port type to the port definition */
		portDefinition.setType(type);
		/* Set the interface name */
		portDefinition.setName(interfaceName);
		/* Set variables */
		portDefinition.getExposedVariable().addAll(variables);

		return portDefinition;
	}

	public static DefinitionBinding createDefinitionBinding(PortDefinition portDefinition) {
		/* Create Definition Binding */
		DefinitionBinding binding = Factories.BEHAVIORS_FACTORY.createDefinitionBinding();
		/* Set definition to the binding */
		binding.setDefinition(portDefinition);

		return binding;
	}

	public static ExportBinding createExportBinding(Port port) {
		/* Create export binding */
		ExportBinding exportBinding = Factories.INTERACTIONS_FACTORY.createExportBinding();
		/* Set target port */
		exportBinding.setTargetPort(port);

		return exportBinding;
	}

	public static PortDefinitionReference createPortDefinitionReference(PortDefinition portDefinition) {
		/* Create port definition reference */
		PortDefinitionReference portDefinitionReference = Factories.BEHAVIORS_FACTORY.createPortDefinitionReference();
		/* set Port definition */
		portDefinitionReference.setTarget(portDefinition);

		return portDefinitionReference;
	}

	public static State createState(String name) {
		/* Create the state */
		State state = (StateImpl) Factories.BEHAVIORS_FACTORY.createState();
		/* Set the name of the state */
		state.setName(name);

		return state;
	}

	public static List<State> createStates(List<String> names) {
		/* Initialize the list */
		List<State> states = new LinkedList<State>();

		/* Iterate names */
		for (String n : names) {
			/* Create the state and set its name */
			State s = Factories.BEHAVIORS_FACTORY.createState();
			s.setName(n);
			/* Add the state in the list */
			states.add(s);
		}

		return states;
	}

	public static Transition createTransition(PortDefinitionReference portDefinitionReference, State origin,
			State destination, Expression guard, Action action) {
		/* Create new transition */
		Transition transition = Factories.BEHAVIORS_FACTORY.createTransition();
		/* set port reference */
		transition.setTrigger(portDefinitionReference);
		/* set origin */
		transition.getOrigin().add(origin);
		/* set destination */
		transition.getDestination().add(destination);
		/* set guard of the transition */
		if (guard != null) {
			transition.setGuard(guard);
		}
		/* set action of the transition */
		if (action != null) {
			transition.setAction(action);
		}

		return transition;
	}

	public static Variable createVariable(String name, OpaqueElement type, boolean isExternal) {
		/* Create the new variable */
		Variable variable = Factories.BEHAVIORS_FACTORY.createVariable();
		/* Set the name of the variable */
		variable.setName(name);
		/* Set the type of the variable */
		variable.setType(type);
		/* Set whether the variable is external or not */
		variable.setIsExternal(isExternal);

		return variable;
	}

	public static PartElementReference createPartElementReference(Part part) {
		/* Create an empty part element reference */
		PartElementReference partElementReference = Factories.INTERACTIONS_FACTORY.createPartElementReference();
		/* Set the target part */
		partElementReference.setTargetPart(part);

		return partElementReference;
	}

	public static InnerPortReference createInnerPortReference(PartElementReference partElementReference, Port port) {
		/* Create an empty inner port reference */
		InnerPortReference innerPortReference = Factories.INTERACTIONS_FACTORY.createInnerPortReference();
		/*
		 * Set the target instance (it can be component instance or connector
		 * instance)
		 */
		innerPortReference.setTargetInstance(partElementReference);

		/* Set the target port from the target reference */
		innerPortReference.setTargetPort(port);

		return innerPortReference;
	}

	public static Interaction createInteraction(List<PortParameter> portParameters) {
		/* Create the new interaction */
		Interaction interaction = Factories.INTERACTIONS_FACTORY.createInteraction();

		/* Create Port Parameter References */
		List<PortParameterReference> portParamRefs = new LinkedList<PortParameterReference>();
		for (PortParameter p : portParameters) {
			PortParameterReference portParamRef = ArchitectureInstanceBuilder.createPortParameterReference(p);
			portParamRefs.add(portParamRef);
		}

		/* Set the involved ports in the interaction */
		interaction.getPort().addAll(portParamRefs);

		return interaction;
	}

	public static InteractionSpecification createInteractionSpecification(Action downAction, Expression guard,
			Interaction interaction, Action upAction) {

		/* create the new Interaction Specification */
		InteractionSpecification interactionSpecification = Factories.INTERACTIONS_FACTORY
				.createInteractionSpecification();
		/* set the down action */
		interactionSpecification.setDownAction(downAction);
		/* set the guard */
		interactionSpecification.setGuard(guard);
		/* set the interaction */
		interactionSpecification.setInteraction(interaction);
		/* set the up action */
		interactionSpecification.setUpAction(upAction);

		return interactionSpecification;

	}

	public static PortParameter createPortParameter(PortType portType, String name) {
		/* Create the new Port Parameter */
		PortParameter portParam = Factories.INTERACTIONS_FACTORY.createPortParameter();
		/* set the port type of the port parameter */
		portParam.setType(portType);
		/* Set name */
		portParam.setName(name);

		return portParam;
	}

	public static BinaryExpression createBinaryExpression(Expression leftOperand, Expression rightOperand,
			BinaryOperator operator) {
		/* Create the binary expression */
		BinaryExpression binaryExpression = Factories.EXPRESSIONS_FACTORY.createBinaryExpression();
		/* Set the left operand */
		binaryExpression.setLeftOperand(leftOperand);
		/* set the right operand */
		binaryExpression.setRightOperand(rightOperand);
		/* Set the operator */
		binaryExpression.setOperator(operator);

		return binaryExpression;
	}

	public static UnaryExpression createUnaryExpression(Expression operand, UnaryOperator operator, boolean isPostfix) {
		/* Create the unary expression */
		UnaryExpression unaryExpression = Factories.EXPRESSIONS_FACTORY.createUnaryExpression();
		/* Set the operand */
		unaryExpression.setOperand(operand);
		/* set the operator */
		unaryExpression.setOperator(operator);
		/* set whether is it postfix or prefix */
		unaryExpression.setPostfix(isPostfix);

		return unaryExpression;
	}

	public static DataParameterReference createDataParameterReference(DataParameter parameter) {
		/* Create the new data parameter reference */
		DataParameterReference dataParameterReference = Factories.EXPRESSIONS_FACTORY.createDataParameterReference();
		/* Set the target parameter */
		dataParameterReference.setTargetParameter(parameter);

		return dataParameterReference;
	}

	public static VariableReference createVariableReference(Variable variable) {
		/* Create the new variable reference */
		VariableReference variableReference = Factories.EXPRESSIONS_FACTORY.createVariableReference();
		/* Set the variable */
		variableReference.setTargetVariable(variable);

		return variableReference;
	}

	public static PortParameterReference createPortParameterReference(PortParameter portParameter) {
		/* Create the new port parameter reference */
		PortParameterReference portParamRef = Factories.INTERACTIONS_FACTORY.createPortParameterReference();
		/* Set the target */
		portParamRef.setTarget(portParameter);

		return portParamRef;
	}

	public static BooleanLiteral createBooleanLiteral(boolean value) {
		/* Create new boolean literal */
		BooleanLiteral booleanLiteral = Factories.EXPRESSIONS_FACTORY.createBooleanLiteral();
		/* set the value of the literal */
		booleanLiteral.setBValue(value);

		return booleanLiteral;
	}

	public static IntegerLiteral createIntegerLiteral(int value) {
		/* Create new Integer Literal */
		IntegerLiteral integerLiteral = Factories.EXPRESSIONS_FACTORY.createIntegerLiteral();
		/* Set the value of the literal */
		integerLiteral.setIValue(value);

		return integerLiteral;
	}

	public static RealLiteral createRealLiteral(double value) {
		/* Create new real literal */
		RealLiteral realLiteral = Factories.EXPRESSIONS_FACTORY.createRealLiteral();
		/* Set the value */
		realLiteral.setRValue(String.valueOf(value));

		return realLiteral;
	}

	public static StringLiteral createStringLiteral(String value) {
		/* Create new string literal */
		StringLiteral stringLiteral = Factories.EXPRESSIONS_FACTORY.createStringLiteral();
		/* set the value of the literal */
		stringLiteral.setSValue(value);

		return stringLiteral;
	}

	public static AssignmentAction createAssignmentAction(DataReference target, Expression value, AssignType operand) {
		/* Create the assignment action */
		AssignmentAction assignmentAction = Factories.ACTIONS_FACTORY.createAssignmentAction();
		/* Set the target(left side) */
		assignmentAction.setAssignedTarget(target);
		/* Set the value(right side) */
		assignmentAction.setAssignedValue(value);
		/* Set the operation */
		assignmentAction.setType(operand);

		return assignmentAction;
	}

	public static IfAction createIfAction(Expression condition, Action ifCase, Action elseCase) {
		/* Create the 'if' action */
		IfAction ifAction = Factories.ACTIONS_FACTORY.createIfAction();
		/* Set condition */
		ifAction.setCondition(condition);
		/* Set if case */
		ifAction.setIfCase(ifCase);
		/* set else case */
		ifAction.setElseCase(elseCase);

		return ifAction;
	}

	public static CompositeAction createCompositeAction(List<Action> actions) {
		/* Create the composite action */
		CompositeAction compositeAction = Factories.ACTIONS_FACTORY.createCompositeAction();
		/* Set the list of all sub actions */
		compositeAction.getContent().addAll(actions);

		return compositeAction;
	}

	public static ACFusion createACFusion(List<ACExpression> expressions) {
		/* Create new ACFusion */
		ACFusion acFusion = Factories.PORT_EXP_FACTORY.createACFusion();
		/* Set the list of AC expressions */
		acFusion.getOperand().addAll(expressions);

		return acFusion;
	}

	public static ACUnion createACUnion(List<ACExpression> expressions) {
		/* Create new ACUnion */
		ACUnion acUnion = Factories.PORT_EXP_FACTORY.createACUnion();
		/* Set the list of AC expressions */
		acUnion.getOperand().addAll(expressions);

		return acUnion;
	}

	public static ACFusionNeutral createACFusionNeutral() {
		/* Create ACFusionNeutral */
		ACFusionNeutral acFusionNeutral = Factories.PORT_EXP_FACTORY.createACFusionNeutral();

		return acFusionNeutral;
	}

	public static ACUnionNeutral createACUnionNeutral() {
		/* Create ACUnionNeutral */
		ACUnionNeutral acUnionNeutral = Factories.PORT_EXP_FACTORY.createACUnionNeutral();

		return acUnionNeutral;
	}

	public static ACTyping createACTyping(ACTypingKind type, ACExpression expression) {
		/* Create new ACTyping */
		ACTyping acTyping = Factories.PORT_EXP_FACTORY.createACTyping();
		/* Set the AC expression */
		acTyping.setOperand(expression);
		/* Set the type of the typing, sync or trigger */
		acTyping.setType(type);

		return acTyping;
	}

	public static AISynchro createAISynchro(List<AIExpression> expressions) {
		/* Create new AISynchro */
		AISynchro aiSynchro = Factories.PORT_EXP_FACTORY.createAISynchro();
		/* Set the list of AI expressions */
		aiSynchro.getOperand().addAll(expressions);

		return aiSynchro;
	}

	public static AIUnion createAIUnion(List<AIExpression> expressions) {
		/* Create new AIUnion */
		AIUnion aiUnion = Factories.PORT_EXP_FACTORY.createAIUnion();
		/* Set the list of AI expressions */
		aiUnion.getOperand().addAll(expressions);

		return aiUnion;
	}

	public static AISynchroNeutral createAISynchroNeutral() {
		/* Create new AISynchroNeutral */
		AISynchroNeutral aiSynchroNeutral = Factories.PORT_EXP_FACTORY.createAISynchroNeutral();

		return aiSynchroNeutral;
	}

	public static AIUnionNeutral createAIUnionNeutral() {
		/* Create new AIUnionNeutral */
		AIUnionNeutral aiUnionNeutral = Factories.PORT_EXP_FACTORY.createAIUnionNeutral();

		return aiUnionNeutral;
	}

	public static OpaqueElement createOpaqueElement(String body, boolean isHeader) {
		/* create the new opaque element */
		OpaqueElement element = Factories.MODULES_FACTORY.createOpaqueElement();
		/* set the body of the element */
		element.setBody(body);
		/* set is it header */
		element.setIsHeader(isHeader);
		return element;
	}

	public static Port deletePortInstance(ComponentType componentType, String portInstanceName)
			throws PortNotFoundException {
		/* Get all port instances in the component type */
		List<Port> allPortInstances = componentType.getPort();

		/* Initialize new list of port instances */
		List<Port> newPortInstances = new LinkedList<Port>();

		/* The deleted port */
		Port deletedPort = null;

		/* Iterate over them */
		for (Port p : allPortInstances) {
			if (p.getName().equals(portInstanceName)) {
				deletedPort = p;
			} else {
				newPortInstances.add(p);
			}
		}

		if (deletedPort != null) {
			componentType.getPort().clear();
			componentType.getPort().addAll(newPortInstances);
			return deletedPort;
		} else {
			throw new PortNotFoundException("Port instance with a name " + portInstanceName
					+ " was not found in the component type named " + componentType.getName());
		}
	}

	public static void deletePortDefinition(AtomType atomType, PortDefinition portDefinition) {
		/* Get all port definitions */
		List<PortDefinition> allPortDefinitions = atomType.getPortDefinition();

		/* Initialize new list of port definitions */
		List<PortDefinition> newPortDefinitions = new LinkedList<PortDefinition>();

		/* Iterate over port definitions */
		for (PortDefinition pd : allPortDefinitions) {
			/* They are equal if from same port type and same name */
			if (!(pd.getType().getName().equals(portDefinition.getType().getName())
					&& pd.getName().equals(portDefinition.getName()))) {
				newPortDefinitions.add(pd);
			}
		}

		atomType.getPortDefinition().clear();
		atomType.getPortDefinition().addAll(newPortDefinitions);
	}

	/**
	 * Delete all transitions in which the port instance is the trigger
	 * 
	 * @param atomType
	 * @param portInstance
	 * @return
	 */
	public static List<Transition> deleteTransitions(AtomType atomType, Port portInstance) {

		/* List of all transitions in the atom type */
		List<Transition> allTransitions = ((PetriNet) atomType.getBehavior()).getTransition();

		/* List of the new transitions */
		List<Transition> newTransitions = new LinkedList<Transition>();

		/* Deleted transitions */
		List<Transition> deletedTransitions = new LinkedList<Transition>();

		/* Iterate over all transitions */
		for (Transition t : allTransitions) {
			/* Not sure about this */

			System.out
					.println("Transition triger: " + ((PortDefinitionReference) t.getTrigger()).getTarget().getName());
			System.out.println("Interface name of the port: "
					+ ((DefinitionBindingImpl) portInstance.getBinding()).getDefinition().getName());
			System.out.println("Inner name of the port: " + portInstance.getName());

			if (((PortDefinitionReference) t.getTrigger()).getTarget().getName().equals(portInstance.getName())) {
				deletedTransitions.add(t);
			} else {
				newTransitions.add(t);
			}
		}

		if (deletedTransitions.size() != 0) {
			((PetriNet) atomType.getBehavior()).getTransition().clear();
			((PetriNet) atomType.getBehavior()).getTransition().addAll(newTransitions);
			return deletedTransitions;
		} else {
			return null;
		}
	}
}
