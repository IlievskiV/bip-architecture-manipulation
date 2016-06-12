package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * This class will represent one instance of the architecture when we will
 * substitute the parameter operands, with the exact operands.
 */
public class ArchitectureInstance {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The BIP file model for this Architecture Instance */
	private BIPFileModel bipFileModel;

	/* Parameters written in the Configuration file */
	private Hashtable<String, String> parameters;

	/* List of operands for this Architecture Instance */
	private List<String> operands;

	/* List of ports for this Architecture Instance */
	private List<String> ports;

	/* List of interactions for this Architecture Instance */
	private List<String> interactions;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, creates an empty Architecture Instance, where
	 * the lists of ports, operands and interactions are empty. Only the BIP
	 * file where the architecture will be generated as a BIP code is specified.
	 * 
	 * @param systemName
	 *            - the name of the BIP system
	 * @param rootTypeName
	 *            - the name of the root type in the BIP system
	 * @param rootName
	 *            - the name of the root component in the BIP system
	 */
	public ArchitectureInstance(String systemName, String rootTypeName, String rootName) {
		/* Create empty BIP file model */
		this.bipFileModel = new BIPFileModel(systemName, rootTypeName, rootName);
		/* Instantiate the parameters */
		this.parameters = new Hashtable<String, String>();
		/* Instantiate the list of operands */
		this.operands = new LinkedList<String>();
		/* Instantiate the list of ports */
		this.ports = new LinkedList<String>();
		/* Instantiate the list of interactions */
		this.interactions = new LinkedList<String>();
	}

	/**
	 * Constructor for this class, when all field of the architecture instance
	 * are already given.
	 * 
	 * @param bipFileModel
	 *            - the BIP file where the architecture is written
	 * @param parameters
	 *            - the map of parameters in the configuration file
	 * @param operands
	 *            - the list of operands
	 * @param ports
	 *            - the list of ports
	 * @param interactions
	 *            - the list of interactions
	 */
	public ArchitectureInstance(BIPFileModel bipFileModel, Hashtable<String, String> parameters, List<String> operands,
			List<String> ports, List<String> interactions) {
		/* Assign the references */
		this.bipFileModel = bipFileModel;
		this.parameters = parameters;
		this.operands = operands;
		this.ports = ports;
		this.interactions = interactions;
	}

	/**
	 * Constructor for this class, creates an architecture instance with the
	 * informations given in the configuration file.
	 * 
	 * @param pathToConfFile
	 *            - the path to the configuration file
	 * @throws FileNotFoundException
	 */
	public ArchitectureInstance(String pathToConfFile) throws FileNotFoundException {

		/* Get the absolute path to the configuration file */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Reading and parsing the configuration file */
		Scanner scanner = new Scanner(new File(absolutePath));

		while (scanner.hasNext()) {
			
		}

	}

	/**
	 * @return the BIP file model
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

	/**
	 * @return the parameters of the Architecture Instance
	 */
	public Hashtable<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @return the list of operands of the Architecture Instance
	 */
	public List<String> getOperands() {
		return operands;
	}

	/**
	 * @return the list of ports of the Architecture Instance
	 */
	public List<String> getPorts() {
		return ports;
	}

	/**
	 * @return the list of interactions of the Architecture Instance
	 */
	public List<String> getInteractions() {
		return interactions;
	}

}
