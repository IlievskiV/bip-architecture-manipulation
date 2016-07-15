package ch.epfl.risd.archman.commandline;

import jcmdline.BooleanParam;
import jcmdline.CmdLineException;
import jcmdline.CmdLineHandler;
import jcmdline.FileParam;
import jcmdline.HelpCmdLineHandler;
import jcmdline.Parameter;
import jcmdline.VersionCmdLineHandler;

/**
 * A factory for command line input
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class CmdLineFactory {

	/* Helping and description definitions */

	private static final String helpInstantiation = "Instantiation of an Architecture";

	private static final String helpComposition = "Composition of Architectures";

	private static final String helpTesting = "Test mode within the project";

	private static final String helpConfFile1 = "Path to the first Configuration File. In case of instantiation it is the path to the Architecture Style configuration file";

	private static final String helpConfFile2 = "Path to the second Configuration File. In case of instantiation it is the path to the Architecture Operands configuration file";

	private static final String helpBipOutput = "Path to BIP Output file";

	private static final String helpConfOutput = "Path to Configuration Output file";
	/* Tool specific description */
	private static final String helpText = "Architecture Manipulation Tool";
	private static final String cmdLineTool = "java -jar /target/bip-am.jar";
	private static final String cmdLineDescription = "Tool for architecture manipulation of BIP models";

	/* Instantiation Flag Parameter */
	private BooleanParam instantiation;

	/* Composition Flag Parameter */
	private BooleanParam composition;

	/* Invisible flag for testing */
	private BooleanParam testing;

	/* First Configuration File */
	private FileParam confFile1;

	/* Second Configuration File */
	private FileParam confFile2;

	/* Output BIP File */
	private FileParam outputBIP;

	/* Output Configuration File */
	private FileParam outputConf;

	/* Command Line Handler */
	private CmdLineHandler cmdLineHandler;

	/**
	 * Constructor for this class.
	 * 
	 * @param args
	 *            - array of parameters to parse
	 */
	public CmdLineFactory(String[] args) {
		/* Initialize File Parameters */
		this.confFile1 = new FileParam("input_conf1", helpConfFile1, FileParam.EXISTS & FileParam.IS_READABLE,
				!FileParam.OPTIONAL, !FileParam.MULTI_VALUED);

		this.confFile2 = new FileParam("input_conf2", helpConfFile2, FileParam.EXISTS & FileParam.IS_READABLE,
				!FileParam.OPTIONAL, !FileParam.MULTI_VALUED);

		this.outputBIP = new FileParam("output_bip", helpBipOutput, FileParam.NO_ATTRIBUTES, !FileParam.OPTIONAL,
				!FileParam.MULTI_VALUED);

		this.outputConf = new FileParam("output_conf", helpConfOutput, FileParam.NO_ATTRIBUTES, !FileParam.OPTIONAL,
				!FileParam.MULTI_VALUED);

		/* Initialize Boolean Parameters */
		this.instantiation = new BooleanParam("instantiation", helpInstantiation);
		this.composition = new BooleanParam("composition", helpComposition);
		this.testing = new BooleanParam("test", helpTesting);
		this.testing.setHidden(true);

		/* Set initial values */
		try {
			instantiation.setValue(false);
			composition.setValue(false);
			testing.setValue(false);
		} catch (CmdLineException e) {
			e.printStackTrace();
			System.err.println("ERROR while initializing! System will now exit...");
			System.exit(0);
		}

		this.cmdLineHandler = new VersionCmdLineHandler("V 1.0",
				(CmdLineHandler) new HelpCmdLineHandler(helpText, cmdLineTool, cmdLineDescription,
						new Parameter[] { this.instantiation, this.composition, this.testing },
						new Parameter[] { this.confFile1, this.confFile2, this.outputBIP, this.outputConf }));

		this.cmdLineHandler.parse(args);
	}

	/* Getters for the parameters at the command line interface */

	public boolean getInstantiation() {
		return instantiation.getValue();
	}

	public boolean getComposition() {
		return composition.getValue();
	}

	public boolean getTesting() {
		return testing.getValue();
	}

	public String getConfFile1() {
		return confFile1.getValue().getAbsolutePath();
	}

	public String getConfFile2() {
		return confFile2.getValue().getAbsolutePath();
	}

	public String getOutputBIP() {
		return outputBIP.getValue().getAbsolutePath();
	}

	public String getOutputConf() {
		return outputConf.getValue().getAbsolutePath();
	}
}
