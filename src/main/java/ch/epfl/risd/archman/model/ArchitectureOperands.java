package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.util.HashMap;
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

	/* Mapping of the operands */
	protected Map<String, ComponentMapping> operandsMapping;

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
		/* Splitting delimiters */
		String delim1 = ",";
		String delim2 = " ";
		String delim3 = ";";

		/* Get all operands mappings */
		this.operandsMapping = this.parseOperandMappings(
				this.archEntityConfigFile.getParameters().get(ConstantFields.OPERANDS_MAPPING_PARAM),
				this.archEntityConfigFile.getParameters().get(ConstantFields.PORTS_MAPPING_PARAM), delim1, delim2,
				delim3);

	}

	/**
	 * Method to parse operand mappings (not very understandable code)
	 * 
	 * @param operandsMappingStr
	 * @param portsMappingStr
	 * @param delim1
	 * @param delim2
	 * @param delim3
	 * @return
	 * @throws ConfigurationFileException
	 */
	private Map<String, ComponentMapping> parseOperandMappings(String operandsMappingStr, String portsMappingStr,
			String delim1, String delim2, String delim3) throws ConfigurationFileException {
		/* Initialize the result */
		Map<String, ComponentMapping> result = new HashMap<String, ComponentMapping>();

		/* Split the string of operand mappings */
		List<String[]> operandTokens = HelperMethods.splitConcatenatedString(operandsMappingStr, delim1, delim2);

		/* Split the string of port mappings */
		List<String[]> portTokens = HelperMethods.splitConcatenatedString(portsMappingStr, delim1, delim2);

		/*
		 * Mappings of the operand components. The key is the name of the
		 * operand to be mapped. The value is the set of mapped components
		 */
		Map<String, Set<String>> componentMappings = new HashMap<String, Set<String>>();
		for (String[] tokens : operandTokens) {
			String operandToMap = tokens[0];
			Set<String> mappedOperands = new HashSet<String>();
			for (int i = 1; i < tokens.length; i++) {
				mappedOperands.add(tokens[i]);
			}

			componentMappings.put(operandToMap, mappedOperands);
		}

		/*
		 * This map shows which ports in one operand have to be mapped. The key
		 * is the name of the operand
		 */
		Map<String, List<String>> portsToMap = new HashMap<String, List<String>>();

		/*
		 * This map shows, each of the ports to be mapped in each component, to
		 * which set of ports will be mapped.
		 */
		Map<String, List<List<Set<String>>>> mappedPorts = new HashMap<String, List<List<Set<String>>>>();
		for (String[] tokens : portTokens) {
			String portToMap = tokens[0];
			String operandName = portToMap.split("\\.")[0];

			/* Build first map */
			List<String> tempList1;
			/* Create new list, or add to existing */
			if (!portsToMap.containsKey(operandName)) {
				tempList1 = new LinkedList<String>();
			} else {
				tempList1 = portsToMap.get(operandName);
			}

			tempList1.add(portToMap);
			portsToMap.put(operandName, tempList1);

			/* build second map */
			List<List<Set<String>>> tempList2;

			if (!mappedPorts.containsKey(operandName)) {
				tempList2 = new LinkedList<List<Set<String>>>();
			} else {
				tempList2 = mappedPorts.get(operandName);
			}

			List<Set<String>> tempList3 = new LinkedList<Set<String>>();
			for (int i = 1; i < tokens.length; i++) {
				Set<String> tempSet = new HashSet<String>();
				/* Remove brackets */
				String token = tokens[i].substring(1, tokens[i].length() - 1);
				String[] subTokens = token.split(delim3);
				for (int j = 0; j < subTokens.length; j++) {
					tempSet.add(subTokens[j]);
				}
				tempList3.add(tempSet);
			}

			tempList2.add(tempList3);
			mappedPorts.put(operandName, tempList2);
		}

		Set<String> keys = componentMappings.keySet();
		for (String key : keys) {
			result.put(key,
					new ComponentMapping(key, componentMappings.get(key), portsToMap.get(key), mappedPorts.get(key)));
		}

		return result;

	}

	@Override
	protected void validate() throws ComponentNotFoundException, ArchitectureExtractorException {

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

	public Map<String, ComponentMapping> getOperandsMapping() {
		return operandsMapping;
	}

	/* Testing methods (passed) */
	public static void main(String[] args) {

		String path1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
		String path2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf-instance2.txt";
		String path3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf-instance12.txt";

		try {
			ArchitectureOperands architectureOperands = new ArchitectureOperands(path1);

			for (String key1 : architectureOperands.operandsMapping.keySet()) {
				ComponentMapping cm = architectureOperands.operandsMapping.get(key1);
				System.out.println("The component to map is: " + cm.componentToMap);
				System.out.println("\t With cardinality name: " + cm.cardinalityTerm.name + " and cardinality value: "
						+ cm.cardinalityTerm.value);
				System.out.println("\t The mapping components are: ");
				for (String s : cm.mappedComponents) {
					System.out.println("\t\t " + s);
				}

				System.out.println("\t The mapping of ports: ");
				for (String key2 : cm.portMappings.keySet()) {
					PortMapping pm = cm.portMappings.get(key2);
					System.out.println("\t\t The name of the port to be mapped: " + pm.portToMap);
					System.out.println("\t\t The mapping ports are: ");
					for (int i = 0; i < pm.mappedPorts.size(); i++) {
						StringBuilder sb = new StringBuilder();
						for (String s : pm.mappedPorts.get(i)) {
							sb.append(s).append(" ");
						}
						System.out.println(
								"\t\t\t " + sb.toString() + " with cardinality name: " + pm.cardinalityTerms.get(i).name
										+ " and cardinality value: " + pm.cardinalityTerms.get(i).value);

					}
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
