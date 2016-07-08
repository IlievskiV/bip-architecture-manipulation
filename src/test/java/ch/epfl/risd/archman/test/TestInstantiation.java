package ch.epfl.risd.archman.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Class for testing the Architecture Instantiation
 */
public class TestInstantiation {

	public static final String BASE_TEST_DIRECTORY = "/TestCases/Instantiation/TestConfFiles";

	public static final String MUTEX_TEST_CONF_FILE = "TestConfiguration_Mutex.txt";
	public static final String MODES_TEST_CONF_FILE = "TestConfiguration_Modes2.txt";
	public static final String ACTION_SEQUENCE_TEST_CONF_FILE = "TestConfiguration_ActionSequqnce.txt";
	
	
	public static void testMutex(String pathToTestConfFile) {
		
	}

	public static void testModes2(String pathToTestConfFile) {
		
	}

	public static void testActionSequence(String pathToTestConfFile) {
		
	}

	public static void main(String[] args) {
		URL resource = TestInstantiation.class.getClass().getResource("/TestCases/Instantiation/TestConfFiles/../");
		try {
			System.out.println(Paths.get(resource.toURI()).toString());

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
