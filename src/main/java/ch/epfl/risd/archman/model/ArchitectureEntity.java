package ch.epfl.risd.archman.model;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Hashtable;

import ch.epfl.risd.archman.exceptions.ConfigurationFileException;

/**
 * This class models the base class of the Architecture instance, style and
 * operands.
 */
public abstract class ArchitectureEntity {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The model of the BIP file representing this entity */
	protected BIPFileModel bipFileModel;

	/*
	 * (key, value) pairs of the parameters of this entity
	 */
	protected Hashtable<String, String> parameters;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * Method for reading the parameters for the Architecture Entity, given the
	 * path to the configuration file.
	 * 
	 * @param pathToConfFile
	 *            - the path to the configuration file
	 * @throws FileNotFoundException
	 *             if the configuration file does not exist
	 * @throws ConfigurationFileException
	 *             if the configuration file is not in the predefined format
	 */
	protected abstract void readParameters(String pathToConfFile)
			throws FileNotFoundException, ConfigurationFileException;

	/**
	 * Method for parsing the parameters from the hash table of parameters
	 * 
	 * @throws ConfigurationFileException
	 *             if the configuration file was not in the predefined format
	 */
	protected abstract void parseParameters() throws ConfigurationFileException;

	/**
	 * This method validates the Architecture Entity, i.e. it checks whether the
	 * Architecture Entity is consistent with the information in the
	 * configuration file.
	 */
	protected abstract void validate();

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * @return the BIP file model of the Architecture Entity
	 */
	public BIPFileModel getBipFileModel() {
		return bipFileModel;
	}

	/**
	 * @return the parameters of the Architecture Entity
	 */
	public Hashtable<String, String> getParameters() {
		return parameters;
	}
}
