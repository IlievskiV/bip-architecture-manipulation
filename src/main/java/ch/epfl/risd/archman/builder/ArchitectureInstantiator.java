package ch.epfl.risd.archman.builder;

import java.io.IOException;
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
import ch.epfl.risd.archman.exceptions.IllegalTransitionPortException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionStatesException;
import ch.epfl.risd.archman.exceptions.InvalidAtomTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortNameException;
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
import ch.epfl.risd.archman.model.GlobalPortMapping;
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
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ExportBinding;

/**
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureInstantiator {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

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
				System.err.println("Type: " + c.getType() + ", name: " + c.getName());
				/* Make a compound type instance of the operand */
				CompoundType compoundType = ArchitectureInstanceBuilder.copyCompoundType(instance,
						(CompoundType) c.getType());
				ArchitectureInstanceBuilder.createComponentInstance(instance, c.getName(), compoundType,
						instance.getBipFileModel().getRootType(), false, true);
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

		// /* Calculate the predicate */
		// instance.setCharacteristicPredicate(
		// ArchitectureInstance.calculateCharacteristicPredicate(instance.getInteractions(),
		// instance.getPorts()));

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
