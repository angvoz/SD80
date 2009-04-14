/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterSetterInsertEditProvider.Type;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class GetterAndSetterContext implements ITreeContentProvider{
	
	public ArrayList<IASTSimpleDeclaration> existingFields = new ArrayList<IASTSimpleDeclaration>();
	public ArrayList<IASTFunctionDefinition> existingFunctionDefinitions = new ArrayList<IASTFunctionDefinition>();
	public ArrayList<IASTSimpleDeclaration> existingFunctionDeclarations = new ArrayList<IASTSimpleDeclaration>();
	public SortedSet<GetterSetterInsertEditProvider> selectedFunctions = new TreeSet<GetterSetterInsertEditProvider>();
	private IASTTranslationUnit unit;
	public IASTName selectedName;
	private ArrayList<FieldWrapper> wrappedFields;

	public Object[] getChildren(Object parentElement) {

		ArrayList<GetterSetterInsertEditProvider> children = new ArrayList<GetterSetterInsertEditProvider>();
		if (parentElement instanceof FieldWrapper) {
			FieldWrapper wrapper = (FieldWrapper) parentElement;
			
			if(wrapper.getChildNodes().isEmpty()) {
				if(!wrapper.getter.exists()){
					wrapper.childNodes.add(createGetterInserter(wrapper.field));
				}
				if(!wrapper.setter.exists() && !wrapper.field.getDeclSpecifier().isConst()){
					wrapper.childNodes.add(createSetterInserter(wrapper.field));
				}
			}
			children = wrapper.getChildNodes();
		}
		return children.toArray();
	}

	public GetterSetterInsertEditProvider createGetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		String varName = simpleDeclaration.getDeclarators()[0].getName().toString();
		IASTFunctionDefinition getter = FunctionFactory.createGetter(varName, simpleDeclaration);
		getter.setParent(unit);
		return new GetterSetterInsertEditProvider(getter, Type.getter);
	}

	public GetterSetterInsertEditProvider createSetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		String varName = simpleDeclaration.getDeclarators()[0].getName().toString();
		IASTFunctionDefinition setter = FunctionFactory.createSetter(varName, simpleDeclaration);
		setter.setParent(unit);
		return new GetterSetterInsertEditProvider(setter, Type.setter);
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof FieldWrapper) {
			FieldWrapper wrapper = (FieldWrapper) element;
			
			return wrapper.missingGetterOrSetter();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		
		return getWrappedFields().toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}
		
	public void setUnit(IASTTranslationUnit unit) {
		this.unit = unit;
	}
	
	private ArrayList<FieldWrapper> getWrappedFields() {
		if(wrappedFields == null) {
			wrappedFields = new ArrayList<FieldWrapper>();
			for(IASTSimpleDeclaration currentField : existingFields){
				FieldWrapper wrapper = new FieldWrapper();
				wrapper.field = currentField;
				wrapper.getter = getGetterForField(currentField);
				wrapper.setter = getSetterForField(currentField);
				if(wrapper.missingGetterOrSetter()){
					wrappedFields.add(wrapper);
				}
			}
		}
		return wrappedFields;
	}

	private FunctionWrapper getGetterForField(IASTSimpleDeclaration currentField) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String trimmedName = NameHelper.trimFieldName(currentField.getDeclarators()[0].getName().toString());
		String getterName = "get" + NameHelper.makeFirstCharUpper(trimmedName); //$NON-NLS-1$
		
		setFunctionToWrapper(wrapper, getterName);
		
		return wrapper;
	}
	
	private FunctionWrapper getSetterForField(IASTSimpleDeclaration currentField) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String trimmedName = NameHelper.trimFieldName(currentField.getDeclarators()[0].getName().toString());
		String setterName = "set" + NameHelper.makeFirstCharUpper(trimmedName); //$NON-NLS-1$
		
		setFunctionToWrapper(wrapper, setterName);
		
		return wrapper;
	}
	private void setFunctionToWrapper(FunctionWrapper wrapper, String getterName) {
		for(IASTFunctionDefinition currentDefinition : existingFunctionDefinitions){
			if(currentDefinition.getDeclarator().getName().toString().endsWith(getterName)){
				wrapper.functionDefinition = currentDefinition;
			}
		}
		
		for(IASTSimpleDeclaration currentDeclaration : existingFunctionDeclarations){
			if(currentDeclaration.getDeclarators()[0].getName().toString().endsWith(getterName)){
				wrapper.functionDeclaration = currentDeclaration;
			}
		}
	}


	protected class FieldWrapper{
		protected IASTSimpleDeclaration field;
		protected FunctionWrapper getter;
		protected FunctionWrapper setter;
		protected ArrayList<GetterSetterInsertEditProvider> childNodes = new ArrayList<GetterSetterInsertEditProvider>(2);
		
		@Override
		public String toString(){
			return field.getDeclarators()[0].getName().toString();
		}

		public ArrayList<GetterSetterInsertEditProvider> getChildNodes() {
			return childNodes;
		}

		public boolean missingGetterOrSetter() {
			return !getter.exists() || !setter.exists();
		}
	}
	
	protected class FunctionWrapper{
		protected IASTSimpleDeclaration functionDeclaration;
		protected IASTFunctionDefinition functionDefinition;
		
		public boolean exists() {

			return functionDeclaration != null || functionDefinition != null;
		}
	}
}
