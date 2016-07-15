package ch.epfl.risd.archman.test;

import java.io.File;

import ch.epfl.risd.archman.commandline.CmdLine;

/**
 * Class for testing the Architecture Instantiation
 */
public class TestInstantiation {

	/* Base Directory for the test cases */
	private static final String BASE_TEST_DIRECTORY = "/TestCases";
	/* Instantiation input files */
	private static final String INSTANTIATION_INPUT_FILES = "/Instantiation/Input";
	/* Instantiation output files */
	private static final String INSTANTIATION_OUTPUT_FILES = "/Instantiation/Output";

	/* Parent path */
	private static final String PARENT = new File("").getAbsolutePath();

	/* Configuration file names */

	// Mutual Exclusion
	private static final String MUTEX_ARCH_STYLE_CONF = "/Mutex/AEConf.txt";
	private static final String MUTEX_ARCH_OP_CONF = "/Mutex/AEConf-instance2.txt";

	// Modes 2
	private static final String MODES_ARCH_STYLE_CONF = "/Modes2/AEConf.txt";
	private static final String MODES_ARCH_OP_CONF = "/Modes2/AEConf-instance2.txt";

	// Action Sequence
	private static final String ACTION_SEQ_ARCH_STYLE_CONF = "/ActionSequence/AEConf.txt";
	private static final String ACTION_SEQ_ARCH_OP_CONF = "/ActionSequence/AEConf-instance2.txt";

	/* Output files */

	// Mutual Exclusion
	private static final String MUTEX_OUTPUT_BIP = "/Mutex/MutexInstance.bip";
	private static final String MUTEX_OUTPUT_CONF = "/Mutex/MutexConf.txt";

	// Modes 2
	private static final String MODES_OUTPUT_BIP = "/Modes2/Modes2Instance.bip";
	private static final String MODES_OUTPUT_CONF = "/Modes2/Modes2Conf.txt";

	// Modes 2
	private static final String ACTION_SEQ_OUTPUT_BIP = "/ActionSequence/Modes2Instance.bip";
	private static final String ACTION_SEQ_OUTPUT_CONF = "/ActionSequence/Modes2Conf.txt";

	/* Flags */
	private static final String INSTANTIATION_FLAG = "-instantiation";
	private static final String TEST_FLAG = "-test";

	private static void testInstantiation(String archStyleConfFilePath, String archOpConfFilePath,
			String outputBipFilePath, String outputConfFilePath) {
		/* List of arguments */
		String[] args = new String[] { INSTANTIATION_FLAG, TEST_FLAG, archStyleConfFilePath, archOpConfFilePath,
				outputBipFilePath, outputConfFilePath };

		/* Call the command */
		CmdLine.main(args);
	}

	public static void testMutex() {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + MUTEX_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + MUTEX_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT, BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + MUTEX_OUTPUT_BIP)
				.getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + MUTEX_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void testModes2() {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + MODES_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + MODES_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT, BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + MODES_OUTPUT_BIP)
				.getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + MODES_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void testActionSequence() {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACTION_SEQ_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACTION_SEQ_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACTION_SEQ_OUTPUT_BIP).getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACTION_SEQ_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void main(String[] args) {
		TestInstantiation.testMutex();
		TestInstantiation.testModes2();
		TestInstantiation.testActionSequence();
	}

}
