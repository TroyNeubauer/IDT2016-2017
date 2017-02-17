package contest.winter2017.ohsfile;

import java.io.*;
import java.security.*;

public class Utils {

	/**
	 * Returns the name of the class of an error from a stack trace.<br>
	 * IE "exception in thread main java.lang.NullpointerException at line 7: test.foo", would return "java.lang.NullpointerException"
	 * @param stackTrace The stacktrace to use
	 * @return The name of the class of the error
	 */
	public static String getErrorType(String stackTrace) {
		try {
			int endIndex = stackTrace.indexOf("Exception", "Exception".length()) + "Exception".length();
			int beginIndex = endIndex -1;
			while (beginIndex >= 0 && stackTrace.substring(beginIndex, endIndex).indexOf(" ") == -1) {
				beginIndex--;
			}
			return stackTrace.substring(beginIndex +1, endIndex);
		} catch (java.lang.StringIndexOutOfBoundsException e) {
		}
		return "";
	}
	
	/**
	 * Returns the min of two ints as long as they are positive.
	 * @param a The first value to use
	 * @param b The second value to use
	 * @return the smaller value of the two of them or 0
	 */
	private static int posativeMin(int a, int b) {
		return Math.max(0, Math.min(a, b));
	}

	/**
	 * Creates an MD5 checksum for a file
	 * @param filename The path to the file
	 * @return The generated checksum
	 * @throws NoSuchAlgorithmException If the MD5 algorithm can't be found
	 * @throws IOException If the file can't be found or an I/O error occurs
	 */
	public static byte[] createChecksum(File filename) throws NoSuchAlgorithmException, IOException {
		InputStream fis = new FileInputStream(filename);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

}
