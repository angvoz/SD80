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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class ASTEvaluationEngine {

	public static final String UNKNOWN_TYPE = "<UNKNOWN>"; //$NON-NLS-1$
	private final DsfServicesTracker tracker;
	private final IDMContext context;
	private final TypeEngine typeEngine;

	/**
	 * @param context 
	 * @param tracker 
	 * 
	 */
	public ASTEvaluationEngine(DsfServicesTracker tracker, IDMContext context, TypeEngine typeEngine) {
		this.tracker = tracker;
		this.context = context;
		this.typeEngine = typeEngine;
	}
	
	public InstructionSequence getCompiledExpression(String expression) throws CoreException {

		FileContent reader = FileContent.create("<edc-expression>", ("void* dummy_func() { return " + //$NON-NLS-1$ //$NON-NLS-2$
							expression + " ; }").toCharArray()); //$NON-NLS-1$
		IScannerInfo scannerInfo = new ScannerInfo(); // creates an empty scanner info
		IScanner scanner = new CPreprocessor(reader, scannerInfo, ParserLanguage.CPP, new NullLogService(), GCCScannerExtensionConfiguration.getInstance(), IncludeFileContentProvider.getEmptyFilesProvider());
		ISourceCodeParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, new NullLogService(), GPPParserExtensionConfiguration.getInstance(), null);
		IASTTranslationUnit ast = parser.parse();

		ASTInstructionCompiler visitor = new ASTInstructionCompiler(expression);
		ast.accept(visitor);
		if (visitor.hasErrors())
			throw EDCDebugger.newCoreException(visitor.getErrorMessage());
		
		visitor.fixupInstructions(typeEngine);
		
		return visitor.getInstructions();

	}

	public Interpreter evaluateCompiledExpression(InstructionSequence expression) throws CoreException {
		Interpreter interpreter = new Interpreter(tracker, context, typeEngine, expression);
		interpreter.execute();
		return interpreter;
	}

	/**
	 * Get the type engine
	 * @return
	 */
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}

	
	static private class ASTTypeVisitor extends ASTVisitor {
		private IASTTypeId theType;
		private String errorMessage;
		
		{
			shouldVisitTypeIds = true;
			shouldVisitProblems = true;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		@Override
		public int visit(IASTTypeId typeId) {
			theType = typeId;
			return PROCESS_ABORT;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTProblem)
		 */
		@Override
		public int visit(IASTProblem problem) {
			errorMessage = problem.getMessage();
			return PROCESS_ABORT;
		}
	}
	/**
	 * Parse the given type string and get the AST tree for it
	 * @param type
	 * @return IASTTypeId instance
	 * @throws CoreException
	 */
	public IASTTypeId getCompiledType(String type) throws CoreException {

		FileContent reader = FileContent.create("<edc-expression>", ("void* dummy_func() { typeof(" + //$NON-NLS-1$ //$NON-NLS-2$
							type + ") x; }").toCharArray()); //$NON-NLS-1$
		IScannerInfo scannerInfo = new ScannerInfo(); // creates an empty scanner info
		IScanner scanner = new CPreprocessor(reader, scannerInfo, ParserLanguage.CPP, new NullLogService(), GCCScannerExtensionConfiguration.getInstance(), IncludeFileContentProvider.getEmptyFilesProvider());
		ISourceCodeParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, new NullLogService(), GPPParserExtensionConfiguration.getInstance(), null);
		IASTTranslationUnit ast = parser.parse();

		ASTTypeVisitor visitor = new ASTTypeVisitor();
		ast.accept(visitor);
		if (visitor.errorMessage != null)
			throw EDCDebugger.newCoreException(visitor.errorMessage);
		if (visitor.theType == null)
			throw EDCDebugger.newCoreException(ASTEvalMessages.ASTEvaluationEngine_DidNotDetectType);
		
		return visitor.theType;
	}

}
