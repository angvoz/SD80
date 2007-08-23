/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner.MacroData;

public class ExpressionEvaluator {

    private static char[] emptyCharArray = new char[0];

    // The context stack
    private static final int initSize = 8;

    private int bufferStackPos = -1;

    private char[][] bufferStack = new char[initSize][];

    private Object[] bufferData = new Object[initSize];

    private int[] bufferPos = new int[initSize];

    private int[] bufferLimit = new int[initSize];

    private ScannerCallbackManager callbackManager = null;

    private ScannerProblemFactory problemFactory = null;

    private int lineNumber = 1;

    private char[] fileName = null;

    private int pos = 0;

    // The macros
    CharArrayObjectMap definitions;

    public ExpressionEvaluator() {
        super();
    }

    public ExpressionEvaluator(ScannerCallbackManager manager,
            ScannerProblemFactory spf) {
        this.callbackManager = manager;
        this.problemFactory = spf;
    }

    public long evaluate(char[] buffer, int p, int length,
            CharArrayObjectMap defs) {
        return evaluate(buffer, p, length, defs, 0, "".toCharArray()); //$NON-NLS-1$
    }

    public long evaluate(char[] buffer, int p, int length,
            CharArrayObjectMap defs, int ln, char[] fn) {
        this.lineNumber = ln;
        this.fileName = fn;
        bufferStack[++bufferStackPos] = buffer;
        bufferPos[bufferStackPos] = p - 1;
        bufferLimit[bufferStackPos] = p + length;
        this.definitions = defs;
        tokenType = 0;

        long r = 0;
        try {
            r = expression();
        } catch (ExpressionEvaluator.EvalException e) {
        }

        while (bufferStackPos >= 0)
            popContext();

        return r;
    }

    private static class EvalException extends Exception {
    	private static final long serialVersionUID = 0;
        public EvalException(String msg) {
            super(msg);
        }
    }

    private long expression() throws EvalException {
        return conditionalExpression();
    }

    private long conditionalExpression() throws EvalException {
        long r1 = logicalOrExpression();
        if (LA() == tQUESTION) {
            consume();
            long r2 = expression();
            if (LA() == tCOLON)
                consume();
            else {
                handleProblem(IProblem.SCANNER_BAD_CONDITIONAL_EXPRESSION,
                        pos);
                throw new EvalException("bad conditional expression"); //$NON-NLS-1$
            }
            long r3 = conditionalExpression();
            return r1 != 0 ? r2 : r3;
        }
        return r1;
    }

    private long logicalOrExpression() throws EvalException {
        long r1 = logicalAndExpression();
        while (LA() == tOR) {
            consume();
            long r2 = logicalAndExpression();
            r1 = ((r1 != 0) || (r2 != 0)) ? 1 : 0;
        }
        return r1;
    }

    private long logicalAndExpression() throws EvalException {
        long r1 = inclusiveOrExpression();
        while (LA() == tAND) {
            consume();
            long r2 = inclusiveOrExpression();
            r1 = ((r1 != 0) && (r2 != 0)) ? 1 : 0;
        }
        return r1;
    }

    private long inclusiveOrExpression() throws EvalException {
        long r1 = exclusiveOrExpression();
        while (LA() == tBITOR) {
            consume();
            long r2 = exclusiveOrExpression();
            r1 = r1 | r2;
        }
        return r1;
    }

    private long exclusiveOrExpression() throws EvalException {
        long r1 = andExpression();
        while (LA() == tBITXOR) {
            consume();
            long r2 = andExpression();
            r1 = r1 ^ r2;
        }
        return r1;
    }

    private long andExpression() throws EvalException {
        long r1 = equalityExpression();
        while (LA() == tBITAND) {
            consume();
            long r2 = equalityExpression();
            r1 = r1 & r2;
        }
        return r1;
    }

    private long equalityExpression() throws EvalException {
        long r1 = relationalExpression();
        for (int t = LA(); t == tEQUAL || t == tNOTEQUAL; t = LA()) {
            consume();
            long r2 = relationalExpression();
            if (t == tEQUAL)
                r1 = (r1 == r2) ? 1 : 0;
            else
                // t == tNOTEQUAL
                r1 = (r1 != r2) ? 1 : 0;
        }
        return r1;
    }

    private long relationalExpression() throws EvalException {
        long r1 = shiftExpression();
        for (int t = LA(); t == tLT || t == tLTEQUAL || t == tGT
                || t == tGTEQUAL; t = LA()) {
            consume();
            long r2 = shiftExpression();
            switch (t) {
            case tLT:
                r1 = (r1 < r2) ? 1 : 0;
                break;
            case tLTEQUAL:
                r1 = (r1 <= r2) ? 1 : 0;
                break;
            case tGT:
                r1 = (r1 > r2) ? 1 : 0;
                break;
            case tGTEQUAL:
                r1 = (r1 >= r2) ? 1 : 0;
                break;
            }
        }
        return r1;
    }

    private long shiftExpression() throws EvalException {
        long r1 = additiveExpression();
        for (int t = LA(); t == tSHIFTL || t == tSHIFTR; t = LA()) {
            consume();
            long r2 = additiveExpression();
            if (t == tSHIFTL)
                r1 = r1 << r2;
            else
                // t == tSHIFTR
                r1 = r1 >> r2;
        }
        return r1;
    }

    private long additiveExpression() throws EvalException {
        long r1 = multiplicativeExpression();
        for (int t = LA(); t == tPLUS || t == tMINUS; t = LA()) {
            consume();
            long r2 = multiplicativeExpression();
            if (t == tPLUS)
                r1 = r1 + r2;
            else
                // t == tMINUS
                r1 = r1 - r2;
        }
        return r1;
    }

    private long multiplicativeExpression() throws EvalException {
        long r1 = unaryExpression();
        for (int t = LA(); t == tMULT || t == tDIV || t == tMOD; t = LA()) {
            int position = pos; // for IProblem /0 below, need position
                                // before
            // consume()
            consume();
            long r2 = unaryExpression();
            if (t == tMULT)
                r1 = r1 * r2;
            else if (r2 != 0) {
            	if (t == tDIV)
            		r1 = r1 / r2;
            	else
            		r1 = r1 % r2;	//tMOD
            } else {
                handleProblem(IProblem.SCANNER_DIVIDE_BY_ZERO, position);
                throw new EvalException("Divide by 0 encountered"); //$NON-NLS-1$
            }
        }
        return r1;
    }

    private long unaryExpression() throws EvalException {
        switch (LA()) {
        case tPLUS:
            consume();
            return unaryExpression();
        case tMINUS:
            consume();
            return -unaryExpression();
        case tNOT:
            consume();
            return unaryExpression() == 0 ? 1 : 0;
        case tCOMPL:
            consume();
            return ~unaryExpression();
        case tNUMBER:
            return consume();
        case t_defined:
            return handleDefined();
        case tLPAREN:
            consume();
            long r1 = expression();
            if (LA() == tRPAREN) {
                consume();
                return r1;
            }
            handleProblem(IProblem.SCANNER_MISSING_R_PAREN, pos);
            throw new EvalException("missing )"); //$NON-NLS-1$ 
        case tCHAR:
            return getChar();
        default:
            handleProblem(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR, pos);
            throw new EvalException("expression syntax error"); //$NON-NLS-1$ 
        }
    }

    private long handleDefined() throws EvalException {
        // We need to do some special handline to get the identifier without
        // it
        // being
        // expanded by macro expansion
        skipWhiteSpace();

        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        if (++bufferPos[bufferStackPos] >= limit)
            return 0;

        // check first character
        char c = buffer[bufferPos[bufferStackPos]];
        boolean inParens = false;
        if (c == '(') {
            inParens = true;
            skipWhiteSpace();
            if (++bufferPos[bufferStackPos] >= limit)
                return 0;
            c = buffer[bufferPos[bufferStackPos]];
        }

        if (!((c >= 'A' && c <= 'Z') || c == '_' || (c >= 'a' && c <= 'z'))) {
            handleProblem(IProblem.SCANNER_ILLEGAL_IDENTIFIER, pos);
            throw new EvalException("illegal identifier in defined()"); //$NON-NLS-1$ 
        }

        // consume rest of identifier
        int idstart = bufferPos[bufferStackPos];
        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                ++idlen;
                continue;
            }
            break;
        }
        --bufferPos[bufferStackPos];

        // consume to the closing paren;
        if (inParens) {
            skipWhiteSpace();
            if (++bufferPos[bufferStackPos] <= limit
                    && buffer[bufferPos[bufferStackPos]] != ')') {
                handleProblem(IProblem.SCANNER_MISSING_R_PAREN, pos);
                throw new EvalException("missing ) on defined"); //$NON-NLS-1$
            }
        }

        // Set up the lookahead to whatever comes next
        nextToken();

        return definitions.get(buffer, idstart, idlen) != null ? 1 : 0;
    }

    // Scanner part
    int tokenType = tNULL;

    long tokenValue;

    private int LA() throws EvalException {
        if (tokenType == tNULL)
            nextToken();
        return tokenType;
    }

    private long consume() throws EvalException {
        long value = tokenValue;
        if (tokenType != tEOF)
            nextToken();
        return value;
    }

    private long getChar() throws EvalException {
        long value = 0;

        // if getting a character then make sure it's in '' otherwise leave
        // it
        // as 0
        if (bufferPos[bufferStackPos] - 1 >= 0
                && bufferPos[bufferStackPos] + 1 < bufferStack[bufferStackPos].length
                && bufferStack[bufferStackPos][bufferPos[bufferStackPos] - 1] == '\''
                && bufferStack[bufferStackPos][bufferPos[bufferStackPos] + 1] == '\'')
            value = bufferStack[bufferStackPos][bufferPos[bufferStackPos]];

        if (tokenType != tEOF)
            nextToken();
        return value;
    }

    private static char[] _defined = "defined".toCharArray(); //$NON-NLS-1$

    private void nextToken() throws EvalException {
        boolean isHex = false;
        boolean isOctal = false;
        boolean isDecimal = false;

        contextLoop: while (bufferStackPos >= 0) {

            // Find the first thing we would care about
            skipWhiteSpace();

            while (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
                // We're at the end of a context, pop it off and try again
                popContext();
                continue contextLoop;
            }

            // Tokens don't span buffers, stick to our current one
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];
            pos = bufferPos[bufferStackPos];

            if (buffer[pos] >= '1' && buffer[pos] <= '9')
                isDecimal = true;
            else if (buffer[pos] == '0' && pos + 1 < limit)
                if (buffer[pos + 1] == 'x' || buffer[pos + 1] == 'X') {
                    isHex = true;
                    ++bufferPos[bufferStackPos];
                    if (pos + 2 < limit)
                        if ((buffer[pos + 2] < '0' || buffer[pos + 2] > '9')
                                && (buffer[pos + 2] < 'a' || buffer[pos + 2] > 'f')
                                && (buffer[pos + 2] < 'A' || buffer[pos + 2] > 'F'))
                            handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT,
                                    pos);
                } else
                    isOctal = true;

            switch (buffer[pos]) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
                int start = bufferPos[bufferStackPos];
                int len = 1;

                while (++bufferPos[bufferStackPos] < limit) {
                    char c = buffer[bufferPos[bufferStackPos]];
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || c == '_' || (c >= '0' && c <= '9')) {
                        ++len;
                        continue;
                    }
                    break;
                }

                --bufferPos[bufferStackPos];

                // Check for defined(
                pos = bufferPos[bufferStackPos];
                if (CharArrayUtils.equals(buffer, start, len, _defined)) {
                    tokenType = t_defined;
                    return;
                }

                // Check for macro expansion
                Object expObject = null;
                if (bufferData[bufferStackPos] instanceof FunctionStyleMacro.Expansion) {
                    // first check if name is a macro arg
                    expObject = ((FunctionStyleMacro.Expansion) bufferData[bufferStackPos]).definitions
                            .get(buffer, start, len);
                }

                if (expObject == null)
                    // now check regular macros
                    expObject = definitions.get(buffer, start, len);

                if (expObject != null) {
                    if (expObject instanceof FunctionStyleMacro) {
                        handleFunctionStyleMacro((FunctionStyleMacro) expObject);
                    } else if (expObject instanceof ObjectStyleMacro) {
                        ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
                        char[] expText = expMacro.getExpansion();
                        if (expText.length > 0 )
                        {
                            if (BaseScanner.shouldExpandMacro(expMacro, bufferStackPos, bufferData, -1, bufferPos, bufferStack ))
                                pushContext(expText, new MacroData(start, start + len, expMacro));
                            else
                            {
                                if (len == 1) { // is a character
                                    tokenType = tCHAR;
                                    return;
                                }
                                // undefined macro, assume 0
                                tokenValue = 0;
                                tokenType = tNUMBER;
                                return;

                            }
                                
                        }
                    } else if (expObject instanceof char[]) {
                        char[] expText = (char[]) expObject;
                        if (expText.length > 0)
                            pushContext(expText, null);
                    }
                    continue;
                }

                if (len == 1) { // is a character
                    tokenType = tCHAR;
                    return;
                }

                // undefined macro, assume 0
                tokenValue = 0;
                tokenType = tNUMBER;
                return;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                tokenValue = buffer[pos] - '0';
                tokenType = tNUMBER;

                while (++bufferPos[bufferStackPos] < limit) {
                    char c = buffer[bufferPos[bufferStackPos]];
                    if (isHex) {
                        if (c >= '0' && c <= '9') {
                            tokenValue *= 16;
                            tokenValue += c - '0';
                            continue;
                        } else if (c >= 'a' && c <= 'f') {
                            tokenValue = (tokenValue == 0 ? 10
                                    : (tokenValue * 16) + 10);
                            tokenValue += c - 'a';
                            continue;
                        } else if (c >= 'A' && c <= 'F') {
                            tokenValue = (tokenValue == 0 ? 10
                                    : (tokenValue * 16) + 10);
                            tokenValue += c - 'A';
                            continue;
                        } else {
                            if (bufferPos[bufferStackPos] + 1 < limit)
                                if (!isValidTokenSeparator(
                                        c,
                                        buffer[bufferPos[bufferStackPos] + 1]))
                                    handleProblem(
                                            IProblem.SCANNER_BAD_HEX_FORMAT,
                                            pos);
                        }
                    } else if (isOctal) {
                        if (c >= '0' && c <= '7') {
                            tokenValue *= 8;
                            tokenValue += c - '0';
                            continue;
                        }
                        if (bufferPos[bufferStackPos] + 1 < limit)
                            if (!isValidTokenSeparator(c,
                                    buffer[bufferPos[bufferStackPos] + 1]))
                                handleProblem(
                                        IProblem.SCANNER_BAD_OCTAL_FORMAT,
                                        pos);
                    } else if (isDecimal) {
                        if (c >= '0' && c <= '9') {
                            tokenValue *= 10;
                            tokenValue += c - '0';
                            continue;
                        }
                        if (bufferPos[bufferStackPos] + 1 < limit
                                && !(c == 'L' || c == 'l' || c == 'U' || c == 'u'))
                            if (!isValidTokenSeparator(c,
                                    buffer[bufferPos[bufferStackPos] + 1]))
                                handleProblem(
                                        IProblem.SCANNER_BAD_DECIMAL_FORMAT,
                                        pos);
                    }

                    // end of number
                    if (c == 'L' || c == 'l' || c == 'U' || c == 'u') {
                        // eat the long/unsigned
                    	int pos= ++bufferPos[bufferStackPos];
                    	if (pos < limit) {
                    		c= buffer[pos];
                    		if (c == 'L' || c == 'l' || c == 'U' || c == 'u') {
                    			pos= ++bufferPos[bufferStackPos];
                    			// gcc-extension: allow ULL for unsigned long long literals
                            	if (pos < limit) {
                            		c= buffer[pos];
                            		if (c == 'L' || c == 'l' || c == 'U' || c == 'u') {
                            			pos= ++bufferPos[bufferStackPos];
                            		}
                            	}
                    		}
                    	}
                    }

                    // done
                    break;
                }
                --bufferPos[bufferStackPos];
                return;
            case '(':
                tokenType = tLPAREN;
                return;

            case ')':
                tokenType = tRPAREN;
                return;

            case ':':
                tokenType = tCOLON;
                return;

            case '?':
                tokenType = tQUESTION;
                return;

            case '+':
                tokenType = tPLUS;
                return;

            case '-':
                tokenType = tMINUS;
                return;

            case '*':
                tokenType = tMULT;
                return;

            case '/':
                tokenType = tDIV;
                return;

            case '%':
                tokenType = tMOD;
                return;

            case '^':
                tokenType = tBITXOR;
                return;

            case '&':
                if (pos + 1 < limit && buffer[pos + 1] == '&') {
                    ++bufferPos[bufferStackPos];
                    tokenType = tAND;
                    return;
                }
                tokenType = tBITAND;
                return;

            case '|':
                if (pos + 1 < limit && buffer[pos + 1] == '|') {
                    ++bufferPos[bufferStackPos];
                    tokenType = tOR;
                    return;
                }
                tokenType = tBITOR;
                return;

            case '~':
                tokenType = tCOMPL;
                return;

            case '!':
                if (pos + 1 < limit && buffer[pos + 1] == '=') {
                    ++bufferPos[bufferStackPos];
                    tokenType = tNOTEQUAL;
                    return;
                }
                tokenType = tNOT;
                return;

            case '=':
                if (pos + 1 < limit && buffer[pos + 1] == '=') {
                    ++bufferPos[bufferStackPos];
                    tokenType = tEQUAL;
                    return;
                }
                handleProblem(IProblem.SCANNER_ASSIGNMENT_NOT_ALLOWED, pos);
                throw new EvalException("assignment not allowed"); //$NON-NLS-1$ 

            case '<':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tLTEQUAL;
                        return;
                    } else if (buffer[pos + 1] == '<') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tSHIFTL;
                        return;
                    }
                }
                tokenType = tLT;
                return;

            case '>':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tGTEQUAL;
                        return;
                    } else if (buffer[pos + 1] == '>') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tSHIFTR;
                        return;
                    }
                }
                tokenType = tGT;
                return;

            default:
            // skip over anything we don't handle
            }
        }

        // We've run out of contexts, our work is done here
        tokenType = tEOF;
        return;
    }

    private void handleFunctionStyleMacro(FunctionStyleMacro macro) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        skipWhiteSpace();
        if (++bufferPos[bufferStackPos] >= limit
                || buffer[bufferPos[bufferStackPos]] != '(')
            return;

        FunctionStyleMacro.Expansion exp = macro.new Expansion();
        char[][] arglist = macro.arglist;
        int currarg = -1;
        int parens = 0;

        while (bufferPos[bufferStackPos] < limit) {
            if (++currarg >= arglist.length || arglist[currarg] == null)
                // too many args
                break;

            skipWhiteSpace();

            int p = ++bufferPos[bufferStackPos];
            char c = buffer[p];
            if (c == ')') {
                if (parens == 0)
                    // end of macro
                    break;
                --parens;
                continue;
            } else if (c == ',') {
                // empty arg
                exp.definitions.put(arglist[currarg], emptyCharArray);
                continue;
            } else if (c == '(') {
                ++parens;
                continue;
            }

            // peel off the arg
            int argstart = p;
            int argend = argstart - 1;

            // Loop looking for end of argument
            while (bufferPos[bufferStackPos] < limit) {
                skipOverMacroArg();
                argend = bufferPos[bufferStackPos];
                skipWhiteSpace();

                if (++bufferPos[bufferStackPos] >= limit)
                    break;
                c = buffer[bufferPos[bufferStackPos]];
                if (c == ',' || c == ')')
                    break;
            }

            char[] arg = emptyCharArray;
            int arglen = argend - argstart + 1;
            if (arglen > 0) {
                arg = new char[arglen];
                System.arraycopy(buffer, argstart, arg, 0, arglen);
            }
            exp.definitions.put(arglist[currarg], arg);

            if (c == ')')
                break;
        }

        char[] expText = macro.getExpansion();
        if (expText.length > 0)
            pushContext(expText, exp);
    }

    private void skipOverMacroArg() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case ' ':
            case '\t':
            case '\r':
            case ',':
            case ')':
                --bufferPos[bufferStackPos];
                return;
            case '\n':
                lineNumber++;
                --bufferPos[bufferStackPos];
                return;
            case '\\':
                int p = bufferPos[bufferStackPos];
                if (p + 1 < limit && buffer[p + 1] == '\n') {
                    // \n is whitespace
                    lineNumber++;
                    --bufferPos[bufferStackPos];
                    return;
                }
                break;
            case '"':
                boolean escaped = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '"':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    default:
                        escaped = false;
                    }
                }
                break;
            }
        }
        --bufferPos[bufferStackPos];
    }

    private void skipWhiteSpace() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            int p = bufferPos[bufferStackPos];
            switch (buffer[p]) {
            case ' ':
            case '\t':
            case '\r':
                continue;
            case '/':
                if (p + 1 < limit) {
                    if (buffer[p + 1] == '/') {
                        // C++ comment, skip rest of line
                        for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
                            p = bufferPos[bufferStackPos];
                            if (buffer[p] == '\\' && p + 1 < limit
                                    && buffer[p + 1] == '\n') {
                                bufferPos[bufferStackPos] += 2;
                                continue;
                            }
                            if (buffer[p] == '\\' && p + 1 < limit
                                    && buffer[p + 1] == '\r'
                                    && p + 2 < limit
                                    && buffer[p + 2] == '\n') {
                                bufferPos[bufferStackPos] += 3;
                                continue;
                            }

                            if (buffer[p] == '\n')
                                break; // break when find non-escaped
                                       // newline
                        }
                        continue;
                    } else if (buffer[p + 1] == '*') { // C comment, find
                        // closing */
                        for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
                            p = bufferPos[bufferStackPos];
                            if (buffer[p] == '*' && p + 1 < limit
                                    && buffer[p + 1] == '/') {
                                ++bufferPos[bufferStackPos];
                                break;
                            }
                        }
                        continue;
                    }
                }
                break;
            case '\\':
                if (p + 1 < limit && buffer[p + 1] == '\n') {
                    // \n is a whitespace
                    lineNumber++;
                    ++bufferPos[bufferStackPos];
                    continue;
                }
            }

            // fell out of switch without continuing, we're done
            --bufferPos[bufferStackPos];
            return;
        }

        // fell out of while without continuing, we're done
        --bufferPos[bufferStackPos];
        return;
    }

    private static final int tNULL = 0;

    private static final int tEOF = 1;

    private static final int tNUMBER = 2;

    private static final int tLPAREN = 3;

    private static final int tRPAREN = 4;

    private static final int tNOT = 5;

    private static final int tCOMPL = 6;

    private static final int tMULT = 7;

    private static final int tDIV = 8;

    private static final int tMOD = 9;

    private static final int tPLUS = 10;

    private static final int tMINUS = 11;

    private static final int tSHIFTL = 12;

    private static final int tSHIFTR = 13;

    private static final int tLT = 14;

    private static final int tGT = 15;

    private static final int tLTEQUAL = 16;

    private static final int tGTEQUAL = 17;

    private static final int tEQUAL = 18;

    private static final int tNOTEQUAL = 19;

    private static final int tBITAND = 20;

    private static final int tBITXOR = 21;

    private static final int tBITOR = 22;

    private static final int tAND = 23;

    private static final int tOR = 24;

    private static final int tQUESTION = 25;

    private static final int tCOLON = 26;

    private static final int t_defined = 27;

    private static final int tCHAR = 28;

    private void pushContext(char[] buffer, Object data) {
        if (++bufferStackPos == bufferStack.length) {
            int size = bufferStack.length * 2;

            char[][] oldBufferStack = bufferStack;
            bufferStack = new char[size][];
            System.arraycopy(oldBufferStack, 0, bufferStack, 0,
                    oldBufferStack.length);

            Object[] oldBufferData = bufferData;
            bufferData = new Object[size];
            System.arraycopy(oldBufferData, 0, bufferData, 0,
                    oldBufferData.length);

            int[] oldBufferPos = bufferPos;
            bufferPos = new int[size];
            System.arraycopy(oldBufferPos, 0, bufferPos, 0,
                    oldBufferPos.length);

            int[] oldBufferLimit = bufferLimit;
            bufferLimit = new int[size];
            System.arraycopy(oldBufferLimit, 0, bufferLimit, 0,
                    oldBufferLimit.length);
        }

        bufferStack[bufferStackPos] = buffer;
        bufferPos[bufferStackPos] = -1;
        bufferLimit[bufferStackPos] = buffer.length;
        bufferData[bufferStackPos] = data;
    }

    private void popContext() {
        bufferStack[bufferStackPos] = null;
        bufferData[bufferStackPos] = null;
        --bufferStackPos;
    }

    private void handleProblem(int id, int startOffset) {
        if (callbackManager != null && problemFactory != null)
            callbackManager
                    .pushCallback(problemFactory
                            .createProblem(
                                    id,
                                    startOffset,
                                    bufferPos[(bufferStackPos == -1 ? 0
                                            : bufferStackPos)],
                                    lineNumber,
                                    (fileName == null ? "".toCharArray() : fileName), emptyCharArray, false, true)); //$NON-NLS-1$
    }

    private boolean isValidTokenSeparator(char c, char c2)
            throws EvalException {
        switch (c) {
        case '\t':
        case '\r':
        case '\n':
        case ' ':
        case '(':
        case ')':
        case ':':
        case '?':
        case '+':
        case '-':
        case '*':
        case '/':
        case '%':
        case '^':
        case '&':
        case '|':
        case '~':
        case '!':
        case '<':
        case '>':
            return true;
        case '=':
            if (c2 == '=')
                return true;
            return false;
        }

        return false;
    }
}
