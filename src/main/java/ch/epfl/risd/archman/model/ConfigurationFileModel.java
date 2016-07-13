package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.helper.HelperMethods;

/**
 * This class is representing the model of one configuration file in a
 * object-oriented manner. This class is encapsulating the required information
 * for one configuration file.
 */
public class ConfigurationFileModel {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The list of required parameters */
	protected List<String> requiredParams;

	/* (key, value) pairs of the parameters */
	protected Hashtable<String, String> parameters;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Method for reading the parameters in this configuration file
	 * 
	 * @param pathToConfFile
	 *            - the absolute path to the configuration file
	 * @param requiredParams
	 *            - the list of required parameters
	 * @throws ConfigurationFileException
	 */
	protected void readParameters(String pathToConfFile, List<String> requiredParams)
			throws ConfigurationFileException {
		/* Instantiate the parameters hash table */
		this.parameters = new Hashtable<String, String>();

		/* Check the presence of the parameter */
		boolean[] hasParam = new boolean[requiredParams.size()];
		for (int i = 0; i < hasParam.length; i++) {
			hasParam[i] = false;
		}

		/* Scanner for reading the configuration file */
		Scanner scanner = null;

		try {
			/* Initialize the scanner */
			scanner = new Scanner(new File(pathToConfFile));

			while (scanner.hasNext()) {
				/* Take the current line and split it where the semicolon is */
				String[] tokens = scanner.nextLine().split(":");

				/* No more than one colon in a line exception */
				if (tokens.length > 2) {
					throw new ConfigurationFileException("More than one colon (:) in the line");
				}

				/* The index of current field */
				int indexOfParam = requiredParams.indexOf(tokens[0]);

				/* If the current field exists */
				if (indexOfParam != -1) {

					/* If the value is missing */
					if (tokens[1].trim().equals("")) {
						throw new ConfigurationFileException(
								"The value of the " + requiredParams.get(indexOfParam) + " parameter is missing");
					}
					/* Insert the value of the parameter */
					parameters.put(tokens[0], tokens[1]);
					/* Validate the presence of the parameter */
					hasParam[indexOfParam] = true;

				} else {
					throw new ConfigurationFileException("The parsed parameter" + tokens[0]
							+ "in the configuration file is not a defined parameter");
				}
			}

			/* Get the positions of the missing parameters */
			List<Integer> missingParams = HelperMethods.mapBoolValuesToPositions(hasParam, false);

			/* If parameters are missing */
			if (missingParams.size() != 0) {
				for (int i : missingParams) {
					throw new ConfigurationFileException(requiredParams.get(i) + " parameter is missing");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Default constructor for this class
	 */
	public ConfigurationFileModel() {
	}

	/**
	 * Constructor for the class when the path to the configuration file is
	 * given, i.e. all information is given
	 * 
	 * @param pathToConfFile
	 *            - absolute path to the configuration file
	 * @param requiredParams
	 *            - the list of required parameters that have to be present in
	 *            the configuration file
	 * @throws ConfigurationFileException
	 */
	public ConfigurationFileModel(String pathToConfFile, List<String> requiredParams)
			throws ConfigurationFileException {
		this.requiredParams = requiredParams;
		this.readParameters(pathToConfFile, requiredParams);
	}

	/**
	 * @return the map of parameters
	 */
	public Hashtable<String, String> getParameters() {
		return parameters;
	}

}
