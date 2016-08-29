package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ComponentNotFoundException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.helper.HelperMethods;

/**
 * This class contains the operands of the architecture, i.e. the operands that
 * have to be substituted, in order to have one instance of the architecture
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class ArchitectureOperands extends ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/*
	 * Which parameter operand from the Architecture Style maps to which set of
	 * operands
	 */
	private Map<String, Set<String>> operandsMapping;

	/* Which port in one parameter operand maps to which set of ports */
	private List<PortMapping> portsMapping;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method parses operand mappings and port mappings
	 * 
	 * @throws ConfigurationFileException
	 */
	@Override
	protected void parseParameters() throws ConfigurationFileException {

		String delim1 = ",";
		String delim2 = " ";

		/* Parse the operands mapping */
		this.operandsMapping = this.parseOperandMappings(
				this.archEntityConfigFile.getParameters().get(ConstantFields.OPERANDS_MAPPING_PARAM), delim1, delim2);

		/* Parse the ports mapping */
		this.portsMapping = this.parsePortMappings(
				this.archEntityConfigFile.getParameters().get(ConstantFields.PORTS_MAPPING_PARAM), delim1, delim2);
	}

	/**
	 * This method parses operand mappings.
	 * 
	 * @param concatenatedString
	 *            - the string representing mapping of the operands
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return the mapping of operands
	 * @throws ConfigurationFileException
	 */
	private Map<String, Set<String>> parseOperandMappings(String concatenatedString, String delim1, String delim2)
			throws ConfigurationFileException {

		/* The resulting mapping */
		Map<String, Set<String>> result = new Hashtable<String, Set<String>>();

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

	/**
	 * This method parses operand ports mappings.
	 * 
	 * @param concatenatedString
	 *            - the string representing mapping of the operand ports
	 * @param delim1
	 *            - external delimiter
	 * @param delim2
	 *            - internal delimiter
	 * @return the list of mapped ports
	 * @throws ConfigurationFileException
	 */
	private List<PortMapping> parsePortMappings(String concatenatedString, String delim1, String delim2)
			throws ConfigurationFileException {
		/* The resulting list of mappings */
		List<PortMapping> result = new LinkedList<PortMapping>();

		/* Split the string */
		List<String[]> tokens = HelperMethods.splitConcatenatedString(concatenatedString, delim1, delim2);

		/* Iterate the tokens */
		for (String[] subTokens : tokens) {
			if (subTokens.length < 2) {
				throw new ConfigurationFileException(
						"There should be at least two parameters to make the mapping in the Architecture Operands configuration file");
			}

			/* Create the key-value pair */
			String portToMap = subTokens[0];
			Set<String> mappedPorts = new HashSet<String>();

			/* Iterate sub-tokens */
			for (int i = 1; i < subTokens.length; i++) {
				mappedPorts.add(subTokens[i]);
			}

			result.add(new PortMapping(portToMap, mappedPorts));
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
		for (PortMapping portMapping : this.portsMapping) {
			this.validatePorts(portMapping.getMappedPorts());
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
	}

	public ArchitectureOperands(String prefixToBip, String pathToConfFile) throws FileNotFoundException,
			ConfigurationFileException, ComponentNotFoundException, ArchitectureExtractorException {
		/* Call the super class constructor */
		super(prefixToBip, pathToConfFile, ConstantFields.architectureOperandsRequiredParams);
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
		return this.archEntityConfigFile.getParameters();
	}

	/**
	 * @return the operands mapping
	 */
	public Map<String, Set<String>> getOperandsMapping() {
		return operandsMapping;
	}

	/**
	 * @return the ports mapping
	 */
	public List<PortMapping> getPortsMapping() {
		return portsMapping;
	}

	/* Testing methods (passed) */
	public static void main(String[] args) {

		String path1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
		String path2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf-instance2.txt";
		String path3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf-instance12.txt";

		try {
			ArchitectureOperands architectureOperands = new ArchitectureOperands(path1);

			Map<String, Set<String>> operandsMapping = architectureOperands.getOperandsMapping();
			List<PortMapping> portsMapping = architectureOperands.getPortsMapping();

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

			/* Iterate ports mapping */
			for (PortMapping portMapping : portsMapping) {
				Set<String> mappedPorts = portMapping.getMappedPorts();

				System.out.println("The port with name " + portMapping.getPortToMap() + " maps to:");
				/* Iterate value ports */
				for (String s : mappedPorts) {
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
