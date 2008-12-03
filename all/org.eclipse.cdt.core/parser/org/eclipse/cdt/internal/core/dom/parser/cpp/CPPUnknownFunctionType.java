/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Models the type of an unknown function.
 */
public class CPPUnknownFunctionType extends CPPFunctionType implements ICPPUnknownType {
	CPPUnknownFunctionType() {
		super(CPPUnknownClass.createUnnamedInstance(), IType.EMPTY_TYPE_ARRAY);
	}
}
