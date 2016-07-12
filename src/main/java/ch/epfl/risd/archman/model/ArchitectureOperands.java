package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.helper.HelperMethods;

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

	/**
	 * This method extracts the parameters from the hash table, after loading
	 * them
	 * 
	 * @throws ConfigurationFileException
	 */
	@Override
	protected void parseParameters() throws ConfigurationFileException {

		String delim1 = ",";
		String delim2 = " ";

		/* Parse the operands mapping */
		this.operandsMapping = this.parseMappings(
				this.confFileModel.getParameters().get(ConstantFields.OPERANDS_MAPPING_PARAM), delim1, delim2);

		/* Parse the ports mapping */
		this.portsMapping = this.parseMappings(
				this.confFileModel.getParameters().get(ConstantFields.PORTS_MAPPING_PARAM), delim1, delim2);
	}

	private Hashtable<String, Set<String>> parseMappings(String concatenatedString, String delim1, String delim2)
			throws ConfigurationFileException {

		/* The resulting mapping */
		Hashtable<String, Set<String>> result = new Hashtable<String, Set<String>>();

		/* Split the string */
		List<String[]> tokens = HelperMethods.splitConcatenatedString(concatenatedString, delim1, delim2);

		/* Iterate the tokens */
		for (String[] subTokens : tokens) {

			if (subTokens.length < 2) {
				throw new ConfigurationFileException(
						"There should be at least two parameters to make the mapping in the Architecture Operands configuration file");
			}

			/* Create the key-value pair */
			String key = subTokens[0];
			Set<String> valueSet = new HashSet<String>();

			/* Iterate sub-tokens */
			for (int i = 1; i < subTokens.length; i++) {
				valueSet.add(subTokens[i]);
			}

			/* Make the mapping */
			result.put(key, valueSet);
		}

		return result;
	}

	@Override
	protected void validate() throws ComponentNotFoundException, ArchitectureExtractorException {
		/* Validate operands */
		for (String key : this.operandsMapping.keySet()) {
			this.validateComponents(this.operandsMapping.get(key));
		}

		/* Validate ports */
		for (String key : this.portsMapping.keySet()) {
			this.validatePorts(this.portsMapping.get(key));
		}

	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class, for reading Architecture Operands from
	 * configuration file.
	 * 
	 * @param pathToConfFile
	 *            - absolute path to the configuration file
	 * @throws ConfigurationFileException
	 * @throws FileNotFoundException
	 * @throws ArchitectureExtractorException
	 * @throws ComponentNotFoundException
	 */
	public ArchitectureOperands(String pathToConfFile) throws FileNotFoundException, ConfigurationFileException,
			ComponentNotFoundException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(pathToConfFile, ConstantFields.architectureOperandsRequiredParams);

		/* Validate architecture operands */
		this.validate();
	}

	public ArchitectureOperands(String prefixToBip, String pathToConfFile) throws FileNotFoundException,
			ConfigurationFileException, ComponentNotFoundException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(prefixToBip, pathToConfFile, ConstantFields.architectureOperandsRequiredParams);

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
		return this.confFileModel.getParameters();
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
