/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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

public interface ICompositeType extends IType, IAggregate {

	public static final int k_class = ICPPASTCompositeTypeSpecifier.k_class;
	public static final int k_struct = IASTCompositeTypeSpecifier.k_struct;
	public static final int k_union = IASTCompositeTypeSpecifier.k_union;

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
	 *            to add
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
	 * Find the composite field/member with the given name
	 * 
	 * @param name
	 * @return field if it exists, or null otherwise
	 */
	public IField findField(String name);

}
