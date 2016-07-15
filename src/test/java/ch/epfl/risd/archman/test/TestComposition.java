package ch.epfl.risd.archman.test;

import java.io.File;

import ch.epfl.risd.archman.commandline.CmdLine;

/**
 * Class for testing the composition of Architecture Instances
 */
public class TestComposition {

	/* Parent path */
	private static final String PARENT = new File("").getAbsolutePath();

	/* Base Directory for the test cases */
	private static final String BASE_TEST_DIRECTORY = "/TestCases";
	/* Composition input files */
	private static final String COMPOSITION_INPUT_FILES = "/Composition/Input";
	/* Composition output files */
	private static final String COMPOSITION_OUTPUT_FILES = "/Composition/Output";

	/* Configuration file names */

	// Mutual Exclusion
	private static final String MUTEX12_CONF = "/Mutex/Conf12.txt";
	private static final String MUTEX13_CONF = "/Mutex/Conf13.txt";

	/* Output files */

	// Mutual Exclusion
	private static final String MUTEX_OUTPUT_BIP = "/Mutex/MutexInstance.bip";
	private static final String MUTEX_OUTPUT_CONF = "/Mutex/MutexConf.txt";

	/* Flags */
	private static final String COMPOSITION_FLAG = "-composition";
	private static final String TEST_FLAG = "-test";

	private static void testComposition(String archInst1ConfFilePath, String archInst2ConfFilePath,
			String outputBipFilePath, String outputConfFilePath) {
		/* List of arguments */
		String[] args = new String[] { COMPOSITION_FLAG, TEST_FLAG, archInst1ConfFilePath, archInst2ConfFilePath,
				outputBipFilePath, outputConfFilePath };

		/* Call the command */
		CmdLine.main(args);
	}

	public static void testMutex() {
		String archInst1ConfFilePath = new File(PARENT, BASE_TEST_DIRECTORY + COMPOSITION_INPUT_FILES + MUTEX12_CONF)
				.getAbsolutePath();

		String archInst2ConfFilePath = new File(PARENT, BASE_TEST_DIRECTORY + COMPOSITION_INPUT_FILES + MUTEX13_CONF)
				.getAbsolutePath();

		String outputBipFilePath = new File(PARENT, BASE_TEST_DIRECTORY + COMPOSITION_OUTPUT_FILES + MUTEX_OUTPUT_BIP)
				.getAbsolutePath();

		String outputConfFilePath = new File(PARENT, BASE_TEST_DIRECTORY + COMPOSITION_OUTPUT_FILES + MUTEX_OUTPUT_CONF)
				.getAbsolutePath();

		TestComposition.testComposition(archInst1ConfFilePath, archInst2ConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void main(String[] args) {
		TestComposition.testMutex();
	}
}
