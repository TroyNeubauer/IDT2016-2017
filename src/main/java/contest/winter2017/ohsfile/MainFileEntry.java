package contest.winter2017.ohsfile;

import java.util.Arrays;

import com.troyberry.util.data.*;

/**
 * Represents a entry in a main file. The entry is a simplified version of OHS file containing basic information about tests passed failed etc.
 * @author Troy Neubauer
 *
 */
public class MainFileEntry {
	private long timestamp;
	private byte[] hashOfJarFile;
	private int passCount, failCount;
	private float percentCoveredForBasicTests;
	private String jarFileName;
	private int secutiryTestCount;
	
	public MainFileEntry() {
		this.hashOfJarFile = new byte[16];
	}
	
	public MainFileEntry(OHSFile file) {
		this.timestamp = file.getTimestamp();
		this.hashOfJarFile = file.getHashOfJarFile();
		this.passCount = file.getPassCount();
		this.failCount = file.getFailCount();
		this.percentCoveredForBasicTests = (float)file.getPercentCoveredForBasicTests();
		this.jarFileName = file.getJarFileName();
		this.secutiryTestCount = file.getSecurityTests().size();
	}

	/**
	 * Writes this entry to the writer. Assuming that the writer is in the right place
	 * @param writer The writer to write to
	 */
	public void write(TroyWriter writer) {
		writer.writeLong(timestamp);
		writer.writeBytes(hashOfJarFile);
		writer.writeString(jarFileName);
		writer.writeInt(passCount);
		writer.writeInt(failCount);
		writer.writeFloat(percentCoveredForBasicTests);
		writer.writeInt(secutiryTestCount);
	}
	
	/**
	 * Reads the data about this Entry from a reader. Assumes that the reader is in the right place and has data to read.
	 * @param reader The reader to read from
	 */
	public void read(TroyReader reader) {
		timestamp = reader.readLong();
		for(int i = 0; i < hashOfJarFile.length; i++) {
			hashOfJarFile[i] = reader.readByte();
		}
		jarFileName = reader.readString();
		passCount = reader.readInt();
		failCount = reader.readInt();
		percentCoveredForBasicTests = reader.readFloat();
		secutiryTestCount = reader.readInt();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public byte[] getHashOfJarFile() {
		return hashOfJarFile;
	}

	public void setHashOfJarFile(byte[] hashOfJarFile) {
		this.hashOfJarFile = hashOfJarFile;
	}

	public int getPassCount() {
		return passCount;
	}

	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}

	public int getFailCount() {
		return failCount;
	}

	public void setFailCount(int failCount) {
		this.failCount = failCount;
	}

	public float getPercentCoveredForBasicTests() {
		return percentCoveredForBasicTests;
	}

	public void setPercentCoveredForBasicTests(float percentCoveredForBasicTests) {
		this.percentCoveredForBasicTests = percentCoveredForBasicTests;
	}

	public String getJarFileName() {
		return jarFileName;
	}

	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
	}

	public int getSecutiryTestCount() {
		return secutiryTestCount;
	}

	public void setSecutiryTestCount(int secutiryTestCount) {
		this.secutiryTestCount = secutiryTestCount;
	}
	
	@Override
	public String toString() {
		return "MainFileEntry [timestamp=" + timestamp + ", hashOfJarFile=" + Arrays.toString(hashOfJarFile)
				+ ", passCount=" + passCount + ", failCount=" + failCount + ", percentCoveredForBasicTests="
				+ percentCoveredForBasicTests + ", jarFileName=" + jarFileName + ", secutiryTestCount="
				+ secutiryTestCount + "]";
	}
	
	
}
