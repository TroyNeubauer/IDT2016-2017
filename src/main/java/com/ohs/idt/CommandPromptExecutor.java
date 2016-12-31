package com.ohs.idt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandPromptExecutor extends Executor {
	
	public CommandPromptExecutor(String pathToJar, Action onEnd, String... args) {
		super(pathToJar, onEnd, args);
	}
	
	@Override
	protected Process execute(String jarFilePath, List<String> args) {
	    // Create run arguments for the
	    final List<String> actualArgs = new ArrayList<String>();
	    actualArgs.add(0, "java");
	    actualArgs.add(1, "-jar");
	    actualArgs.add(2, jarFilePath);
	    actualArgs.addAll(args);
	    	    
	    Process p = null;
	    OutputStream out = null;
		try {
			p = Runtime.getRuntime().exec("cmd.exe /c start cmd.exe");
			 out = p.getOutputStream();
		        out.write("dir".getBytes());
		        out.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
       
	    

		return p;
	}

}
