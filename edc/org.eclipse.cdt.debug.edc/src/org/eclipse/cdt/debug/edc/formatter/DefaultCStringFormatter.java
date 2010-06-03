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
package org.eclipse.cdt.debug.edc.formatter;

import org.eclipse.cdt.debug.edc.internal.symbols.ConstType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.internal.symbols.VolatileType;
import org.eclipse.cdt.debug.edc.symbols.IType;

/**
 * Handle char* and wchar_t* strings (ignoring const, volatile, and typedefs to char type).
 */
public class DefaultCStringFormatter extends AbstractStringFormatter {

	public static boolean handlesType(IType type) {
		if (type instanceof ICPPBasicType) {
			ICPPBasicType basicType = (ICPPBasicType) type;
			// NOTE: we may have neither signed nor unsigned set on a char*, and this is a "classic" string.
			// We would like to explicitly ignore "unsigned char*", since that's normally a byte buffer -- 
			// but RVCT incorrectly sets this flag for some instances of "char*" in source.
			boolean isCharString = /*!basicType.isUnsigned() &&*/ basicType.getBaseType() == ICPPBasicType.t_char;
			boolean isWcharTString = basicType.getBaseType() == ICPPBasicType.t_wchar_t || basicType.getName().equals("wchar_t");
			return isCharString || isWcharTString;
		}
		return false;
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}
	
	public IVariableValueConverter getDetailValueConverter(IType type) {
		IType basicType = getBasePointedType(type);
		if (handlesType(basicType))
			return new DefaultNullTerminatedStringConverter(type, true);
		
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		IType basicType = getBasePointedType(type);
		if (handlesType(basicType))
			return new DefaultNullTerminatedStringConverter(type, false);
		
		return null;
	}
	

	/**
	 * Get the basic unit type pointed to by a string (reducing, e.g.,
	 * "const char *const" to "char").  Also, handle typedefs to char (e.g. "gchar").
	 * @param type
	 * @return basic unit type or <code>null</code> if not a pointer to basic type
	 */
	protected IType getBasePointedType(IType type) {
		// remove upper qualifiers
		while (type instanceof ConstType || type instanceof VolatileType || type instanceof ITypedef)
			type = type.getType();
		
		// make sure it's a single pointer or a single-dimension array
		if (type instanceof IPointerType)
			type = type.getType();
		else if (type instanceof IArrayType && ((IArrayType) type).getBoundsCount() == 1)
			type = type.getType();
		else
			return null;
		
		// remove inner qualifiers
		while (type instanceof ConstType || type instanceof VolatileType || type instanceof ITypedef)
			type = type.getType();
		if (!(type instanceof ICPPBasicType))
			return null;
		return type;
	}
	
}
