package org.eclipse.cdt.core.dom.c99;


import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.SynthesizedToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;

/**
 * An LPG token stream must start with a dummy token and end with an EOF token.
 * @author Mike
 *
 */
public class C99TokenCollector implements IC99TokenCollector {

	private IPreprocessorTokenCollector<IToken> parser;
		
	public void setParser(IParser parser) {
		this.parser = (IPreprocessorTokenCollector<IToken>) parser; // Total HACK!
		this.parser.addToken(Token.DUMMY_TOKEN);
	}
	
	public void addCommentToken(IToken token) {
		parser.addCommentToken(token);
	}

	public void addToken(IToken token) {
		parser.addToken(token);
	}
	
	public void done(int translationUnitSize) {
		parser.addToken(new SynthesizedToken(translationUnitSize, translationUnitSize, C99Parsersym.TK_EOF_TOKEN, ""));
	}

}
