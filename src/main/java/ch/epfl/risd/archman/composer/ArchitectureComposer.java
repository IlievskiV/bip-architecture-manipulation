package ch.epfl.risd.archman.composer;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.model.ArchitectureInstance;

public class ArchitectureComposer {

	/**
	 * Method for composing two Architecture Instances.
	 * 
	 * @param instance1
	 *            - the first architecture instance
	 * @param instance2
	 *            - the second architecture instance
	 * @return
	 */
	public static ArchitectureInstance compose(ArchitectureInstance instance1, ArchitectureInstance instance2) {

		/* The coordinators in the resulting instance */
		List<String> coordinators = new LinkedList<String>();
		coordinators.addAll(instance1.getCoordinators());
		coordinators.addAll(instance2.getCoordinators());

		/* The operands in the resulting instance */
		List<String> operands = new LinkedList<String>();
		operands.addAll(instance1.getOperands());
		operands.addAll(instance2.getOperands());

		/* The ports in the resulting instance */
		List<String> ports = new LinkedList<String>();
		ports.addAll(instance1.getPorts());
		ports.addAll(instance2.getPorts());

		/* Get characteristic predicates */
		String chPredicate1 = instance1.getCharacteristicPredicate();
		String chPredicate2 = instance2.getCharacteristicPredicate();

		System.out.println(chPredicate1);
		System.out.println(chPredicate2);

		/* Conjunction */
		String resultingPredicate = RuleSet.simplify(ExprParser.parse(chPredicate1 + "&" + chPredicate2)).toString();

		System.out.println(resultingPredicate);

		ArchitectureInstance res = new ArchitectureInstance("", "", coordinators, operands, ports, resultingPredicate);

		return null;
	}

	public static void main(String[] args) {

		try {
			ArchitectureInstance instance1 = new ArchitectureInstance("/home/vladimir/A12_conf.txt");
			ArchitectureInstance instance2 = new ArchitectureInstance("/home/vladimir/A13_conf.txt");

			ArchitectureComposer.compose(instance1, instance2);

		} catch (FileNotFoundException | ConfigurationFileException e) {
			e.printStackTrace();
		}

	}

}
