/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.search.tests;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 */
public class ParseTestOnSearchFiles extends TestCase
{
    private String pluginRoot;
    private FileInputStream fileIn;
    private String name;
    /**
     * 
     */
    public ParseTestOnSearchFiles()
    {
        super();
    }
    /**
     * @param name
     */
    public ParseTestOnSearchFiles(String name)
    {
        super(name);        
    }
    
	protected void setUp() throws Exception {	
		pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
        name = pluginRoot+ "resources/search/classDecl.cpp";
        fileIn = new FileInputStream(name);
	}
	
	public void testParseOfAndrewsFile() throws Exception
	{
		ISourceElementRequestor requestor = new NullSourceElementRequestor();
		IScanner scanner = ParserFactory.createScanner( new InputStreamReader( fileIn ), name, new ScannerInfo(), ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, requestor, new NullLogService() );
		IParser parser = ParserFactory.createParser( scanner, requestor, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, null );
		assertTrue( parser.parse() );
	}

}
