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

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.debug.edc.symbols.IType;

public interface ICompositeType extends IType, IAggregate {
	
	// accessibility of an inherited class or of a composite's field
	public static int ACCESS_PUBLIC    = 0;
	public static int ACCESS_PROTECTED = 1;
	public static int ACCESS_PRIVATE   = 2;

	public static final int k_class  = ICPPASTCompositeTypeSpecifier.k_class;
	public static final int k_struct = IASTCompositeTypeSpecifier.k_struct;
	public static final int k_union  = IASTCompositeTypeSpecifier.k_union;

	/**
	 * Kind of composite (class, struct, union)
	 * 
	 * @return kind
	 */
	public int getKey();

	/**
	 * Number of fields/enumerators in composite
	 * 
	 * @return count
	 */
	public int fieldCount();

	/**
	 * Add a field/member to the end of the list of fields or enumerators
	 * Intended for use by a debug information parser.
	 * 
	 * @param field
	 *            field to add
	 */
	public void addField(IField field);

	/**
	 * Get an array of fields/enumerators in composite
	 * 
	 * @return array of fields/enumerators, or IField.EMPTY_FIELD_ARRAY if no
	 *         fields/enumerators
	 */
	public IField[] getFields();

	/**
	 * Find the composite fields/members with the given name
	 * 
	 * @param name field name, which may contain "::" separators
	 * @return array of matching fields if any exist, or null otherwise
	 */
	public IField[] findFields(String name);

	/**
	 * Number of classes and structs from which the composite inherits
	 * 
	 * @return count
	 */
	public int inheritanceCount();

	/**
	 * Add an inherited-from class or struct to the end of the list
	 * of inherited-from classes and structs
	 * Intended for use by a debug information parser.
	 * 
	 * @param inheritance
	 *            information about class/struct from which this
	 *            composite inherits
	 */
	public void addInheritance(IInheritance inheritance);
	
	/**
	 * Get an array of inherited-from classes/structs for composite
	 * 
	 * @return array of information about classes and structs from which this
	 * composite inherits, or an empty array if nothing is inherited
	 */
	public IInheritance[] getInheritances();
	
	/**
	 * Get the name without a type prefix (e.g., "foo" instead of "class foo")
	 * 
	 * @return composite name without a type 
	 */
	public String getBaseName();

}
