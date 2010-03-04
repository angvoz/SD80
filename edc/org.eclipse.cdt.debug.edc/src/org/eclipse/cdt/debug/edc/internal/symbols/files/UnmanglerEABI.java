/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.symbols.files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import org.eclipse.cdt.debug.edc.symbols.IUnmangler;

/**
 * Unmangler for the ARM/Itanium/etc. EABI (http://www.codesourcery.com/public/cxx-abi/abi.html)
 * <p>
 * TODO: <expression> <closure-type-name>  <lambda-sig>
 */
public class UnmanglerEABI implements IUnmangler {

	enum SubstType {
		PREFIX,
		TEMPLATE_PREFIX,
		TYPE,
		TEMPLATE_TEMPLATE_PARAM,
		
	}
	public UnmanglerEABI() {
		
	}

	static class UnmangleState {
		private char[] symbol;
		private int index;
		StringBuilder buffer;
		private Stack<Integer> pushes ; // lengths of buffer when pushed 
		private List<String> substitutions;
		private Map<Integer, SubstType> substitutionTypes;
		
		private List<String> templateArgs;
		private int templateArgBase;
		private Stack<Integer> templateArgStack;	// length of templateArgs when pushed
		
		private Stack<Integer> backtracks ; // grouped entries: index value, lengths of buffer, and substitutions length when pushed 
		
		public UnmangleState(String symbol) {
			this.symbol = symbol.toCharArray();
			index = 0;
			buffer = new StringBuilder();
			pushes = new Stack<Integer>();
			substitutions = new ArrayList<String>();
			substitutionTypes = new HashMap<Integer, UnmanglerEABI.SubstType>();
			templateArgs = new ArrayList<String>();
			templateArgStack = new Stack<Integer>();
			backtracks = new Stack<Integer>();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String remaining = getRemaining();
			if (remaining.length() == 0)
				remaining = "<<end>>";
			return "state: at [" + remaining + "], so far: " + current();
		}
		
		/**
		 * Push when entering a new decoding context (BNF expression).
		 */
		public void push() {
			pushes.push(buffer.length());
		}
		
		/**
		 * Pop the current decoded string and restore context to
		 * the calling context.
		 * @return decoded string
		 */
		public String pop() {
			int oldpos = pushes.isEmpty() ? 0 : pushes.pop();
			String str = buffer.substring(oldpos, buffer.length());
			buffer.setLength(oldpos);
			return str;
		}
		
		/**
		 * Push template argument state
		 */
		public void pushTemplateArgs() {
			templateArgStack.push(templateArgBase);
			templateArgStack.push(templateArgs.size());
		}
		
		/**
		 * Pop template argument state
		 * @throws UnmanglingException 
		 */
		public void popTemplateArgs() throws UnmanglingException {
			try {
				templateArgs.subList(templateArgStack.pop(), templateArgs.size()).clear();
				templateArgBase = templateArgStack.pop();
			} catch (Exception e) {
				throw new UnmanglingException("template stack empty", buffer.toString());
			}
		}
		/**
		 * Push all state, when entering a possible backtrack scenario.
		 * Use #safePop() if an operation succeeds, or #safeBacktrack()
		 * if it failed and you want to retry.
		 */
		public void safePush() {
			backtracks.push(index);
			backtracks.push(buffer.length());
			backtracks.push(substitutions.size());
			backtracks.push(pushes.size());
		}
		
		/**
		 * Call when a #safePush() branch has succeeded to discard backtrack state.
		 */
		public void safePop() {
			backtracks.pop();
			backtracks.pop();
			backtracks.pop();
			backtracks.pop();
		}

		/**
		 * Call when a #safePush() branch has failed to reset backtrack state.
		 * (To perform another backtrack, call #safePush() again)
		 */
		public void safeBacktrack() {
			int oldSize = backtracks.pop();
			pushes.subList(oldSize, pushes.size()).clear();
			oldSize = backtracks.pop();
			substitutions.subList(oldSize, substitutions.size()).clear();
			while (substitutionTypes.size() > oldSize)
				substitutionTypes.remove(substitutionTypes.size() - 1);
			buffer.setLength(backtracks.pop());
			index = backtracks.pop();
		}
		
		/**
		 * Get the current constructed string (since the last #push())
		 * @return
		 */
		public String current() {
			int oldpos = pushes.isEmpty() ? 0 : pushes.peek();
			String str = buffer.substring(oldpos, buffer.length());
			return str;
		}

		/**
		 * Remember the current constructed string as a substitution.
		 * @param substType
		 */
		public void remember(SubstType substType) {
			remember(current(), substType);
		}
		
		/**
		 * Remember the given string as a substitution.
		 * @param name
		 * @param substType
		 */
		public void remember(String name, SubstType substType) {
			substitutionTypes.put(substitutions.size(), substType);
			substitutions.add(name);
		}
		
		/**
		 * Pop the current decoded string as in {@link #pop()}
		 * and remember the string as a substitution.
		 * @return String
		 */
		public String popAndRemember(SubstType substType) {
			String name = pop();
			remember(name, substType);
			return name;
		}
		
		public char peek() {
			return index < symbol.length ? symbol[index] : 0;
		}
		
		public char peek(int offset) {
			return index + offset < symbol.length ? symbol[index + offset] : 0;
		}
		
		public void consume(char ch) throws UnmanglingException {
			if (ch != get())
				throw unexpected();
		}
		public char get() {
			return index < symbol.length ? symbol[index++] : 0;
		}
		
		public void unget() {
			if (index > 0) index--;
		}
		public void skip() {
			if (index < symbol.length)
				index++;
		}
		
		public void skip2() {
			index = Math.min(index + 2, symbol.length);
		}
		
		public boolean done() {
			return index >= symbol.length;
		}

		public UnmanglingException unexpected() {
			return new UnmanglingException("Unexpected text at " + getRemaining(), buffer.toString());			
		}
		public UnmanglingException unexpected(String what) {
			return new UnmanglingException("Wanted " + what + " but got unexpected text at " + getRemaining(), buffer.toString());			
		}
		public UnmanglingException notImplemented() {
			return new UnmanglingException("Unimplemented at " + getRemaining(),
					buffer.toString());			
		}

		/**
		 * @return
		 */
		private String getRemaining() {
			if (index >= symbol.length)
				return "";
			return new String(symbol, index, symbol.length - index);
		}

		/**
		 * @throws UnmanglingException 
		 * 
		 */
		public void throwIfDone() throws UnmanglingException {
			if (done())
				throw new UnmanglingException("Unexpected end of symbol",
						buffer.toString());
		}

		public void updateSubstitution(SubstType substType) {
			substitutionTypes.put(substitutions.size() - 1, substType);
		}

		/**
		 * @return
		 */
		public SubstType lastSubstitution() {
			return substitutionTypes.get(substitutions.size() - 1);
		}

		/**
		 * @param arg
		 */
		public void rememberTemplateArg(String arg) {
			templateArgs.add(arg);
		}

		/**
		 * @param num
		 * @return
		 * @throws UnmanglingException 
		 */
		public String getTemplateArg(int num) throws UnmanglingException {
			num -= templateArgBase;
			if (num < 0 || num >= templateArgs.size())
				throw unexpected("template argument in range 0-" + (templateArgs.size() - templateArgBase)+"; got " + num);
			return templateArgs.get(num);
		}

		public String lastSubstitutedName() {
			if (substitutions.size() == 0)
				return "";
			return substitutions.get(substitutions.size() - 1);
		}
	}
	
	private static WeakHashMap<String, String> unmangledMap = new WeakHashMap<String, String>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#undecorate(java.lang.String)
	 */
	public String undecorate(String symbol) {
		// symbols may have @@GLIBC... type suffixes
		int atat = symbol.indexOf("@@");
		if (atat > 0)
			symbol = symbol.substring(0, atat);
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#isMangled(java.lang.String)
	 */
	public boolean isMangled(String symbol) {
		return symbol != null && symbol.startsWith("_Z");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#unmangle(java.lang.String)
	 */
	public String unmangle(String symbol) throws UnmanglingException {
		if (symbol == null)
			return null;
		
		String unmangled;
		
		if (unmangledMap.containsKey(symbol)) {
			unmangled = unmangledMap.get(symbol);
		} else {
			unmangled = doUnmangle(symbol);
			unmangledMap.put(symbol, unmangled);
		}
		
		return unmangled;
	}

	/**
	 * @param symbol
	 * @return
	 * @throws UnmanglingException
	 */
	private String doUnmangle(String symbol) throws UnmanglingException {
		/*
 Entities with C linkage and global namespace variables are not mangled. Mangled names have the general structure:


    <mangled-name> ::= _Z <encoding>
    <encoding> ::= <function name> <bare-function-type>
	       ::= <data name>
	       ::= <special-name>
		 */
		if (!symbol.startsWith("_Z")) {
			return symbol;
		}
		
		String suffix = "";
		int idx = symbol.indexOf('@');
		if (idx >= 0) {
			suffix = symbol.substring(idx);
			symbol = symbol.substring(0, idx);
		}
		
		UnmangleState state = new UnmangleState(symbol);
		state.skip2();
		
		String unmangled = unmangleEncoding(state);
		unmangled += suffix;
		return unmangled;
	}

	/*
    <encoding> ::= <function name> <bare-function-type>
	       ::= <data name>
	       ::= <special-name>
	 */
	private String unmangleEncoding(UnmangleState state) throws UnmanglingException {
		state.push();
		
		String name;
		
		// ferret out <special-name>
		char ch = state.peek();
		if (ch == 'T' || ch == 'G') {
			name = unmangleSpecialName(state);
		} else {
			name = unmangleName(state);
		}
		
		if (!state.done()) {
			boolean isTemplate = name.endsWith(">");	// HACK
			if (isTemplate) {
				state.buffer.append(unmangleType(state));
				state.buffer.append(' ');
			}
			state.buffer.append(name);
			state.buffer.append(unmangleBareFunctionType(state, false));
		} else {
			state.buffer.append(name);
		}
		
		return state.pop();
	}

	/*
 <special-name> ::= TV <type>	# virtual table
		 ::= TT <type>	# VTT structure (construction vtable index)
		 ::= TI <type>	# typeinfo structure
		 ::= TS <type>	# typeinfo name (null-terminated byte string)
  <special-name> ::= GV <object name>	# Guard variable for one-time initialization
			# No <type>
  <special-name> ::= T <call-offset> <base encoding>
		      # base is the nominal target function of thunk
  <call-offset> ::= h <nv-offset> _
				::= v <v-offset> _
  <nv-offset> ::= <offset number>
		      # non-virtual base override
  <v-offset>  ::= <offset number> _ <virtual offset number>
		      # virtual base override, with vcall offset
 
  <special-name> ::= Tc <call-offset> <call-offset> <base encoding>
		      # base is the nominal target function of thunk
		      # first call-offset is 'this' adjustment
		      # second call-offset is result adjustment

	 */
	private String unmangleSpecialName(UnmangleState state) throws UnmanglingException {
		state.push();
		
		char ch = state.get();
		if (ch == 'T') {
			String type = null;
			switch (state.get()) {
			case 'V':
				type = unmangleType(state);
				state.buffer.append("<virtual table for ");
				state.buffer.append(type);
				state.buffer.append('>');
				break;
			case 'T':
				type = unmangleType(state);
				state.buffer.append("<VTT structure for ");
				state.buffer.append(type);
				state.buffer.append('>');
				break;
			case 'I':
				type = unmangleType(state);
				state.buffer.append("<typeinfo structure for ");
				state.buffer.append(type);
				state.buffer.append('>');
				break;
			case 'S':
				type = unmangleType(state);
				state.buffer.append("<typeinfo name for ");
				state.buffer.append(type);
				state.buffer.append('>');
				break;
			case 'h': {
				// h <nv-offset> _
				int offset = doUnmangleNumber(state);
				state.consume('_');
				state.buffer.append("<non-virtual base override at offset ");
				appendHexNumber(state.buffer, offset);
				state.buffer.append(" for ");
				state.buffer.append(unmangleEncoding(state));
				state.buffer.append('>');
				break;
			}	
			case 'v': {
				// v <offset number> _ <virtual offset number> _
				int offset = doUnmangleNumber(state);
				state.consume('_');
				int voffset = doUnmangleNumber(state);
				state.consume('_');
				state.buffer.append("<virtual base override at offset ");
				appendHexNumber(state.buffer, offset);
				state.buffer.append(", vcall offset ");
				appendHexNumber(state.buffer, voffset);
				state.buffer.append(" for ");
				state.buffer.append(unmangleEncoding(state));
				state.buffer.append('>');
				break;
			}	
			default:
				throw state.unexpected("special name");
			}
		} else if (ch == 'G') {
			switch (state.get()) {
			case 'V':
				state.buffer.append("<one-time-init guard for ");
				state.buffer.append(unmangleName(state));
				state.buffer.append('>');
				break;
			default:
				throw state.unexpected("special name");
			}
		}
		
		return state.pop();
	}

	private void appendHexNumber(StringBuilder builder, int offset) {
		if (offset < 0) {
			builder.append("-0x");
			builder.append(Integer.toHexString(-offset));
		} else {
			builder.append("0x");
			builder.append(Integer.toHexString(offset));
		}
	}

	/**
	 * @param state
	 * @param name
	 * @return
	 * @throws UnmanglingException 
	 */
	private String doUnmangleFunctionWithName(UnmangleState state, boolean expectReturn, String name) throws UnmanglingException {
		state.push();

		state.consume('F');
		
		if (expectReturn) {
			state.buffer.append(unmangleType(state));
			state.buffer.append(' ');
		}
		
		if (name != null)
			state.buffer.append(name);
		
		state.buffer.append(unmangleBareFunctionType(state, false));
		
		state.consume('E');
		
		return state.pop();
	}

	/**
	 * @param state
	 * @param expectReturn true if a return type precedes argument list
	 * @throws UnmanglingException 
	 */
	private String unmangleBareFunctionType(UnmangleState state, boolean expectReturn) throws UnmanglingException {
		state.push();
		if (expectReturn) {
			state.buffer.append(unmangleType(state));
			state.buffer.append(' ');
		}
		state.buffer.append('(');
		if (state.peek() == 'v') {
			state.skip();
		} else {
			boolean first = true;
			while (!state.done() && state.peek() != 'E') {
				if (first) {
					first = false;
				} else {
					state.buffer.append(',');
				}
				state.buffer.append(unmangleType(state));
			}
		}
		state.buffer.append(')');
		return state.pop();
	}

	/*
    <name> ::= <nested-name>  	= N ...
	   ::= <unscoped-name> 		= number or St ...
	   ::= <unscoped-template-name> <template-args>		= unscoped | S ... | I ...
	   ::= <local-name>	# See Scope Encoding below		=  Z ...

	 */
	private String unmangleName(UnmangleState state) throws UnmanglingException {
		state.push();
		char ch = state.peek();
		if (ch == 'N') {
			state.buffer.append(unmangleNestedName(state));
		} else if (ch == 'Z') {
			state.buffer.append(unmangleLocalName(state));
		} else if (ch == 0) {
			state.throwIfDone();
		} else {
			// must be unscoped-name or unscoped-template-name
			
			if (ch == 'S' && state.peek(1) == 't') {
				state.skip2();
				state.buffer.append("::std::");
				state.buffer.append(unmangleUnqualifiedName(state));
				return state.pop();
			} else {
				String name = unmangleUnqualifiedName(state);
				state.buffer.append(name);
				if (state.peek() == 'I') {
					// unscoped-template-name
					state.remember(name, SubstType.PREFIX);
					state.buffer.append(unmangleTemplateArgs(state, false));
				}
			}
		}
		return state.pop();
	}
	
	/*
 <local-name> := Z <function encoding> E <entity name> [<discriminator>]
               := Z <function encoding> E s [<discriminator>]

  <discriminator> := _ <non-negative number>      # when number < 10
                  := __ <non-negative number> _   # when number >= 10

	 */
	private String unmangleLocalName(UnmangleState state) throws UnmanglingException {
		state.push();
		state.consume('Z');
		state.buffer.append(unmangleEncoding(state));
		state.consume('E');
		
		boolean isStringLiteral = false;
		if (state.peek() == 's') {
			isStringLiteral = true;
			state.skip();
			if (state.peek() == '_')
				state.buffer.append("::");
		} else {
			addNameWithColons(state, unmangleName(state));
		}
		if (state.peek() == '_') {
			state.skip();
			int num;
			if (state.peek() == '_') {
				// >= 10
				num = doUnmangleNonNegativeNumber(state);
				state.consume('_');
			} else {
				char ch = state.get();
				if (ch >= '0' && ch <= '9') {
					num = ch - '0';
				} else {
					throw state.unexpected("number");
				}
			}
			if (isStringLiteral)
				state.buffer.append("string literal");
			state.buffer.append("#" + num);
		}
		return state.pop();
	}

	/*
    <source-name> ::= <positive length number> <identifier>
    <number> ::= [n] <non-negative decimal integer>
    <identifier> ::= <unqualified source code identifier>
	 */
	private String unmangleSourceName(UnmangleState state) throws UnmanglingException {
		state.push();
		char ch = state.peek();
		if (ch >= '0' && ch <= '9') {
			int length = doUnmangleNumber(state);
			while (length-- > 0) {
				state.throwIfDone();
				state.buffer.append(state.get());
			}
			return state.pop();
		} else {
			throw state.unexpected();
		}
	}
	
	/*
	 * [0-9]+
	 */
	private int doUnmangleNonNegativeNumber(UnmangleState state) {
		int number = 0;
		char ch;
		while ((ch = state.get()) != 0 && ch >= '0' && ch <= '9') {
			number = number * 10 + (ch - '0');
		}
		state.unget();
		return number;
	}

	/*
	 * [n] <non-negative decimal number>
	 */
	private int doUnmangleNumber(UnmangleState state) {
		boolean neg = false;
		if (state.peek() == 'n') {
			state.skip();
			neg = true;
		}
		int number = doUnmangleNonNegativeNumber(state);
		return neg ? -number : number;
	}

	/*
    <nested-name> ::= N [<CV-qualifiers>] <prefix> <unqualified-name> E
		  ::= N [<CV-qualifiers>] <template-prefix> <template-args> E    (args = I...)

	 */
	private String unmangleNestedName(UnmangleState state) throws UnmanglingException {
		state.push();
		
		state.consume('N');
		String cvquals = unmangleCVQualifiers(state);
		
		// The parse is not LR(1) or even unambiguous, so we need backtracking.
		try {
			state.safePush();
			String templPrefix = unmangleTemplatePrefix(state);
			String templArgs = unmangleTemplateArgs(state, true);
			state.consume('E');
			state.safePop();
			state.buffer.append(templPrefix);
			state.buffer.append(templArgs);
		} catch (UnmanglingException e) {
			state.safeBacktrack(); 
			
			String prefix = unmanglePrefix(state, SubstType.PREFIX);
			
			// due to fishiness in mutual recursion, the prefix may have already consumed the unqualified name
			String unqualName = null;
			if (prefix.length() > 0 && state.peek() != 'E') {
				unqualName = unmangleUnqualifiedName(state);
			}
			state.consume('E');
			
			state.buffer.append(prefix);
			if (prefix.length() > 0 && unqualName != null) {
				addNameWithColons(state, unqualName);
			}
			
		}
		if (cvquals.length() > 0) {
			state.buffer.append(' ');
			state.buffer.append(cvquals);
		}
		return state.pop();
	}

	
	/*
  <template-args> ::= I <template-arg>+ E

	 */
	private String unmangleTemplateArgs(UnmangleState state, boolean atLeastOne) throws UnmanglingException {
		state.push();
		state.consume('I');
		
		state.buffer.append('<');
		if (atLeastOne || state.peek() != 'E') {
			boolean first = true;
			do {
				if (first)
					first = false;
				else
					state.buffer.append(',');
				state.buffer.append(unmangleTemplateArg(state));
			} while (state.peek() != 'E');
		}
		state.skip();
		if (state.buffer.lastIndexOf(">") == state.buffer.length())
			state.buffer.append(' ');
		state.buffer.append('>');
		
		return state.pop();
	}

	/*
  <template-arg> ::= <type>                                        # type or template
                 ::= X <expression> E                              # expression
                 ::= <expr-primary>                                # simple expressions ('L')
                 ::= I <template-arg>* E                           # argument pack
                 ::= sp <expression>                               # pack expansion of (C++0x)

	 */
	private String unmangleTemplateArg(UnmangleState state) throws UnmanglingException {
		state.push();
		
		String arg = null;
		char ch = state.peek();
		if (ch == 'X') {
			throw state.notImplemented();
		} else if (ch == 'I') {
			arg = unmangleTemplateArgs(state, false);
		} else if (ch == 's' && state.peek(1) == 'p') {
			throw state.notImplemented();
		} else if (ch == 'L'){
			//state.buffer.append(unmangleExprPrimary(state));
			throw state.notImplemented();
		} else {
			arg = unmangleType(state);
		}
		state.rememberTemplateArg(arg);
		state.buffer.append(arg);
		
		return state.pop();
	}

	/*
  <template-param> ::= T_	# first template parameter
		   ::= T <parameter-2 non-negative number> _
  <template-template-param> ::= <template-param>
			    ::= <substitution>

		   
	 */
	private String unmangleTemplateParam(UnmangleState state) throws UnmanglingException {
		state.push();
		
		char ch = state.peek();
		if (ch == 'S') {
			state.buffer.append(unmangleSubstitution(state));
			return state.pop();
		}
		else if (ch == 'T') {
			state.skip();
			int num = doUnmangleBase10(state);
			state.buffer.append(state.getTemplateArg(num));
		} 
		else
			throw state.unexpected();
		
		return state.popAndRemember(SubstType.TEMPLATE_TEMPLATE_PARAM);
	}

	/**
	 * Base-10, where _ = 0 and 1..x = 0..x-1
	 * @param state
	 * @return
	 * @throws UnmanglingException 
	 */
	private int doUnmangleBase10(UnmangleState state) throws UnmanglingException {
		char ch;
		if (state.peek() == '_') {
			state.skip();
			return 0;
		}
		int num = 0;
		while ((ch = state.get()) != '_') {
			state.throwIfDone();
			num = (num * 10) + (ch - '0');
		}
		return num + 1;
	}

	/*
  <substitution> ::= S <seq-id> _
		 ::= S_

   <substitution> ::= St # ::std::
   <substitution> ::= Sa # ::std::allocator
   <substitution> ::= Sb # ::std::basic_string
   <substitution> ::= Ss # ::std::basic_string < char,
						 ::std::char_traits<char>,
						 ::std::allocator<char> >
   <substitution> ::= Si # ::std::basic_istream<char,  std::char_traits<char> >
   <substitution> ::= So # ::std::basic_ostream<char,  std::char_traits<char> >
   <substitution> ::= Sd # ::std::basic_iostream<char, std::char_traits<char> >
		 
	 */
	private String unmangleSubstitution(UnmangleState state) throws UnmanglingException {
		state.push();
		state.consume('S');
		
		char ch = state.peek();
		if (ch == '_' || (ch >= '0' && ch <= '9')) {
			int num = doUnmangleBase36(state);
			if (num < 0 || num >= state.substitutions.size()) 
				throw state.unexpected("substitution id in the range 0-"+ state.substitutions.size() + " but got " + num);
			String val = state.substitutions.get(num);
			
			SubstType type = state.substitutionTypes.get(num);
			switch (type) {
			case PREFIX:
				//...?
				state.buffer.append(val);
				break;
			case TEMPLATE_PREFIX:
			case TEMPLATE_TEMPLATE_PARAM:
				state.buffer.append(val);
				state.buffer.append(unmangleTemplateArgs(state, false));
				break;
			case TYPE:
				// ...?
				state.buffer.append(val);
				break;
			}
		} else {
			switch (ch) {
			case 't':
				state.buffer.append("::std"); break;
			case 'a':
				state.buffer.append("::std::allocator"); break;
			case 'b':
				state.buffer.append("::std::basic_string"); break;
			case 's':
				state.buffer.append("::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> >"); break;
			case 'i':
				state.buffer.append("::std::basic_istream<char,::std::char_traits<char> >"); break;
			case 'o':
				state.buffer.append("::std::basic_ostream<char,::std::char_traits<char> >"); break;
			case 'd':
				state.buffer.append("::std::basic_iostream<char,::std::char_traits<char> >"); break;
			default:
				throw state.unexpected("std:: substitution");
			}
			state.skip();
		}
		
		return state.pop();
	}

	/**
	 * As a special case, the first substitutable entity is encoded as "S_",
	 * i.e. with no number, so the numbered entities are the second one as
	 * "S0_", the third as "S1_", the twelfth as "SA_", the thirty-eighth as
	 * "S10_", etc.
	 * @throws UnmanglingException 
	 */
	private int doUnmangleBase36(UnmangleState state) throws UnmanglingException {
		int num = 0;
		char ch = state.peek();
		if (ch == '_') {
			state.skip();
			return 0;
		}
		while ((ch = state.get()) != '_') {
			state.throwIfDone();
			num = (num * 10);
			if (ch >= '0' && ch <= '9')
				num += (ch - '0');
			else if (ch >= 'A' && ch <= 'Z')
				num += (ch - 'A') + 10;
			else
				throw state.unexpected("BASE-36 number");
		}
		return num + 1;
	}

	/*
    <prefix> ::= <prefix> <unqualified-name>  # ... 0-9
	     	 ::= <template-prefix> <template-args>   --> template=T... args=I...
             ::= <template-param>	--> T... 
	         ::= # empty
	         ::= <substitution>		--> S...
             ::= <prefix> <data-member-prefix>  --> name M
             
     left-recursion elimination:
     <prefix> ::= <template-prefix> <template-args> <prefix'>
     		::= <template-param> <prefix'>
     		::= <substitution> <prefix'>
     		::= # empty
     <prefix'> ::= <unqualified-name> <prefix'>
     			::= <data-member-prefix> M <prefix'>
     			::= #empty
	 */
	private String unmanglePrefix(UnmangleState state, SubstType substType) throws UnmanglingException {
		state.push();
		
		char ch = state.peek();
		
		if (ch == 'T') {
			state.buffer.append(unmangleTemplateParam(state));
			state.remember(substType);
		}
		else if (ch == 'S') {
			state.buffer.append(unmangleSubstitution(state));
			state.remember(substType);
		} 
		
		
		// prefix'
		while (true) {
			try {
				// loose: data-member-prefix is only a source-name
				String name = unmangleUnqualifiedName(state);
				addNameWithColons(state, name);
				state.remember(substType);
			} catch (UnmanglingException e) {
				// empty
				break;
			}
			
			ch = state.peek();
			if (ch == 'M') {
				state.skip();
				// TODO: formatting?
			}
			else if (ch == 'I') {
				// we cheat here to break the mutual recursion: 
				// let this prefix handler subsume N number of prefixes,
				// and the template prefix handler only handle arguments
				state.updateSubstitution(SubstType.TEMPLATE_PREFIX);
				state.buffer.append(unmangleTemplateArgs(state, false));
				break;
			}
		}
		
			
		return state.pop();
	}

	/**
	 * @param state
	 * @param name
	 */
	private void addNameWithColons(UnmangleState state, String name) {
		if (state.current().length() > 0 && !name.startsWith("::"))
			state.buffer.append("::");
		state.buffer.append(name);
	}

	/*
	<template-prefix> ::= <prefix> <template unqualified-name>  ... 0-9
	                  ::= <template-param>  # T*
	                  ::= <substitution>	# S*
	--> followed by <template-args> (I)
	
	left-recursion elimination:
	  <template-prefix> ::= <template-param> <template-prefix'> # T*
	                  ::= <substitution> <template-prefix'>	# S*
	  <template-prefix'> ::= <template unqualified-name> <template-prefix'> ... 0-9
	  				::= #empty
	--> followed by <template-args> (I)
	 */
	private String unmangleTemplatePrefix(UnmangleState state) throws UnmanglingException {
		state.push();
		
		char ch = state.peek();
		if (ch == 'S') {
			state.buffer.append(unmangleSubstitution(state));
			state.buffer.append(unmangleTemplatePrefixPrime(state));
			return state.pop();
		}
		
		if (ch == 'T') {
			state.buffer.append(unmangleTemplateParam(state));
			state.buffer.append(unmangleTemplatePrefixPrime(state));
			return state.popAndRemember(SubstType.TEMPLATE_PREFIX);
		//} else if (ch == 'I') {
		//	// end of prefix
		}
		
		// we cheat here to break mutual recursion
		
		state.buffer.append(unmanglePrefix(state, SubstType.TEMPLATE_PREFIX));
		String unqualifiedName = unmangleUnqualifiedName(state);
		addNameWithColons(state, unqualifiedName);
		
		return state.popAndRemember(SubstType.TEMPLATE_PREFIX);
	}

	private String unmangleTemplatePrefixPrime(UnmangleState state) throws UnmanglingException {
		state.push();
		while (true) {
			try {
				state.buffer.append(unmangleUnqualifiedName(state));
			} catch (UnmanglingException e) {
				break;
			}
		}
		
		return state.pop();
	}

	/*
<type> ::= <builtin-type>  = rVKPROCGU ...
	 ::= <function-type>   = 
	 ::= <class-enum-type>
	 ::= <array-type>
	 ::= <pointer-to-member-type>  = M...
	 ::= <template-param>
	 ::= <template-template-param> <template-args>
	 ::= <substitution> # See Compression below

  <type> ::= <CV-qualifiers> <type>
	 ::= P <type>	# pointer-to
	 ::= R <type>	# reference-to
	 ::= O <type>	# rvalue reference-to (C++0x)
	 ::= C <type>	# complex pair (C 2000)
	 ::= G <type>	# imaginary (C 2000)
	 ::= U <source-name> <type>	# vendor extended type qualifier

  <CV-qualifiers> ::= [r] [V] [K] 	# restrict (C99), volatile, const
	 */
	private String unmangleType(UnmangleState state) throws UnmanglingException {
		state.push();
		char ch = state.get(); 
		switch (ch) {
		//
		// qualified types
		//
		case 'r':
		case 'V':
		case 'K':
			state.unget();
			String cvquals = unmangleCVQualifiers(state);
			state.buffer.append(unmangleType(state));
			if (cvquals.length() > 0) {
				state.buffer.append(' ');
				state.buffer.append(cvquals);
			}
			return state.popAndRemember(SubstType.TYPE);
		case 'P':
			state.buffer.append(unmangleType(state));
			state.buffer.append("*");
			return state.popAndRemember(SubstType.TYPE);
		case 'R':
			state.buffer.append(unmangleType(state));
			state.buffer.append("&"); 
			return state.popAndRemember(SubstType.TYPE);
		case 'O': // rvalue reference-to
		case 'C': // complex pair
		case 'G': // imaginary
			throw state.notImplemented(); 
		case 'U': // vendor extension
		{
			// TODO: assuming the extension precedes the type,
			// e.g. int __declspec(dllimport) foo();
			state.buffer.append(unmangleSourceName(state));
			state.buffer.append(' '); 
			state.buffer.append(unmangleType(state));
			return state.popAndRemember(SubstType.TYPE);
		}
		
		//
		// built-in types
		//
		case 'v':
			state.buffer.append("void"); break;
		case 'w':
			state.buffer.append("wchar_t"); break;
		case 'b':
			state.buffer.append("bool"); break;
		case 'c':
			state.buffer.append("char"); break;
		case 'a':
			state.buffer.append("signed char"); break;
		case 'h':
			state.buffer.append("unsigned char"); break;
		case 's':
			state.buffer.append("short"); break;
		case 't':
			state.buffer.append("unsigned short"); break;
		case 'i':
			state.buffer.append("int"); break;
		case 'j':
			state.buffer.append("unsigned int"); break;
		case 'l':
			state.buffer.append("long"); break;
		case 'm':
			state.buffer.append("unsigned long"); break;
		case 'x':
			state.buffer.append("long long"); break;
		case 'y':
			state.buffer.append("unsigned long long"); break;
		case 'n':
			state.buffer.append("__int128"); break;
		case 'o':
			state.buffer.append("unsigned __int128"); break;
		case 'f':
			state.buffer.append("float"); break;
		case 'd':
			state.buffer.append("double"); break;
		case 'e':
			state.buffer.append("long double"); break;
		case 'g':
			state.buffer.append("__float128"); break;
		case 'z':
			state.buffer.append("..."); break;
		case 'D': {
			ch = state.get();
			switch (ch) {
			case 'd':
				state.buffer.append("::std::decimal::decimal64"); break;
			case 'e':
				state.buffer.append("::std::decimal::decimal128"); break;
			case 'f':
				state.buffer.append("::std::decimal::decimal32"); break;
			case 'h':
				state.buffer.append("::std::decimal::binary16"); break; // TODO: a guess; what's the actual C++ name for the half-float?
			case 'i':
				state.buffer.append("char32_t"); break;
			case 's':
				state.buffer.append("char16_t"); break;
			default:
				// Dp, Dt, DT
				state.unget(); throw state.notImplemented();
			}
		}
		case 'u':
			state.buffer.append(unmangleName(state)); 
			return state.popAndRemember(SubstType.TYPE);
			
		//
		// <class-enum-type> ::= <unqualified-name> | <nested-name>
		//
		case 'N':
			state.buffer.append(unmangleNestedName(state));
			state.remember(SubstType.TYPE);
			break;
			
		case 'F':
			// <function-type> ::= F [Y] <bare-function-type> E
			if (state.peek() == 'Y') {
				state.skip();
				state.buffer.append("extern \"C\" ");
			}
			state.buffer.append(unmangleBareFunctionType(state, true));
			state.consume('E');
			state.remember(SubstType.TYPE);
			break;
			
		case 'M': {
			state.unget();
			String name = unmanglePtm(state);
			state.buffer.append(name); 
			state.remember(name, SubstType.TYPE);
			break;
		}
			
		case 'S':
			state.unget();
			state.buffer.append(unmangleSubstitution(state)); 
			break;
			
		case 'T':
			// either <template-param> or <template-template-param> <template-args>
			state.unget();
			state.buffer.append(unmangleTemplateParam(state));
			if (state.peek() == 'I') {
				state.buffer.append(unmangleTemplateArgs(state, false));
			}
			break;
			
		case 'A':
			state.unget();
			state.buffer.append(unmangleArrayType(state));
			break;
			
		default:
			state.unget();
			state.buffer.append(unmangleUnqualifiedName(state));
			state.remember(SubstType.TYPE);
			break;
		}
		return state.pop();
	}

	
	/*
  <array-type> ::= A <positive dimension number> _ <element type>
	       ::= A [<dimension expression>] _ <element type>

	 */
	private String unmangleArrayType(UnmangleState state) throws UnmanglingException {
		state.push();
		state.consume('A');
		
		String count; 
		
		char ch = state.peek();
		if (ch >= '0' && ch <= '9') {
			int num = doUnmangleNonNegativeNumber(state);
			count = "" + num;
		} else {
			throw state.notImplemented();
		}
		state.consume('_');
		
		state.buffer.append(unmangleType(state));
		
		state.buffer.append('[');
		state.buffer.append(count);
		state.buffer.append(']');
		
		return state.pop();
	}

	/*
   <pointer-to-member-type> ::= M <class type> <member type>
	 */
	private String unmanglePtm(UnmangleState state) throws UnmanglingException {
		state.push();
		state.consume('M');
		String klass = unmangleType(state);
		String ptrquals = unmangleCVQualifiers(state);
		try {
			state.safePush();
			state.buffer.append(doUnmangleFunctionWithName(state, true, '(' + klass + "::*)"));
			state.safePop();
		} catch (UnmanglingException e) {
			// may be pointer to member (field)
			state.safeBacktrack();
			state.buffer.append(unmangleType(state));
			state.buffer.append(' ');
			state.buffer.append(klass);
			state.buffer.append("::*");
		}
		if (ptrquals.length() > 0) {
			state.buffer.append(' ');
			state.buffer.append(ptrquals);
		}
		return state.pop();
	}

	/**
	 * Unmangle any sequence of CV quals 
	 * @param state state
	 * @return String
	 */
	private String unmangleCVQualifiers(UnmangleState state) {
		state.push();
		while (true) {
			boolean matched = true;
			switch (state.peek()) {
			case 'r':
				state.skip();
				if (state.current().length() > 0) state.buffer.append(' ');
				state.buffer.append("restrict"); 
				break;
			case 'V':
				state.skip();
				if (state.current().length() > 0) state.buffer.append(' ');
				state.buffer.append("volatile"); 
				break;
			case 'K':
				state.skip();
				if (state.current().length() > 0) state.buffer.append(' ');
				state.buffer.append("const"); 
				break;
			default:
				matched = false;
				break;
			}
			if (!matched)
				break;
		}
		return state.pop();
	}
	
	static class Operator {
		String name;
		/** for unary or binary ops; other questionable ones are 0 */
		int numops;
		
		public Operator(String name, int numops) {
			this.name = name;
			this.numops = numops;
		}
	}
	
	static Map<String, Operator> operators = new HashMap<String, Operator>();
	
	private static void registerOperator(String code, String name, int opcnt) {
		if (operators.containsKey(code))
			throw new IllegalStateException();
		operators.put(code, new Operator(name, opcnt));		
	}

	static {
		registerOperator("nw", "new", 0);
		registerOperator("na", "new[]", 0);
		registerOperator("dl", "delete", 0);
		registerOperator("da", "delete[]", 0);
		registerOperator("ps", "+", 1);
		registerOperator("ng", "-", 1);
		registerOperator("ad", "&", 1);
		registerOperator("de", "*", 1);
		registerOperator("co", "~", 1);
		registerOperator("pl", "+", 2);
		registerOperator("mi", "-", 2);
		registerOperator("ml", "*", 2);
		registerOperator("dv", "/", 2);
		registerOperator("rm", "%", 2);
		registerOperator("an", "&", 2);
		registerOperator("or", "|", 2);
		registerOperator("eo", "^", 2);
		registerOperator("aS", "=", 2);
		registerOperator("pL", "+=", 2);
		registerOperator("mI", "-=", 2);
		registerOperator("mL", "*=", 2);
		registerOperator("dV", "/=", 2);
		registerOperator("rM", "%=", 2);
		registerOperator("aN", "&=", 2);
		registerOperator("oR", "|=", 2);
		registerOperator("eO", "^=", 2);
		registerOperator("ls", "<<", 2);
		registerOperator("rs", ">>", 2);
		registerOperator("lS", "<<=", 2);
		registerOperator("rS", ">>=", 2);
		registerOperator("eq", "==", 2);
		registerOperator("ne", "!=", 2);
		registerOperator("lt", "<", 2);
		registerOperator("gt", ">", 2);
		registerOperator("le", "<=", 2);
		registerOperator("ge", ">=", 2);
		registerOperator("nt", "!", 1);
		registerOperator("aa", "&&", 2);
		registerOperator("oo", "||", 2);
		registerOperator("pp", "++", 1);
		registerOperator("mm", "--", 1);
		registerOperator("cm", ",", 2);
		registerOperator("pm", "->*", 2);
		registerOperator("pt", "->", 2);
		registerOperator("cl", "()", 1);
		registerOperator("ix", "[]", 2);
		registerOperator("qu", "?", 3);
		registerOperator("st", "sizeof ", 0);	// type
		registerOperator("sz", "sizeof", 1); 	// expression
		registerOperator("at", "alignof ", 0);	// type
		registerOperator("az", "alignof", 1); 	// expression
		registerOperator("cv", "()", 1);
	}
	
	/*
    <unqualified-name> ::= <operator-name>			= lowercase
                       ::= <ctor-dtor-name>			= C1-3 D0-2   ...
                       ::= <source-name>   			= <number> ...
                       ::= <unnamed-type-name>   	= Ut ...

	 */
	private String unmangleUnqualifiedName(UnmangleState state) throws UnmanglingException {
		char ch = state.peek();
		if (ch >= '0' && ch <= '9') {
			return unmangleSourceName(state);
		}
		else if (ch >= 'a' && ch <= 'z') {
			return unmangleOperatorName(state);
		}
		else if (ch == 'U') {
			return unmangleUnnamedTypeName(state);
		}
		else if (ch == 'C') {
			state.push();
			String last = state.lastSubstitutedName();
			state.get();
			switch (state.get()) {
			case '1':
			case '2':
			case '3':
				state.buffer.append(last);
				return state.pop();
			default:
				state.unget();
				throw state.unexpected("constructor name");
			}
		}
		else if (ch == 'D') {
			state.push();
			String last = state.lastSubstitutedName();
			state.get();
			switch (state.get()) {
			case '0':
			case '1':
			case '2':
				state.buffer.append(last);
				return state.pop();
			default:
				state.unget();
				throw state.unexpected("constructor name");
			}
		}
		throw state.unexpected();
	}

	/*
  <unnamed-type-name> ::= Ut [ <nonnegative number> ] _ 
  <unnamed-type-name> ::= <closure-type-name>

  <closure-type-name> ::= Ul <lambda-sig> E [ <nonnegative number> ] _ 

	 */
	private String unmangleUnnamedTypeName(UnmangleState state) throws UnmanglingException {
		state.push();
		state.consume('U');
		switch (state.get()) {
		case 't':
			state.buffer.append("<unnamed #");
			if (state.peek() != '_') {
				state.buffer.append("" + (doUnmangleNonNegativeNumber(state) + 2));
			} else {
				state.buffer.append("1");
			}
			state.buffer.append('>');
			state.consume('_');
			break;
		case 'l':
			throw state.notImplemented();
		default:
			throw state.unexpected();
		}
		return state.pop();
	}

	/*
	 */
	private String unmangleOperatorName(UnmangleState state) throws UnmanglingException {
		state.push();
		char ch = state.get();
		String op = "" + ch;
		if (ch == 'v') {
			// vendor type <digit> <source-name>
			ch = state.get();
			if (ch >= '0' && ch <= '9') {
				int opcount = ch - '0';
				op = unmangleSourceName(state);
				boolean first = true;
				
				// pretend it's a function, to differentiate
				state.buffer.append('(');
				while (opcount-- > 0) {
					if (first)
						first = false;
					else
						state.buffer.append(',');
				}
				state.buffer.append(')');
			} else {
				throw state.unexpected();
			}
			return state.pop();
		}
		
		ch = state.get();
		if (!Character.isLetter(ch)) {
			throw state.unexpected();
		}
		
		op += ch;
		
		Operator oper = operators.get(op);
		if (oper == null) {
			throw state.unexpected();
		}
		
		state.buffer.append("operator ");
		
		// special cases
		if (op.equals("cv")) {
			state.buffer.append(unmangleType(state));
			// fall through
		}
		
		state.buffer.append(oper.name);
		return state.pop();
	
	}
}
