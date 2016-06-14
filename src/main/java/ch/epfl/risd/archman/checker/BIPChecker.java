package ch.epfl.risd.archman.checker;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.IllegalComponentException;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.BIPFileModel;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;

public class BIPChecker {

	/**
	 * This method checks whether the given component type exists or not in the
	 * given BIP system
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param componentType
	 *            - the type of the component it is searching for
	 * @return true if the component type exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean componentTypeExists(BIPFileModel bipFileModel, ComponentType componentType)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllComponentTypesNames(bipFileModel).contains(componentType.getName());
	}

	/**
	 * This method checks whether the given component type exists or not in the
	 * given BIP system, given by its name.
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param componentTypeName
	 *            - The name of the components type it is searching for
	 * @return true if the component type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean componentTypeExists(BIPFileModel bipFileModel, String componentTypeName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllComponentTypesNames(bipFileModel).contains(componentTypeName);
	}

	/**
	 * This method checks whether the given port type exists or not in the given
	 * BIP system
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param portType
	 *            - The type of the port it is searching for
	 * @return true if the port type exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean portTypeExists(BIPFileModel bipFileModel, PortType portType)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllPortTypesNames(bipFileModel).contains(portType.getName());
	}

	/**
	 * This method checks whether the given port type exists or not in the given
	 * BIP system, given by its name
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param portTypeName
	 *            - The name of the port type it is searching for
	 * @return true if the port type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean portTypeExists(BIPFileModel bipFileModel, String portTypeName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllPortTypesNames(bipFileModel).contains(portTypeName);
	}

	/**
	 * This method checks whether the given connector type exists or not in the
	 * given BIP system
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param connectorType
	 *            - The type of the connector it is searching for
	 * @return true if the connector type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean connectorTypeExists(BIPFileModel bipFileModel, ConnectorType connectorType)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllConnectorTypesNames(bipFileModel).contains(connectorType.getName());
	}

	/**
	 * This method checks whether the given connector type exists or not in the
	 * given BIP system, given by its name
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param connectorTypeName
	 *            - The name of the connector type it is searching for
	 * @return true if the connector type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean connectorTypeExists(BIPFileModel bipFileModel, String connectorTypeName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllConnectorTypesNames(bipFileModel).contains(connectorTypeName);
	}

	/**
	 * This method checks whether the given instance of component type exists or
	 * not in the given BIP system
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param component
	 *            - The instance of the component it is searching for
	 * @return true if the instance of the component exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean componentExists(BIPFileModel bipFileModel, Component component)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllComponentsNames(bipFileModel).contains(component.getName());
	}

	/**
	 * This method checks whether the given instance of component type exists or
	 * not in the given BIP system, given its name.
	 * 
	 * @param bipFileModel
	 *            - the BIP system to check
	 * @param componentName
	 *            - The name of the component instance it is searching for
	 * @return true if the instance of the component exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean componentExists(BIPFileModel bipFileModel, String componentName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllComponentsNames(bipFileModel).contains(componentName);
	}

	/**
	 * This method checks whether the given instance of a port type exists or
	 * not in the given component
	 * 
	 * @param port
	 *            - the instance of the port it is searching for
	 * @param component
	 *            - The component where the port should be
	 * @return true if the port instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean portExists(Port port, Component component) throws ArchitectureExtractorException {
		return BIPExtractor.getComponentPortNames(component).contains(port.getName());
	}

	/**
	 * * This method checks whether the given instance of a port type exists or
	 * not in the given component, given by its name
	 * 
	 * @param bipFileModel
	 *            - the given BIP system
	 * @param portName
	 *            -The name of the port instance it is searching for
	 * @param componentName
	 *            - The name of the component, where the port should be
	 * @return true if the port instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean portExists(BIPFileModel bipFileModel, String portName, String componentName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getComponentPortNames(BIPExtractor.getComponentByName(bipFileModel, componentName))
				.contains(portName);
	}

	/**
	 * This method checks whether the given instance of a connector type exists
	 * or not in the given BIP system
	 * 
	 * @param bipFileModel
	 *            - the given BIP system
	 * @param connector
	 *            - The instance of the connector it is searching for
	 * @return true if the connector instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean connectorExists(BIPFileModel bipFileModel, Connector connector)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllConnectorsNames(bipFileModel).contains(connector.getName());
	}

	/**
	 * This method checks whether the given instance of a connector type exists
	 * or not in the given BIP system, given its name
	 * 
	 * @param bipFileModel
	 *            - the given BIP system
	 * @param connectorName
	 *            - The name of the connector instance it is searching for
	 * @return true if the connector instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public static boolean connectorExists(BIPFileModel bipFileModel, String connectorName)
			throws ArchitectureExtractorException {
		return BIPExtractor.getAllConnectorsNames(bipFileModel).contains(connectorName);
	}

	/**
	 * 
	 * @param name
	 * @param component
	 * @return
	 * @throws IllegalComponentException
	 */
	public static boolean stateExists(String name, Component component) throws IllegalComponentException {
		return BIPExtractor.getAtomStatesNames(component).contains(name);
	}
}
