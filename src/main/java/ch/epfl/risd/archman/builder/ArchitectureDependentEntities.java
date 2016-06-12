package ch.epfl.risd.archman.builder;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionPortException;
import ch.epfl.risd.archman.exceptions.InvalidAtomTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidCompoundTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidVariableNameException;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.Behavior;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.PortExpressions.PortExpression;

/**
 * This interface defines method for creating entities which are architecture
 * dependent, i.e. they cannot exist without architecture instance
 */
public interface ArchitectureDependentEntities {

	/**
	 * This method adds new component in the architecture
	 * 
	 * @param name
	 *            - the name of the component
	 * @param type
	 *            - the type of component, it can be AtomType or CompoundType
	 * @param parent
	 *            - the CompoundType parent of the component, if null the parent
	 *            is root
	 * @param isCoordinator
	 *            - whether or not the component is coordinator
	 * @throws ArchitectureExtractorException
	 * @throws InvalidComponentNameException
	 * @throws InvalidAtomTypeNameException
	 */
	public void addComponent(String name, ComponentType type, CompoundType parent, boolean isCoordinator)
			throws ArchitectureExtractorException, InvalidComponentNameException, InvalidAtomTypeNameException;

	/**
	 * This method creates new AtomType in the Architecture.
	 * 
	 * @param name
	 *            - the name of the new AtomType
	 * @param behavior
	 *            - the Behavior of the AtomType
	 * @param ports
	 *            - a list of Ports from certain PortType
	 * @param variables
	 *            - a list of Variables
	 * @return the newly created AtomType object
	 * @throws ArchitectureExtractorException
	 * @throws InvalidAtomTypeNameException
	 * @throws InvalidVariableNameException
	 * @throws InvalidPortNameException
	 * @throws IllegalTransitionPortException
	 */

	public AtomType createAtomicType(String name, Behavior behavior, List<Port> ports, List<Variable> variables)
			throws ArchitectureExtractorException, InvalidAtomTypeNameException, InvalidVariableNameException,
			InvalidPortNameException, IllegalTransitionPortException;

	/**
	 * This method makes copy of the given atom type
	 * 
	 * @param type
	 *            - The Atomic Type to copy
	 * @return the copy of the atomic type
	 * @throws ArchitectureExtractorException
	 */
	public AtomType copyAtomicType(AtomType type) throws ArchitectureExtractorException;

	/**
	 * This Method creates new CompondType in the Architecture(still not
	 * defined)
	 * 
	 * @param name
	 *            - the name of the new CompoundType
	 * @return the newly created CompoundType object
	 * @throws ArchitectureExtractorException
	 * @throws InvalidCompoundTypeNameException
	 */
	public CompoundType createCompoundType(String name)
			throws ArchitectureExtractorException, InvalidCompoundTypeNameException;

	/**
	 * This method makes copy of the given compound type
	 * 
	 * @param type
	 *            - The Compound type to copy
	 * @return the copy of the compound type
	 */
	public CompoundType copyCompoundType(ComponentType type);

	/**
	 * This method creates new PortType in the Architecture
	 * 
	 * @param name
	 *            - the name of the new PortType
	 * @param dataParameters
	 *            - list of Data Parameters(as arguments)
	 * @return the newly created PortType object
	 * @throws ArchitectureExtractorException
	 * @throws InvalidPortTypeNameException
	 */
	public PortType createPortType(String name, List<DataParameter> dataParameters)
			throws ArchitectureExtractorException, InvalidPortTypeNameException;

	/**
	 * This method makes copy of the given port type
	 * 
	 * @param type
	 *            - The Port type to copy
	 * @return the copy of the port type
	 */
	public PortType copyPortType(PortType type);

	/**
	 * This method creates new ConnectorType in the Architecture
	 * 
	 * @param connectorTypeName
	 *            - the name of the new ConnectorType
	 * @param portParameters
	 *            - list of Port Parameters used to define interactions
	 * @param portParameterReferences
	 * @param interactionSpecifications
	 *            - list of Interaction Specifications, which defines the
	 *            interaction
	 * @return the newly created ConnectorType object
	 * @throws ArchitectureExtractorException
	 * @throws InvalidConnectorTypeNameException
	 * @throws InvalidPortParameterNameException
	 * @throws IllegalPortParameterReferenceException
	 */
	public ConnectorType createConnectorType(String connectorTypeName, List<PortParameter> portParameters,
			PortExpression interactionDefinition, List<InteractionSpecification> interactionSpecifications)
					throws ArchitectureExtractorException, InvalidConnectorTypeNameException,
					InvalidPortParameterNameException, IllegalPortParameterReferenceException;

	/**
	 * This method makes copy of the given connector type
	 * 
	 * @param type
	 *            - The Connector Type to copy
	 * @return the copy of the Connector Type
	 * @throws ArchitectureExtractorException 
	 */
	public ConnectorType copyConnectorType(ConnectorType type) throws ArchitectureExtractorException;
	
	/**
	 * 
	 * @param components
	 * @param areCoordinators
	 * @throws ArchitectureExtractorException 
	 * @throws InvalidComponentNameException 
	 */
	public void insertComponents(List<Component> components, boolean areCoordinators) throws ArchitectureExtractorException, InvalidComponentNameException;
	
}
