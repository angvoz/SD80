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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;

/**
 * Represents a Macro created with a #define directive. 
 * Can be object-like or function-like. An object-like macro
 * has no parameters.
 * 
 */
public class Macro<TKN> {

	public static final String __VA_ARGS__ = "__VA_ARGS__"; //$NON-NLS-1$
	
	private final TKN name;
	private final String nameAsString;
	
	/**
	 * If paramNames == null then isObjectLike() == true
	 * 
	 * A function like macro might not have parameters..
	 * #define p() blah
	 * In this case paramNames will be an empty list
	 */
	private final LinkedHashSet<String> paramNames;
	private final TokenList<TKN> replacementSequence;
	
	// the name of the variadic parameter, usually __VA_ARGS__
	private final String varArgParamName;
	
	// the source offsets of the start of the #define directive that defined this macro
	private final int startOffset;
	private final int endOffset;
	private final IPPTokenComparator<TKN> comparator;
	
	private final ObjectTagger<TKN, String> disabledTokens;
	
	/**
	 * If paramNames is null then this will create an object like macro,
	 * if not null then a function like macro is created.
	 * If it is a function-like macro with variable arguments then the last 
	 * parameter name in the sequence will be used as the name of the variadic parameter.
	 * 
	 * @param id The name of the macro
	 * @param parameters List<String>
	 * @param replacementSequence List<IToken>
	 * @param startOffset The offset of the '#' token that started the define for this macro.
	 */
	public Macro(TKN name, TokenList<TKN> replacementSequence, int startOffset, int endOffset, 
  			     LinkedHashSet<String> paramNames, String varArgParamName, IPPTokenComparator<TKN> comparator, ObjectTagger<TKN, String> disabledTokens) {
		
		if(replacementSequence == null)
			throw new IllegalArgumentException(Messages.getString("Macro.0")); //$NON-NLS-1$
		if(name == null)
			throw new IllegalArgumentException(Messages.getString("Macro.1")); //$NON-NLS-1$
		if(comparator.getKind(name) != PPToken.IDENT)
			throw new IllegalArgumentException();
		if(varArgParamName != null && paramNames.contains(varArgParamName))
			throw new IllegalArgumentException(Messages.getString("Macro.3") + "'" + varArgParamName + "'");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		
		this.replacementSequence = replacementSequence;
		this.name = name;
		this.nameAsString = name.toString();
		this.paramNames = paramNames;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.varArgParamName = varArgParamName;
		this.comparator = comparator;
		this.disabledTokens = disabledTokens;
		
		normalizeReplacementSequenceOffsets(this.replacementSequence);
	}
	
	/**
	 * Creates an object like macro with no parameters
	 */
	public Macro(TKN name, TokenList<TKN> replacementSequence, int startOffset, int endOffset, IPPTokenComparator<TKN> comparator, ObjectTagger<TKN, String> disabledTokens) {
		this(name, replacementSequence, startOffset, endOffset, null, null, comparator, disabledTokens);
	}
	
	
	private boolean check(PPToken pptoken, TKN token) {
		return comparator.getKind(token) == pptoken;
	}
	
	
	/**
	 * Normalizes the token offsets of the replacement sequence so that 
	 * they start at zero. This way we don't have to worry about the actual
	 * location of the #define in the source code when substituting arguments.
	 */
	private void normalizeReplacementSequenceOffsets(TokenList<TKN> replacementSequence) {
		if(replacementSequence == null || replacementSequence.isEmpty())
			return;
		
		int offset = comparator.getStartOffset(replacementSequence.first());
		for(TKN token : replacementSequence) {
			comparator.setStartOffset(token, comparator.getStartOffset(token) - offset);
			comparator.setEndOffset(token, comparator.getEndOffset(token) - offset);
		}
	}
	
	/**
	 * Returns true if the number of arguments passed to a macro invocation is correct.
	 * @throws IllegalArgumentException if numArgs < 0
	 */
	public boolean isCorrectNumberOfArguments(int numArgs) {
		if(numArgs < 0)
			throw new IllegalArgumentException(Messages.getString("Macro.4")); //$NON-NLS-1$
		
		// Object like macro doesn't take any arguments, this method shouldn't even
		// be called in that situation.
		if(isObjectLike())
			return false;
		
		int numParams = getNumParams();
		return (numArgs == numParams)  ||  (isVarArgs() && numArgs == numParams + 1);
	}
	
	
	/**
	 * PlaceMarker tokens are used to replace empty arguments.
	 */
	private MacroArgument<TKN> createPlaceMarkerToken() {
		TKN placeMarker = comparator.createToken(IPPTokenComparator.KIND_PLACEMARKER, 0, 0, "placemarker"); //$NON-NLS-1$		
		return new MacroArgument<TKN>(new TokenList<TKN>(placeMarker), null, disabledTokens);
	}
	
	
	
	// Map<String, MacroArgument>
	private Map<String, MacroArgument<TKN>> createReplacementMap(List<MacroArgument<TKN>> arguments) {
		Map<String, MacroArgument<TKN>> replacementMap = new HashMap<String, MacroArgument<TKN>>();
		if(arguments == null)
			return replacementMap; // return an empty map
		
		int i = 0;
		for(String name : paramNames) { // this is why paramNames is a LinkedHashSet, so it remains sorted
			MacroArgument<TKN> arg = arguments.get(i);
			arg = arg.isEmpty() ? createPlaceMarkerToken() : arg;
			replacementMap.put(name, arg);
			i++;
		}
		
		if(isVarArgs()) {
			if(arguments.size() < getNumParams() + 1)
				replacementMap.put(varArgParamName, createPlaceMarkerToken());
			else
				replacementMap.put(varArgParamName, arguments.get(i));
		}
		
		return replacementMap;
	}
	
	
	/**
	 * Special object that collects the tokens that are the result
	 * of a macro invokation. The main purpose of this class is to 
	 * compute new offsets when parameters are substituted.
	 */
	private class InvokationResultCollector {
		
		private TokenList<TKN> result = new TokenList<TKN>();
		
		// Used to compute offsets of tokens as they are added to the result
		private int offset = 0;
		
		
		/**
		 * @param token Must be a normalized token from the replacementSequence.
		 */
		public void addToken(TKN token) {
			TKN t = comparator.cloneToken(token);
			disabledTokens.shareTags(token, t);
			comparator.setStartOffset(t, comparator.getStartOffset(t) + offset);
			comparator.setEndOffset(t, comparator.getEndOffset(t) + offset);
			add(t);
		}
		
		
		public void addArgument(TKN parameter, TokenList<TKN> argument) {
			if(argument == null || argument.isEmpty())
				return; 
			
			int argSourceOffset = comparator.getStartOffset(argument.first());
			int argSize = comparator.getEndOffset(argument.last()) - argSourceOffset;
					
			int paramSize = comparator.getEndOffset(parameter) - comparator.getStartOffset(parameter) + 1;
			int parameterOffset = comparator.getStartOffset(parameter) + offset;
			
			for(Iterator<TKN> iter = argument.iterator(); iter.hasNext();) {
				TKN next = iter.next();
				TKN t = comparator.cloneToken(next);
				disabledTokens.shareTags(next, t);
				comparator.setStartOffset(t, comparator.getStartOffset(t) - argSourceOffset + parameterOffset);
				comparator.setEndOffset(t, comparator.getEndOffset(t) - argSourceOffset + parameterOffset);
				add(t);
			}
			
			offset += (argSize - paramSize) + 1;
		}
		
		
		public void addArgumentToken(TKN parameter, TKN token) {
			TokenList<TKN> temp = new TokenList<TKN>();
			temp.add(token);
			addArgument(parameter, temp);
		}
		
		private void add(TKN t) {
			if(check(PPToken.IDENT, t) && t.toString().equals(nameAsString))
				disabledTokens.tag(t, C99Preprocessor.DISABLED_TAG);

			result.add(t);
		}
		
		public TokenList<TKN> getResult() {
			return result;
		}
	}
	

	/**
	 * Invokes the macro with the given arguments.
	 * Pass null to invoke an object like macro.
	 * 
	 * @throws IllegalArgumentException if the wrong number of arguments is passed
	 * @return null if there was some kind of syntax or parameter error during macro invokation
	 */
	public TokenList<TKN> invoke(List<MacroArgument<TKN>> arguments) {
		if(arguments != null && !isCorrectNumberOfArguments(arguments.size()))
			throw new IllegalArgumentException(Messages.getString("Macro.5")); //$NON-NLS-1$
		if(replacementSequence.isEmpty())
			return new TokenList<TKN>();

		InvokationResultCollector result = new InvokationResultCollector();
		Map<String, MacroArgument<TKN>> replacementMap = createReplacementMap(arguments);
		
		Iterator<TKN> iter = replacementSequence.iterator();
		
		// the window 'slides' over the replacement sequence and processes as it goes
		// This should probably be an array but Java doesn't support arrays of generic type.
		TKN window0 = slide(iter);
		TKN window1 = slide(iter);
		TKN window2 = slide(iter);
		
		
		while(window0 != null) {
			if(check(PPToken.HASHHASH, window0)) { // the replacement sequence starts with a ##, thats an error
				return null;
			}
			else if(window1 != null && check(PPToken.HASHHASH, window1)) {
				if(window2 == null) {
					return null;
				}
				else {
					TokenList<TKN> op1 = getHashHashOperand(window0, replacementMap);
					TokenList<TKN> op2 = getHashHashOperand(window2, replacementMap);
					
					TKN newToken = pasteTokens(op1.removeLast(), op2.removeFirst(), 
							comparator.getStartOffset(window0), comparator.getEndOffset(window2));
					
					result.addArgument(window0, op1); // op1 might be empty if it originally had only one token
					
					if(op2.isEmpty()) {
						window0 = newToken;
					}
					else {
						result.addArgumentToken(window0, newToken);
						window0 = op2.removeLast();
						result.addArgument(window2, op1);
					}
					
					window1 = slide(iter);
					window2 = slide(iter);
				}
			}
			else if(check(PPToken.HASH, window0)) {
				if(window1 == null) {
					return null;
				}
				else if(isParam(window1)) {
					MacroArgument<TKN> arg = replacementMap.get(window1.toString());
					if(arg == null)
						return null;
					
					TokenList<TKN> rawTokens = arg.getRawTokens();
					if(rawTokens.isEmpty()) {
						window0 = window2;
						window1 = slide(iter);
						window2 = slide(iter);
					}
					else {
						String newString = handleHashOperator(rawTokens);
						int startOffset = comparator.getStartOffset(window0); // the hash
						int endOffset   = comparator.getStartOffset(window1) + newString.length() - 2; // don't count the double quotes in the string
						TKN strToken = comparator.createToken(IPPTokenComparator.KIND_STRINGLIT, startOffset, endOffset, newString);
						
						window0 = strToken;
						window1 = window2;
						window2 = slide(iter);
					}
				}
				else {
					return null;
				}
			}
			else if(isParam(window0)) {
				MacroArgument<TKN> arg = replacementMap.get(window0.toString());
				
				// calls back into the preprocessor to recursively process the argument
				result.addArgument(window0, arg.getProcessedTokens(comparator));
				
				window0 = window1;
				window1 = window2;
				window2 = slide(iter);
			}
			else {
				result.addToken(window0); 
				window0 = window1;
				window1 = window2;
				window2 = slide(iter);
			}
		}
		
		return result.getResult();
	}
	
	
	
	private TokenList<TKN> getHashHashOperand(TKN replacementToken, Map<String, MacroArgument<TKN>> replacementMap) {
		if(isParam(replacementToken)) {
			MacroArgument<TKN> op1 = replacementMap.get(replacementToken.toString());
			return op1.getRawTokens(); // do not process the tokens
		}
		else {
			return new TokenList<TKN>(replacementToken);
		}
	}
	

	
	private TKN slide(Iterator<TKN> iter) {
		return iter.hasNext() ? iter.next() : null;
	}
	

	
	// TODO, this function is actually really important
	// need to figure out all the cases,
	/**
	 * Combines two tokens into one, used by the ## operator.
	 */
	private TKN pasteTokens(TKN x, TKN y, int startOffset, int endOffset) {
		if(isPlaceMarker(x)) {
			TKN clone = comparator.cloneToken(y);
			disabledTokens.shareTags(y, clone);
			return clone;
		}
		if(isPlaceMarker(y)) {
			TKN clone = comparator.cloneToken(x);
			disabledTokens.shareTags(x, clone);
			return clone;
		}

		int kind  = IPPTokenComparator.KIND_INVALID; // if paste fails then generate an invalid token
		if(check(PPToken.INTEGER, x) && check(PPToken.INTEGER, y))
			kind = IPPTokenComparator.KIND_INTEGER;
		else if(check(PPToken.IDENT, x) && check(PPToken.INTEGER, y))
			kind = IPPTokenComparator.KIND_IDENTIFIER;
		else if(check(PPToken.IDENT, x) && check(PPToken.IDENT, y))
			kind = IPPTokenComparator.KIND_IDENTIFIER;
		
		String s = x.toString() + y.toString();
		return comparator.createToken(kind, startOffset, endOffset, s);
	}
	
	
	
	private boolean isPlaceMarker(TKN token) {
		return comparator.getKind(token) == PPToken.PLACEMARKER;
	}
	
	
	private boolean isParam(TKN token) {
		if(paramNames == null) // object-like macros don't have params
			return false;
		String name = token.toString();
		return paramNames.contains(name) || name.equals(varArgParamName);
	}
	
	
	/**
	 * Converts a list of tokens into a single string literal token,
	 * used by the # operator.
	 */
	private String handleHashOperator(TokenList<TKN> replacement) {
		// TODO: can use C99Preprocessor.spaceBetween to make this more accurate if necessary
		StringBuffer sb = new StringBuffer().append('"');//$NON-NLS-1$
		
		
		Iterator<TKN> iter = replacement.iterator();
		while(iter.hasNext()) {
			TKN token = iter.next();
			sb.append(token.toString().replaceAll("\"", "\\\"")); //$NON-NLS-1$ //$NON-NLS-2$ // replace " with \"
			if(iter.hasNext())
				sb.append(' ');
		}
		sb.append('"');//$NON-NLS-1$
		
		return sb.toString();
	}
	

	public String getExpansion() {
		return C99Preprocessor.tokensToString(comparator, replacementSequence);
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


	public String getName() {
		return nameAsString; // much faster than calling toString() every time
	}


	public List<String> getParamNames() {
		return new ArrayList<String>(paramNames);
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
		return comparator.getStartOffset(name);
	}
	
	public int getNameEndOffset() {
		return comparator.getEndOffset(name);
	}
	
	public int getNameLength() {
		return getNameEndOffset() - getNameStartOffset();
	}
	
	public String toString() {
		if(isObjectLike())
			return nameAsString + " " + replacementSequence; //$NON-NLS-1$
		
		return nameAsString + "(" + paramNames.toString() + (isVarArgs() ? ",..." : "") + ") " + replacementSequence; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	
}
