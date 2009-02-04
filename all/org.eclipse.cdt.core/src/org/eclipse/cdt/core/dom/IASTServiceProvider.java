/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

/**
 * This is the mechanism that represents a parser service in the CDT.
 * 
 * IASTTranslationUnits and ASTCompletionNodes are artifacts that this service returns.
 * 
 * @author jcamelon
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTServiceProvider {
    
    /**
     * This exception is thrown when there is not a service provider that can handle
     * the request due to dialect mis-match.  
     * 
     * @author jcamelon
     */
    public static class UnsupportedDialectException extends Exception 
    {
    	public static final long serialVersionUID = 0;
    }

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question	
     * @return syntactical parse tree
     * @throws UnsupportedDialectException 
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse) throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param parseComments parse commtents flag
     * @return syntactical parse tree
     * @throws UnsupportedDialectException 
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, boolean parseComments) throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param project     project handle to help us figure out build settings 
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException 
     */
    public IASTTranslationUnit getTranslationUnit( IStorage fileToParse, IProject project, ICodeReaderFactory fileCreator ) throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param project     project handle to help us figure out build settings 
     * @return syntactical parse tree
     * @throws UnsupportedDialectException 
     */    
    public IASTTranslationUnit getTranslationUnit( IStorage fileToParse, IProject project ) throws UnsupportedDialectException;
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator  )throws UnsupportedDialectException;

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @param parseComments parse commtents flag
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator, boolean parseComments)throws UnsupportedDialectException;

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @param configuration parser configuration provided rather than discovered by service
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration )throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param offset the offset at which you require completion at
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTCompletionNode getCompletionNode( IFile fileToParse, int offset, ICodeReaderFactory fileCreator) throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param project the project containing the scanner info
     * @param offset the offset at which you require completion at
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTCompletionNode getCompletionNode( IStorage fileToParse, IProject project, int offset, ICodeReaderFactory fileCreator) throws UnsupportedDialectException;
    
}
