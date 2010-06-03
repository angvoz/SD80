/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.debug.edc.symbols.IType;

/**
 * Interface for basic types.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBasicType extends IType {

	public static final int t_unspecified = IASTSimpleDeclSpecifier.t_unspecified;
	public static final int t_void = IASTSimpleDeclSpecifier.t_void;
	public static final int t_char = IASTSimpleDeclSpecifier.t_char;
	public static final int t_int = IASTSimpleDeclSpecifier.t_int;
	public static final int t_float = IASTSimpleDeclSpecifier.t_float;
	public static final int t_double = IASTSimpleDeclSpecifier.t_double;

	/**
	 * This returns the built-in type for the declaration. The type is then
	 * refined by qualifiers for signed/unsigned and short/long. The type could
	 * also be unspecified which usually means int.
	 * 
	 */
	public int getBaseType();

	public boolean isSigned();

	public boolean isUnsigned();

	public boolean isShort();

	public boolean isLong();
	
	public boolean isLongLong();
	
	public boolean isComplex();
}
