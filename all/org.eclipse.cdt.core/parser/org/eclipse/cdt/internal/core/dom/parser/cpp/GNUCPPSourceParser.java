/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *     Ed Swartz (Nokia)
 *     Mike Kucera (IBM) - bug #206952
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.parser.IExtensionToken;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.parser.SimpleDeclarationStrategy;
import org.eclipse.cdt.internal.core.parser.TemplateParameterManager;
import org.eclipse.cdt.internal.core.parser.token.BasicTokenDuple;
import org.eclipse.cdt.internal.core.parser.token.OperatorTokenDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * This is our implementation of the IParser interface, serving as a parser for
 * GNU C and C++. From time to time we will make reference to the ANSI ISO
 * specifications.
 * 
 * @author jcamelon
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {

    private static final String CONST_CAST = "const_cast"; //$NON-NLS-1$
    private static final String REINTERPRET_CAST = "reinterpret_cast"; //$NON-NLS-1$
    private static final String STATIC_CAST = "static_cast"; //$NON-NLS-1$
    private static final String DYNAMIC_CAST = "dynamic_cast"; //$NON-NLS-1$

    private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE = 4;

    
    private ScopeStack templateIdScopes = new ScopeStack();

    private int templateCount = 0;

    protected CPPASTTranslationUnit translationUnit;
    
    private final static class ScopeStack {
        private int[] stack;

        private int index = -1;

        public ScopeStack() {
            stack = new int[8];
        }

        private void grow() {
            int[] newStack = new int[stack.length << 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }

        public void push(int i) {
            if (++index == stack.length)
                grow();
            stack[index] = i;
        }

        public int pop() {
            if (index >= 0)
                return stack[index--];
            return -1;
        }

        public int peek() {
            if (index >= 0)
                return stack[index];
            return -1;
        }

        public int size() {
            return index + 1;
        }
        
    }

    /**
     * Consumes template parameters.
     * 
     * @param previousLast
     *            Previous "last" token (returned if nothing was consumed)
     * @return Last consumed token, or <code>previousLast</code> if nothing
     *         was consumed
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken consumeTemplateParameters(IToken previousLast)
            throws EndOfFileException, BacktrackException {
        int startingOffset = previousLast == null ? LA(1).getOffset()
                : previousLast.getOffset();
        IToken last = previousLast;
        if (LT(1) == IToken.tLT) {
            last = consume();
            // until we get all the names sorted out
            ScopeStack scopes = new ScopeStack();
            scopes.push(IToken.tLT);

            while (scopes.size() > 0) {
                int top;
                last = consume();

                switch (last.getType()) {
                case IToken.tGT:
                    if (scopes.peek() == IToken.tLT) {
                        scopes.pop();
                    }
                    break;
                case IToken.tRBRACKET:
                    do {
                        top = scopes.pop();
                    } while (scopes.size() > 0
                            && (top == IToken.tGT || top == IToken.tLT));
                    if (top != IToken.tLBRACKET)
                        throwBacktrack(startingOffset, last.getEndOffset()
                                - startingOffset);

                    break;
                case IToken.tRPAREN:
                    do {
                        top = scopes.pop();
                    } while (scopes.size() > 0
                            && (top == IToken.tGT || top == IToken.tLT));
                    if (top != IToken.tLPAREN)
                        throwBacktrack(startingOffset, last.getEndOffset()
                                - startingOffset);

                    break;
                case IToken.tLT:
                case IToken.tLBRACKET:
                case IToken.tLPAREN:
                    scopes.push(last.getType());
                    break;
                }
            }
        }
        return last;
    }

    protected List templateArgumentList() throws EndOfFileException,
            BacktrackException {
        IToken start = LA(1);
        int startingOffset = start.getOffset();
        int endOffset = 0;
        start = null;
        List list = new ArrayList();

        boolean completedArg = false;
        boolean failed = false;

        templateIdScopes.push(IToken.tLT);

        while (LT(1) != IToken.tGT && LT(1) != IToken.tEOC) {
            completedArg = false;

            IToken mark = mark();

			IASTTypeId typeId = typeId(false);
			if (typeId == null) {
				backup(mark);
			} else if (LT(1) != IToken.tCOMMA && LT(1) != IToken.tGT && LT(1) != IToken.tEOC){
				//didn't consume the whole argument, probably confused typeId with idExpression
				//backup and try the assignmentExpression
				backup(mark);
			} else {
				list.add(typeId);
				completedArg = true;
			}
            if (!completedArg) {
                try {
                    IASTExpression expression = assignmentExpression();
                    list.add(expression);
                    completedArg = true;
                } catch (BacktrackException e) {
                    backup(mark);
                }
            }

            if (LT(1) == IToken.tCOMMA) {
                consume();
            } else if (LT(1) != IToken.tGT && LT(1) != IToken.tEOC) {
                failed = true;
                endOffset = LA(1).getEndOffset();
                break;
            }
        }

        templateIdScopes.pop();

        if (failed)
            throwBacktrack(startingOffset, endOffset - startingOffset);

        return list;
    }

    /**
     * Parse a name. name : ("::")? name2 ("::" name2)* name2 : IDENTIFER :
     * template-id
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ITokenDuple name() throws BacktrackException, EndOfFileException {

        TemplateParameterManager argumentList = TemplateParameterManager.getInstance();

        try {
            IToken first = LA(1);
            IToken last = null;
            IToken mark = mark();

            boolean hasTemplateId = false;

            if (LT(1) == IToken.tCOLONCOLON) {
                argumentList.addSegment(null);
                last = consume();
            }

            if (LT(1) == IToken.tBITCOMPLEMENT)
                consume();

            switch (LT(1)) {
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
            case IToken.tEOC:
                last = consume();
                IToken templateLast = consumeTemplateArguments(last, argumentList);
                if (last != templateLast) {
                    last = templateLast;
                    hasTemplateId = true;
                }
                break;

            default:
                IToken l = LA(1);
                backup(mark);
                throwBacktrack(first.getOffset(), l.getEndOffset()
                        - first.getOffset());
            }

            while (LT(1) == IToken.tCOLONCOLON) {
                last = consume();

                if (LT(1) == IToken.t_template)
                    consume();

                if (LT(1) == IToken.tBITCOMPLEMENT)
                    consume();

                switch (LT(1)) {
                case IToken.t_operator:
                    IToken l = LA(1);
                    backup(mark);
                    throwBacktrack(first.getOffset(), l.getEndOffset()
                            - first.getOffset());
                case IToken.tIDENTIFIER:
                case IToken.tCOMPLETION:
                case IToken.tEOC:
                    last = consume();
                    last = consumeTemplateArguments(last, argumentList);
                    if (last.getType() == IToken.tGT || last.getType() == IToken.tEOC)
                        hasTemplateId = true;
                }
            }

            ITokenDuple tokenDuple = TokenFactory.createTokenDuple(first, last,
                    (hasTemplateId ? argumentList.getTemplateArgumentsList()
                            : null));
            return tokenDuple;
        } finally {
            TemplateParameterManager.returnInstance(argumentList);
        }

    }

    /**
     * @param last
     * @param argumentList
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IToken consumeTemplateArguments(IToken last,
            TemplateParameterManager argumentList) throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tLT) {
            IToken secondMark = mark();
            consume();
            try {
                List list = templateArgumentList();
                argumentList.addSegment(list);
                switch (LT(1)) {
                case IToken.tGT:
                case IToken.tEOC:
                    last = consume();
                    if( LT(1) == IToken.tINTEGER || LT(1) == IToken.tFLOATINGPT ) {
                        backup( secondMark );
                        return last;
                    }
                    break;
                default:
                    throw backtrack;
                }
            } catch (BacktrackException bt) {
                argumentList.addSegment(null);
                backup(secondMark);
            }
        } else {
            argumentList.addSegment(null);
        }
        return last;
    }

    protected IASTName operatorId(IToken originalToken,
            TemplateParameterManager templateArgs) throws BacktrackException,
            EndOfFileException {
        // we know this is an operator
        IToken operatorToken = consume();
        IToken toSend = null;
        IASTTypeId typeId = null;
        if (LA(1).isOperator() || LT(1) == IToken.tLPAREN
                || LT(1) == IToken.tLBRACKET) {
            if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete)
                    && LT(2) == IToken.tLBRACKET && LT(3) == IToken.tRBRACKET) {
                consume(); // new or delete
                consume(); // lbracket
                toSend = consume(); // rbracket
                // vector new and delete operators
            } else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN) {
                // operator ()
                consume(); // "("
                toSend = consume(); // ")"
            } else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET) {
                consume(); // "["
                toSend = consume(); // "]"
            } else if (LA(1).isOperator())
                toSend = consume();
            else
                throwBacktrack(operatorToken.getOffset(),
                        toSend != null ? toSend.getEndOffset()
                                - operatorToken.getOffset() : 0);
        } else {
            // must be a conversion function
            IToken t = LA(1);
            typeId = typeId(true);
            if (typeId == null) throw backtrack;
            if (t != LA(1)) {
                while (t.getNext() != LA(1)) {
                    t = t.getNext();
                }
                toSend = t;
            }
        }

        boolean hasTemplateId = (templateArgs != null);
        boolean grabbedNewInstance = false;
        if (templateArgs == null) {
            templateArgs = TemplateParameterManager.getInstance();
            grabbedNewInstance = true;
        }

        try {
            toSend = consumeTemplateArguments(toSend, templateArgs);
            if (toSend.getType() == IToken.tGT) {
                hasTemplateId = true;
            }

            ITokenDuple duple = TokenFactory.createTokenDuple(
                    originalToken == null ? operatorToken : originalToken,
                    toSend, (hasTemplateId ? templateArgs
                            .getTemplateArgumentsList() : null));

            OperatorTokenDuple operator = new OperatorTokenDuple(duple);
            if (typeId != null) { // if it's a conversion operator
                operator.setConversionOperator(true);
                operator.setTypeId(typeId);
            }

            return createName(operator);
        } finally {
            if (grabbedNewInstance)
                TemplateParameterManager.returnInstance(templateArgs);
        }
    }

    /**
     * Parse a Pointer Operator. ptrOperator : "*" (cvQualifier)* | "&" | ::?
     * nestedNameSpecifier "*" (cvQualifier)*
     * 
     * @param owner
     *            Declarator that this pointer operator corresponds to.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void consumePointerOperators(List collection)
            throws EndOfFileException, BacktrackException {

        for (;;) {
        	// __attribute__ in-between pointers
            __attribute_decl_seq(supportAttributeSpecifiers, false);
        	
            if (LT(1) == IToken.tAMPER) {
                int length = LA(1).getEndOffset() - LA(1).getOffset();
                int o = consume().getOffset();
                ICPPASTReferenceOperator refOp = createReferenceOperator();
                ((ASTNode) refOp).setOffsetAndLength(o, length);
                collection.add(refOp);
                return;
            }
            IToken last = null;
            IToken mark = mark();
            ITokenDuple nameDuple = null;
            boolean isConst = false, isVolatile = false, isRestrict = false;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
                try {
                    nameDuple = name();
                    if (nameDuple.length() == 1) {
                        backup(mark);
                        return;
                    }
                    if (nameDuple.getLastToken().getType() != IToken.tCOLONCOLON) {
                        backup(mark);
                        return;
                    }
                    last = nameDuple.getLastToken();
                } catch (BacktrackException bt) {
                    backup(mark);
                    return;
                }
            }
            if (LT(1) == IToken.tSTAR) {
                last = consume();
                int starOffset = last.getOffset();

                for (;;) {
                    IToken t = LA(1);
                    int startingOffset = LA(1).getOffset();
                    switch (LT(1)) {
                    case IToken.t_const:
                        last = consume();
                        isConst = true;
                        break;
                    case IToken.t_volatile:
                        last = consume();
                        isVolatile = true;
                        break;
                    case IToken.t_restrict:
                        if (allowCPPRestrict) {
                            last = consume();
                            isRestrict = true;
                            break;
                        }
                        IToken la = LA(1);
                        throwBacktrack(startingOffset, la.getEndOffset() - startingOffset);

                    }
                    if (t == LA(1))
                        break;
                }

                IASTPointerOperator po = null;
                if (nameDuple != null) {
                    IASTName name = createName(nameDuple);
                    ICPPASTPointerToMember p2m = createPointerToMember(isRestrict);
                    ((ASTNode) p2m).setOffsetAndLength(nameDuple
                            .getFirstToken().getOffset(), last.getEndOffset()
                            - nameDuple.getFirstToken().getOffset());
                    p2m.setConst(isConst);
                    p2m.setVolatile(isVolatile);
                    p2m.setName(name);
                    if (isRestrict) {
                        IGPPASTPointerToMember newPo = (IGPPASTPointerToMember) p2m;
                        newPo.setRestrict(isRestrict);
                        p2m = newPo;
                    }
                    po = p2m;

                } else {
                    po = createPointer(isRestrict);
                    ((ASTNode) po).setOffsetAndLength(starOffset, last.getEndOffset() - starOffset);
                    ((IASTPointer) po).setConst(isConst);
                    ((IASTPointer) po).setVolatile(isVolatile);
                    if (isRestrict) {
                        IGPPASTPointer newPo = (IGPPASTPointer) po;
                        newPo.setRestrict(isRestrict);
                        po = newPo;
                    }
                }
                if (po != null)
                    collection.add(po);

                continue;
            }

            backup(mark);
            return;
        }
    }

    /**
     * @param isRestrict
     * @return
     */
    protected ICPPASTPointerToMember createPointerToMember(boolean gnu) {
        if (gnu)
            return new GPPASTPointerToMember();
        return new CPPASTPointerToMember();
    }

    /**
     * @param isRestrict
     * @return
     */
    protected IASTPointerOperator createPointer(boolean gnu) {
        if (gnu)
            return new GPPASTPointer();
        return new CPPASTPointer();
    }

    /**
     * @return
     */
    protected ICPPASTReferenceOperator createReferenceOperator() {
        return new CPPASTReferenceOperator();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression assignmentExpression() throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.t_throw) {
            return throwExpression();
        }

        if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tLBRACE
                && supportStatementsInExpressions) {
            IASTExpression resultExpression = compoundStatementExpression();
            if (resultExpression != null)
                return resultExpression;
        }

        IASTExpression conditionalExpression = conditionalExpression();
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null
                && conditionalExpression instanceof IASTConditionalExpression) // &&
            return conditionalExpression;

        switch (LT(1)) {
        case IToken.tASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_assign,
                    conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_multiplyAssign,
                    conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_divideAssign, conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_moduloAssign, conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_plusAssign, conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_minusAssign, conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_shiftRightAssign,
                    conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_shiftLeftAssign,
                    conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryAndAssign,
                    conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryXorAssign,
                    conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryOrAssign,
                    conditionalExpression);
        }
        return conditionalExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression throwExpression() throws EndOfFileException,
            BacktrackException {
        IToken throwToken = consume();
        IASTExpression throwExpression = null;
        try {
            throwExpression = expression();
        } catch (BacktrackException bte) {
        }
        int o = throwExpression != null ? calculateEndOffset(throwExpression)
                : throwToken.getEndOffset();
        return buildUnaryExpression(ICPPASTUnaryExpression.op_throw,
                throwExpression, throwToken.getOffset(), o); // fix for 95225
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression relationalExpression() throws BacktrackException, EndOfFileException {

        IASTExpression firstExpression = shiftExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tGT:
                if (templateIdScopes.size() > 0
                        && templateIdScopes.peek() == IToken.tLT) {
                    return firstExpression;
                }
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
                IToken m = mark();
                int t = consume().getType();

                IASTExpression secondExpression = null;
                try {
                    secondExpression = shiftExpression();
                } catch (BacktrackException bte) {
                    backup(m);
                    return firstExpression;
                }
                int expressionKind = 0;
                switch (t) {
                case IToken.tGT:
                    expressionKind = IASTBinaryExpression.op_greaterThan;
                    break;
                case IToken.tLT:
                    expressionKind = IASTBinaryExpression.op_lessThan;
                    break;
                case IToken.tLTEQUAL:
                    expressionKind = IASTBinaryExpression.op_lessEqual;
                    break;
                case IToken.tGTEQUAL:
                    expressionKind = IASTBinaryExpression.op_greaterEqual;
                    break;
                }
                firstExpression = buildBinaryExpression(expressionKind,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                if (supportMinAndMaxOperators
                        && (LT(1) == IGCCToken.tMIN || LT(1) == IGCCToken.tMAX)) {
                    int new_operator = 0;
                    switch (LT(1)) {
                    case IGCCToken.tMAX:
                        consume();
                        new_operator = IGPPASTBinaryExpression.op_max;
                        break;
                    case IGCCToken.tMIN:
                        consume();
                        new_operator = IGPPASTBinaryExpression.op_min;
                    }

                    secondExpression = shiftExpression();

                    firstExpression = buildBinaryExpression(new_operator,
                            firstExpression, secondExpression,
                            calculateEndOffset(secondExpression));
                    break;
                }
                return firstExpression;
            }
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression multiplicativeExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = pmExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
                IASTExpression secondExpression = pmExpression();
                int operator = 0;
                switch (t.getType()) {
                case IToken.tSTAR:
                    operator = IASTBinaryExpression.op_multiply;
                    break;
                case IToken.tDIV:
                    operator = IASTBinaryExpression.op_divide;
                    break;
                case IToken.tMOD:
                    operator = IASTBinaryExpression.op_modulo;
                    break;
                }
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression pmExpression() throws EndOfFileException, BacktrackException {

        IASTExpression firstExpression = castExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tDOTSTAR:
            case IToken.tARROWSTAR:
                IToken t = consume();
                IASTExpression secondExpression = castExpression();
                int operator = 0;
                switch (t.getType()) {
                case IToken.tDOTSTAR:
                    operator = ICPPASTBinaryExpression.op_pmdot;
                    break;
                case IToken.tARROWSTAR:
                    operator = ICPPASTBinaryExpression.op_pmarrow;
                    break;
                }
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * castExpression : unaryExpression | "(" typeId ")" castExpression
     */
    @Override
	protected IASTExpression castExpression() throws EndOfFileException, BacktrackException {
// 		TO DO: we need proper symbol checkint to ensure type name
    	if (LT(1) == IToken.tLPAREN) {
    		IToken la = LA(1);
    		int startingOffset = la.getOffset();
    		IToken mark = mark();
    		consume();
    		
    		if (templateIdScopes.size() > 0)  
    			templateIdScopes.push(IToken.tLPAREN); 
    		
    		boolean popped = false;
    		IASTTypeId typeId = null;
    		IToken startCastExpression=null;
    		
    		// If this isn't a type name, then we shouldn't be here
    		if (!avoidCastExpressionByHeuristics()) {
    			typeId = typeId(false);
    		}
            if (typeId != null && LT(1) == IToken.tRPAREN) {
                consume();
                startCastExpression=mark();
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                    popped = true;
                }
                try {
                	IASTExpression castExpression = castExpression();
                
                	mark = null; // clean up mark so that we can garbage collect
                	return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
                			typeId, castExpression, startingOffset,
                			calculateEndOffset(castExpression));
                } catch (BacktrackException b) {
                    try {
                    	// try a compoundStatementExpression
                		backup(startCastExpression);
                    	if (LT(1) == IToken.tLPAREN) {
                        	IASTExpression castExpression = compoundStatementExpression();
                            mark = null; // clean up mark so that we can garbage collect
                            return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
                                    typeId, castExpression, startingOffset,
                                    calculateEndOffset(castExpression));
                    	}
                    } catch (BacktrackException bte2) {}
                }
            }
        	backup(mark);
        	if (templateIdScopes.size() > 0 && !popped) { templateIdScopes.pop(); }
    	}
    	return unaryExpression();

}

    /**
     * @throws BacktrackException
     */
    @Override
	protected IASTTypeId typeId(boolean forNewExpression) throws EndOfFileException {
    	if (!canBeTypeSpecifier()) {
    		return null;
    	}
        IToken mark = mark();
        int startingOffset = mark.getOffset();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;
        try {
            try {
                declSpecifier = declSpecifierSeq(true, true);
            } 
            catch (FoundDeclaratorException e) {
            	return null;
            }
            if (LT(1) != IToken.tEOC)
                declarator = declarator(SimpleDeclarationStrategy.TRY_FUNCTION,
                        forNewExpression);
        } catch (BacktrackException bt) {
        	return null;
        	/*
            backup(mark);
            if (declarator != null || declSpecifier != null)
            	throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
                    declarator)
                    - startingOffset);
            else
            	throwBacktrack(startingOffset, bt.getLength());
            	*/
        }
        if (declarator != null) {
            if (declarator.getName().toCharArray().length > 0) {
            	return null;
            	/*
                backup(mark);
                throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
                        declarator)
                        - startingOffset);
                        */
            }
            if (declSpecifier instanceof IASTSimpleDeclSpecifier
                    && ((ASTNode) declSpecifier).getLength() == 0) {
            	return null;
            	/*
                backup(mark);
                throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
                        declarator)
                        - startingOffset);
                        */
            }
            if (declarator instanceof IASTArrayDeclarator && forNewExpression) {
            	return null;
            	/*
                backup(mark);
                throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
                        declarator)
                        - startingOffset);
                        */
            }
        }

        IASTTypeId result = createTypeId();
        ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(
                declSpecifier, declarator) - startingOffset);

        result.setDeclSpecifier(declSpecifier);
        result.setAbstractDeclarator(declarator);
        return result;

    }

    protected IASTTypeId createTypeId() {
        return new CPPASTTypeId();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression deleteExpression() throws EndOfFileException,
            BacktrackException {
        int startingOffset = LA(1).getOffset();
        boolean global = false;
        if (LT(1) == IToken.tCOLONCOLON) {
            // global scope
            consume();
            global = true;
        }

        consume(IToken.t_delete);

        boolean vectored = false;
        if (LT(1) == IToken.tLBRACKET) {
            // array delete
            consume();
            consume(IToken.tRBRACKET);
            vectored = true;
        }
        IASTExpression castExpression = castExpression();
        ICPPASTDeleteExpression deleteExpression = createDeleteExpression();
        ((ASTNode) deleteExpression).setOffsetAndLength(startingOffset,
                calculateEndOffset(castExpression) - startingOffset);
        deleteExpression.setIsGlobal(global);
        deleteExpression.setIsVectored(vectored);
        deleteExpression.setOperand(castExpression);
        return deleteExpression;
    }

    /**
     * @return
     */
    protected ICPPASTDeleteExpression createDeleteExpression() {
        return new CPPASTDeleteExpression();
    }

    /**
     * Pazse a new-expression.
     * 
     * @param expression
     * @throws BacktrackException
     *             newexpression: ::? new newplacement? newtypeid
     *             newinitializer? ::? new newplacement? ( typeid )
     *             newinitializer? newplacement: ( expressionlist ) newtypeid:
     *             typespecifierseq newdeclarator? newdeclarator: ptroperator *
     *             newdeclarator? | directnewdeclarator directnewdeclarator: [
     *             expression ] directnewdeclarator [ constantexpression ]
     *             newinitializer: ( expressionlist? )
     */
    protected IASTExpression newExpression() throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int lastOffset = 0;

        boolean isGlobal = false;
        if (LT(1) == IToken.tCOLONCOLON) {
            lastOffset = consume().getEndOffset();
            isGlobal = true;
        }
        lastOffset = consume().getEndOffset();
        boolean typeIdInParen = false;
        boolean placementParseFailure = true;
        IToken beforeSecondParen = null;
        IToken backtrackMarker = null;
        IASTTypeId typeId = null;
        IASTExpression newPlacementExpressions = null;
        IASTExpression newInitializerExpressions = null;
        boolean isNewTypeId = false;

        master_new_loop: for (int i = 0; i < 2; ++i) {
            IToken loopMark = LA(1);
            if (LT(1) == IToken.tLPAREN) {
                lastOffset = consume().getEndOffset();
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLPAREN);
                }
                try {
                    // Try to consume placement list
                    if (i == 0) {
                        backtrackMarker = mark();
                        newPlacementExpressions = expression();
                        lastOffset = consume(IToken.tRPAREN).getEndOffset();
                        if (LT(1) == IToken.tLBRACKET) {
                            backup(backtrackMarker);
                            if (templateIdScopes.size() > 0) {
                                templateIdScopes.pop();
                            } // pop 1st Parent
                            placementParseFailure = true;
                            throwBacktrack(backtrackMarker.getOffset(),
                                    backtrackMarker.getLength());
                        } else
                            placementParseFailure = false;
                    }

                    if (LT(1) == IToken.tLPAREN) {
                        beforeSecondParen = mark();
                        lastOffset = consume().getEndOffset();
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.push(IToken.tLPAREN);
                        } // push 2nd Paren
                        typeIdInParen = true;
                    }
                } catch (BacktrackException e) {
                    backup(backtrackMarker);
                }
                if (placementParseFailure) {
                    // CASE: new (typeid-not-looking-as-placement) ...
                    // the first expression in () is not a placement
                    // - then it has to be typeId
                    typeId = typeId(false);
                    if (typeId == null) throw backtrack; 
                    lastOffset = consume(IToken.tRPAREN).getEndOffset();
                    if (templateIdScopes.size() > 0) {
                        templateIdScopes.pop();
                    } // pop 1st Paren
                    break master_new_loop;
                }
                if (!typeIdInParen) {
                    if (LT(1) == IToken.tLBRACKET) {
                        // CASE: new (typeid-looking-as-placement) [expr]...
                        // the first expression in () has been parsed as a
                        // placement;
                        // however, we assume that it was in fact typeId, and
                        // this
                        // new statement creates an array.
                        // Do nothing, fallback to array/initializer processing
                    } else {
                        // CASE: new (placement) typeid ...
                        // the first expression in () is parsed as a placement,
                        // and the next expression doesn't start with '(' or '['
                        // - then it has to be typeId
                        backtrackMarker = mark();
                        typeId = typeId(true);
                        if (typeId != null) {
                            lastOffset = calculateEndOffset(typeId);
                            break master_new_loop;
                        } else {
                            // Hmmm, so it wasn't typeId after all... Then it is
                            // CASE: new (typeid-looking-as-placement)
                            backup(loopMark);
                            placementParseFailure = true;
                            continue master_new_loop;
                        }
                    }
                } else {
                    // Tricky cases: first expression in () is parsed as a
                    // placement,
                    // and the next expression starts with '('.
                    // The problem is, the first expression might as well be a
                    // typeid
                    typeId = typeId(true);
                    if (typeId == null) {
                        backup(beforeSecondParen);
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.pop();
                        }
                    } else
                    	try {
                        lastOffset = consume(IToken.tRPAREN).getEndOffset();
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.pop();
                        } // popping the 2nd Paren

                        if (LT(1) == IToken.tLPAREN
                                || LT(1) == IToken.tLBRACKET) {
                            // CASE: new (placement)(typeid)(initializer)
                            // CASE: new (placement)(typeid)[] ...
                            // Great, so far all our assumptions have been
                            // correct
                            // Do nothing, fallback to array/initializer
                            // processing
                        } else {
                            // CASE: new (placement)(typeid)
                            // CASE: new
                            // (typeid-looking-as-placement)(initializer-looking-as-typeid)
                            // Worst-case scenario - this cannot be resolved w/o
                            // more semantic information.
                            // Luckily, we don't need to know what was that - we
                            // only know that
                            // new-expression ends here.
                            ICPPASTNewExpression result = createNewExpression();
                            ((ASTNode) result)
                                    .setOffsetAndLength(startingOffset,
                                            lastOffset - startingOffset);
                            result.setIsGlobal(isGlobal);
                            result.setIsNewTypeId(isNewTypeId);
                            result.setTypeId(typeId);
                            if (newPlacementExpressions != null) {
                                result.setNewPlacement(newPlacementExpressions);
                            }
                            return result;
                        }
                        break master_new_loop;
                    } catch (BacktrackException e) {
                        // CASE: new
                        // (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
                        // Fallback to initializer processing
                        backup(beforeSecondParen);
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.pop();
                        }// pop that 2nd paren
                    }
                }
            } else {
                // CASE: new typeid ...
                // new parameters do not start with '('
                // i.e it has to be a plain typeId
                typeId = typeId(true);
                if (typeId == null) throw backtrack; 
                lastOffset = calculateEndOffset(typeId);
                isNewTypeId = true;
                break master_new_loop;
            }
        }
        ICPPASTNewExpression result = createNewExpression();
        ((ASTNode) result).setOffsetAndLength(startingOffset, lastOffset
                - startingOffset);
        result.setIsGlobal(isGlobal);
        if (typeId != null) {
            result.setIsNewTypeId(isNewTypeId);
            result.setTypeId(typeId);
        }
        if (newPlacementExpressions != null) {
            result.setNewPlacement(newPlacementExpressions);
        }

        while (LT(1) == IToken.tLBRACKET) {
            // array new
            lastOffset = consume().getEndOffset();

            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLBRACKET);
            }

            IASTExpression a = assignmentExpression();
            lastOffset = consume(IToken.tRBRACKET).getEndOffset();
            result.addNewTypeIdArrayExpression(a);

            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
        }
        // newinitializer
        if (LT(1) == IToken.tLPAREN) {
            lastOffset = consume().getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }

            // we want to know the difference between no newInitializer and an
            // empty new Initializer
            // if the next token is the RPAREN, then we have an Empty expression
            // in our list.
            if (LT(1) != IToken.tRPAREN)
                newInitializerExpressions = expression();

            if (LT(1) == IToken.tEOC) {
            	lastOffset = consume().getEndOffset();
            } else {
            	lastOffset = consume(IToken.tRPAREN).getEndOffset();
            }
            
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
            if (newInitializerExpressions != null) {
                result.setNewInitializer(newInitializerExpressions);
            }

        }
        ((ASTNode) result).setLength(lastOffset - startingOffset);
        return result;
    }

    /**
     * @return
     */
    protected ICPPASTNewExpression createNewExpression() {
        return new CPPASTNewExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression unaryExpression() throws EndOfFileException,
            BacktrackException {
        switch (LT(1)) {
        case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star);// IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
        case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);// IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
        case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus);// IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
        case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus);// IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
        case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not);// IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
        case IToken.tBITCOMPLEMENT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);// IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
        case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);// IASTExpression.Kind.UNARY_INCREMENT);
        case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);// IASTExpression.Kind.UNARY_DECREMENT);
        case IToken.t_sizeof:
            return parseSizeofExpression();
        case IToken.t_new:
            return newExpression();
        case IToken.t_delete:
            return deleteExpression();
        case IToken.tCOLONCOLON:
            switch (LT(2)) {
            case IToken.t_new:
                return newExpression();
            case IToken.t_delete:
                return deleteExpression();
            default:
                return postfixExpression();
            }
        default:
            if (LT(1) == IGCCToken.t_typeof && supportTypeOfUnaries) {
                IASTExpression unary = unaryTypeofExpression();
                if (unary != null)
                    return unary;
            }
            if (LT(1) == IGCCToken.t___alignof__ && supportAlignOfUnaries) {
                IASTExpression align = unaryAlignofExpression();
                if (align != null)
                    return align;
            }
            return postfixExpression();
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression postfixExpression() throws EndOfFileException,
            BacktrackException {
        IASTExpression firstExpression = null;
        boolean isTemplate = false;

        switch (LT(1)) {
        case IToken.t_typename:
            int o = consume().getOffset();

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
                consume();
                templateTokenConsumed = true;
            }
            ITokenDuple nestedName = name();
            IASTName name = createName(nestedName);

            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            IASTExpression expressionList = expression();
            int lastOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }

            ICPPASTTypenameExpression result = createTypenameExpression();
            ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
            result.setIsTemplate(templateTokenConsumed);
            result.setName(name);
            result.setInitialValue(expressionList);
            firstExpression = result;
            break;
        // simple-type-specifier ( assignment-expression , .. )
        case IToken.t_char:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_char);
            break;
        case IToken.t_wchar_t:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_wchar_t);
            break;
        case IToken.t_bool:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_bool);
            break;
        case IToken.t_short:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_short);
            break;
        case IToken.t_int:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_int);
            break;
        case IToken.t_long:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_long);
            break;
        case IToken.t_signed:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_signed);
            break;
        case IToken.t_unsigned:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_unsigned);
            break;
        case IToken.t_float:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_float);
            break;
        case IToken.t_double:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_double);
            break;
        case IToken.t_dynamic_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_dynamic_cast);
            break;
        case IToken.t_static_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_static_cast);
            break;
        case IToken.t_reinterpret_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_reinterpret_cast);
            break;
        case IToken.t_const_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_const_cast);
            break;
        case IToken.t_typeid:
            int so = consume().getOffset();
            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            IASTNode[] n = parseTypeIdOrUnaryExpression(false);
            lastOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }

            switch (n.length) {
            case 0:
                throwBacktrack(LA(1));
                break;
            case 1:
                if (n[0] instanceof IASTTypeId) {
                    firstExpression = buildTypeIdExpression(
                            ICPPASTTypeIdExpression.op_typeid,
                            (IASTTypeId) n[0], so, lastOffset);
                } else if (n[0] instanceof IASTExpression) {
                    firstExpression = buildUnaryExpression(
                            ICPPASTUnaryExpression.op_typeid,
                            (IASTExpression) n[0], so, lastOffset);
                }
                break;
            case 2:
                IASTAmbiguousExpression ambExpr = createAmbiguousExpression();
                IASTExpression e1 = buildTypeIdExpression(
                        ICPPASTTypeIdExpression.op_typeid, (IASTTypeId) n[0],
                        so, lastOffset);
                IASTExpression e2 = buildUnaryExpression(
                        ICPPASTUnaryExpression.op_typeid,
                        (IASTExpression) n[1], so, lastOffset);
                ambExpr.addExpression(e1);
                ambExpr.addExpression(e2);
                ((ASTNode) ambExpr).setOffsetAndLength((ASTNode) e2);
                firstExpression = ambExpr;
                break;
            }

            break;
        default:
            firstExpression = primaryExpression();
        }
        IASTExpression secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume();
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLBRACKET);
                }
                secondExpression = expression();
                int lastOffset;
                switch (LT(1)) {
                case IToken.tRBRACKET:
                case IToken.tEOC:
                    lastOffset = consume().getEndOffset();
                    break;
                default:
                    throw backtrack;
                }
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                }

                IASTArraySubscriptExpression s = createArraySubscriptExpression();
                ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
                s.setArrayExpression(firstExpression);
                s.setSubscriptExpression(secondExpression);
                firstExpression = s;
                break;
            case IToken.tLPAREN:
                // function call
                consume();

                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLPAREN);
                }
                if (LT(1) != IToken.tRPAREN)
                    secondExpression = expression();
                else
                    secondExpression = null;
                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                    lastOffset = consume().getEndOffset();
                    break;
                default:
                    throw backtrack;
                }

                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                }

                IASTFunctionCallExpression fce = createFunctionCallExpression();
                ((ASTNode) fce).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
                fce.setFunctionNameExpression(firstExpression);
                if (secondExpression != null) {
                    fce.setParameterExpression(secondExpression);
                }
                firstExpression = fce;
                break;
            case IToken.tINCR:
                int offset = consume().getEndOffset();
                firstExpression = buildUnaryExpression(
                        IASTUnaryExpression.op_postFixIncr, firstExpression,
                        ((ASTNode) firstExpression).getOffset(), offset);
                break;
            case IToken.tDECR:
                offset = consume().getEndOffset();
                firstExpression = buildUnaryExpression(
                        IASTUnaryExpression.op_postFixDecr, firstExpression,
                        ((ASTNode) firstExpression).getOffset(), offset);
                break;
            case IToken.tDOT:
                // member access
                IToken dot = consume();
                if (LT(1) == IToken.t_template) {
                    consume();
                    isTemplate = true;
                }

                IASTName name = idExpression();
                
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + dot.getLength());

                ICPPASTFieldReference fieldReference = createFieldReference();
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                fieldReference.setIsTemplate(isTemplate);
                fieldReference.setIsPointerDereference(false);
                fieldReference.setFieldName(name);
                fieldReference.setFieldOwner(firstExpression);
                firstExpression = fieldReference;
                break;
            case IToken.tARROW:
                // member access
                IToken arrow = consume();

                if (LT(1) == IToken.t_template) {
                    consume();
                    isTemplate = true;
                }

                name = idExpression();
                
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + arrow.getLength());
                
                fieldReference = createFieldReference();
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                fieldReference.setIsTemplate(isTemplate);
                fieldReference.setIsPointerDereference(true);
                fieldReference.setFieldName(name);
                fieldReference.setFieldOwner(firstExpression);
                firstExpression = fieldReference;
                break;
            default:
                return firstExpression;
            }
        }
    }

    @Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
        return new CPPASTAmbiguousExpression();
    }

    protected IASTArraySubscriptExpression createArraySubscriptExpression() {
        return new CPPASTArraySubscriptExpression();
    }

    protected ICPPASTTypenameExpression createTypenameExpression() {
        return new CPPASTTypenameExpression();
    }

    protected IASTFunctionCallExpression createFunctionCallExpression() {
        return new CPPASTFunctionCallExpression();
    }

    protected ICPPASTFieldReference createFieldReference() {
        return new CPPASTFieldReference();
    }

    protected IASTExpression simpleTypeConstructorExpression(int operator)
            throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        consume();
        consume(IToken.tLPAREN);
        IASTExpression operand = null;
        if (LT(1) != IToken.tRPAREN)
            operand = expression();
        int l = consume(IToken.tRPAREN).getEndOffset();
        ICPPASTSimpleTypeConstructorExpression result = createSimpleTypeConstructorExpression();
        ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
        result.setSimpleType(operator);
        if (operand != null) {
            result.setInitialValue(operand);
        }
        return result;
    }

    /**
     * @return
     */
    protected ICPPASTSimpleTypeConstructorExpression createSimpleTypeConstructorExpression() {
        return new CPPASTSimpleTypeConstructorExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression primaryExpression() throws EndOfFileException,
            BacktrackException {
        IToken t = null;
        ICPPASTLiteralExpression literalExpression = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_integer_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset()- t.getOffset());
            return literalExpression;
        case IToken.tFLOATINGPT:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_float_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tSTRING:
        case IToken.tLSTRING:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_string_literal);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tCHAR:
        case IToken.tLCHAR:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_char_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_false:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_false);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_true:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_true);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;

        case IToken.t_this:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_this);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tLPAREN:
            t = consume();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            IASTExpression lhs = expression();
            int finalOffset= 0;
            if (LT(1) == IToken.tRPAREN) {
                finalOffset = consume().getEndOffset();
            } else {
            	// missing parenthesis, assume it's there and keep going.
            	finalOffset = LA(1).getOffset();
            }
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
            return buildUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, lhs, t.getOffset(), finalOffset);
        case IToken.tIDENTIFIER:
        case IToken.tCOLONCOLON:
        case IToken.t_operator:
        case IToken.tCOMPLETION:
        case IToken.tBITCOMPLEMENT: {
            IASTName name = idExpression();
            IASTIdExpression idExpression = createIdExpression();
            ((ASTNode) idExpression).setOffsetAndLength(((ASTNode) name)
                    .getOffset(), ((ASTNode) name).getOffset()
                    + ((ASTNode) name).getLength()
                    - ((ASTNode) name).getOffset());
            idExpression.setName(name);
            return idExpression;
        }
        default:
            IToken la = LA(1);
            int startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
        }

    }

    /**
     * @return
     */
    protected ICPPASTLiteralExpression createLiteralExpression() {
        return new CPPASTLiteralExpression();
    }

    /**
     * @return
     */
    @Override
	protected IASTIdExpression createIdExpression() {
        return new CPPASTIdExpression();
    }

    protected IASTName idExpression() throws EndOfFileException,
            BacktrackException {
        IASTName name = null;
        try {
            name = createName(name());
        } catch (BacktrackException bt) {
            IToken mark = mark();
            if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                IToken start = consume();
                IToken end = null;
                if (start.getType() == IToken.tIDENTIFIER)
                    end = consumeTemplateParameters(null);
                while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                    end = consume();
                    if (end.getType() == IToken.tIDENTIFIER)
                        end = consumeTemplateParameters(end);
                }

                if (LT(1) == IToken.t_operator)
                    name = operatorId(start, null);
                else {
                    backup(mark);
                    throwBacktrack(start.getOffset(), (end != null ? end.getEndOffset() : start.getEndOffset())
                            - start.getOffset());
                }
            } else if (LT(1) == IToken.t_operator)
                name = operatorId(null, null);
        }
        return name;

    }

    protected IASTExpression specialCastExpression(int kind)
            throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        IToken op = consume();
        consume(IToken.tLT);
        IASTTypeId typeID = typeId(false);
        if (typeID == null) { throw backtrack; } 
        consume(IToken.tGT);
        consume(IToken.tLPAREN);
        IASTExpression lhs = expression();
        int l = consume(IToken.tRPAREN).getEndOffset();
        IASTCastExpression result = createCastExpression();
        ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
        result.setTypeId(typeID);
        result.setOperand(lhs);

        if (op.toString().equals(DYNAMIC_CAST)) {
            result.setOperator(ICPPASTCastExpression.op_dynamic_cast);
        } else if (op.toString().equals(STATIC_CAST)) {
            result.setOperator(ICPPASTCastExpression.op_static_cast);
        } else if (op.toString().equals(REINTERPRET_CAST)) {
            result.setOperator(ICPPASTCastExpression.op_reinterpret_cast);
        } else if (op.toString().equals(CONST_CAST)) {
            result.setOperator(ICPPASTCastExpression.op_const_cast);
        } else {
            result.setOperator(IASTCastExpression.op_cast);
        }

        return result;
    }

    private final boolean allowCPPRestrict;

    private final boolean supportExtendedTemplateSyntax;

    private final boolean supportMinAndMaxOperators;

    private final boolean supportComplex;

    private final boolean supportRestrict;

    private final boolean supportLongLong;

	private final IIndex index;

    private static final int DEFAULT_PARM_LIST_SIZE = 4;

    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;

    private static final int DEFAULT_SIZE_EXCEPTIONS_LIST = 2;

    private static final int DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE = 4;

    /**
     * This is the standard constructor that we expect the Parser to be
     * instantiated with.
     * 
     * @param mode
     *            TODO
     */
    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config) {
    	this(scanner, mode, log, config, null);
    }

    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config,
            IIndex index) {
        super(scanner, log, mode, 
        		config.supportStatementsInExpressions(),
                config.supportTypeofUnaryExpressions(), 
                config.supportAlignOfUnaryExpression(), 
                config.supportKnRC(),
                config.supportAttributeSpecifiers(), 
                config.supportDeclspecSpecifiers(),
                config.getBuiltinBindingsProvider());
        allowCPPRestrict = config.allowRestrictPointerOperators();
        supportExtendedTemplateSyntax = config.supportExtendedTemplateSyntax();
        supportMinAndMaxOperators = config.supportMinAndMaxOperators();
        supportRestrict = config.supportRestrictKeyword();
        supportComplex = config.supportComplexNumbers();
        supportLongLong = config.supportLongLongs();
        this.index= index;
    }

    /**
     * The merger of using-declaration and using-directive in ANSI C++ grammar.
     * using-declaration: using typename? ::? nested-name-specifier
     * unqualified-id ; using :: unqualified-id ; using-directive: using
     * namespace ::? nested-name-specifier? namespace-name ;
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected IASTDeclaration usingClause() throws EndOfFileException,
            BacktrackException {
        IToken firstToken = consume();

        if (LT(1) == IToken.t_namespace) {
            // using-directive
            int endOffset = consume().getEndOffset();
            IASTName name = null;
            switch (LT(1)) {
            case IToken.tIDENTIFIER:
            case IToken.tCOLONCOLON:
            case IToken.tCOMPLETION:
                name = createName(name());
                break;
            default:
                throwBacktrack(firstToken.getOffset(), endOffset - firstToken.getOffset());
            }

            switch (LT(1)) {
            case IToken.tSEMI:
            case IToken.tEOC:
                endOffset = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }

            ICPPASTUsingDirective astUD = createUsingDirective();
            ((ASTNode) astUD).setOffsetAndLength(firstToken.getOffset(), endOffset - firstToken.getOffset());
            astUD.setQualifiedName(name);
            return astUD;
        }

        boolean typeName = false;

        if (LT(1) == IToken.t_typename) {
            typeName = true;
            consume();
        }

        IASTName name = idExpression();
        int end;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            end = consume().getEndOffset();
            break;
        default:
            throw backtrack;
        }

        ICPPASTUsingDeclaration result = createUsingDeclaration();
        ((ASTNode) result).setOffsetAndLength(firstToken.getOffset(), end - firstToken.getOffset());
        result.setIsTypename(typeName);
        result.setName(name);
        return result;
    }


    protected ICPPASTUsingDeclaration createUsingDeclaration() {
        return new CPPASTUsingDeclaration();
    }


    protected ICPPASTUsingDirective createUsingDirective() {
        return new CPPASTUsingDirective();
    }

    /**
     * Implements Linkage specification in the ANSI C++ grammar.
     * linkageSpecification : extern "string literal" declaration | extern
     * "string literal" { declaration-seq }
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected ICPPASTLinkageSpecification linkageSpecification()
            throws EndOfFileException, BacktrackException {
        IToken firstToken = consume(); // t_extern
        IToken spec = consume(); // tString
        ICPPASTLinkageSpecification linkage = createLinkageSpecification();
        ((ASTNode) linkage).setOffset(firstToken.getOffset());
        linkage.setLiteral(spec.getImage());

        if (LT(1) == IToken.tLBRACE) {
            consume();

            linkageDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
                int checkToken = LA(1).hashCode();
                switch (LT(1)) {
                case IToken.tRBRACE:
                    break linkageDeclarationLoop;
                default:
                    try {
                        IASTDeclaration d = declaration();
                        linkage.addDeclaration(d);
                    } catch (BacktrackException bt) {
                        IASTProblem p = failParse(bt);
                        IASTProblemDeclaration pd = createProblemDeclaration();
                        pd.setProblem(p);
                        ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                        linkage.addDeclaration(pd);
                        errorHandling();
                        if (checkToken == LA(1).hashCode())
                            errorHandling();
                    }
                }
                if (checkToken == LA(1).hashCode())
                    failParseWithErrorHandling();
            }
            // consume the }
            int endOffset = consume(IToken.tRBRACE).getEndOffset();
            ((ASTNode) linkage).setLength(endOffset - firstToken.getOffset());
            return linkage;
        }
        // single declaration

        IASTDeclaration d = declaration();
        linkage.addDeclaration(d);
        ((ASTNode) linkage).setLength(calculateEndOffset(d) - firstToken.getOffset());
        return linkage;
    }

    /**
     * @return
     */
    protected ICPPASTLinkageSpecification createLinkageSpecification() {
        return new CPPASTLinkageSpecification();
    }

    /**
     * Represents the amalgamation of template declarations, template
     * instantiations and specializations in the ANSI C++ grammar.
     * template-declaration: export? template < template-parameter-list >
     * declaration explicit-instantiation: template declaration
     * explicit-specialization: template <>declaration
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected IASTDeclaration templateDeclaration() throws EndOfFileException,
            BacktrackException {
        IToken mark = mark();
        IToken firstToken = null;
        boolean exported = false;
        boolean encounteredExtraMod = false;
        ++templateCount;
        if (LT(1) == IToken.t_export) {
            exported = true;
            firstToken = consume();
            consume(IToken.t_template);
        } else {
            if (supportExtendedTemplateSyntax) {
                switch (LT(1)) {
                case IToken.t_static:
                case IToken.t_extern:
                case IToken.t_inline:
                    firstToken = consume();
                    consume(IToken.t_template);
                    encounteredExtraMod = true;
                    break;
                default:
                    firstToken = consume(IToken.t_template);
                    break;
                }
            } else
                firstToken = consume(IToken.t_template);
        }
        if (LT(1) != IToken.tLT) {
            // explicit-instantiation
            ICPPASTExplicitTemplateInstantiation templateInstantiation = null;
            if (encounteredExtraMod && supportExtendedTemplateSyntax) {
                IGPPASTExplicitTemplateInstantiation temp = createGnuTemplateInstantiation();
                switch (firstToken.getType()) {
                case IToken.t_static:
                    temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_static);
                    break;
                case IToken.t_extern:
                    temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_extern);
                    break;
                case IToken.t_inline:
                    temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_inline);
                    break;
                }
                templateInstantiation = temp;
            } else
                templateInstantiation = createTemplateInstantiation();
            try {
                IASTDeclaration d = declaration();
                ((ASTNode) templateInstantiation).setOffsetAndLength(firstToken
                        .getOffset(), calculateEndOffset(d) - firstToken.getOffset());
                templateInstantiation.setDeclaration(d);
            } finally {
                --templateCount;
            }
            return templateInstantiation;
        }
        consume(); // check for LT made before
        if (LT(1) == IToken.tGT) {
            // explicit-specialization
            consume();

            ICPPASTTemplateSpecialization templateSpecialization = createTemplateSpecialization();
            try {
                IASTDeclaration d = declaration();
                ((ASTNode) templateSpecialization).setOffsetAndLength(
                        firstToken.getOffset(), calculateEndOffset(d) - firstToken.getOffset());
                templateSpecialization.setDeclaration(d);
            } finally {
                --templateCount;
            }

            return templateSpecialization;
        }

        try {
            List parms = templateParameterList();
            consume(IToken.tGT);
            ICPPASTTemplateDeclaration templateDecl = createTemplateDeclaration();
            try
            {
                IASTDeclaration d = declaration();
                ((ASTNode) templateDecl).setOffsetAndLength(firstToken.getOffset(),
                        calculateEndOffset(d) - firstToken.getOffset());
                templateDecl.setExported(exported);
                templateDecl.setDeclaration(d);
                for (int i = 0; i < parms.size(); ++i) {
                    ICPPASTTemplateParameter parm = (ICPPASTTemplateParameter) parms.get(i);
                    templateDecl.addTemplateParamter(parm);
                }
            }
            finally
            {
                --templateCount;
            }

            return templateDecl;
        } catch (BacktrackException bt) {
            backup(mark);
            throw bt;
        }
    }


    protected ICPPASTTemplateDeclaration createTemplateDeclaration() {
        return new CPPASTTemplateDeclaration();
    }


    protected ICPPASTTemplateSpecialization createTemplateSpecialization() {
        return new CPPASTTemplateSpecialization();
    }


    protected IGPPASTExplicitTemplateInstantiation createGnuTemplateInstantiation() {
        return new GPPASTExplicitTemplateInstantiation();
    }


    protected ICPPASTExplicitTemplateInstantiation createTemplateInstantiation() {
        return new CPPASTExplicitTemplateInstantiation();
    }

    /**
     * template-parameter-list: template-parameter template-parameter-list ,
     * template-parameter template-parameter: type-parameter
     * parameter-declaration type-parameter: class identifier? class identifier? =
     * type-id typename identifier? typename identifier? = type-id template <
     * template-parameter-list > class identifier? template <
     * template-parameter-list > class identifier? = id-expression template-id:
     * template-name < template-argument-list?> template-name: identifier
     * template-argument-list: template-argument template-argument-list ,
     * template-argument template-argument: assignment-expression type-id
     * id-expression
     * 
     * @param templateDeclaration
     *            Callback's templateDeclaration which serves as a scope to this
     *            list.
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected List templateParameterList() throws BacktrackException,
            EndOfFileException {
        // if we have gotten this far then we have a true template-declaration
        // iterate through the template parameter list
        List returnValue = new ArrayList(DEFAULT_PARM_LIST_SIZE);

        for (;;) {
            if (LT(1) == IToken.tGT)
                return returnValue;
            if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename) {
                IToken startingToken = LA(1);
                int lastOffset = 0;
                int type = (LT(1) == IToken.t_class ? ICPPASTSimpleTypeTemplateParameter.st_class
                        : ICPPASTSimpleTypeTemplateParameter.st_typename);
                lastOffset = consume().getEndOffset();
                IASTName identifierName = null;
                IASTTypeId typeId = null;

                if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
                    identifierName = createName(identifier());
                    lastOffset = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        typeId = typeId(false); // type-id
                        if (typeId == null) 
                        	throw backtrack;
                        lastOffset = calculateEndOffset(typeId);
                    }
                } else {
                    identifierName = createName();
                }

                ICPPASTSimpleTypeTemplateParameter parm = createSimpleTemplateParameter();
                ((ASTNode) parm).setOffsetAndLength(startingToken.getOffset(),
                        lastOffset - startingToken.getOffset());
                parm.setParameterType(type);
                parm.setName(identifierName);
                if (typeId != null) {
                    parm.setDefaultType(typeId);
                }
                returnValue.add(parm);

            } else if (LT(1) == IToken.t_template) {
                IToken firstToken = consume();
                consume(IToken.tLT);

                List subResult = templateParameterList();
                consume(IToken.tGT);
                int last = consume(IToken.t_class).getEndOffset();
                IASTName identifierName = null;
                IASTExpression optionalExpression = null;

                if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
                    identifierName = createName(identifier());
                    last = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        optionalExpression = primaryExpression();
                        last = calculateEndOffset(optionalExpression);
                    }
                } else
                    identifierName = createName();

                ICPPASTTemplatedTypeTemplateParameter parm = createTemplatedTemplateParameter();
                ((ASTNode) parm).setOffsetAndLength(firstToken.getOffset(), last - firstToken.getOffset());
                parm.setName(identifierName);
                if (optionalExpression != null) {
                    parm.setDefaultValue(optionalExpression);
                }

                for (int i = 0; i < subResult.size(); ++i) {
                    ICPPASTTemplateParameter p = (ICPPASTTemplateParameter) subResult.get(i);
                    parm.addTemplateParamter(p);
                }
                returnValue.add(parm);

            } else if (LT(1) == IToken.tCOMMA) {
                consume();
                continue;
            } else {
                ICPPASTParameterDeclaration parm = parameterDeclaration();
                returnValue.add(parm);
            }
        }
    }


    protected ICPPASTTemplatedTypeTemplateParameter createTemplatedTemplateParameter() {
        return new CPPASTTemplatedTypeTemplateParameter();
    }

    protected ICPPASTSimpleTypeTemplateParameter createSimpleTemplateParameter() {
        return new CPPASTSimpleTypeTemplateParameter();
    }

    /**
     * The most abstract construct within a translationUnit : a declaration.
     * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
     * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
     * {"extern"} linkageSpecification | simpleDeclaration Notes: - folded in
     * blockDeclaration - merged alternatives that required same LA -
     * functionDefinition into simpleDeclaration - namespaceAliasDefinition into
     * namespaceDefinition - usingDirective into usingDeclaration -
     * explicitInstantiation and explicitSpecialization into templateDeclaration
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * @throws BacktrackException
     *             request a backtrack
     */
    @Override
	protected IASTDeclaration declaration() throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            return asmDeclaration();
        case IToken.t_namespace:
            return namespaceDefinitionOrAlias();
        case IToken.t_using:
            return usingClause();
        case IToken.t_export:
        case IToken.t_template:
            return templateDeclaration();
        case IToken.t_extern:
            if (LT(2) == IToken.tSTRING)
                return linkageSpecification();
        default:
            if (supportExtendedTemplateSyntax
                    && (LT(1) == IToken.t_static || LT(1) == IToken.t_inline || LT(1) == IToken.t_extern)
                    && LT(2) == IToken.t_template)
                return templateDeclaration();
            return simpleDeclarationStrategyUnion();
        }
    }
    
    protected IASTDeclaration simpleDeclarationStrategyUnion()
            throws EndOfFileException, BacktrackException {
        IToken simpleDeclarationMark = mark();
        IASTDeclaration d1 = null, d2 = null;
        IToken after = null;
        try {
            d1 = simpleDeclaration(SimpleDeclarationStrategy.TRY_FUNCTION, false);
            try {
                after = LA(1);
            } catch (EndOfFileException eof) {
                after = null;
            }
        } catch (BacktrackException bt) {
            d1 = null;
        }
        if (d1 != null) {
            if( templateCount != 0 )
                return d1;
            if( functionBodyCount == 0 )
                return d1;
            if (d1 instanceof IASTFunctionDefinition)
                return d1;
            if (d1 instanceof IASTSimpleDeclaration) {
                IASTSimpleDeclaration sd = (IASTSimpleDeclaration) d1;
                if( sd.getDeclSpecifier() instanceof ICPPASTDeclSpecifier &&
                        ((ICPPASTDeclSpecifier)sd.getDeclSpecifier()).isFriend() )
                    return d1;
                if (sd.getDeclarators().length != 1)
                    return d1;
                if (sd.getDeclSpecifier() instanceof IASTSimpleDeclSpecifier) {
                    IASTSimpleDeclSpecifier simpleSpec = (IASTSimpleDeclSpecifier) sd.getDeclSpecifier();
                    if( simpleSpec.getType() == IASTSimpleDeclSpecifier.t_void && sd.getDeclarators()[0].getPointerOperators().length == 0 )
                        return d1;
                }
                
                if (sd.getDeclarators()[0] instanceof IASTStandardFunctionDeclarator) {
                    if (sd.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef)
                        return d1;
                } else
                    return d1;
            }
        }

        // did not work
        backup(simpleDeclarationMark);

        try {
            d2 = simpleDeclaration(SimpleDeclarationStrategy.TRY_VARIABLE, false);
            if (after != null && after != LA(1)) {
                backup(after);
                return d1;
            }
        } catch (BacktrackException be) {
            d2 = null;
            if (d1 == null)
                throwBacktrack(be);
        }

        if (d2 == null && d1 != null) {
            backup(after);
            return d1;
        }

        if (d1 == null && d2 != null)
            return d2;

        IASTAmbiguousDeclaration result = createAmbiguousDeclaration();
        ((ASTNode) result).setOffsetAndLength((ASTNode) d1);
        result.addDeclaration(d1);
        result.addDeclaration(d2);
        return result;

    }

    protected IASTAmbiguousDeclaration createAmbiguousDeclaration() {
        return new CPPASTAmbiguousDeclaration();
    }

    /**
     * Serves as the namespace declaration portion of the ANSI C++ grammar.
     * namespace-definition: namespace identifier { namespace-body } | namespace {
     * namespace-body } namespace-body: declaration-seq?
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclaration namespaceDefinitionOrAlias()
            throws BacktrackException, EndOfFileException {

        IToken first = consume();
        int last = first.getEndOffset();
        IASTName name = null;
        // optional name
        if (LT(1) == IToken.tIDENTIFIER) {
            name = createName(identifier());
            last = calculateEndOffset(name);
        } else
            name = createName();

        // bug 195701, gcc 4.2 allows visibility attribute for namespaces.
        __attribute_decl_seq(true, false);
        
        if (LT(1) == IToken.tLBRACE) {
            consume();
            ICPPASTNamespaceDefinition namespaceDefinition = createNamespaceDefinition();
            ((ASTNode) namespaceDefinition).setOffset(first.getOffset());
            namespaceDefinition.setName(name);

            namespaceDeclarationLoop: while (true) {
                switch (LT(1)) {
                case IToken.tRBRACE:
                case IToken.tEOC:
                    break namespaceDeclarationLoop;
                default:
                    int checkToken = LA(1).hashCode();
                    try {
                        IASTDeclaration d = declaration();
                        namespaceDefinition.addDeclaration(d);
                    } catch (BacktrackException bt) {
                        IASTProblem p = failParse(bt);
                        IASTProblemDeclaration pd = createProblemDeclaration();
                        pd.setProblem(p);
                        ((ASTNode) pd).setOffsetAndLength((ASTNode) p);
                        namespaceDefinition.addDeclaration(pd);
                        errorHandling();
                        if (checkToken == LA(1).hashCode())
                            errorHandling();
                    }
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
            }

            // consume the }
            int end = consume().getEndOffset();
            ((ASTNode) namespaceDefinition).setLength(end - first.getOffset());
            return namespaceDefinition;
        } else if (LT(1) == IToken.tASSIGN) {
            IToken assign = consume();

            if (name.toString() == null) {
                throwBacktrack(first.getOffset(), assign.getEndOffset() - first.getOffset());
                return null;
            }

            ITokenDuple duple = name();
            IASTName qualifiedName = createName(duple);
            int end = consume(IToken.tSEMI).getEndOffset();

            ICPPASTNamespaceAlias alias = createNamespaceAlias();
            ((ASTNode) alias).setOffsetAndLength(first.getOffset(), end - first.getOffset());
            alias.setAlias(name);
            alias.setMappingName(qualifiedName);
            return alias;
        } else {
            throwBacktrack(first.getOffset(), last - first.getOffset());
            return null;
        }
    }


    protected ICPPASTNamespaceAlias createNamespaceAlias() {
        return new CPPASTNamespaceAlias();
    }


    protected ICPPASTQualifiedName createQualifiedName(ITokenDuple duple) {
        CPPASTQualifiedName result = new CPPASTQualifiedName();
        result.setOffsetAndLength(duple.getStartOffset(), duple.getEndOffset() - duple.getStartOffset());
        result.setSignature(duple.toString());
        ITokenDuple[] segments = duple.getSegments();
        int startingValue = 0;
        if (segments.length > 0) {
        	final ITokenDuple firstSeg= segments[0];
        	final IToken firstToken= firstSeg.getFirstToken();
        	if (firstToken.getType() == IToken.tCOLONCOLON) {
        		if (firstToken == firstSeg.getLastToken() && firstSeg.getTemplateIdArgLists() == null) {
        			++startingValue;
        			result.setFullyQualified(true);
        		}
        	}
        }
        for (int i = startingValue; i < segments.length; ++i) {
            IASTName subName = null;
            // take each name and add it to the result
            final ITokenDuple seg= segments[i];
            if (seg.getTemplateIdArgLists() == null) {
            	final IToken firstToken= seg.getFirstToken();
            	if (firstToken == seg.getLastToken()) {
            		subName= createName(firstToken);
            	}
            	else {
            		subName = createName(seg);
            	}
            }
            else {
                // templateID
                subName = createTemplateID(segments[i]);
            }

            if (i == segments.length - 1 && duple instanceof OperatorTokenDuple) { 
            	// make sure the last segment is an OperatorName/ConversionName
                subName = createOperatorName((OperatorTokenDuple) duple, subName);
            }

            // bug 189299, 193152 indicate that there have been nested qualified names
            // as a work around just flatten them.
            if (subName instanceof ICPPASTQualifiedName) {
            	IASTName[] subNames= ((ICPPASTQualifiedName) subName).getNames();
            	for (int j = 0; j < subNames.length; j++) {
					subName = subNames[j];
	            	result.addName(subName);
				}            
            }
            else {
            	((ASTNode) subName).setOffsetAndLength(segments[i].getStartOffset(), 
            			segments[i].getEndOffset() - segments[i].getStartOffset());
            	result.addName(subName);
            }
        }
        return result;
    }

    /**
     * @param duple
     * @return
     */
    protected ICPPASTTemplateId createTemplateID(ITokenDuple duple) {
        ICPPASTTemplateId result = new CPPASTTemplateId();
        ((ASTNode) result).setOffsetAndLength(duple.getStartOffset(), duple
                .getEndOffset() - duple.getStartOffset());
        CPPASTName templateIdName= null;
        if (duple instanceof BasicTokenDuple) {
        	ITokenDuple nameDuple= ((BasicTokenDuple)duple).getTemplateIdNameTokenDuple();
        	templateIdName= (CPPASTName) createName(nameDuple);
        }
        else {
        	templateIdName= (CPPASTName) createName();
            char[] image = duple.extractNameFromTemplateId();
        	templateIdName.setOffsetAndLength(duple.getStartOffset(), image.length);
            templateIdName.setName(image);
        }
        result.setTemplateName(templateIdName);
        if (duple.getTemplateIdArgLists() != null) {
            List args = duple.getTemplateIdArgLists()[0];
            if (args != null)
                for (int i = 0; i < args.size(); ++i) {
                    IASTNode n = (IASTNode) args.get(i);
                    if (n instanceof IASTTypeId)
                        result.addTemplateArgument((IASTTypeId) n);
                    else if(n instanceof IASTExpression)
                        result.addTemplateArgument((IASTExpression) n);
                }
        }
        return result;
    }


    protected IASTName createName(ITokenDuple duple) {
        if (duple == null)
            return createName();
        if (duple.getSegmentCount() != 1) {
        	// workaround for bug 193152, 
        	// looks like duple.getSeqmentCount() and duple.getSegments().length can be different.
        	ICPPASTQualifiedName qname= createQualifiedName(duple);
        	if (qname.getNames().length > 0) {
        		return qname;
        	}
        }
        if (duple.getTemplateIdArgLists() != null)
            return createTemplateID(duple);

        // We're a single name
        IASTName name = new CPPASTName(duple.toCharArray());
        if (duple instanceof OperatorTokenDuple) {
            name = createOperatorName((OperatorTokenDuple) duple, name);
        }

        IToken token = duple.getFirstToken();
        switch (token.getType()) {
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            createCompletionNode(token).addName(name);
            break;
        }

        ((ASTNode) name).setOffsetAndLength(duple.getStartOffset(), duple
                .getEndOffset() - duple.getStartOffset());

        return name;
    }

    protected IASTName createOperatorName(OperatorTokenDuple duple, IASTName name) {
        IASTName aName = null;

        if (duple.isConversionOperator()) {
            aName = new CPPASTConversionName(name.toCharArray());
            IASTTypeId typeId = duple.getTypeId();
            ((CPPASTConversionName) aName).setTypeId(typeId);
        } else {
            aName = new CPPASTOperatorName(name.toCharArray());
        }

        if (name instanceof ICPPASTTemplateId) {
            ((ICPPASTTemplateId) name).setTemplateName(aName);
            return name;
        }

        return aName;
    }


    protected ICPPASTNamespaceDefinition createNamespaceDefinition() {
        return new CPPASTNamespaceDefinition();
    }

    /**
     * Serves as the catch-all for all complicated declarations, including
     * function-definitions. simpleDeclaration : (declSpecifier)*
     * (initDeclarator ("," initDeclarator)*)? (";" | { functionBody } Notes: -
     * append functionDefinition stuff to end of this rule To do: - work in
     * functionTryBlock
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * @param tryConstructor
     *            true == take strategy1 (constructor ) : false == take strategy
     *            2 ( pointer to function)
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclaration simpleDeclaration(
            SimpleDeclarationStrategy strategy, boolean fromCatchHandler)
            throws BacktrackException, EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstOffset, firstToken.getLength());
        firstToken = null; // necessary for scalability

        IASTDeclSpecifier declSpec;
        IASTDeclarator[] declarators = new IASTDeclarator[2];
        boolean tryAgain = false;
        try {
            declSpec = declSpecifierSeq(false, false);
        } catch (FoundDeclaratorException e) {
            tryAgain = true;
            declSpec = e.declSpec;
            declarators = (IASTDeclarator[]) ArrayUtil.append(IASTDeclarator.class, declarators, e.declarator);
            backup( e.currToken );
        }
        
        if (LT(1) != IToken.tSEMI && LT(1) != IToken.tEOC) {
            if( !tryAgain )
                declarators = (IASTDeclarator[]) ArrayUtil.append(IASTDeclarator.class, declarators, initDeclarator(strategy));
            while (LT(1) == IToken.tCOMMA) {
                consume();
                declarators = (IASTDeclarator[]) ArrayUtil.append(IASTDeclarator.class, declarators, initDeclarator(strategy));
            }
        }

        declarators = (IASTDeclarator[]) ArrayUtil.removeNulls(IASTDeclarator.class, declarators);

        boolean hasFunctionBody = false;
        boolean hasFunctionTryBlock = false;
        boolean consumedSemi = false;
        int semiOffset = 0;
        List constructorChain = Collections.EMPTY_LIST;

        switch (LT(1)) {
        case IToken.tSEMI:
            if( fromCatchHandler )
                break;
            semiOffset = consume().getEndOffset();
            consumedSemi = true;
            break;
        case IToken.t_try:
            consume();
            if (LT(1) == IToken.tCOLON) {
                constructorChain = new ArrayList(DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE);
                ctorInitializer(constructorChain);
            }
            hasFunctionTryBlock = true;
            break;
        case IToken.tCOLON:
            constructorChain = new ArrayList(DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE);
            ctorInitializer(constructorChain);
            hasFunctionBody = true;
            break;
        case IToken.tLBRACE:
            break;
        case IToken.tRPAREN:
            if (!fromCatchHandler)
                throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
            break;
        case IToken.tEOC:
            // Pretend we consumed the semi
            consumedSemi = true;
            break;
        default:
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
        }

        if (!consumedSemi) {
            if (LT(1) == IToken.tLBRACE) {
                hasFunctionBody = true;
            }

            if (hasFunctionTryBlock && !hasFunctionBody)
                throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
        }

        if (hasFunctionBody) {
            if (declarators.length != 1)
                throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

            IASTDeclarator declarator = declarators[0];
            if (!(declarator instanceof IASTStandardFunctionDeclarator))
                throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

            if (!constructorChain.isEmpty() && declarator instanceof ICPPASTFunctionDeclarator) {
                ICPPASTFunctionDeclarator fd = (ICPPASTFunctionDeclarator) declarator;

                int size = constructorChain.size();
                for (int i = 0; i < size; ++i) {
                    ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) constructorChain.get(i);
                    fd.addConstructorToChain(initializer);
                }

                // fix for 86698, now that the constructorChain is established,
                // update the declarator's length
                if (fd instanceof ASTNode && constructorChain.get(size - 1) instanceof ASTNode) {
                    ASTNode init = (ASTNode) constructorChain.get(size - 1);
                    ((ASTNode) fd).setLength(init.getOffset() + init.getLength() - ((ASTNode) fd).getOffset());
                }
            }

            IASTFunctionDefinition funcDefinition = createFunctionDefinition();
            ((ASTNode) funcDefinition).setOffset(firstOffset);
            funcDefinition.setDeclSpecifier(declSpec);
            funcDefinition.setDeclarator((IASTStandardFunctionDeclarator) declarator);

            IASTStatement s = handleFunctionBody();
            if (s != null) {
                funcDefinition.setBody(s);
            }
            ((ASTNode) funcDefinition).setLength(calculateEndOffset(s) - firstOffset);

            if (hasFunctionTryBlock && declarator instanceof ICPPASTFunctionTryBlockDeclarator) {
                List handlers = new ArrayList(DEFAULT_CATCH_HANDLER_LIST_SIZE);
                catchHandlerSequence(handlers);
                for (int i = 0; i < handlers.size(); ++i) {
                    ICPPASTCatchHandler handler = (ICPPASTCatchHandler) handlers.get(i);
                    ((ICPPASTFunctionTryBlockDeclarator) declarator).addCatchHandler(handler);
                    ((ASTNode) funcDefinition).setLength(calculateEndOffset(handler) - firstOffset);
                }
            }
            return funcDefinition;
        }

        IASTSimpleDeclaration simpleDeclaration = createSimpleDeclaration();
        int length = figureEndOffset(declSpec, declarators) - firstOffset;
        if (consumedSemi)
            length = semiOffset - firstOffset;

        ((ASTNode) simpleDeclaration).setOffsetAndLength(firstOffset, length);
        simpleDeclaration.setDeclSpecifier(declSpec);

        for (int i = 0; i < declarators.length; ++i) {
            simpleDeclaration.addDeclarator(declarators[i]);
        }
        return simpleDeclaration;
    }


    protected IASTFunctionDefinition createFunctionDefinition() {
        return new CPPASTFunctionDefinition();
    }


    @Override
	protected IASTSimpleDeclaration createSimpleDeclaration() {
        return new CPPASTSimpleDeclaration();
    }

    /**
     * This method parses a constructor chain ctorinitializer: :
     * meminitializerlist meminitializerlist: meminitializer | meminitializer ,
     * meminitializerlist meminitializer: meminitializerid | ( expressionlist? )
     * meminitializerid: ::? nestednamespecifier? classname identifier
     * 
     * @param declarator
     *            IParserCallback object that represents the declarator
     *            (constructor) that owns this initializer
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void ctorInitializer(List collection) throws EndOfFileException,
            BacktrackException {
        consume();
        ctorLoop: for (;;) {
            ITokenDuple duple = name();
            IASTName name = createName(duple);

            int end;
            IASTExpression expressionList = null;
            switch (LT(1)) {
            case IToken.tLPAREN:
                consume();

                if (LT(1) != IToken.tRPAREN)
                    expressionList = expression();

                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                    end = consume().getEndOffset();
                    break;
                default:
                    throw backtrack;
                }
                break;
            case IToken.tEOC:
                end = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }

            ICPPASTConstructorChainInitializer ctorInitializer = createConstructorChainInitializer();
            ((ASTNode) ctorInitializer).setOffsetAndLength(duple.getStartOffset(), end - duple.getStartOffset());
            ctorInitializer.setMemberInitializerId(name);

            if (expressionList != null) {
                ctorInitializer.setInitializerValue(expressionList);
            }
            collection.add(ctorInitializer);

            switch (LT(1)) {
            case IToken.tCOMMA:
                consume();
                break;
            case IToken.tLBRACE:
            case IToken.tEOC:
                break ctorLoop;
            }
        }
    }

    /**
     * @return
     */
    protected ICPPASTConstructorChainInitializer createConstructorChainInitializer() {
        return new CPPASTConstructorChainInitializer();
    }

    /**
     * This routine parses a parameter declaration
     * 
     * @param containerObject
     *            The IParserCallback object representing the
     *            parameterDeclarationClause owning the parm.
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTParameterDeclaration parameterDeclaration()
            throws BacktrackException, EndOfFileException {
        IToken current = LA(1);
        IASTDeclSpecifier declSpec = null;
        try {
            declSpec = declSpecifierSeq(true, false);
        } catch (FoundDeclaratorException e) {
            throwBacktrack( e.currToken );
        }
        IASTDeclarator declarator = null;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            break;
        default:
            declarator = initDeclarator(SimpleDeclarationStrategy.TRY_FUNCTION);
        }

        if (current == LA(1)) {
            throwBacktrack(current.getOffset(), figureEndOffset(declSpec, declarator) - current.getOffset());
        }
        ICPPASTParameterDeclaration parm = createParameterDeclaration();
        ((ASTNode) parm).setOffsetAndLength(current.getOffset(), figureEndOffset(declSpec, declarator) - current.getOffset());
        parm.setDeclSpecifier(declSpec);
        if (declarator != null) {
            parm.setDeclarator(declarator);
        }
        return parm;
    }


    protected ICPPASTParameterDeclaration createParameterDeclaration() {
        return new CPPASTParameterDeclaration();
    }

    /**
     * This function parses a declaration specifier sequence, as according to
     * the ANSI C++ spec. declSpecifier : "auto" | "register" | "static" |
     * "extern" | "mutable" | "inline" | "virtual" | "explicit" | "char" |
     * "wchar_t" | "bool" | "short" | "int" | "long" | "signed" | "unsigned" |
     * "float" | "double" | "void" | "const" | "volatile" | "friend" | "typedef" |
     * ("typename")? name | {"class"|"struct"|"union"} classSpecifier | {"enum"}
     * enumSpecifier Notes: - folded in storageClassSpecifier, typeSpecifier,
     * functionSpecifier - folded elaboratedTypeSpecifier into classSpecifier
     * and enumSpecifier - find template names in name
     * 
     * @param parm
     *            Is this for a parameter declaration (true) or simple
     *            declaration (false)
     * @param forTypeId
     *            TODO
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     * @throws FoundDeclaratorException 
     */
    protected ICPPASTDeclSpecifier declSpecifierSeq(boolean parm,
            boolean forTypeId) throws BacktrackException, EndOfFileException, FoundDeclaratorException {
        IToken firstToken = LA(1);
        Flags flags = new Flags(parm, false, forTypeId);
        IToken last = null;

        boolean isInline = false, isVirtual = false, isExplicit = false, isFriend = false;
        boolean isConst = false, isVolatile = false, isRestrict = false;
        boolean isLong = false, isShort = false, isUnsigned = false, isSigned = false, isLongLong = false;
        boolean isComplex = false, isImaginary = false;
        boolean isTypename = false;

        int storageClass = IASTDeclSpecifier.sc_unspecified;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
        ITokenDuple duple = null;

        ICPPASTCompositeTypeSpecifier classSpec = null;
        ICPPASTElaboratedTypeSpecifier elabSpec = null;
        IASTEnumerationSpecifier enumSpec = null;
        IASTExpression typeofExpression = null;
        int startOffset = firstToken.getOffset();

        declSpecifiers: for (;;) {
            switch (LT(1)) {
            case IToken.t_inline:
                last = consume();
                isInline = true;
                break;
            case IToken.t_typedef:
                storageClass = IASTDeclSpecifier.sc_typedef;
                last = consume();
                break;
            case IToken.t_auto:
                last = consume();
                storageClass = IASTDeclSpecifier.sc_auto;
                break;
            case IToken.t_register:
                last = consume();
                storageClass = IASTDeclSpecifier.sc_register;
                break;
            case IToken.t_static:
                storageClass = IASTDeclSpecifier.sc_static;
                last = consume();
                break;
            case IToken.t_extern:
                storageClass = IASTDeclSpecifier.sc_extern;
                last = consume();
                break;
            case IToken.t_mutable:
                storageClass = ICPPASTDeclSpecifier.sc_mutable;
                last = consume();
                break;
            case IToken.t_virtual:
                isVirtual = true;
                last = consume();
                break;
            case IToken.t_explicit:
                isExplicit = true;
                last = consume();
                break;
            case IToken.t_friend:
                isFriend = true;
                last = consume();
                break;
            case IToken.t_const:
                isConst = true;
                last = consume();
                break;
            case IToken.t_volatile:
                isVolatile = true;
                last = consume();
                break;
            case IToken.t_restrict:
                if (!supportRestrict) {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getEndOffset() - la.getOffset());
                }
                isRestrict = true;
                last = consume();
                break;
            case IToken.t_signed:
                isSigned = true;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_unsigned:
                isUnsigned = true;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_short:
                isShort = true;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_long:
                if (isLong && supportLongLong) {
                    isLong = false;
                    isLongLong = true;
                } else
                    isLong = true;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t__Complex:
                if (!supportComplex) {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getEndOffset() - la.getOffset());
                }
                last = consume();
                isComplex=true;
                break;
            case IToken.t__Imaginary:
                if (!supportComplex) {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getLength());
                }
                last = consume();
                isImaginary=true;
                break;
            case IToken.t_char:
                simpleType = IASTSimpleDeclSpecifier.t_char;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_wchar_t:
                simpleType = ICPPASTSimpleDeclSpecifier.t_wchar_t;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_bool:
                simpleType = ICPPASTSimpleDeclSpecifier.t_bool;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_int:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_int;
                break;
            case IToken.t_float:
                simpleType = IASTSimpleDeclSpecifier.t_float;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_double:
                simpleType = IASTSimpleDeclSpecifier.t_double;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_void:
                simpleType = IASTSimpleDeclSpecifier.t_void;
                flags.setEncounteredRawType(true);
                last = consume();
                break;
            case IToken.t_typename:
                isTypename = true;
                last = consume();
                duple = name();
                last = duple.getLastToken();
                flags.setEncounteredTypename(true);
                break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
                if (flags.haveEncounteredRawType())
                    break declSpecifiers;

                if (flags.haveEncounteredTypename())
                    break declSpecifiers;

                try
                {
                    lookAheadForDeclarator(flags);
                }
                catch( FoundDeclaratorException fde )
                {
                    ICPPASTSimpleDeclSpecifier simpleDeclSpec = null;
                    if (isLongLong || typeofExpression != null) {
                        simpleDeclSpec = createGPPSimpleDeclSpecifier();
                        ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setLongLong(isLongLong);
                        if (typeofExpression != null) {
                            ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setTypeofExpression(typeofExpression);
                        }
                    } else
                        simpleDeclSpec = createSimpleDeclSpecifier();

                    if( last == null && typeofExpression != null ){
                        ((ASTNode) simpleDeclSpec).setOffsetAndLength((ASTNode) typeofExpression);
                    } else {
                        int l = last != null ? last.getEndOffset() - firstToken.getOffset() : 0;
                        ((ASTNode) simpleDeclSpec).setOffsetAndLength(firstToken.getOffset(), l);
                    }
                    simpleDeclSpec.setConst(isConst);
                    simpleDeclSpec.setVolatile(isVolatile);
                    if (simpleDeclSpec instanceof IGPPASTDeclSpecifier)
                        ((IGPPASTDeclSpecifier) simpleDeclSpec).setRestrict(isRestrict);

                    simpleDeclSpec.setFriend(isFriend);
                    simpleDeclSpec.setInline(isInline);
                    simpleDeclSpec.setStorageClass(storageClass);
                    simpleDeclSpec.setVirtual(isVirtual);
                    simpleDeclSpec.setExplicit(isExplicit);

                    simpleDeclSpec.setType(simpleType);
                    simpleDeclSpec.setLong(isLong);
                    simpleDeclSpec.setShort(isShort);
                    simpleDeclSpec.setUnsigned(isUnsigned);
                    simpleDeclSpec.setSigned(isSigned);
                    fde.declSpec = simpleDeclSpec;
                    throw fde;
                }

                duple = name();
                last = duple.getLastToken();
                flags.setEncounteredTypename(true);
                break;
            case IToken.t_class:
            case IToken.t_struct:
            case IToken.t_union:
                if (flags.haveEncounteredTypename())
                    throwBacktrack(LA(1));
                try {
                    classSpec = classSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    elabSpec = elaboratedTypeSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IToken.t_enum:
                if (flags.haveEncounteredTypename())
                    throwBacktrack(LA(1));
                try {
                    enumSpec = enumSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    // this is an elaborated class specifier
                    elabSpec = elaboratedTypeSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IGCCToken.t__attribute__: // if __attribute__ is after the declSpec
            	if (supportAttributeSpecifiers)
            		__attribute_decl_seq(true, false);
            	else
            		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
            	break;
            case IGCCToken.t__declspec: // if __declspec appears before identifier
            	if (duple == null && supportDeclspecSpecifiers)
            		__attribute_decl_seq(false, true);
            	else
            		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
            	break;
            	
            default:
            	if (LT(1) >= IExtensionToken.t__otherDeclSpecModifierFirst && LT(1) <= IExtensionToken.t__otherDeclSpecModifierLast) {
            		handleOtherDeclSpecModifier();
            		break;
            	}
            	if (supportTypeOfUnaries && LT(1) == IGCCToken.t_typeof) {
                    typeofExpression = unaryTypeofExpression();
                    if (typeofExpression != null) {
                        flags.setEncounteredTypename(true);
                    }
                }
                break declSpecifiers;
            }
        }

        if (elabSpec != null) {
            elabSpec.setConst(isConst);
            elabSpec.setVolatile(isVolatile);
            if (elabSpec instanceof IGPPASTDeclSpecifier)
                ((IGPPASTDeclSpecifier) elabSpec).setRestrict(isRestrict);
            elabSpec.setFriend(isFriend);
            elabSpec.setInline(isInline);
            elabSpec.setStorageClass(storageClass);
            elabSpec.setVirtual(isVirtual);
            elabSpec.setExplicit(isExplicit);
            ((ASTNode) elabSpec).setOffsetAndLength(startOffset, calculateEndOffset(elabSpec) - startOffset);
            return elabSpec;
        }
        if (enumSpec != null) {
            enumSpec.setConst(isConst);
            enumSpec.setVolatile(isVolatile);
            if (enumSpec instanceof IGPPASTDeclSpecifier)
                ((IGPPASTDeclSpecifier) enumSpec).setRestrict(isRestrict);
            ((ICPPASTDeclSpecifier) enumSpec).setFriend(isFriend);
            ((ICPPASTDeclSpecifier) enumSpec).setVirtual(isVirtual);
            ((ICPPASTDeclSpecifier) enumSpec).setExplicit(isExplicit);
            enumSpec.setInline(isInline);
            enumSpec.setStorageClass(storageClass);
            ((ASTNode) enumSpec).setOffsetAndLength(startOffset, calculateEndOffset(enumSpec) - startOffset);
            return (ICPPASTDeclSpecifier) enumSpec;
        }
        if (classSpec != null) {
            classSpec.setConst(isConst);
            classSpec.setVolatile(isVolatile);
            if (classSpec instanceof IGPPASTDeclSpecifier)
                ((IGPPASTDeclSpecifier) classSpec).setRestrict(isRestrict);
            classSpec.setFriend(isFriend);
            classSpec.setInline(isInline);
            classSpec.setStorageClass(storageClass);
            classSpec.setVirtual(isVirtual);
            classSpec.setExplicit(isExplicit);
            ((ASTNode) classSpec).setOffsetAndLength(startOffset, calculateEndOffset(classSpec) - startOffset);
            return classSpec;
        }
        if (duple != null) {
            ICPPASTNamedTypeSpecifier nameSpec = (ICPPASTNamedTypeSpecifier) createNamedTypeSpecifier();
            nameSpec.setIsTypename(isTypename);
            IASTName name = createName(duple);
            nameSpec.setName(name);
            nameSpec.setConst(isConst);
            nameSpec.setVolatile(isVolatile);
            if (nameSpec instanceof IGPPASTDeclSpecifier)
                ((IGPPASTDeclSpecifier) nameSpec).setRestrict(isRestrict);
            nameSpec.setFriend(isFriend);
            nameSpec.setInline(isInline);
            nameSpec.setStorageClass(storageClass);
            nameSpec.setVirtual(isVirtual);
            nameSpec.setExplicit(isExplicit);
            ((ASTNode) nameSpec).setOffsetAndLength(startOffset, last.getEndOffset() - startOffset);
            return nameSpec;
        }
        ICPPASTSimpleDeclSpecifier simpleDeclSpec = null;
        if (isComplex || isImaginary || isLongLong || typeofExpression != null) {
            simpleDeclSpec = createGPPSimpleDeclSpecifier();
            ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setLongLong(isLongLong);
            ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setComplex(isComplex);
            ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setImaginary(isImaginary);
            if (typeofExpression != null) {
                ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setTypeofExpression(typeofExpression);
            }
        } else
            simpleDeclSpec = createSimpleDeclSpecifier();

		if( last == null && typeofExpression != null ){
			((ASTNode) simpleDeclSpec).setOffsetAndLength((ASTNode) typeofExpression);
		} else {
	        int l = last != null ? last.getEndOffset() - firstToken.getOffset() : 0;
	        ((ASTNode) simpleDeclSpec).setOffsetAndLength(firstToken.getOffset(), l);
		}
        simpleDeclSpec.setConst(isConst);
        simpleDeclSpec.setVolatile(isVolatile);
        if (simpleDeclSpec instanceof IGPPASTDeclSpecifier)
            ((IGPPASTDeclSpecifier) simpleDeclSpec).setRestrict(isRestrict);

        simpleDeclSpec.setFriend(isFriend);
        simpleDeclSpec.setInline(isInline);
        simpleDeclSpec.setStorageClass(storageClass);
        simpleDeclSpec.setVirtual(isVirtual);
        simpleDeclSpec.setExplicit(isExplicit);

        simpleDeclSpec.setType(simpleType);
        simpleDeclSpec.setLong(isLong);
        simpleDeclSpec.setShort(isShort);
        simpleDeclSpec.setUnsigned(isUnsigned);
        simpleDeclSpec.setSigned(isSigned);
        
        return simpleDeclSpec;
    }


    protected ICPPASTSimpleDeclSpecifier createGPPSimpleDeclSpecifier() {
        return new GPPASTSimpleDeclSpecifier();
    }


    protected ICPPASTSimpleDeclSpecifier createSimpleDeclSpecifier() {
        return new CPPASTSimpleDeclSpecifier();
    }


    @Override
	protected IASTNamedTypeSpecifier createNamedTypeSpecifier() {
        return new CPPASTNamedTypeSpecifier();
    }

    /**
     * Parse an elaborated type specifier.
     * 
     * @param decl
     *            Declaration which owns the elaborated type
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier()
            throws BacktrackException, EndOfFileException {
        // this is an elaborated class specifier
        IToken t = consume();
        int eck = 0;

        switch (t.getType()) {
        case IToken.t_class:
            eck = ICPPASTElaboratedTypeSpecifier.k_class;
            break;
        case IToken.t_struct:
            eck = IASTElaboratedTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            eck = IASTElaboratedTypeSpecifier.k_union;
            break;
        case IToken.t_enum:
            eck = IASTElaboratedTypeSpecifier.k_enum;
            break;
        default:
            backup(t);
            throwBacktrack(t.getOffset(), t.getLength());
        }

        IASTName name = createName(name());

        ICPPASTElaboratedTypeSpecifier elaboratedTypeSpec = createElaboratedTypeSpecifier();
        ((ASTNode) elaboratedTypeSpec).setOffsetAndLength(t.getOffset(), calculateEndOffset(name) - t.getOffset());
        elaboratedTypeSpec.setKind(eck);
        elaboratedTypeSpec.setName(name);
        return elaboratedTypeSpec;
    }


    protected ICPPASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
        return new CPPASTElaboratedTypeSpecifier();
    }

    @Override
	protected IASTDeclarator initDeclarator() throws EndOfFileException, BacktrackException {
        return initDeclarator(SimpleDeclarationStrategy.TRY_FUNCTION);
    }

    /**
     * Parses the initDeclarator construct of the ANSI C++ spec. initDeclarator :
     * declarator ("=" initializerClause | "(" expressionList ")")?
     * 
     * @param constructInitializers
     *            TODO
     * @param owner
     *            IParserCallback object that represents the owner declaration
     *            object.
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclarator initDeclarator(SimpleDeclarationStrategy strategy)
            throws EndOfFileException, BacktrackException {
        IASTDeclarator d = declarator(strategy, false);

        IASTInitializer initializer = optionalCPPInitializer(d);
        if (initializer != null) {
            d.setInitializer(initializer);
            ((ASTNode) d).setLength(calculateEndOffset(initializer) - ((ASTNode) d).getOffset());
        }
        return d;
    }

    protected IASTInitializer optionalCPPInitializer(IASTDeclarator d)
            throws EndOfFileException, BacktrackException {
        // handle initializer

        if (LT(1) == IToken.tASSIGN) {
            consume();
            try {
                return initializerClause();
            } catch (EndOfFileException eof) {
                failParse();
                throw eof;
            }
        } else if (LT(1) == IToken.tLPAREN) {
            if (d instanceof IASTFunctionDeclarator
                    && d.getNestedDeclarator() == null) {
                // constructor initializer doesn't make sense for a function
                // declarator,
                // we must have an object to initialize, a function doesn't
                // work.
                return null;
            }
            // initializer in constructor
            IToken t = consume(); // EAT IT!
            int o = t.getOffset();
            IASTExpression astExpression = expression();
            if( astExpression == null )
                throwBacktrack( t );
            int l = consume(IToken.tRPAREN).getEndOffset();
            ICPPASTConstructorInitializer result = createConstructorInitializer();
            ((ASTNode) result).setOffsetAndLength(o, l - o);
            result.setExpression(astExpression);
            return result;
        }
        return null;
    }


    protected ICPPASTConstructorInitializer createConstructorInitializer() {
        return new CPPASTConstructorInitializer();
    }


    protected IASTInitializer initializerClause() throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tLBRACE) {
            int startingOffset = consume().getOffset();

            IASTInitializerList result = createInitializerList();
            ((ASTNode) result).setOffset(startingOffset);

            if (LT(1) == (IToken.tRBRACE)) {
                int l = consume().getEndOffset();
                ((ASTNode) result).setLength(l - startingOffset);
                return result;
            }

            // otherwise it is a list of initializer clauses

            for (;;) {
                if (LT(1) == IToken.tRBRACE)
                    break;

                IASTInitializer clause = initializerClause();
                if (clause != null) {
                    result.addInitializer(clause);
                }
                if (LT(1) == IToken.tRBRACE || LT(1) == IToken.tEOC)
                    break;
                consume(IToken.tCOMMA);
            }
            int l = consume().getEndOffset(); // tRBRACE
            ((ASTNode) result).setLength(l - startingOffset);
            return result;
        }

        // if we get this far, it means that we did not
        // try this now instead
        // assignmentExpression
        IASTExpression assignmentExpression = assignmentExpression();
        IASTInitializerExpression result = createInitializerExpression();
        ((ASTNode) result).setOffsetAndLength(((ASTNode) assignmentExpression));
        result.setExpression(assignmentExpression);
        return result;
    }


    protected IASTInitializerList createInitializerList() {
        return new CPPASTInitializerList();
    }


    protected IASTInitializerExpression createInitializerExpression() {
        return new CPPASTInitializerExpression();
    }

    /**
     * Parse a declarator, as according to the ANSI C++ specification.
     * declarator : (ptrOperator)* directDeclarator directDeclarator :
     * declaratorId | directDeclarator "(" parameterDeclarationClause ")"
     * (cvQualifier)* (exceptionSpecification)* | directDeclarator "["
     * (constantExpression)? "]" | "(" declarator")" | directDeclarator "("
     * parameterDeclarationClause ")" (oldKRParameterDeclaration)* declaratorId :
     * name
     * 
     * @param forNewTypeId
     *            TODO
     * @param container
     *            IParserCallback object that represents the owner declaration.
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclarator declarator(SimpleDeclarationStrategy strategy,
            boolean forNewTypeId) throws EndOfFileException, BacktrackException {

        IToken la = LA(1);
        int startingOffset = la.getOffset();
        la = null;
        IASTDeclarator innerDecl = null;
        IASTName declaratorName = null;
        List pointerOps = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
        List parameters = Collections.EMPTY_LIST;
        List arrayMods = Collections.EMPTY_LIST;
        List exceptionSpecIds = Collections.EMPTY_LIST;
        boolean encounteredVarArgs = false;
        boolean tryEncountered = false;
        IASTExpression bitField = null;
        boolean isFunction = false;
        boolean isPureVirtual = false, isConst = false, isVolatile = false;
        int finalOffset = startingOffset;
        overallLoop: do {

            consumePointerOperators(pointerOps);
            
            // if __attribute__ is after the pointer ops and before the declarator ex: void * __attribute__((__cdecl__)) foo();
            __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
            
            if (!pointerOps.isEmpty())
                finalOffset = calculateEndOffset((IASTNode) pointerOps.get(pointerOps.size() - 1));

            if (!forNewTypeId && LT(1) == IToken.tLPAREN) {
                IToken mark = mark();
                try {
                    consume();
                    innerDecl = declarator(strategy, forNewTypeId);
                    finalOffset = consume(IToken.tRPAREN).getEndOffset();
                } catch (BacktrackException bte) {
                    backup(mark);
                    innerDecl = null;
                }
                declaratorName = createName();
            } else {
                try {
                    declaratorName = consumeTemplatedOperatorName();
                    finalOffset = calculateEndOffset(declaratorName);
                    if (declaratorName instanceof ICPPASTQualifiedName
                            && ((ICPPASTQualifiedName) declaratorName).isConversionOrOperator())
                        isFunction = true;
                } catch (BacktrackException bt) {
                    declaratorName = createName();
                }
            }

            for (;;) {
                switch (LT(1)) {
                case IToken.tLPAREN:
                    IToken markityMark = mark();
                    boolean success = false;
                    try
                    {
                        consume(); // tLPAREN
                        expression();
                        consume( IToken.tRPAREN );
                        success = true;
                        backup( markityMark );
                    }
                    catch( BacktrackException bte )
                    {
                        backup( markityMark );
                    }
                    catch( EndOfFileException eof )
                    {
                        backup( markityMark );
                    }
                    if( success )
                    {
                        switch( LT(1) )
                        {
                        case IToken.tSEMI:
                        case IToken.tCOMMA:
                            break overallLoop;
                        }
                    }

                    if( strategy == SimpleDeclarationStrategy.TRY_VARIABLE )
                        break overallLoop;

                    
                    if (!looksLikeExpression(LA(2))&& !forNewTypeId) {
                        // parameterDeclarationClause
                        isFunction = true;
                        // TODO need to create a temporary scope object here
                        IToken last = consume(IToken.tLPAREN);
                        finalOffset = last.getEndOffset();
                        boolean seenParameter = false;
                        parameterDeclarationLoop: for (;;) {
                            switch (LT(1)) {
                            case IToken.tRPAREN:
                                last = consume();
                                finalOffset = last.getEndOffset();
                                break parameterDeclarationLoop;
                            case IToken.tEOC:
                                break parameterDeclarationLoop;
                            case IToken.tELLIPSIS:
                                last = consume();
                                encounteredVarArgs = true;
                                finalOffset = last.getEndOffset();
                                break;
                            case IToken.tCOMMA:
                                last = consume();
                                seenParameter = false;
                                finalOffset = last.getEndOffset();
                                break;
                            default:
                                int endOffset = (last != null) ? last.getEndOffset() : LA(1).getEndOffset();
                                if (seenParameter)
                                    throwBacktrack(startingOffset, endOffset - startingOffset);
                                IASTParameterDeclaration p = parameterDeclaration();
                                finalOffset = calculateEndOffset(p);
                                if (parameters == Collections.EMPTY_LIST)
                                    parameters = new ArrayList(DEFAULT_PARM_LIST_SIZE);
                                parameters.add(p);
                                seenParameter = true;
                            }
                        }
                    }

                    if (LT(1) == IToken.tCOLON)
                        break overallLoop;

                    if (LT(1) == IToken.t_try) {
                        tryEncountered = true;
                        break overallLoop;
                    }

                    // Consume any number of __attribute__ tokens after the parameters
                    __attribute_decl_seq(supportAttributeSpecifiers, false);
                    	

                    IToken beforeCVModifier = mark();
                    IToken[] cvModifiers = new IToken[2];
                    int numCVModifiers = 0;
                    IToken afterCVModifier = beforeCVModifier;
                    // const-volatile
                    // 2 options: either this is a marker for the method,
                    // or it might be the beginning of old K&R style
                    // parameter declaration, see
                    // void getenv(name) const char * name; {}
                    // This will be determined further below
                    while ((LT(1) == IToken.t_const || LT(1) == IToken.t_volatile) && numCVModifiers < 2) {
                        IToken t = consume();
                        finalOffset = t.getEndOffset();
                        cvModifiers[numCVModifiers++] = t;
                        afterCVModifier = mark();
                    }
                    // check for throws clause here

                    if (LT(1) == IToken.t_throw) {
                        exceptionSpecIds = new ArrayList(DEFAULT_SIZE_EXCEPTIONS_LIST);
                        consume(); // throw
                        consume(IToken.tLPAREN); // (
                        boolean done = false;
                        while (!done) {
                            switch (LT(1)) {
                            case IToken.tRPAREN:
                                finalOffset = consume().getEndOffset();
                                done = true;
                                break;
                            case IToken.tCOMMA:
                                consume();
                                break;
                            default:
                                IToken before = LA(1);
                                IASTTypeId typeId = typeId(false);
                                if (typeId != null) {
                                    exceptionSpecIds.add(typeId);
                                } else {
                                    IASTProblem p = createProblem(IASTProblem.SYNTAX_ERROR, 
                                    		before.getOffset(), before.getLength());
                                	IASTProblemTypeId typeIdProblem = createTypeIDProblem();
                                    typeIdProblem.setProblem(p);
                                    ((ASTNode) typeIdProblem).setOffsetAndLength(((ASTNode) p));
                                    exceptionSpecIds.add(typeIdProblem);
                                    if (before == LA(1))
                                        done = true;
                                }
                                break;
                            }
                        }
                        
                        // more __attribute__ after throws
                        __attribute_decl_seq(supportAttributeSpecifiers, false);
                    }
                    // check for optional pure virtual
                    if (LT(1) == IToken.tASSIGN && LT(2) == IToken.tINTEGER) {
                        char[] image = LA(2).getCharImage();
                        if (image.length == 1 && image[0] == '0') {
                            consume(); // tASSIGN
                            finalOffset = consume().getEndOffset(); // tINTEGER
                            isPureVirtual = true;
                        }
                    }
                    if (afterCVModifier != LA(1) || LT(1) == IToken.tSEMI
                            || LT(1) == IToken.tLBRACE) {
                        // There were C++-specific clauses after
                        // const/volatile modifier
                        // Then it is a marker for the method
                        if (numCVModifiers > 0) {
                            for (int i = 0; i < numCVModifiers; i++) {
                                if (cvModifiers[i].getType() == IToken.t_const)
                                    isConst = true;
                                if (cvModifiers[i].getType() == IToken.t_volatile)
                                    isVolatile = true;
                            }
                        }
                        afterCVModifier = mark();
                        // In this case (method) we can't expect K&R
                        // parameter declarations,
                        // but we'll check anyway, for errorhandling
                    }
                    break;
                case IToken.tLBRACKET:
                    if (forNewTypeId)
                        break;
                    arrayMods = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
                    consumeArrayModifiers(arrayMods);
                    if (!arrayMods.isEmpty())
                        finalOffset = calculateEndOffset((IASTNode) arrayMods.get(arrayMods.size() - 1));
                    continue;
                case IToken.tCOLON:
                    consume();
                    bitField = constantExpression();
                    finalOffset = calculateEndOffset(bitField);
                    break;
                case IGCCToken.t__attribute__:  // if __attribute__ is after the declarator
                	if(supportAttributeSpecifiers)
                		__attribute_decl_seq(supportAttributeSpecifiers, false);
                	else
                		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
                	break;
                default:
                    break;
                }
                break;
            }

        } while (false);

        IASTDeclarator d = null;
        if (isFunction) {

            ICPPASTFunctionDeclarator fc = null;
            if (tryEncountered)
                fc = createTryBlockDeclarator();
            else
                fc = createFunctionDeclarator();
            fc.setVarArgs(encounteredVarArgs);
            for (int i = 0; i < parameters.size(); ++i) {
                IASTParameterDeclaration p = (IASTParameterDeclaration) parameters.get(i);
                fc.addParameterDeclaration(p);
            }
            fc.setConst(isConst);
            fc.setVolatile(isVolatile);
            fc.setPureVirtual(isPureVirtual);
            for (int i = 0; i < exceptionSpecIds.size(); ++i) {
                IASTTypeId typeId = (IASTTypeId) exceptionSpecIds.get(i);
                fc.addExceptionSpecificationTypeId(typeId);
            }
            d = fc;
        } else if (arrayMods != Collections.EMPTY_LIST) {
            d = createArrayDeclarator();
            for (int i = 0; i < arrayMods.size(); ++i) {
                IASTArrayModifier m = (IASTArrayModifier) arrayMods.get(i);
                ((IASTArrayDeclarator) d).addArrayModifier(m);
            }
        } else if (bitField != null) {
            IASTFieldDeclarator fl = createFieldDeclarator();
            fl.setBitFieldSize(bitField);
            d = fl;
        } else {
            d = createDeclarator();
        }
        for (int i = 0; i < pointerOps.size(); ++i) {
            IASTPointerOperator po = (IASTPointerOperator) pointerOps.get(i);
            d.addPointerOperator(po);
        }
        if (innerDecl != null) {
            d.setNestedDeclarator(innerDecl);
        }
        if (declaratorName != null) {
            d.setName(declaratorName);
        }

        ((ASTNode) d).setOffsetAndLength(startingOffset, finalOffset - startingOffset);
        return d;

    }

    private boolean looksLikeExpression(IToken t) {
		switch (t.getType()) {
			case IToken.tINTEGER:
			case IToken.t_false:
			case IToken.t_true:
			case IToken.tSTRING:
			case IToken.tLSTRING:
			case IToken.tFLOATINGPT:
			case IToken.tCHAR:
			case IToken.tAMPER:
			case IToken.tDOT:
			case IToken.tLPAREN:
			case IToken.tMINUS:
			case IToken.tSTAR: 
			case IToken.tPLUS: 
			case IToken.tNOT:
			case IToken.tBITCOMPLEMENT:
				return true;
			default:
				break;
		}	
		return false;
	}


    protected IASTProblemTypeId createTypeIDProblem() {
        return new CPPASTProblemTypeId();
    }


    protected ICPPASTFunctionTryBlockDeclarator createTryBlockDeclarator() {
        return new CPPASTFunctionTryBlockDeclarator();
    }


    protected ICPPASTFunctionDeclarator createFunctionDeclarator() {
        return new CPPASTFunctionDeclarator();
    }


    protected IASTFieldDeclarator createFieldDeclarator() {
        return new CPPASTFieldDeclarator();
    }


    protected IASTDeclarator createArrayDeclarator() {
        return new CPPASTArrayDeclarator();
    }


    protected IASTDeclarator createDeclarator() {
        return new CPPASTDeclarator();
    }

    protected IASTName consumeTemplatedOperatorName()
            throws EndOfFileException, BacktrackException {
        TemplateParameterManager argumentList = TemplateParameterManager.getInstance();
        try {
            if (LT(1) == IToken.t_operator)
                return operatorId(null, null);

            try {
                return createName(name());
            } catch (BacktrackException bt) {
            }
            IToken start = null;

            IToken mark = mark();
            if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                start = consume();
                IToken end = null;

                if (start.getType() == IToken.tIDENTIFIER) {
                    end = consumeTemplateArguments(end, argumentList);
                }

                while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                    end = consume();
                    if (end.getType() == IToken.tIDENTIFIER) {
                        end = consumeTemplateArguments(end, argumentList);
                    }
                }

                if (LT(1) == IToken.t_operator) {
                    return operatorId(start, argumentList);
                }

                int endOffset = (end != null) ? end.getEndOffset() : 0;
                backup(mark);
                throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());
            }
            int endOffset = (mark != null) ? mark.getEndOffset() : 0;
            backup(mark);
            throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());

            return null;
        } finally {
            TemplateParameterManager.returnInstance(argumentList);
        }
    }

    /**
     * Parse a class/struct/union definition. classSpecifier : classKey name
     * (baseClause)? "{" (memberSpecification)* "}"
     * 
     * @param owner
     *            IParserCallback object that represents the declaration that
     *            owns this classSpecifier
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTCompositeTypeSpecifier classSpecifier()
            throws BacktrackException, EndOfFileException {
        int classKind = 0;
        IToken classKey = null;
        IToken mark = mark();

        // class key
        switch (LT(1)) {
        case IToken.t_class:
            classKey = consume();
            classKind = ICPPASTCompositeTypeSpecifier.k_class;
            break;
        case IToken.t_struct:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_union;
            break;
        default:
            throwBacktrack(mark.getOffset(), mark.getLength());
        }

        IASTName name = null;

        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier        
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        // class name
        if (LT(1) == IToken.tIDENTIFIER)
            name = createName(name());
        else
            name = createName();
        
        // if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        if (LT(1) != IToken.tCOLON && LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint.getOffset(), errorPoint.getLength());
        }

        ICPPASTCompositeTypeSpecifier astClassSpecifier = createClassSpecifier();
        ((ASTNode) astClassSpecifier).setOffset(classKey.getOffset());
        astClassSpecifier.setKey(classKind);
        astClassSpecifier.setName(name);

        // base clause
        if (LT(1) == IToken.tCOLON) {
            baseSpecifier(astClassSpecifier);
        }

        if (LT(1) == IToken.tLBRACE) {
            consume();

            memberDeclarationLoop: while (true) {
                int checkToken = LA(1).hashCode();
                switch (LT(1)) {
                case IToken.t_public:
                case IToken.t_protected:
                case IToken.t_private: {
                    IToken key = consume();
                    int l = consume(IToken.tCOLON).getEndOffset();
                    ICPPASTVisiblityLabel label = createVisibilityLabel();
                    ((ASTNode) label).setOffsetAndLength(key.getOffset(), l - key.getOffset());
                    label.setVisibility(token2Visibility(key.getType()));
                    astClassSpecifier.addMemberDeclaration(label);
                    label.setParent(astClassSpecifier);
                    // this overrides the MEMBER_DECLARATION property
                    label.setPropertyInParent(ICPPASTCompositeTypeSpecifier.VISIBILITY_LABEL);
                    break;
                }
                case IToken.tRBRACE: {
                    int l = consume().getEndOffset();
                    ((ASTNode) astClassSpecifier).setLength(l - classKey.getOffset());
                    break memberDeclarationLoop;
                }
                case IToken.tEOC:
                    // Don't care about the offsets
                    break memberDeclarationLoop;
                default:
                    try {
                        IASTDeclaration d = declaration();
                        astClassSpecifier.addMemberDeclaration(d);
                    } catch (BacktrackException bt) {
                        IASTProblem p = failParse(bt);
                        IASTProblemDeclaration pd = createProblemDeclaration();
                        pd.setProblem(p);
                        ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                        astClassSpecifier.addMemberDeclaration(pd);
                        if (checkToken == LA(1).hashCode())
                            errorHandling();
                    }

                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
            }
        }
        return astClassSpecifier;
    }


    protected ICPPASTCompositeTypeSpecifier createClassSpecifier() {
        return new CPPASTCompositeTypeSpecifier();
    }


    protected ICPPASTVisiblityLabel createVisibilityLabel() {
        return new CPPASTVisibilityLabel();
    }


    protected int token2Visibility(int type) {
        switch (type) {
        case IToken.t_public:
            return ICPPASTVisiblityLabel.v_public;
        case IToken.t_protected:
            return ICPPASTVisiblityLabel.v_protected;
        case IToken.t_private:
            return ICPPASTVisiblityLabel.v_private;
        }
        return 0;
    }

    /**
     * Parse the subclass-baseclauses for a class specification. baseclause: :
     * basespecifierlist basespecifierlist: basespecifier basespecifierlist,
     * basespecifier basespecifier: ::? nestednamespecifier? classname virtual
     * accessspecifier? ::? nestednamespecifier? classname accessspecifier
     * virtual? ::? nestednamespecifier? classname accessspecifier: private |
     * protected | public
     * 
     * @param classSpecOwner
     * @throws BacktrackException
     */
    protected void baseSpecifier(ICPPASTCompositeTypeSpecifier astClassSpec)
            throws EndOfFileException, BacktrackException {

        IToken last = consume(); // tCOLON

        boolean isVirtual = false;
        int visibility = 0; // ASTAccessVisibility.PUBLIC;
        IASTName name = null;
        IToken firstToken = null;
        baseSpecifierLoop: for (;;) {
            switch (LT(1)) {
            case IToken.t_virtual:
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                isVirtual = true;
                break;
            case IToken.t_public:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_public;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.t_protected:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_protected;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.t_private:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_private;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
                // to get templates right we need to use the class as the scope
                ITokenDuple d = name();
                name = createName(d);
                if (firstToken == null)
                    firstToken = d.getFirstToken();
                last = d.getLastToken();
                break;
            case IToken.tCOMMA:
                if (name == null)
                    name = createName();
                consume();
                ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = createBaseSpecifier();
                if (firstToken != null)
                    ((ASTNode) baseSpec).setOffsetAndLength(firstToken.getOffset(), last.getEndOffset() - firstToken.getOffset());
                baseSpec.setVirtual(isVirtual);
                baseSpec.setVisibility(visibility);
                baseSpec.setName(name);
                astClassSpec.addBaseSpecifier(baseSpec);

                isVirtual = false;
                visibility = 0;
                name = null;
                firstToken = null;

                continue baseSpecifierLoop;
            case IToken.tLBRACE:
            case IToken.tEOC:
                if (name == null)
                    name = createName();
                baseSpec = createBaseSpecifier();
                if (firstToken != null)
                    ((ASTNode) baseSpec).setOffsetAndLength(firstToken.getOffset(), last.getEndOffset() - firstToken.getOffset());
                baseSpec.setVirtual(isVirtual);
                baseSpec.setVisibility(visibility);
                baseSpec.setName(name);
                astClassSpec.addBaseSpecifier(baseSpec);
            // fall through
            default:
                break baseSpecifierLoop;
            }
        }
    }

    /**
     * @return
     */
    protected ICPPASTBaseSpecifier createBaseSpecifier() {
        return new CPPASTBaseSpecifier();
    }

    protected void catchHandlerSequence(List collection)
            throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tEOC)
            return;
        
        if (LT(1) != IToken.t_catch) {
            IToken la = LA(1);
            throwBacktrack(la.getOffset(), la.getLength()); // error, need at
            // least
            // one of these
        }
		
		int nextToken = LT(1);
        while (nextToken == IToken.t_catch) {
            int startOffset = consume().getOffset();
            consume(IToken.tLPAREN);
            boolean isEllipsis = false;
            IASTDeclaration decl = null;
            try {
                if (LT(1) == IToken.tELLIPSIS) {
                    consume(IToken.tELLIPSIS);
                    isEllipsis = true;
                } else {
                    decl = simpleDeclaration(
                            SimpleDeclarationStrategy.TRY_VARIABLE, true);
                }
                if (LT(1) != IToken.tEOC)
                    consume(IToken.tRPAREN);
            } catch (BacktrackException bte) {
                IASTProblem p = failParse(bte);
                IASTProblemDeclaration pd = createProblemDeclaration();
                pd.setProblem(p);
                ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                decl = pd;
            }

            ICPPASTCatchHandler handler = createCatchHandler();
            if (decl != null) {
                handler.setDeclaration(decl);
            }

            if (LT(1) != IToken.tEOC) {
                IASTStatement compoundStatement = catchBlockCompoundStatement();
                ((ASTNode) handler).setOffsetAndLength(startOffset,
                        calculateEndOffset(compoundStatement) - startOffset);
                handler.setIsCatchAll(isEllipsis);
                if (compoundStatement != null) {
                    handler.setCatchBody(compoundStatement);
                }
            }
            
            collection.add(handler);
			
			try {
				nextToken = LT(1);
			} catch (EndOfFileException eofe) {
				// if EOF is reached, then return here and let it be encountered elsewhere 
				// (i.e. try/catch won't be added to the declaration if the exception is thrown here)
				return; 
			}
        }
    }

    /**
     * @return
     */
    protected ICPPASTCatchHandler createCatchHandler() {
        return new CPPASTCatchHandler();
    }

    protected IASTStatement catchBlockCompoundStatement()
            throws BacktrackException, EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE) {
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return compoundStatement();
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        }
        return compoundStatement();
    }

    /**
     * This is the top-level entry point into the ANSI C++ grammar.
     * translationUnit : (declaration)*
     */
    @Override
	protected void translationUnit() {
        try {
            translationUnit = createTranslationUnit();
            translationUnit.setIndex(index);

            // add built-in names to the scope
			// add built-in names to the scope
			if (builtinBindingsProvider != null) {
				IScope tuScope = translationUnit.getScope();
				
				IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
				for(int i=0; i<bindings.length; i++) {
					ASTInternal.addBinding(tuScope, bindings[i]);
				}
			}
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }
        translationUnit.setLocationResolver(scanner.getLocationResolver());

        while (true) {
            try {
                if (LT(1) == IToken.tEOC)
                    break;
                int checkOffset = LA(1).hashCode();
                IASTDeclaration declaration = declaration();
                translationUnit.addDeclaration(declaration);

                if (LA(1).hashCode() == checkOffset)
                    failParseWithErrorHandling();
            } catch (EndOfFileException e) {
                if (translationUnit.getDeclarations().length != 0) {
                	ASTNode d = (ASTNode) translationUnit.getDeclarations()[translationUnit.getDeclarations().length - 1];
                    ((ASTNode) translationUnit).setLength(d.getOffset() + d.getLength());
                } else
                    ((ASTNode) translationUnit).setLength(0);
                break;
            } catch (BacktrackException b) {
                try {
                    // Mark as failure and try to reach a recovery point
                    IASTProblem p = failParse(b);
                    IASTProblemDeclaration pd = createProblemDeclaration();
                    pd.setProblem(p);
                    ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                    translationUnit.addDeclaration(pd);
                    errorHandling();
                } catch (EndOfFileException e) {
                    break;
                }
            } catch (OutOfMemoryError oome) {
                logThrowable("translationUnit", oome); //$NON-NLS-1$
                throw oome;
            } catch (Exception e) {
                logException("translationUnit", e); //$NON-NLS-1$
                try {
                    failParseWithErrorHandling();
                } catch (EndOfFileException e3) {
                    // nothing
                }
            }
        }
    }


    protected IASTProblemDeclaration createProblemDeclaration() {
        return new CPPASTProblemDeclaration();
    }


    protected CPPASTTranslationUnit createTranslationUnit() {
        return new CPPASTTranslationUnit();
    }

    protected void consumeArrayModifiers(List collection)
            throws EndOfFileException, BacktrackException {
        while (LT(1) == IToken.tLBRACKET) {
            int o = consume().getOffset(); // eat the '['

            IASTExpression exp = null;
            if (LT(1) != IToken.tRBRACKET && LT(1) != IToken.tEOC) {
                exp = constantExpression();
            }
            int l;
            switch (LT(1)) {
            case IToken.tRBRACKET:
            case IToken.tEOC:
                l = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }
            IASTArrayModifier arrayMod = createArrayModifier();
            ((ASTNode) arrayMod).setOffsetAndLength(o, l - o);
            if (exp != null) {
                arrayMod.setConstantExpression(exp);
            }
            collection.add(arrayMod);
        }
        return;
    }


    protected IASTArrayModifier createArrayModifier() {
        return new CPPASTArrayModifier();
    }

    @Override
	protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    @Override
	protected IASTCompoundStatement createCompoundStatement() {
        return new CPPASTCompoundStatement();
    }

    @Override
	protected IASTBinaryExpression createBinaryExpression() {
        return new CPPASTBinaryExpression();
    }

    @Override
	protected IASTConditionalExpression createConditionalExpression() {
        return new CPPASTConditionalExpression();
    }

    @Override
	protected IASTUnaryExpression createUnaryExpression() {
        return new CPPASTUnaryExpression();
    }

    @Override
	protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
        return new CPPASTCompoundStatementExpression();
    }

    @Override
	protected IASTExpressionList createExpressionList() {
        return new CPPASTExpressionList();
    }

    @Override
	protected IASTName createName(IToken token) {
        IASTName n = null;

        if (token instanceof OperatorTokenDuple) {
            n = createOperatorName((OperatorTokenDuple) token, n);
        } else {
            n = new CPPASTName(token.getCharImage());
        }

        switch (token.getType()) {
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            createCompletionNode(token).addName(n);
            break;
        }
        ((ASTNode) n).setOffsetAndLength(token.getOffset(), token.getLength());

        return n;
    }

    @Override
	protected IASTName createName() {
        return new CPPASTName();
    }

    @Override
	protected IASTEnumerator createEnumerator() {
        return new CPPASTEnumerator();
    }


    @Override
	protected IASTExpression buildTypeIdExpression(int op, IASTTypeId typeId,
            int startingOffset, int endingOffset) {
        ICPPASTTypeIdExpression typeIdExpression = createTypeIdExpression();
        ((ASTNode) typeIdExpression).setOffsetAndLength(startingOffset, endingOffset - startingOffset);
        ((ASTNode) typeIdExpression).setLength(endingOffset - startingOffset);
        typeIdExpression.setOperator(op);
        typeIdExpression.setTypeId(typeId);
        return typeIdExpression;
    }

    protected ICPPASTTypeIdExpression createTypeIdExpression() {
        return new CPPASTTypeIdExpression();
    }

    @Override
	protected IASTEnumerationSpecifier createEnumerationSpecifier() {
        return new CPPASTEnumerationSpecifier();
    }

    @Override
	protected IASTLabelStatement createLabelStatement() {
        return new CPPASTLabelStatement();
    }

    @Override
	protected IASTGotoStatement createGoToStatement() {
        return new CPPASTGotoStatement();
    }

    @Override
	protected IASTReturnStatement createReturnStatement() {
        return new CPPASTReturnStatement();
    }

    protected ICPPASTForStatement createForStatement() {
        return new CPPASTForStatement();
    }

    @Override
	protected IASTContinueStatement createContinueStatement() {
        return new CPPASTContinueStatement();
    }

    @Override
	protected IASTDoStatement createDoStatement() {
        return new CPPASTDoStatement();
    }

    @Override
	protected IASTBreakStatement createBreakStatement() {
        return new CPPASTBreakStatement();
    }

    @Override
	protected IASTWhileStatement createWhileStatement() {
        return new CPPASTWhileStatement();
    }

    @Override
	protected IASTNullStatement createNullStatement() {
        return new CPPASTNullStatement();
    }

    protected ICPPASTSwitchStatement createSwitchStatement() {
        return new CPPASTSwitchStatement();
    }

    protected ICPPASTIfStatement createIfStatement() {
        return new CPPASTIfStatement();
    }

    @Override
	protected IASTDefaultStatement createDefaultStatement() {
        return new CPPASTDefaultStatement();
    }

    @Override
	protected IASTCaseStatement createCaseStatement() {
        return new CPPASTCaseStatement();
    }

    @Override
	protected IASTExpressionStatement createExpressionStatement() {
        return new CPPASTExpressionStatement();
    }

    @Override
	protected IASTDeclarationStatement createDeclarationStatement() {
        return new CPPASTDeclarationStatement();
    }

    @Override
	protected IASTASMDeclaration createASMDirective() {
        return new CPPASTASMDeclaration();
    }

    @Override
	protected IASTCastExpression createCastExpression() {
        return new CPPASTCastExpression();
    }

    @Override
	protected IASTStatement statement() throws EndOfFileException,
            BacktrackException {

        switch (LT(1)) {
        // labeled statements
        case IToken.t_case:
            return parseCaseStatement();
        case IToken.t_default:
            return parseDefaultStatement();
        // compound statement
        case IToken.tLBRACE:
            return parseCompoundStatement();
        // selection statement
        case IToken.t_if:
            return parseIfStatement();
        case IToken.t_switch:
            return parseSwitchStatement();
        // iteration statements
        case IToken.t_while:
            return parseWhileStatement();
        case IToken.t_do:
            return parseDoStatement();
        case IToken.t_for:
            return parseForStatement();
        // jump statement
        case IToken.t_break:
            return parseBreakStatement();
        case IToken.t_continue:
            return parseContinueStatement();
        case IToken.t_return:
            return parseReturnStatement();
        case IToken.t_goto:
            return parseGotoStatement();
        case IToken.tSEMI:
            return parseNullStatement();
        case IToken.t_try:
            return parseTryStatement();
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
                return parseLabelStatement();
            }

            return parseDeclarationOrExpressionStatement();
        }
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseTryStatement() throws EndOfFileException, BacktrackException {
        int startO = consume().getOffset();
        IASTStatement tryBlock = compoundStatement();
        List catchHandlers = new ArrayList(DEFAULT_CATCH_HANDLER_LIST_SIZE);
        catchHandlerSequence(catchHandlers);
        ICPPASTTryBlockStatement tryStatement = createTryBlockStatement();
        ((ASTNode) tryStatement).setOffset(startO);
        tryStatement.setTryBody(tryBlock);

        for (int i = 0; i < catchHandlers.size(); ++i) {
            ICPPASTCatchHandler handler = (ICPPASTCatchHandler) catchHandlers.get(i);
            tryStatement.addCatchHandler(handler);
            ((ASTNode) tryStatement).setLength(calculateEndOffset(handler) - startO);
        }
        return tryStatement;
    }


    protected ICPPASTTryBlockStatement createTryBlockStatement() {
        return new CPPASTTryBlockStatement();
    }

    @Override
	protected void nullifyTranslationUnit() {
        translationUnit = null;
    }

    @Override
	protected IASTProblemStatement createProblemStatement() {
        return new CPPASTProblemStatement();
    }

    @Override
	protected IASTProblemExpression createProblemExpression() {
        return new CPPASTProblemExpression();
    }

    @Override
	protected IASTProblem createProblem(int signal, int offset, int length) {
        IASTProblem result = new CPPASTProblem(signal, EMPTY_STRING, true);
        ((ASTNode) result).setOffsetAndLength(offset, length);
        ((ASTNode) result).setLength(length);
        return result;
    }

    @Override
	protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTNode while_condition = cppStyleCondition(true);
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement while_body = null;
        if (LT(1) != IToken.tEOC)
            while_body = statement();

        ICPPASTWhileStatement while_statement = (ICPPASTWhileStatement) createWhileStatement();
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
        if (while_condition instanceof IASTExpression) {
            while_statement.setCondition((IASTExpression) while_condition);
        } else if (while_condition instanceof IASTDeclaration) {
            while_statement.setConditionDeclaration((IASTDeclaration) while_condition);
        }
        
        if (while_body != null) {
            while_statement.setBody(while_body);
        }

        return while_statement;

    }

    /**
     * @param expectSemi TODO
     * @return
     */
    protected IASTNode cppStyleCondition(boolean expectSemi) throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        try {
            IASTExpression e = expression();
            if( ! expectSemi )
                return e;
            switch (LT(1)) {
            case IToken.tRPAREN:
            case IToken.tEOC:
                return e;
            default:
                throwBacktrack(LA(1));
            }
            return e;
        } catch (BacktrackException bt) {
            backup(mark);
            try {
                return simpleDeclaration(SimpleDeclarationStrategy.TRY_VARIABLE, true);
            } catch (BacktrackException b) {
                failParse();
                throwBacktrack(b);
                return null;
            }
        }
    }

    private static class EmptyVisitor extends CPPASTVisitor {
        {
            shouldVisitStatements = true;
        }
    }

    private static final EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

    private int functionBodyCount;

    @Override
	protected ASTVisitor createVisitor() {
        return EMPTY_VISITOR;
    }

    @Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
        return new CPPASTAmbiguousStatement();
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
        ICPPASTIfStatement result = null;
        ICPPASTIfStatement if_statement = null;
        int start = LA(1).getOffset();
        if_loop: while (true) {
            int so = consume(IToken.t_if).getOffset();
            consume(IToken.tLPAREN);
            IASTNode condition = null;
            try {
                condition = cppStyleCondition(true); 
                // condition
                if (LT(1) == IToken.tEOC) {
                    // Completing in the condition
                    ICPPASTIfStatement new_if = createIfStatement();
                    if (condition instanceof IASTExpression)
                        new_if.setConditionExpression((IASTExpression) condition);
                    else if (condition instanceof IASTDeclaration)
                        new_if.setConditionDeclaration((IASTDeclaration) condition);

                    if (if_statement != null) {
                        if_statement.setElseClause(new_if);
                    }
                    return result != null ? result : new_if;
                }
                consume(IToken.tRPAREN);
            } catch (BacktrackException b) {
                IASTProblem p = failParse(b);
                IASTProblemExpression ps = createProblemExpression();
                ps.setProblem(p);
                ((ASTNode) ps).setOffsetAndLength(((ASTNode) p).getOffset(), ((ASTNode) p).getLength());
                condition = ps;
                if (LT(1) == IToken.tRPAREN)
                    consume();
                else if (LT(2) == IToken.tRPAREN) {
                    consume();
                    consume();
                } else
                    failParseWithErrorHandling();
            }

            IASTStatement thenClause = statement();

            ICPPASTIfStatement new_if_statement = createIfStatement();
            ((ASTNode) new_if_statement).setOffset(so);
            if (condition != null && (condition instanceof IASTExpression || condition instanceof IASTDeclaration)) 
            	// shouldn't be possible but failure in condition() makes it so
            {
            	if( condition instanceof IASTExpression )
            		new_if_statement.setConditionExpression((IASTExpression) condition);
            	else if( condition instanceof IASTDeclaration )
            		new_if_statement.setConditionDeclaration((IASTDeclaration) condition);
            }
            if (thenClause != null) {
                new_if_statement.setThenClause(thenClause);
                ((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause) - ((ASTNode) new_if_statement).getOffset());
            }
            if (LT(1) == IToken.t_else) {
                consume();
                if (LT(1) == IToken.t_if) {
                    // an else if, don't recurse, just loop and do another if

                    if (if_statement != null) {
                        if_statement.setElseClause(new_if_statement);
                        ((ASTNode) if_statement).setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
                    }
                    if (result == null && if_statement != null)
                        result = if_statement;
                    if (result == null)
                        result = new_if_statement;

                    if_statement = new_if_statement;
                    continue if_loop;
                }
                IASTStatement elseStatement = statement();
                new_if_statement.setElseClause(elseStatement);
                if (if_statement != null) {
                    if_statement.setElseClause(new_if_statement);
                    ((ASTNode) if_statement).setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
                } else {
                    if (result == null && if_statement != null)
                        result = if_statement;
                    if (result == null)
                        result = new_if_statement;
                    if_statement = new_if_statement;
                }
            } else {
                if (thenClause != null)
                    ((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause) - start);
                if (if_statement != null) {
                    if_statement.setElseClause(new_if_statement);
                    ((ASTNode) new_if_statement).setLength(calculateEndOffset(new_if_statement) - start);
                }
                if (result == null && if_statement != null)
                    result = if_statement;
                if (result == null)
                    result = new_if_statement;

                if_statement = new_if_statement;
            }
            break if_loop;
        }

        reconcileLengths(result);
        return result;
    }

    @Override
	protected void checkTokenVsDeclarator(IToken la, IASTDeclarator d) throws FoundDeclaratorException {
        switch (la.getType()) {
        case IToken.tCOLON:
        case IToken.t_try:
            throw new FoundDeclaratorException( d, la );
        default:
            super.checkTokenVsDeclarator(la, d);
        }
    }
    
    @Override
	protected IASTStatement functionBody() throws EndOfFileException, BacktrackException {
        ++functionBodyCount;
        IASTStatement s = super.functionBody();
        --functionBodyCount;
        return s;
    }

    @Override
	protected IASTDeclaration simpleDeclaration() throws BacktrackException, EndOfFileException {
        return simpleDeclarationStrategyUnion();
    }

    protected IASTExpression unaryOperatorCastExpression(int operator) throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        int offset = consume().getOffset();
        IASTExpression castExpression = castExpression();
        if (castExpression instanceof IASTLiteralExpression) {
        	IASTLiteralExpression literal = (IASTLiteralExpression) castExpression;
            if( literal.getKind() != ICPPASTLiteralExpression.lk_this ) {
            	if ( operator == IASTUnaryExpression.op_amper || 
            			(operator == IASTUnaryExpression.op_star && 
            					literal.getKind() != IASTLiteralExpression.lk_string_literal) ) {
            		backup( mark );
            		throwBacktrack( mark );
            	}
            }
        }
        return buildUnaryExpression(operator, castExpression, offset,
                calculateEndOffset(castExpression));
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTNode switch_condition = cppStyleCondition(true);
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement  switch_body = null;
        if (LT(1) != IToken.tEOC)
        	switch_body = statement();
    
        ICPPASTSwitchStatement switch_statement = createSwitchStatement();
        ((ASTNode) switch_statement).setOffsetAndLength(startOffset,
                (switch_body != null ? calculateEndOffset(switch_body) : LA(1).getEndOffset()) - startOffset);
        if( switch_condition instanceof IASTExpression ) {
            switch_statement.setControllerExpression((IASTExpression) switch_condition);
        }
        else if( switch_condition instanceof IASTDeclaration ) {
            switch_statement.setControllerDeclaration((IASTDeclaration) switch_condition);
        }
        
        if (switch_body != null) {
            switch_statement.setBody(switch_body);
        }

        return switch_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTStatement init = forInitStatement();
        IASTNode for_condition = null;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            break;
        default:
            for_condition = cppStyleCondition(false);
        }
        switch (LT(1)) {
        case IToken.tSEMI:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throw backtrack;
        }
        IASTExpression iterationExpression = null;
        switch (LT(1)) {
        case IToken.tRPAREN:
        case IToken.tEOC:
            break;
        default:
            iterationExpression = expression();
        }
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throw backtrack;
        }
        ICPPASTForStatement for_statement = createForStatement();
        IASTStatement for_body = null;
        if (LT(1) != IToken.tEOC) {
            for_body = statement();
            ((ASTNode) for_statement).setOffsetAndLength(startOffset, calculateEndOffset(for_body) - startOffset);
        }
    
        for_statement.setInitializerStatement(init);
        
        if (for_condition != null) {
            if( for_condition instanceof IASTExpression ) {
                for_statement.setConditionExpression((IASTExpression) for_condition);
            }
            else if( for_condition instanceof IASTDeclaration ) {
                for_statement.setConditionDeclaration((IASTDeclaration) for_condition);              
            }
        }
        if (iterationExpression != null) {
            for_statement.setIterationExpression(iterationExpression);
        }
        if (for_body != null) {
            for_statement.setBody(for_body);
        }
        return for_statement;
    }
}
