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

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.edc.symbols.IScope;

public class CompositeType extends MayBeQualifiedType implements ICompositeType {
	
	// kind of composite (class, struct, union)
	private final int key;
	
	// composite name without "class ", "struct " or "union " prefix
	private String baseName;

	// fields in the composite 
	protected ArrayList<IField> fields = new ArrayList<IField>();
	
	// classes inherited from
	protected ArrayList<IInheritance> inheritances = null;
	
	// template parameters
	protected ArrayList<ITemplateParam> templateParams = null;
	boolean nameIncludesTemplateParams = false;

	public CompositeType(String name, IScope scope, int key, int byteSize, Map<Object, Object> properties, String prefix) {
		super(name, scope, byteSize, properties);
		this.baseName = name;
		this.name = prefix + " " + name; //$NON-NLS-1$
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
		ArrayList<IField> fieldList = new ArrayList<IField>(fields);
		
		return fieldList.toArray(new IField[fields.size()]);
	}

	public void addTemplateParam(ITemplateParam templateParam) {
		if (templateParams == null) {
			templateParams = new ArrayList<ITemplateParam>(2);
		}
		templateParams.add(templateParam);
	}

	public ITemplateParam[] getTemplateParams() {
		if (templateParams == null)
			return new ITemplateParam[0];

		ArrayList<ITemplateParam> templateParamList = new ArrayList<ITemplateParam>(templateParams);

		return templateParamList.toArray(new ITemplateParam[templateParams.size()]);
	}

	@Override
	public String getName() {
		if (templateParams != null && !nameIncludesTemplateParams)
			addTemplateStringToNames();
		return name;
	}
	
	public String getBaseName() {
		if (templateParams != null && !nameIncludesTemplateParams)
			addTemplateStringToNames();
		return baseName;
	}

	// add template parameters (e.g. "<Long>") to name and base name
	private void addTemplateStringToNames() {
		nameIncludesTemplateParams = true;
		String templateName = "<"; //$NON-NLS-1$
		for (int i = 0; i < templateParams.size(); i++) {
			templateName += templateParams.get(i).getName();
			if (i + 1 < templateParams.size())
				templateName += ","; //$NON-NLS-1$
		}
		templateName += ">"; //$NON-NLS-1$
		// remove composite type names (e.g., "class")
		templateName = templateName.replaceAll("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		templateName = templateName.replaceAll("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		templateName = templateName.replaceAll("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		name += templateName;
		baseName += templateName;
	}

	public int inheritanceCount() {
		return inheritances == null ? 0 : inheritances.size();
	}

	public void addInheritance(IInheritance inheritance) {
		if (inheritances == null)
			inheritances = new ArrayList<IInheritance>();
		inheritances.add(inheritance);
	}

	public IInheritance[] getInheritances() {
		if (inheritances == null)
			return new IInheritance[0];

		return inheritances.toArray(new IInheritance[inheritances.size()]);
	}	

	public IField[] findFields(String name) {
		// For a qualified name containing "::", save the qualifiers to match against
		String baseFieldName = name;
		ArrayList<String> nameQualifiers = new ArrayList<String>();
		
		if (name.contains("::")) { //$NON-NLS-1$
			StringTokenizer st = new StringTokenizer(name, "::", false); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				baseFieldName = st.nextToken();
				nameQualifiers.add(baseFieldName);
			}
			
			// last token in the array is the base field name
			nameQualifiers.remove(nameQualifiers.size() - 1);

			// if the first nameQualifier is the composite's name, remove it
			// E.g., if we're in class foo, change "foo::x" to "x".
			if ((nameQualifiers.size() >= 0) && nameQualifiers.get(0).equals(this.baseName))
				nameQualifiers.remove(0);
		}

		// try for a fast exit: match against the non-inherited fields and names of
		// composites we're inheriting from
		if (nameQualifiers.size() == 0) {
			for (int i = 0; i < fields.size(); i++) {
				if (((FieldType) fields.get(i)).getName().equals(baseFieldName)) {
					IField[] foundFields = new IField[1];
					foundFields[0] = fields.get(i);
					return foundFields;
				}
			}
			
			if (inheritances != null) {
				for (IInheritance inheritance : inheritances) {
					String inheritanceName = inheritance.getName();
					// for templates, remove composite type names (e.g., "class")
					if (inheritanceName.indexOf('<') != -1) {
						inheritanceName = inheritanceName.replaceAll("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
						inheritanceName = inheritanceName.replaceAll("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
						inheritanceName = inheritanceName.replaceAll("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$
					}

					if (inheritanceName.equals(baseFieldName)) {
						IField[] foundFields = new IField[1];
						
						// treat the inherited type as a field
						FieldType newField = new FieldType(inheritanceName, scope, this,
								inheritance.getFieldsOffset(), 0 /* bitSize */, 0 /* bitOffset */,
								inheritance.getType().getByteSize(), inheritance.getAccessibility(),
								inheritance.getProperties());
						newField.setType(inheritance.getType());

						foundFields[0] = newField;
						return foundFields;
					}
				}
			}
		}
		
		// check the inherited types
		if (inheritances == null)
			return null;

		ArrayList<IField> matches = new ArrayList<IField>();
		
		for (IInheritance inheritance : inheritances) {
			if (inheritance.getType() instanceof ICompositeType) {
				ICompositeType inheritComposite = (ICompositeType)inheritance.getType();
				matches = findInheritedByName(baseFieldName, inheritComposite, inheritComposite.getBaseName(), inheritance.getFieldsOffset(), matches);
			}
		}
		
		// eliminate partial matches
		matches = pruneMatches(nameQualifiers, matches);

		// create the list of all inherited fields
		IField[] foundFields = null;
		
		// gather the names and offsets of the inherited fields
		if (matches.size() > 0) {
			foundFields = new IField[matches.size()];
			for (int i = 0; i < matches.size(); i++) {
				foundFields[i] = matches.get(i);
			}
		}
		
		return foundFields;
	}
	
	/**
	 * From a list of fields whose name matches the one we're looking for, remove those
	 * whose "::" qualifiers do not match. E.g., "foo::x" would match "bar::foo::x", but
	 * it would not match "bar::x" - so "bar::x" would be pruned.
	 *  
	 * @param nameQualifiers qualifiers of the field we're matching against
	 * @param matches list of fields whose base name matches, but whose qualifiers may not match
	 * @return list of fields whose base name and qualifiers match the field we're looking for
	 */
	private ArrayList<IField> pruneMatches(ArrayList<String> nameQualifiers, ArrayList<IField> matches) {
		if (nameQualifiers.size() == 0)
			return matches;

		for (int i = 0; i < matches.size(); i++) {
			ArrayList<String> matchQualifiers = new ArrayList<String>();
			String matchName = matches.get(i).getName();
			
			if (!matchName.contains("::")) //$NON-NLS-1$
				continue;
			
			// tokenize the match's name
			StringTokenizer st = new StringTokenizer(matchName, "::", false); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				matchQualifiers.add(st.nextToken());
			}
			
			// last token in the array is the base name, which we already know matches
			matchQualifiers.remove(matchQualifiers.size() - 1);

			for (int nameIndex = 0, matchIndex = 0;
					nameIndex < nameQualifiers.size() && matchIndex < matchQualifiers.size();
					nameIndex++) {
				// match against each name qualifier, in order
				boolean found = false;
				while (!found && matchIndex < matchQualifiers.size()) {
					found = nameQualifiers.get(nameIndex).equals(matchQualifiers.get(matchIndex));
					matchIndex++;
				}
				
				// if did not find a qualifier, remove the match
				if (!found) {
					matches.remove(i);
					break;
				}
			}
		}
		
		return matches;
	}

	/**
	 * Find all inherited fields whose base name, ignoring "::" qualifiers, match the search name
	 * 
	 * @param name name to match
	 * @param composite composite type whose fields or inherited fields may match 
	 * @param prefix string of "::" qualifiers so far
	 * @param offset byte offset of the composite from the composite that inherits from it
	 * @param matches list of matches found so far
	 * @return list of matches 
	 */
	private ArrayList<IField> findInheritedByName(String name, ICompositeType composite, String prefix, long offset, ArrayList<IField> matches) {
		IField[] fields = composite.getFields();
		if (fields != null) {
			for (IField field : fields) {
				String fieldName = field.getName();
				
				if (fieldName.equals(name)) {
					// create a field with the prefixed name
					FieldType newField = new FieldType(prefix + "::" + field.getName(), scope, //$NON-NLS-1$
							composite, offset + field.getFieldOffset(), 0 /* bitSize */, 0 /* bitOffset */,
							field.getType().getByteSize(), field.getAccessibility(),
							field.getProperties());
					newField.setType(field.getType());
					matches.add(newField);
					break;
				}
			}
		}

		IInheritance[] compositeInheritances = composite.getInheritances(); 
		if (compositeInheritances.length == 0)
			return matches;

		for (IInheritance inheritance : compositeInheritances) {
			if (inheritance.getName().equals(name)) {
				// treat the inherited type as a field
				FieldType newField = new FieldType(inheritance.getName(), scope, this,
						offset + inheritance.getFieldsOffset(), 0 /* bitSize */, 0 /* bitOffset */,
						inheritance.getType().getByteSize(), inheritance.getAccessibility(),
						inheritance.getProperties());
				newField.setType(inheritance.getType());
			}

			if (inheritance.getType() instanceof ICompositeType) {
				ICompositeType inheritComposite = (ICompositeType)inheritance.getType();
				matches = findInheritedByName(name, inheritComposite, prefix + "::" + inheritComposite.getBaseName(), //$NON-NLS-1$
								offset + inheritance.getFieldsOffset(), matches);
			}
		}

		return matches;
	}
}
