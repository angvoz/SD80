/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on May 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;
import java.util.Map;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IParameterizedSymbol extends IContainerSymbol {
	
	public void addParameter( ISymbol param );
	public void addParameter( TypeInfo.eType type, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault );
	public void addParameter( ISymbol typeSymbol, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault );
	
	public Map getParameterMap();
	public List getParameterList();
	//public void setParameterList( List list );

	public boolean hasSameParameters(IParameterizedSymbol newDecl);
	
	public void setReturnType( ISymbol type );
	public ISymbol getReturnType();
	
	public void setHasVariableArgs( boolean var );
	public boolean hasVariableArgs( );
}
