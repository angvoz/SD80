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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InvalidExpression;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.core.runtime.CoreException;

public class ASTEvaluationEngine {

	public static final String UNKNOWN_TYPE = "<UNKNOWN>"; //$NON-NLS-1$

	public InstructionSequence getCompiledExpression(String expression) {

		CodeReader reader = new CodeReader(("void dummy_func() { " + expression + " ; }").toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

		IScannerInfo scannerInfo = new ScannerInfo(); // creates an empty
		// scanner info
		IScanner scanner = new CPreprocessor(reader, scannerInfo, ParserLanguage.CPP, new NullLogService(),
				GCCScannerExtensionConfiguration.getInstance(), NullCodeReaderFactory.getInstance());
		ISourceCodeParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, new NullLogService(),
				GPPParserExtensionConfiguration.getInstance(), null);
		IASTTranslationUnit ast = parser.parse();

		ASTInstructionCompiler visitor = new ASTInstructionCompiler(expression);
		ast.accept(visitor);
		return visitor.getInstructions();

	}

	public Interpreter evaluateCompiledExpression(InstructionSequence expression, Object context) {
		Interpreter interpreter = new Interpreter(expression, context);
		try {
			interpreter.execute();
		} catch (CoreException ce) {
			// if an exception occurred, the exception message will be the
			// result
			interpreter.setLastValue(new InvalidExpression(ce.getMessage()));
		}

		return interpreter;
	}

}
