/*
 * Created on Jun 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.QuickParseCallback;

/**
 * @author Doug Schaefer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpeedTest extends TestCase {

	public void test() throws Exception {
		String code =
			"#include <windows.h>\n" +
			"#include <stdio.h>\n" +
			"#include <iostream>\n";
		
		Reader reader = new StringReader(code);
		IScannerInfo info = mingwScannerInfo(false);
		//IScannerInfo info = msvcScannerInfo(quick);
		testParse(reader, "text", false, info, ParserLanguage.CPP);
	}

	/**
	 * @param path
	 * @param quick TODO
	 */
	protected void testParse(Reader reader, String path, boolean quick, IScannerInfo info, ParserLanguage lang) throws Exception {
		ParserMode mode = quick ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE;
		IScanner scanner = ParserFactory.createScanner(reader, path, info, mode, lang, CALLBACK, null, Collections.EMPTY_LIST ); 
		IParser parser = ParserFactory.createParser( scanner, CALLBACK, mode, lang, null);
		long startTime = System.currentTimeMillis();
		long totalTime;
		parser.parse();
		totalTime = System.currentTimeMillis() - startTime;
		System.out.println( "Resulting parse for " + path + " took " + totalTime + " millisecs"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static final QuickParseCallback CALLBACK = new QuickParseCallback();

	/**
	 * @param quick
	 * @return
	 */
	protected IScannerInfo msvcScannerInfo(boolean quick) {
		if( quick )
			return new ScannerInfo();
		Map definitions = new Hashtable();
		//definitions.put( "__GNUC__", "3" );  //$NON-NLS-1$ //$NON-NLS-2$

		String [] includePaths = new String[] {
			"C:\\Program Files\\Microsoft SDK\\Include",
			"C:\\Program Files\\Microsoft Visual C++ Toolkit 2003\\include"
		};
		return new ScannerInfo( definitions, includePaths );
	}

	protected IScannerInfo mingwScannerInfo(boolean quick) {
		if( quick )
			return new ScannerInfo();
		Map definitions = new Hashtable();
		definitions.put("__GNUC__", "3");
		definitions.put("__GNUC_MINOR__", "2");
		definitions.put("__GNUC_PATCHLEVEL__", "3");
		definitions.put("__GXX_ABI_VERSION", "102");
		definitions.put("_WIN32", "");
		definitions.put("__WIN32", "");
		definitions.put("__WIN32__", "");
		definitions.put("WIN32", "");
		definitions.put("__MINGW32__", "");
		definitions.put("__MSVCRT__", "");
		definitions.put("WINNT", "");
		definitions.put("_X86_", "1");
		definitions.put("__WINNT", "");
		definitions.put("_NO_INLINE__", "");
		definitions.put("__STDC_HOSTED__", "1");
		definitions.put("i386", "");
		definitions.put("__i386", "");
		definitions.put("__i386__", "");
		definitions.put("__tune_i586__", "");
		definitions.put("__tune_pentium__", "");
		definitions.put("__stdcall", "__attribute__((__stdcall__))");
		definitions.put("__cdecl", "__attribute__((__cdecl__))");
		definitions.put("__fastcall", "__attribute__((__fastcall__))");
		definitions.put("_stdcall", "__attribute__((__stdcall__))");
		definitions.put("_cdecl", "__attribute__((__cdecl__))");
		definitions.put("_fastcall", "__attribute__((__fastcall__))");
		definitions.put("__declspec(x)", "__attribute__((x))");
		definitions.put("__DEPRECATED", "");
		definitions.put("__EXCEPTIONS", "");
		
		String [] includePaths = new String[] {
			"c:/mingw/include/c++/3.2.3",
			"c:/mingw/include/c++/3.2.3/mingw32",
			"c:/mingw/include/c++/3.2.3/backward",
			"c:/mingw/include",
			"c:/mingw/lib/gcc-lib/mingw32/3.2.3/include"
		};

		return new ScannerInfo( definitions, includePaths );
	}

}
