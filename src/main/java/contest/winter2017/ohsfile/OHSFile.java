package contest.winter2017.ohsfile;

import java.util.*;

/**
 * Represents a OHS file that can be created or read from. A OHS file is a binary based file the contains information about a 
 * single execution of this tool. It will include information about all failed basic tests as well as all the information of all security tests.
 * @author Troy Neubauer
 *
 */
public class OHSFile {
	
	private int version;
	private long timestamp;
	private int securityTestOffset;
	private byte[] hashOfJarFile = new byte[16];
	private int passCount, failCount;
	private double percentCoveredForBasicTests;
	private int securityTestsConducted;
	
	private List<BasicTest> basicTests = new ArrayList<BasicTest>();
	private List<SecurityTest> securityTests = new ArrayList<SecurityTest>();
	
	/**
	 * TODO
	 */
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getSecurityTestOffset() {
		return securityTestOffset;
	}
	public void setSecurityTestOffset(int securityTestOffset) {
		this.securityTestOffset = securityTestOffset;
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
	public int getSecurityTestsConducted() {
		return securityTestsConducted;
	}
	public void setSecurityTestsConducted(int securityTestsConducted) {
		this.securityTestsConducted = securityTestsConducted;
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
	}
	public List<SecurityTest> getSecurityTests() {
		return securityTests;
	}
	public void addSecurityTest(SecurityTest test) {
		this.securityTests.add(test);
	}
	
	public void toYAML()
	{
		
		  System.out.println("Total predefined tests run: " + securityTestsConducted);
          System.out.println("Number of predefined tests that passed: "+getPassCount());
          System.out.println("Number of predefined tests that failed: "+getFailCount());
          System.out.println("Total code coverage percentage: "+getPercentCoveredForBasicTests()+" Unique error count: "+getFailCount());
          System.out.println("Errors seen: ");
          	for(BasicTest t:getPassedBasicTests())
          		System.out.println("	-"+t.getOutputErr());
		 
	}
	
	
}
