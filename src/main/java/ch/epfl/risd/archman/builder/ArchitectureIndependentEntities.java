package ch.epfl.risd.archman.builder;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionStatesException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidStateNameException;
import ch.epfl.risd.archman.exceptions.ListEmptyException;
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
import ujf.verimag.bip.Core.Behaviors.Behavior;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.DefinitionBinding;
import ujf.verimag.bip.Core.Behaviors.Expression;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinition;
import ujf.verimag.bip.Core.Behaviors.PortDefinitionReference;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
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

/**
 * This interface defines method for creating entities which are architecture
 * independent, i.e. they can exist without architecture instance
 */
public interface ArchitectureIndependentEntities {

	/* Behavior part */

	/**
	 * This method creates a PetriNet, which is subclass of Behavior. The
	 * PetriNet is describing the behavior of one atom, that is its initial
	 * state and action, then all possible states and the transitions between
	 * them.
	 * 
	 * @param initialStates
	 *            - the list of initial states when the atom is initialized
	 * @param initialAction
	 *            - the first action which will be executed when the atom is
	 *            initialized
	 * @param states
	 *            - list of all states in the atom
	 * @param transitions
	 *            - list of all transitions in the atom
	 * @return the newly created PetriNet(subclass of Behavior)
	 * @throws InvalidStateNameException
	 * @throws ListEmptyException
	 * @throws IllegalTransitionStatesException
	 */
	public Behavior createBehavior(List<State> initialStates, Action initialAction, List<State> states,
			List<Transition> transitions)
					throws InvalidStateNameException, ListEmptyException, IllegalTransitionStatesException;

	/**
	 * This method returns a DataParameter. The DataParameter is used as the
	 * parameter for example in Port Types.
	 * 
	 * @param name
	 *            - name of the data parameter
	 * @param type
	 *            - the opaque type(integer, float, double) of the parameter
	 * @return the newly created DataParameter object
	 */
	public DataParameter createDataParameter(String name, OpaqueElement type);

	/**
	 * This method returns a PortDefinition class. This class defines how can we
	 * access(by which name) one instance of some Port Type in some Atom Type.
	 * 
	 * @param interfaceName
	 *            - the name of the port which will serve as an interface to the
	 *            defined port in some Atom Type
	 * @param type
	 *            - the type of the port for which the definition is created
	 * @return the newly created PortDefinition object
	 */
	public PortDefinition createPortDefinition(String interfaceName, PortType type);

	/**
	 * This method returns DefinitionBinding. We use this class to bind one
	 * instance of port in some atom with the definition of port
	 * 
	 * @param portDefinition
	 *            - object of type PortDefinition, which describes the
	 *            definition of the port
	 * @return the newly created DefinitionBinding object
	 */
	public DefinitionBinding createDefinitionBinding(PortDefinition portDefinition);

	/**
	 * This method returns new instance of Port.
	 * 
	 * @param innerName
	 *            - the name of the port in the scope of one atom
	 * @param interfaceName
	 *            - the name used to access the port outside the atom
	 * @param type
	 *            - the type of the port
	 * @return the newly created Port object
	 */
	public Port createPort(String innerName, String interfaceName, PortType type);

	/**
	 * This method returns PortDefinitionReference, which is used if we want to
	 * reference the definition of the port somewhere else in the model
	 * 
	 * @param portDefinition
	 *            - object of type PortDefinition, which describes the
	 *            definition of the port
	 * @return
	 */
	public PortDefinitionReference createPortDefinitionReference(PortDefinition portDefinition);;

	/**
	 * This method returns State, which can be inserted in some Atom Type
	 * 
	 * @param name
	 *            - the name of the created state
	 * @return object of type State
	 */
	public State createState(String name);

	/**
	 * This method returns a list of States, which can be inserted in some Atom
	 * Type
	 * 
	 * @param names
	 *            - list of names of states
	 * @return List of objects of type State
	 */
	public List<State> createStates(List<String> names);

	/**
	 * This method returns Transition between two states, and can be inserted in
	 * some Atom Type where we have the same states
	 * 
	 * @param portDefinitionReference
	 *            - which ports are involved in the transition
	 * @param origin
	 *            - the State where the Transition starts
	 * @param destination
	 *            - the State where the Transition ends
	 * @param guard
	 *            - the condition which need to be satisfied in order the
	 *            Transition to happen
	 * @param action
	 *            - the action which will be executed during the transition
	 * @return the newly created Transition
	 */
	public Transition createTransition(PortDefinitionReference portDefinitionReference, State origin, State destination,
			Expression guard, Action action);

	/**
	 * This method creates Variable, which can be inserted in some Atom Type.
	 * 
	 * @param name
	 *            - the name of the Variable
	 * @param type
	 *            - the opaque type(integer, float, double) of the variable
	 * @param isExternal
	 *            - whether the Variable is external or not
	 * @return the newly created Variable
	 */
	public Variable createVariable(String name, OpaqueElement type, boolean isExternal);

	/* Interaction part */

	/**
	 * 
	 * @param part
	 * @return
	 */
	public PartElementReference createPartElementReference(Part part);

	/**
	 * This method creates inner port reference, to pass as an input argument in
	 * the connector instance
	 * 
	 * @param partElementReference
	 *            - The target instance of part, which can be component or
	 *            connector instance
	 * @param port
	 *            - the port from the instance
	 * @return the newly created inner port reference
	 */
	public InnerPortReference createInnerPortReference(PartElementReference partElementReference, Port port);

	/**
	 * This method creates Connector from a given Type
	 * 
	 * @param name
	 *            - the name of the Connector
	 * @param type
	 *            - the Type of the Connector
	 * @param parent
	 *            - the Compound Type which is a parent
	 * @param actualPortParameters
	 *            - the list of input ports
	 * @return
	 */
	public Connector createConnector(String name, ConnectorType type, CompoundType parent,
			List<ActualPortParameter> actualPortParameters);

	/**
	 * This method returns Interaction between the involved ports passed as an
	 * input parameters in some Connector Type.
	 * 
	 * @param portParameters
	 *            - the involved ports in the interactions
	 * @return - new newly created Interaction
	 */
	public Interaction createInteraction(List<PortParameter> portParameters);

	/**
	 * This method returns Interaction Specification, which specifies the
	 * interaction which is happening in some Connector Type. The specification
	 * includes the involved port, the condition which must hold in order for up
	 * and down(this is BIP specific) actions to execute.
	 * 
	 * @param downAction
	 *            - the 'down' Action
	 * @param guard
	 *            - the condition Expression
	 * @param interaction
	 *            - the Interaction between ports
	 * @param upAction
	 *            - the 'up' Action
	 * @return the newly created Interaction Specification object
	 */
	public InteractionSpecification createInteractionSpecification(Action downAction, Expression guard,
			Interaction interaction, Action upAction);

	/**
	 * This method returns Port Parameter from a given Port Type, which can be
	 * passed as an input parameter in some Connector Type
	 * 
	 * @param portType
	 *            - the Port Type of the parameter
	 * @param name
	 *            - the name of the parameter
	 * @return the newly created Port Parameter object
	 */
	public PortParameter createPortParameter(PortType portType, String name);

	/* Expressions */

	/**
	 * This method returns Binary Expression, which means it has two sides, left
	 * and right and some operand between them(like =, !=, > etc.). Both sides
	 * are also expressions from any type.
	 * 
	 * @param leftOperand
	 *            - the left side expression
	 * @param rightOperand
	 *            - the right side expression
	 * @param operator
	 *            - the operator between left and right side
	 * @return the newly created Binary Expression object
	 */
	public BinaryExpression createBinaryExpression(Expression leftOperand, Expression rightOperand,
			BinaryOperator operator);

	/**
	 * This method returns Unary Expression, which means it has only one operand
	 * and one postfix of prefix operator(like ++, --); The operand is also
	 * another expression from any type.
	 * 
	 * @param operand
	 *            - the operand in the unary expression
	 * @param operator
	 *            - the operator in the expression
	 * @param isPostfix
	 *            - if true operand is postfix, otherwise is prefix
	 * @return the newly created Unary Expression object
	 */
	public UnaryExpression createUnaryExpression(Expression operand, UnaryOperator operator, boolean isPostfix);

	/**
	 * This method returns the Data Parameter reference to use somewhere as a
	 * reference
	 * 
	 * @param parameter
	 *            - the DataParameter object for which we want to create
	 *            reference
	 * @return - the newly created Data Parameter Reference object
	 */
	public DataParameterReference createDataParameterReference(DataParameter parameter);

	/**
	 * This method returns Variable Reference to use somewhere as a reference
	 * 
	 * @param variable
	 *            - the Variable object for which we want to create reference
	 * @return the newly created Variable Reference object
	 */
	public VariableReference createVariableReference(Variable variable);

	/**
	 * This method returns Port Parameter Reference to use somewhere as a
	 * reference
	 * 
	 * @param portParameter
	 *            - the Port Parameter object for which we want to create
	 *            reference
	 * @return - the newly created Port Parameter Reference object
	 */
	public PortParameterReference createPortParameterReference(PortParameter portParameter);

	/**
	 * This method returns Boolean Literal
	 * 
	 * @param value
	 *            - the value of the boolean literal either true or false
	 * @return the newly created Boolean Literal object
	 */
	public BooleanLiteral createBooleanLiteral(boolean value);

	/**
	 * This method returns Integer Literal
	 * 
	 * @param value
	 *            - the value of the integer literal
	 * @return the newly created Integer Literal
	 */
	public IntegerLiteral createIntegerLiteral(int value);

	/**
	 * This method returns Real Literal
	 * 
	 * @param value
	 *            - the value of the Real Literal
	 * @return the newly created Real Literal
	 */
	public RealLiteral createRealLiteral(double value);

	/**
	 * This function returns String Literal
	 * 
	 * @param value
	 *            - the value of the String Literal
	 * @return the newly created String Literal
	 */
	public StringLiteral createStringLiteral(String value);

	/* Actions part */

	/**
	 * This method returns Assignment Action, which means that this action
	 * assigns a value to a target by means of some operand.
	 * 
	 * @param target
	 *            - the target Data Reference (it could be Variable Reference,
	 *            Data Parameter Reference etc)
	 * @param value
	 *            - the value which will be assigned to the target
	 * @param operand
	 *            - the assignment operand(it could be =, +=, -= etc.)
	 * @return the newly created Assignment Action
	 */
	public AssignmentAction createAssignmentAction(DataReference target, Expression value, AssignType operand);

	/**
	 * This method returns If Action, which means that there is a condition, and
	 * action if the condition holds, and the action if the condition does not
	 * hold.
	 * 
	 * @param condition
	 *            - the testing condition
	 * @param ifCase
	 *            - the Action which will be executed if the condition holds
	 * @param elseCase
	 *            - the Action which will be executed if the condition does not
	 *            hold
	 * @return the newly created If Action
	 */
	public IfAction createIfAction(Expression condition, Action ifCase, Action elseCase);

	/**
	 * This method returns a Composite Action which is consisted of other
	 * Actions, which can be of any type.
	 * 
	 * @param actions
	 *            - the list of Actions making this Composite Action
	 * @return the newly created Composite Action
	 */
	public CompositeAction createCompositeAction(List<Action> actions);

	/* Port Expression part */
	/**
	 * This method returns ACFusion, which we use if we want to define more than
	 * one port in the Connector Type
	 * 
	 * @param portParamRef
	 *            - The list of Port Parameter References which need to be
	 *            defined
	 * @return the newly created ACFusion object
	 */
	public ACFusion createACFusion(List<ACExpression> expressions);

	public ACUnion createACUnion(List<ACExpression> expressions);

	public ACFusionNeutral createACFusionNeutral();

	public ACUnionNeutral createACUnionNeutral();

	public ACTyping createACTyping(ACTypingKind type, ACExpression expression);

	public AISynchro createAISynchro(List<AIExpression> expressions);

	public AIUnion createAIUnion(List<AIExpression> expressions);

	public AISynchroNeutral createAISynchroNeutral();

	public AIUnionNeutral createAIUnionNeutral();

	/* Modules Part */

	public OpaqueElement createOpaqueElement(String body, boolean isHeader);

}
