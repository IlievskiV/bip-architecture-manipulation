package ch.epfl.risd.archman.extractor;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ComponentTypeNotFoundException;
import ch.epfl.risd.archman.exceptions.IllegalComponentException;
import ujf.verimag.bip.Core.Behaviors.AbstractTransition;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;

public interface Extractor {

	/* Components */

	/**
	 * This method prints the structure of the architecture
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printStructure() throws ArchitectureExtractorException;

	/**
	 * This method returns a list of all Components in the Architecture
	 * 
	 * @return - list of all Components
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getAllComponents() throws ArchitectureExtractorException;

	/**
	 * This method returns all Component names in the Architecture
	 * 
	 * @return List of all Component names
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllComponentsNames() throws ArchitectureExtractorException;

	/**
	 * This method returns all Atomic Components in the Architecture
	 * 
	 * @return List of Atomic Components
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getAllAtoms() throws ArchitectureExtractorException;

	/**
	 * This method prints all Atom Components in the Architecture
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printAtoms() throws ArchitectureExtractorException;

	/**
	 * This method returns all Compound Components in the Architecture
	 * 
	 * @return List of all Compound Types
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getAllCompounds() throws ArchitectureExtractorException;

	/**
	 * This method prints all Compound Components in the Architecture
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printCompounds() throws ArchitectureExtractorException;

	/**
	 * This method returns all Component Types in the Architecture
	 * 
	 * @return List of all Component Types
	 * @throws ArchitectureExtractorException
	 */
	public List<ComponentType> getAllComponentTypes() throws ArchitectureExtractorException;

	/**
	 * This method returns names of all Component Types in the Architecture
	 * 
	 * @return
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllComponentTypesNames() throws ArchitectureExtractorException;

	/* Atom and Compound Types */

	/**
	 * This method returns all Atom Types in the Architecture
	 * 
	 * @return a list of Atom Type
	 * @throws ArchitectureExtractorException
	 */
	public List<AtomType> getAllAtomTypes() throws ArchitectureExtractorException;

	/**
	 * 
	 * @param atomType
	 * @return
	 * @throws ComponentTypeNotFoundException
	 * @throws ArchitectureExtractorException
	 */
	public AtomType getAtomTypeByName(String atomTypeName) throws ComponentTypeNotFoundException, ArchitectureExtractorException;

	/**
	 * This method returns all Compound Types in the Architectures
	 * 
	 * @return a list of Compound Type
	 * @throws ArchitectureExtractorException
	 */
	public List<CompoundType> getAllCompoundTypes() throws ArchitectureExtractorException;

	/**
	 * This method returns a Component with a given name
	 * 
	 * @param name
	 *            - the name of the component
	 * @return a Component
	 * @throws ArchitectureExtractorException
	 */
	public Component getComponentByName(String name) throws ArchitectureExtractorException;

	/**
	 * This method returns a list of all Components of a given type
	 * 
	 * @param type
	 *            - the Type of the Component
	 * @return a list of Components
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getComponentsByType(String type) throws ArchitectureExtractorException;

	/* States */

	/**
	 * This method returns all States in a particular Atomic Component
	 * 
	 * @param atom
	 * @return a list of States
	 * @throws IllegalComponentException
	 */
	public List<State> getAtomStates(Component atom) throws IllegalComponentException;

	/**
	 * This method prints all states of a particular atom
	 * 
	 * @param atom
	 *            - The atom which the states will be printed
	 * @throws IllegalComponentException
	 */
	public void printAtomStates(Component atom) throws IllegalComponentException;

	/**
	 * This method returns a list of all atom's state names
	 * 
	 * @param atom
	 *            - The atom which state names will be retrieved
	 * @return The list of all atom's state names
	 * @throws IllegalComponentException
	 */
	public List<String> getAtomStatesNames(Component atom) throws IllegalComponentException;

	/* Transitions */

	/**
	 * This method returns a list of all transitions in one atom
	 * 
	 * @param atom
	 *            - The atom which transitions should be retrieved
	 * @return The list of all transitions
	 * @throws IllegalComponentException
	 */
	public List<Transition> getAtomTransitions(Component atom) throws IllegalComponentException;

	/**
	 * This method prints the atom transitions together with the guards and
	 * actions of the transition.
	 * 
	 * @param atom
	 *            - The atom which transitions will be printed
	 * @throws IllegalComponentException
	 */
	public void printAtomTransitions(Component atom) throws IllegalComponentException;

	/**
	 * This method returns all origin states of a particular transition
	 * 
	 * @param transition
	 *            - The transition which origin states should be retrieved
	 * @return The list of all origin states
	 */
	public List<State> getTransitionOrigins(Transition transition);

	/**
	 * This method returns all of the destination states of a particular
	 * transition
	 * 
	 * @param transition
	 *            - The transition which destination states should be retrieved
	 * @return The list of all destination states
	 */
	public List<State> getTransitionDestinations(Transition transition);

	/**
	 * This method returns all incoming transitions of a particular state
	 * 
	 * @param state
	 *            - The state which incoming transitions should be retrieved
	 * @return - The list of all incoming transitions
	 */
	public List<Transition> getIncomingTransitions(State state);

	/**
	 * This method returns all outgoing transitions of a particular state
	 * 
	 * @param state
	 *            - The state which outgoing transitions should be retrieved
	 * @return The list of all outgoing transitions
	 */
	public List<AbstractTransition> getOutgoingTransitions(State state);

	/* Ports */

	/**
	 * This method returns all ports in the architecture
	 * 
	 * @return The list of all ports in the architecture
	 * @throws ArchitectureExtractorException
	 */
	public List<Port> getAllPorts() throws ArchitectureExtractorException;

	/**
	 * This method prints the name of each port in the architecture, together
	 * with the type of the port and the type of the component in which it
	 * belongs
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printAllPorts() throws ArchitectureExtractorException;

	/**
	 * This method returns a list of all port names in the architecture
	 * 
	 * @return The list of all ports in the architecture
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllPortNames() throws ArchitectureExtractorException;

	/**
	 * This method returns all port types in the architecture
	 * 
	 * @return The list of all port types in the architecture
	 * @throws ArchitectureExtractorException
	 */
	public List<PortType> getAllPortTypes() throws ArchitectureExtractorException;

	/**
	 * This method returns a list of all port type names in the architecture
	 * 
	 * @return The list of all port type names
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllPortTypesNames() throws ArchitectureExtractorException;

	/**
	 * This method returns a port with a given name
	 * 
	 * @param name
	 *            - The name of the port it is searching for
	 * @return - The port with the specified name
	 * @throws ArchitectureExtractorException
	 */
	public Port getPortByName(String name) throws ArchitectureExtractorException;

	public PortType getPortTypeByName(String name) throws ArchitectureExtractorException;

	/**
	 * This method returns all ports with the given type in the architecture
	 * 
	 * @param type
	 *            - The port type
	 * @return The list of all ports
	 * @throws ArchitectureExtractorException
	 */
	public List<Port> getPortsByType(String type) throws ArchitectureExtractorException;

	/**
	 * This method returns all ports in one component
	 * 
	 * @param component
	 *            - the component which ports should be retrieved
	 * @return The list of all ports
	 * @throws ArchitectureExtractorException
	 */
	public List<Port> getComponentPorts(Component component) throws ArchitectureExtractorException;

	/**
	 * This method returns the names of the ports in one component
	 * 
	 * @param component
	 *            -
	 * @return The list of all ports names
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getComponentPortNames(Component component) throws ArchitectureExtractorException;

	/**
	 * This method prints all ports of the component
	 * 
	 * @param component
	 *            - The component which ports should be printed
	 * @throws ArchitectureExtractorException
	 */
	public void printComponentPorts(Component component) throws ArchitectureExtractorException;

	/**
	 * This method returns all variables of a particular port
	 * 
	 * @param port
	 *            - The port which variables should be retrieved
	 * @return - The list of all variables
	 */
	public List<DataParameter> getPortVariables(Port port);

	/**
	 * This method prints all variables in a port
	 * 
	 * @param port
	 *            - The port which variables should be printed
	 */
	public void printPortVariables(Port port);

	/* Component variables */

	/**
	 * This method returns all variables of a particular atom
	 * 
	 * @param atom
	 *            - The atom which variables should be retrieved
	 * @return - The list of all variables
	 * @throws IllegalComponentException
	 */
	public List<Variable> getComponentVariables(Component atom) throws IllegalComponentException;

	/**
	 * This method prints all variables of a particular component, together with
	 * the type of the variable(integer, float, double) and its initial value if
	 * exist
	 * 
	 * @param atom
	 *            - The atom which variables should be printed
	 * @throws IllegalComponentException
	 */
	public void printComponentVariables(Component atom) throws IllegalComponentException;

	/* Connectors */

	/**
	 * This method returns all connectors in the architecture
	 * 
	 * @return The list of all connectors
	 * @throws ArchitectureExtractorException
	 */
	public List<Connector> getAllConnectors() throws ArchitectureExtractorException;

	/**
	 * This method prints the name of all connectors together with their type
	 * and the type of the compound in which they belong
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printConnectors() throws ArchitectureExtractorException;

	/**
	 * This method returns all connector names in the architecture
	 * 
	 * @return The list of all connector names
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllConnectorsNames() throws ArchitectureExtractorException;

	/**
	 * This method returns all connector types in the architecture
	 * 
	 * @return The list of all connector types
	 * @throws ArchitectureExtractorException
	 */
	public List<ConnectorType> getAllConnectorTypes() throws ArchitectureExtractorException;

	/**
	 * This method returns the names of all connector types in the architecture
	 * 
	 * @return The list of all connector type names
	 * @throws ArchitectureExtractorException
	 */
	public List<String> getAllConnectorTypesNames() throws ArchitectureExtractorException;

	/**
	 * This method returns a connector with a given name
	 * 
	 * @param name
	 *            - The name of the connector it is searching for
	 * @return The connector with the specified name
	 * @throws ArchitectureExtractorException
	 */
	public Connector getConnectorByName(String name) throws ArchitectureExtractorException;

	/**
	 * This method returns a connector type with a give name
	 * 
	 * @param name
	 *            - the name of the connector type
	 * @return
	 * @throws ArchitectureExtractorException
	 */
	public ConnectorType getConnectorTypeByName(String name) throws ArchitectureExtractorException;

	/**
	 * This method returns all connectors of a given type
	 * 
	 * @param type
	 *            - The type of the connectors it is searching for
	 * @return - The list of all connectors with a specified type
	 * @throws ArchitectureExtractorException
	 */
	public List<Connector> getConnectorsByType(String type) throws ArchitectureExtractorException;

	public String getConnectorInteractionDefinition(ConnectorType connectorType);

	public String getConnectorInteractionDefinition(String connectorTypeName);

}
