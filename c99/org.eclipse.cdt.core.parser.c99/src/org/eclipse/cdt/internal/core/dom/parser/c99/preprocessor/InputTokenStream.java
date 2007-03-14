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

package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.cdt.core.parser.CodeReader;

import lpg.lpgjavaruntime.IToken;

/**
 * A stack of token context, feeds tokens to the preprocessor.
 * 
 * Unfortunately this class does not support arbitrary lookahead.
 * 
 * This class allows for new sequences of tokens to be added to the
 * preprocessor input, for example when the source of an #include
 * needs to be processed or when a macro is expanded.
 * 
 * This class is also partly responsible for computing adjusted
 * token offsets (for example if a file is included then any tokens
 * after the #include will have their offsets adjusted by the size
 * of the included file).
 * 
 * @author Mike Kucera
 */
class InputTokenStream {
	
	
	private final Stack contextStack = new Stack(); // Stack<Context>
	private Context topContext = null;
	private boolean stuck = false;
	
	// Used to calculate the size of ther resulting translation unit
	private int translationUnitSize = 0;
	

	
	/**
	 * Event that is fired when the content of an included file
	 * is fully consumed. This can be used to notify the preprocessor
	 * that an include has been exited.
	 */
	public interface IIncludeContextCallback {
		void contextClosed();
	}
	
	/**
	 * A frame on the contextStack.
	 * A Context can represent:
	 * - an included file
	 * - an argument to a macro, which should be processed in isolation from the rest of the tokens in the file
	 * - the contents of an include directive (the tokens between #include and the newline)
	 * - the result of a macro invocation
	 */
	private class Context {
		CodeReader reader;
		TokenList tokenList;
		boolean stop = false; // a flag to indicate that processing should stop when the end of the context is reached
		IIncludeContextCallback callback;
		int adjustedGlobalOffset;
		
		/**
		 * @param startingOffset The adjusted offset of the start of the context. Adjusted means that the start offset
		 * of an included file will be adjusted according to the offset of the location where the file was included.
		 */
		Context(CodeReader reader, TokenList tokenList, boolean stop, IIncludeContextCallback callback, int startingOffset) {
			this.reader = reader;
			this.tokenList = tokenList;
			this.stop = stop;
			this.callback = callback;
			this.adjustedGlobalOffset = startingOffset;
		}
	}
	
	
	/**
	 * Pushes the tokens that make up an included file onto the stack.
	 * 
	 * @param tokenList Tokens that comprise the included file
	 * @param reader The CodeReader that contains the source buffer of the included file
	 * @param inclusionLocaionOffset The global offset of the location of the #include directive
	 * @param callback A callback that will be fired when the included file is completely consumed
	 */
	public void pushIncludeContext(TokenList tokenList, CodeReader reader, int inclusionLocaionOffset, IIncludeContextCallback callback) {
		// adjust the offsets of the contexts that are already on the stack
		adjustGlobalOffsets(reader.buffer.length);
		topContext = (Context) contextStack.push(new Context(reader, tokenList, false, callback, inclusionLocaionOffset));
	}

	
	/**
	 * Pushes the tokens that are the result of a macro invocation.
	 * These tokens are made available for further processing.
	 */
	public void pushMacroExpansionContext(TokenList expansion, int expansionLocationOffset) {
		if(expansion == null || expansion.isEmpty())
			return;
		
		int expansionSize = expansion.last().getEndOffset() - expansion.first().getStartOffset() + 1;
		System.out.println("expansionLocationOffset: " + expansionLocationOffset + ", " + expansionSize);
		adjustGlobalOffsets(expansionSize);
		if(expansion != null)
			topContext = (Context) contextStack.push(new Context(topContext.reader, expansion, false, null, expansionLocationOffset));
	}
	
	
	private void adjustGlobalOffsets(int contextSize) {
		for(int i = 0; i < contextStack.size(); i++) {
			Context context = (Context) contextStack.get(i);
			context.adjustedGlobalOffset += contextSize;
		}
		translationUnitSize += contextSize;
	}
	
	/**
	 * A stop context is used when the given tokenList should be processed in isolation,
	 * for example a macro argument or the tokens in an include directive.
	 */
	public void pushStopContext(TokenList tokenList) {
		// in this case the offset of the context does not really matter, so just set it to 0
		if(tokenList != null)
			topContext = (Context) contextStack.push(new Context(topContext.reader, tokenList, true, null, 0));
	}
	
	
	public int adjust(int localOffset) {
		return localOffset + topContext.adjustedGlobalOffset;
	}
	
	public String getCurrentFileName() {
		return new String(topContext.reader.filename);
	}
	
	public File getCurrentDirectory() {
		 File file = new File(getCurrentFileName());
         return file.getParentFile();
	}

	public void addTokenToFront(IToken token) {
		topContext.tokenList.addFirst(token);
	}
	
	public void resume() {
		stuck = false;
	}
	
	public boolean isStuck() {
		return stuck;
	}
	
	/**
	 * This will only return an accurate value when the preprocessor
	 * is at the very end of the input (when it is time to create an EOF token).
	 */
	public int getTranslationUnitSize() {
		return translationUnitSize;
	}
	
	
	public IToken peek() {
		return tokenSearch(true);
	}
	
	
	public IToken next() {
		IToken token = tokenSearch(false);
		int offset = topContext.adjustedGlobalOffset;
		token.setStartOffset(token.getStartOffset() + offset);
		token.setEndOffset(token.getEndOffset() + offset);
		return token;
	}
	
	
	private IToken tokenSearch(boolean peek) {
		if(stuck || topContext == null)
			return null;
		
		TokenList topList = topContext.tokenList;
		
		if(topList.isEmpty()) {
			if(topContext.stop)
				stuck = true;
			if(topContext.callback != null)
				topContext.callback.contextClosed();
			popContext();
			return tokenSearch(peek);
		}
		else {
			return peek ?  topList.first() : topList.removeFirst();
		}
	}
	
	
	private void popContext() {
		contextStack.pop();
		topContext = (Context) (contextStack.isEmpty() ? null : contextStack.peek());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("InputTokenStream { \n");
		for(Iterator iter = contextStack.iterator(); iter.hasNext();) {
			Context context = (Context) iter.next();
			sb.append("Context: ");
			sb.append("(stop ").append(context.stop).append(")");
			sb.append(context.tokenList.toString()).append("\n");
		}
		return sb.append("\n}\n").toString();
	}
}
