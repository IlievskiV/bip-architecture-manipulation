package ch.epfl.risd.archman.extractor;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ComponentMapping;
import ch.epfl.risd.archman.model.GlobalPortMapping;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Interactions.Component;

public class ArchitectureOperandsExtractor {

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * This method returns all Operands in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @return the list of all Operands
	 * @throws ArchitectureExtractorException
	 */
	public static List<Component> getArchitectureOperands(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {

		/* Initialize the list of operands */
		List<Component> operands = new LinkedList<Component>();
		/* Get the list of all components */
		List<Component> allComponents = BIPExtractor.getAllComponents(architectureOperands.getBipFileModel());

		/* Flag for the existence of the coordinator in the BIP model */
		boolean flag;

		for (String key : architectureOperands.getOperandsMapping().keySet()) {
			for (String s : architectureOperands.getOperandsMapping().get(key).getMappedComponents()) {
				flag = false;

				/* Iterate all components */
				for (Component c : allComponents) {
					if (c.getName().equals(s)) {
						flag = true;
						operands.add(c);
						break;
					}
				}

				/* If the coordinator does not exist */
				if (!flag) {
					throw new ComponentNotFoundException("Operand " + s + " does not exist");
				}
			}
		}

		return operands;

	}

	/**
	 * This method prints all Operands in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @throws ArchitectureExtractorException
	 */
	public static void printArchitectureOperands(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {
	}

	/**
	 * This method returns the operands that should substitute the given
	 * parameter operand
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @param parameterOperand
	 *            - The name of the parameter operand mapped to the set of
	 *            operands
	 * @return the list of operands that can substitute the parameter operand
	 * @throws Exception
	 */
	public static List<Component> getSubstitutionOperands(ArchitectureOperands architectureOperands,
			String parameterOperand) throws Exception {
		return null;
	}

	/**
	 * This method returns all Ports in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @return the list of all ports in the Architecture Operands
	 * @throws ArchitectureExtractorException
	 */
	public static List<Port> getArchitectureOperandsPorts(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {
		return null;
	}

	/**
	 * This method returns all ports that should substitute the given parameter
	 * port
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @param parameterPort-
	 *            the name of the parameter port mapped to the set of ports
	 * @return the list of ports that can substitute the parameter port
	 * @throws ArchitectureExtractorException
	 */
	public static List<Port> getAllSubstitutionPorts(ArchitectureOperands architectureOperands, String parameterPort)
			throws ArchitectureExtractorException {
		return null;
	}

	/**
	 * This method prints all ports in the Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 */
	public void printPorts(ArchitectureOperands architectureOperands) {

	}
}
