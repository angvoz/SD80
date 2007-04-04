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
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.core.runtime.CoreException;


class C99Main {

	protected static final String INPUT_FILE_NAME = "tempTestFile.c"; //$NON-NLS-1$
	
	public static void main(String [] args) throws IOException, CoreException {
		
		char[] input = getInputChars(INPUT_FILE_NAME);
		CodeReader reader = new CodeReader(input);
		
		System.out.println("Original Code"); //$NON-NLS-1$
		System.out.println(input);

		System.out.println("\nParsing"); //$NON-NLS-1$
		
		IASTTranslationUnit ast = new C99Language().getASTTranslationUnit(reader, null, null, null, null);
		
		System.out.println();
		System.out.println("AST: " + ast); //$NON-NLS-1$
		System.out.println();
		ASTPrinter.printAST(ast);
		System.out.println();
		
		System.out.println("Done"); //$NON-NLS-1$
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
