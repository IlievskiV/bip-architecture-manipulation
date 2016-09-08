package ch.epfl.risd.archman.test;

import java.io.File;

import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.commandline.CmdLine;
import ch.epfl.risd.archman.exceptions.ListEmptyException;
import ch.epfl.risd.archman.exceptions.TestFailException;

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

	// Parallel Memory
	private static final String PAR_MEM_ARCH_STYLE_CONF = "/ParallelMem/ConfStyle.txt";
	private static final String PAR_MEM_ARCH_OP_CONF = "/ParallelMem/ConfOp.txt";

	// Action Flow
	private static final String ACT_FLOW_ARCH_STYLE_CONF = "/ActionFlow/ConfStyle.txt";
	private static final String ACT_FLOW_ARCH_OP_CONF = "/ActionFlow/ConfOp.txt";

	// Action Flow Abort
	private static final String ACT_FLOW_ABORT_ARCH_STYLE_CONF = "/ActionFlowAbort/ConfStyle.txt";
	private static final String ACT_FLOW_ABORT_ARCH_OP_CONF = "/ActionFlowAbort/ConfOp.txt";

	/* Output files */

	// Mutual Exclusion
	private static final String MUTEX_OUTPUT_BIP = "/Mutex/MutexInstance.bip";
	private static final String MUTEX_OUTPUT_CONF = "/Mutex/MutexConf.txt";

	// Modes 2
	private static final String MODES_OUTPUT_BIP = "/Modes2/Modes2Instance.bip";
	private static final String MODES_OUTPUT_CONF = "/Modes2/Modes2Conf.txt";

	// Action Sequence
	private static final String ACTION_SEQ_OUTPUT_BIP = "/ActionSequence/ActionSequenceInstance.bip";
	private static final String ACTION_SEQ_OUTPUT_CONF = "/ActionSequence/ActionSequenceConf.txt";

	// Parallel Memory
	private static final String PAR_MEM_OUTPUT_BIP = "/ParallelMem/SaveToMemInstance.bip";
	private static final String PAR_MEM_SEQ_OUTPUT_CONF = "/ParallelMem/SaveToMemConf.txt";

	// Action Flow
	private static final String ACT_FLOW_OUTPUT_BIP = "/ActionFlow/ActionFlowInstance.bip";
	private static final String ACT_FLOW_OUTPUT_CONF = "/ActionFlow/ActionFlowConf.txt";

	// Action Flow Abort
	private static final String ACT_FLOW_ABORT_OUTPUT_BIP = "/ActionFlowAbort/ActionFlowAbortInstance.bip";
	private static final String ACT_FLOW_ABORT_OUTPUT_CONF = "/ActionFlowAbort/ActionFlowAbortConf.bip";

	/* Flags */
	private static final String INSTANTIATION_FLAG = "-instantiation";
	private static final String TEST_FLAG = "-test";

	private static void testInstantiation(String archStyleConfFilePath, String archOpConfFilePath,
			String outputBipFilePath, String outputConfFilePath)
			throws Z3Exception, TestFailException, ListEmptyException {
		/* List of arguments */
		String[] args = new String[] { INSTANTIATION_FLAG, TEST_FLAG, archStyleConfFilePath, archOpConfFilePath,
				outputBipFilePath, outputConfFilePath };

		/* Call the command */
		CmdLine.main(args);
	}

	public static void testMutex() throws Z3Exception, TestFailException, ListEmptyException {
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

	public static void testModes2() throws Z3Exception, TestFailException, ListEmptyException {
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

	public static void testActionSequence() throws Z3Exception, TestFailException, ListEmptyException {
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

	public static void testParallelMem() throws Z3Exception, TestFailException, ListEmptyException {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + PAR_MEM_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + PAR_MEM_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + PAR_MEM_OUTPUT_BIP).getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + PAR_MEM_SEQ_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void testActionFlow() throws Z3Exception, ListEmptyException, TestFailException {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACT_FLOW_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACT_FLOW_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACT_FLOW_OUTPUT_BIP).getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACT_FLOW_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void testActionFlowAbort() throws Z3Exception, ListEmptyException, TestFailException {
		String archStyleConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACT_FLOW_ABORT_ARCH_STYLE_CONF).getAbsolutePath();

		String archOpConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + ACT_FLOW_ABORT_ARCH_OP_CONF).getAbsolutePath();

		String outputBipFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACT_FLOW_ABORT_OUTPUT_BIP).getAbsolutePath();

		String outputConfFilePath = new File(PARENT,
				BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES + ACT_FLOW_ABORT_OUTPUT_CONF).getAbsolutePath();

		TestInstantiation.testInstantiation(archStyleConfFilePath, archOpConfFilePath, outputBipFilePath,
				outputConfFilePath);
	}

	public static void main(String[] args) {

		try {
			// TestInstantiation.testMutex();
			// TestInstantiation.testModes2();
			// TestInstantiation.testActionSequence();
			// TestInstantiation.testParallelMem();
			// TestInstantiation.testActionFlow();

			TestInstantiation.testActionFlowAbort();
		} catch (Z3Exception | TestFailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ListEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
