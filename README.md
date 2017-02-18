# IDT2016-2017

1)  Overview
This Black Box Testing application allows a user to test a jar file of the specified format without having access to the source code of said jar. The results from each test will be printed as tests are being executed. Code coverage details, along with a report in YAML format that summarizes results from the testing session, are printed once testing is completed. The tests run on the software under test (SUT) is saved in a file which can be viewed via the OHS File Reader Program. At the end of the testing period, the OHS File Reader Program will be opened wherein the user may sort through and view past tests conducted using this software.

For in depth analysis of the testing sessions, we recommend utilizing the reader program to view and sort through executed test results.
This software usues Troy Berry Core for basic random number generation: https://github.com/TroyNeubauer/Troy-Berry-Core

#2) System requirements
The Black Box Testing application is written purely in Java 8 so it is important to have the correct Java version installed.
2.1) Java Installation: Obtain the current Java 8 version for your desired OS / platform
	2.1.1)  https://www.java.com/en/download/
	2.1.2) Run the installer file
2.2) Check if current version Java is correct
	2.2.1) Open a new command prompt window and run “java -version”
	2.2.2) The output should be similar to


#3) Download the Application
The application is available on GitHub at https://github.com/TroyNeubauer/IDT2016-2017/releases
1. Obtain the Black Box Testing application by downloading and extracting files from the zip folder entitled com.idtus.contest.winter2017.frameworkJars.zip


#4) Run the application
1. Open a command prompt window to the folder with the containing the application and run “java -jar com.idtus.contest.winter2017.framework.jar” in the folder where the jar is stored with the following parameters.
2. Add the following arguments after the command before executing it. All arguments are entered with a hyphen preceding the argument name and a space separating the argument name and value.
	Required arguments
	-jarToTestPath  <jar to test path here>
	-jacocoOutputPath <jacoco output dir path here>
	-jacocoAgentJarPath <jacoco agent jar path here>
Optional arguments regarding the reader program
	-saveTo  <OHS file output dir path here>
	-open <path to OHS file save>
	-saveName <name to save OHS file as here>
	-disableSaving
		Test information will not be saved in a custom OHS file
Other optional arguments
	-timeGoal <time goal in minutes here>
	The default is 5 minutes
	-bbTests <specified number of exploratory black box test iterations here>
		The default is 1000 tests
	-toolChain
 		Enter this argument if you wish for the output to only include a YAML report summarizing the black box tests 
	-help 
		Displays help
	-h
		Alternative method of calling -help

If both timeGoal and bbTests are entered as arguments, the black box tester will run the number of black box tests specified. If tests are performed before the time goal is reached, additional tests will be performed.until the time goal is reached.


#5) Using the reader program

Out solution exports data about each execution of our software to a custom file format structure. The reader program allows the user to view and analyze the results completed tests.

For the purposes of understanding, a few terms need to be established.
	-An OHS file / entry holds information about each execution of our software. Inside, these files contain all the command line arguments, expected arguments, resulting std streams, jar file tested etc. 
	-An OHS file save is a collection of OHS files / entries. 

The OHS file save is constructed like so:
	-There is a main folder that is the root of a OHS file save. Inside this folder, there is a “main file”, which contains the total amount of entries for this OHS file save, as well as some basic data about each entry. The main file doesn’t contain information about each execution of the jar under test. That is what the OHS files are for. 
	-Also inside the main folder, is a “Files” folder that contains all the OHS files in this OHS file save. The number of entries in the main file, and the number of OHS files in the Files folder should be the same. 

All data inside the OHS files and main file, are stored in a binary format as opposed to a text based format to keep overall file size down. 

The reader program can read all the information stored in an OHS file save. If saving to an OHS file at the end of execution isn't disabled (using -disableSaving as a command argument), the structure for an OHS file save will be created (if it isn't already), then the OHS file created and added to the Files folder as well as the basic data will be appended to the main file. After that the reader program will open automatically. 



#Reader Program Components

The Reader Program has five main components. 
All the way on the left is the Main File Info Panel. This panel displays some basic information about the main file, like number of entries, the time the last entry was added, tests count, etc.

To the right of the Main File Info Panel is the Entries List. The Entries list shows all the entries currently in the main file along with the basic information about each entry.

In the upper right corner is the Search Panel. The Search Panel can be used to filter and sort what entries are shown in the Entries List. The Search Panel’s most prominent feature is the search bar. When any text is typed, all entries are sorted by how similar all their characteristics are with the search term. For instance, if “3” was the search term, the entries that had more 3’s in their jar file names, security tests count, passed count, fail count, percent covered etc. would be closer to the top. To the right of the search bar is a combo box containing all the options that can be used to sort the entries. The entries can be sorted by their names, date created, basic tests count, security tests count and total code coverage. The entries list can be flipped by clicking the “A-Z” button. The combo box sort and the search bar cannot be used simultaneously. In the second row is the date filter. The date can be used to only show entries that come before of after a specified date.

When an entry is selected from the entries list, the corresponding OHS file is read and all the information about the tests for that OHS file are shown in the execution Info Panel. The panel shows all of the basic tests and security tests executed along with all their parameters used, the resulting streams and expected streams (for basic tests). If an error is detected from one of the error streams, it is shown below the execution. These errors are tailed and the top of the panel will display the error and the number of times it appears throughout the entry in the error summary. 

The Additional Filters Menu can be used to show or hide one type of exception when viewing an entry. To use this filter, the check box must be checked. If any errors are detected in the selected entry, they will appear in the combo box. By default, the selected error in the combo box will be shown and any executions that exhibit that error will be shown. Conversely, when the show button is clicked, the button will change to hide and all executions that have that error will be hidden.

#Options Menu
If there are too many executions showing up in the execution info panel, the settings for what shows up can be changed via the options ->open options menu.
The settings that can be changed go as follows:
	Show security tests first;
		Shows the security tests above the basic tests in the execution info panel.
	Hide error streams if blank;
		If the error streams are blank, hide them.
	Only show tests that have errors
		Only tests that have errors will be shown. What exactly an error is, can be configured below.
	Include basic tests in error summary;
		If checked, any errors that are detected in the basic tests’ error streams will be included in the additional options filter for errors combo box as well as being listed in the error summary.
	What should count as an error
		Determines “what an error is.” This setting is used for the “Only show tests that have errors” option. 
			If “any data in the error stream counts as an error” is chosen, all tests that have something in the error stream will be shown if the “Only show tests that have errors” option is checked.
			Alternatively, If “Only actual error statements like “java.lang. NullPointerException" count as errors” is chosen, only tests that have a recognizable throwable will be shown, assuming that the “Only show tests that have errors” option is checked.
	The last option determines what format the hour is in for the date filter, 12 or 24 hour.

When the reader program is launched with the stand alone jar, an Open Save Menu will appear. From this menu any OHS file save can be opened. Any file of folder within an OHS file save can be used to open that save. Ie, the main folder, main file, or any files within the /Files folder can be used to tell the reader what OHS file save to open. Alternatively, the command line arguments “-open <path to OHS file save>” can be used to open an OHS file save upon startup. This will prevent the open OHS file save dialog from opening.

#Menu Bar

The menu bar can be used to do various things:
	Under the File menu:
		Using Open, new OHS file saves can be opened alongside all the previously opened OHS file saves.
		Using Close, the selected OHS file save can be closed.
	Under the Options menu:
		Using Open Options, opens the options menu. For more details see the Options Menu section.
		Using Refresh Open Files, all OHS file saves will be reloaded in case a file has changed, or the main program added another entry.

