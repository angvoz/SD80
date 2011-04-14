/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Emanuel Graf IFS - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.togglefunction.messages"; //$NON-NLS-1$
	public static String DeclaratorFinder_NestedFunction;
	public static String DeclaratorFinder_NoDeclarator;
	public static String DeclaratorFinder_MultipleDeclarators;
	public static String RefactoringJob_UndoName;
	public static String ToggleFileCreator_andMove;
	public static String ToggleFileCreator_CanNotCreateNewFile;
	public static String ToggleFileCreator_CreateNewFile;
	public static String ToggleFileCreator_NewImplFile;
	public static String ToggleFileCreator_NoTuForSibling;
	public static String ToggleFileCreator_QMark;
	public static String ToggleFromClassToInHeaderStrategy_DefAndDecInsideClass;
	public static String EditGroupName;
	public static String ToggleFromImplementationToHeaderOrClassStrategy_CanNotCreateNewFile;
	public static String ToggleFromImplementationToHeaderOrClassStrategy_CanNotToggle;
	public static String ToggleFromInHeaderToClassStrategy_CanNotToggleTemplateFreeFunction;
	public static String ToggleFromInHeaderToImplementationStrategy_CanNotCreateImplFile;
	public static String ToggleRefactoring_AnalyseSelection;
	public static String ToggleRefactoring_CalculateModifications;
	public static String ToggleRefactoring_CanNotSaveFiles;
	public static String ToggleRefactoring_InvalidSelection;
	public static String ToggleRefactoring_NoIndex;
	public static String ToggleRefactoring_WaitingForIndexer;
	public static String ToggleRefactoringContext_MultipleDeclarations;
	public static String ToggleRefactoringContext_MultipleDefinitions;
	public static String ToggleRefactoringContext_NoDefinitionFound;
	public static String ToggleRefactoringContext_NoTuFound;
	public static String ToggleStrategyFactory_NoDefinitionFound;
	public static String ToggleStrategyFactory_UnsupportedSituation;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
