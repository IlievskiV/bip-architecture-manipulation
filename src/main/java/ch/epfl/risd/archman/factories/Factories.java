package ch.epfl.risd.archman.factories;

import ujf.verimag.bip.Core.ActionLanguage.Actions.ActionsFactory;
import ujf.verimag.bip.Core.ActionLanguage.Actions.impl.ActionsFactoryImpl;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.ExpressionsFactory;
import ujf.verimag.bip.Core.ActionLanguage.Expressions.impl.ExpressionsFactoryImpl;
import ujf.verimag.bip.Core.Behaviors.BehaviorsFactory;
import ujf.verimag.bip.Core.Behaviors.impl.BehaviorsFactoryImpl;
import ujf.verimag.bip.Core.Interactions.InteractionsFactory;
import ujf.verimag.bip.Core.Interactions.impl.InteractionsFactoryImpl;
import ujf.verimag.bip.Core.Modules.ModulesFactory;
import ujf.verimag.bip.Core.Modules.impl.ModulesFactoryImpl;
import ujf.verimag.bip.Core.PortExpressions.PortExpressionsFactory;
import ujf.verimag.bip.Core.PortExpressions.impl.PortExpressionsFactoryImpl;
import ujf.verimag.bip.Core.Priorities.PrioritiesFactory;
import ujf.verimag.bip.Core.Priorities.impl.PrioritiesFactoryImpl;

/**
 * This class will contain all factories for creating entities for a particular
 * BIP file.
 */
public final class Factories {

	/* Factory for creating a modules */
	public static final ModulesFactory MODULES_FACTORY = new ModulesFactoryImpl();

	/* Factory for creating a behaviors */
	public static final BehaviorsFactory BEHAVIORS_FACTORY = new BehaviorsFactoryImpl();

	/* Factory for creating an interactions */
	public static final InteractionsFactory INTERACTIONS_FACTORY = new InteractionsFactoryImpl();

	/* Factory for creating a priorities */
	public static final PrioritiesFactory PRIORITIES_FACTORY = new PrioritiesFactoryImpl();

	/* Factory for creating an actions */
	public static final ActionsFactory ACTIONS_FACTORY = new ActionsFactoryImpl();

	/* Factory for creating an expressions */
	public static final ExpressionsFactory EXPRESSIONS_FACTORY = new ExpressionsFactoryImpl();
	
	/* Factory for creating port expressions */
	public static final PortExpressionsFactory PORT_EXP_FACTORY = new PortExpressionsFactoryImpl();
}