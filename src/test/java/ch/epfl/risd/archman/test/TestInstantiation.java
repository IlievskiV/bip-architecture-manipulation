package ch.epfl.risd.archman.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Scanner;

import ch.epfl.risd.archman.builder.ArchitectureInstantiator;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.TestConfigurationFileException;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.BIPFileModel;

/**
 * Class for testing the Architecture Instantiation
 */
public class TestInstantiation {

	/* Base Directory for the test cases */
	public static final String BASE_TEST_DIRECTORY = "/TestCases";
	/* Instantiation configuration files directory */
	public static final String INSTANTIATION_CONF_FILES = "/Instantiation/TestConfFiles/";
	/* Instantiation input files */
	public static final String INSTANTIATION_INPUT_FILES = "/Instantiation/Input";
	/* Instantiation output files */
	public static final String INSTANTIATION_OUTPUT_FILES = "/Instantiation/Output";

	/* Configuration file names */
	public static final String MUTEX_TEST_CONF_FILE = "TestConfiguration_Mutex.txt";
	public static final String MODES_TEST_CONF_FILE = "TestConfiguration_Modes2.txt";
	public static final String ACTION_SEQUENCE_TEST_CONF_FILE = "TestConfiguration_ActionSequqnce.txt";

	/* General method for testing */
	public static void testInstantiation(String testConFileName)
			throws URISyntaxException, TestConfigurationFileException, ConfigurationFileException,
			ArchitectureExtractorException, ArchitectureBuilderException, IOException {
		/* Get the absolute path to the test configuration file */
		URL testConFileRes = TestInstantiation.class.getClass()
				.getResource(BASE_TEST_DIRECTORY + INSTANTIATION_CONF_FILES + testConFileName);
		String testConfFilePath = Paths.get(testConFileRes.toURI()).toString();

		/* Get the prefix */
		URL prefixRes = TestInstantiation.class.getClass().getResource(BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES);
		String prefix = Paths.get(prefixRes.toURI()).toString();

		/* Declare scanner */
		Scanner scanner = null;

		/*
		 * Set to null the three required entities to instantiate architecture
		 */
		ArchitectureStyle architectureStyle = null;
		ArchitectureOperands architectureOperands = null;
		String outputFolderPath = null;

		/* Existence of the ARCH_STYLE_CONF_FILE_PATH parameter */
		boolean hasArchStyleConfFilePath = false;

		/* Existence of the ARCH_OP_CONF_FILE_PATH parameter */
		boolean hasArchOpConfFilePath = false;

		/* Existence of the OUTPUT_FOLDER_PATH parameter */
		boolean hasOutputFolderPath = false;

		try {
			/* Initialize the scanner */
			scanner = new Scanner(new File(testConfFilePath));

			while (scanner.hasNext()) {
				/* Take the current line and split it where the semicolon is */
				String[] tokens = scanner.nextLine().split(":");

				/* No more than one colon in a line exception */
				if (tokens.length > 2) {
					throw new TestConfigurationFileException("More than one colon (:) in the line");
				}
				/* Check for ARCH_STYLE_CONF_FILE_PATH parameter */
				if (tokens[0].equals(ConstantFields.ARCH_STYLE_CONF_FILE_PATH_PARAM)) {
					hasArchStyleConfFilePath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the ARCH_STYLE_CONF_FILE_PATH parameter is missing");
					} else {
						/* Get the absolute path */
						URL archStyleConfFileRes = TestInstantiation.class.getClass()
								.getResource(BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + tokens[1]);
						String archStyleConfFilePath = Paths.get(archStyleConfFileRes.toURI()).toString();

						/* Instantiate the architecture style */
						architectureStyle = new ArchitectureStyle(prefix, archStyleConfFilePath);
					}
				}

				/* Check for ARCH_OP_CONF_FILE_PATH_PARAM parameter */
				if (tokens[0].equals(ConstantFields.ARCH_OP_CONF_FILE_PATH_PARAM)) {
					hasArchOpConfFilePath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the ARCH_OP_CONF_FILE_PATH parameter is missing");
					} else {
						/* Get the absolute path */
						URL archOpConfFileRes = TestInstantiation.class.getClass()
								.getResource(BASE_TEST_DIRECTORY + INSTANTIATION_INPUT_FILES + tokens[1]);
						String archOpConfFilePath = Paths.get(archOpConfFileRes.toURI()).toString();

						/* Instantiate the architecture operands */
						architectureOperands = new ArchitectureOperands(prefix, archOpConfFilePath);
					}
				}

				/* Check for OUTPUT_FOLDER_PATH_PARAM parameter */
				if (tokens[0].equals(ConstantFields.OUTPUT_FOLDER_PATH_PARAM)) {
					hasOutputFolderPath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the OUTPUT_FOLDER_PATH parameter is missing");
					} else {
						/* Get the absolute path */
						URL outputDirRes = TestInstantiation.class.getClass()
								.getResource(BASE_TEST_DIRECTORY + INSTANTIATION_OUTPUT_FILES);
						String outputDirPath = Paths.get(outputDirRes.toURI()).toString();

						/* Instantiate the output folder path */
						outputFolderPath = outputDirPath + tokens[1];
						
						System.out.println(outputFolderPath);
					}
				}

			}

		} finally {
			if (scanner != null)
				scanner.close();
		}

		/* If there is not some of the mandatory parameters */
		if (!hasArchStyleConfFilePath) {
			throw new TestConfigurationFileException("ARCH_STYLE_CONF_FILE_PATH parameter is missing");
		}

		if (!hasArchOpConfFilePath) {
			throw new TestConfigurationFileException("ARCH_OP_CONF_FILE_PATH parameter is missing");
		}

		if (!hasOutputFolderPath) {
			throw new TestConfigurationFileException("OUTPUT_FOLDER_PATH is missing");
		}

		/* The name of the module */
		String systemName = architectureStyle.getBipFileModel().getSystem().getName();
		/* The name of the root type in the module */
		String rootTypeName = architectureStyle.getBipFileModel().getRootType().getName();
		/* The name of the root type instance in the module */
		String rootInstanceName = architectureStyle.getBipFileModel().getRoot().getName();

		/* Create the BIP File Model for the instance */
		BIPFileModel bipFileModel = new BIPFileModel(systemName, rootTypeName, rootInstanceName);

		/* Create the output folder if not exists */
		File outputFolder = new File(outputFolderPath);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		/* Create the path to the resulting BIP file */
		String pathToSaveBIPFile = outputFolderPath + "/" + systemName + ".bip";
		/* Create the path to the resulting configuration file */
		String pathToSaveConfFile = outputFolderPath + "/" + systemName + "Conf.txt";

		/* Create the instance */
		ArchitectureInstantiator.createArchitectureInstance(architectureStyle, architectureOperands, bipFileModel,
				pathToSaveBIPFile, pathToSaveConfFile);
	}

	public static void testMutex() throws URISyntaxException, TestConfigurationFileException,
			ConfigurationFileException, ArchitectureExtractorException, ArchitectureBuilderException, IOException {
		TestInstantiation.testInstantiation(MUTEX_TEST_CONF_FILE);
	}

	public static void testModes2() throws URISyntaxException, TestConfigurationFileException,
			ConfigurationFileException, ArchitectureExtractorException, ArchitectureBuilderException, IOException {
		TestInstantiation.testInstantiation(MODES_TEST_CONF_FILE);
	}

	public static void testActionSequence() throws URISyntaxException, TestConfigurationFileException,
			ConfigurationFileException, ArchitectureExtractorException, ArchitectureBuilderException, IOException {
		TestInstantiation.testInstantiation(ACTION_SEQUENCE_TEST_CONF_FILE);
	}

	public static void main(String[] args) {
		try {
			TestInstantiation.testMutex();
		} catch (ConfigurationFileException | ArchitectureExtractorException | ArchitectureBuilderException
				| URISyntaxException | TestConfigurationFileException | IOException e) {
			e.printStackTrace();
		}
	}

}
