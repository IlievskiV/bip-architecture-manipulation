package ch.epfl.risd.archman.solver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ComponentMapping;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ch.epfl.risd.archman.model.NameValue;
import ch.epfl.risd.archman.model.PortMapping;
import ch.epfl.risd.archman.model.PortTuple;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;

/**
 * Class using the SMT Z3 solver, to find the unknown variables in the style in
 * order to instantiate an architecture.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class Solver {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public static void calculateVariables(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands) {

		/* Map of all variables */
		Map<String, NameValue> variables = new HashMap<String, NameValue>();

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Iterate the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {
			/* Get the port tuples */
			List<PortTuple> portTuples = connectorTuple.getPortTuples();
			/* Iterate the port tuples */
			for (PortTuple portTuple : portTuples) {
				/* name and the component instance where it belongs */
				String portInstanceName = portTuple.getPortInstanceName();
				String compInstanceName = portInstanceName.split("\\.")[0];

				/* The mappings where the port belongs */
				ComponentMapping componentMapping;
				PortMapping portMapping;

				/* If the port tuple is coordinator tuple */
				if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
					componentMapping = architectureStyle.getCoordinatorsMapping().get(compInstanceName);
					portMapping = componentMapping.getPortMappings().get(portInstanceName);
				} else {
					componentMapping = architectureOperands.getOperandsMapping().get(compInstanceName);
					portMapping = componentMapping.getPortMappings().get(portInstanceName);
				}

				/*
				 * Using Z3 we should make one term of the equation and add the
				 * variables
				 */
			}
		}
	}

}
