package ch.epfl.risd.archman.composer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;

import ch.epfl.risd.archman.builder.ArchitectureInstanceBuilder;
import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.BIPFileModel;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.PortParameterReference;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.ACFusion;

public class ArchitectureComposer {

	/**
	 * Match a given interaction with some connector assuming all connectors are
	 * flat
	 * 
	 * @param bipFileModel
	 *            - the BIP file model to search for connector types
	 * @param interaction
	 *            - the given interaction to match
	 * @return the corresponding connector type, otherwise null
	 * @throws ArchitectureExtractorException
	 */
	public static ConnectorType matchInteraction(BIPFileModel bipFileModel, String interaction)
			throws ArchitectureExtractorException {

		/* How do we match the connector types? */

		/* Assume interaction is in format C.take B1.begin ... */
		/* Where C is the name of component instance */
		/* And take is the name of the port instance */

		/* Split the interaction in interaction ports */
		String[] interactionPorts = interaction.split(" ");

		/* The resulting list of ports */
		List<Port> resultingPorts = new LinkedList<Port>();

		/* Iterate over the interaction ports */
		for (String intPort : interactionPorts) {

			/* Get the component instance name */
			String componentInstanceName = intPort.split("\\.")[0];
			/* Get the port instance name */
			String portInstanceName = intPort.split("\\.")[1];

			/* Check whether the port exists */
			if (BIPChecker.portExists(bipFileModel, portInstanceName, componentInstanceName)) {
				/* Add the port */
				resultingPorts.add(BIPExtractor.getPortByName(bipFileModel, portInstanceName));
			} else {
				/*
				 * If the port does not exist there is no reason to search for a
				 * match
				 */
				return null;
			}

		}

		/* Take all connector types */
		List<ConnectorType> allConnectorTypes = BIPExtractor.getAllConnectorTypes(bipFileModel);

		/* Iterate over them */
		for (ConnectorType connType : allConnectorTypes) {
			/* Get all port parameters */
			List<PortParameter> portParameters = connType.getPortParameter();
			List<Port> tempPorts = new LinkedList<Port>(resultingPorts);

			/*
			 * The number of port parameters and the number of interaction ports
			 * must be the same
			 */
			if (portParameters.size() == tempPorts.size()) {
				/* They must be of same type */

				/* Flag indicating a match */
				boolean foundMatch = false;

				/* Iterate over them */
				for (int i = portParameters.size() - 1; i >= 0; i--) {
					/* Get the current port parameter */
					PortParameter portParam = portParameters.get(i);

					/* No match in the beginning */
					foundMatch = false;
					for (int j = tempPorts.size() - 1; j >= 0; j--) {
						/* Get the current port */
						Port port = tempPorts.get(j);

						/* Found match */
						if (portParam.getType().getName().equals(port.getType().getName())) {
							foundMatch = true;
							/* Remove them */
							portParameters.remove(i);
							tempPorts.remove(j);
							break;
						}
					}

					if (!foundMatch) {
						break;
					}
				}

				/* If the lists are empty */
				if (portParameters.size() == 0 && tempPorts.size() == 0) {
					return connType;
				}
			}
		}

		return null;
	}

	/**
	 * Method for composing two Architecture Instances.
	 * 
	 * @param instance1
	 *            - the first architecture instance
	 * @param instance2
	 *            - the second architecture instance
	 * @param architectureInstanceBIPFile
	 *            - BIP file model of the resulting architecture instance
	 * @param pathToSaveBIPFile
	 *            - absolute path where the resulting BIP file should be saved
	 * @param pathToSaveConfFile
	 *            - absolute path where the resulting configuration file should
	 *            be saved
	 * @return the newly composed Architecture Instance
	 * @throws ArchitectureExtractorException
	 * @throws InvalidComponentNameException
	 * @throws IllegalPortParameterReferenceException
	 * @throws InvalidPortParameterNameException
	 * @throws InvalidConnectorTypeNameException
	 * @throws IOException
	 */
	public static ArchitectureInstance compose(ArchitectureInstance instance1, ArchitectureInstance instance2,
			String systemName, String rootTypeName, String rootInstanceName, String pathToSaveBIPFile,
			String pathToSaveConfFile)
			throws ArchitectureExtractorException, InvalidComponentNameException, InvalidConnectorTypeNameException,
			InvalidPortParameterNameException, IllegalPortParameterReferenceException, IOException {

		/* 0.Create an empty architecture instance */
		ArchitectureInstance instance = new ArchitectureInstance(systemName, rootTypeName, rootInstanceName);

		/* 1.Take all Port Types and plug them */
		Set<PortType> allPortTypes = new HashSet<PortType>();
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(instance1.getBipFileModel()));
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(instance2.getBipFileModel()));
		List<PortType> tempList = new LinkedList<PortType>();
		tempList.addAll(allPortTypes);
		ArchitectureInstanceBuilder.copyAllPortTypes(instance, tempList);

		for (PortType pt : tempList) {
			System.out.println("Port Type Name: " + pt.getName());
		}

		/* 2.Take all components except the roots and plug them */
		Set<Component> allComponents = new HashSet<Component>();
		allComponents.addAll(BIPExtractor.getAllComponents(instance1.getBipFileModel()));
		allComponents.addAll(BIPExtractor.getAllComponents(instance2.getBipFileModel()));

		for (Component c : allComponents) {

			System.out.println("Component name: " + c.getName());

			if (!BIPChecker.componentExists(instance.getBipFileModel(), c)) {

				System.out.println("Component not exists");

				/* Check if it is coordinator */
				boolean isCoordinator = instance1.getCoordinators().contains(c.getName())
						|| instance2.getCoordinators().contains(c.getName());

				if (c.getType() instanceof AtomType) {
					/* Copy or retrieve the atomic type */
					AtomType atomType = ArchitectureInstanceBuilder.copyAtomicType(instance, (AtomType) c.getType());
					/* Create an instance of the atomic type */
					ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), atomType,
							instance.getBipFileModel().getRootType(), isCoordinator);

				} else if (c.getType() instanceof CompoundType) {
					/* If the component is not one the roots */
					if (!(c.getName().equals(instance1.getBipFileModel().getRoot().getName())
							|| c.getName().equals(instance2.getBipFileModel().getRoot().getName()))) {

						/* Copy or retrieve the compound type */
						CompoundType compoundType = ArchitectureInstanceBuilder.copyCompoundType(instance,
								(CompoundType) c.getType());
						/* Create an instance of the compound type */
						ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), compoundType,
								instance.getBipFileModel().getRootType(), isCoordinator);
					}
				}

			}
		}

		/* 3. Match the interactions with connector types */
		Set<String> interactions = ArchitectureInstance.calculateInteractionsFromInstances(instance1, instance2);

		for (String i : interactions) {
			System.out.println("Interaction: " + i);
		}

		/* Counter for the connector type */
		int connectorTypeCounter = 1;
		/* Map for counting the connector type instances */
		Hashtable<String, Integer> connectorTypeInstances = new Hashtable<String, Integer>();
		/* To name port parameters */
		String alphabet = "abcdefghijklmnopqrstuvwxyz";

		/* Iterate the interactions */
		for (String interaction : interactions) {

			if (interaction.equals("")) {
				instance.getInteractions().add("");
				continue;
			}

			/* Split the interaction in interaction ports */
			String[] interactionPorts = interaction.split(" ");

			/* Create an empty list of actual port parameters */
			List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

			/* Iterate over the interaction ports */
			for (String intPort : interactionPorts) {
				/* Get the component instance name */
				String componentInstanceName = intPort.split("\\.")[0];
				/* Get the port instance name */
				String portInstanceName = intPort.split("\\.")[1];

				actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
						ArchitectureInstanceBuilder.createPartElementReference(
								BIPExtractor.getComponentByName(instance.getBipFileModel(), componentInstanceName)),
						BIPExtractor.getPortByName(instance.getBipFileModel(), portInstanceName)));
			}

			/* Check for match */
			ConnectorType connectorType = ArchitectureComposer.matchInteraction(instance.getBipFileModel(),
					interaction);

			if (connectorType == null) {
				/* If no match we have to create new connector type */

				/* Name of the connector type */
				String connectorTypeName = ConstantFields.CONNECTOR_TYPE + String.valueOf(connectorTypeCounter);
				/* Insert in the map the first instance */
				connectorTypeInstances.put(connectorTypeName, 1);

				/* Create an empty list of port parameters for the type */
				List<PortParameter> portParameters = new LinkedList<PortParameter>();

				/* Iterate over the interaction ports */
				for (int i = 0; i < interactionPorts.length; i++) {
					String intPort = interactionPorts[i];
					/* Get the port instance name */
					String portInstanceName = intPort.split("\\.")[1];

					/* Get the type name of the port */
					String componentPortTypeName = BIPExtractor
							.getPortByName(instance.getBipFileModel(), portInstanceName).getType().getName();

					/* Create port parameter */
					portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
							BIPExtractor.getPortTypeByName(instance.getBipFileModel(), componentPortTypeName),
							String.valueOf(alphabet.charAt(i))));
				}

				/* Create a list of port parameter references */
				List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
				for (PortParameter pp : portParameters) {

					PortParameterReference portParameterReference = ArchitectureInstanceBuilder
							.createPortParameterReference(pp);
					portParameterReferences.add(portParameterReference);
				}

				/* Create a list of AC Expressions */
				List<ACExpression> expressions = new LinkedList<ACExpression>();
				expressions.addAll(portParameterReferences);

				/* Create the ACFusion */
				ACFusion acFusion = ArchitectureInstanceBuilder.createACFusion(expressions);

				/* Create an empty list of interactions */
				List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

				// Collections.reverse(portParameters);

				/* Create the connector type */
				connectorType = ArchitectureInstanceBuilder.createConnectorType(instance, connectorTypeName,
						portParameters, acFusion, null);

				/* Update counter */
				connectorTypeCounter++;
			}

			/* If match we just have to create an instance */
			String connectorInstanceName = connectorType.getName() + ConstantFields.INSTANCE
					+ String.valueOf(connectorTypeInstances.get((String) connectorType.getName()));
			/* Create the connector instance */
			ArchitectureInstanceBuilder.createConnectorInstance(instance, connectorInstanceName, connectorType,
					instance.getBipFileModel().getRootType(), actualPortParameters);
			/* Update the counter */
			connectorTypeInstances.put(connectorType.getName(),
					connectorTypeInstances.get((String) connectorType.getName()) + 1);
		}
		/* End. Generate BIP file */
		instance.generateBipFile(pathToSaveBIPFile);
		/* End. Generate configuration file */
		instance.generateConfigurationFile(pathToSaveConfFile);

		return instance;
	}

	public static void main(String[] args) {
		String pathToConfFile1 = "/home/vladimir/Architecture_examples/Compose/Conf12.txt";
		String pathToConfFile2 = "/home/vladimir/Architecture_examples/Compose/Conf13.txt";

		try {
			ArchitectureInstance instance1 = new ArchitectureInstance(pathToConfFile1, true);
			ArchitectureInstance instance2 = new ArchitectureInstance(pathToConfFile2, true);

			String systemName = "MutualExclusion123";
			String rootTypeName = "Mutex";
			String rootInstanceName = "mutex";

			String pathToSaveBIPFile = "/home/vladimir/Architecture_examples/Compose/MutualExclusion123.bip";
			String pathToSaveConfFile = "/home/vladimir/Architecture_examples/Compose/ConfFile.txt";

			ArchitectureComposer.compose(instance1, instance2, systemName, rootTypeName, rootInstanceName,
					pathToSaveBIPFile, pathToSaveConfFile);

		} catch (ConfigurationFileException | ArchitectureExtractorException | InvalidComponentNameException
				| InvalidConnectorTypeNameException | InvalidPortParameterNameException
				| IllegalPortParameterReferenceException | IOException e) {
			e.printStackTrace();
		}
	}
}
