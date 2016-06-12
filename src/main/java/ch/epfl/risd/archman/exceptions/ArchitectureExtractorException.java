package ch.epfl.risd.archman.exceptions;

/**
 * This class is representing the general exception for the extraction of the
 * BIP architecture. Every other exception related with the extracting BIP
 * architecture is derived from this class.
 */
public class ArchitectureExtractorException extends ComposabilityException {

	public ArchitectureExtractorException(String message) {
		super(message);
	}

}
