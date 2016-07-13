package ch.epfl.risd.archman.model;

import java.util.List;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;

public class TestConfigFile extends ConfigurationFileModel {

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/
	public TestConfigFile(String pathToConfFile, List<String> requiredParams) throws ConfigurationFileException {
		super(pathToConfFile, requiredParams);
	}

}
