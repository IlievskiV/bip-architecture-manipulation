package ch.epfl.risd.archman.extractor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ComponentTypeNotFoundException;
import ch.epfl.risd.archman.exceptions.ConnectorNotFoundException;
import ch.epfl.risd.archman.exceptions.ConnectorTypeNotFoundException;
import ch.epfl.risd.archman.exceptions.IllegalComponentException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.exceptions.PortTypeNotFoundException;
import ch.epfl.risd.archman.model.BIPFileModel;
import ujf.verimag.bip.Core.ActionLanguage.Actions.AssignmentAction;
import ujf.verimag.bip.Core.ActionLanguage.Actions.CompositeAction;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.ArrayNavigationExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BinaryExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BinaryOperator;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BooleanLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.DataParameterReference;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.DataReference;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.FunctionCallExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.IntegerLiteral;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.UnaryExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.UnaryOperator;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.VariableReference;
import ujf.verimag.bip.Core.Behaviors.AbstractTransition;
import ujf.verimag.bip.Core.Behaviors.Action;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.BipType;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.Expression;
import ujf.verimag.bip.Core.Behaviors.PetriNet;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinitionReference;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Modules.OpaqueElement;
import ujf.verimag.bip.Core.PortExpressions.PortExpression;

public class ExtractorImpl implements Extractor, InspectArchitecture {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The BIP file model for extracting */
	private BIPFileModel bipFileModel;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Recursive function for printing the structure of the architecture
	 * 
	 * @param componentType
	 *            - current component type
	 * @param name
	 *            - the name of the current component type
	 * @param depth
	 *            - the depth of the current component type
	 * @throws ArchitectureExtractorException
	 */
	private void printStructureTemp(ComponentType componentType, String name, int depth)
			throws ArchitectureExtractorException {

		/* Print spaces according to the depth of the component */
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}

		/* Print the name and the type of the current component */

		if (name.equals("")) {
			System.out.println(componentType.getName());
		} else {
			System.out.println("Name: " + name + "(type: " + componentType.getName() + ")");
		}

		/*
		 * If the component is composite structure, it contains other
		 * subcomponents
		 */
		if (componentType instanceof CompoundType) {
			/* Get all subcomponents and recursively call the function */
			EList<Component> subcomponents = ((CompoundType) componentType).getSubcomponent();
			for (Component component : subcomponents) {
				printStructureTemp(component.getType(), component.getName(), depth + 1);
			}
		}
		/* If the component is atom, there aren't subcomponents */
		else if (componentType instanceof AtomType) {
			return;
		}
		/* Just in case the component is not defined */
		else {
			throw new ArchitectureExtractorException("Undefined component");
		}
	}

	/**
	 * This method recursively retrieves all subcomponents of particular
	 * component
	 * 
	 * @param component
	 *            - The current component
	 * @param components
	 *            - Empty list which should contain all subcomponents in the end
	 * @return The list of all subcomponents
	 * @throws ArchitectureExtractorException
	 */
	private List<Component> getAllComponentsTemp(Component component, List<Component> components)
			throws ArchitectureExtractorException {

		/* Check whether the resulting list is initialized */
		if (components == null) {
			throw new NullPointerException("The resulting list of components is not initialized");
		}

		/* Get the type of the current component */
		ComponentType componentType = component.getType();

		/* If the component is atom, there aren't subcomponents */
		if (componentType instanceof AtomType) {
			components.add(component);
		}
		/*
		 * If the component is composite structure, it contains other
		 * subcomponents
		 */
		else if (componentType instanceof CompoundType) {
			components.add(component);

			/* Get all subcomponents and recursively call the function */
			EList<Component> subcomponents = ((CompoundType) componentType).getSubcomponent();
			for (Component c : subcomponents) {
				components.add(c);
				getAllComponentsTemp(c, components);
			}
		}
		/* Just in case the component is not defined */
		else {
			throw new ArchitectureExtractorException("Undefined component");
		}

		return components;
	}

	/**
	 * This method recursively retrieves all ports of the given component type
	 * 
	 * @param componentType
	 *            - The current component type
	 * @param ports
	 *            - Empty list of ports which should contain all ports in the
	 *            end
	 * @return The list of all ports
	 * @throws ArchitectureExtractorException
	 */
	private List<Port> getAllPortsTemp(ComponentType componentType, List<Port> ports)
			throws ArchitectureExtractorException {
		/* Check whether the resulting list is initialized */
		if (ports == null) {
			throw new NullPointerException("The resulting list of all ports is not initalized");
		}

		/*
		 * If the component is composite structure, it contains other
		 * subcomponents that maybe contain some other ports
		 */
		if (componentType instanceof CompoundType) {
			/* Add all ports */
			ports.addAll(componentType.getPort());
			/* Get subcomponents */
			EList<Component> subcomponents = ((CompoundType) componentType).getSubcomponent();

			/* Iterate subcomponents */
			for (Component c : subcomponents) {
				getAllPortsTemp(c.getType(), ports);
			}
		}
		/*
		 * If the component is atom, there aren't subcomponents and connectors
		 */
		else if (componentType instanceof AtomType) {
			ports.addAll(componentType.getPort());
		}
		/* Just in case the component is not defined */
		else {
			throw new ArchitectureExtractorException("Undefined component");
		}

		return ports;
	}

	/**
	 * This method recursively retrieve all connectors in one component type
	 * 
	 * @param componentType
	 *            - The current component type
	 * @param connectors
	 *            - Empty list of connectors which should contain all connectors
	 *            in the end
	 * @return The list of all connectors
	 * @throws ArchitectureExtractorException
	 */
	private List<Connector> getAllConnectorsTemp(ComponentType componentType, List<Connector> connectors)
			throws ArchitectureExtractorException {
		/* Check whether the resulting list is initialized */
		if (connectors == null) {
			throw new NullPointerException("The resulting list of all connectors is not initalized");
		}

		/*
		 * If the component is composite structure, it contains other
		 * subcomponents that maybe contain some other connectors
		 */
		if (componentType instanceof CompoundType) {
			/* Insert the connectors in the resulting list */
			connectors.addAll(((CompoundType) componentType).getConnector());

			/* Get all subcomponents and recursively call the function */
			EList<Component> subcomponents = ((CompoundType) componentType).getSubcomponent();
			for (Component c : subcomponents) {
				getAllConnectorsTemp(c.getType(), connectors);
			}
		}
		/*
		 * If the component is atom, there aren't subcomponents and connectors
		 */
		else if (componentType instanceof AtomType) {
			return connectors;
		}
		/* Just in case the component is not defined */
		else {
			throw new ArchitectureExtractorException("Undefined component");
		}

		return connectors;
	}

	/**
	 * This method converts the operands in mathematical notation
	 * 
	 * @param operator
	 *            - The name of the operator
	 */
	private void printOperator(String operator) {
		/* Everything is self-descriptive */
		if (operator.equals("addition"))
			System.out.print("+");
		else if (operator.equals("equality"))
			System.out.print("tor, InspectArchitecture==");
		else if (operator.equals("less_than"))
			System.out.print("<");
		else if (operator.equals("logical_not"))
			System.out.print("!");
		else if (operator.equals("logical_and"))
			System.out.print("&&");
		else if (operator.equals("modulus"))
			System.out.print("%");
		else if (operator.equals("inequality"))
			System.out.print("!=");
		else if (operator.equals("negative"))
			System.out.print("-");
		else if (operator.equals("increment"))
			System.out.print("++");
		else
			System.out.print("ERROR: " + operator);
	}

	/**
	 * This method parses and prints a particular expression. The expression can
	 * be found in guards or actions of the transitions.
	 * 
	 * @param exp
	 *            - The expression for printing
	 */
	private void printExpression(Expression exp) {

		/* If the expression is binary, i.e. we have left and right operand */
		if (exp instanceof BinaryExpression) {
			/* Cast down to binary expression */
			BinaryExpression binaryExp = (BinaryExpression) exp;
			/* Get the left operand */
			Expression leftExp = binaryExp.getLeftOperand();
			/* Get right operand */
			Expression rightExp = binaryExp.getRightOperand();
			/* Get the operator */
			BinaryOperator operator = binaryExp.getOperator();
			System.out.print("(");
			/* If left operand is another expression */
			printExpression(leftExp);
			System.out.print(") ");
			/* Print operator */
			printOperator(operator.toString());
			System.out.print(" (");
			/* If the right operand is another expression */
			printExpression(rightExp);
			System.out.print(")");
		}
		/* If the expression is binary. For example i++ */
		else if (exp instanceof UnaryExpression) {
			/* Cast down to unary expression */
			UnaryExpression unaryExp = (UnaryExpression) exp;
			/* Get the operand */
			Expression operand = unaryExp.getOperand();
			/* Get the operator */
			UnaryOperator operator = unaryExp.getOperator();
			/* Print operator */
			printOperator(operator.toString());
			System.out.print(" (");
			/* If the operand is another expression */
			printExpression(operand);
			System.out.print(")");
		}
		/* If the expression is referring to other variable */
		else if (exp instanceof VariableReference) {
			/* Cast down to variable reference */
			VariableReference varRef = (VariableReference) exp;
			/* Get the name of the variable */
			Variable var = varRef.getTargetVariable();
			System.out.print(var.getName());
		} else if (exp instanceof DataParameterReference) {
			DataParameterReference dataParamRef = (DataParameterReference) exp;
			DataParameter dataParam = dataParamRef.getTargetParameter();
			System.out.print(dataParam.getName());
		}
		/* If the expression is only integer */
		else if (exp instanceof IntegerLiteral) {
			IntegerLiteral intLit = (IntegerLiteral) exp;
			System.out.print(intLit.getIValue());
		}
		/* If the expression is only boolean */
		else if (exp instanceof BooleanLiteral) {
			BooleanLiteral boolLit = (BooleanLiteral) exp;
			System.out.print(boolLit.isBValue());
		}
		/* If the expression is some array of values */
		else if (exp instanceof ArrayNavigationExpression) {
			/* Cast down to ArrayNavigationExpression */
			ArrayNavigationExpression arrayNavExp = (ArrayNavigationExpression) exp;
			Expression index = arrayNavExp.getIndex();
			VariableReference navigated = (VariableReference) arrayNavExp.getNavigated();
			Variable arrayName = navigated.getTargetVariable();
			System.out.print(arrayName.getName() + "[");
			printExpression(index);
			System.out.print("]");
		}
		/* If the expression is call of a function */
		else if (exp instanceof FunctionCallExpression) {
			/* Cast down to FunctionCallExpression */
			FunctionCallExpression funCallExp = (FunctionCallExpression) exp;
			System.out.print(funCallExp.getFunctionName() + "(");

			/* Get parameters of the function and print them */
			int numOfParams = funCallExp.getActualData().size();
			for (int i = 0; i < numOfParams; i++) {
				Expression param = funCallExp.getActualData().get(i);
				printExpression(param);
				if (i != numOfParams - 1)
					System.out.print(", ");
			}
			System.out.print(")");
		} else {
			System.out.println("ERROR: " + exp.toString());
		}

		System.out.println();
	}

	private void printAction(Action act) {
		if (act instanceof AssignmentAction) {
			AssignmentAction assignAct = (AssignmentAction) act;
			DataReference dataRef = assignAct.getAssignedTarget();
			if (dataRef instanceof VariableReference) {
				VariableReference varRef = (VariableReference) dataRef;
				Variable var = varRef.getTargetVariable();
				System.out.print(var.getName());
			} else if (dataRef instanceof ArrayNavigationExpression) {
				ArrayNavigationExpression arrayNavExp = (ArrayNavigationExpression) dataRef;
				Expression index = arrayNavExp.getIndex();
				VariableReference navigated = (VariableReference) arrayNavExp.getNavigated();
				Variable arrayName = navigated.getTargetVariable();
				System.out.print(arrayName.getName() + "[");
				printExpression(index);
				System.out.print("]");
			} else {
				System.out.println("ERROR: " + dataRef.toString());
			}

			System.out.print(" = ");

			Expression exp = assignAct.getAssignedValue();
			printExpression(exp);
			System.out.println(";");
		} else if (act instanceof FunctionCallExpression) {
			FunctionCallExpression funCallExp = (FunctionCallExpression) act;
			System.out.print(funCallExp.getFunctionName() + "(");

			int numOfParams = funCallExp.getActualData().size();
			for (int i = 0; i < numOfParams; i++) {
				Expression param = funCallExp.getActualData().get(i);
				printExpression(param);
				if (i != numOfParams - 1)
					System.out.print(", ");
			}
			System.out.println(");");
		} else {
			System.out.print("ERROR: " + act.toString());
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/****************************************************************************/

	/**
	 * Constructor for the class. It creates the architecture for extracting.
	 * 
	 * @param pathToConfFile
	 *            - path to the BIP file
	 */
	public ExtractorImpl(String path) {
		this.bipFileModel = new BIPFileModel(path);
	}

	/**
	 * @param architecture
	 *            - already created BIP file model
	 */
	public ExtractorImpl(BIPFileModel bipFileModel) {
		this.bipFileModel = bipFileModel;
	}

	/**
	 * @return the BIP file model
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

	@Override
	public void printStructure() throws ArchitectureExtractorException {
		System.out.println("The structure of the BIP model is: ");
		this.printStructureTemp(this.bipFileModel.getRootType(), "", 0);
		System.out.println();
	}

	@Override
	public List<Component> getAllComponents() throws ArchitectureExtractorException {
		List<Component> components = new LinkedList<Component>();

		/* Get all subcomponents of the architecture */
		EList<Component> architectureComponents = (this.bipFileModel.getRootType()).getSubcomponent();
		for (Component c : architectureComponents) {
			components.addAll(getAllComponentsTemp(c, new LinkedList<Component>()));
		}

		return components;
	}

	@Override
	public List<String> getAllComponentsNames() throws ArchitectureExtractorException {
		/* Get all components in the architecture */
		List<Component> allComponents = this.getAllComponents();
		/* We will get the name of each component */
		List<String> allComponentNames = new LinkedList<String>();

		/* Iterate components */
		for (Component t : allComponents) {
			allComponentNames.add(t.getName());
		}

		return allComponentNames;
	}

	@Override
	public boolean componentExists(Component component) throws ArchitectureExtractorException {
		return this.getAllComponentsNames().contains(component.getName());
	}

	@Override
	public boolean componentExists(String componentName) throws ArchitectureExtractorException {
		return this.getAllComponentsNames().contains(componentName);
	}

	@Override
	public List<Component> getAllAtoms() throws ArchitectureExtractorException {
		/* Instantiate the list of all atoms */
		List<Component> atoms = new LinkedList<Component>();
		/* Get all components from the architecture */
		List<Component> components = this.getAllComponents();

		/* Iterate components */
		for (Component c : components) {
			/* If the current component is atomic */
			if (c.getType() instanceof AtomType) {
				atoms.add(c);
			}
		}

		return atoms;
	}

	@Override
	public void printAtoms() throws ArchitectureExtractorException {
		/* Get all atoms */
		List<Component> atoms = this.getAllAtoms();

		System.out.println("Atoms are: ");
		/* Iterate atoms */
		for (Component atom : atoms) {
			System.out.println("\t Name: " + atom.getName() + "(type: " + atom.getType().getName() + ")");
		}

		System.out.println();
	}

	@Override
	public List<Component> getAllCompounds() throws ArchitectureExtractorException {
		/* Instantiate the list of all atoms */
		List<Component> compounds = new LinkedList<Component>();
		/* Get all components from the architecture */
		List<Component> components = this.getAllComponents();

		/* Iterate components */
		for (Component c : components) {
			/* If the current component is atomic */
			if (c.getType() instanceof CompoundType) {
				compounds.add(c);
			}
		}

		return compounds;
	}

	@Override
	public void printCompounds() throws ArchitectureExtractorException {
		/* Get all compounds in the architecture */
		List<Component> compounds = this.getAllCompounds();

		System.out.println("Compounds are: ");
		/* Iterate compounds */
		for (Component compound : compounds) {
			System.out.println("\t Name: " + compound.getName() + "(type: " + compound.getType().getName() + ")");
		}

		System.out.println();
	}

	@Override
	public List<ComponentType> getAllComponentTypes() throws ArchitectureExtractorException {
		/* The list of all Component Types in the Architecture */
		List<ComponentType> types = new LinkedList<ComponentType>();

		/*
		 * This will return me all Component Types, Port Types and Connector
		 * Types in the Architecture
		 */
		List<BipType> allTypes = this.bipFileModel.getSystem().getBipType();

		/* Iterate all BIP Types */
		for (BipType bt : allTypes) {
			/* If it is instance of ComponentType add it */
			if (bt instanceof ComponentType) {
				types.add((ComponentType) bt);
			}

		}

		return types;
	}

	@Override
	public List<String> getAllComponentTypesNames() throws ArchitectureExtractorException {
		/* Get all component types in the architecture */
		List<ComponentType> allTypes = this.getAllComponentTypes();
		/* We will get the name of each component type */
		List<String> allTypesNames = new LinkedList<String>();

		/* Iterate types */
		for (ComponentType t : allTypes) {
			allTypesNames.add(t.getName());
		}

		return allTypesNames;
	}

	@Override
	public boolean componentTypeExists(ComponentType componentType) throws ArchitectureExtractorException {
		return this.getAllComponentTypesNames().contains(componentType.getName());
	}

	@Override
	public boolean componentTypeExists(String componentTypeName) throws ArchitectureExtractorException {
		return this.getAllComponentTypesNames().contains(componentTypeName);
	}

	@Override
	public List<AtomType> getAllAtomTypes() throws ArchitectureExtractorException {
		List<AtomType> atomTypes = new LinkedList<AtomType>();

		/* Get all types in the architecture */
		List<ComponentType> allTypes = this.getAllComponentTypes();

		/* Iterate types */
		for (ComponentType t : allTypes) {
			if (t instanceof AtomType) {
				atomTypes.add((AtomType) t);
			}
		}

		return atomTypes;
	}

	@Override
	public AtomType getAtomTypeByName(String atomTypeName) throws ArchitectureExtractorException{
		/* Get the list of all atom types */
		List<AtomType> allAtomTypes = this.getAllAtomTypes();
		
		/* Iterate all atom types */
		for(AtomType a : allAtomTypes){
			if(a.getName().equals(atomTypeName)){
				return a;
			}
		}
		
		throw new ComponentTypeNotFoundException("The atom type with a name " + atomTypeName + " is not found");
	}

	@Override
	public List<CompoundType> getAllCompoundTypes() throws ArchitectureExtractorException {
		List<CompoundType> compoundTypes = new LinkedList<CompoundType>();

		/* Get all types in the architecture */
		List<ComponentType> allTypes = this.getAllComponentTypes();

		/* Iterate types */
		for (ComponentType t : allTypes) {
			if (t instanceof CompoundType) {
				compoundTypes.add((CompoundType) t);
			}
		}

		return compoundTypes;
	}

	@Override
	public Component getComponentByName(String name) throws ArchitectureExtractorException {
		/* Get all components in the architecture */
		List<Component> components = this.getAllComponents();

		/* Iterate components */
		for (Component c : components) {
			if (c.getName().equals(name)) {
				return c;
			}
		}

		throw new ComponentNotFoundException("A component with the name " + name + " does not exist");
	}

	@Override
	public List<Component> getComponentsByType(String type) throws ArchitectureExtractorException {
		List<Component> result = new LinkedList<Component>();
		/* Get all components in the architecture */
		List<Component> components = this.getAllComponents();

		/* Iterate components */
		for (Component c : components) {
			if (c.getType().getName().equals(type)) {
				result.add(c);
			}
		}

		/* In case if there is not a component of the given type */
		if (result.size() == 0) {
			throw new ComponentTypeNotFoundException("A component of type " + type + " does not exist");
		}

		return result;
	}

	@Override
	public List<State> getAtomStates(Component atom) throws IllegalComponentException {
		List<State> states = new LinkedList<State>();

		/* If the provided component is not atomic */
		if (!(atom.getType() instanceof AtomType)) {
			throw new IllegalComponentException("The given component is not atomic");
		}

		/* Get the type of the component */
		AtomType atomType = (AtomType) atom.getType();
		/* Get the Petri net */
		PetriNet petriNet = (PetriNet) atomType.getBehavior();
		states.addAll(petriNet.getState());

		return states;
	}

	@Override
	public void printAtomStates(Component atom) throws IllegalComponentException {
		/* Get all states of the atom */
		List<State> states = this.getAtomStates(atom);
		/* Print the states */
		System.out.println("States of the atom " + atom.getName() + " are: ");
		for (State s : states) {
			System.out.print("\t Name: " + s.getName());
		}
		System.out.println();
	}

	@Override
	public List<String> getAtomStatesNames(Component atom) throws IllegalComponentException {
		/* Initialize the list of all state names */
		List<String> atomStatesNames = new LinkedList<String>();
		/* Get all states for the given atom */
		List<State> allAtomStates = this.getAtomStates(atom);

		/* Iterate states */
		for (State s : allAtomStates) {
			atomStatesNames.add(s.getName());
		}

		/* Return the list */
		return atomStatesNames;
	}

	public boolean stateExists(String name, Component atom) throws IllegalComponentException {
		return getAtomStatesNames(atom).contains(name);
	}

	@Override
	public List<Transition> getAtomTransitions(Component atom) throws IllegalComponentException {
		/* Initialize the list of all transitions */
		List<Transition> transitions = new LinkedList<Transition>();

		/* If the provided component is not atomic */
		if (!(atom.getType() instanceof AtomType)) {
			throw new IllegalComponentException("The given component is not atomic");
		}

		/* Get the type of the component */
		AtomType atomType = (AtomType) atom.getType();
		/* Get the Petri-Net */
		PetriNet petriNet = (PetriNet) atomType.getBehavior();
		transitions.addAll(petriNet.getTransition());

		return transitions;
	}

	@Override
	public void printAtomTransitions(Component atom) throws IllegalComponentException {
		/* Get all transitions of the atom */
		List<Transition> transitions = this.getAtomTransitions(atom);

		/* Iterate transitions */
		for (Transition t : transitions) {
			PortDefinitionReference portDefRef = (PortDefinitionReference) t.getTrigger();
			System.out.println("on " + portDefRef.getTarget().getName());
			System.out
					.println(" from " + t.getOrigin().get(0).getName() + " to " + t.getDestination().get(0).getName());// change
																														// this

			/* Get guard */
			Expression exp = t.getGuard();
			if (exp != null) {
				System.out.println("provided ");
				printExpression(exp);
			}

			if (t.getAction() != null && t.getAction() instanceof CompositeAction) {
				System.out.println("do {");
				CompositeAction ca = (CompositeAction) t.getAction();
				int numOfActions = ca.getContent().size();
				for (int j = 0; j < numOfActions; j++) {
					Action act = ca.getContent().get(j);
					printAction(act);
				}
				System.out.println("}");
			}
			System.out.println();
		}
		System.out.println();
	}

	@Override
	public List<State> getTransitionOrigins(Transition transition) {
		List<State> states = new LinkedList<State>();
		states.addAll(transition.getOrigin());
		return states;
	}

	@Override
	public List<State> getTransitionDestinations(Transition transition) {
		List<State> states = new LinkedList<State>();
		states.addAll(transition.getDestination());
		return states;
	}

	@Override
	public List<Transition> getIncomingTransitions(State state) {
		List<Transition> transitions = new LinkedList<Transition>();
		transitions.addAll(state.getIncoming());
		return transitions;
	}

	@Override
	public List<AbstractTransition> getOutgoingTransitions(State state) {
		List<AbstractTransition> transitions = new LinkedList<AbstractTransition>();
		transitions.addAll(state.getOutgoing());
		return transitions;
	}

	@Override
	public List<Port> getAllPorts() throws ArchitectureExtractorException {
		return getAllPortsTemp(this.bipFileModel.getRootType(), new LinkedList<Port>());
	}

	@Override
	public void printAllPorts() throws ArchitectureExtractorException {
		System.out.println("Ports are: ");
		for (Port p : this.getAllPorts()) {
			System.out.println("\t Name: " + p.getName() + "(type: " + p.getType().getName()
					+ ") in a component of type: " + p.getComponentType().getName());
		}
	}

	@Override
	public List<String> getAllPortNames() throws ArchitectureExtractorException {
		/* Get all ports in the architecture */
		List<Port> allPorts = this.getAllPorts();

		/* We will get the name of each port in the architecture */
		List<String> allPortNames = new LinkedList<String>();

		/* Iterate all ports */
		for (Port p : allPorts) {
			allPortNames.add(p.getName());
		}

		return allPortNames;
	}

	@Override
	public boolean portExists(Port port, Component component) throws ArchitectureExtractorException {
		return getComponentPortNames(component).contains(port.getName());
	}

	@Override
	public boolean portExists(String portName, String componentName) throws ArchitectureExtractorException {
		return getComponentPortNames(getComponentByName(componentName)).contains(portName);
	}

	@Override
	public List<PortType> getAllPortTypes() throws ArchitectureExtractorException {
		List<PortType> portTypes = new LinkedList<PortType>();

		/* Get all BIP types in the architecture */
		List<BipType> bipTypes = this.bipFileModel.getSystem().getBipType();

		/* Iterate all BIP Types */
		for (BipType p : bipTypes) {
			/* If it us instance of the Port Type, add it */
			if (p instanceof PortType) {
				portTypes.add((PortType) p);
			}
		}

		return portTypes;
	}

	@Override
	public List<String> getAllPortTypesNames() throws ArchitectureExtractorException {
		/* Get all port types in the architecture */
		List<PortType> allPortTypes = this.getAllPortTypes();

		/* We will get the name of each port type in the architecture */
		List<String> allPortTypesNames = new LinkedList<String>();

		/* Iterate all port types */
		for (PortType p : allPortTypes) {
			allPortTypesNames.add(p.getName());
		}

		return allPortTypesNames;
	}

	@Override
	public boolean portTypeExists(PortType portType) throws ArchitectureExtractorException {
		return this.getAllPortTypesNames().contains(portType.getName());
	}

	@Override
	public boolean portTypeExists(String portTypeName) throws ArchitectureExtractorException {
		return this.getAllPortTypesNames().contains(portTypeName);
	}

	@Override
	public Port getPortByName(String name) throws ArchitectureExtractorException {
		/* Get all ports in the architecture */
		List<Port> ports = this.getAllPorts();

		/* Iterate ports */
		for (Port p : ports) {
			if (p.getName().equals(name)) {
				return p;
			}
		}

		throw new PortNotFoundException("A port with the name " + name + " does not exist");
	}

	public PortType getPortTypeByName(String name) throws ArchitectureExtractorException {
		/* Get all port types */
		List<PortType> allPortTypes = this.getAllPortTypes();

		/* Iterate port types for match */
		for (PortType p : allPortTypes) {
			if (p.getName().equals(name)) {
				return p;
			}
		}

		/* If the port type with the given name is not found */
		throw new PortTypeNotFoundException("The port type with name " + name + "is not found");
	}

	@Override
	public List<Port> getPortsByType(String type) throws ArchitectureExtractorException {
		List<Port> result = new LinkedList<Port>();
		/* Get all ports in the architecture */
		List<Port> ports = this.getAllPorts();

		/* Iterate ports */
		for (Port p : ports) {
			if (p.getType().getName().equals(type)) {
				result.add(p);
			}
		}

		/* In case if there is not a port of the given type */
		if (result.size() == 0) {
			throw new PortNotFoundException("A port of a type " + type + "does not exist");
		}

		return result;
	}

	@Override
	public List<Port> getComponentPorts(Component component) throws ArchitectureExtractorException {
		return getAllPortsTemp(component.getType(), new LinkedList<Port>());
	}

	@Override
	public List<String> getComponentPortNames(Component component) throws ArchitectureExtractorException {
		/* Get all ports of the component */
		List<Port> allPorts = getComponentPorts(component);
		List<String> result = new LinkedList<String>();

		/* Iterate ports */
		for (Port p : allPorts) {
			/* Add the name of the port */
			result.add(p.getName());
		}

		return result;
	}

	@Override
	public void printComponentPorts(Component component) throws ArchitectureExtractorException {

		System.out.println("Ports of the component " + component.getName() + " are:");
		/* Iterate ports */
		for (Port p : this.getComponentPorts(component)) {
			System.out.println("\t Name: " + p.getName() + "(type: " + p.getType().getName() + ")");
		}
	}

	@Override
	public List<DataParameter> getPortVariables(Port port) {
		List<DataParameter> variables = new LinkedList<DataParameter>();

		PortType portType = port.getType();
		variables.addAll(portType.getDataParameter());

		return variables;
	}

	@Override
	public void printPortVariables(Port port) {
		/* Get all variables of the port */
		List<DataParameter> variables = getPortVariables(port);

		System.out.println("The variables of the port named " + port.getName() + " from type "
				+ port.getType().getName() + " are: ");

		/* Iterate variables */
		for (DataParameter v : variables) {
			OpaqueElement oe = (OpaqueElement) v.getType();
			System.out.println("\t" + oe.getBody() + " " + v.getName());
		}
	}

	@Override
	public List<Variable> getComponentVariables(Component atom) throws IllegalComponentException {
		List<Variable> variables = new LinkedList<Variable>();

		/* If the provided component is not atomic */
		if (!(atom.getType() instanceof AtomType)) {
			throw new IllegalComponentException("The given component is not atomic");
		}

		/* Get the type of the component */
		AtomType atomType = (AtomType) atom.getType();
		variables.addAll(atomType.getVariable());

		return variables;
	}

	@Override
	public void printComponentVariables(Component atom) throws IllegalComponentException {
		/* Get all variables of the atom */
		List<Variable> variables = getComponentVariables(atom);

		System.out.println("The variables of the atom named " + atom.getName() + " from type "
				+ atom.getType().getName() + " are: ");

		/* Iterate variables */
		for (Variable v : variables) {
			/* Get the type of the variable(integer, double) */
			OpaqueElement oe = (OpaqueElement) v.getType();
			System.out.print("\t " + oe.getBody() + " " + v.getName());
			/* If the variable has an initial value */
			if (v.getInitialValue() != null) {
				System.out.print("=");
				printExpression(v.getInitialValue());
			}
			System.out.println();

		}
	}

	@Override
	public List<Connector> getAllConnectors() throws ArchitectureExtractorException {
		return getAllConnectorsTemp(this.bipFileModel.getRootType(), new LinkedList<Connector>());
	}

	@Override
	public void printConnectors() throws ArchitectureExtractorException {
		System.out.println("Connectors are: ");
		for (Connector c : this.getAllConnectors()) {
			System.out.println("\t Name: " + c.getName() + " of type: " + c.getType().getName()
					+ " in compound of type " + c.getCompoundType().getName());
		}
	}

	@Override
	public List<String> getAllConnectorsNames() throws ArchitectureExtractorException {
		/* Get all connectors in the architecture */
		List<Connector> allConnectors = this.getAllConnectors();

		/* We will get the name of each connector name */
		List<String> allConnectorsNames = new LinkedList<String>();

		/* Iterate connectors */
		for (Connector c : allConnectors) {
			allConnectorsNames.add(c.getName());
		}

		return allConnectorsNames;
	}

	@Override
	public boolean connectorExists(Connector connector) throws ArchitectureExtractorException {
		return this.getAllConnectorsNames().contains(connector.getName());
	}

	@Override
	public boolean connectorExists(String connectorName) throws ArchitectureExtractorException {
		return this.getAllConnectorsNames().contains(connectorName);
	}

	@Override
	public List<ConnectorType> getAllConnectorTypes() throws ArchitectureExtractorException {
		List<ConnectorType> connectorTypes = new LinkedList<ConnectorType>();

		/* Get all BIP types in the architecture */
		List<BipType> bipTypes = this.bipFileModel.getSystem().getBipType();

		/* Iterate BIP Types connectors */
		for (BipType c : bipTypes) {
			if (c instanceof ConnectorType) {
				connectorTypes.add((ConnectorType) c);
			}
		}

		return connectorTypes;
	}

	@Override
	public List<String> getAllConnectorTypesNames() throws ArchitectureExtractorException {
		/* Get all connector types in the architecture */
		List<ConnectorType> allConnectorTypes = this.getAllConnectorTypes();

		/* We will get the name of each connector name */
		List<String> allConnectorTypesNames = new LinkedList<String>();

		/* Iterate connector types */
		for (ConnectorType c : allConnectorTypes) {
			allConnectorTypesNames.add(c.getName());
		}

		return allConnectorTypesNames;
	}

	@Override
	public boolean connectorTypeExists(ConnectorType connectorType) throws ArchitectureExtractorException {
		return this.getAllConnectorTypesNames().contains(connectorType.getName());
	}

	@Override
	public boolean connectorTypeExists(String connectorTypeName) throws ArchitectureExtractorException {
		return this.getAllConnectorTypesNames().contains(connectorTypeName);
	}

	@Override
	public Connector getConnectorByName(String name) throws ArchitectureExtractorException {
		/* Get all connectors in the architecture */
		List<Connector> connectors = this.getAllConnectors();

		/* Iterate connectors */
		for (Connector c : connectors) {
			if (c.getName().equals(name)) {
				return c;
			}
		}

		throw new ConnectorNotFoundException("A connector with the name " + name + " does not exist");
	}

	@Override
	public ConnectorType getConnectorTypeByName(String name) throws ArchitectureExtractorException {
		/* Get all connector types */
		List<ConnectorType> connectorTypes = this.getAllConnectorTypes();

		/* Iterate connector types */
		for (ConnectorType c : connectorTypes) {
			if (c.getName().equals(name)) {
				return c;
			}
		}

		throw new ConnectorTypeNotFoundException("A connector type with name " + name + " does not exist");
	}

	@Override
	public List<Connector> getConnectorsByType(String type) throws ArchitectureExtractorException {
		List<Connector> result = new LinkedList<Connector>();

		/* Get all connectors in the architecture */
		List<Connector> connectors = this.getAllConnectors();

		/* Iterate connectors */
		for (Connector c : connectors) {
			if (c.getType().getName().equals(type)) {
				result.add(c);
			}
		}

		/* In case if there is not a connector of the given type */
		if (result.size() == 0) {
			throw new ConnectorNotFoundException("A connector of type " + type + " does not exist");
		}

		return result;
	}

	public String getConnectorInteractionDefinition(ConnectorType connectorType) {
		StringBuilder sb = new StringBuilder();

		/* Get the port expression */
		PortExpression portExpression = connectorType.getDefinition();
		return sb.toString();
	}

	public String getConnectorInteractionDefinition(String connectorTypeName) {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

}
