package contest.winter2017.ohsfile;

import java.io.*;
import java.security.*;

public class Utils {
	public static boolean sameChecksum(File file1, File file2) throws NoSuchAlgorithmException, IOException {
		byte[] array1 = createChecksum(file1);
		byte[] array2 = createChecksum(file2);
		
		if(array1.length != array2.length) return false;
		for(int i = 0; i < array1.length; i++) {
			if(array1[i] != array2[i]) return false;
		}
		return true;
	}
	
    public static byte[] createChecksum(File filename) throws NoSuchAlgorithmException, IOException {
        InputStream fis =  new FileInputStream(filename);
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
