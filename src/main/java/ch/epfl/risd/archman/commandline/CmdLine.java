package ch.epfl.risd.archman.commandline;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.microsoft.z3.Z3Exception;

import ch.epfl.risd.archman.builder.ArchitectureInstantiator;
import ch.epfl.risd.archman.composer.ArchitectureComposer;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.TestFailException;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;

/**
 * The main command line interface for the BIP Architecture Manipulation tool
 * 
 * @author Vladimir Ilievski, RiSD@EPFL
 */
public class CmdLine {

	/* Logger for showing the logs */
	private static final Logger logger = Logger.getLogger(CmdLine.class);

	/* An error prefix for printing purposes */
	private static final String ERROR_PREFIX = "BIP-AM::ERROR";

	public static void main(String[] args) throws Z3Exception, TestFailException {
		/* Pass the arguments */
		CmdLineFactory cmdLineFactory = new CmdLineFactory(args);

		/* Get the parsed paths */
		String conf1Path = cmdLineFactory.getConfFile1();
		String conf2Path = cmdLineFactory.getConfFile2();
		String outputBIPPath = cmdLineFactory.getOutputBIP();
		String outputConf = cmdLineFactory.getOutputConf();

		/* If instantiation as a choice */
		if (cmdLineFactory.getInstantiation() && !cmdLineFactory.getComposition()) {

			ArchitectureStyle architectureStyle;
			ArchitectureOperands architectureOperands;

			try {

				/* If not in testing mode */
				if (!cmdLineFactory.getTesting()) {
					architectureStyle = new ArchitectureStyle(conf1Path);
					architectureOperands = new ArchitectureOperands(conf2Path);
				}
				/* If in testing mode */
				else {
					String prefix = new File("").getAbsolutePath();
					architectureStyle = new ArchitectureStyle(prefix, conf1Path);
					architectureOperands = new ArchitectureOperands(prefix, conf2Path);
				}

				String systemName = architectureStyle.getBipFileModel().getSystem().getName();
				String rootTypeName = architectureStyle.getBipFileModel().getRootType().getName();
				String rootInstanceName = architectureStyle.getBipFileModel().getRoot().getName();

				/* Instantiate */
				ArchitectureInstantiator.createArchitectureInstance(architectureStyle, architectureOperands, systemName,
						rootTypeName, rootInstanceName, outputBIPPath, outputConf);

			} catch (ConfigurationFileException | ArchitectureExtractorException | ArchitectureBuilderException
					| IOException | InterruptedException e) {
				System.out.println(ERROR_PREFIX + " : " + e.getMessage());
				System.exit(0);
			}

		}
		/* If composition as a choice */
		else if (!cmdLineFactory.getInstantiation() && cmdLineFactory.getComposition()) {
			ArchitectureInstance instance1;
			ArchitectureInstance instance2;

			try {

				/* If not in testing mode */
				if (!cmdLineFactory.getTesting()) {
					instance1 = new ArchitectureInstance(conf1Path, true);
					instance2 = new ArchitectureInstance(conf2Path, true);
				}
				/* If in testing mode */
				else {
					String prefix = new File("").getAbsolutePath();
					instance1 = new ArchitectureInstance(prefix, conf1Path, true);
					instance2 = new ArchitectureInstance(prefix, conf2Path, true);
				}

				String systemName = instance1.getBipFileModel().getSystem().getName() + "_"
						+ instance2.getBipFileModel().getSystem().getName() + "_Composed";
				String rootTypeName = instance1.getBipFileModel().getRootType().getName() + "_"
						+ instance2.getBipFileModel().getRootType().getName() + "_Composed";
				String rootInstanceName = instance1.getBipFileModel().getRoot().getName() + "_"
						+ instance2.getBipFileModel().getRootType().getName() + "_Composed";

				ArchitectureComposer.compose(instance1, instance2, systemName, rootTypeName, rootInstanceName,
						outputBIPPath, outputConf);

			} catch (ConfigurationFileException | ArchitectureExtractorException | InvalidComponentNameException
					| InvalidConnectorTypeNameException | InvalidPortParameterNameException
					| IllegalPortParameterReferenceException | IOException | InterruptedException e) {
				System.out.println(ERROR_PREFIX + " : " + e.getMessage());
				System.exit(0);
			}

		} else {
			/* If both of them true or false */
			System.out.println(
					ERROR_PREFIX + " : The instantiation and composition flags can't be both true or both false");
			System.exit(0);
		}
	}
}
