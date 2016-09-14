package ch.epfl.risd.archman.solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.exceptions.TestFailException;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ComponentMapping;
import ch.epfl.risd.archman.model.ComponentPortMapping;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ch.epfl.risd.archman.model.NameValue;
import ch.epfl.risd.archman.model.GlobalPortMapping;
import ch.epfl.risd.archman.model.PortTuple;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;

/**
 * Class using the SMT Z3 solver, to find the unknown variables in the style in
 * order to instantiate an architecture.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureStyleSolver {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/
	private static Map<String, String> cfg = new HashMap<String, String>();

	private static void checkNameValues(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands) {

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Check if everything is calculated */
		for (ConnectorTuple connectorTuple : connectorTuples) {
			for (PortTuple portTuple : connectorTuple.getPortTuples()) {
				System.out.println("Multiplicity variable named " + portTuple.getMultiplicityTerm().getName()
						+ " is having a value " + portTuple.getMultiplicityTerm().getValue());
				System.out.println("Degree variable named " + portTuple.getDegreeTerm().getName()
						+ " is having a value " + portTuple.getDegreeTerm().getValue());

				/* name and the component instance where it belongs */
				String portInstanceName = portTuple.getPortInstanceName();
				String compInstanceName = portInstanceName.split("\\.")[0];

				/* The mappings where the port belongs */
				ComponentMapping componentMapping;
				GlobalPortMapping globalPortMapping;

				/* If the port tuple is coordinator tuple */
				if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
					componentMapping = architectureStyle.getCoordinatorsMapping().get(compInstanceName);
					globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
				} else {
					componentMapping = architectureOperands.getOperandsMapping().get(compInstanceName);
					globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
				}

				System.out.println("Component named " + compInstanceName + " is having cardinality value "
						+ componentMapping.getCardinalityTerm().getValue());
				/* Take the collection of all port mappings */
				Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings()
						.values();

				for (ComponentPortMapping cpm : componentPortMappings) {
					System.out.println("Port cardinality value: " + cpm.getCardinalityTerm().getValue());
				}
			}
		}
	}

	private static void generateMissingPortNames(ArchitectureStyle architectureStyle) {

		/* Iterate in component mappings */
		for (String compToMap : architectureStyle.getCoordinatorsMapping().keySet()) {
			ComponentMapping componentMapping = architectureStyle.getCoordinatorsMapping().get(compToMap);
			/* Iterate in global port mappings */
			for (String portToMap : componentMapping.getGlobalPortMappings().keySet()) {
				GlobalPortMapping globalPortMapping = componentMapping.getGlobalPortMappings().get(portToMap);
				/* Inner port name */
				String portInnerName = portToMap.split("\\.")[1];
				for (String mappedComp : globalPortMapping.getComponentPortMappings().keySet()) {
					ComponentPortMapping componentPortMapping = globalPortMapping.getComponentPortMappings()
							.get(mappedComp);

					/* If the ports are not generated */
					if (componentPortMapping.getMappedPorts().size() == 0
							&& componentPortMapping.getCardinalityTerm().isCalculated()) {
						/* Generate port names */
						for (int i = 0; i < componentPortMapping.getCardinalityTerm().getValue(); i++) {
							componentPortMapping.getMappedPorts().add(mappedComp + "." + portInnerName + (i + 1));
						}
					}

				}
			}
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public static void calculateVariables(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands) throws Z3Exception, TestFailException {

		/* Configuration of the solver */
		Map<String, String> cfg = new HashMap<String, String>();
		/* Model generation turned on */
		cfg.put("model", "true");
		/* Create the context */
		Context ctx = new Context(cfg);

		/* Map of all variables */
		Map<String, NameValue> variables = new HashMap<String, NameValue>();

		/* Map of variables in the additional constraints */
		Map<String, ArithExpr> additionalConstraints = new HashMap<String, ArithExpr>();
		Map<String, Integer> mapOfOccurences = architectureStyle.getOccurrencesOfVariables();

		/* Map of variables in Z3 */
		Map<String, ArithExpr> variableEpressions = new HashMap<String, ArithExpr>();
		/* List of all constraints in the model */
		List<BoolExpr> constraints = new LinkedList<BoolExpr>();

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Expression for the zero */
		IntExpr zero = ctx.mkInt(0);

		/* Iterate the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {
			/* Get the port tuples */
			List<PortTuple> portTuples = connectorTuple.getPortTuples();
			/* List of matching factors */
			List<ArithExpr> matchingFactors = new LinkedList<ArithExpr>();

			/* Iterate the port tuples */
			for (PortTuple portTuple : portTuples) {

				/* Take multiplicity and degree terms */
				NameValue multiplicityTerm = portTuple.getMultiplicityTerm();
				NameValue degreeTerm = portTuple.getDegreeTerm();

				/* The multiplicity and degree expressions in Z3 */
				IntExpr multiplicityExpr = ctx.mkIntConst(multiplicityTerm.getName());
				IntExpr degreeExp = ctx.mkIntConst(degreeTerm.getName());

				/* Add constraint that they must be greater than zero */
				constraints.add(ctx.mkGt(multiplicityExpr, zero));
				constraints.add(ctx.mkGt(degreeExp, zero));

				/* If multiplicity is variable */
				if (!multiplicityTerm.isCalculated()) {
					variables.put(multiplicityTerm.getName(), multiplicityTerm);
					variableEpressions.put(multiplicityTerm.getName(), multiplicityExpr);

					/* If in the map of additional constraints */
					if (mapOfOccurences.containsKey(multiplicityTerm.getName())) {
						additionalConstraints.put(multiplicityTerm.getName(), multiplicityExpr);
					}
				} else {
					/* Add constraint for equality */
					IntExpr value = ctx.mkInt(multiplicityTerm.getValue());
					constraints.add(ctx.mkEq(multiplicityExpr, value));
				}

				/* If degree is variable */
				if (!degreeTerm.isCalculated()) {
					variables.put(degreeTerm.getName(), degreeTerm);
					variableEpressions.put(degreeTerm.getName(), degreeExp);

					/* If in the map of additional constraints */
					if (mapOfOccurences.containsKey(degreeTerm.getName())) {
						additionalConstraints.put(degreeTerm.getName(), degreeExp);
					}

				} else {
					/* Add constraint for equality */
					IntExpr value = ctx.mkInt(degreeTerm.getValue());
					constraints.add(ctx.mkEq(degreeExp, value));
				}

				/* name and the component instance where it belongs */
				String portInstanceName = portTuple.getPortInstanceName();
				String compInstanceName = portInstanceName.split("\\.")[0];

				/* The mappings where the port belongs */
				ComponentMapping componentMapping;
				GlobalPortMapping globalPortMapping;

				System.err.println(portInstanceName);

				/* If the port tuple is coordinator tuple */
				if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
					componentMapping = architectureStyle.getCoordinatorsMapping().get(compInstanceName);
					globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
				} else {
					componentMapping = architectureOperands.getOperandsMapping().get(compInstanceName);
					globalPortMapping = componentMapping.getGlobalPortMappings().get(portInstanceName);
				}

				/* The cardinality of the component where it belongs */
				NameValue componentCardinality = componentMapping.getCardinalityTerm();
				/* Create expression and constraint for the comp card. */
				IntExpr compCardExpr = ctx.mkIntConst(componentCardinality.getName());
				constraints.add(ctx.mkGt(compCardExpr, zero));

				/* Take the collection of all port mappings */
				Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings()
						.values();
				/* Array of port cardinalities */
				IntExpr[] portCardinalitiesExpr = new IntExpr[componentPortMappings.size()];

				/* Counter */
				int i = 0;
				/* Iterate over them to take the cardinalities */
				for (ComponentPortMapping cpm : componentPortMappings) {

					/* Create expression and constrain for the port card. */
					IntExpr portCardExpr = ctx.mkIntConst(cpm.getCardinalityTerm().getName());
					constraints.add(ctx.mkGt(portCardExpr, zero));

					/* If the port cardinality is not known */
					if (!cpm.getCardinalityTerm().isCalculated()) {
						/* Add in the list of variables */
						variables.put(cpm.getCardinalityTerm().getName(), cpm.getCardinalityTerm());
						variableEpressions.put(cpm.getCardinalityTerm().getName(), portCardExpr);

						/* If in the map of additional constraints */
						if (mapOfOccurences.containsKey(cpm.getCardinalityTerm().getName())) {
							additionalConstraints.put(cpm.getCardinalityTerm().getName(), portCardExpr);
						}

					} else {
						/* Make equality constrain */
						constraints.add(ctx.mkEq(portCardExpr, ctx.mkInt(cpm.getCardinalityTerm().getValue())));
					}

					/* Add in the list of port card. expressions */
					portCardinalitiesExpr[i] = portCardExpr;
					i++;
				}

				/* Make sum of cardinalities */
				ArithExpr sumOfPortCard = ctx.mkAdd(portCardinalitiesExpr);
				/* Add constraint for the sum */
				constraints.add(ctx.mkGt(sumOfPortCard, zero));

				/* Add the first consistency constraint */
				constraints.add(ctx.mkLe(multiplicityExpr, sumOfPortCard));

				/* Create matching factor */
				ArithExpr matchingFactor = ctx.mkDiv(ctx.mkMul(new ArithExpr[] { sumOfPortCard, degreeExp }),
						multiplicityExpr);
				matchingFactors.add(matchingFactor);

			}

			/* This is not a solution */
			if (matchingFactors.size() == 1) {
				constraints.add(ctx.mkGt(matchingFactors.get(0), zero));
			} else {
				/* Make all matching factors equal */
				for (int i = 1; i < matchingFactors.size(); i++) {
					constraints.add(ctx.mkEq(matchingFactors.get(i), matchingFactors.get(i - 1)));
				}
			}

		}

		/* Take the additional constraints */
		List<String> listOfAdditionalConst = architectureStyle.getAdditionalConstraints();
		/* Strings for splitting */
		String equal = "=";
		String mathOperation = "[*+/-]";

		for (String constraint : listOfAdditionalConst) {
			String[] tokens = constraint.split(equal);
			String leftSide = tokens[0];
			String rightSide = tokens[1];
			
			/*  */
			String[] leftSideTokens = leftSide.split(mathOperation);
		}

		BoolExpr[] arrayConstraints = new BoolExpr[constraints.size()];
		for (int i = 0; i < constraints.size(); i++) {
			arrayConstraints[i] = constraints.get(i);
		}

		/* The final constraint */
		BoolExpr finalConstraint = ctx.mkAnd(arrayConstraints);
		Model model = Check(ctx, finalConstraint, Status.SATISFIABLE);

		/* Insert values for variables */
		for (String name : variableEpressions.keySet()) {
			variables.get(name)
					.setValue(Integer.parseInt(model.evaluate(variableEpressions.get(name), false).toString()));
		}

		/* Generate the missing ports */
		generateMissingPortNames(architectureStyle);

		checkNameValues(architectureStyle, architectureOperands);
	}

	public static Model Check(Context ctx, BoolExpr f, Status sat) throws Z3Exception, TestFailException {
		Solver s = ctx.mkSolver();
		s.add(f);
		if (s.check() != sat)
			throw new TestFailException();
		if (sat == Status.SATISFIABLE)
			return s.getModel();
		else
			return null;
	}
}
