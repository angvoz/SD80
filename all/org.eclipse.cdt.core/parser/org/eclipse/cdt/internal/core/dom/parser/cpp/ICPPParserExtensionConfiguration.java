/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 22, 2004
 *
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

/**
 * @author jcamelon
 */
public interface ICPPParserExtensionConfiguration {
	
	public boolean allowRestrictPointerOperators();
	public boolean supportTypeofUnaryExpressions();
	public boolean supportAlignOfUnaryExpression();
	public boolean supportExtendedTemplateSyntax();
	public boolean supportMinAndMaxOperators();
	public boolean supportStatementsInExpressions();
	public boolean supportComplexNumbers();
	public boolean supportRestrictKeyword();
    public boolean supportLongLongs();
	public boolean supportKnRC();
	public boolean supportGCCOtherBuiltinSymbols();
	public boolean supportAttributeSpecifiers();

}
