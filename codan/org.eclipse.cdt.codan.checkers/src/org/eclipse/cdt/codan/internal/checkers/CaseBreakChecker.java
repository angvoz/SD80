/*******************************************************************************
 * Copyright (c) 2010,2011 Gil Barash 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Gil Barash  - Initial implementation
 *    Elena laskavaia - Rewrote checker to reduce false positives in complex cases
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class CaseBreakChecker extends AbstractIndexAstChecker implements ICheckerWithPreferences {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.CaseBreakProblem"; //$NON-NLS-1$
	public static final String PARAM_LAST_CASE = "last_case_param"; //$NON-NLS-1$
	public static final String PARAM_EMPTY_CASE = "empty_case_param"; //$NON-NLS-1$
	public static final String PARAM_NO_BREAK_COMMENT = "no_break_comment"; //$NON-NLS-1$
	public static final String DEFAULT_NO_BREAK_COMMENT = "no break"; //$NON-NLS-1$
	private Boolean _checkLastCase; // Should we check the last case in the switch?
	private Boolean _checkEmptyCase; // Should we check an empty case (a case without any statements within it)
	private String _noBreakComment; // The comment suppressing this warning 

	/**
	 * This visitor looks for "switch" statements and invokes "SwitchVisitor" on
	 * them.
	 */
	class SwitchFindingVisitor extends ASTVisitor {
		SwitchFindingVisitor() {
			shouldVisitStatements = true;
		}

		/**
		 * @param statement
		 * @return true iff the statement is on of:
		 *         - "break" (checks that the break actually exists the
		 *         "switch")
		 *         - "return"
		 *         - "continue"
		 *         - "goto" (does not check that the goto actually exists the
		 *         switch)
		 *         - "thorw"
		 *         - "exit"
		 */
		protected boolean isBreakOrExitStatement(IASTStatement statement) {
			CxxAstUtils utils = CxxAstUtils.getInstance();
			return (statement instanceof IASTBreakStatement) || statement instanceof IASTReturnStatement
					|| statement instanceof IASTContinueStatement || statement instanceof IASTGotoStatement
					|| utils.isThrowStatement(statement) || utils.isExitStatement(statement);
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTSwitchStatement) {
				IASTSwitchStatement switchStmt = (IASTSwitchStatement) statement;
				IASTStatement body = switchStmt.getBody();
				if (body instanceof IASTCompoundStatement) {
					// if not it is not really a switch
					IASTStatement[] statements = ((IASTCompoundStatement) body).getStatements();
					IASTStatement prevCase = null;
					for (int i = 0; i < statements.length; i++) {
						IASTStatement curr = statements[i];
						if (curr instanceof IASTSwitchStatement) {
							visit(curr);
						}
						IASTStatement next = null;
						if (i < statements.length - 1)
							next = statements[i + 1];
						if (isCaseStatement(curr)) {
							prevCase = curr;
						}
						// next is case or end of switch - means this one is the last
						if (prevCase != null && (isCaseStatement(next) || next == null)) {
							// check that current statement end with break or any other exit statement
							if (!_checkEmptyCase && isCaseStatement(curr) && next != null) {
								continue; // empty case & we don't care
							}
							if (!_checkLastCase && next == null) {
								continue; // last case and we don't care
							}
							if (isFallThroughStamement(curr)) {
								IASTComment comment = null;
								if (next != null) {
									comment = getLeadingComment(next);
								} else {
									comment = getFreestandingComment(statement);
									if (comment == null)
										comment = getFreestandingComment(body);
								}
								if (comment != null) {
									String str = getTrimmedComment(comment);
									if (str.equalsIgnoreCase(_noBreakComment))
										continue;
								}
								reportProblem(ER_ID, prevCase, (Object) null);
							}
						}
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * @param statement
		 * @return
		 */
		public boolean isCaseStatement(IASTStatement statement) {
			return statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement;
		}

		/**
		 * @param nextstatement
		 * @return
		 */
		public boolean isFallThroughStamement(IASTStatement body) {
			if (body == null) return true;
			if (body instanceof IASTCompoundStatement) {
				IASTStatement[] statements = ((IASTCompoundStatement) body).getStatements();
				return isFallThroughStamement(statements[statements.length - 1]);
			} else if (isBreakOrExitStatement(body)) {
				return false;
			} else if (body instanceof IASTExpressionStatement) {
				return true;
			} else if (body instanceof IASTIfStatement) {
				IASTIfStatement ifs = (IASTIfStatement) body;
				return isFallThroughStamement(ifs.getThenClause()) || isFallThroughStamement(ifs.getElseClause()) ;
			}
			return true; // TODO
		}
	}

	public CaseBreakChecker() {
	}

	/**
	 * @param comment
	 * @return
	 */
	public String getTrimmedComment(IASTComment comment) {
		String str = new String(comment.getComment());
		if (comment.isBlockComment())
			str = str.substring(2, str.length() - 2);
		else
			str = str.substring(2);
		str = str.trim();
		return str;
	}

	/**
	 * @param statement
	 * @return
	 */
	public IASTComment getLeadingComment(IASTStatement statement) {
		return getCommentMap().getLastLeadingCommentForNode(statement);
	}

	/**
	 * @param statement
	 * @return
	 */
	public IASTComment getFreestandingComment(IASTStatement statement) {
		return getCommentMap().getLastFreestandingCommentForNode(statement);
	}

	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_NO_BREAK_COMMENT, CheckersMessages.CaseBreakChecker_DefaultNoBreakCommentDescription,
				DEFAULT_NO_BREAK_COMMENT);
		addPreference(problem, PARAM_LAST_CASE, CheckersMessages.CaseBreakChecker_LastCaseDescription, Boolean.TRUE);
		addPreference(problem, PARAM_EMPTY_CASE, CheckersMessages.CaseBreakChecker_EmptyCaseDescription, Boolean.FALSE);
	}

	public void processAst(IASTTranslationUnit ast) {
		_checkLastCase = (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_LAST_CASE);
		_checkEmptyCase = (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_EMPTY_CASE);
		_noBreakComment = (String) getPreference(getProblemById(ER_ID, getFile()), PARAM_NO_BREAK_COMMENT);
		SwitchFindingVisitor visitor = new SwitchFindingVisitor();
		ast.accept(visitor);
	}
}
