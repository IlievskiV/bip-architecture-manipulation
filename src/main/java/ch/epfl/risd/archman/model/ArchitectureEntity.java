package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;

/**
 * This class represents the base class of the Architecture Instance, Style and
 * Operands.
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public abstract class ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The model of the BIP file representing this entity */
	protected BIPFileModel bipFileModel;

	/* The model of the configuration file for this entity */
	protected ConfigurationFileModel archEntityConfigFile;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Method for parsing the parameters from the hash table of parameters
	 * 
	 * @throws ConfigurationFileException
	 *             if the configuration file was not in the predefined format
	 */
	protected abstract void parseParameters() throws ConfigurationFileException;

	/**
	 * Method for validating the Architecture Entity, i.e. it checks whether the
	 * Architecture Entity is consistent with the information in the
	 * configuration file.
	 * 
	 * @throws ArchitectureExtractorException
	 * @throws ComponentNotFoundException
	 */
	protected abstract void validate() throws ComponentNotFoundException, ArchitectureExtractorException;

	/**
	 * Method for validating the existing of components in the Architecture
	 * Entity. It checks whether all of the components specified in the
	 * configuration file exist in the BIP system of the Architecture Entity.
	 * 
	 * @param componentInstanceNames
	 *            - set of names of the components
	 * @throws ArchitectureExtractorException
	 *             if some component specified in the configuration file does
	 *             not exist in the BIP system
	 */
	protected void validateComponents(Set<String> componentInstanceNames) throws ArchitectureExtractorException {

		/* Iterate over the set of component instance names */
		for (String componentInstanceName : componentInstanceNames) {
			if (!BIPChecker.componentExists(this.bipFileModel, componentInstanceName)) {
				throw new ComponentNotFoundException(
						"The component with name " + componentInstanceName + "does not exist in the BIP file");
			}
		}
	}

	/**
	 * Method for validating the existence of the ports in the Architecture
	 * Entity. It checks whether all of the ports specified in the configuration
	 * file exist in the BIP system if the Architecture Entity.
	 * 
	 * @param fullPortNames
	 *            - full name of the port (example C1.begin)
	 * @throws PortNotFoundException
	 *             if some of the ports specified in the configuration file does
	 *             not exist in the BIP system
	 * @throws ArchitectureExtractorException
	 */
	protected void validatePorts(Set<String> fullPortNames)
			throws PortNotFoundException, ArchitectureExtractorException {
		/* Iterate over the set of port instance names */
		for (String fullPortName : fullPortNames) {
			String[] tokens = fullPortName.split("\\.");
			String portInstanceName = tokens[1];
			String componentInstanceName = tokens[0];
			if (!BIPChecker.portExists(this.bipFileModel, portInstanceName, componentInstanceName)) {
				throw new PortNotFoundException("The port with name " + portInstanceName
						+ " in the component with name " + componentInstanceName + " does not exist");
			}
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, when the configuration file is having values
	 * for the required parameters where the path to the BIP file is absolute
	 * 
	 * @param pathToConfFile
	 *            - absolute path to the configuration file
	 * @param requiredParams
	 *            - list of required parameters in the configuration file
	 * @throws ConfigurationFileException
	 * @throws ArchitectureExtractorException
	 * @throws ComponentNotFoundException
	 */
	public ArchitectureEntity(String pathToConfFile, List<String> requiredParams)
			throws ConfigurationFileException, ComponentNotFoundException, ArchitectureExtractorException {
		this.archEntityConfigFile = new ConfigurationFileModel(pathToConfFile, requiredParams);
		this.bipFileModel = new BIPFileModel(this.archEntityConfigFile.getParameters().get(ConstantFields.PATH_PARAM));
		this.parseParameters();
		// this.validate();
	}

	/**
	 * Constructor for this class, when the configuration file is having values
	 * for the required parameters, where the path to the BIP file is relative,
	 * so the prefix must be given.
	 * 
	 * @param prefixToBip
	 *            - the prefix before the relative path of the BIP file
	 * @param pathToConfFile
	 *            - absolute path to the configuration file
	 * @param requiredParams
	 *            - list of required parameters in the configuration file
	 * @throws ConfigurationFileException
	 * @throws ArchitectureExtractorException
	 * @throws ComponentNotFoundException
	 */
	public ArchitectureEntity(String prefixToBip, String pathToConfFile, List<String> requiredParams)
			throws ConfigurationFileException, ComponentNotFoundException, ArchitectureExtractorException {
		this.archEntityConfigFile = new ConfigurationFileModel(pathToConfFile, requiredParams);
		this.bipFileModel = new BIPFileModel(
				prefixToBip + this.archEntityConfigFile.getParameters().get(ConstantFields.PATH_PARAM));
		this.parseParameters();
		// this.validate();
	}

	/**
	 * Constructor for this class, when the configuration file is not containing
	 * values for the required parameters, and the BIP file is empty
	 * 
	 * @param systemName
	 *            - the name of the BIP system's module
	 * @param rootTypeName
	 *            - the name of the type of the root component in the BIP system
	 * @param rootInstanceName
	 *            - the name of the root instance component in the BIP system
	 * @param requiredParams
	 */
	public ArchitectureEntity(String systemName, String rootTypeName, String rootInstanceName,
			List<String> requiredParams) {
		/* Create empty configuration file */
		this.archEntityConfigFile = new ConfigurationFileModel(requiredParams);
		/* Create empty BIP file model */
		this.bipFileModel = new BIPFileModel(systemName, rootTypeName, rootInstanceName);
	}

	/**
	 * Method for generating and saving the BIP file in the file system,
	 * corresponding to the Architecture Entity.
	 * 
	 * @param pathToBipFile
	 *            - path in the file system, to save the BIP file
	 * @throws FileNotFoundException
	 */
	public void generateBipFile(String pathToBipFile) throws FileNotFoundException {
		this.archEntityConfigFile.getParameters().put(ConstantFields.PATH_PARAM, pathToBipFile);
		this.bipFileModel.createFile(pathToBipFile);
	}

	/**
	 * 
	 * @param pathToConfFile
	 * @throws IOException
	 */
	public void generateConfigurationFile(String pathToConfFile) throws IOException {
		this.archEntityConfigFile.createFile(pathToConfFile);
	}

	/**
	 * @return the BIP file model of the Architecture Entity
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

}
