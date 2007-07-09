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

import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.core.dom.parser.c99.GCCPreprocessorExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c99.ITokenMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99TokenMap;


/**
 * Implementation of the ILanguage extension point, 
 * adds C99 as a language to CDT.
 *
 * @author Mike Kucera
 */
public class C99Language extends BaseExtensibleLanguage {
	
	protected static final IPreprocessorExtensionConfiguration 
		GCC_PREPROCESSOR_EXTENSION = new GCCPreprocessorExtensionConfiguration();
	
	
	// TODO: this should probably go somewhere else
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.c99"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".c99"; //$NON-NLS-1$ 
	
	private static final C99KeywordMap keywordMap = new C99KeywordMap();
	private static final C99Language myDefault    = new C99Language();
	
	
	public static C99Language getDefault() {
		return myDefault;
	}
	
	public String getId() {
		return ID;
	}

	public String getName() {
		// TODO: this has to be read from a message bundle
		return "C99"; //$NON-NLS-1$
	}
	
	protected IKeywordMap getKeywordMap() {
		return keywordMap;
	}
	
	protected IParser getParser() {
		return new C99Parser();
	}

	protected IPreprocessorExtensionConfiguration getPreprocessorExtensionConfiguration() {
		return GCC_PREPROCESSOR_EXTENSION;
	}
	
	protected ILexerFactory getLexerFactory() {
		return new C99LexerFactory();
	}
	
	protected ITokenMap getTokenMap() {
		return new C99TokenMap();
	} 
}
