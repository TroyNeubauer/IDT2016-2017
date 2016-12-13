package com.ohsidt.test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarException;

public class Executor {
	
	private Process process;
	private Action onEnd;
	private String path;
	
	public Executor(String path, Action onEnd, String... args){
		this.process = execute(path, ArrayUtil.toArrayList(args));
		this.onEnd = onEnd;
		this.path = path;
		processWaitThread();
	}
	
	private void processWaitThread() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				int exitCode = -1;
				try {
					exitCode = process.waitFor();
				} catch (InterruptedException e) {
					System.err.println("Process waiting thread was interupted: " + e.getMessage());
				}
				//Print out information about the exit code
				System.out.println("Process \"" + path + "\" ended with an exit code of " + exitCode + " (" + (exitCode == 0 ? "No error" : "Error") + ")");
				// Call the onAction method so that the user of this class knows that the process ended
				onEnd.onAction();
			}
		}, "Process Wait Thread");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
	}

	protected Process execute(String jarFilePath, List<String> args) {
	    // Create run arguments for the
	    final List<String> actualArgs = new ArrayList<String>();
	    actualArgs.add(0, "java");
	    actualArgs.add(1, "-jar");
	    actualArgs.add(2, jarFilePath);
	    actualArgs.addAll(args);
	    
	    Runtime re = Runtime.getRuntime();
	    String[] arrayArgs = actualArgs.toArray(new String[0]);
	    
	    ProcessBuilder pb = new ProcessBuilder(arrayArgs);
	    pb.redirectError();
	    pb.redirectOutput();
	    pb.directory(new File(jarFilePath).getParentFile());
	    Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			System.err.println("Unable to find file " + jarFilePath);
			e.printStackTrace();
		}
		System.out.println("Running process \"" + jarFilePath + "\"");

		return p;
	}

}
