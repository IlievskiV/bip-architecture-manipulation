package ch.epfl.risd.archman.model;

import java.util.LinkedList;
import java.util.List;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;

/**
 * This class will represent one tuple from the connector line in the
 * configuration file. For example in the configuration file, one connector
 * tuple could be: {B_to_C_begin,t,1,n,b,1,1}. The members of this tuple are:
 * B_to_C_begin is the name of the connector instance, t is the port from the
 * coordinator, 1 is the "multiplicity" of this port, n is the "degree" of this
 * port, b is the port from the operand, 1 is the "multiplicity" of this port, 1
 * is the "degree" of this port
 */
public class ConnectorTuple {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The name of the connector instance */
	private String connectorInstanceName;

	/*
	 * A list of port tuples, they appear in the same order as the arguments in
	 * the port
	 */
	private List<PortTuple> portTuples;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	public ConnectorTuple(String connectorInstanceName, List<PortTuple> portTuples) {
		this.connectorInstanceName = connectorInstanceName;
		this.portTuples = portTuples;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name of the connector instance: " + this.connectorInstanceName + ". Port Tuples:");
		sb.append("\n");
		for (PortTuple p : portTuples) {
			sb.append("\t" + p.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * @return the name of the connector instance
	 */
	public String getConnectorInstanceName() {
		return connectorInstanceName;
	}

	/**
	 * @return the list of all port tuples
	 */
	public List<PortTuple> getPortTuples() {
		return portTuples;
	}

	/**
	 * @return the list of all operand type port tuples
	 */
	public List<PortTuple> getOperandPortTuples() {
		List<PortTuple> result = new LinkedList<PortTuple>();

		/* Iterate all port tuples */
		for (PortTuple portTuple : this.portTuples) {
			/* Check if the port tuple is operand type */
			if (portTuple.getType() == PortTupleType.OPERAND_TUPLE) {
				result.add(portTuple);
			}
		}

		return result;
	}

	/**
	 * @return the list of all coordinator type port tuples
	 */
	public List<PortTuple> getCoordinatorPortTuples() {
		List<PortTuple> result = new LinkedList<PortTuple>();

		/* Iterate all port tuples */
		for (PortTuple portTuple : this.portTuples) {
			/* Check if the port tuple is coordinator type */
			if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
				result.add(portTuple);
			}
		}

		return result;
	}
}
