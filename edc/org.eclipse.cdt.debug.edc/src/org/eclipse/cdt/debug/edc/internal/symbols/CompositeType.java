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

import java.util.ArrayList;
import java.util.Map;

public class CompositeType extends MayBeQualifiedType implements ICompositeType {

	// kind of composite (class, struct, union)
	private final int key;

	// fields in the composite 
	protected ArrayList<IField> fields = new ArrayList<IField>();
	
	// classes inherited from
	protected ArrayList<IInheritance> inheritances = new ArrayList<IInheritance>();
	private boolean addedInherited = false;

	public CompositeType(String name, IScope scope, int key, int byteSize, Map<Object, Object> properties, String prefix) {
		super(name, scope, byteSize, properties);
		this.name = prefix + " " + this.name; //$NON-NLS-1$
		this.key = key;
	}

	public int getKey() {
		return this.key;
	}

	public int fieldCount() {
		if (!addedInherited) {
			addInheritedFields();
		}
		return fields.size();
	}

	public void addField(IField field) {
		fields.add(field);
	}

	public IField[] getFields() {
		if (!addedInherited) {
			addInheritedFields();
		}
		
		IField[] fieldArray = new IField[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			fieldArray[i] = fields.get(i);
		}
		return fieldArray;
	}

	public IField findField(String name) {
		if (!addedInherited) {
			addInheritedFields();
		}
		
		for (int i = 0; i < fields.size(); i++) {
			if (((FieldType) fields.get(i)).getName().equals(name))
				return fields.get(i);
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void addInheritance(IInheritance inheritance) {
		inheritances.add(inheritance);
	}

	public IInheritance[] getInheritances() {
		IInheritance[] inheritanceArray = new IInheritance[inheritances.size()];
		for (int i = 0; i < inheritances.size(); i++) {
			inheritanceArray[i] = inheritances.get(i);
		}
		return inheritanceArray;
	}
	
	/**
	 * Add the inherited fields to the list of fields
	 */
	private void addInheritedFields() {
		for (IInheritance inheritance : inheritances) {
			if (!(inheritance.getType() instanceof ICompositeType))
				continue;
			
			ICompositeType inheritanceType = (ICompositeType) inheritance.getType();
			
			// add the fields of the inherited type
			IField[] inheritanceFields = inheritanceType.getFields();
			long fieldsOffset = inheritance.getFieldsOffset();
			
			for (IField field : inheritanceFields) {
				FieldType newField = new FieldType(field.getName(), scope, field.getCompositeTypeOwner(),
						fieldsOffset + field.getFieldOffset(), field.getBitSize(), field.getBitSize(),
						field.getByteSize(), field.getAccessibility(), field.getProperties());
				newField.setType(field.getType());
				fields.add(newField);
			}
		}
		
		addedInherited = true;
	}

}
