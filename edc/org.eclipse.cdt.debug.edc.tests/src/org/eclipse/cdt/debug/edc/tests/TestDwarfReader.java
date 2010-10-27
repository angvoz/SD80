/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.tests;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.arm.IARMSymbol;
import org.eclipse.cdt.debug.edc.internal.HostOS;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ConstType;
import org.eclipse.cdt.debug.edc.internal.symbols.FieldType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.IInheritance;
import org.eclipse.cdt.debug.edc.internal.symbols.ILexicalBlockScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITemplateParam;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.internal.symbols.InheritanceType;
import org.eclipse.cdt.debug.edc.internal.symbols.SubroutineType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfDebugInfoProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfDebugInfoProvider.PublicNameInfo;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfInfoReader;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.LocationEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.LocationExpression;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.LocationList;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class TestDwarfReader extends BaseDwarfTestCase {
	protected static final String[] symFilesToTest = {
		"BlackFlagMinGW.exe",
		"BlackFlag_gcce.sym",
		"BlackFlag_linuxgcc.exe",
		"BlackFlag_rvct.sym",
		"BlackFlag_gcce_343.sym",
		"HelloWorld_rvct_2_2.exe.sym",
		"HelloWorld_rvct_4_0.exe.sym",
		"QtConsole_gcce_343.sym",
		"SimpleCpp_rvct_22.sym",
		"SimpleCpp_rvct_40.sym",
		"SimpleCpp_gcce_432.sym",
		"SimpleCpp_gcc_x86.exe",
	};

	/** Bag of data for testing sym files.  The key is 'symFile' and other
	 * elements are used by specific tests.
	 */
	protected static class TestInfo {
		IPath symFile;
		IPath exeFile;
		int numberOfSources;
		int numberOfModuleScopeChildren;
		int numberOfVariables;
		Map<String, Map<String, VariableInfo>> cuVarMap = new HashMap<String, Map<String,VariableInfo>>();
		int numberOfTypes;
		IPath blackFlagMainFilePath;
		int numberOfSymbols;
		int numberOfPubFuncNames;
		int numberOfPubFuncEntries;
		int numberOfPubVarNames;
		int numberOfPubVarEntries;
		List<String> pubVars = new ArrayList<String>();
		List<String> pubFuncs = new ArrayList<String>();
		Map<String, List<ScopeInfo>> scopeInfos = new LinkedHashMap<String, List<ScopeInfo>>();
	}

	protected  static Map<String, TestInfo> testInfos = new LinkedHashMap<String, TestInfo>();
	
	static {
		for (String sym : symFilesToTest) {
			TestInfo info = new TestInfo();
			info.symFile = getFile(sym);
			testInfos.put(sym, info);
		}
	}
	
	protected  static TestInfo lookupInfo(String sym) {
		return testInfos.get(sym);
	}
	
	protected  static void setExe(String sym, String exe) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.exeFile = getFile(exe);
	}
	
	// not all the *.exes really exist; used to test mapping from *.exe to *.sym
	static {
		setExe("BlackFlagMinGW.exe", "BlackFlagMinGW.exe");
		setExe("BlackFlag_gcce.sym", "BlackFlag_gcce.exe");
		setExe("BlackFlag_linuxgcc.exe", "BlackFlag_linuxgcc.exe");
		setExe("BlackFlag_rvct.sym", "BlackFlag_rvct.exe");
		setExe("HelloWorld_rvct_2_2.exe.sym", "HelloWorld_rvct_2_2.exe");
		setExe("HelloWorld_rvct_4_0.exe.sym", "HelloWorld_rvct_4_0.exe");
		setExe("QtConsole_gcce_343.sym", "QtConsole_gcce_343.exe");
	}
	protected  static void setSources(String sym, int i) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.numberOfSources = i;
	}
	static {
		// TODO: this differs in Win and Lin
		
		setSources("BlackFlagMinGW.exe", 121);
		setSources("BlackFlag_gcce.sym", 108);
		setSources("BlackFlag_linuxgcc.exe", 139);
		setSources("BlackFlag_rvct.sym", HostOS.IS_WIN32 ? 207 : 172);
		setSources("HelloWorld_rvct_2_2.exe.sym", HostOS.IS_WIN32 ? 327 : 320);
		setSources("HelloWorld_rvct_4_0.exe.sym", 315);
		setSources("QtConsole_gcce_343.sym", 434);
	}
	protected  static void setModuleScopeChilden(String sym, int i) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.numberOfModuleScopeChildren = i;
	}

	static {
		setModuleScopeChilden("BlackFlagMinGW.exe", 29);
		setModuleScopeChilden("BlackFlag_gcce.sym", 27);
		setModuleScopeChilden("BlackFlag_linuxgcc.exe", 25);
		setModuleScopeChilden("BlackFlag_rvct.sym", 693);
		setModuleScopeChilden("HelloWorld_rvct_2_2.exe.sym", 1579);
		setModuleScopeChilden("HelloWorld_rvct_4_0.exe.sym", 1014);
		setModuleScopeChilden("QtConsole_gcce_343.sym", 3);
	}
	
	protected  static void setVariableCount(String sym, int i) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.numberOfVariables = i;
	}
	static {
		setVariableCount("BlackFlagMinGW.exe", 52);
		setVariableCount("BlackFlag_gcce.sym", 105);
		setVariableCount("BlackFlag_linuxgcc.exe", 48);
		setVariableCount("BlackFlag_rvct.sym", 61);
		setVariableCount("HelloWorld_rvct_2_2.exe.sym", 1);
		setVariableCount("HelloWorld_rvct_4_0.exe.sym", 1);
		setVariableCount("QtConsole_gcce_343.sym", 2);
	}
	
	static class VariableInfo {
		String name;
		String typeName;
		
		public VariableInfo(String name, String typeName) {
			this.name = name;
			this.typeName = typeName;
		}
		
	}
	protected  static void setCUVariableInfo(String sym, String cu, String var, String type) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			VariableInfo vi = new VariableInfo(var, type);
			Map<String, VariableInfo> varMap = info.cuVarMap.get(cu);
			if (varMap == null) {
				varMap = new HashMap<String, VariableInfo>();
				info.cuVarMap.put(cu, varMap);
			}
			varMap.put(var, vi);
		}
	}
	static {
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KBase", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KDer1", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KDer2", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KDerDer", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KIFace1", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/dbg_namespaceRealms.h", "KIFace2", "const class TLitC8");
		setCUVariableInfo("BlackFlag_rvct.sym", "M:/sf/os/kernelhwsrv/kernel/eka/compsupp/symaehabi/callfirstprocessfn.cpp", "KLitUser", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "KTxtEPOC32EX", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "KTxtExampleCode", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "KFormatFailed", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "KTxtOK", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "KTxtPressAnyKey", "const class TLitC");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/INC/CommonFramework.h", "console", "class CConsoleBase *");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgchar", "char");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgdouble", "double");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgfloat", "float");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgint", "int");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sglong", "long");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sglongdouble", "long double");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgschar", "SCHAR");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgshort", "short");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgsint", "SINT");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgslong", "SLONG");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgslonglong", "SLONGLONG");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgsshort", "SSHORT");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sguchar", "UCHAR");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sguint", "UINT");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgulong", "ULONG");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgulonglong", "ULONGLONG");
		setCUVariableInfo("BlackFlag_rvct.sym", "/BlackFlag/SRC/dbg_simple_types.cpp", "sgushort", "USHORT");
		setCUVariableInfo("HelloWorld_rvct_2_2.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/inc/ArmTestApplication.h", "KUidArmTestApp", "const class TUid");
		setCUVariableInfo("HelloWorld_rvct_2_2.exe.sym", "/src/cedar/generic/base/e32/compsupp/symaehabi/callfirstprocessfn.cpp", "KLitUser", "const class TLitC");
		setCUVariableInfo("HelloWorld_rvct_2_2.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestAppUi.cpp", "KFileName", "const class TLitC");
		setCUVariableInfo("HelloWorld_rvct_2_2.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestAppUi.cpp", "KText", "const class TLitC");
		setCUVariableInfo("HelloWorld_rvct_4_0.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestApplication.cpp", "KUidArmTestApp", "const struct TUid");
		setCUVariableInfo("HelloWorld_rvct_4_0.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestDocument.cpp", "mylit", "char[5]");
		setCUVariableInfo("HelloWorld_rvct_4_0.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestAppUi.cpp", "KFileName", "const struct TLitC");
		setCUVariableInfo("HelloWorld_rvct_4_0.exe.sym", "/home/eswartz/source/runtime-New_configuration/ArmTest/src/ArmTestAppUi.cpp", "KText", "const struct TLitC");
		setCUVariableInfo("HelloWorld_rvct_4_0.exe.sym", "M:/dev2/sf/os/kernelhwsrv/kernel/eka/compsupp/symaehabi/callfirstprocessfn.cpp", "KLitUser", "const struct TLitC");
		setCUVariableInfo("QtConsole_gcce_343.sym", "/Source/GCCE3/GCCE3/main.cpp", "myGlobalInt", "int");
	}
	
	protected  static void setTypeCount(String sym, int i) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.numberOfTypes = i;
	}
	static {
		setTypeCount("BlackFlagMinGW.exe", 1378);
		setTypeCount("BlackFlag_gcce.sym", 3419);
		setTypeCount("BlackFlag_linuxgcc.exe", 1104);
		setTypeCount("BlackFlag_rvct.sym", 33699);
		setTypeCount("HelloWorld_rvct_2_2.exe.sym", 84681);
		setTypeCount("HelloWorld_rvct_4_0.exe.sym", 31560);
		setTypeCount("QtConsole_gcce_343.sym", 1434);
	}
	
	
	protected  static void setMainBlackFlagFilePath(String sym, String path) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.blackFlagMainFilePath = PathUtils.createPath(path);
	}
	
	static {
		setMainBlackFlagFilePath("BlackFlagMinGW.exe", "C:/wascana/workspace/BlackFlagWascana/src/BlackFlagWascana.cpp");
		setMainBlackFlagFilePath("BlackFlag_gcce.sym", "/BlackFlag/SRC/main.cpp");
		setMainBlackFlagFilePath("BlackFlag_linuxgcc.exe", "/mydisk/myprog/BlackFlag/src/BlackFlagWascana.cpp");
		setMainBlackFlagFilePath("BlackFlag_rvct.sym", "\\BlackFlag\\SRC\\main.cpp");
	}
	
	private static void setSymbolCount(String sym, int i) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.numberOfSymbols = i;
	}
	static {
		setSymbolCount("BlackFlagMinGW.exe", 2520);
		setSymbolCount("BlackFlag_gcce.sym", 2372);
		setSymbolCount("BlackFlag_linuxgcc.exe", 429);
		setSymbolCount("BlackFlag_rvct.sym", 626);
		setSymbolCount("HelloWorld_rvct_2_2.exe.sym", 151);
		setSymbolCount("HelloWorld_rvct_4_0.exe.sym", 227);
		setSymbolCount("QtConsole_gcce_343.sym", 509);
	}
	

	protected  static void setPubCount(String sym, int funcnames, int funcentries, int varnames, int varentries) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			info.numberOfPubFuncNames = funcnames;
			info.numberOfPubFuncEntries = funcentries;
			info.numberOfPubVarNames = varnames;
			info.numberOfPubVarEntries = varentries;
		}
	}
	static {
		setPubCount("BlackFlagMinGW.exe", 209, 241, 52, 52);
		setPubCount("BlackFlag_gcce.sym", 217, 286, 94, 105);
		setPubCount("BlackFlag_linuxgcc.exe", 174, 206, 48, 48);
		setPubCount("BlackFlag_rvct.sym", 100, 101, 51, 87);
		setPubCount("HelloWorld_rvct_2_2.exe.sym", 11, 14, 2, 4);
		setPubCount("HelloWorld_rvct_4_0.exe.sym", 958, 978, 1, 1);
	}
	
	protected static void addPubFuncs(String sym, Object... names) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			for (Object o : names) {
				info.pubFuncs.add(o.toString());
			}
		}
	}
	static {
		addPubFuncs("BlackFlagMinGW.exe", "main", "dbg_watchpoints", "Base01::~Base01");
		addPubFuncs("BlackFlag_gcce.sym", "E32Main", "dbg_watchpoints", "Base01::~Base01");
		addPubFuncs("BlackFlag_gcce_343.sym", "E32Main", "dbg_watchpoints", "Base01::~Base01");
		addPubFuncs("BlackFlag_linuxgcc.exe", "main", "dbg_watchpoints", "Base01::~Base01");
		addPubFuncs("BlackFlag_rvct.sym", "E32Main", "dbg_watchpoints", "globalDestructorFunc");
		addPubFuncs("HelloWorld_rvct_2_2.exe.sym", "E32Main", "CallThrdProcEntry", "CleanupClosePushL");
		addPubFuncs("HelloWorld_rvct_4_0.exe.sym", "E32Main", "CallThrdProcEntry", "CleanupClosePushL");
		addPubFuncs("QtConsole_gcce_343.sym", "main", "__gxx_personality_v0", "__gnu_unwind_frame");
	}
	
	protected static void addPubVars(String sym, Object... names) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			for (Object o : names) {
				info.pubVars.add(o.toString());
			}
		}
	}
	static {
		addPubVars("BlackFlagMinGW.exe", "vgushort", "gulong", "char_wp");
		addPubVars("BlackFlag_gcce.sym",  "vgushort", "gulong", "g_char");
		addPubVars("BlackFlag_gcce_343.sym",  "vgushort", "gulong", "g_char");
		addPubVars("BlackFlag_linuxgcc.exe", "vgushort", "gulong", "gchar");
		addPubVars("BlackFlag_rvct.sym", "vgushort", "gulong", "g_char");
		addPubVars("HelloWorld_rvct_2_2.exe.sym", "mylit");
		addPubVars("HelloWorld_rvct_4_0.exe.sym", "mylit");
		addPubVars("QtConsole_gcce_343.sym", "myGlobalInt");
	}
	
	@Test
	public void testSymFromExeDetect() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (info.exeFile == null) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.exeFile);
			assertNotNull(symbolReader);
			System.out.println("Sym for exe " + info.exeFile + " is " + symbolReader.getSymbolFile());
			assertEquals(info.symFile, symbolReader.getSymbolFile());
		}
	}
	
	@Before
	public void setup() throws Exception {
		// each test relies on starting from scratch
		Symbols.releaseReaderCache();
	}
	
	/**
	 * This should be a quick check, not a slow one
	 * @throws Exception
	 */
	@Test
	public void testSymDetect() throws Exception {
		long time = System.currentTimeMillis();
		_testSymDetect();
		long span = System.currentTimeMillis() - time;
		System.out.println(span + " ms (testSymDetect)");
	}

	private void _testSymDetect() {
		for (TestInfo info : testInfos.values()) {
			System.out.println("Checking sym for " + info.symFile);
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			assertNotNull(info.symFile.toString(), symbolReader);
			assertTrue(info.symFile.toString(), symbolReader.hasRecognizedDebugInformation());
		}
	}
	
	@Test
	public void testSourceFiles() throws Exception {
		long time = System.currentTimeMillis();
		_testSourceFiles();
		long span = System.currentTimeMillis() - time;
		System.out.println(span + " ms (testSourceFiles)");
	}

	private void _testSourceFiles() {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfSources == 0) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			String[] sources=  symbolReader.getSourceFiles();
			assertNotNull(sources);
			//System.out.println(info.symFile.toString() + " : " + sources.length);
			assertEquals(info.symFile.toString(), info.numberOfSources, sources.length);
		}
	}

	@Test
	public void testCompileUnits() throws Exception {
		long time = System.currentTimeMillis();
		_testCompileUnits();
		long span = System.currentTimeMillis() - time;
		System.out.println(span + " ms (testCompileUnits)");
	}
	
	private void _testCompileUnits() {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfModuleScopeChildren == 0) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			Collection<IScope> scopes=  symbolReader.getModuleScope().getChildren();
			assertNotNull(scopes);
			//System.out.println(info.symFile.toString() + ": " + scopes.size());
			//for (IScope kid : scopes)
			//	System.out.println("\t" + kid.getName());
			assertEquals(info.symFile.toString(), info.numberOfModuleScopeChildren, scopes.size());
		}
	}

	@Test
	public void testGlobalVariables() throws Exception {
		long time = System.currentTimeMillis();
		_testGlobalVariables();
		long span = System.currentTimeMillis() - time;
		System.out.println(span + " ms (testGlobalVariables)");
	}
	
	private void _testGlobalVariables() {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfVariables == 0) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			Collection<IVariable> variables = symbolReader.getModuleScope().getVariables();
			assertNotNull(variables);
			//System.out.println(info.symFile.toString() + ": " + variables.size());
			assertEquals(info.symFile.toString(), info.numberOfVariables, variables.size());
		}
	}
	
	/**
	 * Test that we can find and get types for globals in compilation units
	 * @throws Exception
	 */
 	@Test
	public void testPerCUGlobals() throws Exception {
		long time = System.currentTimeMillis();
		_testPerCUGlobals();
		long span = System.currentTimeMillis() - time;
		System.out.println(span + " ms (testPerCUGlobals)");
	}
	
	private void _testPerCUGlobals() {
		for (TestInfo info : testInfos.values()) {
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
			boolean discover = false;
			
			if (discover) {
				// DISCOVERY (rerun if reader gets better and paste into static block at top) 
				//System.out.println(info.symFile);
				boolean any = false;
				for (String srcFile : symbolReader.getSourceFiles()) {
					List<ICompileUnitScope> cusList = symbolReader.getModuleScope().getCompileUnitsForFile(PathUtils.createPath(srcFile));
					any = true;
					for (ICompileUnitScope cus : cusList) {
						Collection<IVariable> vars = cus.getVariables();
						if (!vars.isEmpty()) {
							//System.out.println(srcFile +" : # vars = " + vars.size());
							for (IVariable var : vars) {
								//System.out.println(var.getName() + " : " + getTypeName(var.getType()));
								System.out.println(MessageFormat.format(
										"setCUVariableInfo(\"{0}\", \"{1}\", \"{2}\", \"{3}\");",
										info.symFile.lastSegment(),
										srcFile,
										var.getName(),
										getTypeName(var.getType())));
							}
						}
					}
				}
				assertTrue("Any CUs in " + info.symFile, any);
			} else {
				if (info.cuVarMap == null)
					continue;

				for (Map.Entry<String, Map<String, VariableInfo>> entry : info.cuVarMap.entrySet()) {
					String cu = entry.getKey();
					List<ICompileUnitScope> cusList = symbolReader.getModuleScope().getCompileUnitsForFile(PathUtils.createPath(cu));
					assertNotNull(info.symFile + " : " + cu, cusList);
					for (Map.Entry<String, VariableInfo> varEntry : entry.getValue().entrySet()) {
						// TODO: getter by name
						boolean found = false;
						for (ICompileUnitScope cus : cusList) {
							for (IVariable var : cus.getVariables()) {
								if (var.getName().equals(varEntry.getKey())) {
									found = true;
									VariableInfo varInfo = varEntry.getValue();
									assertNotNull(var.getType());
									String theTypeName = getTypeName(var.getType());
									System.out.println(info.symFile + " : " + cu + " : " + var.getName() + " = " + theTypeName); 
									assertEquals(info.symFile + " : " + cu + " : " + var.getName(),
											varInfo.typeName, theTypeName);
								}
							}
						}
						assertTrue(info.symFile + " : " + cu + " : " + entry.getKey(), found);
					}
				}
			}
		}
	}

	private String getTypeName(IType type) {
		return TypeUtils.getFullTypeName(type);
	}

	/**
	 * Test that we can find and resolve all types.  The lazy type evaluator only
	 * is lazy as far as dereferenced types go, so we don't check anything but names
	 * and type trees here.
	 * 
	 * This test isn't exhaustive; it just ferrets out assertion errors and null pointer references.
	 * @throws Exception
	 */
	@Test
	public void testTypes() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfTypes == 0) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
			boolean discover = false;
			
			if (discover) {
				// DISCOVERY 
				//System.out.println(info.symFile);
				int cnt = symbolReader.getModuleScope().getTypes().size();
				//for (IType type : symbolReader.getModuleScope().getTypes()) {
				//	System.out.println(getTypeName(type));
				//}
				System.out.println(info.symFile + " : " + cnt);
			}
			else {
				Collection<IType> types = symbolReader.getModuleScope().getTypes();
				assertNotNull(types);
				System.out.println("Initial: " + types.size());
				
				// this should trigger expansion of subtypes
				int idx = 0;
				for (IType type : types) {
					doTestType(idx, type);
				}
				
				types = symbolReader.getModuleScope().getTypes();
				System.out.println("After: " + types.size());
				assertEquals(info.symFile.toString(), info.numberOfTypes, types.size());
				
				// now, ensure the types are still there in a new reader
				symbolReader = Symbols.getSymbolReader(info.symFile);
				assertEquals(info.symFile.toString(), info.numberOfTypes, symbolReader.getModuleScope().getTypes().size());
			}
		}
	}

	/**
	 * @param idx
	 * @param type
	 */
	private void doTestType(int idx, IType type) {
		String name = getTypeName(type);
		idx++;
		if (type.getByteSize() == 0) {
			IType checkType = type;
			while (checkType != null) {
				if (checkType instanceof IQualifierType || checkType instanceof TypedefType)
					checkType = checkType.getType();
				else
					break;
			}
			if (checkType == null)
				return;
			if (checkType instanceof ICompositeType) 
				return; // this is allowed, even though the spec says it should be here.
						// we can't fix it up, because even if we sum up the field sizes, we can't predict the extra space used by alignment
			if (checkType instanceof SubroutineType || checkType instanceof InheritanceType)
				return; // this is allowed
			
			//System.out.println(name);
			
			if (checkType instanceof FieldType)
				checkType = ((FieldType) checkType).getType();
			if (checkType instanceof ICompositeType) 
				return; // this is allowed, even though the spec says it should be here.
						// we can't fix it up, because even if we sum up the field sizes, we can't predict the extra space used by alignment
			if (checkType instanceof ArrayType) {
				for (IArrayBoundType bound : ((ArrayType) checkType).getBounds()) {
					if (bound.getElementCount() == 0)
						return;
				}
				// else, should have more
			}
				
			if (checkType == DwarfDebugInfoProvider.ForwardTypeReference.NULL_TYPE_ENTRY)
				return; // should not get here, but something else tests this
			if (checkType instanceof ICPPBasicType && ((ICPPBasicType) checkType).getBaseType() == ICPPBasicType.t_void)
				return; // yup
			if (checkType instanceof ITemplateParam)
				return; // no inherent size
			if (checkType instanceof IArrayBoundType)
				return; // no inherent size
			fail(name + " has zero size");
		}
		if (idx % 1000 == 0) System.out.print(".");
	}

	/**
	 * This method is useful for dumping the types actually parsed and comparing them with each other.
	 */
	/* 
	public void testTypesXX() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (!info.symFile.lastSegment().contains("gcce"))
				continue;
			
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
			Collection<IType> types = symbolReader.getModuleScope().getTypes();
			assertNotNull(types);
			
			// force expansion
			for (IType type : types) {
				//String name = 
				getTypeName(type);
			}
			
			// start again
			types = symbolReader.getModuleScope().getTypes();
			List<String> names = new ArrayList<String>();
			for (IType type : types) {
				names.add(getTypeName(type));
			}
			//Collections.sort(names);
			String fname = "true".equals(System.getProperty(Symbols.DWARF_USE_NEW_READER)) ? "new.txt" : "old.txt";
			PrintStream dump = new PrintStream(new File("/tmp/" + fname));
			for (String name : names) {
				dump.println(name);
			}
			dump.close();
		}
	}
	*/
	
	/**
	 * Get a low-level DWARF reader for the symbol reader for testing
	 * @param symbolReader
	 * @return
	 */
	private DwarfDebugInfoProvider getDwarfDebugInfoProvider(IEDCSymbolReader symbolReader) {
		
		if (!(symbolReader instanceof EDCSymbolReader)) 
			return null;
		
		IDebugInfoProvider debugInfoProvider = ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
		if (!(debugInfoProvider instanceof DwarfDebugInfoProvider))
			return null;
		
		DwarfDebugInfoProvider dip = (DwarfDebugInfoProvider) debugInfoProvider;
		
		// do initial parse (so forward types are detected)
		DwarfInfoReader reader = new DwarfInfoReader(dip);
		dip.setParsedInitially();
		reader.parseInitial();
		dip.setParsedForAddresses();
		reader.parseForAddresses(true);
		
		return dip;
	}


	/**
	 * Test case(s) for specific type parses
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes1a() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		List<ICompileUnitScope> cuList = getCompileUnitsFor(symbolReader, "dbg_multipleInheritance.cpp");
		if (cuList.isEmpty())
			cuList = getCompileUnitsFor(symbolReader, "dbg_multipleinheritance.cpp");
		assertFalse(cuList.isEmpty());
		List<ICompileUnitScope> cuhList = getCompileUnitsFor(symbolReader, "dbg_multipleInheritance.h");
		assertFalse(cuhList.isEmpty());
		
		// multipleInheritance
		IFunctionScope function = null;
		for (ICompileUnitScope cu : cuList) {
			function = cu.getFunctionAtAddress(new Addr32(0xa940));
			if (function != null)
				break;
		}
		assertNotNull(function);
		assertEquals("multipleInheritance", function.getName());
		for (IVariable var : function.getVariablesInTree()) {
			if (var.getName().equals("pdv1")) {
				IType type = var.getType();
				assertFalse(type instanceof IForwardTypeReference);	// should not peek through interface
				assertNotNull(type);
				assertEquals(function, var.getScope());
				_testSpecificTypePointerToDerv1(type);
			}
		}
	}

	/**
	 * Test case(s) for specific type parses, from a focused parse
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes1b() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		DwarfDebugInfoProvider provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		// DW_TAG_pointer_type -> Derv1
		IType type = provider.readType(0x1cb139);
		_testSpecificTypePointerToDerv1(type);

	}
	/**
	 * @param function 
	 * @param headerScope 
	 * @param var
	 */
	private void _testSpecificTypePointerToDerv1(IType type) {
		assertTrue(type.getName(), type instanceof IPointerType);
		
		IType derv1 = ((IPointerType) type).getType();
		assertFalse(derv1 instanceof IForwardTypeReference);	// should not peek through interface
		assertNotNull(derv1);
		assertTrue(derv1.getName(), derv1 instanceof ICompositeType);
		
		ICompositeType derv1Comp = (ICompositeType) derv1;
		assertEquals("class Derv1", derv1Comp.getName());
		assertEquals(1, derv1Comp.fieldCount());
		assertEquals(2, derv1Comp.inheritanceCount());
		IField vptrField = derv1Comp.findFields("__vptr")[0];
		assertNotNull("__vptr", vptrField);
		
		// inherited fields also visible
		IField xField = derv1Comp.findFields("x")[0];
		assertNotNull("x", xField);
		
		// x is in an inherited type
		IInheritance[] inhs = derv1Comp.getInheritances();
		assertEquals(2, inhs.length);
		
		IType base1 = inhs[0].getType();
		assertNotNull(base1);
		assertEquals("class Base1", base1.getName());
		assertTrue(base1.getName(), base1 instanceof ICompositeType);
		
		ICompositeType base1Comp = (ICompositeType) base1;
		assertEquals(1, base1Comp.fieldCount());
		assertEquals(0, base1Comp.inheritanceCount());
		xField = base1Comp.findFields("x")[0];
		assertNotNull("x", xField);
		
		IType base2 = inhs[1].getType();
		assertNotNull(base2);
		assertEquals("class Base2", base2.getName());
		assertTrue(base2.getName(), base2 instanceof ICompositeType);
		
		ICompositeType base2Comp = (ICompositeType) base2;
		assertEquals(0, base2Comp.fieldCount());
		assertEquals(0, base2Comp.inheritanceCount());
		
		// watch for side effects (late adding of inherited fields)
		assertEquals(1, derv1Comp.fieldCount());
		assertEquals(2, derv1Comp.inheritanceCount());
		assertEquals(1, base1Comp.fieldCount());
		assertEquals(0, base1Comp.inheritanceCount());
		assertEquals(0, base2Comp.fieldCount());
		assertEquals(0, base2Comp.inheritanceCount());
		

		// the class is in the header
		IScope classScope = ((IPointerType)type).getType().getScope();
		assertTrue(classScope instanceof ICompileUnitScope);
		IPath path = ((ICompileUnitScope) classScope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equalsIgnoreCase("dbg_multipleInheritance.h"));

		// the pointer type is declared in a function (either "show3", due to "this", or the one we looked in)
		assertTrue(type.getScope() instanceof IFunctionScope);
		//assertEquals(((IFunctionScope) type.getScope()).getName(), "show3");
		IScope fileScope = type.getScope().getParent();
		assertTrue(fileScope instanceof ICompileUnitScope);
		path = ((ICompileUnitScope) fileScope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equalsIgnoreCase("dbg_multipleInheritance.cpp"));
	}
	

	/**
	 * Test case(s) for specific type parses
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes2() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		List<ICompileUnitScope> cuList = getCompileUnitsFor(symbolReader, "dbg_multipleInheritance.cpp");
		if (cuList.isEmpty())
			cuList = getCompileUnitsFor(symbolReader, "dbg_multipleinheritance.cpp");
		assertFalse(cuList.isEmpty());
		List<ICompileUnitScope> cuhList = getCompileUnitsFor(symbolReader, "dbg_multipleInheritance.h");
		assertFalse(cuhList.isEmpty());
		
		// Base2::show2
		IFunctionScope function = null;
		for (ICompileUnitScope cu : cuList) {
			function = cu.getFunctionAtAddress(new Addr32(0xa916));
			if (function != null)
				break;
		}
		if (function == null)
			for (ICompileUnitScope cu : cuhList) {
			function = cu.getFunctionAtAddress(new Addr32(0xa916));
			if (function != null)
				break;
		}

		assertNotNull(function);
		assertEquals("show2", function.getName());
		for (IVariable var : function.getVariablesInTree()) {
			if (var.getName().equals("Base2Show"))
				_testSpecificTypeCharArray(function, cuhList.get(0), var);
		}
	}
	/**
	 * @param function 
	 * @param headerScope 
	 * @param var
	 */
	private void _testSpecificTypeCharArray(IFunctionScope function, ICompileUnitScope headerScope, IVariable var) {
		IType type = var.getType();
		assertFalse(type instanceof IForwardTypeReference);	// should not peek through interface
		assertNotNull(type);
		assertTrue(type.getName(), type instanceof IArrayType);
		assertEquals(function, var.getScope());
		
		IType baseType = ((IArrayType) type).getType();
		assertNotNull(baseType);
		assertTrue(baseType.getName(), baseType instanceof IBasicType);
		
		// FIXME: this should be null for a primitive type, but is the previous function in the old reader
		//assertEquals(randomFunction, baseType.getScope());		

		IBasicType charType = (IBasicType) baseType;
		assertEquals("char", charType.getName());
		assertEquals(1, charType.getByteSize());
	}
	
	/**
	 * Test case(s) for specific type parses, from the whole module scope
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes3a() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlagMinGW.exe"));
		List<ICompileUnitScope> cuList = getCompileUnitsFor(symbolReader, "dbg_expressions.cpp");
		
		// TODO: lookup by name
		for (ICompileUnitScope cu : cuList) {
			for (IVariable var : cu.getVariables()) {
				if (var.getName().equals("genum")) {
					assertEquals(cu, var.getScope());
					_testSpecificTypeEnum(var.getType(), "dbg_typedefs.h");
					break;
				}
			}
		}
		
		////////
		
		symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		cuList = getCompileUnitsFor(symbolReader, "dbg_typedefs.h");
		
		// TODO: lookup by name
		for (ICompileUnitScope cu : cuList) {
			for (IVariable var : cu.getVariables()) {
				if (var.getName().equals("genum")) {
					assertEquals(cu, var.getScope());
					_testSpecificTypeEnum(var.getType(), "dbg_typedefs.h");
					break;
				}
			}
		}
	}
	
	/**
	 * Test case(s) for specific type parses, from a focused scope
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes3b() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlagMinGW.exe"));
		DwarfDebugInfoProvider provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		// DW_TAG_enumeration_type : enum_type
		IType type = provider.readType(0x6a65);
		_testSpecificTypeEnum(type, "dbg_expressions.cpp");
		
		///////
		
		// defined in different place here
		symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		// DW_TAG_enumeration_type : enum_type
		type = provider.readType(0x1a6a3d);
		_testSpecificTypeEnum(type, "dbg_typedefs.h");
	}

	private void _testSpecificTypeEnum(IType type, String file) {
		assertTrue(getTypeName(type), type instanceof IEnumeration);
		IEnumeration enumType = (IEnumeration) type;
		assertEquals("enum_type", enumType.getName());
		assertEquals(5, enumType.enumeratorCount());
		
		assertEquals("zero", enumType.getEnumerators()[0].getName());
		assertEquals("one", enumType.getEnumerators()[1].getName());
		assertEquals("two", enumType.getEnumerators()[2].getName());
		assertEquals("three", enumType.getEnumerators()[3].getName());
		assertEquals("four", enumType.getEnumerators()[4].getName());
		
		IScope scope = type.getScope();
		assertTrue(scope instanceof ICompileUnitScope);
		ICompileUnitScope cus = (ICompileUnitScope) scope;
		assertTrue(cus.getFilePath().toString(), cus.getFilePath().lastSegment().equals(file));
	}

	@Test
	public void testStaticLocals1() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (info.blackFlagMainFilePath != null) {
				IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
				List<ICompileUnitScope> cuList = getCompileUnitsFor(symbolReader, info.blackFlagMainFilePath.lastSegment());
				assertFalse(cuList.isEmpty());
				
				for (ICompileUnitScope cu : cuList) {
					for (IFunctionScope func : cu.getFunctions()) {
						if (func.getName().equals("doExampleL")) {
							for (IVariable var : func.getVariablesInTree()) {
								if (var.getName().equals("KHelloWorldText")) {
									IScope vScope = var.getScope();
									if (vScope instanceof ILexicalBlockScope)
										vScope = vScope.getParent();
									assertEquals(func, vScope);
									
									_testStaticLocal(var.getType());
								}
							}
						}
					}
				}
			}
		}
	}


	@Test
	public void testStaticLocals2() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_gcce.sym"));
	
		DwarfDebugInfoProvider provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		IType type = provider.readType(0x7e11);
		_testStaticLocal(type);
		
		///////////
		
		symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		
		provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		type = provider.readType(0x1dc89a);
		_testStaticLocal(type);

	}

	/**
	 * @param func 
	 * @param scope
	 */
	private void _testStaticLocal(IType type) {
		assertTrue(getTypeName(type), type instanceof ConstType);
		type = type.getType();
		assertTrue(getTypeName(type), type instanceof ICompositeType);
		ICompositeType comp = (ICompositeType) type;
		// differs in RVCT and GCCE
		assertTrue(getTypeName(type), "struct TLitC<14>".equals(comp.getName())
				|| "class TLitC".equals(comp.getName()));
		
		assertEquals(2, comp.fieldCount());
		assertEquals(0, comp.inheritanceCount());
		
		IField f = comp.getFields()[0];
		assertEquals("iTypeLength", f.getName());
		assertTrue(getTypeName(f.getType()), f.getType() instanceof ITypedef);
		type = f.getType().getType();
		assertTrue(getTypeName(type), type instanceof IBasicType);
		
		f = comp.getFields()[1];
		assertEquals("iBuf", f.getName());
		assertTrue(getTypeName(f.getType()), f.getType() instanceof IArrayType);
		IArrayType arr = (IArrayType) f.getType();
		assertNotNull(arr.getBounds());
		assertEquals(1, arr.getBoundsCount());
		IArrayBoundType bound = arr.getBound(0);
		assertEquals(0, bound.getDimensionIndex());
		assertEquals(14, bound.getBoundCount());
		assertEquals(1, bound.getElementCount());
		type = arr.getType();
		assertTrue(getTypeName(type), type instanceof ITypedef);
		type = type.getType();
		assertTrue(getTypeName(type), type instanceof ICPPBasicType);
		
	}
	
	/**
	 * Discovered while testing GCC-E
	 * @throws Exception
	 */
	@Test
	public void testSpecificTypes4() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_gcce.sym"));
		
		DwarfDebugInfoProvider provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		IType type = provider.readType(0x36104);
		_testSpecificTypeGCCEDerv1(type);
	}
	
	/**
	 * @param function 
	 * @param headerScope 
	 * @param var
	 */
	private void _testSpecificTypeGCCEDerv1(IType type) {
		assertTrue(type.getName(), type instanceof ICompositeType);
		
		ICompositeType derv1Comp = (ICompositeType) type;
		assertEquals("struct Derv1", derv1Comp.getName());
		assertEquals(1, derv1Comp.fieldCount());
		assertEquals(2, derv1Comp.inheritanceCount());
		IField vptrField = derv1Comp.findFields("_vptr$Derv1")[0];	// renamed by parser to avoid having an invalid expression-like field
		assertNotNull("_vptr$Derv1", vptrField);
		
		// inherited fields also visible
		IField xField = derv1Comp.findFields("x")[0];
		assertNotNull("x", xField);
		
		// x is in an inherited type
		IInheritance[] inhs = derv1Comp.getInheritances();
		assertEquals(2, inhs.length);
		
		IType base1 = inhs[0].getType();
		assertNotNull(base1);
		assertEquals("struct Base1", base1.getName());
		assertTrue(base1.getName(), base1 instanceof ICompositeType);
		
		ICompositeType base1Comp = (ICompositeType) base1;
		assertEquals(1, base1Comp.fieldCount());
		assertEquals(0, base1Comp.inheritanceCount());
		xField = base1Comp.findFields("x")[0];
		assertNotNull("x", xField);
		
		IType base2 = inhs[1].getType();
		assertNotNull(base2);
		assertEquals("struct Base2", base2.getName());
		assertTrue(base2.getName(), base2 instanceof ICompositeType);
		
		ICompositeType base2Comp = (ICompositeType) base2;
		assertEquals(0, base2Comp.fieldCount());
		assertEquals(0, base2Comp.inheritanceCount());
		
		// watch for side effects (late adding of inherited fields)
		assertEquals(1, derv1Comp.fieldCount());
		assertEquals(2, derv1Comp.inheritanceCount());
		assertEquals(1, base1Comp.fieldCount());
		assertEquals(0, base1Comp.inheritanceCount());
		assertEquals(0, base2Comp.fieldCount());
		assertEquals(0, base2Comp.inheritanceCount());
		

		IScope scope = type.getScope();
		assertTrue(scope instanceof ICompileUnitScope);
		IPath path = ((ICompileUnitScope) scope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equalsIgnoreCase("dbg_multipleInheritance.cpp"));
	}
	
	/**
	 * Test that we do not have multiple entries for the same symbol with a zero size.
	 * @throws Exception
	 */
	@Test
	public void testSymbols() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfSymbols == 0) continue;
			IEDCSymbolReader reader = Symbols.getSymbolReader(info.symFile);
			Collection<ISymbol> symbols = reader.getSymbols();
			int numSymbols = symbols.size();
			//System.out.println(info.symFile.lastSegment() + " : " + numSymbols);
			assertEquals(info.symFile.lastSegment(), info.numberOfSymbols, numSymbols);
			Map<IAddress, ISymbol> zeroSymbols = new HashMap<IAddress, ISymbol>();
			for (ISymbol symbol : symbols) {
				if (symbol.getSize() == 0) {
					// you may have more than one zero-sized symbol at the same address
					// in ARM.  But they should not be the same mode, e.g. both ARM or
					// both Thumb.
					if (symbol instanceof IARMSymbol) {
						IARMSymbol sameAddress = (IARMSymbol)zeroSymbols.get(symbol.getAddress());
						if (sameAddress != null) {
							assertFalse(((IARMSymbol)symbol).isThumbAddress() == sameAddress.isThumbAddress());
						}
					} else {
						assertFalse(symbol.getName(), zeroSymbols.containsKey(symbol.getAddress()));
					}
					zeroSymbols.put(symbol.getAddress(), symbol);
				}
			}
		}
	}

	/**
	 * Test some type lookup edge cases
	 */
	@Test
	public void testSpecificTypes4a() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		
		List<ICompileUnitScope> scopes = getCompileUnitsFor(symbolReader, "dbg_multipleInheritance.cpp");
		if (scopes.isEmpty())
			scopes = getCompileUnitsFor(symbolReader, "dbg_multipleinheritance.cpp");
		assertFalse(scopes.isEmpty());
		/*
		if (Symbols.useNewReader())
			// TODO: along with other filepath lookup issues:
			// what happens here is, there are three CUs for dbg_multipleInheritance.cpp,
			// but one has no DW_AT_comp_dir.  All of their names are \BlackFlags\SRC\dbg_multipleInheritance.cpp,
			// which (once canonicalized) looks like an absolute path in Linux, thus comes in as
			// three distinct CUs for the same path.  In Win32, though, the comp dir is prepended
			// in two cases, since the name is not considered absolute.
			assertEquals(HostOS.IS_WIN32 ? 2 : 3, scopes.size());		
		else
			assertEquals(1, scopes.size());
		*/
		
		IFunctionScope functionScope = null;
		for (ICompileUnitScope scope : scopes) {
			functionScope = scope.getFunctionAtAddress(new Addr32(0xaa4e));
			if (functionScope != null)
				break;
		}
		assertNotNull(functionScope);
		
		Collection<IVariable> vars = functionScope.getVariablesInTree();
		assertEquals(2, vars.size());
		
		java.util.Iterator<IVariable> vit = vars.iterator();
		assertEquals("FromBase2", vit.next().getName());
		assertEquals("a", vit.next().getName());
		
		vars = functionScope.getParameters();
		assertEquals(1, vars.size());
		vit = vars.iterator();
		IVariable thisVar = vit.next();
		assertEquals("this", thisVar.getName());
		
		assertNotNull(thisVar.getType());
		_testSpecificTypes4(thisVar.getType());
	}

	/**
	 * Test some type lookup edge cases
	 */
	@Test
	public void testSpecificTypes4b() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		
		DwarfDebugInfoProvider provider = getDwarfDebugInfoProvider(symbolReader);
		if (provider == null)
			return;
		
		// 0x1cb0c6: subprogram
		// 0x1cb0de: this
		// 0x1cb098: Base2*
		
		IType type = provider.readType(0x1cb098);
		assertNotNull(type);
		_testSpecificTypes4(type);
	}

	/**
	 * @param type
	 */
	private void _testSpecificTypes4(IType type) {
		assertTrue(type.getName(), type instanceof IPointerType);
		
		IType base2 = ((IPointerType) type).getType();
		assertFalse(base2 instanceof IForwardTypeReference);	// should not peek through interface
		assertNotNull(base2);
		assertTrue(base2.getName(), base2 instanceof ICompositeType);
		
		ICompositeType base2Comp = (ICompositeType) base2;
		assertEquals("class Base2", base2Comp.getName());
		assertEquals(0, base2Comp.fieldCount());
		assertEquals(0, base2Comp.inheritanceCount());

		// the class is in the header
		IScope classScope = ((IPointerType)type).getType().getScope();
		assertTrue(classScope instanceof ICompileUnitScope);
		IPath path = ((ICompileUnitScope) classScope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equalsIgnoreCase("dbg_multipleInheritance.h"));

		// the pointer type is declared in a function
		assertTrue(type.getScope() instanceof IFunctionScope);
		IScope fileScope = type.getScope().getParent();
		assertTrue(fileScope instanceof ICompileUnitScope);
		path = ((ICompileUnitScope) fileScope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equalsIgnoreCase("dbg_multipleInheritance.cpp"));
	}
	
	/**
	 * Test some type lookup edge cases
	 */
	@Test
	public void testSpecificTypes5a() throws Exception {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_rvct.sym"));
		
		// inlined Der1::Der1()
		IScope scope = symbolReader.getModuleScope().getScopeAtAddress(new Addr32(0x8cda));
		assertTrue(scope instanceof IFunctionScope);
		
		IFunctionScope functionScope = (IFunctionScope) scope;

		// two locals __func_local__0 and __result are optimized out
		Collection<IVariable> vars = functionScope.getVariablesInTree();
		assertEquals(0, vars.size());
		
		java.util.Iterator<IVariable> vit;
		/*
		vit = vars.iterator();
		IVariable var = vit.next();
		assertEquals("__func_local__0", var.getName());
		var = vit.next();
		assertEquals("__result", var.getName());
		*/
		
		vars = functionScope.getParameters();
		assertEquals(1, vars.size());
		vit = vars.iterator();
		IVariable thisVar = vit.next();
		assertEquals("this", thisVar.getName());
		
		assertNotNull(thisVar.getType());
		_testSpecificTypes5(thisVar.getType());
	}
	

	/**
	 * @param type
	 */
	private void _testSpecificTypes5(IType type) {
		assertTrue(type.getName(), type instanceof IPointerType);
		
		IType der1 = ((IPointerType) type).getType();
		assertFalse(der1 instanceof IForwardTypeReference);	// should not peek through interface
		assertNotNull(der1);
		assertTrue(der1.getName(), der1 instanceof ICompositeType);
		
		ICompositeType der1Comp = (ICompositeType) der1;
		assertEquals("struct Der1", der1Comp.getName());
		assertEquals(1, der1Comp.fieldCount());
		assertEquals(1, der1Comp.inheritanceCount());

		IField field = der1Comp.findFields("b1")[0];
		assertNotNull("b1", field);

		field = der1Comp.findFields("__vptr")[0];
		assertNotNull("__vptr", field);

		// inherited fields also visible
		field = der1Comp.findFields("a")[0];
		assertNotNull("a", field);
		
		// x is in an inherited type
		IInheritance[] inhs = der1Comp.getInheritances();
		assertEquals(1, inhs.length);
		
		IType base01 = inhs[0].getType();
		assertNotNull(base01);
		assertEquals("struct Base01", base01.getName());
		assertTrue(base01.getName(), base01 instanceof ICompositeType);
		
		ICompositeType base01Comp = (ICompositeType) base01;
		assertEquals(2, base01Comp.fieldCount());
		assertEquals(0, base01Comp.inheritanceCount());
		
		field = base01Comp.findFields("a")[0];
		assertNotNull("a", field);
		

		field = der1Comp.findFields("__vptr")[0];
		assertNotNull("__vptr", field);
		
		// the class is in the header
		IScope classScope = ((IPointerType)type).getType().getScope();
		assertTrue(classScope instanceof ICompileUnitScope);
		IPath path = ((ICompileUnitScope) classScope).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equals("dbg_rtti.cpp"));

		// the pointer type is declared in a function
		assertTrue(type.getScope() instanceof ICompileUnitScope);
		path = ((ICompileUnitScope) type.getScope()).getFilePath();
		assertTrue(path.toString(), path.lastSegment().equals("dbg_rtti.cpp"));
	}
	

	/**
	 * Look for DWARF files with bad scopes and make sure we fix them up properly.
	 * Use full scanning to populate the content.
	 */
	@Test
	public void testScopes1a() {
		for (TestInfo info : testInfos.values()) {
			System.out.println("Scopes for " + info.symFile.lastSegment());
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			if (symbolReader != null) {
				readFully(symbolReader);
				doTestScopes(symbolReader);
			}
		}
	}
	
	/**
	 * @param symbolReader
	 */
	private void readFully(IEDCSymbolReader symbolReader) {
		IModuleScope moduleScope = symbolReader.getModuleScope();
		moduleScope.getChildren();
		moduleScope.getVariablesByName(null, false);
		moduleScope.getFunctionsByName(null);
	}

	/**
	 * Look for DWARF files with bad scopes and make sure we fix them up properly.
	 * Use random scanning to populate the content.
	 */
	@Test
	public void testScopes1b() {
		for (TestInfo info : testInfos.values()) {
			System.out.println("Scopes for " + info.symFile.lastSegment());
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			if (symbolReader != null) {
				readRandomly(symbolReader);
				doTestScopes(symbolReader);
			}
		}
	}

	/**
	 * @param symbolReader
	 */
	private void readRandomly(IEDCSymbolReader symbolReader) {
		IModuleScope moduleScope = symbolReader.getModuleScope();
		IAddress low = moduleScope.getLowAddress();
		IAddress high = moduleScope.getHighAddress();
		
		long range = high.getValue().subtract(low.getValue()).longValue();
		assertTrue(range > 0);
		
		Random random = new Random(0x120044ff);
		
		for (int cnt = 0; cnt < 1000; cnt++) {
			switch (random.nextInt() % 4) {
			case 0: {
				IAddress addr = low.add(random.nextLong() % range);
				moduleScope.getScopeAtAddress(addr);
				break;
			}
			case 1: {
				ICompileUnitScope scope = getRandomCU(symbolReader, random, true);
				if (scope != null) {
					long curange = scope.getHighAddress().getValue().subtract(scope.getLowAddress().getValue()).longValue();
					IAddress addr = scope.getLowAddress().add(random.nextLong() % curange);
					scope.getFunctionAtAddress(addr);
				}
				break;
			}
			case 2: {
				ICompileUnitScope scope = getRandomCU(symbolReader, random, false);
				if (scope != null) {
					scope.getVariables();
				}
				break;
			}
			case 3: {
				ICompileUnitScope scope = getRandomCU(symbolReader, random, false);
				if (scope != null) {
					scope.getEnumerators();
				}
				break;
			}
			}
		}
	}
		
	/**
	 * @param symbolReader
	 * @return
	 */
	private ICompileUnitScope getRandomCU(IEDCSymbolReader symbolReader, Random random, boolean withCode) {
		String[] srcs = symbolReader.getSourceFiles();
		int tries = 10;
		Collection<ICompileUnitScope> scopes = null;
		while (tries-- > 0) {
			IPath src = PathUtils.createPath(srcs[(random.nextInt() & 0xfffff) % srcs.length]);
			scopes = symbolReader.getModuleScope().getCompileUnitsForFile(src);
			if (!scopes.isEmpty())
				break;
		}
		if (scopes == null)
			return null;
		
		ICompileUnitScope last = null;
		for (ICompileUnitScope scope : scopes) {
			if (withCode) {
				long curange = scope.getHighAddress().getValue().subtract(scope.getLowAddress().getValue()).longValue();
				if (curange > 0) {
					last = scope;
					if (random.nextBoolean()) {
						return scope;
					}
				}
			}
			else if (random.nextInt(5) == 0) {
				return scope;
			}
		}
		return last;
	}

	/**
	 * @param symbolReader
	 */
	private void doTestScopes(IEDCSymbolReader symbolReader) {
		IModuleScope moduleScope = symbolReader.getModuleScope();
		IAddress low = moduleScope.getLowAddress();
		IAddress high = moduleScope.getHighAddress();
		assertTrue(low.compareTo(high) < 0);
		
		checkChildScopes(moduleScope, low, high);
	}

	/**
	 * @param moduleScope
	 * @param low
	 * @param high
	 */
	private void checkChildScopes(IScope scope, IAddress low,
			IAddress high) {
		for (IScope kid : scope.getChildren()) {
			IAddress kidlo = kid.getLowAddress();
			IAddress kidhi = kid.getHighAddress();
			
			if (!kid.hasEmptyRange()) {
				if (!(kidlo.compareTo(kidhi) <= 0)) {
					fail(describeScope(kid));
				}
				if (!(low.compareTo(kidlo) <= 0)) {
					fail(describeScope(kid));
				}
				if (!(kidhi.compareTo(high) <= 0)) {
					fail(describeScope(kid));
				}
			}	
			
			if (!(kid instanceof ILexicalBlockScope) && !(scope instanceof ILexicalBlockScope)) {
				checkChildScopes(kid, kidlo, kidhi);
			} else {
				// lexical blocks are not constrained to be within other lexical blocks,
				// but they should be within the function.
				checkChildScopes(kid, low, high);
				
			}
		}
		
	}

	/**
	 * @return
	 */
	private String describeScope(IScope scope) {
		if (scope == null)
			return "";
		String myscope = scope.getClass().getSimpleName() + "[" + scope.getName() +"]";
		return describeScope(scope.getParent()) + ": " + myscope;
	}
	
	/**
	 * Make sure our fixing up of CU scopes makes sense.  In old GCC-E (e.g. 3.4.3), the
	 * compile units do not have low_pc and high_pc, which makes it hard to find functions.
	 */
	@Test
	public void testScopes3() {
		for (TestInfo info : testInfos.values()) {
			String label = info.symFile.lastSegment();
			System.out.println("Address->Function mapping for "+ label);
			
			// explicitly read the functions
			IExecutableSymbolicsReader exeReader = ExecutableSymbolicsReaderFactory.createFor(info.symFile);
			if (exeReader == null)
				continue;
			
			DwarfDebugInfoProvider debugInfoProvider = new DwarfDebugInfoProvider(exeReader);
			IEDCSymbolReader explicitSymbolReader = new EDCSymbolReader(
					exeReader,
					debugInfoProvider);
			
			// this is NOT guaranteed:  GCC-E at link time will share function definitions among CUs,
			// so two CUs can "overlap" if you consider only their raw Lo and Hi PCs.
			/*
			// make sure the CUs are proper
			ArrayList<IScope> cuList= new ArrayList<IScope>(explicitSymbolReader.getModuleScope().getChildren());
			Collections.sort(cuList);
			long low = 0, high = 0;
			
			for (IScope cu : cuList) {
				String culabel = label + ":" + cu;
				long start = cu.getLowAddress().getValue().longValue();
				long end = cu.getHighAddress().getValue().longValue();
				if (start != end) {
					assertTrue(culabel, start < end);
					assertTrue(culabel, start >= low);
					if (high > 0)
						assertTrue(culabel, end > high);
					high = end;
				}
			}
			*/
			
			// remember all the functions
			List<IFunctionScope> allFuncs = new ArrayList<IFunctionScope>(
					explicitSymbolReader.getModuleScope().getFunctionsByName(null));
			assertTrue(allFuncs.size() >= 10);
			
			debugInfoProvider.dispose();
			
			// now, make sure we can find all those functions with a random scan
			debugInfoProvider = new DwarfDebugInfoProvider(exeReader);
			IEDCSymbolReader testSymbolReader = new EDCSymbolReader(
					exeReader,
					debugInfoProvider);
			
			StringBuilder missing = new StringBuilder();
			Random random = new Random(0x12145895);
			int count = allFuncs.size();
			while (count-- > 0) {
				IFunctionScope allFunc = allFuncs.get(random.nextInt(allFuncs.size()));
				doTestScope3Func(label, missing, testSymbolReader, allFunc);
			}
			debugInfoProvider.dispose();
			
			
			if(missing.length() > 0)
				fail(missing.toString());
		}
	}
	
	/**
	 * Make sure our fixing up of CU scopes makes sense.  In old GCC-E (e.g. 3.4.3), the
	 * compile units do not have low_pc and high_pc, which makes it hard to find functions.
	 */
	@Test
	public void testScopes3b() {
		IEDCSymbolReader symbolReader = Symbols.getSymbolReader(getFile("BlackFlag_gcce_343.sym"));
		IScope scope = symbolReader.getModuleScope().getScopeAtAddress(new Addr32(0xad32));
		assertTrue(scope instanceof IFunctionScope || scope instanceof ILexicalBlockScope);
	}

	/**
	 * @param label
	 * @param missing
	 * @param symbolReader
	 * @param allFunc
	 */
	private void doTestScope3Func(String label, StringBuilder missing,
			IEDCSymbolReader symbolReader, IFunctionScope allFunc) {
		IScope scope1 = symbolReader.getModuleScope().getScopeAtAddress(allFunc.getLowAddress().add(1));
		IScope scope2 = symbolReader.getModuleScope().getScopeAtAddress(allFunc.getHighAddress().add(-1));
		
		if (scope1 == null || scope2 == null) {
			missing.append(label + ":"+ allFunc +" missing\n");
			
		} 
		// skip if the function is defined over a range, because this may mean interleaved runtime code
		else if (allFunc.getRangeList() == null){
			// one of both may be inlined
			while (scope1 instanceof ILexicalBlockScope ||  scope1.getParent() instanceof IFunctionScope)
				scope1 = scope1.getParent();
			while (scope2 instanceof ILexicalBlockScope || scope2.getParent() instanceof IFunctionScope)
				scope2 = scope2.getParent();
			if (scope1 != scope2) {
				missing.append(label + ":"+ allFunc +" did not find same function at low and high: " + scope1 + " / "+ scope2 + "\n");
			}
		}
	}
	
	
	@Test
	public void testPubnames1() {
		for (TestInfo info : testInfos.values()) {
			if (info.numberOfPubFuncNames == 0) continue;
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
			DwarfDebugInfoProvider debugInfoProvider = (DwarfDebugInfoProvider) ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
			Map<String, List<PublicNameInfo>> pubs;
			pubs = debugInfoProvider.getPublicFunctions();
			
			boolean discover = false;
			int total = 0;
			for (String pub : pubs.keySet()) {
				total += pubs.get(pub).size();
			}
			if (discover) {
				System.out.println(info.symFile + ": " + pubs.size() + " / "+ total);
			} else {
				assertEquals(info.numberOfPubFuncNames, pubs.size());
				assertEquals(info.numberOfPubFuncEntries, total);
			}
			
			pubs = debugInfoProvider.getPublicVariables();
			
			total = 0;
			for (String pub : pubs.keySet()) {
				total += pubs.get(pub).size();
			}
			if (discover) {
				System.out.println(info.symFile + ": " + pubs.size() + " / "+ total);
			} else {
				assertEquals(info.numberOfPubVarNames, pubs.size());
				assertEquals(info.numberOfPubVarEntries, total);
			}

		}
	}
	
	@Test
	public void testPubnames2() {
		for (TestInfo info : testInfos.values()) {
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			
			for (String name : info.pubFuncs) {
				String simpleName = stripName(name);
				Collection<IFunctionScope> funcs = symbolReader.getModuleScope().getFunctionsByName(simpleName);
				assertNotNull(info.symFile.lastSegment() + ":" + name, funcs);
				assertTrue(info.symFile.lastSegment() + ":" + name, !funcs.isEmpty());
				for (IFunctionScope func : funcs) {
					assertEquals(func.getName(), simpleName);
				}
			}
			for (String name : info.pubVars) {
				String simpleName = stripName(name);
				Collection<IVariable> vars = symbolReader.getModuleScope().getVariablesByName(simpleName, false);
				assertNotNull(info.symFile.lastSegment() + ":" + name, vars);
				assertTrue(info.symFile.lastSegment() + ":" + name, !vars.isEmpty());
				for (IVariable var : vars) {
					assertEquals(var.getName(), simpleName);
				}
			}
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private String stripName(String name) {
		int idx = name.lastIndexOf(':');
		if (idx > 0)
			return name.substring(idx+1);
		else
			return name;
	}
	

	static class ScopeInfo {
		int address;
		String[] names;
		public ScopeInfo(int address, String[] names) {
			super();
			this.address = address;
			this.names = names;
		}
		
	}
	
	protected static void addScopeVars(String sym, String srcFile, String function, String className, int address, String... names) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			ScopeInfo scope = new ScopeInfo(address, names);
			List<ScopeInfo> scopes;
			String key = srcFile + "|" + function + "|" + className;
			scopes = info.scopeInfos.get(key);
			if (scopes == null) {
				scopes = new ArrayList<ScopeInfo>();
				info.scopeInfos.put(key, scopes);
			}
			scopes.add(scope);
		}
	}
	static {
		/*
		 * The address information for the unit test is gathered like this:
		 * 
		 * 1) Find a function of interest, usually with DW_TAG_lexical_block entries inside
		 * DW_TAG_subroutine.
		 * 
		 * 2) Add a PC for the entry point (DW_AT_low_pc for that subroutine).
		 * Usually DW_TAG_formal_parameter entries are live here.  Any DW_TAG_variable
		 * entries with DW_AT_scope==0 (or unspecified) are also live.
		 * 
		 * 3) Find some interesting points where lexical blocks open and add the variables
		 * from there.
		 * 
		 * 4) Referencing the original source code is useful too, to avoid getting confused.
		 * 
		 * 5) Finally, ALSO check the DW_AT_location entries for the variables.  If
		 * it references a location list (rather than a static expression), then the
		 * compiler is indicating that the variable has a narrower scope than otherwise
		 * indicated.  But, that location list may specify locations *outside* the
		 * advertised lexical block scope.  So those should not be considered. 
		 * Note that RVCT may have bogus lexical block ranges, but the location lists
		 * are useful.  GCC-E, on the other hand, has better lexical block ranges, but
		 * never uses location lists.
		 */
		
		// RVCT has totally broken scope info; all lexical scopes go backwards, so
		// we clamp them to the end of the function
		
		// entry to function 
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3e0);
		
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3e4,
				"stackArray");
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3ee,
				"stackArray" /*, "pstackArray" */);
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3f0,
				"stackArray", "pstackArray");
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3f0+2,
				"stackArray", "pstackArray");
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb3f4,
				"stackArray", "pstackArray", "i", "value");
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "ptrToArray", null, 0xb40a,
				"stackArray", "pstackArray", "pheapArray", "value");
		
		// GCCE has valid scope info
		
		// entry to function
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x10854);
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x1085a,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj");
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x10876,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj", "i");
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x108ac+2,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj");
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x108b8+4,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj", "j");
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x108f2 + 2,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj", "k");
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x10970+2,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj", "m");
		// show all variables at end of function, but not variable of last for loop scope
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x10a2a,
				"stackArray", "pstackArray", "value", "pheapArray", "objArray", "pobj");
		// but not past, in case instruction stepping at end of function
		addScopeVars("BlackFlag_gcce.sym", "dbg_pointers.cpp", "ptrToArray", null, 0x10a2a + 2);
		
		
		
		// entry to function
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "arrayOfPtrs", null, 0xb802);
		
		addScopeVars("BlackFlag_rvct.sym", "dbg_pointers.cpp", "arrayOfPtrs", null, 0xb808, 
				"parray", "pClass2", "i" );
		
		// entry to function
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "dlist", 0xa82a,
				"this", "k");
		
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "dlist", 0xa82e,
				"this", "k");
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "dlist", 0xa830,
				"this", "found", "k", "search_node");
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "dlist", 0xa83c,
				"this", "found", "k", "search_node","__result");
		
		// entry to function
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "list", 0xa652, 
				"this", "k" );
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "list", 0xa656, 
				"this", "k");
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "list", 0xa658, 
				"this", "k", "found", "aux");
		addScopeVars("BlackFlag_rvct.sym", "dbg_linked_lists.cpp", "find", "list", 0xa666, 
				"this", "k", "__result", "found", "aux");
		
		// good lexical scopes here
		
		// entry to function
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa07a, 
				"this", "key");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa080, 
				"this", "key", "direction", "previous");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa082, 
				"this", "key", "direction", "previous");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa082, 
				"this", "key", "direction", "previous");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa092, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa098, 
				"this", "key", "__result", "direction", "previous", "theNode" );
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa0f2, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa0f8, 
				"this", "key",  "direction", "previous", "theNode", "subtree");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa11e, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa138, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa164, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa166, 
				"this", "key", "direction", "previous", "theNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa16c, 
				"this", "key", "direction", "previous", "theNode", "pcurrentNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa1ea, 
				"this", "key", "direction", "previous", "theNode", "next", "pcurrentNode");
		addScopeVars("BlackFlag_rvct.sym", "dbg_binary_tree.cpp", "DeleteFromTree", "binary_tree", 0xa21e, 
				"this", "key", "direction", "previous", "theNode");
		
		// enty to function
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x8386, 
				"this", "item");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x8394, 
				"this", "item");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x8398, 
				"this", "item", "newmax");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x83a6, 
				"this", "item", "newmax", "copy");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x83a8, 
				"this", "item", "newmax", "copy", "i");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x83b0, 
				"this", "item", "newmax", "copy", "i");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x83bc, 
				"this", "item", "newmax", "copy");
		addScopeVars("SimpleCpp_rvct_22.sym", "Templates.h", "add", "List", 0x83cc);
		
		
		// enty to function
		addScopeVars("SimpleCpp_rvct_40.sym", "Templates.cpp", "add", "List", 0x835a, 
				"this", "item", "__result$$1$$_Znaj");
		addScopeVars("SimpleCpp_rvct_40.sym", "Templates.cpp", "add", "List", 0x836c, 
				"this", "item", "newmax", "__result$$1$$_Znaj");
		addScopeVars("SimpleCpp_rvct_40.sym", "Templates.cpp", "add", "List", 0x837a, 
				"this", "item", "newmax", "copy", "__result$$1$$_Znaj");
		addScopeVars("SimpleCpp_rvct_40.sym", "Templates.cpp", "add", "List", 0x837c, 
				"this", "item", "newmax", "copy", "i", "__result$$1$$_Znaj");
		addScopeVars("SimpleCpp_rvct_40.sym", "Templates.cpp", "add", "List", 0x83a0, 
				"this", "item", "__result$$1$$_Znaj");

		
		// entry to function
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x80487a6, 
				"this", "item");
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x80487c0, 
				"this", "item", "newmax", "copy");
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x80487e9, 
				"this", "item", "newmax", "copy", "i");
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x804881e + 1, 
				"this", "item", "newmax", "copy");
		// show all locals at end of function
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x8048854, 
				"this", "item", "newmax", "copy");
		// but not past
		addScopeVars("SimpleCpp_gcc_x86.exe", "Templates.cpp", "add", "List", 0x8048854 + 1, 
				"this", "item");
	}
	
	
	/**
	 * Test that, for a given PC, we know which locals are in scope.
	 */
	@Test
	public void testLocalScopes1() {
		StringBuilder errors = new StringBuilder();
		for (TestInfo info : testInfos.values()) {
			for (String key : info.scopeInfos.keySet()) {
				String[] split = key.split("\\|");
				String file = split[0];
				String func = split[1];
				String className = split[2];
				if (className.equals("null"))
					className = null;
				
				IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
				List<ICompileUnitScope> cuList = getCompileUnitsFor(symbolReader, file);
				boolean found = false;
				for (ICompileUnitScope cu : cuList) {
					for (IFunctionScope scope : cu.getFunctions()) {
						if (scope.getName().equals(func)) {
							
							if (className != null) {
								String theClassName = getClassFor(scope);
								if (!theClassName.matches("(class|struct) " +className + "(<.*>)?"))
									continue;
							}
							
							found = true;
							
							String label = info.symFile.lastSegment() + ":" + file + (className!= null ? ":" + className  : "") + ":" + func;
							
							boolean discover = false;
							if (discover) {
								System.out.println(label);
								Collection<IVariable> vars = scope.getVariables();
								for (IVariable var : vars) System.out.println(var.getName());
								vars = scope.getParameters();
								for (IVariable var : vars) System.out.println(var.getName());
							} else {							
								doTestLocalScopes1(errors, label, info.scopeInfos.get(key), scope);
							}
						}
					}
				}
				assertTrue(info.symFile.lastSegment() + ":" + key, found);
			}
		}
		if (errors.length() > 0)
			fail(errors.toString());
	}

	/**
	 * @param scope
	 * @return
	 */
	private String getClassFor(IFunctionScope scope) {
		for (IVariable arg : scope.getParameters()) {
			if (arg.getName().equals("this")) {
				ICompositeType ct = (ICompositeType) TypeUtils.getBaseType(arg.getType());
				return ct.getName();
			}
		}
		return null;
	}

	/**
	 * @param errors 
	 * @param scopes 
	 * @param scope
	 */
	private void doTestLocalScopes1(StringBuilder errors, String label, List<ScopeInfo> scopeInfos, IFunctionScope scope) {
		// TODO: watch out for template functions, which are named the same
		Addr32 startAddr = new Addr32(scopeInfos.get(0).address);
		if (scope.getLowAddress().compareTo(startAddr) > 0
				|| scope.getHighAddress().compareTo(startAddr) <= 0) {
			return;
		}
		
		for (ScopeInfo scopeInfo : scopeInfos) {
			try {
				doTestScopeInfo(label, scope, scopeInfo);
			} catch (AssertionError e) {
				errors.append(e.getMessage());
				errors.append('\n');
			}
		}
	}

	private void doTestScopeInfo(String label, IFunctionScope scope,
			ScopeInfo scopeInfo) {
		label += ":" + Integer.toHexString(scopeInfo.address); 
		Collection<IVariable> vars = scope.getScopedVariables(new Addr32(scopeInfo.address));
		StringBuilder missing = new StringBuilder();
		for (String name : scopeInfo.names) {
			boolean found = false;
			for (IVariable var : vars) {
				if (var.getName().equals(name)) {
					found = true;
					break;
				}
			}
			if (!found)
				missing.append(name + ", ");
		}
		if (missing.length() > 0)
			fail(label + ": missing "+ missing);
		
		StringBuilder extra = new StringBuilder();
		for (IVariable var : vars) { 
			boolean found = false;
			for (String name : scopeInfo.names) {
				if (name.equals(var.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				extra.append(var.getName());
				extra.append(", ");
			}
		}
		if (extra.length() > 0)
			fail(label +": found extra variables: " + extra);
	}
	
	/**
	 * Test that, for a given PC, we know which locals are in scope.
	 */
	@Test
	public void testLocalLocations() {
		StringBuilder errors = new StringBuilder();
		for (TestInfo info : testInfos.values()) {
			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(info.symFile);
			for (IScope cuscope : symbolReader.getModuleScope().getChildren()) {
				ICompileUnitScope cu = (ICompileUnitScope) cuscope;
				for (IFunctionScope scope : cu.getFunctions()) {
					String label = info.symFile.lastSegment() + ":" + scope;

					for (IVariable var : scope.getVariablesInTree()) {
						checkVariableLocation(errors, label, scope, var);
					}
				}
			}
		}
		if (errors.length() > 0)
			fail(errors.toString());
	}

	/**
	 * @param errors
	 * @param label
	 * @param var
	 */
	private void checkVariableLocation(StringBuilder errors, String label,
			IScope scope, IVariable var) {
		ILocationProvider locationProvider = var.getLocationProvider();
		if (locationProvider instanceof LocationExpression) {
			LocationExpression expr = (LocationExpression) locationProvider;
			IScope varScope = expr.getScope();
			while (varScope instanceof ILexicalBlockScope ||
					(varScope != null && varScope.getParent() instanceof IFunctionScope))
				varScope = varScope.getParent();
			if (!(varScope instanceof IFunctionScope)) {
				errors.append("Wrong scope for " + var + ": " + varScope + "\n");
			}
		}
		
	}

	
	/**
	 * Be sure we zero extend when reading short DWARF attributes.
	 * One place this is quite important is in array bounds.  
	 * Other cases in the reader are line tables, scope ranges, etc. which
	 * are unsigned.  Enum values, though, must remain signed.
	 * @throws Exception
	 */
	@Test
	public void testAttributePromotion() throws Exception {
		// In here, we have a tmp[256] array, with DW_AT_upper_bound == 255.
		// This validates some lazy code that always cast this stuff to a long
		// without considering the sign extension.
		
		// (BTW, this bug was logged for something else, and is not the bug being tested here)
		IEDCSymbolReader reader = Symbols.getSymbolReader(getFile("bug303066.exe"));
		assertNotNull(reader);
		Collection<IFunctionScope> scopes = reader.getModuleScope().getFunctionsByName("main");
		assertNotNull(scopes);
		assertEquals(1, scopes.size());
		boolean found = false;
		int enumcnt = 0;
		for (IVariable var : scopes.iterator().next().getVariables()) {
			if (var.getName().equals("tmp")) {
				IType type = var.getType();
				assertTrue(type instanceof IArrayType);
				IArrayBoundType bound = ((IArrayType) type).getBound(0);
				assertEquals(256, bound.getBoundCount());
				found = true;
			}
			
			// these attributes *should* be sign extended
			if (var.getName().equals("val")) {
				IType type = TypeUtils.getBaseType(var.getType());
				for (IEnumerator enumr : ((IEnumeration)type).getEnumerators()) {
					if (enumr.getName().equals("Val1")) {
						assertEquals(255, enumr.getValue());
						enumcnt++;
					} else if (enumr.getName().equals("Val2")) {
						assertEquals(65535, enumr.getValue());
						enumcnt++;
					} else if (enumr.getName().equals("Val3")) {
						assertEquals(-255, enumr.getValue());
						enumcnt++;
					} else if (enumr.getName().equals("Val4")) {
						assertEquals(-65535, enumr.getValue());
						enumcnt++;
					}
				}
			}
		}
		assertEquals("Found all Val enums", 4, enumcnt);
		assertTrue("Did not find 'tmp'", found);
		
		found = false;
		for (IVariable var : reader.getModuleScope().getVariablesByName("bigbuffer", false)) {
			IType type = var.getType();
			assertTrue(type instanceof IArrayType);
			IArrayBoundType bound = ((IArrayType) type).getBound(0);
			assertEquals(65536, bound.getBoundCount());
			found = true;
			break;
		}
		assertTrue("Did not find 'bigbuffer'", found);
		
	}
	
	/**
	 * RVCT generates location lists with two duplicate address ranges, where
	 * the latter is the one to trust.  Be sure we filter out the earlier ones.
	 * @throws Exception
	 */
	@Test
	public void testBrokenLocationLists() throws Exception {
		IPath file = getFile("BlackFlag_rvct.sym");
		IEDCSymbolReader reader = Symbols.getSymbolReader(file);
		// show_Const_Arguments
		IFunctionScope func = (IFunctionScope) reader.getModuleScope().getScopeAtAddress(new Addr32(0x9648));
		Collection<IVariable> variables = func.getParameters();
		boolean found = false;
		for (IVariable var : variables) {
			if (var.getName().equals("aArg1")) {
				found = true;
				LocationEntry[] entries = ((LocationList)var.getLocationProvider()).getLocationEntries();
				assertEquals(2, entries.length);
				assertEquals(0x9648, entries[0].getLowPC());
				assertEquals(1, entries[0].getBytes().length);
				assertEquals((byte)0x50, entries[0].getBytes()[0]);
				break;
			}
		}
		assertTrue(found);
	}
}
