/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.parser.tests.scanner2;

import java.util.List;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.extension.ExtensionDialect;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ParserExtensionFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.Scanner2;

/**
 * @author jcamelon
 *
 */
public class BaseScanner2Test extends TestCase {

	protected IScanner scanner;
	
	public BaseScanner2Test( String x )
	{
		super(x);
	}

	protected void initializeScanner( String input, ParserMode mode ) throws ParserFactoryError
	{
		initializeScanner( input, mode, new NullSourceElementRequestor( mode ));
	}

	protected void initializeScanner( String input, ParserMode mode, ISourceElementRequestor requestor ) throws ParserFactoryError
	{
		scanner = createScanner( new CodeReader(input.toCharArray()), new ScannerInfo(), mode, ParserLanguage.CPP, requestor, null, null ); //$NON-NLS-1$
	}

	protected void initializeScanner( String input, ParserLanguage language ) throws ParserFactoryError
	{
	    scanner = createScanner( new CodeReader(input.toCharArray()), 
	                             new ScannerInfo(), ParserMode.COMPLETE_PARSE, language, 
	                             new NullSourceElementRequestor( ParserMode.COMPLETE_PARSE ), null, null ); 
	}
	
	protected void initializeScanner(String input) throws ParserFactoryError
	{
       initializeScanner( input, ParserMode.COMPLETE_PARSE );
	}
	
    public static Scanner2 createScanner( CodeReader code, IScannerInfo config, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService log, List workingCopies ) throws ParserFactoryError
    {
    	if( config == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_CONFIG );
    	if( language == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_LANGUAGE );
    	IParserLogService logService = ( log == null ) ? ParserFactory.createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode );
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor ); 
		return new Scanner2( code, config, ourRequestor, ourMode, language, logService, new ParserExtensionFactory( ExtensionDialect.GCC ).createScannerExtension(), workingCopies );
    }

	public int fullyTokenize() throws Exception
	{
		try
		{
			IToken t= scanner.nextToken();
			while (t != null)
			{
				if (verbose)
					System.out.println("Token t = " + t); //$NON-NLS-1$

				if ((t.getType()> IToken.tLAST))
					System.out.println("Unknown type for token " + t); //$NON-NLS-1$
				t= scanner.nextToken();
			}
		}
		catch ( EndOfFileException e)
		{
		}
		return scanner.getCount();
	}
	public void validateIdentifier(String expectedImage) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertEquals( t.getType(), IToken.tIDENTIFIER );
			assertEquals(t.getImage(), expectedImage );
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateInteger(String expectedImage) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tINTEGER);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFileException e) {
			assertTrue(false);
		}
	}
	
	public void validateFloatingPointLiteral(String expectedImage) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tFLOATINGPT);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFileException e) {
			assertTrue(false);
		}
	}
	
	public void validateChar( char expected )throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tCHAR );
			Character c = new Character( expected ); 
			assertEquals( t.getImage(), '\'' + c.toString() + '\'' ); 
		} catch (EndOfFileException e) {
			assertTrue(false);
		}
	}

	public void validateChar( String expected ) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tCHAR );
			assertEquals( t.getImage(), '\'' + expected + '\''); 
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateString( String expectedImage ) throws Exception
	{
		validateString( expectedImage, false );
	}

	public void validateString(String expectedImage, boolean lString ) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			if( lString )
				assertEquals(IToken.tLSTRING, t.getType());
			else
				assertEquals(IToken.tSTRING, t.getType());
			assertEquals(expectedImage, t.getImage());
		} catch (EndOfFileException e) {
			fail("EOF received"); //$NON-NLS-1$
		} 
	}

	public void validateToken(int tokenType) throws Exception
	{
		try {
			IToken t= scanner.nextToken();
			assertEquals(tokenType, t.getType());
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateBalance(int expected)
	{
		// This isn't kept track of any more
		//assertTrue(scanner.getDepth() == expected);
	}

	public void validateEOF() throws Exception
	{
		try {
			assertNull(scanner.nextToken());
		} catch (EndOfFileException e) {
		} 
	}

	public static void assertCharArrayEquals(char[] expected, char[] actual) {
		if (!CharArrayUtils.equals(expected, actual))
			throw new ComparisonFailure(null, new String(expected), new String(actual));
	}
	
	public void validateDefinition(String name, String value)
	{
		Object expObject = scanner.getRealDefinitions().get(name.toCharArray());
		assertNotNull(expObject);
		assertTrue(expObject instanceof ObjectStyleMacro);
		assertCharArrayEquals(value.toCharArray(), ((ObjectStyleMacro)expObject).expansion);
	}

	public void validateDefinition(String name, int value)
	{
		validateDefinition(name, String.valueOf(value));
	}

	public void validateAsUndefined(String name)
	{
		assertNull(scanner.getDefinitions().get(name.toCharArray()));
	}

	public static final String EXCEPTION_THROWN = "Exception thrown "; //$NON-NLS-1$

	public static final String EXPECTED_FAILURE = "This statement should not be reached " //$NON-NLS-1$
				+ "as we sent in bad preprocessor input to the scanner"; //$NON-NLS-1$

	public static final boolean verbose = false;


    /**
         * @param string
         */
    protected void validateWideChar(String string) throws Exception
    {
		try {
			IToken t= scanner.nextToken();
			assertEquals(IToken.tLCHAR, t.getType());
			assertEquals(t.getImage(), "L\'" + string + "\'");  //$NON-NLS-1$ //$NON-NLS-2$
		} catch (EndOfFileException e) {
			assertTrue(false);
		}		
    }
	

}
