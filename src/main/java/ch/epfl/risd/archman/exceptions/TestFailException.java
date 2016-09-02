package ch.epfl.risd.archman.exceptions;

public class TestFailException extends Exception {
	public TestFailException() {
		super("Check FAILED");
	}
};