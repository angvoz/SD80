/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99BaseKeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.KeywordMap;


class C99Main {

	protected static final String INPUT_FILE_NAME = "tempTestFile.c";
	
	public static void main(String [] args) throws IOException {
		
		char[] input = getInputChars(INPUT_FILE_NAME);
		CodeReader reader = new CodeReader(input);
		
		System.out.println("Original Code");
		System.out.println(input);

		System.out.println("\nParsing");
		
		C99SourceCodeParser parser = new C99SourceCodeParser();
		IASTTranslationUnit ast = parser.parse(reader, null, null, null);
		
		System.out.println();
		System.out.println("AST: " + ast);
		System.out.println();
		ASTPrinter.printAST(ast);
		System.out.println();
		
		System.out.println("Done");
	}
	
	public static char[] getInputChars(String fileName) throws IOException
    {
        try
        {
            // Create a reader for the input stream and read the input file into a char array.
            File f = new File(fileName);
            int len = (int) f.length();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

            char[] inputChars = new char[len];
            in.read(inputChars, 0, len);
            return inputChars;
        }
        catch (Exception e)
        {
            IOException io = new IOException();
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw io;
        }
    }
}
