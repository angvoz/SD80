/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;

import org.eclipse.cdt.internal.ui.text.util.CWordDetector;


/**
 * A C code scanner.
 */
public final class CppCodeScanner extends AbstractCScanner {

	private static String[] fgConstants= { "NULL", 			//$NON-NLS-1$  
			                               "__DATE__", "__LINE__", "__TIME__", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
										   "__FILE__", "__STDC__"};				//$NON-NLS-1$ //$NON-NLS-2$ 
	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
        ICColorConstants.C_OPERATOR,
        ICColorConstants.C_BRACES,
        ICColorConstants.C_NUMBER,
		ICColorConstants.C_DEFAULT
	};
	

	/**
	 * Creates a C++ code scanner
	 */
	public CppCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}
	
	/*
	 * @see AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see AbstractCScanner#createRules()
	 */
	protected List createRules() {
				
		List rules= new ArrayList();		
		Token token;
		
		// Add generic whitespace rule.
		//rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		
		// Add word rule for keywords, types, and constants.
		token= getToken(ICColorConstants.C_DEFAULT);
		WordRule wordRule= new WordRule(new CWordDetector(), token);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		Iterator iter = ParserFactory.getKeywordSet( KeywordSetKey.KEYWORDS, ParserLanguage.CPP ).iterator();
		while( iter.hasNext() )
			wordRule.addWord((String)iter.next(), token);
		token= getToken(ICColorConstants.C_TYPE);
		iter = ParserFactory.getKeywordSet( KeywordSetKey.TYPES, ParserLanguage.CPP ).iterator();
		while( iter.hasNext() )
			wordRule.addWord((String) iter.next(), token);
		for (int i=0; i<fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], token);
		rules.add(wordRule);

		token = getToken(ICColorConstants.C_TYPE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), token);
		iter = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.CPP ).iterator();
		
		while( iter.hasNext() )
			preprocessorRule.addWord((String) iter.next(), token);
		
		rules.add(preprocessorRule);

        token = getToken(ICColorConstants.C_NUMBER);
        NumberRule numberRule = new NumberRule(token);
        rules.add(numberRule);
        
        token = getToken(ICColorConstants.C_OPERATOR);
        COperatorRule opRule = new COperatorRule(token);
        rules.add(opRule);

        token = getToken(ICColorConstants.C_BRACES);
        CBraceRule braceRule = new CBraceRule(token);
        rules.add(braceRule);
        
		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	public void setRules(IRule[] rules) {
		super.setRules(rules);	
	}

	/*
	 * @see AbstractCScanner#affectsBehavior(PropertyChangeEvent)
	 */	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return super.affectsBehavior(event);
	}

	/*
	 * @see AbstractCScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
			
		if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
	
}