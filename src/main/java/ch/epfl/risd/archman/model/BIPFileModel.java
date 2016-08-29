package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.util.List;

import BIPTransformation.TransformationFunction;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.factories.Factories;
import ujf.verimag.bip.Core.Behaviors.ComponentType;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinition;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.Variable;
import ujf.verimag.bip.Core.Behaviors.impl.AtomTypeImpl;
import ujf.verimag.bip.Core.Behaviors.impl.DefinitionBindingImpl;
import ujf.verimag.bip.Core.Behaviors.impl.PortDefinitionImpl;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Modules.impl.RootImpl;
import ujf.verimag.bip.Core.Modules.impl.SystemImpl;

/**
 * This class is representing the model of the BIP file in object-oriented
 * manner. This class is encapsulating required information for one BIP file.
 */
public class BIPFileModel {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/**
	 * The system of the BIP model, or the module of the architecture. This is
	 * BIP specific variable
	 */
	private SystemImpl system;

	/**
	 * The root of the system. This is BIP specific variable.
	 */
	private RootImpl root;

	/**
	 * The type of the root in the BIP system. The definition of type is similar
	 * with the definition of class in OOP. This is BIP specific variable.
	 */
	private CompoundType rootType;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/****************************************************************************/
	/* PUBLIC METHODS */
	/****************************************************************************/

	/**
	 * Constructor for this class
	 * 
	 * @param path
	 *            - The path to the BIP file
	 */
	public BIPFileModel(String path) {
		/* Parse the BIP model */
		this.rootType = TransformationFunction.ParseBIPFile(path);

		/* Get the system of the BIP model */
		this.system = (SystemImpl) this.rootType.getModule();

		/* Get the root of the BIP model */
		this.root = (RootImpl) this.system.getRoot();
	}

	/**
	 * Constructor of the class, for building a new BIP model
	 * 
	 * @param systemName
	 *            - the name of the system
	 * @param rootTypeName
	 *            - the name of the type definition of the root
	 * @param rootInstanceName
	 *            - the name of the root component, which is instance of the
	 *            type of the root
	 */
	public BIPFileModel(String systemName, String rootTypeName, String rootInstanceName) {
		/* Create new system and set name */
		this.system = (SystemImpl) Factories.MODULES_FACTORY.createSystem();
		this.system.setName(systemName);

		/* Create the type of the root, set module and name */
		this.rootType = Factories.INTERACTIONS_FACTORY.createCompoundType();
		this.rootType.setName(rootTypeName);
		this.rootType.setModule(this.system);

		/* Create root, set module, type and name */
		RootImpl root = (RootImpl) Factories.MODULES_FACTORY.createRoot();
		root.setSystem(this.system);
		root.setType(rootType);
		root.setName(rootInstanceName);
	}

	/**
	 * This method changes the system of every component, port and connector
	 * type
	 * 
	 * @param systemImpl
	 *            - The new system implementation
	 * @throws ArchitectureExtractorException
	 */
	public void changeSystem(SystemImpl systemImpl) throws ArchitectureExtractorException {

		/* Get all components */
		List<ComponentType> allComponentTypes = BIPExtractor.getAllComponentTypes(this);
		/* Get all ports */
		List<PortType> allPortTypes = BIPExtractor.getAllPortTypes(this);
		/* Get all connectors */
		List<ConnectorType> allConnectorTypes = BIPExtractor.getAllConnectorTypes(this);

		/* Iterate component types */
		for (ComponentType ct : allComponentTypes) {
			/* Change the module of the component type */
			ct.setModule(systemImpl);
		}

		/* Iterate port types */
		for (PortType pt : allPortTypes) {
			/* Change the module of the port type */
			pt.setModule(systemImpl);
		}

		/* Iterate connector types */
		for (ConnectorType ct : allConnectorTypes) {
			/* Change the module of the connector type */
			ct.setModule(systemImpl);
		}

		/* Change the module of the root type */
		this.rootType.setModule(systemImpl);

		/* Change the system */
		this.system = systemImpl;
	}

	/**
	 * Method to generate the resulting BIP file
	 * 
	 * @param pathToBIPFile
	 *            - absolute path, where the BIP file should be written
	 * @throws FileNotFoundException
	 */
	public void createFile(String pathToBIPFile) throws FileNotFoundException {
		/* Write the generated code in the file */
		TransformationFunction.CreateBIPFile(pathToBIPFile, this.system);
	}

	/**
	 * @return The system of the BIP model
	 */
	public SystemImpl getSystem() {
		return system;
	}

	/**
	 * @param system
	 *            - The system of the BIP file model
	 */
	public void setSystem(SystemImpl system) {
		this.system = system;
	}

	/**
	 * index
	 * 
	 * @return The root of the BIP model
	 */
	public RootImpl getRoot() {
		return root;
	}

	/**
	 * @return The type of the root of the BIP model
	 */
	public CompoundType getRootType() {
		return rootType;
	}

	public static void main(String[] args) {
		BIPFileModel bipFileModel = new BIPFileModel(
				"/home/vladimir/workspace/bip-architecture-manipulation/TestCases/Instantiation/Output/ParallelMem/SaveToMemInstance.bip");

		try {
			List<ConnectorType> allConnectorTypes = BIPExtractor.getAllConnectorTypes(bipFileModel);

			for (ConnectorType ct : allConnectorTypes) {
				System.out.println(ct);
			}

		} catch (ArchitectureExtractorException e) {
			e.printStackTrace();
		}

	}
}
