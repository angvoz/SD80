/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 package org.eclipse.cdt.make.scannerdiscovery;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.internal.errorparsers.tests.ResourceHelper;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuildCommandParser;
import org.eclipse.cdt.make.core.scannerconfig.GCCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class GCCBuildCommandParserTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}
	
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	
	public void testAbstractBuildCommandParser_Nulls() throws Exception {
		AbstractBuildCommandParser parser = new AbstractBuildCommandParser() {
			@Override
			public boolean processLine(String line) {
				return true;
			}
			
		};
		parser.startup(null);
		parser.processLine(null);
		parser.shutdown();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
		assertNull(entries);
	}
	
	public void testAbstractBuildCommandParser_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		
		// create test class
		AbstractBuildCommandParser parser = new AbstractBuildCommandParser() {
			@Override
			public boolean processLine(String line) {
				// pretending that we parsed the line
				List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
				ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
				entries.add(entry);
				String fileString = "file.cpp";
				
				setSettingEntries(entries, fileString);
				return true;
			}
			
		};
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -DMACRO=VALUE file.cpp");
		parser.shutdown();
		
		// sanity check that it does not return same values for all inputs
		List<ICLanguageSettingEntry> noentries = parser.getSettingEntries(null, null, null);
		assertNull(noentries);
		
		// check populated entries
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CMacroEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	/**
	 */
	public void testOneEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
		
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
		
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path0", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
	}
	
	/**
	 */
	public void testCIncludePathEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
		
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
		
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc"
				// regular
				+ " -I/path0 "
				// space after -I
				+ " -I /path1 "
				// unknown option, should be ignored
				+ " -? "
				// double-quoted path with spaces
				+ " -I\"/path with spaces\""
				// single-quoted path with spaces
				+ " -I'/path with spaces2'"
				// second single-quoted and space after -I
				+ " -I '/path with spaces3'"
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path0", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path1", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(1);
			assertEquals(expected, entry);
		}
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path with spaces", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(2);
			assertEquals(expected, entry);
		}
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path with spaces2", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(3);
			assertEquals(expected, entry);
		}
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path with spaces3", ICSettingEntry.READONLY);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(4);
			assertEquals(expected, entry);
		}
	}
	
	/**
	 */
	public void testCMacroEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
	
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
	
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -DMACRO0"
				+ " -DMACRO1=value"
				+ " -DMACRO2=\"value with spaces\""
				+ " -DMACRO3='value with spaces'"
				+ " -DMACRO4='\"quoted value\"'"
				+ " -D'MACRO5=\"quoted value\"'"
				+ " -DMACRO6=\"'single-quoted value'\""
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CMacroEntry expected = new CMacroEntry("MACRO0", "", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO1", "value", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(1);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO2", "value with spaces", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(2);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO3", "value with spaces", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(3);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO4", "\"quoted value\"", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(4);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO5", "\"quoted value\"", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(5);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO6", "'single-quoted value'", ICSettingEntry.READONLY);
			CMacroEntry entry = (CMacroEntry)entries.get(6);
			assertEquals(expected, entry);
		}
	}

	/**
	 */
	public void testCIncludeFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();

		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -include /include.file"
				+ " -include '/include.file with spaces'"
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CIncludeFileEntry expected = new CIncludeFileEntry("/include.file", ICSettingEntry.READONLY);
			CIncludeFileEntry entry = (CIncludeFileEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CIncludeFileEntry expected = new CIncludeFileEntry("/include.file with spaces", ICSettingEntry.READONLY);
			CIncludeFileEntry entry = (CIncludeFileEntry)entries.get(1);
			assertEquals(expected, entry);
		}
	}

	/**
	 */
	public void testCMacroFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
	
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
	
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -macros macro.file file.cpp");
		parser.processLine("gcc "
				+ " -macros /macro.file"
				+ " -macros '/macro.file with spaces'"
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CMacroFileEntry expected = new CMacroFileEntry("/macro.file", ICSettingEntry.READONLY);
			CMacroFileEntry entry = (CMacroFileEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CMacroFileEntry expected = new CMacroFileEntry("/macro.file with spaces", ICSettingEntry.READONLY);
			CMacroFileEntry entry = (CMacroFileEntry)entries.get(1);
			assertEquals(expected, entry);
		}
	}

	/**
	 */
	public void testCLibraryPathEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
	
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
	
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -L/path0"
				+ " -L'/path with spaces'"
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CLibraryPathEntry expected = new CLibraryPathEntry("/path0", ICSettingEntry.READONLY);
			CLibraryPathEntry entry = (CLibraryPathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CLibraryPathEntry expected = new CLibraryPathEntry("/path with spaces", ICSettingEntry.READONLY);
			CLibraryPathEntry entry = (CLibraryPathEntry)entries.get(1);
			assertEquals(expected, entry);
		}
	}

	/**
	 */
	public void testCLibraryFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();

		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -ldomain file.cpp");
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CLibraryFileEntry expected = new CLibraryFileEntry("libdomain.a", ICSettingEntry.READONLY);
		CLibraryFileEntry entry = (CLibraryFileEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);
	}

	/**
		 */
		public void testMixedSettingEntries() throws Exception {
			// Create model project and accompanied descriptions
			String projectName = getName();
			IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
			ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			IFile file=ResourceHelper.createFile(project, "file.cpp");
			ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
			String languageId = ls.getLanguageId();
	
			// create GCCBuildCommandParser
			GCCBuildCommandParser parser = new GCCBuildCommandParser();
	
			// parse fake line
			parser.startup(cfgDescription);
			parser.processLine("gcc"
					+ " -I/path0 "
					+ " -DMACRO1=value"
					+ " -v"
					+ " -ldomain"
					+ " -E"
					+ " -I /path1 "
					+ " -DMACRO2=\"value with spaces\""
					+ " -I\"/path with spaces\""
					+ " -o file.exe"
					+ " -L/usr/lib"
					+ " file.cpp"
					+ " -mtune=pentiumpro"
				);
			parser.shutdown();
			
			// check populated entries
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
	//		+ " -I/path0 "
			{
				CIncludePathEntry expected = new CIncludePathEntry("/path0", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(0));
			}
	//		+ " -DMACRO1=value"
			{
				CMacroEntry expected = new CMacroEntry("MACRO1", "value", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(1));
			}
	//		+ " -ldomain"
			{
				CLibraryFileEntry expected = new CLibraryFileEntry("libdomain.a", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(2));
			}
	//		+ " -I /path1 "
			{
				CIncludePathEntry expected = new CIncludePathEntry("/path1", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(3));
			}
	//		+ " -DMACRO2=\"value with spaces\""
			{
				CMacroEntry expected = new CMacroEntry("MACRO2", "value with spaces", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(4));
			}
	//		+ " -I\"/path with spaces\""
			{
				CIncludePathEntry expected = new CIncludePathEntry("/path with spaces", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(5));
			}
	//		+ " -L/usr/lib"
			{
				CLibraryPathEntry expected = new CLibraryPathEntry("/usr/lib", ICSettingEntry.READONLY);
				assertEquals(expected, entries.get(6));
			}
			
			assertEquals(7, entries.size());
		}

	/**
	 */
	public void testMissingFile() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
	
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 missing.cpp");
		parser.shutdown();
		
		// add the file to the project (to be able to inquire)
		IFile file=ResourceHelper.createFile(project, "missing.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(null, entries);
	}
	
	/**
	 */
	public void testFileWithSpaces() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		IFile file1=ResourceHelper.createFile(project, "file with spaces 1.cpp");
		ICLanguageSetting ls1 = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId1 = ls1.getLanguageId();
		
		IFile file2=ResourceHelper.createFile(project, "file with spaces 2.cpp");
		ICLanguageSetting ls2 = cfgDescription.getLanguageSettingForFile(file2.getProjectRelativePath(), true);
		String languageId2 = ls2.getLanguageId();
		
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = new GCCBuildCommandParser();
		
		// parse fake line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 'file with spaces 1.cpp'");
		parser.processLine("gcc -I/path0 \"file with spaces 2.cpp\"");
		parser.shutdown();
		
		// check populated entries
		{
			// in single quotes
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId1);
			CIncludePathEntry expected = new CIncludePathEntry("/path0", ICSettingEntry.READONLY);
			assertEquals(expected, entries.get(0));
		}
		{
			// in double quotes
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId2);
			CIncludePathEntry expected = new CIncludePathEntry("/path0", ICSettingEntry.READONLY);
			assertEquals(expected, entries.get(0));
		}
	}
	

}
