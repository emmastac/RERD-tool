package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Several utils for program functions
 * 
 * @author alkoclick
 *
 */
public class Utils {

	private static final String COMMENT_CHAR = ExtStrings.getString("CommentChar");
	private static final Logger logger = Logger.getLogger(Utils.class.getName());

	/*
	 * Reads a file line by line and returns all lines in a new list Ignores
	 * lines starting with the commentChar, specified in strings.properties
	 * 
	 * @param filename The file to read from
	 * 
	 * @return ArrayList where each element is one line of the file
	 */
	public static List<String> reader(String filename) {
		ArrayList<String> lines = new ArrayList<>();

		// Try with resources autocloses the reader when done, regardless of try
		// result - Java 1.7 +
		// No need to close the temporary FileReader, java does it for us
		// http://stackoverflow.com/questions/16584777/is-it-necessary-to-close-a-filewriter-provided-it-is-written-through-a-buffered
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String L;
			while ((L = reader.readLine()) != null) {
				// Ignore comments and empty lines
				if (!(L.startsWith(COMMENT_CHAR) || L.isEmpty()))
					lines.add(L);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		return lines;
	}

	 /*Opens and writes into a file with the given filename. The file will be
	 * created if it does not exist
	 * 
	 * @param filename
	 *            The file to write in
	 */
	public static void writeToFile(String filename, ArrayList<String> linesToWrite) {

		// Try with resources autocloses the writer when done, regardless of try
		// result - Java 1.7 +
		// No need to close the temporary FileWriter, java does it for us
		// http://stackoverflow.com/questions/16584777/is-it-necessary-to-close-a-filewriter-provided-it-is-written-through-a-buffered

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

			for (String line : linesToWrite){
				writer.write(line+System.lineSeparator());
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

	}

}
