package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;

/**
 * This class contains the operands of the architecture, i.e. the operands that
 * have to be substituted, in order to have one instance of the architecture
 */
public class ArchitectureOperands extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/*
	 * Which parameter operand from the Architecture Style maps to which set of
	 * operands
	 */
	private Hashtable<String, Set<String>> operandsMapping;

	/* Which port in one parameter operand maps to which set of ports */
	private Hashtable<String, Set<String>> portsMapping;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	@Override
	protected void readParameters(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException {
		/* Instantiate the hash table */
		parameters = new Hashtable<String, String>();

		/* Flag for existence of the PATH parameter in the configuration file */
		boolean hasPath = false;

		/*
		 * Flag for existence of the OPERANDS parameter in the configuration
		 * file
		 */
		boolean hasOperands = false;

		/* Flag for existence PORTS parameter in the configuration file */
		boolean hasPorts = false;

		/* Get the absolute path to the configuration file */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Reading and parsing the configuration file */
		Scanner scanner = new Scanner(new File(absolutePath));

		while (scanner.hasNext()) {
			String[] tokens = scanner.nextLine().split(":");

			/* No more than one colon in a line exception */
			if (tokens.length > 2) {
				throw new ConfigurationFileException("More than one colon (:) in the line");
			}

			/* Check for PATH parameter */
			if (tokens[0].equals(ConstantFields.PATH_PARAM)) {
				hasPath = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the PATH parameter is missing");
				}
			}

			/* Check for OPERANDS parameter */
			if (tokens[0].equals(ConstantFields.OPERANDS_PARAM)) {
				hasOperands = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the OPERANDS parameter is missing");
				}
			}

			/* Check for PORTS parameter */
			if (tokens[0].equals(ConstantFields.PORTS_PARAM)) {
				hasPorts = true;

				/* Check if value is missing */
				if (tokens[1].trim().equals("")) {
					throw new ConfigurationFileException("The value of the PORTS parameter is missing");
				}
			}
			/* Put the parameter in the hash table */
			parameters.put(tokens[0], tokens[1]);
		}

		/* If there is not some of the mandatory parameters */
		if (!hasPath) {
			throw new ConfigurationFileException("PATH parameter is missing");
		}

		if (!hasOperands) {
			throw new ConfigurationFileException("OPERANDS parameter is missing");
		}

		if (!hasPorts) {
			throw new ConfigurationFileException("PORTS parameter is missing");
		}
	}

	/**
	 * This method extracts the parameters from the hash table, after loading
	 * them
	 * 
	 * @throws ConfigurationFileException
	 */
	@Override
	protected void parseParameters() throws ConfigurationFileException {

		/* Concatenated string of all parameter operands in the Architecture */
		String allOperands = this.parameters.get(ConstantFields.OPERANDS_PARAM);
		/* Concatenated string of all ports in the architecture */
		String allPorts = this.parameters.get(ConstantFields.PORTS_PARAM);

		/* Parse the operands mapping */
		this.operandsMapping = this.parse(allOperands);

		/* Parse the ports mapping */
		this.portsMapping = this.parse(allPorts);
	}

	private Hashtable<String, Set<String>> parse(String concatenatedString) throws ConfigurationFileException {

		/* The resulting mapping */
		Hashtable<String, Set<String>> result = new Hashtable<String, Set<String>>();

		/* Split the string */
		String[] tokens = concatenatedString.split(",");

		/* Iterate the tokens */
		for (String token : tokens) {
			/* Split to sub tokens */
			String[] subtokens = token.substring(1, token.length() - 1).split(" ");

			if (subtokens.length < 2) {
				throw new ConfigurationFileException(
						"There should be at least two parameters to make the mapping in the Architecture Operands configuration file");
			}

			/* Create the key-value pair */
			String key = subtokens[0];
			Set<String> valueSet = new HashSet<String>();

			/* Iterate sub-tokens */
			for (int i = 1; i < subtokens.length; i++) {
				valueSet.add(subtokens[i]);
			}

			/* Make the mapping */
			result.put(key, valueSet);
		}

		return result;
	}

	@Override
	protected void validate() throws ComponentNotFoundException, ArchitectureExtractorException {
		validateOperands();
		validatePorts();
	}

	private void validateOperands() throws ComponentNotFoundException, ArchitectureExtractorException {
		/* Get the key set of operands mapping */
		Set<String> keySet = this.operandsMapping.keySet();

		/* Iterate key set */
		for (String key : keySet) {
			/* Get the value set */
			Set<String> valueSet = this.operandsMapping.get(key);

			/* Iterate value set */
			for (String value : valueSet) {
				if (!BIPChecker.componentExists(this.bipFileModel, value)) {
					throw new ComponentNotFoundException(
							"The component with name " + value + " does not exist in the BIP file");
				}
			}
		}
	}

	private void validatePorts() throws PortNotFoundException, ArchitectureExtractorException {
		/* Get the key set of the ports mapping */
		Set<String> keySet = this.portsMapping.keySet();

		/* Iterate key set */
		for (String key : keySet) {
			/* Get the value set */
			Set<String> valueSet = this.portsMapping.get(key);

			/* Iterate value set */
			for (String value : valueSet) {
				String[] tokens = value.split("\\.");
				String portName = tokens[1];
				String componentName = tokens[0];
				if (!BIPChecker.portExists(this.bipFileModel, portName, componentName)) {
					throw new PortNotFoundException("The port with name " + portName + " in the component with name "
							+ componentName + " does not exist");
				}
			}
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, for reading Architecture operands from
	 * configuration file
	 * 
	 * @param pathToConfFile
	 * @throws ConfigurationFileException
	 * @throws FileNotFoundException
	 * @throws ArchitectureExtractorException
	 * @throws ComponentNotFoundException
	 */
	public ArchitectureOperands(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException,
			ComponentNotFoundException, ArchitectureExtractorException {
		/* Read the parameters from the configuration file */
		this.readParameters(pathToConfFile);

		/* After reading the parameters, parse parameters */
		this.parseParameters();

		/* Parse the BIP file model */
		this.bipFileModel = new BIPFileModel(this.parameters.get(ConstantFields.PATH_PARAM));

		/* Validate architecture operands */
		this.validate();
	}

	/**
	 * @return the BIP file model for the Architecture Operands
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

	/**
	 * @return the parameters for the Architecture Operands
	 */
	public Hashtable<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @return the operands mapping
	 */
	public Hashtable<String, Set<String>> getOperandsMapping() {
		return operandsMapping;
	}

	/**
	 * @return the ports mapping
	 */
	public Hashtable<String, Set<String>> getPortsMapping() {
		return portsMapping;
	}

	/* Testing methods (passed) */
	public static void main(String[] args) {

		String path1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
		String path2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf-instance2.txt";
		String path3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf-instance12.txt";

		try {
			ArchitectureOperands architectureOperands = new ArchitectureOperands(path1);

			Hashtable<String, Set<String>> operandsMapping = architectureOperands.getOperandsMapping();
			Hashtable<String, Set<String>> portsMapping = architectureOperands.getPortsMapping();

			/* Get the operands key set */
			Set<String> operandsKeySet = operandsMapping.keySet();
			/* Iterate operands mapping */
			for (String key : operandsKeySet) {
				Set<String> value = operandsMapping.get(key);

				System.out.println("The operand with name " + key + " maps to:");
				/* Iterate value operands */
				for (String s : value) {
					System.out.println("\t" + s);
				}

			}

			/* Get the ports key set */
			Set<String> portsKeySet = portsMapping.keySet();
			/* Iterate ports mapping */
			for (String key : portsKeySet) {
				Set<String> value = portsMapping.get(key);

				System.out.println("The port with name " + key + " maps to:");
				/* Iterate value ports */
				for (String s : value) {
					System.out.println("\t" + s);
				}
			}

		} catch (ComponentNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigurationFileException e) {
			e.printStackTrace();
		} catch (ArchitectureExtractorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
