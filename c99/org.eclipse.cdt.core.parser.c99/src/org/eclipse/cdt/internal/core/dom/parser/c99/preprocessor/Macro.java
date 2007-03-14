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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;

/**
 * Represents a Macro created with a #define directive. 
 * Can be object-like or funciton-like. An object-like macro
 * has no parameters.
 * 
 * 
 */
public class Macro implements C99Parsersym {

	public static final String __VA_ARGS__ = "__VA_ARGS__";
	
	private final IToken name;
	
	/**
	 * If paramNames == null then isObjectLike() == true
	 * 
	 * A function like macro might not have parameters..
	 * #define p() blah
	 * In this case paramNames != null and paramNames.isEmpty() == true
	 */
	private final LinkedHashSet paramNames;
	private final TokenList replacementSequence;
	
	// the name of the variadic parameter, usually __VA_ARGS__
	private final String varArgParamName;
	
	// the source offsets of the start of the #define directive that defined this macro
	private final int startOffset;
	private final int endOffset;
	
	
	
	/**
	 * If paramNames is null then this will create an object like marcro,
	 * if not null then a function like macro is created.
	 * If it is a funciton-like macro with variable arguments then the last 
	 * parameter name in the sequence will be used as the name of the variadic parameter.
	 * 
	 * @param id The name of the macro
	 * @param parameters List<String>
	 * @param replacementSequence List<IToken>
	 */
	public Macro(IToken name, TokenList replacementSequence, int startOffset, int endOffset, LinkedHashSet paramNames, String varArgParamName) {
		// TODO: the code might not be correct, doesn't mean that its an exception
		if(replacementSequence == null)
			throw new IllegalArgumentException("replacementSequence cannot be null, it may be empty though");
		if(name == null)
			throw new IllegalArgumentException("Macro name cannot be null");
		if(name.getKind() != TK_identifier)
			throw new IllegalArgumentException("Macro name must be an identifier token, got: " + name.getKind() + ", "+ name);
		if(varArgParamName != null && paramNames.contains(varArgParamName))
			throw new IllegalArgumentException("duplicate parameter name: '" + varArgParamName + "'");
		
		this.name = name;
		this.paramNames = paramNames;
		this.replacementSequence = replacementSequence;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.varArgParamName = varArgParamName;
	}
	
	
	/**
	 * Creates an object like macro with no parameters
	 */
	public Macro(IToken name, TokenList replacementSequence, int startOffset, int endOffset) {
		this(name, replacementSequence, startOffset, endOffset, null, null);
	}
	
	
	
	public boolean isCorrectNumberOfArguments(List arguments) {
		if(arguments == null)
			return isObjectLike();
		else
			return arguments.size() == getNumParams() + (isVarArgs() ? 1 : 0);
	}
	
	
	
	
	
	/**
	 * PlaceMarker tokens are used to replace empty arguments.
	 */
	private static MacroArgument createPlaceMarker() {
		TokenList placeMaker = new TokenList(new C99Token(0, 0, TK_PlaceMarker, ""));
		return new MacroArgument(placeMaker, null);
	}
	
	
	
	// Map<String, MacroArgument>
	private Map createReplacementMap(List arguments) {
		Map replacementMap = new HashMap();
		if(arguments == null)
			return replacementMap; // return an empty map
		
		Iterator iter = paramNames.iterator();
		int i = 0;
		while(iter.hasNext()) {
			String name = (String) iter.next();
			MacroArgument arg = (MacroArgument) arguments.get(i);
			arg = arg.isEmpty() ? createPlaceMarker() : arg;
			replacementMap.put(name, arg);
			i++;
		}
		
		if(isVarArgs()) 
			replacementMap.put(varArgParamName, arguments.get(i));
		
		return replacementMap;
	}
	
	
	/**
	 * Special object that collects the tokens that are the result
	 * of a macro invokation. The main purpose of this class is to 
	 * compute new offsets when parameters are substituted.
	 */
	private class InvokationResultCollector {
		
		private TokenList result = new TokenList();
		private int offset = 0;
		private final int fileOffset;
		
		public InvokationResultCollector(int fileOffset) {
			this.fileOffset = fileOffset;
		}
		
		public void addToken(IToken token) {
			IToken t = new C99Token(token);
			t.setStartOffset(adjust(t.getStartOffset()));
			t.setEndOffset(adjust(t.getEndOffset()));
			add(t);
		}
		
		
		private int adjust(int tokenOffset) {
			return tokenOffset - fileOffset + offset;
		}
		
		
		public void addArgument(IToken parameter, TokenList argument) {
			if(argument == null || argument.isEmpty())
				return; 
			
			int argSourceOffset = argument.first().getStartOffset();
			int argSize = argSourceOffset - argument.last().getEndOffset();
					
			int parameterOffset = adjust(parameter.getStartOffset());
			int paramSize = parameter.getEndOffset() - parameter.getStartOffset() + 1;
			
			for(Iterator iter = argument.iterator(); iter.hasNext();) {
				IToken t = new C99Token((IToken)iter.next());
				t.setStartOffset(t.getStartOffset() - argSourceOffset + parameterOffset);
				t.setEndOffset(t.getEndOffset() - argSourceOffset + parameterOffset);
				add(t);
			}
			
			offset += argSize - paramSize;
		}
		
		
		public void addArgumentToken(IToken parameter, IToken token) {
			TokenList temp = new TokenList();
			temp.add(token);
			addArgument(parameter, temp);
		}
		
		private void add(IToken t) {
			if(t.getKind() == TK_identifier && t.toString().equals(name.toString()))
				t.setKind(TK_DisabledMacroName);
			result.add(t);
		}
		
		public TokenList getResult() {
			return result;
		}
	}
	
	
	/**
	 * For object like macros, just returns the replacement sequence.
	 */
	public TokenList invoke() {
		return invoke(null);
	}
	
	
	/**
	 * Invokes the macro with the given arguments.
	 */
	public TokenList invoke(List/*<MacroArgument>*/ arguments) {
		if(!isCorrectNumberOfArguments(arguments)) {
			throw new IllegalArgumentException("wrong number of arguments to macro invocation");
		}
		
		if(replacementSequence.isEmpty()) {
			return null;
		}

		InvokationResultCollector result = new InvokationResultCollector(replacementSequence.first().getStartOffset());
		Map replacementMap = createReplacementMap(arguments);
		
		Iterator iter = replacementSequence.iterator();
		
		// the window 'slides' over the replacement sequence and processes as it goes
		IToken[] window = new IToken[3];
		
		// set the window over the first three tokens
		for(int i = 0; i < 3; i++) 
			window[i] = slide(iter);
		
		while(window[0] != null) {
			if(window[0].getKind() == TK_HashHash) { // TODO, the replacement sequence starts with a ##, not allowed, maybe detect this when the macro is created
				return null;
				// TODO: problem
			}
			else if(window[1] != null && window[1].getKind() == TK_HashHash) {
				if(window[2] == null) {
					return null;
					// TODO: problem
				}
				else {
					TokenList op1 = getHashHashOperand(window[0], replacementMap);
					TokenList op2 = getHashHashOperand(window[2], replacementMap);
					
					IToken newToken = concatenateTokens(op1.removeLast(), op2.removeFirst());
					
					result.addArgument(window[0], op1);
					
					if(op2.isEmpty()) {
						window[0] = newToken;
					}
					else {
						result.addArgumentToken(window[0], newToken);
						window[0] = op2.removeLast();
						result.addArgument(window[2], op1);
					}
					
					window[1] = slide(iter);
					window[2] = slide(iter);
				}
			}
			else if(window[0].getKind() == TK_Hash) {
				if(window[1] == null) {
					return null;
					// TODO: problem
				}
				else if(window[1].getKind() == TK_Parameter) {
					MacroArgument arg = (MacroArgument) replacementMap.get(window[1].toString());
					IToken strToken = createStringToken(arg.getRawTokens());
	
					window[0] = strToken;
					window[1] = window[2];
					window[2] = slide(iter);
				}
				else {
					//return null;
					// TODO: problem
				}
			}
			else if(window[0].getKind() == TK_Parameter) {
				MacroArgument arg = (MacroArgument) replacementMap.get(window[0].toString());
				
				// calls back into the preprocessor to recursively process the argument
				result.addArgument(window[0], arg.getProcessedTokens());
				
				window[0] = window[1];
				window[1] = window[2];
				window[2] = slide(iter);
			}
			else {
				result.addToken(window[0]); 
				window[0] = window[1];
				window[1] = window[2];
				window[2] = slide(iter);
			}
		}
		
		return result.getResult();
	}
	
	
	
	
	private static TokenList getHashHashOperand(IToken replacementToken, Map replacementMap) {
		if(replacementToken.getKind() == TK_Parameter) {
			MacroArgument op1 = (MacroArgument) replacementMap.get(replacementToken.toString());
			return op1.getRawTokens();
		}
		else {
			return new TokenList(replacementToken);
		}
	}
	

	
	private static IToken slide(Iterator iter) {
		return iter.hasNext() ? (IToken)iter.next() : null;
	}
	

	
	// TODO, this function is actually really important
	// need to figure out all the cases,
	/**
	 * Combines two tokens into one, used by the ## operator.
	 */
	private IToken concatenateTokens(IToken x, IToken y) {
		int xkind = x.getKind();
		int ykind = y.getKind();
		
		if(xkind == TK_PlaceMarker && ykind == TK_PlaceMarker) {
			return new C99Token(0, 0, TK_PlaceMarker, "");
		}
		if((xkind == TK_integer || xkind == TK_PlaceMarker) &&
		   (ykind == TK_integer || ykind == TK_PlaceMarker))
		{
			return concatenateTokens(x, y, TK_integer);
		}
		
		if(xkind == TK_identifier && ykind == TK_integer) {
			return concatenateTokens(x, y, TK_identifier);
		}
		
		if((xkind == TK_identifier || xkind == TK_DisabledMacroName) &&
		   (ykind == TK_identifier || ykind == TK_DisabledMacroName)) {
			return concatenateTokens(x, y, TK_identifier);
		}
		
		return concatenateTokens(x, y, TK_Invalid);
	}
	
	
	
	private IToken concatenateTokens(IToken x, IToken y, int kind) {
		return new C99Token(x.getStartOffset(), y.getEndOffset(), kind, x.toString() + y.toString());
	}
	
	
	/**
	 * Converts a list of tokens into a single string literal token,
	 * used by the # operator.
	 */
	private IToken createStringToken(TokenList replacement) {
		// TODO: can use C99Preprocessor.spaceBetween to make this more accurate if necessary
		StringBuffer sb = new StringBuffer().append('"');
		
		Iterator iter = replacement.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			sb.append(token.toString().replace("\"", "\\\"")); // replace " with \"
			if(iter.hasNext())
				sb.append(' ');
		}
		sb.append('"');
		
		int start = replacement.first().getStartOffset();
		int end   = replacement.last().getEndOffset();
		return new C99Token(start, end, TK_stringlit, sb.toString());
	}
	

	public String getReplacementSequenceAsString() {
		StringBuffer sb = new StringBuffer();
		
		Iterator iter = replacementSequence.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			sb.append(token.toString());
			if(iter.hasNext())
				sb.append(' ');
		}
		
		return sb.toString();
	}
	
	
	public boolean isFunctionLike() {
		return !isObjectLike();
	}
	
	public boolean isObjectLike() {
		return paramNames == null;
	}
	
	

	
	
	// TODO: should be defined as having the same parameters and replacement sequence
	public boolean equals() {
		return false;
	}


	public IToken getName() {
		return name;
	}


	public List getParamNames() {
		return new Vector(paramNames);
	}


	public boolean isVarArgs() {
		return varArgParamName != null;
	}

	/**
	 * Returns the number of parameters not including the '...'
	 */
	public int getNumParams() {
		return paramNames == null ? 0 : paramNames.size();
	}

	public int getStartOffset() {
		return startOffset;
	}


	

	public int getDirectiveStartOffset() {
		return startOffset;
	}
	
	public int getDirectiveEndOffset() {
		return endOffset;
	}
	
	public int getDirectiveLength() {
		return getDirectiveEndOffset() - getDirectiveStartOffset();
	}
	
	public int getNameStartOffset() {
		return name.getStartOffset();
	}
	
	public int getNameEndOffset() {
		return name.getEndOffset();
	}
	
	public int getNameLength() {
		return getNameEndOffset() - getNameStartOffset();
	}
	
	
}
