/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;

import org.eclipse.cdt.internal.ui.text.util.CWhitespaceDetector;
import org.eclipse.cdt.internal.ui.text.util.CWordDetector;

/**
 * A scanner for preprocessor directives.
 *
 * @since 4.0
 */
public class CPreprocessorScanner extends AbstractCScanner {

    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.C_MULTI_LINE_COMMENT,
		ICColorConstants.C_KEYWORD,
		ICColorConstants.PP_DIRECTIVE,
		ICColorConstants.PP_DEFAULT,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
        ICColorConstants.PP_HEADER
	};
	
	private boolean fIsCpp;

	/**
	 * Creates a C/C++ preprocessor scanner.
	 * 
     * @param manager  the color manager.
     * @param store  the preference store.
     * @param isCpp  if <code>true</code> C++ keywords are used, else C keywords
	 */
	public CPreprocessorScanner(IColorManager manager, IPreferenceStore store, boolean isCpp) {
		super(manager, store);
		fIsCpp= isCpp;
		initialize();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCScanner#createRules()
	 */
	protected List createRules() {

		Token defaultToken= getToken(ICColorConstants.PP_DEFAULT);

		List rules= new ArrayList();		
		Token token;
		
		// Add generic white space rule.
		rules.add(new WhitespaceRule(new CWhitespaceDetector()));
		
		token= getToken(ICColorConstants.PP_DIRECTIVE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), token);
		Iterator iter;
		if (fIsCpp) {
			iter = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.CPP ).iterator();
		} else {
			iter = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.C ).iterator();
		}
		while( iter.hasNext() )
			preprocessorRule.addWord((String) iter.next(), token);

		// add ## operator
		preprocessorRule.addWord("##", token); //$NON-NLS-1$
		rules.add(preprocessorRule);

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new CWordDetector(), defaultToken);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		if (fIsCpp) {
			iter = ParserFactory.getKeywordSet( KeywordSetKey.KEYWORDS, ParserLanguage.CPP ).iterator();
		} else {
			iter = ParserFactory.getKeywordSet( KeywordSetKey.KEYWORDS, ParserLanguage.C ).iterator();
		}
		while( iter.hasNext() )
			wordRule.addWord((String) iter.next(), token);

		token= getToken(ICColorConstants.C_TYPE);
		iter = ParserFactory.getKeywordSet( KeywordSetKey.TYPES, ParserLanguage.C ).iterator();
		while( iter.hasNext() )
			wordRule.addWord((String) iter.next(), token);
		rules.add(wordRule);
		
        token = getToken(ICColorConstants.PP_HEADER);
        CHeaderRule headerRule = new CHeaderRule(token);
        rules.add(headerRule);

        token = getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
        IRule lineCommentRule = new EndOfLineRule("//", token, '\\', true); //$NON-NLS-1$
        rules.add(lineCommentRule);

        token = getToken(ICColorConstants.C_MULTI_LINE_COMMENT);
        IRule blockCommentRule = new MultiLineRule("/*", "*/", token, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(blockCommentRule);

        token = getToken(ICColorConstants.C_STRING);
        IRule stringRule = new PatternRule("\"", "\"", token, '\\', true, true, true); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(stringRule);

        token = getToken(ICColorConstants.C_STRING);
        IRule charRule = new PatternRule("'", "'", token, '\\', true, true, true); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(charRule);

        setDefaultReturnToken(defaultToken);
		return rules;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

}
