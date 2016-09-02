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

	/****************************************************************************/
	/* PUBLIC METHODS */
	/**
	 * @throws TestFailedException
	 * @throws Z3Exception
	 *************************************************************************/

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
				} else {
					/* Add constraint for equality */
					IntNum value = ctx.mkInt(multiplicityTerm.getValue());
					constraints.add(ctx.mkEq(multiplicityExpr, value));
				}

				/* If degree is variable */
				if (!degreeTerm.isCalculated()) {
					variables.put(degreeTerm.getName(), degreeTerm);
				} else {
					/* Add constraint for equality */
					IntNum value = ctx.mkInt(degreeTerm.getValue());
					constraints.add(ctx.mkEq(degreeExp, value));
				}

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

				/* The cardinality of the component where it belongs */
				NameValue componentCardinality = componentMapping.getCardinalityTerm();
				/* Create expression and constraint for the comp card. */
				IntExpr compCardExpr = ctx.mkIntConst(componentCardinality.getName());
				constraints.add(ctx.mkGt(compCardExpr, zero));

				/* Take the collection of all port mappings */
				Collection<ComponentPortMapping> componentPortMappings = globalPortMapping.getComponentPortMappings()
						.values();
				/* Array of port cardinalities */
				ArithExpr[] portCardinalitiesExpr = new ArithExpr[componentCardinality.getValue()];

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
					} else {
						/* Make equality constrain */
						constraints.add(ctx.mkEq(portCardExpr, ctx.mkInt(cpm.getCardinalityTerm().getValue())));
					}

					/* Add in the list of port card. expressions */
					portCardinalitiesExpr[i] = portCardExpr;
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

			/* Make all matching factors equal */
			for (int i = 1; i < matchingFactors.size(); i++) {
				constraints.add(ctx.mkEq(matchingFactors.get(i), matchingFactors.get(i - 1)));
			}

		}

		BoolExpr[] arrayConstraints = new BoolExpr[constraints.size()];
		for (int i = 0; i < constraints.size(); i++) {
			arrayConstraints[i] = constraints.get(i);
		}

		/* The final constraint */
		BoolExpr finalConstraint = ctx.mkAnd(arrayConstraints);
		Model model = Check(ctx, finalConstraint, Status.SATISFIABLE);
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
