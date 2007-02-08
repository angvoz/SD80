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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;


class C99Main {

	protected static final String INPUT_FILE_NAME = "tempTestFile.c";
	
	public static void main(String [] args) throws IOException {
		
		char[] input = getInputChars(INPUT_FILE_NAME);
		C99Lexer lexer = new C99Lexer(input, INPUT_FILE_NAME);
		C99Parser parser = new C99Parser(lexer);
		
		System.out.println("File: " + INPUT_FILE_NAME);
		// Pass 1: Lex the input, Fill the parser with a stream of tokens
		System.out.println("Lexing");
		lexer.lexer(parser);
		//lexer.lex();
		
		
		System.out.println("Original Code");
		System.out.println(input);
		
		// Pass 2: Parse the tokens, return an AST
		System.out.println("Parsing");
		parser.parser(-1);
		//parser.dumpTokens();
		IASTTranslationUnit ast = parser.getAST();
		
		
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
