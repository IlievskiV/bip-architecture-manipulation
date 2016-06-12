package ch.epfl.risd.archman.extractor;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.BinaryExpression;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.DataParameterReference;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.VariableReference;
import ujf.verimag.bip.Core.Behaviors.AbstractTransition;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.DataParameter;
import ujf.verimag.bip.Core.Behaviors.Expression;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.State;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.Connector;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.impl.InnerPortReferenceImpl;
import ujf.verimag.bip.Core.Interactions.impl.PortParameterReferenceImpl;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.impl.ACFusionImpl;

public class ArchitectureStyleExtractorImpl extends ExtractorImpl implements ArchitectureStyleExtractor {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The set of all coordinators in the Architecture */
	private Set<String> coordinators;

	/* The set of all parameter operands in the Architecture */
	private Set<String> operands;

	/* The set of all ports in the Architecture */
	private Set<String> ports;

	/* The set of all connectors in the Architecture */
	private List<ConnectorTuple> connectors;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public ArchitectureStyleExtractorImpl(ArchitectureStyle architectureStyle) {
		super(architectureStyle.getBipFileModel());

		this.coordinators = architectureStyle.getCoordinators();
		this.operands = architectureStyle.getOperands();
		this.ports = architectureStyle.getPorts();
		this.connectors = architectureStyle.getConnectors();
	}

	@Override
	public List<Component> getArchitectureStyleCoordinators() throws ArchitectureExtractorException {
		/* Instantiate the list of coordinators */
		List<Component> coordinators = new LinkedList<Component>();
		/* Get all components from the BIP model */
		List<Component> components = this.getAllComponents();

		/* Flag for the existence of the coordinator in the BIP model */
		boolean flag;

		/* Iterate coordinators names */
		for (String s : this.coordinators) {
			/* Set the flag to false */
			flag = false;

			/* Iterate components */
			for (Component c : components) {
				/*
				 * If the coordinator really exist then add it in the list and
				 * break
				 */
				if (c.getName().equals(s)) {
					flag = true;
					coordinators.add(c);
					break;
				}
			}

			/* If the coordinator does not exist */
			if (!flag) {
				throw new ComponentNotFoundException("Coordinator " + s + " does not exist");
			}
		}

		return coordinators;
	}

	@Override
	public void printArchitectureStyleCoordinators() throws ArchitectureExtractorException {
		System.out.println("Coordinators are: ");

		/* Iterate coordinators */
		for (Component c : this.getArchitectureStyleCoordinators()) {
			System.out.println("\t Name: " + c.getName() + "(type: " + c.getType().getName() + ")");
		}
		System.out.println();
	}

	@Override
	public List<Component> getArchitectureStyleOperands() throws ArchitectureExtractorException {
		/* Instantiate the list of operands */
		List<Component> result = new LinkedList<Component>();
		/* Get all components from the BIP model */
		List<Component> components = this.getAllComponents();

		/* Iterate components of the BIP model */
		for (Component c : components) {
			if (this.operands.contains(c.getName())) {
				result.add(c);
			}
		}

		return result;
	}

	@Override
	public void printArchitectureStyleOperands() throws ArchitectureExtractorException {
		System.out.println("Operands are: ");

		/* Iterate operands */
		for (Component c : this.getArchitectureStyleOperands()) {
			System.out.println("\t Name: " + c.getName() + "(type: " + c.getType().getName() + ")");
		}
		System.out.println();
	}

	@Override
	public List<Port> getArchitectureStylePorts() throws ArchitectureExtractorException {
		/* The resulting list */
		List<Port> result = new LinkedList<Port>();

		/* Get all ports in the Architecture Style */
		List<Port> allPorts = this.getAllPorts();

		/* Iterate the list of all ports */
		for (Port p : allPorts) {
			if (this.ports.contains(p.getName())) {
				result.add(p);
			}
		}

		return result;
	}

	@Override
	public void printArchitectureStylePorts() {
		StringBuilder sb = new StringBuilder();
		sb.append("The ports in the Architecture Style are: ");

		/* Iterate the list of port names */
		for (String s : this.ports) {
			sb.append(s + ", ");
		}
		sb.append("\n");
		System.out.println(sb.toString());
	}

	@Override
	public List<Connector> getArchitectureStyleConnectors() throws ArchitectureExtractorException {

		/* The resulting list */
		List<Connector> result = new LinkedList<Connector>();

		/* Get all connectors in the Architecture Style */
		List<Connector> allConnectors = this.getAllConnectors();

		/* Iterate all connectors */
		for (Connector c : allConnectors) {
			/* Iterate Connector Tuples */
			for (ConnectorTuple tuple : this.connectors) {
				if (tuple.getConnectorInstanceName().equals(c.getName())) {
					result.add(c);
					break;
				}
			}
		}

		return result;
	}

	public static void main(String[] args) {

		String path1 = "/localhome/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
		String path2 = "/localhome/vladimir/Architecture_examples/Archive/Modes2/AEConf.txt";
		String path3 = "/localhome/vladimir/Architecture_examples/Archive/ActionSequence/AEConf.txt";

		try {
			ArchitectureStyle architectureStyle = new ArchitectureStyle(path1);
			ArchitectureStyleExtractorImpl extractor = new ArchitectureStyleExtractorImpl(architectureStyle);

			/**
			 * Global
			 */

			List<String> portsNames = extractor.getAllPortNames();

			for (String s : portsNames) {
				System.out.println(s);
			}

			System.out.println("All component types in the architecture");

			List<ComponentType> types = extractor.getAllComponentTypes();
			for (ComponentType t : types) {
				System.out.println(t.getName());
			}

			System.out.println("All port types in the architecture");

			List<PortType> portTypes = extractor.getAllPortTypes();
			for (PortType p : portTypes) {
				System.out.println(p.getName());
			}

			System.out.println("All components in the architecture\n");

			/* Print the structure of the architecture */
			extractor.printStructure();

			/* Get all components in the architecture */
			List<Component> components = extractor.getAllComponents();

			/* Get component by name */
			Component compByName = extractor.getComponentByName(components.get(0).getName());
			System.out.println("Component by name: " + compByName.getName());

			/* Get components by type */
			List<Component> compByType = extractor.getComponentsByType(components.get(0).getType().getName());
			System.out.println();

			System.out.println("All connectors in the architecture\n");
			/* Print all connectors in the architecture */
			extractor.printConnectors();
			/* Get all connectors in the architecture */
			List<Connector> connectors = extractor.getAllConnectors();
			/* Get connector by type */
			Connector connByName = extractor.getConnectorByName(connectors.get(0).getName());
			System.out.println("Connector by name: " + connByName.getName());

			/* Get connectors by type */
			List<Connector> connByType = extractor.getConnectorsByType(connectors.get(0).getType().getName());

			/* Iterate connectors */
			for (Connector c : connectors) {
				List<ActualPortParameter> acp = c.getActualPort();

				/* Iterate Actual Port Parameters */
				for (ActualPortParameter a : acp) {
					System.out.println(((InnerPortReferenceImpl) a).getTargetInstance().getTargetPart().getName());
					System.out.println(((InnerPortReferenceImpl) a).getTargetPort().getName());
				}

			}
			
			
			
			// System.out.println();
			// /* Print all compounds in the architecture */
			// extractor.printCompounds();
			//
			// /* Get all compounds in the architecture */
			// List<Component> compounds = extractor.getAllCompounds();
			//
			// /* Iterate compounds */
			// System.out.println("Manipulating compounds");
			// for (Component compound : compounds) {
			// /* I need methods here */
			// }
			//
			// System.out.println();
			//
			// /* Print all atoms in the architecture */
			// extractor.printAtoms();
			//
			// /* Get all atoms in the architecture */
			// List<Component> atoms = extractor.getAllAtoms();
			//
			// /* Iterate atoms */
			// System.out.println("Manipulating atoms");
			// for (Component atom : atoms) {
			//
			// System.out.println("\t Atom: " + atom.getName());
			//
			// System.out.println("\t Variables");
			// /* Print all variables of the atom */
			// extractor.printComponentVariables(atom);
			//
			// /* Get all variables of the atom */
			// List<Variable> variables = extractor.getComponentVariables(atom);
			// /* I can make something wit variables */
			//
			// System.out.println("\t States");
			//
			// /* Print all states of the current atom */
			// extractor.printAtomStates(atom);
			//
			// /* Get all states of the current atom */
			// List<State> states = extractor.getAtomStates(atom);
			// System.out.println("\t Manipulating states");
			//
			// /* Iterate all states of the current atom */
			// for (State state : states) {
			// /* Get all incoming transitions */
			// List<Transition> incoming =
			// extractor.getIncomingTransitions(state);
			//
			// /* Get all outgoing transitions */
			// List<AbstractTransition> outgoing =
			// extractor.getOutgoingTransitions(state);
			//
			// /* I can make something with transitions */
			// }
			//
			// System.out.println("\t Ports: ");
			//
			// /* Print all ports of the atom */
			// extractor.printComponentPorts(atom);
			//
			// System.out.println("\t Manipulating ports");
			//
			// /* Get all ports in the atom */
			// List<Port> ports = extractor.getComponentPorts(atom);
			// /* Iterate ports of the atom */
			// for (Port port : ports) {
			// /* Print data parameters of the port */
			// extractor.printPortVariables(port);
			// /* Get all data parameters of the port */
			// List<DataParameter> params = extractor.getPortVariables(port);
			// }
			//
			// System.out.println("\t Transitioning: ");
			//
			// /* Print all transitions of the current atom */
			// extractor.printAtomTransitions(atom);
			//
			// List<Transition> transitions =
			// extractor.getAtomTransitions(atom);
			// for (Transition t : transitions) {
			// t.getName();
			// if (t.getGuard() instanceof BinaryExpression) {
			// BinaryExpression exp = (BinaryExpression) t.getGuard();
			// Expression leftOperand = exp.getLeftOperand();
			// Expression rightOperand = exp.getRightOperand();
			//
			// if (leftOperand instanceof DataParameterReference) {
			// System.out.println("Left Operand is Data Parameter Reference");
			// } else if (leftOperand instanceof VariableReference) {
			// System.out.println("Left Operand is Variable Reference");
			// System.out.println(
			// "Name: " + ((VariableReference)
			// leftOperand).getTargetVariable().getName());
			// System.out.println(
			// "Type: " + ((VariableReference)
			// leftOperand).getTargetVariable().getType());
			// }
			//
			// if (rightOperand instanceof DataParameterReference) {
			// System.out.println("Right Operand is Data Parameter Reference");
			// } else if (rightOperand instanceof VariableReference) {
			// System.out.println("Right Operand is Variable Reference");
			// }
			//
			// }
			// }
			//
			// System.out.println();
			// System.out.println();
			// }
			//
			// /**
			// * Manipulating coordinators
			// */
			//
			// /* Print coordinators */
			// extractor.printArchitectureStyleCoordinators();
			//
			// /* Get the list of all coordinators */
			// List<Component> coordinators =
			// extractor.getArchitectureStyleCoordinators();
			//
			// /* Iterate coordinators */
			// for (Component coordinator : coordinators) {
			// /* Everything I did with atoms I can do for coordinators too */
			// }
			//
			// /**
			// * Manipulating operands
			// */
			//
			// /* Print operands */
			// extractor.printArchitectureStyleOperands();
			//
			// /* Get list of all operands */
			// List<Component> operands =
			// extractor.getArchitectureStyleOperands();
			//
			// /* Iterate operands */
			// for (Component operand : operands) {
			// /* Everything I did with atoms I can do for operands too */
			// }
			//
			// System.out.println();
			// System.out.println("Connector Types: ");
			//
			// List<ConnectorType> allConnectorTypes =
			// extractor.getAllConnectorTypes();
			// for (ConnectorType c : allConnectorTypes) {
			// System.out.println("Connector Type " + c.getName());
			// System.out.println("\tPort Expression: " + c.getDefinition());
			//
			// if (c.getDefinition() instanceof PortParameterReferenceImpl) {
			// System.out.println("\t\t" + ((PortParameterReferenceImpl)
			// c.getDefinition()).getTarget().getName());
			// } else if (c.getDefinition() instanceof ACFusionImpl) {
			// List<ACExpression> expression = ((ACFusionImpl)
			// c.getDefinition()).getOperand();
			//
			// for (int i = 0; i < expression.size(); i++) {
			// System.out.println("\t" + expression.get(i).getClass());
			// }
			//
			// System.out
			// .println("Name: " + ((PortParameterReferenceImpl)
			// expression.get(0)).getTarget().getName());
			// System.out.println("\t\t" + ((ACFusionImpl)
			// c.getDefinition()).getOperand());
			// }
			//
			// System.out.println("\tPort: " + c.getPort()); // Port
			// System.out.println("\tPort Definition: " +
			// c.getPortDefinition());
			//
			// System.out.println("\tConnector Type Variables: ");
			// for (PortParameter p : c.getPortParameter()) {
			// System.out.println("\t\t" + p.getName());
			// }
			// }

		} catch (FileNotFoundException | ConfigurationFileException e) {
			e.printStackTrace();
		} catch (ArchitectureExtractorException e) {
			e.printStackTrace();
		}
	}

}
