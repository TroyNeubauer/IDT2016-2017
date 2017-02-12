package contest.winter2017.ohsfile;

import java.util.*;

/**
 * Represents a OHS file that can be created or read from. A OHS file is a binary based file the contains information about a 
 * single execution of this tool. It will include information about all failed basic tests as well as all the information of all security tests.
 * @author Troy Neubauer
 *
 */
public class OHSFile {

	private short version;
	private long timestamp;
	private byte[] hashOfJarFile = new byte[16];
	private int passCount, failCount;
	private float percentCoveredForBasicTests;
	private String jarFileName;
	
	private List<BasicTest> basicTests = new ArrayList<BasicTest>();
	private List<SecurityTest> securityTests = new ArrayList<SecurityTest>();
	
	public OHSFile() {
	}
	
	
	
	public long getTimestamp() {
		return timestamp;
	}
	public String getJarFileName() {
		return jarFileName;
	}

	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
	}

	public short getVersion() {
		return version;
	}
	public void setVersion(short version) {
		this.version = version;
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
	public double getPercentCoveredForBasicTests() {
		return percentCoveredForBasicTests;
	}
	public void setPercentCoveredForBasicTests(float percentCoveredForBasicTests) {
		this.percentCoveredForBasicTests = percentCoveredForBasicTests;
	}
	public List<BasicTest> getBasicTests() {
		return basicTests;
	}
	public List<BasicTest> getFailedBasicTests() {
		ArrayList<BasicTest> result = new ArrayList<BasicTest>();
		for(BasicTest t : basicTests) {
			if(!t.isPass())result.add(t);
		}
		return result;
	}
	public List<BasicTest> getPassedBasicTests() {
		ArrayList<BasicTest> result = new ArrayList<BasicTest>();
		for(BasicTest t : basicTests) {
			if(t.isPass())result.add(t);
		}
		return result;
	}
	public void addBasicTest(BasicTest test) {
		this.basicTests.add(test);
		if(test.isPass()) {
			passCount++;
		} else {
			failCount++;
		}
	}
	public List<SecurityTest> getSecurityTests() {
		return securityTests;
	}
	public void addSecurityTest(SecurityTest test) {
		this.securityTests.add(test);
	}



	@Override
	public String toString() {
		return "OHSFile [version=" + version + ", timestamp=" + timestamp + ", hashOfJarFile="
				+ Arrays.toString(hashOfJarFile) + ", passCount=" + passCount + ", failCount=" + failCount
				+ ", percentCoveredForBasicTests=" + percentCoveredForBasicTests + ", securityTestsConducted="
				+ securityTests.size()+ ", jarFileName=" + jarFileName + ", basicTests=" + basicTests
				+ ", securityTests=" + securityTests + "]";
	}
	
	
}
