/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;

/**
 * Interface between the parser and the preprocessor. 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 */
public interface IScanner {
	
	/**
	 * Puts the scanner into content assist mode.
	 */
	public void setContentAssistMode( int offset );

	/**
	 * Turns on/off comment parsing.
	 * @since 4.0
	 */
	public void setScanComments(boolean val);
	
	/**
	 * Turns on/off creation of image locations. 
	 * @see IASTName#getImageLocation().
	 * @since 5.0
	 */
	public void setComputeImageLocations(boolean val);
	
	/**
	 * Returns a map from {@link String} to {@link IMacroBinding} containing
	 * all the definitions that are defined at the current point in the 
	 * process of scanning.
	 */
	public Map<String, IMacroBinding> getMacroDefinitions();

	/**
     * Returns next token for the parser. String literals are concatenated.
     * @throws EndOfFileException when the end of the translation unit has been reached.
     * @throws OffsetLimitReachedException see {@link Lexer}.
     */
	public IToken nextToken() throws EndOfFileException;
			
	/**
	 * Returns <code>true</code>, whenever we are processing the outermost file of the translation unit.
	 */
	public boolean isOnTopContext();
	
	/**
	 * Attempts to cancel the scanner.
	 */
	public void cancel();
	
	/**
	 * Returns the location resolver associated with this scanner.
	 */
	public ILocationResolver getLocationResolver();
}
