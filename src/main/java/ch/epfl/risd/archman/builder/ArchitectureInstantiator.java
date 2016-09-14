package ch.epfl.risd.archman.builder;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.builder.ArchitectureInstanceBuilder.PortBindingType;
import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionPortException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionStatesException;
import ch.epfl.risd.archman.exceptions.InvalidAtomTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.InvalidStateNameException;
import ch.epfl.risd.archman.exceptions.InvalidVariableNameException;
import ch.epfl.risd.archman.exceptions.ListEmptyException;
import ch.epfl.risd.archman.exceptions.TestFailException;
import ch.epfl.risd.archman.extractor.ArchitectureOperandsExtractor;
import ch.epfl.risd.archman.extractor.ArchitectureStyleExtractor;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ComponentMapping;
import ch.epfl.risd.archman.model.ComponentPortMapping;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ch.epfl.risd.archman.model.GlobalPortMapping;
import ch.epfl.risd.archman.model.PortTuple;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;
import ch.epfl.risd.archman.solver.ArchitectureStyleSolver;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DefinitionBinding;
import ujf.verimag.bip.Core.Behaviors.PetriNet;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinition;
import ujf.verimag.bip.Core.Behaviors.PortDefinitionReference;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.ExportBinding;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PartElementReference;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.PortParameterReference;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.ACFusion;
import ujf.verimag.bip.Core.PortExpressions.ACTypingKind;

/**
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureInstantiator {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/* Enumeration for the type of credit in the map of credits */
	public enum CreditType {
		CREDIT_DEGREE, CREDIT_ONE;
	}

	/**
	 * Helper method to plug all ports in the instance from the style and the
	 * operands
	 */
	private static void plugAllPorts(ArchitectureStyle architectureStyle, ArchitectureOperands architectureOperands,
			ArchitectureInstance instance) throws ArchitectureExtractorException {

		List<PortType> allPortTypes = new LinkedList<PortType>();
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureStyle.getBipFileModel()));
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureOperands.getBipFileModel()));
		ArchitectureInstanceBuilder.copyAllPortTypes(instance, allPortTypes);
	}

	/**
	 * Helper method for generating the port cardinality string, i.e.
	 * concatenated string of the cardinalities of each port in one mapped
	 * component
	 */
	private static String generatePortsCardinalityString(Map<String, GlobalPortMapping> globalPortMappings,
			String mappedComponent) {
		/* The resulting cardinality string */
		StringBuilder cardinalityString = new StringBuilder();

		for (String portToMap : globalPortMappings.keySet()) {
			/* The global mapping of the port, to every mapped component */
			GlobalPortMapping globalPortMapping = globalPortMappings.get(portToMap);

			/* The mapping of the port, only to the given mapped component */
			ComponentPortMapping componentPortMapping = globalPortMapping.getComponentPortMappings()
					.get(mappedComponent);

			/* Append the cardinality */
			cardinalityString.append(componentPortMapping.getCardinalityTerm().getValue());
		}

		return cardinalityString.toString();
	}

	/**
	 * Helper method to copy the port type from the style to the instance or the
	 * get it if it already exists in the instance.
	 */
	private static PortType getOrCopyPortType(ArchitectureStyle architectureStyle, ArchitectureInstance instance,
			String portToMap) throws ArchitectureExtractorException {

		/* The port type to return */
		PortType portType;

		/* The name of the port inside the component type */
		String innerPortName = portToMap.split("\\.")[1];

		/* Take the port instance */
		Port portInstance = BIPExtractor.getPortByName(architectureStyle.getBipFileModel(), innerPortName);
		/* The type of the parameter port */
		PortType portInstanceType = portInstance.getType();

		if (!BIPChecker.portTypeExists(instance.getBipFileModel(), portInstanceType.getName())) {
			/* Copy the port type */
			portType = ArchitectureInstanceBuilder.copyPortType(instance, portInstanceType);
		} else {
			/* Get the port type */
			portType = BIPExtractor.getPortTypeByName(instance.getBipFileModel(), portInstanceType.getName());
		}

		return portType;
	}

	/**
	 * Helper method to create all port instances in one particular component to
	 * which one port is mapped to.
	 */
	private static List<Port> createMappedPortInstances(Map<String, GlobalPortMapping> globalPortMappings,
			Port coordPortInstance, PortType portType, String portToMap, String mappedComponent) {
		/* Initialize the result */
		List<Port> result = new LinkedList<Port>();

		/* Get the set of mapping ports */
		GlobalPortMapping globalPortMapping = globalPortMappings.get(portToMap);
		ComponentPortMapping componentPortMapping = globalPortMapping.getComponentPortMappings().get(mappedComponent);
		Set<String> mappedPorts = componentPortMapping.getMappedPorts();

		/* Iterate over mapped ports to create them */
		for (String mappedPort : mappedPorts) {
			/* Internal name of the port instance */
			String internalPortName = mappedPort.split("\\.")[1];

			/* This is a bit strange */
			Port newPortInstance;
			if (coordPortInstance.getBinding() instanceof DefinitionBinding) {
				newPortInstance = ArchitectureInstanceBuilder.createPortInstance(internalPortName, portType,
						coordPortInstance.getBinding(), PortBindingType.DEFINITION_BINDING);
			} else if (coordPortInstance.getBinding() instanceof ExportBinding) {
				newPortInstance = ArchitectureInstanceBuilder.createPortInstance(internalPortName, portType,
						coordPortInstance.getBinding(), PortBindingType.EXPORT_BINDING);
			} else {
				newPortInstance = ArchitectureInstanceBuilder.createPortInstance(internalPortName, portType,
						coordPortInstance.getBinding(), PortBindingType.CONTRACT_BINDING);
			}

			/* Add it to the list */
			result.add(newPortInstance);
		}

		return result;

	}

	/**
	 * Helper method to filter the transitions that are labeled by the given
	 * port name.
	 */
	private static List<Transition> filterTransitionsLabeledByPort(List<Transition> allTransitions, String portName) {
		/* The resulting transitions */
		List<Transition> result = new LinkedList<Transition>();

		/* Filter them */
		for (Transition t : allTransitions) {
			if (((PortDefinitionReference) t.getTrigger()).getTarget().getName().equals(portName)) {
				result.add(t);
			}
		}

		return result;
	}

	/**
	 * Helper method to extract states from the given transitions. The list of
	 * state names is passed by reference.
	 */
	private static void extractStatesFromTransitions(List<String> stateNames, List<Transition> transitions) {
		/* Iterate over the transitions */
		for (Transition t : transitions) {
			/* Get the origins and destinations */
			List<State> origins = t.getOrigin();
			List<State> destinations = t.getDestination();

			/* In the origins */
			for (State s : origins) {
				if (!stateNames.contains(s.getName())) {
					stateNames.add(s.getName());
				}
			}

			/* In the destinations */
			for (State s : destinations) {
				if (!stateNames.contains(s.getName())) {
					stateNames.add(s.getName());
				}
			}

		}
	}

	/**
	 * Helper method to create new transitions
	 */
	private static List<Transition> createNewTransitions(List<Transition> transitions, List<Port> ports) {
		/* Initialize the result */
		List<Transition> result = new LinkedList<Transition>();

		/* Create the new transitions */
		for (Transition t : transitions) {
			for (Port port : ports) {

				PortDefinition portDefinition = ArchitectureInstanceBuilder.createPortDefinition(port.getName(),
						port.getType());

				PortDefinitionReference ref = ArchitectureInstanceBuilder.createPortDefinitionReference(portDefinition);

				/* Not sure about this */
				Transition newTransition = ArchitectureInstanceBuilder.createTransition(ref, t.getOrigin(),
						t.getDestination(), t.getGuard(), t.getAction());

				result.add(newTransition);
			}
		}

		return result;

	}

	/**
	 * Helper method to plug all coordinators in the instance
	 */
	private static void plugAllCoordinators(ArchitectureStyle architectureStyle, ArchitectureInstance instance)
			throws ArchitectureExtractorException, InvalidComponentNameException, InvalidAtomTypeNameException,
			InvalidVariableNameException, InvalidPortNameException, IllegalTransitionPortException,
			InvalidStateNameException, IllegalTransitionStatesException, ListEmptyException {

		/* Take all coordinators from the style */
		List<Component> allCoordinators = ArchitectureStyleExtractor
				.getArchitectureStyleCoordinators(architectureStyle);

		/* Counter for how many new types will be created */
		/* This counter is at the end of the type name */
		int typeNameCount = 1;

		/* Iterate over the coordinators */
		for (Component coordinator : allCoordinators) {

			/* The name of the current coordinator */
			String currCoordName = coordinator.getName();

			/* The type name of the current coordinator */
			String currCoordTypeName = coordinator.getType().getName();

			/*
			 * Key: concatenated string of the cardinalities for each port that
			 * maps in one of the components to which the current coordinator is
			 * mapping (vertical cardinality). Value: the name of the
			 * coordinator type that matches to those port cardinalities.
			 */
			Map<String, String> portsCardToCoordTypeName = new HashMap<String, String>();

			/* The mappings of the current coordinator */
			ComponentMapping currCoordMapping = architectureStyle.getCoordinatorsMapping().get(currCoordName);

			/*
			 * The set of component names to which the current coordinator is
			 * mapped
			 */
			Set<String> mappedComponents = currCoordMapping.getMappedComponents();

			/*
			 * The mapping of each port in the current coordinator to all
			 * components to which it is mapped.
			 */
			Map<String, GlobalPortMapping> globalPortMappings = currCoordMapping.getGlobalPortMappings();

			/* Iterate over mapped components of the current coordinator */
			for (String mappedComponent : mappedComponents) {

				/* The cardinality string for the current mapped component */
				String cardinalityString = generatePortsCardinalityString(globalPortMappings, mappedComponent);

				/* Check for existence in the map */
				if (portsCardToCoordTypeName.containsKey(cardinalityString)) {
					/* Take the type and create an instance */
					ComponentType type = BIPExtractor.getComponentTypeByName(instance.getBipFileModel(),
							portsCardToCoordTypeName.get(cardinalityString));
					ArchitectureInstanceBuilder.createComponentInstance(instance, mappedComponent, type,
							instance.getBipFileModel().getRootType(), true, true);
				}
				/* Create new type in the instance */
				else {

					/* The type of the current coordinator */
					AtomType currCoordType = (AtomType) coordinator.getType();

					/* The Petri Net of the current coordinator */
					PetriNet currCoordPetriNet = (PetriNet) ((AtomType) currCoordType).getBehavior();

					/* The transitions of the current coordinator */
					List<ujf.verimag.bip.Core.Behaviors.Transition> parameterTransitions = currCoordPetriNet
							.getTransition();

					/* Initialize the list of the new type port instances */
					List<Port> allNewTypePortInstances = new LinkedList<Port>();
					/* Initialize the list of states of the new type */
					List<State> allNewTypeStates = new LinkedList<State>();
					/* The name of the newly created states */
					List<String> allNewTypeStateNames = new LinkedList<String>();
					/* The list of new transitions */
					List<ujf.verimag.bip.Core.Behaviors.Transition> allNewTypeTransitions = new LinkedList<ujf.verimag.bip.Core.Behaviors.Transition>();

					/* Iterate over the ports to map */
					for (String portToMap : globalPortMappings.keySet()) {

						/* New port instances for the current port to map */
						List<Port> newPortInstances = new LinkedList<Port>();

						/* Get or copy the port type in the instance */
						PortType portType = getOrCopyPortType(architectureStyle, instance, portToMap);

						/* The name of the port in the coordinator */
						String coordPortInstanceName = portToMap.split("\\.")[1];
						/* The port instance in the coordinator */
						Port coordPortInstance = BIPExtractor.getPortInComponentByName(coordinator,
								coordPortInstanceName);

						/* Create the port instances for the mapped ports */
						newPortInstances.addAll(createMappedPortInstances(globalPortMappings, coordPortInstance,
								portType, portToMap, mappedComponent));

						/* Filter the transitions labeled by the current port */
						List<Transition> filteredTransitions = filterTransitionsLabeledByPort(parameterTransitions,
								coordPortInstanceName);

						/*
						 * Extract the states. We pass the list of names by
						 * reference, since it is global
						 */
						extractStatesFromTransitions(allNewTypeStateNames, filteredTransitions);

						/* Create all new transitions */
						allNewTypeTransitions.addAll(createNewTransitions(filteredTransitions, newPortInstances));

						/*
						 * Add the port instances created for the current
						 * mapping port
						 */
						allNewTypePortInstances.addAll(newPortInstances);
					}

					/* Create the new states */
					allNewTypeStates.addAll(ArchitectureInstanceBuilder.createStates(allNewTypeStateNames));
					/* Create the name of the new type */
					String newTypeName = currCoordTypeName + String.valueOf(typeNameCount);
					/* Increment the type name counter */
					typeNameCount++;

					/* Create the behavior of the new type */
					PetriNet newBehavior = (PetriNet) ArchitectureInstanceBuilder.createBehavior(
							currCoordPetriNet.getInitialState(), currCoordPetriNet.getInitialization(),
							allNewTypeStates, allNewTypeTransitions);
					/* Create the new type */
					AtomType newType = ArchitectureInstanceBuilder.createAtomicType(instance, newTypeName, newBehavior,
							allNewTypePortInstances, currCoordType.getVariable());
					/* Create the instance */
					ArchitectureInstanceBuilder.createComponentInstance(instance, mappedComponent, newType,
							instance.getBipFileModel().getRootType(), true, true);

					/* Insert new matching in the map */
					portsCardToCoordTypeName.put(cardinalityString, newTypeName);
				}
			}

		}
	}

	/**
	 * Helper method to plug all operands in the instance
	 */
	public static void plugAllOperands(ArchitectureOperands architectureOperands, ArchitectureInstance instance)
			throws ArchitectureExtractorException, InvalidComponentNameException, InterruptedException {
		/* Take all operands and plug them */
		List<Component> operands = ArchitectureOperandsExtractor.getArchitectureOperands(architectureOperands);

		for (Component c : operands) {
			if (c.getType() instanceof AtomType) {
				AtomType atomType = ArchitectureInstanceBuilder.copyAtomicType(instance, (AtomType) c.getType());
				/* Make an atomic type instance of the operand */
				ArchitectureInstanceBuilder.createComponentInstance(instance, c.getName(), atomType,
						instance.getBipFileModel().getRootType(), false, true);
			} else {
				/* Make a compound type instance of the operand */
				CompoundType compoundType = ArchitectureInstanceBuilder.copyCompoundType(instance,
						(CompoundType) c.getType());
				ArchitectureInstanceBuilder.createComponentInstance(instance, c.getName(), compoundType,
						instance.getBipFileModel().getRootType(), false, true);
			}
		}
	}

	/**
	 * Helper method for creating port parameters and port parameter references.
	 * The lists are passed by reference.
	 */
	public static void createPortParamsAndReference(ArchitectureStyle architectureStyle, ArchitectureInstance instance,
			List<PortTuple> portTuples, List<PortParameter> portParameters, List<ACExpression> portParameterReferences)
			throws ArchitectureExtractorException {

		/* Iterate over the port tuples */
		for (PortTuple portTuple : portTuples) {
			/* Get the name of the port instance in the component */
			String componentPortInstanceName = portTuple.getPortInstanceName().split("\\.")[1];
			/* Get the type name of the port in the style */
			String componentPortTypeName = BIPExtractor
					.getPortByName(architectureStyle.getBipFileModel(), componentPortInstanceName).getType().getName();
			/* Get the port type in the instance */
			PortType componentPortType = BIPExtractor.getPortTypeByName(instance.getBipFileModel(),
					componentPortTypeName);

			/* Create the port parameters */
			for (int i = 0; i < portTuple.getCalculatedMultiplicity(); i++) {
				PortParameter pp = ArchitectureInstanceBuilder.createPortParameter(componentPortType,
						componentPortInstanceName + (i + 1));
				portParameters.add(pp);

				PortParameterReference ppr = ArchitectureInstanceBuilder.createPortParameterReference(pp);

				if (portTuple.isTrigger()) {
					portParameterReferences.add(ArchitectureInstanceBuilder.createACTyping(ACTypingKind.TRIG, ppr));
				} else {
					portParameterReferences.add(ArchitectureInstanceBuilder.createACTyping(ACTypingKind.SYNC, ppr));
				}
			}
		}
	}

	/**
	 * Helper method to create AC fusion from the port parameter references
	 */
	public static ACFusion createConnectorTypeFusion(List<ACExpression> portParameterReferences) {

		/* Create a list of AC Expressions */
		List<ACExpression> expressions = new LinkedList<ACExpression>();
		expressions.addAll(portParameterReferences);

		/* Create the ACFusion */
		ACFusion acFusion = ArchitectureInstanceBuilder.createACFusion(expressions);

		return acFusion;
	}

	/**
	 * Helper method to create all connector types in the instance
	 */
	public static void plugAllConnectorTypes(ArchitectureStyle architectureStyle, ArchitectureInstance instance)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Iterate over the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {

			/* The name of the connector instance */
			String connectorInstanceName = connectorTuple.getConnectorInstanceName();
			/* The name of the connector type */
			String connectorTypeName = BIPExtractor
					.getConnectorByName(architectureStyle.getBipFileModel(), connectorInstanceName).getType().getName();

			/* If the connector type not exist */
			if (!BIPChecker.connectorTypeExists(instance.getBipFileModel(), connectorTypeName)) {
				/* Get the coordinator port tuples */
				List<PortTuple> coordinatorPortTuples = connectorTuple.getCoordinatorPortTuples();
				/* Get the operand port tuples */
				List<PortTuple> operandPortTuples = connectorTuple.getOperandPortTuples();

				/* Initialize list of port parameters */
				List<PortParameter> portParameters = new LinkedList<PortParameter>();
				/* Initialize the list of port parameter references */
				List<ACExpression> portParameterReferences = new LinkedList<ACExpression>();

				createPortParamsAndReference(architectureStyle, instance, coordinatorPortTuples, portParameters,
						portParameterReferences);
				createPortParamsAndReference(architectureStyle, instance, operandPortTuples, portParameters,
						portParameterReferences);

				/* Create the AC fusion */
				ACFusion acFusion = createConnectorTypeFusion(portParameterReferences);

				/* Create an empty list of interactions */
				List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

				/* Create the connector type */
				ArchitectureInstanceBuilder.createConnectorType(instance, connectorTypeName, portParameters, acFusion,
						interactionSpecifications, null);
			}
		}

	}

	/**
	 * Helper method to calculate the matching factor
	 */
	public static int calculateMatchingFactor(PortTuple portTuple, Map<String, ComponentMapping> componentMappings) {
		/* Degree and Multiplicity of the port */
		int degree = portTuple.getCalculatedDegree();
		int multiplicity = portTuple.getCalculatedMultiplicity();

		/* Calculate sum of the port cardinalities */
		int sumOfPortCardinalities = 0;

		/* Get the mappings of the component where the port belongs */
		String portInstanceName = portTuple.getPortInstanceName();
		String componentName = portInstanceName.split("\\.")[0];
		ComponentMapping componentMapping = componentMappings.get(componentName);

		/* Get global port mappings */
		GlobalPortMapping globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
		/* Take the collection of all port mappings to each mapped component */
		Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings().values();

		/* Iterate over them */
		for (ComponentPortMapping cpm : componentPortMappings) {
			sumOfPortCardinalities += cpm.getCardinalityTerm().getValue();
		}

		return (sumOfPortCardinalities * degree) / multiplicity;
	}

	/**
	 * Helper method to create "map of credits", where the credit is equal to
	 * the degree
	 */
	public static Map<String, Integer> createMapOfCredits(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands, ConnectorTuple connectorTuple, CreditType creditType) {
		/* Initialize the resulting map */
		Map<String, Integer> result = new HashMap<String, Integer>();

		/* Get all port tuples */
		List<PortTuple> portTuples = connectorTuple.getPortTuples();
		/* Iterate over them */
		for (PortTuple portTuple : portTuples) {

			int credit;
			if (creditType == CreditType.CREDIT_DEGREE) {
				/* The degree will be the credit */
				credit = portTuple.getCalculatedDegree();
			} else {
				/* The credit will be 1 */
				credit = 1;
			}

			/* The name of the component where the port belongs */
			String portInstanceName = portTuple.getPortInstanceName();
			String compInstanceName = portInstanceName.split("\\.")[0];

			/* The mappings where the port belongs */
			ComponentMapping componentMapping;
			GlobalPortMapping globalPortMapping;

			/* If the port tuple is coordinator tuple */
			if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
				componentMapping = architectureStyle.getCoordinatorsMapping().get(compInstanceName);
				globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
			} else {
				componentMapping = architectureOperands.getOperandsMapping().get(compInstanceName);
				globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
			}

			/* Take the collection of all port mappings */
			Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings()
					.values();

			/* Iterate over them to make the map */
			for (ComponentPortMapping cpm : componentPortMappings) {
				/* Iterate over the mapped ports */
				for (String mappedPort : cpm.getMappedPorts()) {
					/* Put the credit */
					result.put(mappedPort, credit);
				}
			}

		}

		return result;
	}

	public static List<ActualPortParameter> createActualPortParams(ArchitectureInstance instance,
			List<PortTuple> portTuples, Map<String, ComponentMapping> componentMappings,
			Map<String, Integer> mapOfCredits) throws ArchitectureExtractorException {

		/* Initialize the result */
		List<ActualPortParameter> result = new LinkedList<ActualPortParameter>();

		/* Iterate over port tuples */
		for (PortTuple portTuple : portTuples) {

			/* Get the multiplicity of the port tuple */
			int multiplicity = portTuple.getCalculatedMultiplicity();

			/* Get the mappings of the component where the port belongs */
			String portInstanceName = portTuple.getPortInstanceName();
			String compInstanceName = portInstanceName.split("\\.")[0];
			ComponentMapping componentMapping = componentMappings.get(compInstanceName);

			/* Get global port mappings */
			GlobalPortMapping globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);

			/*
			 * Take the collection of all port mappings to each mapped component
			 */
			Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings()
					.values();

			/* Initialize the counter */
			int counter = 0;

			/*
			 * The map of credits is still the same. That is why this is working
			 */

			/* Iterate over them */
			for (ComponentPortMapping cpm : componentPortMappings) {
				/* Iterate over port mappings */
				for (String mappedPort : cpm.getMappedPorts()) {
					/* Create actual port parameter */
					if (mapOfCredits.get(mappedPort) > 0) {

						PartElementReference per = ArchitectureInstanceBuilder.createPartElementReference(
								BIPExtractor.getComponentByName(instance.getBipFileModel(), cpm.getMappedComponent()));
						Port p = BIPExtractor.getPortByName(instance.getBipFileModel(), mappedPort.split("\\.")[1]);
						ActualPortParameter app = ArchitectureInstanceBuilder.createInnerPortReference(per, p);
						result.add(app);

						/* Decrement the credit */
						mapOfCredits.put(mappedPort, mapOfCredits.get(mappedPort) - 1);

						/* Increment the counter */
						counter++;

						/* Jump to the next component */
						break;
					}
				}

				if (counter == multiplicity) {
					break;
				}
			}

		}

		return result;

	}

	public static void createAllConnectorInstances(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands, ArchitectureInstance instance)
			throws ArchitectureExtractorException {

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Iterate over the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {

			/* Create map of credits for each mapped port */
			Map<String, Integer> mapOfCredits = createMapOfCredits(architectureStyle, architectureOperands,
					connectorTuple, CreditType.CREDIT_DEGREE);

			/* Calculate the matching factor */
			int matchingFactor;
			if (connectorTuple.getPortTuples().get(0).getType() == PortTupleType.COORDINATOR_TUPLE) {
				matchingFactor = calculateMatchingFactor(connectorTuple.getPortTuples().get(0),
						architectureStyle.getCoordinatorsMapping());
			} else {
				matchingFactor = calculateMatchingFactor(connectorTuple.getPortTuples().get(0),
						architectureOperands.getOperandsMapping());
			}

			/* Get the coordinator port tuples */
			List<PortTuple> coordinatorPortTuples = connectorTuple.getCoordinatorPortTuples();
			/* Get the operand port tuples */
			List<PortTuple> operandPortTuples = connectorTuple.getOperandPortTuples();

			/* Get the name of the connector instance */
			String connectorInstanceName = connectorTuple.getConnectorInstanceName();
			/* Get the connector type */
			String connectorTypeName = BIPExtractor
					.getConnectorByName(architectureStyle.getBipFileModel(), connectorInstanceName).getType().getName();
			ConnectorType connectorType = BIPExtractor.getConnectorTypeByName(instance.getBipFileModel(),
					connectorTypeName);

			for (int i = 0; i < matchingFactor; i++) {

				/* Create actual port parameters */
				List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();
				actualPortParameters.addAll(createActualPortParams(instance, coordinatorPortTuples,
						architectureStyle.getCoordinatorsMapping(), mapOfCredits));
				actualPortParameters.addAll(createActualPortParams(instance, operandPortTuples,
						architectureOperands.getOperandsMapping(), mapOfCredits));
				/* create connector instance */
				ArchitectureInstanceBuilder.createConnectorInstance(instance, connectorInstanceName + (i + 1),
						connectorType, instance.getBipFileModel().getRootType(), actualPortParameters);
			}

		}

	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * 
	 * @param architectureStyle
	 * @param architectureOperands
	 * @param architectureInstanceBIPFile
	 * @param pathToSaveBIPFile
	 * @param pathToSaveConfFile
	 * @return
	 * @throws ArchitectureBuilderException
	 * @throws ArchitectureExtractorException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TestFailedException
	 * @throws Z3Exception
	 * @throws ListEmptyException
	 */
	public static ArchitectureInstance createArchitectureInstance(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands, String systemName, String rootTypeName, String rootInstanceName,
			String pathToSaveBIPFile, String pathToSaveConfFile)
			throws ArchitectureBuilderException, ArchitectureExtractorException, IOException, InterruptedException,
			Z3Exception, TestFailException, ListEmptyException {

		/* 1. Create an empty architecture instance */
		ArchitectureInstance instance = new ArchitectureInstance(systemName, rootTypeName, rootInstanceName);

		/* 2. Calculate variables */
		ArchitectureStyleSolver.calculateVariables(architectureStyle, architectureOperands);

		/* 3.Take all Port Types and plug them */
		plugAllPorts(architectureStyle, architectureOperands, instance);

		/* 4.Take all coordinators and plug them */
		plugAllCoordinators(architectureStyle, instance);

		/* 5. Take all operands and plug them */
		plugAllOperands(architectureOperands, instance);

		/* 6. Plug all connectors */
		plugAllConnectorTypes(architectureStyle, instance);

		/* 7. Create all connector instances */
		createAllConnectorInstances(architectureStyle, architectureOperands, instance);

		/* 8. Calculate the predicate */
		instance.setCharacteristicPredicate(
				ArchitectureInstance.calculateCharacteristicPredicate(instance.getInteractions(), instance.getPorts()));

		/* End. Generate BIP file */
		instance.generateBipFile(pathToSaveBIPFile);
		/* End. Generate configuration file */
		instance.generateConfigurationFile(pathToSaveConfFile);

		return instance;
	}

	public static void main(String[] args) throws ConfigurationFileException, ArchitectureExtractorException,
			IOException, ArchitectureBuilderException {

	}

}
