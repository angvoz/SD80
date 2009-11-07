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

	// Kind of composite (class, struct, union)
	private final int key;

	protected ArrayList<IField> fields = new ArrayList<IField>();

	public CompositeType(String name, IScope scope, int key, int byteSize, Map<Object, Object> properties, String prefix) {
		super(name, scope, byteSize, properties);
		this.name = prefix + " " + this.name; //$NON-NLS-1$
		this.key = key;
	}

	public int getKey() {
		return this.key;
	}

	public int fieldCount() {
		return fields.size();
	}

	public void addField(IField field) {
		fields.add(field);
	}

	public IField[] getFields() {
		IField[] fieldArray = new IField[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			fieldArray[i] = fields.get(i);
		}
		return fieldArray;
	}

	public IField findField(String name) {
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

}
