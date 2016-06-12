package ch.epfl.risd.archman.extractor;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Interactions.Component;

public class ArchitectureOperandsExtractorImpl extends ExtractorImpl implements ArchitectureOperandsExtractor {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The mapping of the parameter operand */
	private Hashtable<String, Set<String>> operandsMapping;

	/* The mapping of the parameter port */
	private Hashtable<String, Set<String>> portsMapping;

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * 
	 * @param architectureOperands
	 */
	public ArchitectureOperandsExtractorImpl(ArchitectureOperands architectureOperands) {
		super(architectureOperands.getBipFileModel());

		this.operandsMapping = architectureOperands.getOperandsMapping();
		this.portsMapping = architectureOperands.getPortsMapping();
	}

	@Override
	public List<Component> getArchitectureOperands() throws ArchitectureExtractorException {
		/* The resulting list */
		List<Component> result = new LinkedList<Component>();

		/* Get all components */
		List<Component> allComponents = this.getAllComponents();

		/* Get the key set for operands */
		Set<String> keySet = operandsMapping.keySet();

		/* Iterate keys */
		for (String key : keySet) {
			/* Get the set of operands for the given key */
			Set<String> valueSet = operandsMapping.get(key);

			/* Iterate components */
			for (Component c : allComponents) {
				if (valueSet.contains(c.getName())) {
					result.add(c);
				}
			}
		}

		return result;
	}

	@Override
	public void printArchitectureOperands() throws ArchitectureExtractorException {
		/* Get the key set of the mapping operands */
		Set<String> keySet = this.operandsMapping.keySet();

		StringBuilder sb = new StringBuilder();

		/* Iterate the key set */
		for (String key : keySet) {
			sb.append("The component with name: " + key + " is mapped to components: ");
			sb.append("\n");

			Set<String> valueSet = this.operandsMapping.get(key);

			/* Iterate the value set */
			for (String value : valueSet) {
				sb.append("\t" + value);
				sb.append("\n");
			}

		}

		System.out.println(sb.toString());

	}

	@Override
	public List<Component> getSubstitutionOperands(String parameterOperand) throws ArchitectureExtractorException {
		List<Component> result = new LinkedList<Component>();

		/*
		 * Get the set of operands that should substitute the given parameter
		 * operand
		 */
		Set<String> operands = this.operandsMapping.get(parameterOperand);

		/* Get all components in the Architecture Operands */
		List<Component> allComponents = this.getAllComponents();

		/* Iterate all components */
		for (Component c : allComponents) {
			if (operands.contains(c.getName())) {
				result.add(c);
			}
		}

		return result;
	}

	@Override
	public List<Port> getArchitectureOperandsPorts() throws ArchitectureExtractorException {
		/* The resulting list */
		List<Port> result = new LinkedList<Port>();

		/* Get all ports */
		List<Port> allPorts = this.getAllPorts();

		/* Get the key set for ports */
		Set<String> keySet = this.portsMapping.keySet();

		/* Iterate keys */
		for (String key : keySet) {
			/* Get the value set for the give key */
			Set<String> valueSet = portsMapping.get(key);

			/* Iterate ports */
			for (Port p : allPorts) {
				if (valueSet.contains(p.getName())) {
					result.add(p);
				}
			}
		}

		return result;
	}

	@Override
	public List<Port> getSubstitutionPorts(String parameterPort) throws ArchitectureExtractorException {
		/* The resulting list */
		List<Port> result = new LinkedList<Port>();
		/* Get the set of ports that should substitute the given operand port */
		Set<String> ports = this.portsMapping.get(parameterPort);
		/* Get all ports in the Architecture Operands */
		List<Port> allPorts = this.getAllPorts();

		/* Iterate all ports */
		for (Port p : allPorts) {
			if (ports.contains(p.getName())) {
				result.add(p);
			}
		}

		return result;
	}

	@Override
	public void printPorts() {
		/* Get the key set */
		Set<String> keySet = this.portsMapping.keySet();

		StringBuilder sb = new StringBuilder();

		/* Iterate the key set */
		for (String key : keySet) {
			sb.append("The port with name: " + key + " is mapped to ports: ");
			sb.append("\n");

			/* Get the value set */
			Set<String> valueSet = this.portsMapping.get(key);
			/* Iterate the value set */
			for (String value : valueSet) {
				sb.append("\t" + value);
				sb.append("\n");
			}
		}

		System.out.println(sb.toString());
	}
}
