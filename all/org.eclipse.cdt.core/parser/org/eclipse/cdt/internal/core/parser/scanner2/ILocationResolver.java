/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ast.IASTMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;

/**
 * @author jcamelon
 */
public interface ILocationResolver {
    
    public IASTMacroDefinition[] getAllMacroDefinitions();
    public IASTPreprocessorStatement [] getPreprocessorStatements();
    public IASTNodeLocation [] getLocations( int offset, int length );
}
