package ch.epfl.risd.archman.extractor;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Interactions.Component;

public class ArchitectureOperandsExtractor {

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * This method returns all Operands in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @return the list of all Operands
	 * @throws ArchitectureExtractorException
	 */
	public static List<Component> getArchitectureOperands(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {
		/* The resulting list */
		List<Component> result = new LinkedList<Component>();

		/* Get all components */
		List<Component> allComponents = BIPExtractor.getAllComponents(architectureOperands.getBipFileModel());

		/* Get the key set for operands */
		Set<String> keySet = architectureOperands.getOperandsMapping().keySet();

		/* Iterate keys */
		for (String key : keySet) {
			/* Get the set of operands for the given key */
			Set<String> valueSet = architectureOperands.getOperandsMapping().get(key);

			System.out.println("Key: " + key);
			System.out.println("Mapping components:");
			for (String s : valueSet) {
				System.out.println("\t" + s);
			}

			/* Iterate components */
			for (Component c : allComponents) {
				System.out.println("Component name: " + c.getName());
				if (valueSet.contains(c.getName())) {
					result.add(c);
				}
			}
		}

		return result;
	}

	/**
	 * This method prints all Operands in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @throws ArchitectureExtractorException
	 */
	public static void printArchitectureOperands(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {
		/* Get the key set of the mapping operands */
		Set<String> keySet = architectureOperands.getOperandsMapping().keySet();

		StringBuilder sb = new StringBuilder();

		/* Iterate the key set */
		for (String key : keySet) {
			sb.append("The component with name: " + key + " is mapped to components: ");
			sb.append("\n");

			Set<String> valueSet = architectureOperands.getOperandsMapping().get(key);

			/* Iterate the value set */
			for (String value : valueSet) {
				sb.append("\t" + value);
				sb.append("\n");
			}

		}

		System.out.println(sb.toString());

	}

	/**
	 * This method returns the operands that should substitute the given
	 * parameter operand
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @param parameterOperand
	 *            - The name of the parameter operand mapped to the set of
	 *            operands
	 * @return the list of operands that can substitute the parameter operand
	 * @throws ArchitectureExtractorException
	 */
	public static List<Component> getSubstitutionOperands(ArchitectureOperands architectureOperands,
			String parameterOperand) throws ArchitectureExtractorException {
		List<Component> result = new LinkedList<Component>();

		/*
		 * Get the set of operands that should substitute the given parameter
		 * operand
		 */
		Set<String> operands = architectureOperands.getOperandsMapping().get(parameterOperand);

		/* Get all components in the Architecture Operands BIP file */
		List<Component> allComponents = BIPExtractor.getAllComponents(architectureOperands.getBipFileModel());

		/* Iterate all components */
		for (Component c : allComponents) {
			if (operands.contains(c.getName())) {
				result.add(c);
			}
		}

		return result;
	}

	/**
	 * This method returns all Ports in the given Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @return the list of all ports in the Architecture Operands
	 * @throws ArchitectureExtractorException
	 */
	public static List<Port> getArchitectureOperandsPorts(ArchitectureOperands architectureOperands)
			throws ArchitectureExtractorException {
		/* The resulting list */
		List<Port> result = new LinkedList<Port>();

		/* Get all ports */
		List<Port> allPorts = BIPExtractor.getAllPorts(architectureOperands.getBipFileModel());

		/* Get the key set for ports */
		Set<String> keySet = architectureOperands.getPortsMapping().keySet();

		/* Iterate keys */
		for (String key : keySet) {
			/* Get the value set for the give key */
			Set<String> valueSet = architectureOperands.getPortsMapping().get(key);

			/* Iterate ports */
			for (Port p : allPorts) {
				if (valueSet.contains(p.getName())) {
					result.add(p);
				}
			}
		}

		return result;
	}

	/**
	 * This method returns all ports that should substitute the given parameter
	 * port
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 * @param parameterPort-
	 *            the name of the parameter port mapped to the set of ports
	 * @return the list of ports that can substitute the parameter port
	 * @throws ArchitectureExtractorException
	 */
	public static List<Port> getSubstitutionPorts(ArchitectureOperands architectureOperands, String parameterPort)
			throws ArchitectureExtractorException {
		/* The resulting list */
		List<Port> result = new LinkedList<Port>();
		/* Get the set of ports that should substitute the given operand port */
		Set<String> ports = architectureOperands.getPortsMapping().get(parameterPort);
		/* Get all ports in the Architecture Operands */
		List<Port> allPorts = BIPExtractor.getAllPorts(architectureOperands.getBipFileModel());

		/* Iterate all ports */
		for (Port p : allPorts) {
			if (ports.contains(p.getName())) {
				result.add(p);
			}
		}

		return result;
	}

	/**
	 * This method prints all ports in the Architecture Operands
	 * 
	 * @param architectureOperands
	 *            - the given Architecture Operands object
	 */
	public void printPorts(ArchitectureOperands architectureOperands) {
		/* Get the key set */
		Set<String> keySet = architectureOperands.getPortsMapping().keySet();

		StringBuilder sb = new StringBuilder();

		/* Iterate the key set */
		for (String key : keySet) {
			sb.append("The port with name: " + key + " is mapped to ports: ");
			sb.append("\n");

			/* Get the value set */
			Set<String> valueSet = architectureOperands.getPortsMapping().get(key);
			/* Iterate the value set */
			for (String value : valueSet) {
				sb.append("\t" + value);
				sb.append("\n");
			}
		}

		System.out.println(sb.toString());
	}
}
