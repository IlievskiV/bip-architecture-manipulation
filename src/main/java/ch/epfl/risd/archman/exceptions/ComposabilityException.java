package ch.epfl.risd.archman.exceptions;

/**
 * This class is representing the general exception in the BIP Architecture
 * Composability. Every other exception related with the BIP Architecture
 * Composability is derived from this class.
 */
public class ComposabilityException extends Exception {

	public ComposabilityException(String message) {
		super(message);
	}
}
