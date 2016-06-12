package ch.epfl.risd.archman.builder.helper;

import java.util.LinkedList;
import java.util.List;

import ch.epfl.risd.archman.exceptions.IllegalTransitionStatesException;
import ch.epfl.risd.archman.exceptions.InvalidStateNameException;
import ch.epfl.risd.archman.exceptions.ListEmptyException;
import ch.epfl.risd.archman.factories.Factories;
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
import ujf.verimag.bip.Core.Behaviors.impl.StateImpl;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.Interaction;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
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

public class BuilderHelperImpl implements BuilderHelper {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/************************************************************************/

	/**
	 * This method check whether in the list of States there are duplicates,
	 * i.e. States with a same name.
	 * 
	 * @param states
	 *            - a list of states for checking
	 * @return true if there are duplicates, false otherwise
	 */
	private boolean checkDuplicateStates(List<State> states) {

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

	/* Should revise this function. I am not sure is it logically true */
	private boolean checkTransitionStates(Transition transition, List<State> states) {

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

	/****************************************************************************/
	/* PUBLIC METHODS */
	/**
	 ***************************************************************************/

	@Override
	public Behavior createBehavior(List<State> initialStates, Action initialAction, List<State> states,
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

	@Override
	public DataParameter createDataParameter(String name, OpaqueElement type) {
		/* Create the new Data Parameter */
		DataParameter dataParameter = Factories.BEHAVIORS_FACTORY.createDataParameter();
		/* Set the name of the new data parameter */
		dataParameter.setName(name);
		/* Set the type */
		dataParameter.setType(type);

		return dataParameter;
	}

	@Override
	public PortDefinition createPortDefinition(String interfaceName, PortType type) {
		/* First create Port Definition */
		PortDefinition portDefinition = Factories.BEHAVIORS_FACTORY.createPortDefinition();
		/* set port type to the port definition */
		portDefinition.setType(type);
		/* Set the interface name */
		portDefinition.setName(interfaceName);

		return portDefinition;
	}

	@Override
	public DefinitionBinding createDefinitionBinding(PortDefinition portDefinition) {
		/* Create Definition Binding */
		DefinitionBinding binding = Factories.BEHAVIORS_FACTORY.createDefinitionBinding();
		/* Set definition to the binding */
		binding.setDefinition(portDefinition);

		return binding;
	}

	@Override
	public Port createPort(String innerName, String interfaceName, PortType type) {
		/* First create Port Definition */
		PortDefinition portDefinition = this.createPortDefinition(interfaceName, type);

		/* Create Definition Binding */
		DefinitionBinding binding = this.createDefinitionBinding(portDefinition);

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

	@Override
	public PortDefinitionReference createPortDefinitionReference(PortDefinition portDefinition) {
		/* Create port definition reference */
		PortDefinitionReference portDefinitionReference = Factories.BEHAVIORS_FACTORY.createPortDefinitionReference();
		/* set Port definition */
		portDefinitionReference.setTarget(portDefinition);

		return portDefinitionReference;
	}

	@Override
	public State createState(String name) {
		/* Create the state */
		State state = (StateImpl) Factories.BEHAVIORS_FACTORY.createState();
		/* Set the name of the state */
		state.setName(name);

		return state;
	}

	@Override
	public List<State> createStates(List<String> names) {
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

	@Override
	public Transition createTransition(PortDefinitionReference portDefinitionReference, State origin, State destination,
			Expression guard, Action action) {
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

	@Override
	public Variable createVariable(String name, OpaqueElement type, boolean isExternal) {
		/* Create the new variable */
		Variable variable = Factories.BEHAVIORS_FACTORY.createVariable();
		/* Set the name of the variable */
		variable.setName(name);
		/* Set the type of the variable */
		variable.setType(type);
		/* Set whether the variable os external or not */
		variable.setIsExternal(isExternal);

		return variable;
	}

	@Override
	public Connector createConnector(String name, ConnectorType type, CompoundType parent) {
		/* Create the new Connector */
		Connector connector = Factories.INTERACTIONS_FACTORY.createConnector();
		/* Set the name of the Connector */
		connector.setName(name);
		/* set the type of the Connector */
		connector.setType(type);
		/* Set the parent of the Connector */
		connector.setCompoundType(parent);

		return connector;
	}

	@Override
	public Interaction createInteraction(List<PortParameter> portParameters) {
		/* Create the new interaction */
		Interaction interaction = Factories.INTERACTIONS_FACTORY.createInteraction();

		/* Create Port Parameter References */
		List<PortParameterReference> portParamRefs = new LinkedList<PortParameterReference>();
		for (PortParameter p : portParameters) {
			PortParameterReference portParamRef = this.createPortParameterReference(p);
			portParamRefs.add(portParamRef);
		}

		/* Set the involved ports in the interaction */
		interaction.getPort().addAll(portParamRefs);

		return interaction;
	}

	@Override
	public InteractionSpecification createInteractionSpecification(Action downAction, Expression guard,
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

	@Override
	public PortParameter createPortParameter(PortType portType, String name) {
		/* Create the new Port Parameter */
		PortParameter portParam = Factories.INTERACTIONS_FACTORY.createPortParameter();
		/* set the port type of the port parameter */
		portParam.setType(portType);
		/* Set name */
		portParam.setName(name);

		return portParam;
	}

	@Override
	public BinaryExpression createBinaryExpression(Expression leftOperand, Expression rightOperand,
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

	@Override

	public UnaryExpression createUnaryExpression(Expression operand, UnaryOperator operator, boolean isPostfix) {
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

	@Override
	public DataParameterReference createDataParameterReference(DataParameter parameter) {
		/* Create the new data parameter reference */
		DataParameterReference dataParameterReference = Factories.EXPRESSIONS_FACTORY.createDataParameterReference();
		/* Set the target parameter */
		dataParameterReference.setTargetParameter(parameter);

		return dataParameterReference;
	}

	@Override
	public VariableReference createVariableReference(Variable variable) {
		/* Create the new variable reference */
		VariableReference variableReference = Factories.EXPRESSIONS_FACTORY.createVariableReference();
		/* Set the variable */
		variableReference.setTargetVariable(variable);

		return variableReference;
	}

	@Override
	public PortParameterReference createPortParameterReference(PortParameter portParameter) {
		/* Create the new port parameter reference */
		PortParameterReference portParamRef = Factories.INTERACTIONS_FACTORY.createPortParameterReference();
		/* Set the target */
		portParamRef.setTarget(portParameter);

		return portParamRef;
	}

	@Override
	public BooleanLiteral createBooleanLiteral(boolean value) {
		/* Create new boolean literal */
		BooleanLiteral booleanLiteral = Factories.EXPRESSIONS_FACTORY.createBooleanLiteral();
		/* set the value of the literal */
		booleanLiteral.setBValue(value);

		return booleanLiteral;
	}

	@Override
	public IntegerLiteral createIntegerLiteral(int value) {
		/* Create new Integer Literal */
		IntegerLiteral integerLiteral = Factories.EXPRESSIONS_FACTORY.createIntegerLiteral();
		/* Set the value of the literal */
		integerLiteral.setIValue(value);

		return integerLiteral;
	}

	@Override
	public RealLiteral createRealLiteral(double value) {
		/* Create new real literal */
		RealLiteral realLiteral = Factories.EXPRESSIONS_FACTORY.createRealLiteral();
		/* Set the value */
		realLiteral.setRValue(String.valueOf(value));

		return realLiteral;
	}

	@Override
	public StringLiteral createStringLiteral(String value) {
		/* Create new string literal */
		StringLiteral stringLiteral = Factories.EXPRESSIONS_FACTORY.createStringLiteral();
		/* set the value of the literal */
		stringLiteral.setSValue(value);

		return stringLiteral;
	}

	@Override
	public AssignmentAction createAssignmentAction(DataReference target, Expression value, AssignType operand) {
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

	@Override
	public IfAction createIfAction(Expression condition, Action ifCase, Action elseCase) {
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

	@Override
	public CompositeAction createCompositeAction(List<Action> actions) {
		/* Create the composite action */
		CompositeAction compositeAction = Factories.ACTIONS_FACTORY.createCompositeAction();
		/* Set the list of all sub actions */
		compositeAction.getContent().addAll(actions);

		return compositeAction;
	}

	@Override
	public ACFusion createACFusion(List<ACExpression> expressions) {
		/* Create new ACFusion */
		ACFusion acFusion = Factories.PORT_EXP_FACTORY.createACFusion();
		/* Set the list of AC expressions */
		acFusion.getOperand().addAll(expressions);

		return acFusion;
	}

	@Override
	public ACUnion createACUnion(List<ACExpression> expressions) {
		/* Create new ACUnion */
		ACUnion acUnion = Factories.PORT_EXP_FACTORY.createACUnion();
		/* Set the list of AC expressions */
		acUnion.getOperand().addAll(expressions);

		return acUnion;
	}

	@Override
	public ACFusionNeutral createACFusionNeutral() {
		/* Create ACFusionNeutral */
		ACFusionNeutral acFusionNeutral = Factories.PORT_EXP_FACTORY.createACFusionNeutral();

		return acFusionNeutral;
	}

	@Override
	public ACUnionNeutral createACUnionNeutral() {
		/* Create ACUnionNeutral */
		ACUnionNeutral acUnionNeutral = Factories.PORT_EXP_FACTORY.createACUnionNeutral();

		return acUnionNeutral;
	}

	@Override
	public ACTyping createACTyping(ACTypingKind type, ACExpression expression) {
		/* Create new ACTyping */
		ACTyping acTyping = Factories.PORT_EXP_FACTORY.createACTyping();
		/* Set the AC expression */
		acTyping.setOperand(expression);
		/* Set the type of the typing, sync or trigger */
		acTyping.setType(type);

		return acTyping;
	}

	@Override
	public AISynchro createAISynchro(List<AIExpression> expressions) {
		/* Create new AISynchro */
		AISynchro aiSynchro = Factories.PORT_EXP_FACTORY.createAISynchro();
		/* Set the list of AI expressions */
		aiSynchro.getOperand().addAll(expressions);

		return aiSynchro;
	}

	@Override
	public AIUnion createAIUnion(List<AIExpression> expressions) {
		/* Create new AIUnion */
		AIUnion aiUnion = Factories.PORT_EXP_FACTORY.createAIUnion();
		/* Set the list of AI expressions */
		aiUnion.getOperand().addAll(expressions);

		return aiUnion;
	}

	@Override
	public AISynchroNeutral createAISynchroNeutral() {
		/* Create new AISynchroNeutral */
		AISynchroNeutral aiSynchroNeutral = Factories.PORT_EXP_FACTORY.createAISynchroNeutral();

		return aiSynchroNeutral;
	}

	@Override
	public AIUnionNeutral createAIUnionNeutral() {
		/* Create new AIUnionNeutral */
		AIUnionNeutral aiUnionNeutral = Factories.PORT_EXP_FACTORY.createAIUnionNeutral();

		return aiUnionNeutral;
	}

	@Override
	public OpaqueElement createOpaqueElement(String body, boolean isHeader) {
		/* create the new opaque element */
		OpaqueElement element = Factories.MODULES_FACTORY.createOpaqueElement();
		/* set the body of the element */
		element.setBody(body);
		/* set is it header */
		element.setIsHeader(isHeader);
		return element;
	}
}
