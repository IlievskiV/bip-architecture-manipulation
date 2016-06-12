package ch.epfl.risd.archman.composer;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.model.ArchitectureInstance;

public class ArchitectureComposer {

	public static ArchitectureInstance compose(ArchitectureInstance instance1, ArchitectureInstance instance2) {

		/* The coordinators in the resulting instance */
		List<String> coordinators = new LinkedList<String>();
		coordinators.addAll(instance1.getCoordinators());
		coordinators.addAll(instance2.getCoordinators());

		/* The operands in the resulting instance */
		List<String> operands = new LinkedList<String>();
		operands.addAll(instance1.getOperands());
		operands.addAll(instance2.getOperands());

		/* Get characteristic predicates */
		String chPredicate1 = instance1.getCharacteristicPredicate();
		String chPredicate2 = instance2.getCharacteristicPredicate();

		/* Conjunction */
		Expression<String> expr = ExprParser.parse("(" + chPredicate1 + ") & (" + chPredicate2 + ")");
		/* Simplify the expression */
		Expression<String> simplified = RuleSet.simplify(expr);

		String simplifiedToString = simplified.toString();

		System.out.println(simplifiedToString);

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
