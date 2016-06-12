package ch.epfl.risd.archman.extractor;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Interactions.Component;

public interface ArchitectureOperandsExtractor {

	/**
	 * This method returns all Operands in the Architecture Operands
	 * 
	 * @return List of all Operands
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getArchitectureOperands() throws ArchitectureExtractorException;

	/**
	 * This method prints all Operands in the Architecture Operands
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printArchitectureOperands() throws ArchitectureExtractorException;

	/**
	 * This method returns the operands that should substitute the given
	 * parameter operand
	 * 
	 * @param parameterOperand
	 *            - The name of the parameter operand mapped to the set of
	 *            operands
	 * @return the list of operands that can substitute the parameter operand
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getSubstitutionOperands(String parameterOperand) throws ArchitectureExtractorException;

	/**
	 * This method returns all Ports in the Architecture Operands
	 *
	 * @return the list of all ports in the Architecture Operands
	 * @throws ArchitectureExtractorException
	 */
	public List<Port> getArchitectureOperandsPorts() throws ArchitectureExtractorException;

	/**
	 * This method returns all ports that should substitute the given parameter
	 * port
	 * 
	 * @param parameterPort
	 *            - The name of the parameter port mapped to the set of ports
	 * @return the list of ports that can substitute the parameter port
	 * @throws ArchitectureExtractorException
	 */
	public List<Port> getSubstitutionPorts(String parameterPort) throws ArchitectureExtractorException;

	/**
	 * This method prints all ports in the Architecture Operands
	 */
	public void printPorts();
}
