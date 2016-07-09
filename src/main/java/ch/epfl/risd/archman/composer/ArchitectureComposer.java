package ch.epfl.risd.archman.composer;

import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import ch.epfl.risd.archman.builder.ArchitectureInstanceBuilder;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.BIPFileModel;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.PortParameter;

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
			/* Get the port instance name */
			String portInstanceName = intPort.split("\\.")[1];
			/* Add the port */
			resultingPorts.add(BIPExtractor.getPortByName(bipFileModel, portInstanceName));
		}

		/* Take all connector types */
		List<ConnectorType> allConnectorTypes = BIPExtractor.getAllConnectorTypes(bipFileModel);

		/* Iterate over them */
		for (ConnectorType connType : allConnectorTypes) {
			/* Get all port parameters */
			List<PortParameter> portParameters = connType.getPortParameter();
			List<Port> tempPorts = resultingPorts;
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
	 */
	public static ArchitectureInstance compose(ArchitectureInstance instance1, ArchitectureInstance instance2,
			BIPFileModel architectureInstanceBIPFile, String pathToSaveBIPFile, String pathToSaveConfFile)
			throws ArchitectureExtractorException, InvalidComponentNameException {

		/* 0.Create an empty architecture instance */
		ArchitectureInstance instance = new ArchitectureInstance(architectureInstanceBIPFile);

		/* 1.Take all Port Types and plug them */
		List<PortType> allPortTypes = new LinkedList<PortType>();
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(instance1.getBipFileModel()));
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(instance2.getBipFileModel()));
		ArchitectureInstanceBuilder.copyAllPortTypes(instance, allPortTypes);

		/* 2.Take all components except the roots and plug them */
		List<Component> allComponents = new LinkedList<Component>();
		allComponents.addAll(BIPExtractor.getAllComponents(instance1.getBipFileModel()));
		allComponents.addAll(BIPExtractor.getAllComponents(instance2.getBipFileModel()));

		for (Component c : allComponents) {
			/* Check if it is coordinator */
			boolean isCoordinator = instance1.getCoordinators().contains(c.getName())
					|| instance2.getCoordinators().contains(c.getName());

			if (c instanceof AtomType) {
				/* Copy or retrieve the atomic type */
				AtomType atomType = ArchitectureInstanceBuilder.copyAtomicType(instance, (AtomType) c.getType());
				/* Create an instance of the atomic type */
				ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), atomType,
						instance.getBipFileModel().getRootType(), isCoordinator);

			} else {
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

		/* 3. Match the interactions with connector types */

		/* Mock: Interactions are given, but still they have to be calculated */
		List<String> interactions = new LinkedList<String>();
		interactions.add("a b");
		interactions.add("c d");

		/* Counter for the connector type */
		int connectorTypeCounter = 1;
		/* Map for counting the connector type instances */
		Hashtable<String, Integer> connectorTypeInstances = new Hashtable<String, Integer>();

		/* Iterate the interactions */
		for (String interaction : interactions) {
			if (ArchitectureComposer.matchInteraction(instance.getBipFileModel(), interaction) == null) {
				/* If no match we have to create new connector type */
				
				/* Name of the connector type */
				String connectorTypeName = ConstantFields.CONNECTOR_TYPE + String.valueOf(connectorTypeCounter);
				

				connectorTypeCounter++;
			} else {

			}
		}

		return instance;
	}

	public static void main(String[] args) {

		String interaction = "C.take B1.finish B.begin";
		String archStylePath = "/home/vladimir/Architecture_examples/Archive1/Mutex/AEConf.txt";

		try {
			ArchitectureStyle architectureStyle = new ArchitectureStyle(archStylePath);

			System.out.println(ArchitectureComposer.matchInteraction(architectureStyle.getBipFileModel(), interaction));

		} catch (FileNotFoundException | ConfigurationFileException | ArchitectureExtractorException e) {
			e.printStackTrace();
		}

	}

}
