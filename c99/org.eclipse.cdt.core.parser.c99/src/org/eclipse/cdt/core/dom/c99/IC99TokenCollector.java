package org.eclipse.cdt.core.dom.c99;

import lpg.lpgjavaruntime.IToken;



public interface IC99TokenCollector extends IPreprocessorTokenCollector<IToken> {

	public void setParser(IParser parser);
	public void done(int translationUnitSize);
}
