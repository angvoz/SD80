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
import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.core.dom.parser.c99.C99ParseResult;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.LocationResolver;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, adds C99 as a language to CDT.
 *
 * @author Mike Kucera
 */
public class C99Language extends AbstractLanguage implements ILanguage, ICLanguageKeywords {
	
	// TODO: this should probably go somewhere else
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.c99"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".c99"; //$NON-NLS-1$ 
	
	private static C99KeywordMap keywordMap = new C99KeywordMap();
	
	private static C99Language myDefault = new C99Language();
	
	
	public static C99Language getDefault() {
		return myDefault;
	}
	
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		else
			return super.getAdapter(adapter);
	}
	
	/**
	 * Returns a keyword map that will provide the information necessary
	 * to implement the methods in ICLanguageKeywords.
	 * 
	 * Can be overridden in subclasses to provide additional keywords
	 * for language extensions.
	 */
	protected C99KeywordMap getKeywordMap() {
		return keywordMap;
	}
	
	
	/**
	 * Retreive the token parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a langauge extension.
	 */
	protected IParser getParser() {
		return new C99Parser();
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

	
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		
		IParseResult parseResult = parse(reader, scanInfo, fileCreator, index, null, options);
		IASTTranslationUnit tu = parseResult.getTranslationUnit();

		return tu;
	}
	
	
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {
		
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	
	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) {
		
		IParseResult parseResult = completionParse(reader, scanInfo, fileCreator, index, offset);
		IASTCompletionNode node = parseResult.getCompletionNode();
		return node;
	}
	
	
	
	/**
	 * Preform the actual parse.
	 */
	public IParseResult parse(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, Integer contentAssistOffset) {
		return parse(reader, scanInfo, fileCreator, index, contentAssistOffset, 0);
	}
	
	/**
	 * Preform the actual parse.
	 */
	public IParseResult parse(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, Integer contentAssistOffset, int options) {

		boolean scanComments = (options & OPTION_ADD_COMMENTS) != 0;
		int preprocessorOptions = scanComments ? C99Preprocessor.OPTION_GENERATE_COMMENTS_FOR_ACTIVE_CODE : 0;
		
		ILexerFactory lexerFactory = new C99LexerFactory();
		C99Preprocessor preprocessor = new C99Preprocessor(lexerFactory, reader, scanInfo, fileCreator, preprocessorOptions);
		IParser parser = getParser();

		LocationResolver resolver = new LocationResolver();
		
		// the preprocessor injects tokens into the parser
		if (contentAssistOffset == null)
			preprocessor.preprocess(parser, resolver);
		else
			preprocessor.preprocess(parser, resolver, contentAssistOffset.intValue());

		if(preprocessor.encounteredError())
			return new C99ParseResult(null, null, true);

		// bit of a CDT AST specific hack
		IParseResult result = parser.parse();
		if(result.getTranslationUnit() instanceof CASTTranslationUnit) {
			CASTTranslationUnit tu = (CASTTranslationUnit) result.getTranslationUnit();
			tu.setIndex(index);
			tu.setLocationResolver(resolver);
		}

		return result;
	}

	
	
	/**
	 * Public so that these methods may be called directly from unit tests.
	 */
	public IParseResult parse(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index) {

		return parse(reader, scanInfo, fileCreator, index, null);
	}

	
	/**
	 * Public so that these methods may be called directly from unit tests.
	 */
	public IParseResult completionParse(CodeReader reader,
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
		return getKeywordMap().getBuiltinTypes();
	}


	public String[] getKeywords() {
		return getKeywordMap().getKeywords();
	}


	public String[] getPreprocessorKeywords() {
		return getKeywordMap().getPreprocessorKeywords();
	}

}
