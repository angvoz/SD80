/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.ast;
import java.util.List;

import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
/**
 * @author jcamelon
 *
 */
public interface IASTFactory
{
    public IASTMacro createMacro(
        String name,
        int startingOffset,
        int startingLine,
        int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, IMacroDescriptor info);
        
    public IASTInclusion createInclusion(
        String name,
        String fileName,
        boolean local,
        int startingOffset,
        int startingLine,
        int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine) ;
        
    public IASTUsingDirective createUsingDirective(
        IASTScope scope,
        ITokenDuple duple, int startingOffset, int startingLine, int endingOffset, int endingLine)
        throws ASTSemanticException;
        
    public IASTUsingDeclaration createUsingDeclaration(
        IASTScope scope,
        boolean isTypeName,
        ITokenDuple name, int startingOffset, int startingLine, int endingOffset, int endingLine) throws ASTSemanticException;
        
    public IASTASMDefinition createASMDefinition(
        IASTScope scope,
        String assembly,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine);

    public IASTNamespaceDefinition createNamespaceDefinition(
        IASTScope scope,
        String identifier,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLineNumber) throws ASTSemanticException;
        
    public IASTNamespaceAlias    createNamespaceAlias( 
    	IASTScope scope, 
    	String identifier, 
    	ITokenDuple alias, 
    	int startingOffset, 
    	int startingLine, 
    	int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endingLine ) throws ASTSemanticException;
        
    public IASTCompilationUnit createCompilationUnit() ;

    public IASTLinkageSpecification createLinkageSpecification(
        IASTScope scope,
        String spec, int startingOffset, int startingLine) ;

    public IASTClassSpecifier createClassSpecifier(
        IASTScope scope,
        ITokenDuple name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine) throws ASTSemanticException;

    /**
     * @param astClassSpec
     * @param isVirtual
     * @param visibility
     * @param string
     */
    public void addBaseSpecifier(
        IASTClassSpecifier astClassSpec,
        boolean isVirtual,
        ASTAccessVisibility visibility,
        ITokenDuple parentClassName) throws ASTSemanticException;

    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(
        IASTScope scope,
        ASTClassKind elaboratedClassKind,
        ITokenDuple typeName,
        int startingOffset, int startingLine, int endOffset, int endingLine, boolean isForewardDecl, boolean isFriend) throws ASTSemanticException;

    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        String name,
        int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine) throws ASTSemanticException;

    public void addEnumerator(
        IASTEnumerationSpecifier enumeration,
        String string,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endLine, IASTExpression initialValue)throws ASTSemanticException;

    public IASTExpression createExpression(
        IASTScope scope,
        IASTExpression.Kind kind,
        IASTExpression lhs,
        IASTExpression rhs,
        IASTExpression thirdExpression,
        IASTTypeId typeId,
        ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) throws ASTSemanticException;

    public IASTExpression.IASTNewExpressionDescriptor createNewDescriptor(List newPlacementExpressions,List newTypeIdExpressions,List newInitializerExpressions);

    public IASTInitializerClause createInitializerClause(
        IASTScope scope,
        IASTInitializerClause.Kind kind,
        IASTExpression assignmentExpression, List initializerClauses, List designators) ;

    public IASTExceptionSpecification createExceptionSpecification(IASTScope scope, List typeIds) throws ASTSemanticException;

    /**
     * @param exp
     */
    public IASTArrayModifier createArrayModifier(IASTExpression exp);

    /**
     * @param duple
     * @param expressionList
     * @return
     */

    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        IASTScope scope,
        ITokenDuple duple, IASTExpression expressionList) throws ASTSemanticException;

    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        IASTScope scope,
        IASTSimpleTypeSpecifier.Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned,
        boolean isUnsigned, boolean isTypename, boolean isComplex, boolean isImaginary) throws ASTSemanticException;

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
		boolean isVirtual, boolean isExplicit, boolean isPureVirtual, List constructorChain, boolean isDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException;
    
    
    public IASTAbstractDeclaration createAbstractDeclaration(
        boolean isConst,
        boolean isVolatile,
        IASTTypeSpecifier typeSpecifier,
        List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOperator);
    
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
        boolean isVirtual, boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean isDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments) throws ASTSemanticException;
        
	public IASTVariable createVariable(IASTScope scope, ITokenDuple name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, 
		   IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression ) throws ASTSemanticException;
		   
	public IASTField createField( IASTScope scope, ITokenDuple name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, ASTAccessVisibility visibility) throws ASTSemanticException;

	public IASTDesignator createDesignator( IASTDesignator.DesignatorKind kind, IASTExpression constantExpression, IToken fieldIdentifier );
	
	public IASTParameterDeclaration createParameterDeclaration( boolean isConst, boolean isVolatile, IASTTypeSpecifier getTypeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, String parameterName, IASTInitializerClause initializerClause, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine ) ;
	
	public IASTTemplateDeclaration createTemplateDeclaration( IASTScope scope, List templateParameters, boolean exported, int startingOffset, int startingLine ) throws ASTSemanticException; 

	public IASTTemplateParameter createTemplateParameter( IASTTemplateParameter.ParamKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List parms ) throws ASTSemanticException; 

	public IASTTemplateInstantiation createTemplateInstantiation(IASTScope scope, int startingOffset, int startingLine); 
	
	public IASTTemplateSpecialization createTemplateSpecialization(IASTScope scope, int startingOffset, int startingLine); 
	
	public IASTTypedefDeclaration createTypedef( IASTScope scope, String name, IASTAbstractDeclaration mapping, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine ) throws ASTSemanticException;

	public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration( IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate template, int startingOffset, int startingLine, int endingOffset, int endingLine);
	
	public boolean queryIsTypeName( IASTScope scope, ITokenDuple nameInQuestion ) ;

	static final String DOUBLE_COLON = "::"; //$NON-NLS-1$
	static final String TELTA = "~"; //$NON-NLS-1$
	/**
	 * @param scope
	 * @return
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope);
	
	public IASTTypeId    createTypeId( IASTScope scope, IASTSimpleTypeSpecifier.Type kind, boolean isConst, boolean isVolatile, boolean isShort, 
			boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, ITokenDuple name, List pointerOps, List arrayMods ) throws ASTSemanticException;
    /**
     * @param astClassSpecifier
     */
    public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier);

	/**
	 * @param kind
	 * @param firstExpression
	 */
	public IASTNode getCompletionContext(Kind kind, IASTExpression expression);
	
	public IASTNode lookupSymbolInContext( IASTScope scope, ITokenDuple duple ) throws ASTNotImplementedException;

	/**
	 * @param log
	 */
	public void setLogger(IParserLogService log);
}