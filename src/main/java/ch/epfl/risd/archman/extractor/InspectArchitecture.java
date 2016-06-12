package ch.epfl.risd.archman.extractor;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.IllegalComponentException;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;

/**
 * Interface which defines methods for inspecting the architecture, like
 * existing of some component, port and connector types, and also same instances
 * of these types
 */
public interface InspectArchitecture {

	/* Existing of types with two different input arguments */

	/**
	 * This method checks whether the given component type exists or not
	 * 
	 * @param componentType
	 *            - the type of the component it is searching for
	 * @return true if the component type exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean componentTypeExists(ComponentType componentType) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given component type exists or not, given
	 * by its name.
	 * 
	 * @param componentTypeName
	 *            - The name of the components type it is searching for
	 * @return true if the component type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean componentTypeExists(String componentTypeName) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given port type exists or not in the
	 * architecture.
	 * 
	 * @param portType
	 *            - The type of the port it is searching for
	 * @return true if the port type exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean portTypeExists(PortType portType) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given port type exists or not in the
	 * architecture, given by its name
	 * 
	 * @param portTypeName
	 *            - The name of the port type it is searching for
	 * @return true if the port type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean portTypeExists(String portTypeName) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given connector type exists or not in the
	 * architecture
	 * 
	 * @param connectorType
	 *            - The type of the connector it is searching for
	 * @return true if the connector type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean connectorTypeExists(ConnectorType connectorType) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given connector type exists or not in the
	 * architecture, given by its name
	 * 
	 * @param connectorTypeName
	 *            - The name of the connector type it is searching for
	 * @return true if the connector type exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean connectorTypeExists(String connectorTypeName) throws ArchitectureExtractorException;

	/* Existing of instances */

	/**
	 * This method checks whether the given instance of component type exists or
	 * not
	 * 
	 * @param component
	 *            - The instance of the component it is searching for
	 * @return true if the instance of the component exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean componentExists(Component component) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given instance of component type exists or
	 * not, given its name.
	 * 
	 * @param componentName
	 *            - The name of the component instance it is searching for
	 * @return true if the instance of the component exist, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean componentExists(String componentName) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given instance of a port type exists or
	 * not in the architecture
	 * 
	 * @param port
	 *            - the instance of the port it is searching for
	 * @param component
	 *            - The component where the port should be
	 * @return true if the port instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean portExists(Port port, Component component) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given instance of a port type exists or
	 * not in the architecture, given its name.
	 * 
	 * @param portName
	 *            - The name of the port instance it is searching for
	 * @param componentName
	 *            - The name of the component, where the porth should be
	 * @return true if the port instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean portExists(String portName, String componentName) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given instance of a connector type exists
	 * or not in the architecture
	 * 
	 * @param connector
	 *            - The instance of the connector it is searching for
	 * @return true if the connector instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean connectorExists(Connector connector) throws ArchitectureExtractorException;

	/**
	 * This method checks whether the given instance of a connector type exists
	 * or not in the architecture, given its name
	 * 
	 * @param connectorName
	 *            - The name of the connector instance it is searching for
	 * @return true if the connector instance exists, false otherwise
	 * @throws ArchitectureExtractorException
	 */
	public boolean connectorExists(String connectorName) throws ArchitectureExtractorException;

	/* States of atoms */

	public boolean stateExists(String name, Component atom) throws IllegalComponentException;

}
