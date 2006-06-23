/*******************************************************************************
 * Copyright (c) 2004, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ManagedCommandLineGeneratorTest extends TestCase {

    private static String[] testCommandLinePatterns = {null, "${COMMAND}", "${COMMAND} ${FLAGS}", "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}",
            "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}", "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT}",
            "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS}", 
            "${command} ${flags} ${output_flag}${output_prefix}${output} ${WRONG_VAR_NAME}"};
    private static String COMMAND_VAL = "[command]";
    private static String FLAGS_VAL = "[flags]";
    private static String[] FLAGS_ARRAY_VAL = FLAGS_VAL.split( "\\s" );
    private static String OUTPUT_FLAG_VAL = "[outputFlag]";
    private static String OUTPUT_PREFIX_VAL = "[outputPrefix]";
    private static String OUTPUT_VAL = "[output]";
    private static String INPUTS_VAL = "[inputs]";
    private static String[] INPUTS_ARRAY_VAL = INPUTS_VAL.split( "\\s" );
    private static String[] commandLineEtalonesForPatterns = {
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\"" + INPUTS_VAL + "\"",
            COMMAND_VAL, COMMAND_VAL + " " + FLAGS_VAL, COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL, 
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL +  OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"",
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\"" + INPUTS_VAL + "\"",
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "${WRONG_VAR_NAME}" };
    private static String[] commandLineEtalonesForParameters = {
            FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + "  " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " ",
             };
    
    public ManagedCommandLineGeneratorTest( String name ) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite( ManagedCommandLineGeneratorTest.class.getName() );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testGetCommandLineGenerator" ) );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testGenerateCommandLineInfoPatterns" ) );
        //  TODO:  The parameters set to NULL in these tests are not currently allowed to be null
        //suite.addTest( new ManagedCommandLineGeneratorTest( "testGenerateCommandLineInfoParameters" ) );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testCustomGenerator" ) );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testDollarValue" ) );
        return suite;
    }
    
    public final void testGetCommandLineGenerator() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        assertNotNull( gen );
    }

    public final void testGenerateCommandLineInfoPatterns() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        IManagedCommandLineInfo info = null;
        for( int i = 0; i < testCommandLinePatterns.length; i++ ) {
            info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, 
                    testCommandLinePatterns[i] );
            assertNotNull( info );
            if( i < commandLineEtalonesForPatterns.length ) {
                assertEquals( info.getCommandLine(), commandLineEtalonesForPatterns[i] );
            }
        }
    }
    
    public final void testGenerateCommandLineInfoParameters() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        IManagedCommandLineInfo info = gen.generateCommandLineInfo( null, null, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[0] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, null, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[1] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, null, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[2] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, null, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[3] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, null, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[4] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, null, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine().trim(), commandLineEtalonesForParameters[5].trim() );
    }

    public final void testCustomGenerator() {
    	
    	//  First, verify the elements in the project type
    	IProjectType proj = ManagedBuildManager.getProjectType("cdt.managedbuild.test.java.attrs");
    	assertNotNull(proj);
    	IConfiguration[] configs = proj.getConfigurations();
    	assertEquals(1, configs.length);
    	IConfiguration config = proj.getConfiguration("cdt.managedbuild.test.java.attrs.config");
    	assertNotNull(config);
    	ITool[] tools = config.getTools();
    	assertEquals(1, tools.length);
    	ITool tool = config.getTool("cdt.managedbuild.test.java.attrs.tool");
    	assertNotNull(tool);
    	IOption[] options = tool.getOptions();
    	assertEquals(20, options.length);
    	IOption option = tool.getOption("testgnu.c.compiler.option.preprocessor.def.symbols.test");
    	assertNotNull(option);
    	Object val = option.getValue();
    	assertTrue(val instanceof ArrayList);
    	ArrayList list = (ArrayList)val;
    	assertEquals("foo", list.get(0));
    	assertEquals("bar", list.get(1));
    	
    	//  Next, invoke the commandLineGenerator for this tool
    	IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
    	String[] flags = {"-a", "-b", "-c"};
    	String[] inputs = {"xy.cpp", "ab.cpp", "lt.cpp", "c.cpp"};
    	IManagedCommandLineInfo info = gen.generateCommandLineInfo(tool, "MyName", flags, "-of", "opre", "TheOutput.exe", inputs, "[COMMAND] [FLAGS]");
    	assertEquals("compiler.gnu.cMyName", info.getCommandName());
    	assertEquals("-c -b -a", info.getFlags());
    	assertEquals("ab.cpp c.cpp foo.cpp lt.cpp xy.cpp", info.getInputs());
    	assertEquals("-0h", info.getOutputFlag());
    	assertEquals("", info.getOutputPrefix());
    	assertEquals("Testme", info.getOutput());
    	assertEquals("[COMMAND] [FLAGS]", info.getCommandLinePattern());
    	assertEquals("This is a test command line", info.getCommandLine());
    	
    	//  Next, invoke the build file generator for the tool chain
    	IManagedBuilderMakefileGenerator makeGen = ManagedBuildManager.getBuildfileGenerator(config);
    	String name = makeGen.getMakefileName();
    	assertEquals("TestBuildFile.mak", name);
    }
    
    public final void testDollarValue() {
		try{
			IProject project = ManagedBuildTestHelper.createProject("CDV", null, (IPath)null, "cdt.test.dollarValue.ProjectType");
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration config = info.getDefaultConfiguration();
			//  Set values for the options
			ITool[] tools = config.getToolsBySuperClassId("cdt.test.dollarValue.Tool");
			assertEquals(tools.length, 1);
			ITool tool = tools[0];
			IOption option1 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option1");
			IOption option2 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option2");
			IOption option3 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option3");
			IOption option4 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option4");
			IOption option5 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option5");
			IOption option6 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option6");

			String command;

			option1 = config.setOption(tool, option1, "OPT1VALUE");
			option2 = config.setOption(tool, option2, "");
			option3 = config.setOption(tool, option3, "X");
			option4 = config.setOption(tool, option4, "opt4");
			command = tool.getToolCommandFlagsString(null, null);
	    	assertEquals("-opt1 OPT1VALUE X ${opt4}", command);
						
	    	option1 = config.setOption(tool, option1, "");
	    	option2 = config.setOption(tool, option2, "Opt2");
	    	option3 = config.setOption(tool, option3, "All work and no play...");
	    	option4 = config.setOption(tool, option4, "123456789");
	    	option5 = config.setOption(tool, option5, "DollarValue");
			command = tool.getToolCommandFlagsString(null, null);
	    	assertEquals("-opt2Opt2suffix All work and no play... ${123456789} DollarValueDollarValue", command);
			
	    	option1 = config.setOption(tool, option1, "0");
	    	option2 = config.setOption(tool, option2, "LongValue");
	    	option3 = config.setOption(tool, option3, "");
	    	option4 = config.setOption(tool, option4, "");
	    	option5 = config.setOption(tool, option5, "$");
	    	option6 = config.setOption(tool, option6, "%%");
			command = tool.getToolCommandFlagsString(null, null);
	    	assertEquals("-opt1 0 -opt2LongValuesuffix $$ x%%yy%%z", command);
			
	    	option1 = config.setOption(tool, option1, "1");
	    	option2 = config.setOption(tool, option2, "2");
	    	option3 = config.setOption(tool, option3, "3");
	    	option4 = config.setOption(tool, option4, "4");
	    	option5 = config.setOption(tool, option5, "");
	    	option6 = config.setOption(tool, option6, "");
			command = tool.getToolCommandFlagsString(null, null);
	    	assertEquals("-opt1 1 -opt22suffix 3 ${4}", command);
			
			ManagedBuildTestHelper.removeProject("CDV");
		}
		catch(Exception e){
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
    }

}
