/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.IFilenameProvider;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.pst.ForewardDeclaredSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDeferredTemplateInstance;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDeclarationSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDirectiveSymbol;
import org.eclipse.cdt.internal.core.parser.pst.NamespaceSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TemplateSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension.ExtensionException;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;


/**
 * @author jcamelon
 * 
 * The CompleteParseASTFactory class creates a complete AST 
 * for a given parsed code. 
 *
 */
public class CompleteParseASTFactory extends BaseASTFactory implements IASTFactory
{
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final static List SUBSCRIPT;
    private final static IProblemFactory problemFactory = new ASTProblemFactory();
	private final IFilenameProvider fileProvider;
	private final IASTFactoryExtension extension;
	
    static 
    {
    	SUBSCRIPT = new ArrayList();
    	SUBSCRIPT.add( TypeInfo.OperatorExpression.subscript );
    }

    static private class LookupType extends Enum {
		public static final LookupType QUALIFIED = new LookupType( 1 );
		public static final LookupType UNQUALIFIED = new LookupType( 2 );
		public static final LookupType FORDEFINITION = new LookupType( 3 );
		public static final LookupType FORFRIENDSHIP = new LookupType( 4 );
		public static final LookupType FORPARENTSCOPE = new LookupType( 5 );

		private LookupType( int constant)
		{
			super( constant ); 
		}
    }
    
    public CompleteParseASTFactory( IFilenameProvider filenameProvider, ParserLanguage language, ParserMode mode, IASTFactoryExtension extension )
    {
        super();
		pst = new ParserSymbolTable( language, mode );
		fileProvider = filenameProvider;
		this.extension = extension;
    }

	/*
	 * Adds a reference to a reference list
	 * Overrides an existing reference if it has the same name and offset
	 */
	protected void addReference(List references, IASTReference reference){
		if( references == null || reference == null ) return;
		Iterator i = references.iterator();
		while (i.hasNext()){
			IASTReference ref = (IASTReference)i.next();
			if (ref != null){
				if( (ref.getName().equals(reference.getName()))
				&& (ref.getOffset() == reference.getOffset())
				){
					i.remove();
					break; 
				}
			}
		}
		references.add(reference);
	}
	
	protected void addTemplateIdReferences( List references, List templateArgs ){
		if( templateArgs == null )
			return;
		
		Iterator i = templateArgs.iterator();
		while( i.hasNext() ){
			ASTExpression exp = (ASTExpression) i.next();
			Iterator j = null;
			if( exp.getExpressionKind() == IASTExpression.Kind.POSTFIX_TYPEID_TYPEID )
				j = ((ASTTypeId) exp.getTypeId()).getReferences().iterator();
			else
				j = exp.getReferences().iterator();
			while( j.hasNext() ){
				addReference( references, (IASTReference) j.next() );
			}
		}
	}
	/*
	 * Test if the provided list is a valid parameter list
	 * Parameters are list of TypeInfos
	 */
	protected boolean validParameterList(List parameters){
		Iterator i = parameters.iterator();
		while (i.hasNext()){
			TypeInfo info = (TypeInfo)i.next();
			if (info != null){
				if((info.getType() == TypeInfo.t_type) 
					&& (info.getTypeSymbol() == null))
					return false;
			}else
				return false;
		}
		return true;
	}
	
	private ISymbol lookupElement (IContainerSymbol startingScope, String name, TypeInfo.eType type, List parameters, LookupType lookupType ) throws ASTSemanticException {
		return lookupElement( startingScope, name, type, parameters, null, lookupType );
	}
	
	private ISymbol lookupElement (IContainerSymbol startingScope, String name, TypeInfo.eType type, List parameters, List arguments, LookupType lookupType ) throws ASTSemanticException {
		ISymbol result = null;
		if( startingScope == null ) return null;
		try {
			if((type == TypeInfo.t_function) || (type == TypeInfo.t_constructor)){
				// looking for a function
				if(validParameterList(parameters))
					if(type == TypeInfo.t_constructor){
						IDerivableContainerSymbol startingDerivableScope = (IDerivableContainerSymbol) startingScope;
						result = startingDerivableScope.lookupConstructor( new LinkedList(parameters));
					}
					else {
						if( arguments != null )
							result = startingScope.lookupFunctionTemplateId( name, new LinkedList( parameters), new LinkedList( arguments ), ( lookupType == LookupType.FORDEFINITION ) );
						else if( lookupType == LookupType.QUALIFIED )
							result = startingScope.qualifiedFunctionLookup(name, new LinkedList(parameters));
						else if( lookupType == LookupType.UNQUALIFIED || lookupType == LookupType.FORPARENTSCOPE)
							result = startingScope.unqualifiedFunctionLookup( name, new LinkedList( parameters ) );
						else if( lookupType == LookupType.FORDEFINITION )
							result = startingScope.lookupMethodForDefinition( name, new LinkedList( parameters ) );
						else if( lookupType == LookupType.FORFRIENDSHIP ){
							result = ((IDerivableContainerSymbol)startingScope).lookupFunctionForFriendship( name, new LinkedList( parameters) );
						}
					}
				else
					result = null;
			}else{
				// looking for something else
				if( arguments != null )
					result = startingScope.lookupTemplateId( name, arguments );
				else if( lookupType == LookupType.QUALIFIED )
					result = startingScope.qualifiedLookup(name, type);
				else if( lookupType == LookupType.UNQUALIFIED || lookupType == LookupType.FORPARENTSCOPE )
					result = startingScope.elaboratedLookup( type, name );
				else if( lookupType == LookupType.FORDEFINITION )
					result = startingScope.lookupMemberForDefinition( name );
				else if( lookupType == LookupType.FORFRIENDSHIP )
					result = ((IDerivableContainerSymbol)startingScope).lookupForFriendship( name );
			}
		} catch (ParserSymbolTableException e) {
			if( e.reason != ParserSymbolTableException.r_UnableToResolveFunction )
				handleProblem( e.createProblemID(), name );
		} catch (ParserSymbolTableError e){
			handleProblem( IProblem.INTERNAL_RELATED, name );
		}
		return result;		
	}
	
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, String name, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, TypeInfo.t_any, null, 0, references, throwOnError, lookup );
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, String name, TypeInfo.eType type, List parameters, int offset, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException
	{
		ISymbol result = null;
		if( name == null && throwOnError )
			handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
		else if( name == null ) return null;
			
		try
		{
			result = lookupElement(startingScope, name, type, parameters, lookup);
			if( result != null ) 
				addReference(references, createReference( result, name, offset ));
			else if( throwOnError )
				handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, name );    
		}
		catch (ASTSemanticException e)
		{
			if( throwOnError )
				throw new ASTSemanticException( e );
			
			return null;
		}
		return result;			
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, List references, boolean throwOnError ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, references, throwOnError, LookupType.UNQUALIFIED);
	}
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, TypeInfo.t_any, null, references, throwOnError, lookup );
	}
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, TypeInfo.eType type, List parameters, List references, boolean throwOnError ) throws ASTSemanticException{
		return lookupQualifiedName( startingScope, name, type, parameters, references, throwOnError, LookupType.UNQUALIFIED );
	}
	
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, TypeInfo.eType type, List parameters, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException
	{
		ISymbol result = null;
		IToken firstSymbol = null;
		if( name == null && throwOnError ) handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
		else if( name == null ) return null;
		
		List [] templateArgLists = name.getTemplateIdArgLists();
		int idx = 0;
		switch( name.length() )
		{
			case 0: 
				if( throwOnError )
					handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
				else
					return null;
			case 1:
				firstSymbol = name.getFirstToken();
				result = lookupElement(startingScope, firstSymbol.getImage(), type, parameters, lookup );
                if( result != null )
                {
                	if( lookup == LookupType.FORPARENTSCOPE && startingScope instanceof ITemplateFactory ){
                		((ITemplateFactory)startingScope).pushSymbol( result );
                	}
					addReference( references, createReference( result, firstSymbol.getImage(), firstSymbol.getOffset() ));
                }
				else
				{	
					if( startingScope.getASTExtension().getPrimaryDeclaration() instanceof IASTCodeScope )
					{
						if( ((IASTCodeScope) startingScope.getASTExtension().getPrimaryDeclaration()).getContainingFunction() instanceof IASTMethod )
						{
							IASTClassSpecifier classSpecifier = ((IASTMethod) ((IASTCodeScope) startingScope.getASTExtension().getPrimaryDeclaration()).getContainingFunction()).getOwnerClassSpecifier();
							if( classSpecifier != null )
								((ASTClassSpecifier)classSpecifier).addUnresolvedReference( new UnresolvedReferenceDuple( startingScope, name ));
							break;
						}
					}
					if ( throwOnError )
						handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, firstSymbol.getImage(), -1, -1, firstSymbol.getLineNumber() );
					return null;
				}
                break;
			default:
				Iterator iter = name.iterator();
				firstSymbol = name.getFirstToken();
				result = startingScope;
				if( firstSymbol.getType() == IToken.tCOLONCOLON )
					result = pst.getCompilationUnit();
				
				while( iter.hasNext() )
				{
					IToken t = (IToken)iter.next();
					if( t.getType() == IToken.tCOLONCOLON ){
						idx++;
						continue;
					}
					if( t.isPointer() ) break;
					
					String image = t.getImage();
					int offset = t.getOffset();
					
					if( templateArgLists != null && templateArgLists[ idx ] != null ){
						if( iter.hasNext() && t.getNext().getType() == IToken.tLT )
							t = name.consumeTemplateIdArguments( (IToken) iter.next(), iter );
					}
					
					try
					{
						if( result instanceof IDeferredTemplateInstance ){
							result = ((IDeferredTemplateInstance)result).getTemplate().getTemplatedSymbol();
						}
						
						if( t == name.getLastToken() ) 
							if( templateArgLists != null )
								result = lookupElement((IContainerSymbol)result, image, type, parameters, getTemplateArgList( templateArgLists[idx] ), ( lookup == LookupType.FORDEFINITION ) ? lookup : LookupType.QUALIFIED );
							else
								result = lookupElement((IContainerSymbol)result, image, type, parameters, ( lookup == LookupType.FORDEFINITION ) ? lookup : LookupType.QUALIFIED );
						else
							if( templateArgLists != null && templateArgLists[idx] != null )
								result = ((IContainerSymbol)result).lookupTemplateId( image, getTemplateArgList( templateArgLists[idx] ) );
							else
								result = ((IContainerSymbol)result).lookupNestedNameSpecifier( image );
						
						if( result != null ){
		                	if( lookup == LookupType.FORPARENTSCOPE && startingScope instanceof ITemplateFactory ){
		                		if( templateArgLists != null && templateArgLists[idx] != null )
		                			((ITemplateFactory)startingScope).pushTemplateId( result, getTemplateArgList( templateArgLists[idx] ) );
		                		else 
		                			((ITemplateFactory)startingScope).pushSymbol( result );
		                	}
							addReference( references, createReference( result, image, offset ));
							if( templateArgLists != null && templateArgLists[idx] != null )
								addTemplateIdReferences( references, templateArgLists[idx] );
						}
						else
							break;
					}
					catch( ParserSymbolTableException pste )
					{
						if ( throwOnError )
							handleProblem( pste.createProblemID(), image );
						return null;
					}
					catch( ParserSymbolTableError e )
					{
						if( throwOnError )
							handleProblem( IProblem.INTERNAL_RELATED, image );
						return null;
					}
				}
		}
		return result;
	}

	protected IToken consumeTemplateIdArguments( IToken name, Iterator iter ){
	    IToken token = name;
	    if( token.getNext().getType() == IToken.tLT )
	    {
	    	token = (IToken) iter.next();
	    	Stack scopes = new Stack();
	        scopes.push(new Integer(IToken.tLT));
	        
	        while (!scopes.empty())
	        {
	        	int top;
	        	
	        	token = (IToken) iter.next();
	        	switch( token.getType() ){
	        		case IToken.tGT:
	        			if (((Integer)scopes.peek()).intValue() == IToken.tLT) {
							scopes.pop();
						}
	                    break;
	        		case IToken.tRBRACKET :
						do {
							top = ((Integer)scopes.pop()).intValue();
						} while (!scopes.empty() && (top == IToken.tGT || top == IToken.tLT));
						//if (top != IToken.tLBRACKET) throw backtrack;
						break;
	        		case IToken.tRPAREN :
						do {
							top = ((Integer)scopes.pop()).intValue();
						} while (!scopes.empty() && (top == IToken.tGT || top == IToken.tLT));
						//if (top != IToken.tLPAREN) throw backtrack;
							
						break;
	                case IToken.tLT :
					case IToken.tLBRACKET:
					case IToken.tLPAREN:
						scopes.push(new Integer(token.getType()));
	                    break;
					
	        	}
	        }
	    }
	   
	    return token;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple, int, int)
     */
    public IASTUsingDirective createUsingDirective(
        IASTScope scope,
        ITokenDuple duple,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine)
        throws ASTSemanticException
    {		
		List references = new ArrayList();	
		ISymbol symbol = lookupQualifiedName( 
			scopeToSymbol( scope), duple, references, true ); 

		
		IUsingDirectiveSymbol usingDirective = null;
		if( symbol != null )
			try {
				usingDirective = ((ASTScope)scope).getContainerSymbol().addUsingDirective( (IContainerSymbol)symbol );
			} catch (ParserSymbolTableException pste) {
				handleProblem( pste.createProblemID(), duple.toString(), startingOffset, endingOffset, startingLine );
			}
		
		return new ASTUsingDirective( scopeToSymbol(scope), usingDirective, startingOffset, startingLine, endingOffset, endingLine, references );
    }
    

    protected IContainerSymbol getScopeToSearchUpon(
        IASTScope currentScope,
        IToken firstToken, Iterator iterator ) 
    {
		if( firstToken.getType() == IToken.tCOLONCOLON )  
		{ 
			iterator.next();
			return pst.getCompilationUnit();
		}
		else
		{
			return scopeToSymbol(currentScope);
		}
        	
        
    }
    protected IContainerSymbol scopeToSymbol(IASTScope currentScope)
    {
    	if( currentScope instanceof ASTScope )
        	return ((ASTScope)currentScope).getContainerSymbol();
    	else if ( currentScope instanceof ASTTemplateDeclaration )
    		return ((ASTTemplateDeclaration)currentScope).getContainerSymbol();
    	else if ( currentScope instanceof ASTTemplateInstantiation )
    		return ((ASTTemplateInstantiation)currentScope).getContainerSymbol();
        else
        	return scopeToSymbol(((ASTAnonymousDeclaration)currentScope).getOwnerScope());
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.core.parser.ITokenDuple, int, int)
     */
    public IASTUsingDeclaration createUsingDeclaration(
        IASTScope scope,
        boolean isTypeName,
        ITokenDuple name,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine) throws ASTSemanticException
    {
        List references = new ArrayList(); 
        
        IUsingDeclarationSymbol endResult = null;
        
        if(name.getSegmentCount() > 1)
        {
        	ITokenDuple duple = name.getLeadingSegments();
        	ISymbol symbol = lookupQualifiedName( scopeToSymbol(scope), duple, references, true );
        	IContainerSymbol containerSymbol = null;
        	if( symbol instanceof IContainerSymbol )
        		containerSymbol = (IContainerSymbol) symbol;
        	else if ( symbol instanceof IDeferredTemplateInstance )
        		containerSymbol = ((IDeferredTemplateInstance)symbol).getTemplate().getTemplatedSymbol();
	        try
	        {
	            endResult = scopeToSymbol(scope).addUsingDeclaration( name.getLastToken().getImage(), containerSymbol );
	        }
	        catch (ParserSymbolTableException e)
	        {
	        	handleProblem(e.createProblemID(), name.getLastToken().getImage(), startingOffset, endingOffset, startingLine );
	        }
        } else
			try {
				endResult = scopeToSymbol(scope).addUsingDeclaration(name.getLastToken().getImage());
			} catch (ParserSymbolTableException e) {
				handleProblem(e.createProblemID(), name.getLastToken().getImage(), startingOffset, endingOffset, startingLine );
	        }
        
			if( endResult != null )
			{
				Iterator i = endResult.getReferencedSymbols().iterator();
				while( i.hasNext() )
				{
					IASTReference reference = createReference( (ISymbol) i.next(), name.getLastToken().getImage(), name.getLastToken().getOffset() );
					addReference( references, reference );
				}
			}

        return new ASTUsingDeclaration( scope, name.getLastToken().getImage(),
        	endResult.getReferencedSymbols(), isTypeName, startingOffset, startingLine, endingOffset, endingLine, references );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTASMDefinition createASMDefinition(
        IASTScope scope,
        String assembly,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine)
    {
        return new ASTASMDefinition( scopeToSymbol(scope), assembly, startingOffset, startingLine, endingOffset, endingLine);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTNamespaceDefinition createNamespaceDefinition(
        IASTScope scope,
        String identifier,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLineNumber) throws ASTSemanticException
    {
    	
    	IContainerSymbol pstScope = scopeToSymbol(scope);
    	ISymbol namespaceSymbol  = null; 
    	
    	if( ! identifier.equals( EMPTY_STRING ) ) 
    	{
	    	try
	        {
	            namespaceSymbol = pstScope.qualifiedLookup( identifier );
	        }
	        catch (ParserSymbolTableException e)
	        {
	        	handleProblem( e.createProblemID(), identifier, nameOffset, nameEndOffset, nameLineNumber );
	        }
    	}
        
        if( namespaceSymbol != null )
        {
        	if( namespaceSymbol.getType() != TypeInfo.t_namespace )
        		handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, identifier, nameOffset, nameEndOffset, nameLineNumber );
        }
        else
        {
        	namespaceSymbol = pst.newContainerSymbol( identifier, TypeInfo.t_namespace );
        	if( identifier.equals( EMPTY_STRING ) ) 
        		namespaceSymbol.setContainingSymbol( pstScope );	
        	else
        	{
	        	
	        	try
	            {
	                pstScope.addSymbol( namespaceSymbol );
	            }
	            catch (ParserSymbolTableException e1)
	            {
//	            	assert false : e1;
	            }
        	}
        }
        
        ASTNamespaceDefinition namespaceDef = new ASTNamespaceDefinition( namespaceSymbol, startingOffset, startingLine, nameOffset, nameEndOffset, nameLineNumber);
        attachSymbolExtension( namespaceSymbol, namespaceDef, true );
        return namespaceDef;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createCompilationUnit()
     */
    public IASTCompilationUnit createCompilationUnit()
    {
    	ISymbol symbol = pst.getCompilationUnit();
    	ASTCompilationUnit compilationUnit = new ASTCompilationUnit( symbol );
        attachSymbolExtension(symbol, compilationUnit, true );
    	return compilationUnit; 
    }
    
    
	protected void attachSymbolExtension(
		ISymbol symbol,
		ASTSymbol astSymbol, boolean asDefinition )
	{
		ISymbolASTExtension extension = symbol.getASTExtension();
		if( extension == null )
		{
			if( astSymbol instanceof IASTNamespaceDefinition ) 
				extension = new NamespaceSymbolExtension( symbol, astSymbol );
			else if( astSymbol instanceof IASTFunction || astSymbol instanceof IASTMethod || 
					astSymbol instanceof IASTEnumerationSpecifier || 
					astSymbol instanceof IASTClassSpecifier || 
					astSymbol instanceof IASTElaboratedTypeSpecifier )
			{
				extension = new ForewardDeclaredSymbolExtension( symbol, astSymbol );
			}
			else if( astSymbol instanceof IASTTemplateDeclaration ){
				extension = new TemplateSymbolExtension( symbol, astSymbol );
			}
			else 
			{
				extension = new StandardSymbolExtension( symbol, astSymbol );
			}
			symbol.setASTExtension( extension );
		}
		else
		{
			if( asDefinition )
				try {
					extension.addDefinition( astSymbol );
				} catch (ExtensionException e) {
//					assert false : ExtensionException.class;
				}
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int)
     */
    public IASTLinkageSpecification createLinkageSpecification(
        IASTScope scope,
        String spec,
        int startingOffset, int startingLine)
    {
        return new ASTLinkageSpecification( scopeToSymbol( scope ), spec, startingOffset, startingLine );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ASTClassKind, org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int)
     */
    public IASTClassSpecifier createClassSpecifier(
        IASTScope scope,
        ITokenDuple name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine) throws ASTSemanticException
    {
        IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
        TypeInfo.eType pstType = classKindToTypeInfo(kind);		
		List references = new ArrayList();

		String newSymbolName = EMPTY_STRING; 
		List templateIdArgList = null;
		boolean isTemplateId = false;
		
		if( name != null ){
			IToken nameToken = null;
			if( name.getSegmentCount() != 1 ) // qualified name 
			{
				ITokenDuple containerSymbolName = name.getLeadingSegments(); 
				ISymbol temp = lookupQualifiedName( currentScopeSymbol, containerSymbolName, references, true);
				if( temp instanceof IDeferredTemplateInstance )
					currentScopeSymbol = ((IDeferredTemplateInstance)temp).getTemplate().getTemplatedSymbol();
				else
					currentScopeSymbol = (IContainerSymbol) temp;
				if( currentScopeSymbol == null )
				 	handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, containerSymbolName.toString(), containerSymbolName.getFirstToken().getOffset(), containerSymbolName.getLastToken().getEndOffset(), containerSymbolName.getLastToken().getLineNumber() );	
				 
				nameToken = name.getLastSegment().getFirstToken();
			} else {
				nameToken = name.getFirstToken();
			}
			//template-id
			List [] array = name.getTemplateIdArgLists();
			if( array != null ){
				templateIdArgList = array[ array.length - 1 ];
				isTemplateId = (templateIdArgList != null);
			}
				 
			newSymbolName = nameToken.getImage();
		}
		ISymbol classSymbol = null;
		if( !newSymbolName.equals(EMPTY_STRING) && !isTemplateId ){ 
			try
			{
				classSymbol = currentScopeSymbol.lookupMemberForDefinition(newSymbolName);
			}
			catch (ParserSymbolTableException e)
			{
				handleProblem(IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED, name.toString(), nameOffset, nameEndOffset, nameLine);
			}
	        
			if( classSymbol != null && ! classSymbol.isForwardDeclaration() )
				handleProblem( IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED, newSymbolName, nameOffset, nameEndOffset, nameLine );  
			
			if( classSymbol != null && classSymbol.getType() != pstType )
				handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, newSymbolName, nameOffset, nameEndOffset, nameLine );
		}

		IDerivableContainerSymbol newSymbol = pst.newDerivableContainerSymbol( newSymbolName, pstType );
		
		if( classSymbol != null )
			classSymbol.setTypeSymbol( newSymbol );
			
		List args = null;
		if( isTemplateId ){
			args = getTemplateArgList( templateIdArgList );
		}
		try
        {
			if( !isTemplateId )
				currentScopeSymbol.addSymbol( newSymbol );
			else 
				currentScopeSymbol.addTemplateId( newSymbol, args );
        }
        catch (ParserSymbolTableException e2)
        {
        	handleProblem( e2.createProblemID(), newSymbolName );
        }
			
        ASTClassSpecifier classSpecifier = new ASTClassSpecifier( newSymbol, kind, type, access, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references );
        attachSymbolExtension(newSymbol, classSpecifier, true );
        return classSpecifier;
    }
    
	private List getTemplateArgList( List args ){
		if( args == null )
			return null;
		
		List list = new LinkedList();
		Iterator iter = args.iterator();
		
		ASTExpression exp;
		while( iter.hasNext() )
		{
			exp = (ASTExpression) iter.next();
			
			TypeInfo info = exp.getResultType().getResult();
			
			list.add( info );
		}
			
		return list;
	}
	
    protected void handleProblem( int id, String attribute ) throws ASTSemanticException
	{
    	handleProblem( null, id, attribute, -1, -1, -1 );  //TODO make this right
    }

    protected void handleProblem( IASTScope scope, int id, String attribute ) throws ASTSemanticException
	{
    	handleProblem( scope, id, attribute, -1, -1, -1 );
	}
    
    protected void handleProblem( int id, String attribute, int startOffset, int endOffset, int lineNumber) throws ASTSemanticException {
    	handleProblem( null, id, attribute, startOffset, endOffset, lineNumber );
    }
    
	/**
	 * @param id
	 * @param attribute
	 * @param startOffset
	 * @param endOffset
	 * @param lineNumber
	 * @throws ASTSemanticException
	 */
	protected void handleProblem( IASTScope scope, int id, String attribute, int startOffset, int endOffset, int lineNumber) throws ASTSemanticException {			
		IProblem p = problemFactory.createProblem( id, 
				startOffset, endOffset, lineNumber, fileProvider.getCurrentFilename(), attribute, false, true );
		
		TraceUtil.outputTrace(logService, "CompleteParseASTFactory - IProblem : ", p, null, null, null ); //$NON-NLS-1$
		
		if( shouldThrowException( scope, id ) ) 
			throw new ASTSemanticException(p);
	}

	protected boolean shouldThrowException( IASTScope scope, int id ){
		if( scope != null ){
			IContainerSymbol symbol = scopeToSymbol( scope );
			if( symbol.isTemplateMember() ){
				if( id == IProblem.SEMANTIC_INVALID_CONVERSION_TYPE ||
					id == IProblem.SEMANTIC_INVALID_TYPE )
				{
					return false;
				}
			}
		}
		return true;
	}
	protected TypeInfo.eType classKindToTypeInfo(ASTClassKind kind)
    {
        TypeInfo.eType pstType = null;
        
        if( kind == ASTClassKind.CLASS )
        	pstType = TypeInfo.t_class;
        else if( kind == ASTClassKind.STRUCT )
        	pstType = TypeInfo.t_struct;
        else if( kind == ASTClassKind.UNION )
        	pstType = TypeInfo.t_union;
        else if( kind == ASTClassKind.ENUM )
            pstType = TypeInfo.t_enumeration;
//        else
//        	assert false : kind ;
        return pstType;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, java.lang.String)
     */
    public void addBaseSpecifier(
        IASTClassSpecifier astClassSpec,
        boolean isVirtual,
        ASTAccessVisibility visibility,
        ITokenDuple parentClassName) throws ASTSemanticException 
    {
    	IDerivableContainerSymbol classSymbol = (IDerivableContainerSymbol)scopeToSymbol( astClassSpec);
        Iterator iterator = null; 
        List references = new ArrayList(); 
        
        if( parentClassName != null )
        {
        	iterator = parentClassName.iterator();
        	if( !iterator.hasNext() )
               	handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
        }
        else
           	handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
        	 
        //Its possible that the parent is not an IContainerSymbol if its a template parameter or some kinds of template instances
		ISymbol symbol = lookupQualifiedName( classSymbol, parentClassName, references, true );
		classSymbol.addParent( symbol, isVirtual, visibility, parentClassName.getFirstToken().getOffset(), references );
		 
    }
    /**
     * @param symbol
     * @param referenceElementName
     * @return
     */
    protected IASTReference createReference(ISymbol symbol, String referenceElementName, int offset ) throws ASTSemanticException 
    {
    	if( symbol.getASTExtension() == null ){
    		//referenced symbol doesn't have an attached AST node, could happen say for the copy constructor added 
    		//by the symbol table.
    		return null;
    	}
    	
//    	assert (symbol != null ) : "createReference cannot be called on null symbol ";
    	if( symbol.getTypeInfo().checkBit( TypeInfo.isTypedef ) ||
		    symbol.getASTExtension().getPrimaryDeclaration() instanceof IASTTypedefDeclaration )
			return new ASTTypedefReference( offset, referenceElementName, (IASTTypedefDeclaration)symbol.getASTExtension().getPrimaryDeclaration());
        else if( symbol.getType() == TypeInfo.t_namespace )
        	return new ASTNamespaceReference( offset, referenceElementName, (IASTNamespaceDefinition)symbol.getASTExtension().getPrimaryDeclaration());
        else if( symbol.getType() == TypeInfo.t_class || 
				 symbol.getType() == TypeInfo.t_struct || 
				 symbol.getType() == TypeInfo.t_union ) 
			return new ASTClassReference( offset, referenceElementName, (IASTTypeSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		else if( symbol.getType() == TypeInfo.t_enumeration )
			return new ASTEnumerationReference( offset, referenceElementName,  (IASTEnumerationSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		else if( symbol.getType() == TypeInfo.t_enumerator )
			return new ASTEnumeratorReference( offset, referenceElementName, (IASTEnumerator)symbol.getASTExtension().getPrimaryDeclaration() );
		else if(( symbol.getType() == TypeInfo.t_function ) || (symbol.getType() == TypeInfo.t_constructor))
		{
			ASTSymbol referenced = symbol.getASTExtension().getPrimaryDeclaration(); 
			if( referenced instanceof IASTMethod )
				return new ASTMethodReference( offset, referenceElementName, (IASTMethod)referenced ); 
			else
				return new ASTFunctionReference( offset, referenceElementName, (IASTFunction)referenced );
		}
		else if( ( symbol.getType() == TypeInfo.t_type ) || 
				( symbol.getType() == TypeInfo.t_bool )||
				( symbol.getType() == TypeInfo.t_char  ) ||     
				( symbol.getType() == TypeInfo.t_wchar_t )||
				( symbol.getType() == TypeInfo.t_int )   ||
				( symbol.getType() == TypeInfo.t_float )||
				( symbol.getType() == TypeInfo.t_double ) ||    
				( symbol.getType() == TypeInfo.t_void ) ||
				( symbol.getType() == TypeInfo.t__Bool) ||
				( symbol.getType() == TypeInfo.t_templateParameter ) )
			
		{
			if( symbol.getContainingSymbol().getType() == TypeInfo.t_class || 
				symbol.getContainingSymbol().getType() == TypeInfo.t_struct || 
				symbol.getContainingSymbol().getType() == TypeInfo.t_union )
			{
				return new ASTFieldReference( offset, referenceElementName, (IASTField)symbol.getASTExtension().getPrimaryDeclaration());
			}
			else if( ( 	symbol.getContainingSymbol().getType() == TypeInfo.t_function || 
						symbol.getContainingSymbol().getType() == TypeInfo.t_constructor ) && 
				symbol.getContainingSymbol() instanceof IParameterizedSymbol && 
				((IParameterizedSymbol)symbol.getContainingSymbol()).getParameterList() != null && 
				((IParameterizedSymbol)symbol.getContainingSymbol()).getParameterList().contains( symbol ) )
			{
				return new ASTParameterReference( offset, referenceElementName, (IASTParameterDeclaration)symbol.getASTExtension().getPrimaryDeclaration() );
			}
			else
			{
				ASTSymbol s = symbol.getASTExtension().getPrimaryDeclaration();
				if(s instanceof IASTVariable)
					return new ASTVariableReference( offset, referenceElementName, (IASTVariable)s);
				else if (s instanceof IASTParameterDeclaration)
					return new ASTParameterReference( offset, referenceElementName, (IASTParameterDeclaration)s);
				else if (s instanceof IASTTemplateParameter )
					return new ASTTemplateParameterReference( offset, referenceElementName, (IASTTemplateParameter)s );
			}
		}
//		assert false : "Unreachable code : createReference()";
		return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        String name,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine) throws ASTSemanticException
    {
		IContainerSymbol containerSymbol = scopeToSymbol(scope);
		TypeInfo.eType pstType = TypeInfo.t_enumeration;
			
		IDerivableContainerSymbol classSymbol = pst.newDerivableContainerSymbol( name, pstType );
		try
		{
			containerSymbol.addSymbol( classSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem( e.createProblemID(), name );
		}
        
        ASTEnumerationSpecifier enumSpecifier = new ASTEnumerationSpecifier( classSymbol, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine  );
		
		attachSymbolExtension(classSymbol, enumSpecifier, true );
		return enumSpecifier;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public void addEnumerator(
        IASTEnumerationSpecifier enumeration,
        String name,
        int startingOffset,
        int startingLine,
        int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endLine, IASTExpression initialValue) throws ASTSemanticException
    {
        IContainerSymbol enumerationSymbol = (IContainerSymbol)((ISymbolOwner)enumeration).getSymbol();
        
        ISymbol enumeratorSymbol = pst.newSymbol( name, TypeInfo.t_enumerator );
        try
        {
            enumerationSymbol.addSymbol( enumeratorSymbol );
        }
        catch (ParserSymbolTableException e1)
        {
        	if( e1.reason == ParserSymbolTableException.r_InvalidOverload )
        		handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, name, startingOffset, endingOffset, startingLine );
//			assert false : e1;
        }
        ASTEnumerator enumerator = new ASTEnumerator( enumeratorSymbol, enumeration, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endLine, initialValue ); 
        ((ASTEnumerationSpecifier)enumeration).addEnumerator( enumerator );
        attachSymbolExtension( enumeratorSymbol, enumerator, true );
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor)
     */
    public IASTExpression createExpression(
        IASTScope scope,
        Kind kind,
        IASTExpression lhs,
        IASTExpression rhs,
        IASTExpression thirdExpression,
        IASTTypeId typeId,
        ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) throws ASTSemanticException
    {
    	if( idExpression != null )
    	{
    		TraceUtil.outputTrace(
    				logService,
    				"Entering createExpression with Kind=", //$NON-NLS-1$
					null,
					kind.getKindName(),
					" idexpression=", //$NON-NLS-1$
					idExpression.toString()
    				);
    	}
    	else if( literal != null && !literal.equals( EMPTY_STRING )) 
    	{
       		TraceUtil.outputTrace(
       				logService,
    				"Entering createExpression with Kind=", //$NON-NLS-1$
					null,
					kind.getKindName(),
					" literal=", //$NON-NLS-1$
					literal
    				);
    	}
    	
    	List references = new ArrayList(); 
		ISymbol symbol = getExpressionSymbol(scope, kind, lhs, rhs, idExpression, references );
        
        // Try to figure out the result that this expression evaluates to
		ExpressionResult expressionResult = getExpressionResultType(scope, kind, lhs, rhs, thirdExpression, typeId, literal, symbol);
			
		// expression results could be empty, but should not be null
//		assert expressionResult != null  : expressionResult; //throw new ASTSemanticException();
			
		// create the ASTExpression	
		ASTExpression expression =  new ASTExpression( kind, lhs, rhs, thirdExpression, 
										typeId,	idExpression, literal, newDescriptor, references);
			
		// Assign the result to the created expression										
		expression.setResultType (expressionResult);

		return expression;			
    }
    /*
     * Try and dereference the symbol in the expression
     */
    private ISymbol getExpressionSymbol(
		    IASTScope scope, 
		    Kind kind, 
		    IASTExpression lhs,
			IASTExpression rhs,
			ITokenDuple idExpression, 
			List references )throws ASTSemanticException
	{
    	ISymbol symbol = null;
		IContainerSymbol startingScope = scopeToSymbol( scope );
	    	    	
		//If the expression has an id, look up id and add it to references	
		if( idExpression != null )
			symbol = lookupQualifiedName( startingScope, idExpression, references, false );
	        
		// If the expression is lookup symbol if it is in the scope of a type after a "." or an "->"
		IContainerSymbol searchScope = getSearchScope(kind, lhs, startingScope);
		if ( searchScope != null && !searchScope.equals(startingScope))
			symbol = lookupQualifiedName(searchScope, ((ASTExpression)rhs).getIdExpressionTokenDuple(), references, false, LookupType.QUALIFIED );
			    			
		// get symbol if it is the "this" pointer
		// go up the scope until you hit a class
		if (kind == IASTExpression.Kind.PRIMARY_THIS){
			try{
				symbol = startingScope.lookup("this"); //$NON-NLS-1$
			}catch (ParserSymbolTableException e){
				handleProblem( e.createProblemID(), "this"); //$NON-NLS-1$
			}
		}
		// lookup symbol if it is a function call
		if (kind == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){ 
			ITokenDuple functionId =  getFunctionId(lhs); 
			IContainerSymbol functionScope = getSearchScope(lhs.getExpressionKind(), lhs.getLHSExpression(), startingScope);    

			if( functionScope == null )
				return null;
			
			ExpressionResult expResult = ((ASTExpression)rhs).getResultType();
			List parameters = null;
			if(expResult instanceof ExpressionResultList){
				ExpressionResultList expResultList = (ExpressionResultList) expResult;
				parameters = expResultList.getResultList();
								 
			}else {
				parameters = new ArrayList();
				parameters.add(expResult.getResult());
			}
			if( functionScope.equals( startingScope ) )
				symbol = lookupQualifiedName(functionScope, functionId, TypeInfo.t_function, parameters, references, false);
			else
				symbol = lookupQualifiedName(functionScope, functionId, TypeInfo.t_function, parameters, references, false, LookupType.QUALIFIED );
		}
		
    	return symbol;
    }
    /*
     * Returns the function ID token
     */
	private ITokenDuple getFunctionId (IASTExpression expression){
		if(expression.getExpressionKind().isPostfixMemberReference() ) 
			return ((ASTExpression)expression.getRHSExpression()).getIdExpressionTokenDuple();
		else 
			return ((ASTExpression)expression).getIdExpressionTokenDuple();
	}

    private IContainerSymbol getSearchScope (Kind kind, IASTExpression lhs, IContainerSymbol startingScope) throws ASTSemanticException{
		if((kind == IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION) 
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS)
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP)		
		|| (kind == IASTExpression.Kind.PM_DOTSTAR)
		|| (kind == IASTExpression.Kind.PM_ARROWSTAR)
		){
			TypeInfo lhsInfo = ((ASTExpression)lhs).getResultType().getResult();
			if(lhsInfo != null){
				TypeInfo info = null;
				try{
					info = lhsInfo.getFinalType();
				} catch ( ParserSymbolTableError e ){
					return null;
				}
				ISymbol containingScope = info.getTypeSymbol();
//				assert containingScope != null : "Malformed Expression";	
				if( containingScope instanceof IDeferredTemplateInstance )
					return ((IDeferredTemplateInstance) containingScope).getTemplate().getTemplatedSymbol();
				return ( containingScope instanceof IContainerSymbol ) ? (IContainerSymbol)containingScope : null;
			} 
//			assert lhsInfo != null : "Malformed Expression";
			return null;
		}
		else { 
			return startingScope;
		}		
    }
    	
    /*
     * Conditional Expression conversion
     */
     protected TypeInfo conditionalExpressionConversions(TypeInfo second, TypeInfo third){
     	TypeInfo info = new TypeInfo();
     	if(second.equals(third)){
     		info = second;
     		return info;
     	}
     	if((second.getType() == TypeInfo.t_void) && (third.getType() != TypeInfo.t_void)){
     		info = third;
     		return info;	
     	}
		if((second.getType() != TypeInfo.t_void) && (third.getType() == TypeInfo.t_void)){
			info = second;
			return info;	
		}
		if((second.getType() == TypeInfo.t_void) && (third.getType() == TypeInfo.t_void)){
			info = second;
			return info;	
		}
		try{
	     	info = ParserSymbolTable.getConditionalOperand(second, third);
	     	return info;
		} catch(ParserSymbolTableException e){
			// empty info
			return info;
		}
     }
	/*
	 * Apply the usual arithmetic conversions to find out the result of an expression 
	 * that has a lhs and a rhs as indicated in the specs (section 5.Expressions, page 64)
	 */
	protected TypeInfo usualArithmeticConversions( IASTScope scope, TypeInfo lhs, TypeInfo rhs) throws ASTSemanticException{
		
		// if you have a variable of type basic type, then we need to go to the basic type first
		while( (lhs.getType() == TypeInfo.t_type) && (lhs.getTypeSymbol() != null)){
			lhs = lhs.getTypeSymbol().getTypeInfo();  
		}
		while( (rhs.getType() == TypeInfo.t_type) && (rhs.getTypeSymbol() != null)){
			rhs = rhs.getTypeSymbol().getTypeInfo();  
		}
		
		if( !lhs.isType(TypeInfo.t__Bool, TypeInfo.t_enumerator ) && 
			!rhs.isType(TypeInfo.t__Bool, TypeInfo.t_enumerator ) ) 
		{
			handleProblem( scope, IProblem.SEMANTIC_INVALID_CONVERSION_TYPE, null ); 
		}

		TypeInfo info = new TypeInfo();
		if( 
		   ( lhs.checkBit(TypeInfo.isLong)  && lhs.getType() == TypeInfo.t_double)
		|| ( rhs.checkBit(TypeInfo.isLong)  && rhs.getType() == TypeInfo.t_double)
		){
			info.setType(TypeInfo.t_double);
			info.setBit(true, TypeInfo.isLong);		
			return info; 
		}
		else if(
		   ( lhs.getType() == TypeInfo.t_double )
		|| ( rhs.getType() == TypeInfo.t_double )
		){
			info.setType(TypeInfo.t_double);
			return info; 			
		}
		else if (
		   ( lhs.getType() == TypeInfo.t_float )
		|| ( rhs.getType() == TypeInfo.t_float )
		){
			info.setType(TypeInfo.t_float);
			return info; 						
		} else {
			// perform intergral promotions (Specs section 4.5)
			info.setType(TypeInfo.t_int);
		}
		
		if(
		   ( lhs.checkBit(TypeInfo.isUnsigned) && lhs.checkBit(TypeInfo.isLong)) 
		|| ( rhs.checkBit(TypeInfo.isUnsigned) && rhs.checkBit(TypeInfo.isLong))
		){
			info.setBit(true, TypeInfo.isUnsigned);
			info.setBit(true, TypeInfo.isLong);
			return info;
		} 
		else if(
			( lhs.checkBit(TypeInfo.isUnsigned) && rhs.checkBit(TypeInfo.isLong) ) 
		 || ( rhs.checkBit(TypeInfo.isUnsigned) && lhs.checkBit(TypeInfo.isLong) )
		){
			info.setBit(true, TypeInfo.isUnsigned);
			info.setBit(true, TypeInfo.isLong);
			return info;
		}
		else if (		
			( lhs.checkBit(TypeInfo.isLong)) 
		 || ( rhs.checkBit(TypeInfo.isLong))
		){
			info.setBit(true, TypeInfo.isLong);
			return info;			
		}
		else if (
			( lhs.checkBit(TypeInfo.isUnsigned) ) 
		 || ( rhs.checkBit(TypeInfo.isUnsigned) )		
		){
			info.setBit(true, TypeInfo.isUnsigned);
			return info;			
		} else {
			// it should be both = int
			return info;
		}		
	}
		
	private TypeInfo addToInfo(ASTExpression exp, boolean flag, int mask)
	{
//		assert exp != null : exp;
		TypeInfo info = exp.getResultType().getResult();
		info.setBit(flag, mask);
		return info;
	}
	
	protected ExpressionResult getExpressionResultType(
			IASTScope scope, 
			Kind kind, IASTExpression lhs,
			IASTExpression rhs,
			IASTExpression thirdExpression,
			IASTTypeId typeId,
			String literal,
			ISymbol symbol)	throws ASTSemanticException{
		
		
		TypeInfo info = new TypeInfo();
		
		if( extension.canHandleExpressionKind( kind ))
		{
			extension.getExpressionResultType( kind, lhs, rhs, typeId );
			return new ExpressionResult( info );
		}
		
		ExpressionResult result = null;
		if( literal != null && !literal.equals(EMPTY_STRING) ){ 
			info.setDefault( literal );
		}
		// types that resolve to void
		if ((kind == IASTExpression.Kind.PRIMARY_EMPTY)
		|| (kind == IASTExpression.Kind.THROWEXPRESSION) 
		|| (kind == IASTExpression.Kind.POSTFIX_DOT_DESTRUCTOR) 
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_DESTRUCTOR)
		|| (kind == IASTExpression.Kind.DELETE_CASTEXPRESSION)
		|| (kind == IASTExpression.Kind.DELETE_VECTORCASTEXPRESSION)
		){
			info.setType(TypeInfo.t_void);
			result = new ExpressionResult(info);
			return result;
		}
		// types that resolve to int
		if ((kind == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT)
		){
			info.setType(TypeInfo.t_int);
			result = new ExpressionResult(info);
			return result;
		}
		// size of is always unsigned int
		if ((kind == IASTExpression.Kind.UNARY_SIZEOF_TYPEID) 		
		|| (kind == IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION) 		
		){
			info.setType(TypeInfo.t_int);
			info.setBit(true, TypeInfo.isUnsigned);
			result = new ExpressionResult(info);
			return result;
		}
		// types that resolve to char
		if( (kind == IASTExpression.Kind.PRIMARY_CHAR_LITERAL)
		||  (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR)){
			info.setType(TypeInfo.t_char);
			// check that this is really only one literal
			if(literal.length() > 1){
				// this is a string
				info.addPtrOperator(new TypeInfo.PtrOp(TypeInfo.PtrOp.t_pointer));					
			}
			result = new ExpressionResult(info);
			return result;				
		}		
		// types that resolve to string
		if (kind == IASTExpression.Kind.PRIMARY_STRING_LITERAL){
			info.setType(TypeInfo.t_char);
			info.addPtrOperator(new TypeInfo.PtrOp(TypeInfo.PtrOp.t_pointer));
			result = new ExpressionResult(info);
			return result;				
		}		
		// types that resolve to float
		if( (kind == IASTExpression.Kind.PRIMARY_FLOAT_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT)){
			info.setType(TypeInfo.t_float);
			result = new ExpressionResult(info);
			return result;
		}
		// types that resolve to double
		if( kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE){
			info.setType(TypeInfo.t_double);
			result = new ExpressionResult(info);
			return result;				
		}		
		// types that resolve to wchar
		if(kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART){
			info.setType(TypeInfo.t_wchar_t);
			result = new ExpressionResult(info);
			return result;				
		}		
		// types that resolve to bool
		if( (kind == IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL)
		|| (kind == IASTExpression.Kind.RELATIONAL_GREATERTHAN)
		|| (kind == IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO)
		|| (kind == IASTExpression.Kind.RELATIONAL_LESSTHAN)
		|| (kind == IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO) 
		|| (kind == IASTExpression.Kind.EQUALITY_EQUALS) 
		|| (kind == IASTExpression.Kind.EQUALITY_NOTEQUALS) 
		|| (kind == IASTExpression.Kind.LOGICALANDEXPRESSION) 
		|| (kind == IASTExpression.Kind.LOGICALOREXPRESSION) 				
		)
		{
			info.setType(TypeInfo.t_bool);
			result = new ExpressionResult(info);
			return result;
		}
		// short added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT ){
			info = addToInfo((ASTExpression)lhs, true, TypeInfo.isShort);
			result = new ExpressionResult(info);
			return result;
		}
		// long added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG ){
			info = addToInfo((ASTExpression)lhs, true, TypeInfo.isLong);
			result = new ExpressionResult(info);
			return result;
		}
		// signed added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED ){
			info = addToInfo((ASTExpression)lhs, false, TypeInfo.isUnsigned);
			result = new ExpressionResult(info);
			return result;
		}
		// unsigned added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED ){
			info = addToInfo((ASTExpression)lhs, true, TypeInfo.isUnsigned);
			result = new ExpressionResult(info);
			return result;
		}
		// Id expressions resolve to t_type, symbol already looked up
		if( kind == IASTExpression.Kind.ID_EXPRESSION )
		{
			info.setType(TypeInfo.t_type);
			info.setTypeSymbol(symbol);								
			result = new ExpressionResult(info);
			if (symbol == null)
				result.setFailedToDereference(true);
			return result;
		}
		
		// an ampersand implies a pointer operation of type reference
		if (kind == IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			info = left.getResultType().getResult();
			if ((info != null) && (info.getTypeSymbol() != null)){
				info.addOperatorExpression( TypeInfo.OperatorExpression.addressof );
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// a star implies a pointer operation of type pointer
		if (kind == IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null ); 
			info = left.getResultType().getResult();
			if ((info != null)&& (info.getTypeSymbol() != null)){
				info.addOperatorExpression( TypeInfo.OperatorExpression.indirection );
			}else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		// subscript
		if (kind == IASTExpression.Kind.POSTFIX_SUBSCRIPT){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			info = left.getResultType().getResult();
			if ((info != null) && (info.getTypeSymbol() != null)){
				info.addOperatorExpression( TypeInfo.OperatorExpression.subscript );
			}else {
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			}
			result = new ExpressionResult(info);
			return result;
		}
		// the dot and the arrow resolves to the type of the member
		if ((kind == IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS)
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP)
		){
			if(symbol != null){
				info = new TypeInfo(symbol.getTypeInfo());			
			}
			result = new ExpressionResult(info);
			return result;
		}
		// the dot* and the arrow* are the same as dot/arrow + unary star
		if ((kind == IASTExpression.Kind.PM_DOTSTAR)
		|| (kind == IASTExpression.Kind.PM_ARROWSTAR)
		){
			ASTExpression right =(ASTExpression)rhs;
			if (right == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			info = right.getResultType().getResult();
			if ((info != null) && (symbol != null)){
				info.addOperatorExpression( TypeInfo.OperatorExpression.indirection );
				info.setTypeSymbol(symbol);
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		// this
		if (kind == IASTExpression.Kind.PRIMARY_THIS){
			if(symbol != null)
			{
				info.setType(TypeInfo.t_type);
				info.setTypeSymbol(symbol);	
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// conditional
		if (kind == IASTExpression.Kind.CONDITIONALEXPRESSION){
			ASTExpression right = (ASTExpression)rhs;  
			ASTExpression third = (ASTExpression)thirdExpression;
			if((right != null ) && (third != null)){
				TypeInfo rightType =right.getResultType().getResult();
				TypeInfo thirdType =third.getResultType().getResult();
				if((rightType != null) && (thirdType != null)){
					info = conditionalExpressionConversions(rightType, thirdType);   
				} else 
					handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
				
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}		
		// new 
		if( ( kind == IASTExpression.Kind.NEW_TYPEID )
		|| ( kind == IASTExpression.Kind.NEW_NEWTYPEID ) )
		{
			try
            {
                info = typeId.getTypeSymbol().getTypeInfo();
				info.addPtrOperator( new TypeInfo.PtrOp(TypeInfo.PtrOp.t_pointer));
            }
            catch (ASTNotImplementedException e)
            {
            	// will never happen
            }
			result = new ExpressionResult(info);
			return result;
		}
		// types that use the usual arithmetic conversions
		if((kind == IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY) 
		|| (kind == IASTExpression.Kind.MULTIPLICATIVE_DIVIDE) 
		|| (kind == IASTExpression.Kind.MULTIPLICATIVE_MODULUS) 
		|| (kind == IASTExpression.Kind.ADDITIVE_PLUS) 
		|| (kind == IASTExpression.Kind.ADDITIVE_MINUS) 
		|| (kind == IASTExpression.Kind.ANDEXPRESSION) 
		|| (kind == IASTExpression.Kind.EXCLUSIVEOREXPRESSION)
		|| (kind == IASTExpression.Kind.INCLUSIVEOREXPRESSION)
		){
			ASTExpression left = (ASTExpression)lhs;
			ASTExpression right = (ASTExpression)rhs;  
			if((left != null ) && (right != null)){
				TypeInfo leftType =left.getResultType().getResult();
				TypeInfo rightType =right.getResultType().getResult();
				info = usualArithmeticConversions( scope, leftType, rightType);
			}
			else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null ); 
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// types that resolve to LHS types 
		if ((kind == IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_INCREMENT) 
		|| (kind == IASTExpression.Kind.POSTFIX_DECREMENT)
		|| (kind == IASTExpression.Kind.POSTFIX_TYPEID_EXPRESSION)		 
		|| (kind == IASTExpression.Kind.UNARY_INCREMENT) 
		|| (kind == IASTExpression.Kind.UNARY_DECREMENT) 
		|| (kind == IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION)
		|| (kind == IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.SHIFT_LEFT) 
		|| (kind == IASTExpression.Kind.SHIFT_RIGHT) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR) 
		){
			ASTExpression left = (ASTExpression)lhs;  
			if(left != null){
				info =left.getResultType().getResult();   
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}		
		// the cast changes the types to the type looked up in typeId = symbol
		if(( kind == IASTExpression.Kind.CASTEXPRESSION )
		|| ( kind == IASTExpression.Kind.POSTFIX_DYNAMIC_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_STATIC_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_REINTERPRET_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_CONST_CAST )		
		){
			try{
				info = new TypeInfo(typeId.getTypeSymbol().getTypeInfo()); 
			}catch (ASTNotImplementedException e)
			{
				// will never happen
			}
			result = new ExpressionResult(info);
			return result;
		}				
		// a list collects all types of left and right hand sides
		if(kind == IASTExpression.Kind.EXPRESSIONLIST){
			result = new ExpressionResultList();
			if(lhs != null){
				TypeInfo leftType = ((ASTExpression)lhs).getResultType().getResult();
				result.setResult(leftType);	
			}
			if(rhs != null){
				TypeInfo rightType = ((ASTExpression)rhs).getResultType().getResult();
				result.setResult(rightType);
			}
			return result;			
		}
		// a function call type is the return type of the function
		if(kind == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){
			if(symbol != null){
				IParameterizedSymbol psymbol = (IParameterizedSymbol) symbol;
				ISymbol returnTypeSymbol = psymbol.getReturnType();
				if(returnTypeSymbol != null){
					info.setType(returnTypeSymbol.getType());  
					info.setTypeSymbol(returnTypeSymbol);
				}else {
					// this is call to a constructor
				}
			} 
			result = new ExpressionResult(info);
			if(symbol == null)
				result.setFailedToDereference(true);
			return result;
		}
		// typeid
		if( kind == IASTExpression.Kind.POSTFIX_TYPEID_TYPEID )
		{
			try
            {
                info = typeId.getTypeSymbol().getTypeInfo();
            }
            catch (ASTNotImplementedException e)
            {
            	// will not ever happen from within CompleteParseASTFactory
            }
			result = new ExpressionResult(info);
			return result;
		}
		// typename
		if ( ( kind == IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER )
		|| ( kind == IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID ) )
		{
			if(symbol != null){
				info.setType(TypeInfo.t_type);
				info.setTypeSymbol(symbol);
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
//		assert false : this;
		return null;						
	}

    protected void getExpressionReferences(IASTExpression expression, List references)
    {
        if( expression != null )
        {
        	references.addAll( ((ASTExpression)expression).getReferences() );
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
     */
    public IASTNewExpressionDescriptor createNewDescriptor(List newPlacementExpressions,List newTypeIdExpressions,List newInitializerExpressions)
    {
		return new ASTNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(java.util.List)
     */
    public IASTExceptionSpecification createExceptionSpecification(IASTScope scope, List typeIds) throws ASTSemanticException
    {
    	List references = new ArrayList(); 
    	List newTypeIds = new ArrayList(); 
        if( typeIds != null )
        {
        	Iterator iter =typeIds.iterator();
        	while( iter.hasNext() )
        	{
        		IASTTypeId duple = (IASTTypeId)iter.next();
        		if( duple != null )
        		{
        			lookupQualifiedName( scopeToSymbol( scope ), ((ASTTypeId)duple).getTokenDuple(), references, false  );
        			newTypeIds.add( duple.toString() );
        		}
        	}
        }
        return new ASTExceptionSpecification( newTypeIds, references );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        IASTScope scope,
        ITokenDuple duple, IASTExpression expressionList)
    {
        List references = new ArrayList(); 
        
        IContainerSymbol scopeSymbol = scopeToSymbol(scope);
        
		boolean requireReferenceResolution = false;
        if( duple != null )
        {
        	try
        	{
        		lookupQualifiedName( scopeSymbol, duple, references, true );
        	} catch( ASTSemanticException ase )
        	{
        		requireReferenceResolution = true;
        	}
        }
        
        getExpressionReferences( expressionList, references ); 
        return new ASTConstructorMemberInitializer( 
        	expressionList, 
        	duple == null ? EMPTY_STRING : duple.toString(), 
			duple == null ? 0 : duple.getFirstToken().getOffset(),
        	references, requireReferenceResolution );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean, boolean, boolean)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        IASTScope scope,
        Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned,
        boolean isUnsigned, 
		boolean isTypename, 
		boolean isComplex, 
		boolean isImaginary,
		boolean isGlobal, Hashtable extensionParms ) throws ASTSemanticException
    {
    	if( extension.overrideCreateSimpleTypeSpecifierMethod( kind ))
    		return extension.createSimpleTypeSpecifier(pst, scope, kind, typeName, isShort, isLong, isSigned, isUnsigned, isTypename, isComplex, isImaginary, isGlobal, extensionParms );
    	TypeInfo.eType type = null;
    	
    	if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
    		type = TypeInfo.t_type;
	    else if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
        	type = TypeInfo.t_bool;
        else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
			type = TypeInfo.t_char;
		else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE ||kind == IASTSimpleTypeSpecifier.Type.FLOAT  ) 
			type = TypeInfo.t_double;
		else if( kind == IASTSimpleTypeSpecifier.Type.INT )
			type = TypeInfo.t_int;
		else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
			type = TypeInfo.t_void;
		else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
			type = TypeInfo.t_wchar_t;
		else if( kind == IASTSimpleTypeSpecifier.Type._BOOL )
			type = TypeInfo.t__Bool;
	
		List references = new ArrayList(); 
		ISymbol s = pst.newSymbol( EMPTY_STRING, type ); 
		if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
		{
			// lookup the duple
			Iterator i = typeName.iterator();
			IToken first = typeName.getFirstToken();
			
			ISymbol typeSymbol = getScopeToSearchUpon( scope, first, i );
			
			if( isGlobal )
				typeSymbol = typeSymbol.getSymbolTable().getCompilationUnit();
			
			List [] argLists = typeName.getTemplateIdArgLists();
			int idx = 0;
			
			while( i.hasNext() )
			{
				IToken current = (IToken)i.next(); 
				
				if( current.getType() == IToken.tCOLONCOLON ){
					idx++;
					continue;
				}

				String image = current.getImage();
				int offset = current.getOffset();
				
				if( argLists != null && argLists[ idx ] != null ){
					if( i.hasNext() && current.getNext().getType() == IToken.tLT )
						current = typeName.consumeTemplateIdArguments( (IToken) i.next(), i );
				}
				
				if( typeSymbol instanceof IDeferredTemplateInstance ){
					typeSymbol = ((IDeferredTemplateInstance)typeSymbol).getTemplate().getTemplatedSymbol();
				}
				try
                {
					if( argLists != null && argLists[ idx ] != null )
						typeSymbol = ((IContainerSymbol)typeSymbol).lookupTemplateId( image, getTemplateArgList( argLists[idx] ) );
					else if( current != typeName.getLastToken() )
                		typeSymbol = ((IContainerSymbol)typeSymbol).lookupNestedNameSpecifier( image );
                    else
                    	typeSymbol = ((IContainerSymbol)typeSymbol).lookup( image );
					
					if( typeSymbol != null )
					{	
                    	addReference( references, createReference( typeSymbol, image, offset ));
                    	if( argLists != null && argLists[idx] != null )
							addTemplateIdReferences( references, argLists[idx] );
					}
					else
                    	handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, image, -1, -1, current.getLineNumber() );
                }
                catch (ParserSymbolTableException e)
                {
                	handleProblem( e.createProblemID(), image,typeName.getStartOffset(), typeName.getEndOffset(), typeName.getLineNumber() );
                } 
			}
			s.setTypeSymbol( typeSymbol );
		}
		
		s.getTypeInfo().setBit( isLong, TypeInfo.isLong );
		s.getTypeInfo().setBit( isShort, TypeInfo.isShort);
		s.getTypeInfo().setBit( isUnsigned, TypeInfo.isUnsigned );
		s.getTypeInfo().setBit( isComplex, TypeInfo.isComplex );
		s.getTypeInfo().setBit( isImaginary, TypeInfo.isImaginary );
		s.getTypeInfo().setBit( isSigned, TypeInfo.isSigned );
			
		return new ASTSimpleTypeSpecifier( s, false, typeName.toString(), references );

    }
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate)
	 */
	public IASTFunction createFunction(
	    IASTScope scope,
	    ITokenDuple name,
	    List parameters,
	    IASTAbstractDeclaration returnType,
	    IASTExceptionSpecification exception,
	    boolean isInline,
	    boolean isFriend,
	    boolean isStatic,
	    int startOffset,
	    int startLine,
	    int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual, 
		boolean isExplicit, 
		boolean isPureVirtual, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
	{
		List references = new ArrayList();
		IContainerSymbol ownerScope = scopeToSymbol( scope );		
		
		// check if this is a method in a body file
		if(name.getSegmentCount() > 1){
			ISymbol symbol = lookupQualifiedName( ownerScope, 
					  name.getLeadingSegments(), 
					  references, 
					  false,
					  LookupType.FORPARENTSCOPE );
			
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if((parentScope != null) && 
			( (parentScope.getType() == TypeInfo.t_class) 
			|| (parentScope.getType() == TypeInfo.t_struct)
			|| (parentScope.getType() == TypeInfo.t_union))
			){
				IASTScope methodParentScope = (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();
				ITokenDuple newName = name.getLastSegment();
                return createMethod(
                    methodParentScope,
                    newName, 
                    parameters,
                    returnType,
                    exception,
                    isInline,
                    isFriend,
                    isStatic,
                    startOffset,
                    startLine,
                    newName.getFirstToken().getOffset(),
                    nameEndOffset,
                    nameLine,
                    ownerTemplate,
                    isConst,
                    isVolatile,
                    isVirtual,
                    isExplicit,
                    isPureVirtual,
                    ASTAccessVisibility.PRIVATE,
                    constructorChain, references, isFunctionDefinition, hasFunctionTryBlock, hasVariableArguments );
			}
		}
	
		IParameterizedSymbol symbol = pst.newParameterizedSymbol( name.getFirstToken().getImage(), TypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		
		symbol.setHasVariableArgs( hasVariableArguments );
		
		setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters.iterator() );
		 
		symbol.setIsForwardDeclaration(!isFunctionDefinition);
		boolean previouslyDeclared = false;

		List functionParameters = new LinkedList();
		// the lookup requires a list of type infos
		// instead of a list of IASTParameterDeclaration
		Iterator p = parameters.iterator();
		while (p.hasNext()){
			ASTParameterDeclaration param = (ASTParameterDeclaration)p.next();
			if( param.getSymbol() == null )
				handleProblem( IProblem.SEMANTICS_RELATED, param.getName(), param.getNameOffset(), param.getEndingOffset(), param.getStartingLine() );
			functionParameters.add(param.getSymbol().getTypeInfo());
		}
		
		IParameterizedSymbol functionDeclaration = null; 
		
		functionDeclaration = 
			(IParameterizedSymbol) lookupQualifiedName(ownerScope, name.getFirstToken().getImage(), TypeInfo.t_function, functionParameters, 0, new ArrayList(), false, LookupType.FORDEFINITION );                

		if( functionDeclaration != null && symbol.isType( TypeInfo.t_function )){
			previouslyDeclared = true;
			
			if( isFunctionDefinition ){
				functionDeclaration.setTypeSymbol( symbol );
			}
		}
		
		if( previouslyDeclared == false || isFunctionDefinition ){
			try
			{
				ownerScope.addSymbol( symbol );
			}
			catch (ParserSymbolTableException e)
			{
				handleProblem( e.createProblemID(), name.toString());   
			}	
		} else {
			symbol = functionDeclaration;
		}
		
		ASTFunction function = new ASTFunction( symbol, nameEndOffset, parameters, returnType, exception, startOffset, startLine, nameOffset, nameLine, ownerTemplate, references, previouslyDeclared, hasFunctionTryBlock, isFriend );        
	    attachSymbolExtension(symbol, function, isFunctionDefinition); 
	    return function;
	}
    
    protected void setFunctionTypeInfoBits(
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        IParameterizedSymbol symbol)
    {
        symbol.getTypeInfo().setBit( isInline, TypeInfo.isInline );
        symbol.getTypeInfo().setBit( isFriend, TypeInfo.isFriend );
        symbol.getTypeInfo().setBit( isStatic, TypeInfo.isStatic );
    }
    
    /**
     * @param symbol
     * @param iterator
     */
    protected void setParameters(IParameterizedSymbol symbol, List references, Iterator iterator) throws ASTSemanticException
    {
        while( iterator.hasNext() )
        {
        	setParameter( symbol, (IASTParameterDeclaration)iterator.next(), true, references );	
        }
    }

	protected TypeInfo getParameterTypeInfo( IASTAbstractDeclaration absDecl)throws ASTSemanticException{
		TypeInfo type = new TypeInfo();
		if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ) 
		{
			IASTSimpleTypeSpecifier simpleType = ((IASTSimpleTypeSpecifier)absDecl.getTypeSpecifier());
			IASTSimpleTypeSpecifier.Type kind = simpleType.getType();
			if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
				type.setType(TypeInfo.t_bool);
			else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
				type.setType(TypeInfo.t_char);
			else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE )
				type.setType(TypeInfo.t_double);
			else if( kind == IASTSimpleTypeSpecifier.Type.FLOAT )
				type.setType(TypeInfo.t_float); 
			else if( kind == IASTSimpleTypeSpecifier.Type.INT )
				type.setType(TypeInfo.t_int);
			else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
				type.setType(TypeInfo.t_void);
			else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
				type.setType(TypeInfo.t_wchar_t);
			else if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
				type.setType(TypeInfo.t_type);
			else if( kind == IASTSimpleTypeSpecifier.Type._BOOL ){
				type.setType( TypeInfo.t__Bool );
			}
//			else
//				assert false : "Unexpected IASTSimpleTypeSpecifier.Type";
			
			setTypeBitFlags(type, simpleType);
			
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTClassSpecifier )
		{
			type.setType( TypeInfo.t_type );
			type.setTypeSymbol( ((ASTClassSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTEnumerationSpecifier )
		{
			type.setType( TypeInfo.t_type );
			type.setTypeSymbol( ((ASTEnumerationSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier )
		{
			type.setType( TypeInfo.t_type );
			type.setTypeSymbol( ((ASTElaboratedTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
//		else
//			assert false : this; 		
		return type;		
	}
    /**
	 * @param type
	 * @param simpleType
	 */
	private void setTypeBitFlags(TypeInfo type, IASTSimpleTypeSpecifier simpleType) {
		type.setBit( simpleType.isLong(), TypeInfo.isLong);
		type.setBit( simpleType.isShort(), TypeInfo.isShort);
		type.setBit( simpleType.isUnsigned(), TypeInfo.isUnsigned);
		type.setBit( simpleType.isComplex(), TypeInfo.isComplex);
		type.setBit( simpleType.isImaginary(), TypeInfo.isImaginary);
		type.setBit( simpleType.isSigned(), TypeInfo.isSigned);
	}

	/**
	 * @param symbol
	 * @param returnType
	 */
	protected void setParameter(IParameterizedSymbol symbol, IASTAbstractDeclaration absDecl, boolean isParameter, List references) throws ASTSemanticException
	{
		if (absDecl.getTypeSpecifier() == null)
			return;
	
		// now determined by another function    
		TypeInfo info = getParameterTypeInfo( absDecl );
		TypeInfo.eType type = info.getType();
		
		ISymbol xrefSymbol = info.getTypeSymbol();
		List newReferences = null; 
		int infoBits = 0;
	    if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ) 
	    {
	   		if( ((IASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getType() == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
	    	{
	    		xrefSymbol = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol(); 
	    		newReferences = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getReferences();
	    	}
	   		
	   		infoBits = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol().getTypeInfo().getTypeInfo();
	    }
	    else if( absDecl.getTypeSpecifier() instanceof ASTElaboratedTypeSpecifier )
	    {
	    	ASTElaboratedTypeSpecifier elab = (ASTElaboratedTypeSpecifier)absDecl.getTypeSpecifier();
	    	xrefSymbol = elab.getSymbol();
	    	newReferences = new ArrayList(); 
	    	newReferences.addAll( elab.getReferences() );
	    	if( xrefSymbol != null )
	    		newReferences.add( createReference( xrefSymbol, elab.getName(), elab.getNameOffset()) );  
	    }
	    
	    String paramName = EMPTY_STRING; 
	    if(absDecl instanceof IASTParameterDeclaration){
	    	paramName = ((IASTParameterDeclaration)absDecl).getName();
	    }
	    
	    ISymbol paramSymbol = pst.newSymbol( paramName, type );
	    if( xrefSymbol != null ){
	    	if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier )
	    		paramSymbol.setTypeSymbol( xrefSymbol.getTypeSymbol() );
	    	else 
	    		paramSymbol.setTypeSymbol( xrefSymbol );
	    }
	    
	    paramSymbol.getTypeInfo().setTypeInfo( infoBits );
	    paramSymbol.getTypeInfo().setBit( absDecl.isConst(), TypeInfo.isConst );
	    paramSymbol.getTypeInfo().setBit( absDecl.isVolatile(), TypeInfo.isVolatile );
	    
	    setPointerOperators( paramSymbol, absDecl.getPointerOperators(), absDecl.getArrayModifiers() );
	
	    if( isParameter)
	    	symbol.addParameter( paramSymbol );
	    else
			symbol.setReturnType( paramSymbol );
			
		if( newReferences != null )
			references.addAll( newReferences );
		
		if( absDecl instanceof ASTParameterDeclaration )
		{
			ASTParameterDeclaration parm = (ASTParameterDeclaration)absDecl;
			parm.setSymbol( paramSymbol );
			attachSymbolExtension( paramSymbol, parm, true );
		}
	}

    /**
     * @param paramSymbol
     * @param iterator
     */
    protected void setPointerOperators(ISymbol symbol, Iterator pointerOpsIterator, Iterator arrayModsIterator) throws ASTSemanticException
    {
        while( pointerOpsIterator.hasNext() )
        {
        	ASTPointerOperator pointerOperator = (ASTPointerOperator)pointerOpsIterator.next();
        	if( pointerOperator == ASTPointerOperator.REFERENCE )
        		symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_reference )); 
        	else if( pointerOperator == ASTPointerOperator.POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer ));
			else if( pointerOperator == ASTPointerOperator.CONST_POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer, true, false ));
			else if( pointerOperator == ASTPointerOperator.VOLATILE_POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer, false, true));
//			else
//				assert false : pointerOperator;
        }
        
        while( arrayModsIterator.hasNext() )
        {
        	arrayModsIterator.next();
        	symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_array )); 
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
     
	public IASTMethod createMethod(
		IASTScope scope,
		ITokenDuple name,
		List parameters,
		IASTAbstractDeclaration returnType,
		IASTExceptionSpecification exception,
		boolean isInline,
		boolean isFriend,
		boolean isStatic,
		int startOffset,
		int startLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual, 
		boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
	{
		return createMethod(scope, name, parameters, returnType, exception,
		isInline, isFriend, isStatic, startOffset, startLine, nameOffset,
		nameEndOffset, nameLine, ownerTemplate, isConst, isVolatile, isVirtual,
		isExplicit, isPureVirtual, visibility, constructorChain, null, isFunctionDefinition, hasFunctionTryBlock, hasVariableArguments );
	}   
	  
    public IASTMethod createMethod(
        IASTScope scope,
        ITokenDuple nameDuple,
        List parameters, 
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int startingLine,
        int nameOffset,
        int nameEndOffset,
        int nameLine,
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isVirtual,
        boolean isExplicit, 
        boolean isPureVirtual,
        ASTAccessVisibility visibility, List constructorChain, List references, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
    {
		boolean isConstructor = false;
		boolean isDestructor = false;

		IContainerSymbol ownerScope = scopeToSymbol( ownerTemplate != null ? (IASTScope) ownerTemplate : scope );

		IParameterizedSymbol symbol = null;

		if( references == null )
		{
			references = new ArrayList();
			if( nameDuple.length() > 2  ) // destructor
			{
				ITokenDuple leadingSegments = nameDuple.getLeadingSegments();
				ISymbol test = lookupQualifiedName( ownerScope, leadingSegments, references, false );
				if( test == ownerScope )
					nameDuple = nameDuple.getLastSegment();
			}
		}
		symbol = pst.newParameterizedSymbol( nameDuple.toString(), TypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		setMethodTypeInfoBits( symbol, isConst, isVolatile, isVirtual, isExplicit );
		symbol.setHasVariableArgs( hasVariableArguments );
		    	
    	if( returnType.getTypeSpecifier() != null )
			setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters.iterator() );
		
		IASTClassSpecifier classifier = null; 
		if( scope instanceof IASTTemplateDeclaration ){
			classifier = (IASTClassSpecifier) ((IASTTemplateDeclaration)scope).getOwnerScope();
		} else {
			classifier = (IASTClassSpecifier) scope; 
		}
		String parentName = classifier.getName(); 
  
		// check constructor / destructor if no return type
		if ( returnType.getTypeSpecifier() == null ){
			if(parentName.indexOf(DOUBLE_COLON) != -1){				
				parentName = parentName.substring(parentName.lastIndexOf(DOUBLE_COLON) + DOUBLE_COLON.length());
			}    	
			if( parentName.equals(nameDuple.toString()) ){
				isConstructor = true; 
			} else if(nameDuple.getFirstToken().getType() == IToken.tCOMPL && parentName.equals(nameDuple.getLastToken().getImage())){
				isDestructor = true;
			}
		}

		symbol.setIsForwardDeclaration(!isFunctionDefinition);
		boolean previouslyDeclared = false; 
		
		IParameterizedSymbol functionDeclaration = null;
		
		if( isFunctionDefinition || isFriend )
		{
			List functionParameters = new LinkedList();
			// the lookup requires a list of type infos
			// instead of a list of IASTParameterDeclaration
			Iterator p = parameters.iterator();
			while (p.hasNext()){
				ASTParameterDeclaration param = (ASTParameterDeclaration)p.next();
				if( param.getSymbol() == null )
					handleProblem( IProblem.SEMANTICS_RELATED, param.getName(), param.getNameOffset(), param.getEndingOffset(), param.getNameLineNumber() );
				functionParameters.add(param.getSymbol().getTypeInfo());
			}
			
			List functionReferences = new ArrayList();
			
			if( isFriend )
			{
				functionDeclaration = 
					(IParameterizedSymbol) lookupQualifiedName(ownerScope, nameDuple, isConstructor ? TypeInfo.t_constructor : TypeInfo.t_function, functionParameters, functionReferences, false, LookupType.FORFRIENDSHIP );
			} else {
				functionDeclaration = 
					(IParameterizedSymbol) lookupQualifiedName(ownerScope, nameDuple.toString(), isConstructor ? TypeInfo.t_constructor : TypeInfo.t_function, functionParameters, 0, functionReferences, false, LookupType.FORDEFINITION );
			}
			
			previouslyDeclared = ( functionDeclaration != null ) && functionDeclaration.isType( isConstructor ? TypeInfo.t_constructor : TypeInfo.t_function );
			
			if( isFriend )
			{
				if( functionDeclaration != null && functionDeclaration.isType( isConstructor ? TypeInfo.t_constructor : TypeInfo.t_function ))
				{
					symbol.setTypeSymbol( functionDeclaration );
					// friend declaration, has no real visibility, set private
					visibility = ASTAccessVisibility.PRIVATE;		
				} else if( ownerScope.getContainingSymbol().isType( TypeInfo.t_constructor ) ||
						   ownerScope.getContainingSymbol().isType( TypeInfo.t_function )    ||
						   ownerScope.getContainingSymbol().isType( TypeInfo.t_block ) )
				{	
					//only needs to be previously declared if we are in a local class
					handleProblem( IProblem.SEMANTIC_ILLFORMED_FRIEND, nameDuple.toString(), nameDuple.getStartOffset(), nameDuple.getEndOffset(), nameDuple.getLineNumber() );
				}
				 
			} else if( functionDeclaration != null && functionDeclaration.isType( isConstructor ? TypeInfo.t_constructor : TypeInfo.t_function ) )
			{
				functionDeclaration.setTypeSymbol( symbol );
				// set the definition visibility = declaration visibility
				ASTMethodReference reference = (ASTMethodReference) functionReferences.iterator().next();
				visibility = ((IASTMethod)reference.getReferencedElement()).getVisiblity();		
			}
		}
		
		try
		{
			if( isFriend )
			{
				if( functionDeclaration != null )
					((IDerivableContainerSymbol)ownerScope).addFriend( functionDeclaration );
				else
					((IDerivableContainerSymbol)ownerScope).addFriend( symbol );
			} else if( !isConstructor )
				ownerScope.addSymbol( symbol );
			else
			{
				symbol.setType( TypeInfo.t_constructor );
				((IDerivableContainerSymbol)ownerScope).addConstructor( symbol );
			}
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), nameDuple.toString(), nameDuple.getStartOffset(), nameDuple.getEndOffset(), nameDuple.getLineNumber() );   
		}

		resolveLeftoverConstructorInitializerMembers( symbol, constructorChain );
  
        ASTMethod method = new ASTMethod( symbol, parameters, returnType, exception, startOffset, startingLine, nameOffset, nameEndOffset, nameLine, ownerTemplate, references, previouslyDeclared, isConstructor, isDestructor, isPureVirtual, visibility, constructorChain, hasFunctionTryBlock, isFriend );
        attachSymbolExtension( symbol, method, isFunctionDefinition );
        return method;
    }
    /**
	 * @param symbol
	 * @param constructorChain
	 */
	protected void resolveLeftoverConstructorInitializerMembers(IParameterizedSymbol symbol, List constructorChain) throws ASTSemanticException
	{
		if( constructorChain != null )
		{
			Iterator initializers = constructorChain.iterator();
			while( initializers.hasNext())
			{
				IASTConstructorMemberInitializer initializer = (IASTConstructorMemberInitializer)initializers.next();
				if( !initializer.getName().equals( EMPTY_STRING) && 
					initializer instanceof ASTConstructorMemberInitializer && 
					((ASTConstructorMemberInitializer)initializer).requiresNameResolution() ) 
				{
					ASTConstructorMemberInitializer realInitializer = ((ASTConstructorMemberInitializer)initializer);
					IDerivableContainerSymbol container = (IDerivableContainerSymbol) symbol.getContainingSymbol(); 
					lookupQualifiedName(container, initializer.getName(), TypeInfo.t_any, null, realInitializer.getNameOffset(), realInitializer.getReferences(), false, LookupType.QUALIFIED);
					// TODO try and resolve parameter references now in the expression list
				}
			}
		}
		
		
	}

	/**
     * @param symbol
     * @param isConst
     * @param isVolatile
     * @param isConstructor
     * @param isDestructor
     * @param isVirtual
     * @param isExplicit
     * @param isPureVirtual
     */
    protected void setMethodTypeInfoBits(IParameterizedSymbol symbol, boolean isConst, boolean isVolatile, boolean isVirtual, boolean isExplicit)
    {
        symbol.getTypeInfo().setBit( isConst, TypeInfo.isConst );
		symbol.getTypeInfo().setBit( isVolatile, TypeInfo.isConst );
		symbol.getTypeInfo().setBit( isVirtual, TypeInfo.isVirtual );
		symbol.getTypeInfo().setBit( isExplicit, TypeInfo.isExplicit );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int)
     */
    public IASTVariable createVariable(
        IASTScope scope,
        ITokenDuple name,
        boolean isAuto,
        IASTInitializerClause initializerClause,
        IASTExpression bitfieldExpression,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression) throws ASTSemanticException
    {
		List references = new ArrayList(); 
		IContainerSymbol ownerScope = scopeToSymbol( scope );		

		if(name.getSegmentCount() > 1)
		{
			ISymbol symbol = lookupQualifiedName( ownerScope, 
												  name.getLeadingSegments(), 
												  references, 
												  false,
												  LookupType.FORPARENTSCOPE );
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if( (parentScope != null) && ( (parentScope.getType() == TypeInfo.t_class) ||
					                       (parentScope.getType() == TypeInfo.t_struct)|| 
										   (parentScope.getType() == TypeInfo.t_union) ) )
			{
				IASTScope fieldParentScope = (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();

				ITokenDuple newName = name.getLastSegment();

				return createField(fieldParentScope, newName,isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, 
						isRegister, isStatic, startingOffset, startingLine, newName.getStartOffset(), nameEndOffset, nameLine, constructorExpression, ASTAccessVisibility.PRIVATE, references);
			}
		}

		ISymbol newSymbol = cloneSimpleTypeSymbol(name.getFirstToken().getImage(), abstractDeclaration, references);
        if( newSymbol == null )
        	handleProblem( IProblem.SEMANTICS_RELATED, name.toString() );
        
        setVariableTypeInfoBits(
            isAuto,
            abstractDeclaration,
            isMutable,
            isExtern,
            isRegister,
            isStatic,
            newSymbol);
		setPointerOperators( newSymbol, abstractDeclaration.getPointerOperators(), abstractDeclaration.getArrayModifiers() );
		
		newSymbol.setIsForwardDeclaration( isStatic || isExtern );
		boolean previouslyDeclared = false;
		if(!isStatic){
			ISymbol variableDeclaration = lookupQualifiedName(ownerScope, name.toString(), new ArrayList(), false, LookupType.UNQUALIFIED);                
	
			if( variableDeclaration != null && newSymbol.getType() == variableDeclaration.getType() )
			{
				if( !newSymbol.isType( TypeInfo.t_type ) ||
					(newSymbol.isType( TypeInfo.t_type ) && newSymbol.getTypeSymbol() != variableDeclaration.getTypeSymbol() ) )
				{
					variableDeclaration.setTypeSymbol( newSymbol );
					previouslyDeclared = true;
				}
			}
		}
		try
		{
			ownerScope.addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), name.getFirstToken().getImage() );
		}
        
        ASTVariable variable = new ASTVariable( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, constructorExpression, previouslyDeclared );
        if( variable.getInitializerClause() != null )
        {
        	variable.getInitializerClause().setOwnerVariableDeclaration(variable);
        	addDesignatorReferences( (ASTInitializerClause)variable.getInitializerClause() );
        }
        
        attachSymbolExtension(newSymbol, variable, !isStatic );
        return variable;        
    }
    
    
    /**
     * @param clause
     */
    protected void addDesignatorReferences( ASTInitializerClause clause )
    {
        if( clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST || 
        	clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION )
        {
			ISymbol variableSymbol = ((ASTVariable)clause.getOwnerVariableDeclaration()).getSymbol();
			ISymbol currentSymbol = variableSymbol.getTypeSymbol();
				
			TypeInfo currentTypeInfo = new TypeInfo( currentSymbol.getTypeInfo() ); 
			Iterator designators = clause.getDesignators();
			while( designators.hasNext() )
			{
				IASTDesignator designator = (IASTDesignator)designators.next();
				if( designator.getKind() == IASTDesignator.DesignatorKind.FIELD )
				{
					ISymbol lookup = null;
					if( ! ( currentSymbol instanceof IContainerSymbol ) ) 
						break;
					
					try
                    {
                        lookup = ((IContainerSymbol)currentSymbol).lookup( designator.fieldName() );
                    }
                    catch (ParserSymbolTableException e){
                        break;
                    }
                    
                    if( lookup == null || lookup.getContainingSymbol() != currentSymbol )  
                        break;
                        
                    try
                    {
                    	if( lookup != null )
                    		clause.getReferences().add( createReference( lookup, designator.fieldName(), designator.fieldOffset() ));
                    }
                    catch (ASTSemanticException e1)
                    {
                        // error
                    }
                    
					// we have found the correct field
					currentTypeInfo = new TypeInfo( lookup.getTypeInfo() );
					if( lookup.getTypeInfo() == null )
						break;
					currentSymbol = lookup.getTypeSymbol();

				}
				else if( designator.getKind() == IASTDesignator.DesignatorKind.SUBSCRIPT )
					currentTypeInfo.applyOperatorExpressions( SUBSCRIPT );		
			}
			
        }			
        
        if( clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST || 
        	clause.getKind() == IASTInitializerClause.Kind.INITIALIZER_LIST )
        {	
        	Iterator subInitializers = clause.getInitializers();
        	while( subInitializers.hasNext() )
        		addDesignatorReferences( (ASTInitializerClause)subInitializers.next() );
        }
    }

    protected void setVariableTypeInfoBits(
        boolean isAuto,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        ISymbol newSymbol)
    {
        newSymbol.getTypeInfo().setBit( isMutable, TypeInfo.isMutable );
        newSymbol.getTypeInfo().setBit( isAuto, TypeInfo.isAuto );
        newSymbol.getTypeInfo().setBit( isExtern, TypeInfo.isExtern );
        newSymbol.getTypeInfo().setBit( isRegister, TypeInfo.isRegister );
        newSymbol.getTypeInfo().setBit( isStatic, TypeInfo.isStatic );
        newSymbol.getTypeInfo().setBit( abstractDeclaration.isConst(), TypeInfo.isConst );
        newSymbol.getTypeInfo().setBit( abstractDeclaration.isVolatile(), TypeInfo.isVolatile );
    }
    
    protected ISymbol cloneSimpleTypeSymbol(
        String name,
        IASTAbstractDeclaration abstractDeclaration,
        List references) throws ASTSemanticException
    {
//    	assert abstractDeclaration.getTypeSpecifier() != null : this;
        ISymbol newSymbol = null;
		ISymbol symbolToBeCloned = null;		
        if( abstractDeclaration.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
        {
        	symbolToBeCloned = ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol();
            references.addAll( ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getReferences() );
        }
        else if( abstractDeclaration.getTypeSpecifier() instanceof ASTClassSpecifier )  
        {
            symbolToBeCloned = pst.newSymbol(name, TypeInfo.t_type);
            symbolToBeCloned.setTypeSymbol(((ASTClassSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol());
		}
		else if( abstractDeclaration.getTypeSpecifier() instanceof ASTElaboratedTypeSpecifier ) 
		{
			ASTElaboratedTypeSpecifier elab = ((ASTElaboratedTypeSpecifier)abstractDeclaration.getTypeSpecifier());
			symbolToBeCloned = pst.newSymbol(name, TypeInfo.t_type);
			symbolToBeCloned.setTypeSymbol(elab.getSymbol());
			if( elab.getSymbol() != null )
				references.add( createReference( elab.getSymbol(), elab.getName(), elab.getNameOffset()) );
		} 
		else if ( abstractDeclaration.getTypeSpecifier() instanceof ASTEnumerationSpecifier )
		{
			symbolToBeCloned = pst.newSymbol( name, TypeInfo.t_type );
			symbolToBeCloned.setTypeSymbol(((ASTEnumerationSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol());
		}
		if( symbolToBeCloned != null ){
			newSymbol = (ISymbol) symbolToBeCloned.clone(); 
			newSymbol.setName( name );
		}

        return newSymbol;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
	public IASTField createField(
		IASTScope scope,
		ITokenDuple name,
		boolean isAuto,
		IASTInitializerClause initializerClause,
		IASTExpression bitfieldExpression,
		IASTAbstractDeclaration abstractDeclaration,
		boolean isMutable,
		boolean isExtern,
		boolean isRegister,
		boolean isStatic,
		int startingOffset,
		int startingLine,
		int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, ASTAccessVisibility visibility) throws ASTSemanticException
	{
		return createField(scope, name,isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, 
		isRegister, isStatic, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, constructorExpression, visibility, null);		
	}
	
    public IASTField createField(
        IASTScope scope,
        ITokenDuple name,
        boolean isAuto,
        IASTInitializerClause initializerClause,
        IASTExpression bitfieldExpression,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        int startingOffset,
        int startingLine,
        int nameOffset, 
        int nameEndOffset, 
        int nameLine,
        IASTExpression constructorExpression, ASTAccessVisibility visibility, List references) throws ASTSemanticException
    {
		IContainerSymbol ownerScope = scopeToSymbol( scope );		

		if(references == null)
			references = new ArrayList();
		ISymbol newSymbol = cloneSimpleTypeSymbol(name.toString(), abstractDeclaration, references);
		if( newSymbol == null )
			handleProblem( IProblem.SEMANTICS_RELATED, name.toString() );
			
		
		setVariableTypeInfoBits(
			isAuto,
			abstractDeclaration,
			isMutable,
			isExtern,
			isRegister,
			isStatic,
			newSymbol);
		setPointerOperators( newSymbol, abstractDeclaration.getPointerOperators(), abstractDeclaration.getArrayModifiers() );
		
		newSymbol.setIsForwardDeclaration(isStatic);
		boolean previouslyDeclared = false;
		if(!isStatic){
			List fieldReferences = new ArrayList();		
			ISymbol fieldDeclaration = lookupQualifiedName(ownerScope, name.toString(), fieldReferences, false, LookupType.FORDEFINITION);                
			
			if( fieldDeclaration != null && newSymbol.getType() == fieldDeclaration.getType() )
			{
				if( !newSymbol.isType( TypeInfo.t_type ) ||
					(newSymbol.isType( TypeInfo.t_type ) && newSymbol.getTypeSymbol() != fieldDeclaration.getTypeSymbol() ) )
				{
					previouslyDeclared = true;
					fieldDeclaration.setTypeSymbol( newSymbol );
					// set the definition visibility = declaration visibility
					ASTReference reference = (ASTReference) fieldReferences.iterator().next();
					visibility = ((IASTField)reference.getReferencedElement()).getVisiblity();					}
			}
		}
		
		try
		{
			ownerScope.addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), name.toString() );
		}
		
		ASTField field = new ASTField( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, previouslyDeclared, constructorExpression, visibility );
		attachSymbolExtension(newSymbol, field, !isStatic );
		return field;        


    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, java.util.List, boolean, int)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(
        IASTScope scope,
        List templateParameters,
        boolean exported,
        int startingOffset, int startingLine) throws ASTSemanticException
    {
    	ITemplateSymbol template = pst.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );

		// the lookup requires a list of type infos
		// instead of a list of IASTParameterDeclaration
		Iterator iter = templateParameters.iterator();
		while (iter.hasNext()){
			ASTTemplateParameter param = (ASTTemplateParameter)iter.next();
			try {
				template.addTemplateParameter( param.getSymbol() );
			} catch (ParserSymbolTableException e) {
				handleProblem( e.createProblemID(), param.getName(), startingOffset, -1, startingLine );
			}
		}
		
		ASTTemplateDeclaration ast = new ASTTemplateDeclaration( template, scope, templateParameters);
		
        attachSymbolExtension( template, ast, false );

        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration, java.util.List)
     */
    public IASTTemplateParameter createTemplateParameter(
        ParamKind kind,
        String identifier,
        IASTTypeId defaultValue,
        IASTParameterDeclaration parameter,
        List parms, 
		IASTCodeScope parameterScope ) throws ASTSemanticException
    {
    	ISymbol symbol = null;
    	if( kind == ParamKind.TEMPLATE_LIST ){
    		ITemplateSymbol template = pst.newTemplateSymbol( identifier );
    		template.setType( TypeInfo.t_templateParameter );
    		template.getTypeInfo().setTemplateParameterType( TypeInfo.t_template );
    		
    		Iterator iter = parms.iterator();
    		while (iter.hasNext()){
    			ASTTemplateParameter param = (ASTTemplateParameter)iter.next();
    			try {
    				template.addTemplateParameter( param.getSymbol() );
    			} catch (ParserSymbolTableException e) {
    				handleProblem( e.createProblemID(), param.getName(), param.getStartingOffset(), param.getEndingOffset(), param.getStartingLine() );  //$NON-NLS-1$
    			}
    		}
    		symbol = template;
     	} else {
    		symbol = pst.newSymbol( identifier, TypeInfo.t_templateParameter );
    		if(  kind == ParamKind.CLASS || kind == ParamKind.TYPENAME ){
        		symbol.getTypeInfo().setTemplateParameterType( TypeInfo.t_typeName );
        	} else /*ParamKind.PARAMETER*/ {
        		pst.newSymbol( identifier, TypeInfo.t_templateParameter );
           		symbol.setName( parameter.getName() );
        		symbol.setTypeInfo( ((ASTSimpleTypeSpecifier)parameter.getTypeSpecifier()).getSymbol().getTypeInfo() );
        		symbol.getTypeInfo().setTemplateParameterType( symbol.getType() );
        		symbol.setType( TypeInfo.t_templateParameter );
        		
        		setPointerOperators( symbol, parameter.getPointerOperators(), parameter.getArrayModifiers() );
        	}
    	}

    	IContainerSymbol codeScope = ((ASTCodeScope)parameterScope).getContainerSymbol();
    	try {
			codeScope.addSymbol( symbol );
		} catch (ParserSymbolTableException e) {
		}
		
		if( defaultValue != null ){
			try {
				symbol.getTypeInfo().setDefault( defaultValue.getTypeSymbol().getTypeInfo() );
			} catch (ASTNotImplementedException e1) {
			}
		}
    	ASTTemplateParameter ast = new ASTTemplateParameter( symbol, defaultValue,  parameter, parms );
    	
   	    attachSymbolExtension( symbol, ast, false );
        
        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateInstantiation createTemplateInstantiation(
        IASTScope scope,
        int startingOffset, int startingLine)
    {
        return new ASTTemplateInstantiation( scope );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateSpecialization createTemplateSpecialization(
        IASTScope scope,
        int startingOffset, int startingLine)
    {
        ITemplateSymbol template = pst.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );

        ASTTemplateSpecialization ast = new ASTTemplateSpecialization( template, scope );
		
        attachSymbolExtension( template, ast, false );

        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, int, int)
     */
    public IASTTypedefDeclaration createTypedef(
        IASTScope scope,
        String name,
        IASTAbstractDeclaration mapping,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine) throws ASTSemanticException
    {
    	IContainerSymbol containerSymbol = scopeToSymbol(scope);
    	ISymbol newSymbol = pst.newSymbol( name, TypeInfo.t_type);
    	newSymbol.getTypeInfo().setBit( true,TypeInfo.isTypedef );
    
		ISymbol typeSymbol = cloneSimpleTypeSymbol( EMPTY_STRING, mapping, new ArrayList() ); 
		setPointerOperators( typeSymbol, mapping.getPointerOperators(), mapping.getArrayModifiers() );
		
		newSymbol.setTypeSymbol( typeSymbol );
		
    	List references = new ArrayList();
		if( mapping.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
	    {
			references.addAll( ((ASTSimpleTypeSpecifier)mapping.getTypeSpecifier()).getReferences() );
		}
    	
    	try
        {
            containerSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e)
        {
        	handleProblem(e.createProblemID(), name );
        }
        ASTTypedef d = new ASTTypedef( newSymbol, mapping, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references );
        attachSymbolExtension(newSymbol, d, true );
        return d; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int)
     */
    public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration(
        IASTScope scope,
        IASTTypeSpecifier typeSpecifier,
        IASTTemplate template,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine, boolean isFriend)
    {
        return new ASTAbstractTypeSpecifierDeclaration( scopeToSymbol(scope), typeSpecifier, template, startingOffset, startingLine, endingOffset, endingLine, isFriend);
    }

    
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(IASTScope scope, ASTClassKind kind, ITokenDuple name, int startingOffset, int startingLine, int endOffset, int endingLine, boolean isForewardDecl, boolean isFriend) throws ASTSemanticException
    {
		IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
		IContainerSymbol originalScope = currentScopeSymbol;
		
		TypeInfo.eType pstType = classKindToTypeInfo(kind);
		List references = new ArrayList();
		IToken nameToken = name.getFirstToken();
		String newSymbolName = EMPTY_STRING; 
		List templateIdArgList = null;
		boolean isTemplateId = false;
		if (name.getSegmentCount() != 1) // qualified name
		{
			ITokenDuple containerSymbolName = name.getLeadingSegments();
			currentScopeSymbol = (IContainerSymbol) lookupQualifiedName(
					currentScopeSymbol, containerSymbolName, references, true);
			if (currentScopeSymbol == null)
				handleProblem(IProblem.SEMANTIC_NAME_NOT_FOUND,
						containerSymbolName.toString(), containerSymbolName
								.getFirstToken().getOffset(),
						containerSymbolName.getLastToken().getEndOffset(),
						containerSymbolName.getLastToken().getLineNumber());
			nameToken = name.getLastSegment().getFirstToken();
		}
		//template-id
		List[] array = name.getTemplateIdArgLists();
		if (array != null) {
			isTemplateId = true;
			templateIdArgList = array[array.length - 1];
		}
		newSymbolName = nameToken.getImage();
		ISymbol checkSymbol = null;
		if (!isTemplateId) {
			try {
				if (isFriend) {
					checkSymbol = ((IDerivableContainerSymbol) currentScopeSymbol)
							.lookupForFriendship(newSymbolName);
				} else {
					checkSymbol = currentScopeSymbol.elaboratedLookup(pstType,
							newSymbolName);
				}
			} catch (ParserSymbolTableException e) {
				handleProblem(e.createProblemID(), nameToken.getImage(),
						nameToken.getOffset(), nameToken.getEndOffset(),
						nameToken.getLineNumber());
			}
		}
		List args = null;
		if (isTemplateId) {
			args = getTemplateArgList(templateIdArgList);
		}
		if (scope instanceof IASTTemplateInstantiation) {
			if (isTemplateId) {
				checkSymbol = pst.newDerivableContainerSymbol(newSymbolName,
						pstType);
				try {
					currentScopeSymbol.addTemplateId(checkSymbol, args);
				} catch (ParserSymbolTableException e) {
					handleProblem(e.createProblemID(), nameToken.getImage(),
							nameToken.getOffset(), nameToken.getEndOffset(),
							nameToken.getLineNumber());
				}
			} else {
				handleProblem(IProblem.SEMANTIC_INVALID_TEMPLATE, nameToken
						.getImage());
			}
			checkSymbol = ((ASTTemplateInstantiation) scope)
					.getInstanceSymbol();
		} else if (checkSymbol == null) {
			checkSymbol = pst.newDerivableContainerSymbol(newSymbolName,
					pstType);
			checkSymbol.setIsForwardDeclaration(true);
			try {
				if (isFriend) {
					((IDerivableContainerSymbol) originalScope).addFriend(checkSymbol);
				} else {
					if (!isTemplateId)
						currentScopeSymbol.addSymbol(checkSymbol);
					else
						currentScopeSymbol.addTemplateId(checkSymbol, args);
				}
			} catch (ParserSymbolTableException e1) {
				handleProblem(e1.createProblemID(), nameToken.getImage(),
						nameToken.getOffset(), nameToken.getEndOffset(),
						nameToken.getLineNumber());
			}
			ASTElaboratedTypeSpecifier elab = new ASTElaboratedTypeSpecifier(
					checkSymbol, kind, startingOffset, startingLine, name
							.getFirstToken().getOffset(), name.getLastToken()
							.getEndOffset(), name.getLastToken()
							.getLineNumber(), endOffset, endingLine,
					references, isForewardDecl);
			attachSymbolExtension(checkSymbol, elab, !isForewardDecl);
		} else if (isFriend) {
			((IDerivableContainerSymbol) originalScope).addFriend(checkSymbol);
		}
		if (checkSymbol != null) {
			if (scope instanceof IASTTemplateInstantiation) {
				addReference(references, createReference(checkSymbol,
						newSymbolName, nameToken.getOffset()));
			}
			if (checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTClassSpecifier
					|| checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTEnumerationSpecifier) {
				ASTElaboratedTypeSpecifier elab = new ASTElaboratedTypeSpecifier(
						checkSymbol, kind, startingOffset, startingLine, name
								.getFirstToken().getOffset(), name
								.getLastToken().getEndOffset(), name
								.getLastToken().getLineNumber(), endOffset,
						endingLine, references, isForewardDecl);
				attachSymbolExtension(checkSymbol, elab, !isForewardDecl);
				return elab;
			}
			if (checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTElaboratedTypeSpecifier)
				return (IASTElaboratedTypeSpecifier) checkSymbol
						.getASTExtension().getPrimaryDeclaration();
		} else {
			handleProblem(IProblem.SEMANTIC_NAME_NOT_FOUND, newSymbolName,
					nameToken.getOffset(), nameToken.getEndOffset(), nameToken
							.getLineNumber());
		}
	
		
//		assert false : this;
		return null;
    }

    protected ParserSymbolTable pst;


    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceAlias(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *      java.lang.String, org.eclipse.cdt.core.parser.ITokenDuple, int, int,
	 *      int)
	 */
    public IASTNamespaceAlias createNamespaceAlias(IASTScope scope, String identifier, ITokenDuple alias, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endingLine) throws ASTSemanticException
    {
        IContainerSymbol startingSymbol = scopeToSymbol(scope);
        List references = new ArrayList();
        
        ISymbol namespaceSymbol = lookupQualifiedName( startingSymbol, alias, references, true );
        
        if( namespaceSymbol.getType() != TypeInfo.t_namespace )
        	handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, alias.toString(), startingOffset, endOffset, startingLine );
        
        ISymbol newSymbol = pst.newContainerSymbol( identifier, TypeInfo.t_namespace );
        newSymbol.setTypeSymbol( namespaceSymbol );
        
        try
        {
            startingSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e)
        {
        	handleProblem( e.createProblemID(), identifier, startingOffset, endOffset, startingLine );
        }
        
        ASTNamespaceAlias astAlias = new ASTNamespaceAlias(
        	newSymbol, alias.toString(), (IASTNamespaceDefinition)namespaceSymbol.getASTExtension().getPrimaryDeclaration(), 
        	startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endOffset, endingLine, references ); 
        attachSymbolExtension( newSymbol, astAlias, true );
        return astAlias;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope) {
		IContainerSymbol symbol = scopeToSymbol( scope );
		
		IContainerSymbol newScope = pst.newContainerSymbol(EMPTY_STRING, TypeInfo.t_block); 
		newScope.setContainingSymbol(symbol);
		
		ASTCodeScope codeScope = new ASTCodeScope( newScope );
		attachSymbolExtension( newScope, codeScope, true );
		return codeScope;
	}

	public IASTScope getDeclaratorScope(IASTScope scope, ITokenDuple duple){
		if( duple != null && duple.getSegmentCount() > 1){
			List refs = new ArrayList();
			
			IContainerSymbol ownerScope = scopeToSymbol( scope );
			ISymbol symbol;
			
			try {
				symbol = lookupQualifiedName( ownerScope, duple.getLeadingSegments(), refs, false, LookupType.FORDEFINITION );
			} catch (ASTSemanticException e) {
				return scope;
			}
			
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if( parentScope != null && parentScope.getASTExtension() != null ){
				if( scope instanceof IASTTemplateDeclaration || scope instanceof IASTTemplateSpecialization ){
					symbol = scopeToSymbol( scope );
					if( symbol instanceof ITemplateFactory ){
						symbol.setContainingSymbol( parentScope );
					}
					return scope;
				}
				return (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();
			}
		}
		
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#queryIsTypeName(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean queryIsTypeName(IASTScope scope, ITokenDuple nameInQuestion) {
		ISymbol lookupSymbol = null;
		try {
			lookupSymbol =
				lookupQualifiedName(
					scopeToSymbol(scope),
					nameInQuestion,
					new ArrayList(),
					false);
		} catch (ASTSemanticException e) {
			// won't get thrown
		} 
		if( lookupSymbol == null ) return false;
		if( lookupSymbol.isType( TypeInfo.t_type, TypeInfo.t_enumeration ) ||
			(lookupSymbol.isType( TypeInfo.t_templateParameter ) && lookupSymbol.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName ) ||
			(lookupSymbol.getASTExtension() != null && lookupSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTTypedefDeclaration ) )
		{
			return true;
		}
		return false;
	}

    public IASTParameterDeclaration createParameterDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, String parameterName, IASTInitializerClause initializerClause, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine)
    {
        return new ASTParameterDeclaration( null, isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp, parameterName, initializerClause, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeId(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, java.util.List, java.util.List)
     */
    public IASTTypeId createTypeId(IASTScope scope, Type kind, boolean isConst, boolean isVolatile, boolean isShort, 
	boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, ITokenDuple name, List pointerOps, List arrayMods, String completeSignature) throws ASTSemanticException
    {
        ASTTypeId result = 
        	new ASTTypeId( kind, name, pointerOps, arrayMods, completeSignature,   
        	isConst, isVolatile, isUnsigned, isSigned, isShort, isLong, isTypename );
        result.setTypeSymbol( createSymbolForTypeId( scope, result ) );
        return result;
    }

	 /**
	  * @param id
	  * @return
	  */
	 public static TypeInfo.eType getTypeKind(IASTTypeId id)
	 {
		 IASTSimpleTypeSpecifier.Type type = id.getKind();
		 if( type == IASTSimpleTypeSpecifier.Type.BOOL )
			 return TypeInfo.t_bool;
		 else if( type == IASTSimpleTypeSpecifier.Type._BOOL )
		     return TypeInfo.t__Bool;
		 else if( type == IASTSimpleTypeSpecifier.Type.CHAR )
			 return TypeInfo.t_char;
		 else if ( type == IASTSimpleTypeSpecifier.Type.WCHAR_T )
		 	 return TypeInfo.t_wchar_t;
		 else if( type == IASTSimpleTypeSpecifier.Type.DOUBLE )
			 return TypeInfo.t_double;
		 else if( type == IASTSimpleTypeSpecifier.Type.FLOAT )
			 return TypeInfo.t_float;
		 else if( type == IASTSimpleTypeSpecifier.Type.INT )
			 return TypeInfo.t_int;
		 else if( type == IASTSimpleTypeSpecifier.Type.VOID )
			 return TypeInfo.t_void;
		 else if( id.isShort() || id.isLong() || id.isUnsigned() || id.isSigned() )
		 	return TypeInfo.t_int;
		 else 
			 return TypeInfo.t_type;
	 }
	 
	protected ISymbol createSymbolForTypeId( IASTScope scope, IASTTypeId id ) throws ASTSemanticException
	{
		if( id == null ) return null;
    	
    	ASTTypeId typeId = (ASTTypeId)id;
		ISymbol result = pst.newSymbol( EMPTY_STRING, CompleteParseASTFactory.getTypeKind(id)); 
    	
    	result.getTypeInfo().setBit( id.isConst(), TypeInfo.isConst );
		result.getTypeInfo().setBit( id.isVolatile(), TypeInfo.isVolatile );
		
		result.getTypeInfo().setBit( id.isShort(), TypeInfo.isShort);
		result.getTypeInfo().setBit( id.isLong(), TypeInfo.isLong);
		result.getTypeInfo().setBit( id.isUnsigned(), TypeInfo.isUnsigned);
		result.getTypeInfo().setBit( id.isSigned(), TypeInfo.isSigned );
		
		List refs = new ArrayList();
		if( result.getType() == TypeInfo.t_type )
		{
			ISymbol typeSymbol = lookupQualifiedName( scopeToSymbol(scope), typeId.getTokenDuple(), refs, true );
			if( typeSymbol == null || typeSymbol.getType() == TypeInfo.t_type )
				handleProblem( scope, IProblem.SEMANTIC_INVALID_TYPE, id.getTypeOrClassName() );
            result.setTypeSymbol( typeSymbol );
            typeId.addReferences( refs );
		}		
		
		setPointerOperators( result, id.getPointerOperators(), id.getArrayModifiers() );
		return result;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#signalEndOfClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
     */
    public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier)
    {
    	ASTClassSpecifier astImplementation = (ASTClassSpecifier)astClassSpecifier;
		try
        { 
			((IDerivableContainerSymbol)(astImplementation).getSymbol()).addCopyConstructor();
        }
        catch (ParserSymbolTableException e)
        {
        	// do nothing, this is best effort
        }
        
        astImplementation.setProcessingUnresolvedReferences( true );
        Iterator i = astImplementation.getUnresolvedReferences();
        List references = new ArrayList();
        while( i.hasNext() )
        {	
        	UnresolvedReferenceDuple duple = (UnresolvedReferenceDuple) i.next();
        	ISymbol s = null;
        	
        	List subReferences = new ArrayList();
        	try
			{
        		s = lookupQualifiedName( duple.getScope(), duple.getName(), subReferences, false );
        	}
        	catch( ASTSemanticException ase )
			{
        	}
        	
        	if( s != null )
        		references.addAll( subReferences );
        	
        	
        }
        
        astImplementation.setProcessingUnresolvedReferences( false );
        
        if( ! references.isEmpty() )
        	astImplementation.setExtraReferences( references );
        
    }

    public IASTInitializerClause createInitializerClause(IASTScope scope, IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses, List designators)
    {
    	return new ASTInitializerClause( kind, assignmentExpression, initializerClauses, designators );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getCompletionContext(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTNode getCompletionContext(Kind kind, IASTExpression expression) {
		IContainerSymbol context = null;
		try {
			context = getSearchScope( kind, expression, null );
		} catch (ASTSemanticException e) {
			return null;
		}
		
		if( context != null ){
			ISymbolASTExtension extension = context.getASTExtension();
			return ( extension != null ) ? extension.getPrimaryDeclaration() : null;
		}
		
		return null;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#lookupSymbolInContext(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTNode lookupSymbolInContext(IASTScope scope, ITokenDuple duple) throws ASTNotImplementedException {
		ISymbol s = null;
		try {
			s = lookupQualifiedName( scopeToSymbol( scope ), duple, new ArrayList(), false );
		} catch (ASTSemanticException e) {
		}
		if ( s == null ) return null;
		return s.getASTExtension().getPrimaryDeclaration();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getNodeForThisExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTNode expressionToASTNode(IASTScope scope, IASTExpression expression) {
		if( expression == null ) return null;
		if( expression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION )
			if( expression instanceof ASTExpression)
			{
				try {
					return lookupSymbolInContext(scope, ((ASTExpression)expression).getIdExpressionTokenDuple());
				} catch (ASTNotImplementedException e) {
//	            	assert false : e;
				}
			}
		
		return null;
	}
}
