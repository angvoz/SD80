/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 *
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public abstract class BaseExtensibleLanguage extends AbstractLanguage implements ILanguage, ICLanguageKeywords {
			
	
	private static final boolean DEBUG_PRINT_GCC_AST = true;
	private static final boolean DEBUG_PRINT_AST = true;
	
	/**
	 * Retrieve the parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a language extension.
	 */
	protected abstract IParser getParser();
	
	
	/**
	 * A token map is used to map tokens from the DOM preprocessor
	 * to the tokens defined by an LPG parser.
	 */
	protected abstract ITokenMap getTokenMap();
	
	
	/**
	 * Normally all the AST nodes are created by the parser, but we
	 * need the root node ahead of time.
	 * 
	 * The preprocessor is responsible for creating preprocessor AST nodes,
     * so the preprocessor needs access to the translation unit so that it can
     * set the parent pointers on the AST nodes it creates.
     * 
	 * @return an IASTTranslationUnit object thats empty and will be filled in by the parser
	 */
	protected abstract IASTTranslationUnit createASTTranslationUnit(IIndex index, IScanner preprocessor);
	
	
	/**
	 * Returns the ParserLanguage value that is to be used when creating
	 * an instance of CPreprocessor.
	 * 
	 */
	protected abstract ParserLanguage getParserLanguageForPreprocessor();
	
	
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		
		return super.getAdapter(adapter);
	}
	
	
	
	
	@SuppressWarnings("nls")
	@Override
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		IASTTranslationUnit gtu = null;
		if(DEBUG_PRINT_GCC_AST) {
			ILanguage gppLanguage = GPPLanguage.getDefault();
			gtu = gppLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
			
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("Parsing");
			System.out.println("Options: " + options);
			System.out.println("GPP AST:");
			ASTPrinter.print(gtu);
			System.out.println();
		}

		// TODO temporary
		IScannerExtensionConfiguration config = new GCCScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguageForPreprocessor();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setScanComments((options & OPTION_ADD_COMMENTS) != 0);
		preprocessor.setComputeImageLocations((options & AbstractLanguage.OPTION_NO_IMAGE_LOCATIONS) == 0);
		
		IParser parser = getParser();
		IASTTranslationUnit tu = createASTTranslationUnit(index, preprocessor);
		
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		parser.parse(tu); // the parser will fill in the rest of the AST
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			ASTPrinter.print(tu);
		}
		
		return tu;
	}
	
	
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {
		
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	
	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) throws CoreException {
		
		
		if(DEBUG_PRINT_GCC_AST) {
			ILanguage gppLanguage = GPPLanguage.getDefault();
			IASTCompletionNode cn = gppLanguage.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
			
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("GPP AST:");
			ASTPrinter.print(cn.getTranslationUnit());
			System.out.println();
		}
		
		// TODO temporary
		IScannerExtensionConfiguration config = new GCCScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguageForPreprocessor();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setContentAssistMode(offset);
		
		IParser parser = getParser();
		IASTTranslationUnit tu = createASTTranslationUnit(index, preprocessor);
		
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		// the parser will fill in the rest of the AST
		IASTCompletionNode completionNode = parser.parse(tu);
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			ASTPrinter.print(tu);
			System.out.println();
			System.out.println("Completion Node: " + completionNode);
		}
		
		return completionNode;
	}
	
	
	
	
}
