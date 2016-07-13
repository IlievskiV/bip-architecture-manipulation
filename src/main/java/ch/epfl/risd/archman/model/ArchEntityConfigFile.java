package ch.epfl.risd.archman.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.helper.HelperMethods;

public class ArchEntityConfigFile extends ConfigurationFileModel {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Method to initialize the parameters.
	 */
	protected void initializeParameters() {
		/* Initialize the parameters */
		this.parameters = new Hashtable<String, String>();

		/* Iterate over the required parameters */
		for (String requiredParam : this.requiredParams) {
			/* Initialize the parameter */
			this.parameters.put(requiredParam, "");
		}
	}

	/**
	 * Method to add new value in the parameters.
	 * 
	 * @param key
	 *            - the key to the value
	 * @param valueChunk
	 *            - one value chunk of the parameter value
	 */
	protected void addToParameters(String key, String valueChunk) {
		if (this.parameters.get(key).equals("")) {
			this.parameters.put(key, valueChunk);
		} else {
			this.parameters.put(key, this.parameters.get(key) + "," + valueChunk);
		}
	}

	/**
	 * Method to remove value from the parameters
	 * 
	 * @param key
	 *            - the key to the value
	 * @param valueChunk
	 *            - one value chunk of the parameter value
	 */
	protected void removeFromParameters(String key, String valueChunk) {
		/* Get the string of parameters */
		String params = this.parameters.get((String) key);

		/* String Builder for the result */
		StringBuilder sb = new StringBuilder();

		/* Split to tokens */
		String[] tokens = HelperMethods.splitConcatenatedString(params, ",");

		/* Iterate over them */
		for (String t : tokens) {
			if (!t.equals(valueChunk)) {
				sb.append(t).append(",");
			}
		}

		/* Cut the last comma */
		sb.setLength(sb.length() - 1);

		/* Update parameters */
		this.parameters.put(key, sb.toString());
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for the class when the configuration file is not containing
	 * value for the given required parameters, i.e. they have to be filled.
	 * 
	 * @param requiredParams
	 *            - the list required parameters that have to be present in the
	 *            configuration file
	 */
	public ArchEntityConfigFile(List<String> requiredParams) {
		this.requiredParams = requiredParams;
		this.initializeParameters();
	}

	public ArchEntityConfigFile(String pathToConfFile, List<String> requiredParams) throws ConfigurationFileException {
		super(pathToConfFile, requiredParams);
	}

	/**
	 * Method to generate the resulting architecture entity configuration file.
	 * 
	 * @param pathToConfFile
	 *            - relative or absolute path to the configuration file
	 * @throws IOException
	 */
	public void createFile(String pathToConfFile) throws IOException {
		/* Get the absolute path */
		String absolutePath = new File(pathToConfFile).getAbsolutePath();

		/* Create the file */
		File confFile = new File(absolutePath);

		if (!confFile.exists()) {
			confFile.createNewFile();
		}

		PrintWriter printer = null;

		try {
			/* Initialize the printer */
			printer = new PrintWriter(confFile);

			/* Get the key set of the parameters */
			Set<String> keys = this.parameters.keySet();

			/* Iterate over the key set */
			for (String key : keys) {
				/* Write the value in new line */
				printer.println(key + ":" + this.parameters.get(key));
			}

		}
		/* Close the printer */
		finally {
			if (printer != null) {
				printer.flush();
				printer.close();
			}
		}
	}
}
