/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ImplementMethodData implements ITreeContentProvider{

	public ImplementMethodData() {
	}


	private List<MethodToImplementConfig> methodDeclarations;

	public void setMethodDeclarations(List<IASTSimpleDeclaration> methodDeclarations) {
		this.methodDeclarations = new ArrayList<MethodToImplementConfig>();
		
		for (IASTSimpleDeclaration declaration : methodDeclarations) {
			this.methodDeclarations.add(new MethodToImplementConfig(declaration, new ParameterHandler(declaration)));
		}
	}

	public List<MethodToImplementConfig> getMethodDeclarations() {
		return methodDeclarations;
	}

	public Object[] getChildren(Object parentElement) {
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return methodDeclarations.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}
	
	public List<MethodToImplementConfig> getMethodsToImplement() {
		List<MethodToImplementConfig> ret = new ArrayList<MethodToImplementConfig>();
		for (MethodToImplementConfig config : methodDeclarations) {
			if(config.isChecked()) {
				ret.add(config);
			}
		}
		return ret;
	}
	
	public boolean needParameterInput() {
		for (MethodToImplementConfig config : getMethodsToImplement()) {
			if(config.getParaHandler().needsAdditionalArgumentNames())return true;
		}
		return false;
	}
	
	public MethodToImplementConfig getNextConfigNeedingParameterNames(MethodToImplementConfig currentConfig) {
		int i = 0;
		List<MethodToImplementConfig> methodsToImplement = getMethodsToImplement();
		for(;i < methodsToImplement.size();++i) {
			if(currentConfig == methodsToImplement.get(i)) {
				++i;
				break;
			}
		}
		
		for(;i < methodsToImplement.size();++i) {
			if(methodsToImplement.get(i).getParaHandler().needsAdditionalArgumentNames()) {
				return methodsToImplement.get(i);
			}
		}
		return null;
	}


	public MethodToImplementConfig getFirstConfigNeedingParameterNames() {
		List<MethodToImplementConfig> methodsToImplement = getMethodsToImplement();
		for (MethodToImplementConfig config : methodsToImplement) {
			if(config.getParaHandler().needsAdditionalArgumentNames()) {
				return config;
			}
		}
		return null;
	}

}
