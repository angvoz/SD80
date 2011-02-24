/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class SuspiciousSemicolonChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.SuspiciousSemicolonProblem"; //$NON-NLS-1$
	public static final String PARAM_ELSE = "else"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					IASTStatement thenStmt = ((IASTIfStatement) statement).getThenClause();
					IASTStatement elseStmt = ((IASTIfStatement) statement).getElseClause();
					if (elseStmt != null && doNotReportIfElse() == true)
						return PROCESS_CONTINUE;
					if (thenStmt instanceof IASTNullStatement && noMacroInvolved(thenStmt)) {
						reportProblem(ER_ID, thenStmt, (Object) null);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	protected boolean doNotReportIfElse() {
		final IProblem pt = getProblemById(ER_ID, getFile());
		return (Boolean) getPreference(pt, PARAM_ELSE);
	}

	protected boolean noMacroInvolved(IASTStatement node) {
		IASTNodeSelector nodeSelector = node.getTranslationUnit().getNodeSelector(node.getTranslationUnit().getFilePath());
		IASTFileLocation fileLocation = node.getFileLocation();
		IASTPreprocessorMacroExpansion macro = nodeSelector.findEnclosingMacroExpansion(fileLocation.getNodeOffset() - 1, 1);
		return macro == null;
	}

	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_ELSE, CheckersMessages.SuspiciousSemicolonChecker_ParamElse, Boolean.FALSE);
	}
}
