/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ExtractConstantRefactoringDescription extends
		CRefactoringDescription {
	protected static final String NAME = "name"; //$NON-NLS-1$
	protected static final String VISIBILITY = "visibility"; //$NON-NLS-1$
	
	protected ExtractConstantRefactoringDescription(String project,
			String description, String comment, Map<String, String> arguments) {
		super(ExtractConstantRefactoring.ID, project, description, comment, RefactoringDescriptor.MULTI_CHANGE, arguments);
	}
	
	@Override
	public Refactoring createRefactoring(RefactoringStatus status)
			throws CoreException {
		IFile file;
		ExtractConstantInfo info = new ExtractConstantInfo();
		ICProject proj;
		
		info.setName(arguments.get(NAME));
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(arguments.get(VISIBILITY)));
		
		proj = getCProject();
		
		file = getFile();
		
		ISelection selection = getSelection();
		return new ExtractConstantRefactoring(file, selection, info, proj);
	}

}
