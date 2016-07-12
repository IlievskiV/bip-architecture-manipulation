package ch.epfl.risd.archman.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class containing static helper methods
 *
 */
public final class HelperMethods {

	/**
	 * Helper method to map the elements of a given boolean value in one boolean
	 * array to their positions in the array
	 * 
	 * @param array
	 *            - the boolean array
	 * @param value
	 *            - the checking value
	 * @return list of mapped positions
	 */
	public static List<Integer> mapBoolValuesToPositions(boolean[] array, boolean value) {
		/* The resulting list */
		List<Integer> result = new LinkedList<Integer>();

		/* Map the values */
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				result.add(i);
			}
		}

		return result;
	}

	/**
	 * Helper method to split the concatenated string using a given delimiter
	 * 
	 * @param concatenatedString
	 *            - the string to split
	 * @param delim
	 *            - the given delimiter
	 * @return array of tokens
	 */
	public static String[] splitConcatenatedString(String concatenatedString, String delim) {
		/* Split the string and return tokens */
		String[] tokens = concatenatedString.split(delim);
		return tokens;
	}
	
	/**
	 * 
	 * @param concatenatedString
	 * @param delim1
	 * @param delim2
	 * @return
	 */
	public static List<String[]> splitConcatenatedString(String concatenatedString, String delim1, String delim2) {
		/* Initialize the result */
		List<String[]> result = new LinkedList<String[]>();

		/* Split to the first delimiter */
		String[] tokens = HelperMethods.splitConcatenatedString(concatenatedString, delim1);
		/* Iterate over them */
		for (String token : tokens) {
			/* Split to the second delimiter and add to results */
			result.add(HelperMethods.splitConcatenatedString(token, delim2));
		}

		return result;
	}

}
