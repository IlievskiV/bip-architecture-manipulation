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

public class ArchitectureStyleExtractor {

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * This method returns all Coordinators in the given Architecture Style.
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 * @return the list of all Coordinators
	 * @throws ArchitectureExtractorException
	 */
	public static List<Component> getArchitectureStyleCoordinators(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {
		/* Instantiate the list of coordinators */
		List<Component> coordinators = new LinkedList<Component>();
		/* Get all components from the BIP model */
		List<Component> components = BIPExtractor.getAllComponents(architectureStyle.getBipFileModel());

		/* Flag for the existence of the coordinator in the BIP model */
		boolean flag;

		/* Iterate coordinators names */
		for (String s : architectureStyle.getCoordinators()) {
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

	/**
	 * This method prints the Coordinators in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 * @throws ArchitectureExtractorException
	 */
	public static void printArchitectureStyleCoordinators(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {
		System.out.println("Coordinators are: ");

		/* Iterate coordinators */
		for (Component c : ArchitectureStyleExtractor.getArchitectureStyleCoordinators(architectureStyle)) {
			System.out.println("\t Name: " + c.getName() + "(type: " + c.getType().getName() + ")");
		}
		System.out.println();
	}

	/**
	 * This method returns all Operands in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 * @return the list of all Operands in the Architecture Style
	 * @throws ArchitectureExtractorException
	 */
	public static List<Component> getArchitectureStyleOperands(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {
		/* Instantiate the list of operands */
		List<Component> result = new LinkedList<Component>();
		/* Get all components from the BIP model */
		List<Component> components = BIPExtractor.getAllComponents(architectureStyle.getBipFileModel());

		/* Iterate components of the BIP model */
		for (Component c : components) {
			if (architectureStyle.getOperands().contains(c.getName())) {
				result.add(c);
			}
		}

		return result;
	}

	/**
	 * This method prints all Operands in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style objects
	 * @throws ArchitectureExtractorException
	 */
	public static void printArchitectureStyleOperands(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {
		System.out.println("Operands are: ");

		/* Iterate operands */
		for (Component c : ArchitectureStyleExtractor.getArchitectureStyleOperands(architectureStyle)) {
			System.out.println("\t Name: " + c.getName() + "(type: " + c.getType().getName() + ")");
		}
		System.out.println();
	}

	/**
	 * This method returns all ports in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 * @return the list of ports in the Architecture Style
	 * @throws ArchitectureExtractorException
	 */
	public static List<Port> getArchitectureStylePorts(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {
		/* Get all ports in the Architecture Style */
		List<Port> allPorts = BIPExtractor.getAllPorts(architectureStyle.getBipFileModel());

		return allPorts;
	}

	/**
	 * This method prints all ports in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 */
	public static void printArchitectureStylePorts(ArchitectureStyle architectureStyle) {
		StringBuilder sb = new StringBuilder();
		sb.append("The ports in the Architecture Style are: ");

		/* Iterate the list of port names */
		for (String s : architectureStyle.getPorts()) {
			sb.append(s + ", ");
		}
		sb.append("\n");
		System.out.println(sb.toString());
	}

	/**
	 * This method returns all connectors in the given Architecture Style
	 * 
	 * @param architectureStyle
	 *            - the given Architecture Style object
	 * @return the list of all connectors in the Architecture Style
	 * @throws ArchitectureExtractorException
	 */
	public static List<Connector> getArchitectureStyleConnectors(ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException {

		/* The resulting list */
		List<Connector> result = new LinkedList<Connector>();

		/* Get all connectors in the Architecture Style */
		List<Connector> allConnectors = BIPExtractor.getAllConnectors(architectureStyle.getBipFileModel());

		/* Iterate all connectors */
		for (Connector c : allConnectors) {
			/* Iterate Connector Tuples */
			for (ConnectorTuple tuple : architectureStyle.getConnectorsTuples()) {
				if (tuple.getConnectorInstanceName().equals(c.getName())) {
					result.add(c);
					break;
				}
			}
		}

		return result;
	}
}
