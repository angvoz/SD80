package org.eclipse.cdt.core.parser.c99.tests;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMPreprocessorInformationTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMPreprocessorInformationTest extends DOMPreprocessorInformationTest {

	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
		//if(lang != ParserLanguage.C)
		//	return super.parse(code, lang);
		
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) throws ParserException {
		//if(lang != ParserLanguage.C)
		//	return super.parse(code, lang, useGNUExtensions);
		
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	//if(lang != ParserLanguage.C)
    	//	return super.parse(code, lang, useGNUExtensions, expectNoProblems);
    	
    	return ParseHelper.parse(code, getLanguage(), expectNoProblems);
    }
    
    
    protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
    
    
}
