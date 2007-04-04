/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.c99;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.ParseResult;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99KeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, adds C99 as a language to CDT.
 *
 */
public class C99Language extends AbstractLanguage {

	// TODO: this should probably go somewhere else
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.c99"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".c99"; //$NON-NLS-1$ 
	
	private static C99KeywordMap keywordMap = new C99KeywordMap();
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		else
			return super.getAdapter(adapter);
	}
	
	
	public String getId() {
		return ID;
	}

	public String getName() {
		// TODO: this has to be read from a message bundle
		return "C99"; //$NON-NLS-1$
	}
	
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		return null;
	}

	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {

		ParseResult parseResult = parse(reader, scanInfo, fileCreator, index);
		IASTTranslationUnit tu = parseResult.getTranslationUnit();
		return tu;
	}

	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) {

		
		ParseResult parseResult = completionParse(reader, scanInfo, fileCreator, index, offset);
		IASTCompletionNode node = parseResult.getCompletionNode();
		return node;
	}

	
	/**
	 * Preform the actual parse.
	 */
	private static ParseResult parse(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, Integer contentAssistOffset) {

		ILexerFactory lexerFactory = new C99LexerFactory();
		C99Preprocessor preprocessor = new C99Preprocessor(lexerFactory, reader, scanInfo, fileCreator, keywordMap);
		C99Parser parser = new C99Parser();

		// the preprocessor injects tokens into the parser
		ILocationResolver resolver;
		if (contentAssistOffset == null)
			resolver = preprocessor.preprocess(parser);
		else
			resolver = preprocessor.preprocess(parser, contentAssistOffset.intValue());

		if (resolver == null)
			return new ParseResult(null, null, true); 

		parser.prepareToParse();

		IASTTranslationUnit tu = parser.parse(resolver, index);
		boolean encounteredError = parser.encounteredError();
		IASTCompletionNode compNode = parser.getASTCompletionNode();

		return new ParseResult(tu, compNode, encounteredError);
	}

	
	public static ParseResult parse(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index) {

		return parse(reader, scanInfo, fileCreator, index, null);
	}

	public static ParseResult completionParse(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, int offset) {

		return parse(reader, scanInfo, fileCreator, index, new Integer(offset));
	}
	
	
	
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		IASTNode selectedNode= ast.selectNodeForLocation(ast.getFilePath(), start, length);

		if (selectedNode == null)
			return new IASTName[0];

		if (selectedNode instanceof IASTName)
			return new IASTName[] { (IASTName) selectedNode };

		
		final List nameList= new ArrayList();
		
		CASTVisitor nameCollector = new CASTVisitor() {
			{ shouldVisitNames= true; }
			public int visit(IASTName name) {
				nameList.add(name);
				return PROCESS_CONTINUE;
			}
		};
		
		selectedNode.accept(nameCollector);
		return (IASTName[]) nameList.toArray(new IASTName[nameList.size()]);
	}


	public String[] getBuiltinTypes() {
		return keywordMap.getBuiltinTypes();
	}


	public String[] getKeywords() {
		return keywordMap.getKeywords();
	}


	public String[] getPreprocessorKeywords() {
		return keywordMap.getPreprocessorKeywords();
	}


}
