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
import java.util.Vector;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.IToken;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;

/**
 * Represents a Macro created with a #define directive. 
 * Can be object-like or function-like. An object-like macro
 * has no parameters.
 * 
 * 
 */
public class Macro {

	public static final String __VA_ARGS__ = "__VA_ARGS__"; //$NON-NLS-1$
	
	private final IToken name;
	private final String nameAsString;
	
	/**
	 * If paramNames == null then isObjectLike() == true
	 * 
	 * A function like macro might not have parameters..
	 * #define p() blah
	 * In this case paramNames will be an empty list
	 */
	private final LinkedHashSet paramNames;
	private final TokenList replacementSequence;
	
	// the name of the variadic parameter, usually __VA_ARGS__
	private final String varArgParamName;
	
	// the source offsets of the start of the #define directive that defined this macro
	private final int startOffset;
	private final int endOffset;
	private final IPPTokenComparator comparator;
	
	
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
	public Macro(IToken name, TokenList replacementSequence, int startOffset, int endOffset, 
  			     LinkedHashSet paramNames, String varArgParamName, IPPTokenComparator comparator) {
		
		if(replacementSequence == null)
			throw new IllegalArgumentException(Messages.getString("Macro.0")); //$NON-NLS-1$
		if(name == null)
			throw new IllegalArgumentException(Messages.getString("Macro.1")); //$NON-NLS-1$
		if(!comparator.compare(PPToken.IDENT, name))
			throw new IllegalArgumentException(Messages.getString("Macro.2") + name.getKind() + ", "+ name);  //$NON-NLS-1$//$NON-NLS-2$
		if(varArgParamName != null && paramNames.contains(varArgParamName))
			throw new IllegalArgumentException(Messages.getString("Macro.3") + "'" + varArgParamName + "'");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		
		this.replacementSequence = replacementSequence;
		normalizeReplacementSequenceOffsets(this.replacementSequence);
		
		this.name = name;
		this.nameAsString = name.toString();
		this.paramNames = paramNames;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.varArgParamName = varArgParamName;
		this.comparator = comparator;
	}
	
	/**
	 * Creates an object like macro with no parameters
	 */
	public Macro(IToken name, TokenList replacementSequence, int startOffset, int endOffset, IPPTokenComparator comparator) {
		this(name, replacementSequence, startOffset, endOffset, null, null, comparator);
	}
	
	
	private boolean check(PPToken pptoken, IToken token) {
		return comparator.compare(pptoken, token);
	}
	
	
	/**
	 * Normalizes the token offsets of the replacement sequence so that 
	 * they start at zero. This way we don't have to worry about the actual
	 * location of the #define in the source code when substituting arguments.
	 */
	private static void normalizeReplacementSequenceOffsets(TokenList replacementSequence) {
		if(replacementSequence == null || replacementSequence.isEmpty())
			return;
		
		int offset = replacementSequence.first().getStartOffset();
		Iterator iter = replacementSequence.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			token.setStartOffset(token.getStartOffset() - offset);
			token.setEndOffset(token.getEndOffset() - offset);
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
	private MacroArgument createPlaceMarker() {
		
		IToken placeMarker = new Token(0, 0, -1, ""); //$NON-NLS-1$
		
		// this is how we track that this token is in fact a place marker
		placeMarker.setPreprocessorAttribute(IToken.ATTR_PLACE_MARKER);
		
		return new MacroArgument(new TokenList(placeMarker), null);
	}
	
	
	
	// Map<String, MacroArgument>
	private Map createReplacementMap(List arguments) {
		Map replacementMap = new HashMap();
		if(arguments == null)
			return replacementMap; // return an empty map
		
		int i = 0;
		for(Iterator iter = paramNames.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			MacroArgument arg = (MacroArgument) arguments.get(i);
			arg = arg.isEmpty() ? createPlaceMarker() : arg;
			replacementMap.put(name, arg);
			i++;
		}
		
		if(isVarArgs()) {
			if(arguments.size() < getNumParams() + 1)
				replacementMap.put(varArgParamName, createPlaceMarker());
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
		
		private TokenList result = new TokenList();
		
		// Used to compute offsets of tokens as they are added to the result
		private int offset = 0;
		
		
		/**
		 * @param token Must be a normalized token from the replacementSequence.
		 */
		public void addToken(IToken token) {
			IToken t = new Token(token);
			t.setStartOffset(t.getStartOffset() + offset);
			t.setEndOffset(t.getEndOffset() + offset);
			add(t);
		}
		
		
		public void addArgument(IToken parameter, TokenList argument) {
			if(argument == null || argument.isEmpty())
				return; 
			
			int argSourceOffset = argument.first().getStartOffset();
			int argSize = argument.last().getEndOffset() - argSourceOffset;
					
			int paramSize = parameter.getEndOffset() - parameter.getStartOffset() + 1;
			int parameterOffset = parameter.getStartOffset() + offset;
			
			for(Iterator iter = argument.iterator(); iter.hasNext();) {
				IToken t = new Token((IToken)iter.next());
				t.setStartOffset(t.getStartOffset() - argSourceOffset + parameterOffset);
				t.setEndOffset(t.getEndOffset() - argSourceOffset + parameterOffset);
				add(t);
			}
			
			offset += (argSize - paramSize) + 1;
		}
		
		
		public void addArgumentToken(IToken parameter, IToken token) {
			TokenList temp = new TokenList();
			temp.add(token);
			addArgument(parameter, temp);
		}
		
		private void add(IToken t) {
			// prevents recursive replacement of the macro
			if(check(PPToken.IDENT, t) && t.toString().equals(nameAsString))
				t.setPreprocessorAttribute(IToken.ATTR_DISABLED_MACRO_NAME);
			
			result.add(t);
		}
		
		public TokenList getResult() {
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
	public TokenList invoke(List/*<MacroArgument>*/ arguments) {
		if(arguments != null && !isCorrectNumberOfArguments(arguments.size()))
			throw new IllegalArgumentException(Messages.getString("Macro.5")); //$NON-NLS-1$
		if(replacementSequence.isEmpty())
			return new TokenList();

		InvokationResultCollector result = new InvokationResultCollector();
		Map replacementMap = createReplacementMap(arguments);
		
		Iterator iter = replacementSequence.iterator();
		
		// the window 'slides' over the replacement sequence and processes as it goes
		IToken[] window = new IToken[3];
		
		// set the window over the first three tokens
		for(int i = 0; i < 3; i++) 
			window[i] = slide(iter);
		
		while(window[0] != null) {
			if(check(PPToken.HASHHASH, window[0])) { // the replacement sequence starts with a ##, thats an error
				return null;
			}
			else if(window[1] != null && check(PPToken.HASHHASH, window[1])) {
				if(window[2] == null) {
					return null;
				}
				else {
					TokenList op1 = getHashHashOperand(window[0], replacementMap);
					TokenList op2 = getHashHashOperand(window[2], replacementMap);
					
					IToken newToken = pasteTokens(op1.removeLast(), op2.removeFirst(), window[0].getStartOffset(), window[2].getEndOffset());
					result.addArgument(window[0], op1); // op1 might be empty if it originally had only one token
					
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
			else if(check(PPToken.HASH, window[0])) {
				if(window[1] == null) {
					return null;
				}
				else if(window[1].getPreprocessorAttribute() == IToken.ATTR_PARAMETER) {
					MacroArgument arg = (MacroArgument) replacementMap.get(window[1].toString());
					if(arg == null)
						return null;
					
					TokenList rawTokens = arg.getRawTokens();
					if(rawTokens.isEmpty()) {
						window[0] = window[2];
						window[1] = slide(iter);
						window[2] = slide(iter);
					}
					else {
						String newString = handleHashOperator(rawTokens);
						int startOffset = window[0].getStartOffset(); // the hash
						int endOffset   = window[1].getStartOffset() + newString.length() - 2; // don't count the double quotes in the string
						IToken strToken = new Token(startOffset, endOffset, comparator.getKind(IPPTokenComparator.KIND_STRINGLIT), newString);
						
						window[0] = strToken;
						window[1] = window[2];
						window[2] = slide(iter);
					}
				}
				else {
					return null;
				}
			}
			else if(window[0].getPreprocessorAttribute() == IToken.ATTR_PARAMETER) {
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
		if(replacementToken.getPreprocessorAttribute() == IToken.ATTR_PARAMETER) {
			MacroArgument op1 = (MacroArgument) replacementMap.get(replacementToken.toString());
			return op1.getRawTokens(); // do not process the tokens
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
	private IToken pasteTokens(IToken x, IToken y, int startOffset, int endOffset) {
		String s = x.toString() + y.toString();
		int invalidKind = comparator.getKind(IPPTokenComparator.KIND_INVALID);
		
		if(isPlaceMarker(x) && isPlaceMarker(y)) {
			IToken token = new Token(startOffset, endOffset, invalidKind, s);
			token.setPreprocessorAttribute(IToken.ATTR_PLACE_MARKER);
			return token;
		}
		
		// pasting with a place marker doesn't need much
		if(isPlaceMarker(x)) {
			return new Token(startOffset, endOffset, y.getKind(), s);
		}
		else if(isPlaceMarker(y)) {
			return new Token(startOffset, endOffset, x.getKind(), s);
		}
		
		int kind  = invalidKind; // if paste fails then generate an invalid token
		
		if(check(PPToken.INTEGER, x) && check(PPToken.INTEGER, y)) {
			kind = comparator.getKind(IPPTokenComparator.KIND_INTEGER);
		}
		else if(check(PPToken.IDENT, x) && check(PPToken.INTEGER, y)) {
			kind = comparator.getKind(IPPTokenComparator.KIND_IDENTIFIER);
		}
		else if(check(PPToken.IDENT, x) && check(PPToken.IDENT, y)) {
			kind = comparator.getKind(IPPTokenComparator.KIND_IDENTIFIER);
		}
		
		return new Token(startOffset, endOffset, kind, s);
	}
	
	
	
	private static boolean isPlaceMarker(IToken token) {
		return token.getPreprocessorAttribute() == IToken.ATTR_PLACE_MARKER;
	}
	
	
	/**
	 * Converts a list of tokens into a single string literal token,
	 * used by the # operator.
	 */
	private String handleHashOperator(TokenList replacement) {
		// TODO: can use C99Preprocessor.spaceBetween to make this more accurate if necessary
		StringBuffer sb = new StringBuffer().append('"');
		
		Iterator iter = replacement.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			sb.append(token.toString().replaceAll("\"", "\\\"")); // replace " with \"
			if(iter.hasNext())
				sb.append(' ');
		}
		sb.append('"');
		
		return sb.toString();
	}
	

	public String getExpansion() {
		return replacementSequence.toString();
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
	
	public String toString() {
		if(isObjectLike())
			return nameAsString + " " + replacementSequence; //$NON-NLS-1$
		
		return nameAsString + "(" + paramNames.toString() + (isVarArgs() ? ",..." : "") + ") " + replacementSequence; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	
}
