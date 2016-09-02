package ch.epfl.risd.archman.builder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.TestFailException;
import ch.epfl.risd.archman.extractor.ArchitectureStyleExtractor;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ComponentMapping;
import ch.epfl.risd.archman.model.ComponentPortMapping;
import ch.epfl.risd.archman.model.GlobalPortMapping;
import ch.epfl.risd.archman.solver.ArchitectureStyleSolver;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.Component;

/**
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureInstantiator {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Helper method to plug all ports in the instance from the style and the
	 * operands
	 */
	private static void plugAllPorts(ArchitectureStyle architectureStyle, ArchitectureOperands architectureOperands,
			ArchitectureInstance instance) throws ArchitectureExtractorException {

		List<PortType> allPortTypes = new LinkedList<PortType>();
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureStyle.getBipFileModel()));
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureOperands.getBipFileModel()));
		ArchitectureInstanceBuilder.copyAllPortTypes(instance, allPortTypes);
	}

	private static void plugAllCoordinators(ArchitectureStyle architectureStyle, ArchitectureInstance instance)
			throws ArchitectureExtractorException {
		/* Take all coordinators */
		List<Component> coordinators = ArchitectureStyleExtractor.getArchitectureStyleCoordinators(architectureStyle);
		/* Iterate over them */
		for (Component c : coordinators) {
			String typeName = c.getType().getName();
			/* Take the mapping of this component */
			ComponentMapping componentMapping = architectureStyle.getCoordinatorsMapping().get(c.getName());
			/* Take the set of components to which is mapped */
			Set<String> mappedComponents = componentMapping.getMappedComponents();
			/* Take the mapping of ports */
			Map<String, GlobalPortMapping> globalPortMappings = componentMapping.getGlobalPortMappings();

			/* Iterate over mapped components */
			for (String mappedComp : mappedComponents) {
				/* Take the set of ports to map */
				Set<String> portsToMap = globalPortMappings.keySet();

				/* Iterate over the map of ports */
				for (String portMap : portsToMap) {
					/* Take the mapping of the port */
					GlobalPortMapping globalPortMapping = globalPortMappings.get(portMap);
					/* Take the port mappings for the current component */
					ComponentPortMapping componentPortMapping = globalPortMapping.getComponentPortMappings()
							.get(mappedComp);

					/* Get the set of ports to map */
					Set<String> mappedPorts = componentPortMapping.getMappedPorts();

					/*
					 * Now I can make a type where I will delete the port
					 * portMap and insert the ports mappedPorts
					 */
				}
			}

		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * 
	 * @param architectureStyle
	 * @param architectureOperands
	 * @param architectureInstanceBIPFile
	 * @param pathToSaveBIPFile
	 * @param pathToSaveConfFile
	 * @return
	 * @throws ArchitectureBuilderException
	 * @throws ArchitectureExtractorException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TestFailedException
	 * @throws Z3Exception
	 */
	public static ArchitectureInstance createArchitectureInstance(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands, String systemName, String rootTypeName, String rootInstanceName,
			String pathToSaveBIPFile, String pathToSaveConfFile) throws ArchitectureBuilderException,
			ArchitectureExtractorException, IOException, InterruptedException, Z3Exception, TestFailException {

		/* 1. Create an empty architecture instance */
		ArchitectureInstance instance = new ArchitectureInstance(systemName, rootTypeName, rootInstanceName);

		/* 2. Calculate variables */
		ArchitectureStyleSolver.calculateVariables(architectureStyle, architectureOperands);

		/* 3.Take all Port Types and plug them */
		plugAllPorts(architectureStyle, architectureOperands, instance);

		/* 4.Take all coordinators and plug them */
		plugAllCoordinators(architectureStyle, instance);

		return instance;
	}

	public static void main(String[] args) throws ConfigurationFileException, ArchitectureExtractorException,
			IOException, ArchitectureBuilderException {

	}

}
