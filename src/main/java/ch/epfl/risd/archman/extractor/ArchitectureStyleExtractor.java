package ch.epfl.risd.archman.extractor;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.Connector;

public interface ArchitectureStyleExtractor {

	/**
	 * This method returns all Coordinators in the Architecture.
	 * 
	 * @return List of all Coordinators
	 * @throws ComponentNotFoundException
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getArchitectureStyleCoordinators() throws ComponentNotFoundException, ArchitectureExtractorException;

	/**
	 * This method prints the coordinators in the architecture. In the
	 * configuration file the coordinator parameter should be in this format:
	 * coordinators:C1;C2;C3
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printArchitectureStyleCoordinators() throws ArchitectureExtractorException;

	/**
	 * This method returns all Operands in the Architecture
	 * 
	 * @return List of all Operands
	 * @throws ArchitectureExtractorException
	 */
	public List<Component> getArchitectureStyleOperands() throws ArchitectureExtractorException;

	/**
	 * This method prints all Operands in the Architecture
	 * 
	 * @throws ArchitectureExtractorException
	 */
	public void printArchitectureStyleOperands() throws ArchitectureExtractorException;

	public List<Port> getArchitectureStylePorts() throws ArchitectureExtractorException;

	public void printArchitectureStylePorts();

	public List<Connector> getArchitectureStyleConnectors() throws ArchitectureExtractorException;

}
