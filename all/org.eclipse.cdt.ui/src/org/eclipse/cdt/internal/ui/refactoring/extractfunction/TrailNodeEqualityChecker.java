/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;



import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.EqualityChecker;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;

public class TrailNodeEqualityChecker implements EqualityChecker<IASTNode> {

	private final Map<String, Integer> names;
	private final Container<Integer> namesCounter;
	
	public TrailNodeEqualityChecker(Map<String, Integer> names, Container<Integer> namesCounter) {
		super();
		this.names = names;
		this.namesCounter = namesCounter;
	}
	
	public boolean isEquals(IASTNode trailNode, IASTNode node) {
		if( (trailNode instanceof TrailName && node instanceof IASTName)
				|| Arrays.equals(getInterfaces(node), getInterfaces(trailNode)) ) {
			//Is same type
			if(node instanceof IASTExpression){
				return isExpressionEquals(trailNode, node);
			} else if(node instanceof IASTStatement){
				return isStatementEquals(trailNode, node);
			} else if(node instanceof IASTPointerOperator){
				return isPointerOperatorEquals(trailNode, node);
			} else if(node instanceof IASTDeclaration){
				return isDeclarationEquals(trailNode, node);
			} else if(node instanceof IASTDeclarator){	
				return isDeclaratorEquals(trailNode, node);
			} else if(node instanceof IASTInitializer){
				//no speciality, is the same type return true
				return true;
			} else if(node instanceof IASTDeclSpecifier){
				return isDeclSpecifierEquals(trailNode, node);
			} else if(node instanceof IASTName){
				return isNameEquals(trailNode, node);
			} else {
				Assert.isLegal(false, "Unexpected Node, this code shoud nod reached"); //$NON-NLS-1$
				return true;
			}
		}
		return false;
		
		
	}

	private boolean isNameEquals(IASTNode trailNode, IASTNode node) {
		if(trailNode instanceof ICPPASTConversionName) {
			return true;
		} else if(trailNode instanceof ICPPASTOperatorName) {
			ICPPASTOperatorName trailName= ( ICPPASTOperatorName )trailNode;
			ICPPASTOperatorName name = ( ICPPASTOperatorName )node;

			return trailName.equals(name);
		} else if(trailNode instanceof TrailName && node instanceof IASTName) {
			TrailName trailName = (TrailName) trailNode;
			IASTName name = (IASTName)node;
			
			return isNameEquals(trailName, name); 
		} else {
			return true;
		}
	}

	private boolean isDeclSpecifierEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IGPPASTSimpleDeclSpecifier) {
			IGPPASTSimpleDeclSpecifier trailSimpleDecl = (IGPPASTSimpleDeclSpecifier) trailNode;
			IGPPASTSimpleDeclSpecifier simpleDecl = (IGPPASTSimpleDeclSpecifier) node;
			
			return isSimpleDeclSpecifierEquals(trailSimpleDecl, simpleDecl)
				&& trailSimpleDecl.isComplex() == simpleDecl.isComplex()
				&& trailSimpleDecl.isImaginary() == simpleDecl.isImaginary()
				&& trailSimpleDecl.isLongLong() == simpleDecl.isLongLong()
				&& trailSimpleDecl.isComplex() == simpleDecl.isComplex()
				&& trailSimpleDecl.isExplicit() == simpleDecl.isExplicit()
				&& trailSimpleDecl.isFriend() == simpleDecl.isFriend();
		} else if (trailNode instanceof IGPPASTDeclSpecifier) {
			IGPPASTDeclSpecifier trailDecl = (IGPPASTDeclSpecifier) trailNode;
			IGPPASTDeclSpecifier decl = (IGPPASTDeclSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.isRestrict() == decl.isRestrict();
		} else if (trailNode instanceof ICASTSimpleDeclSpecifier) {
			ICASTSimpleDeclSpecifier trailDecl = (ICASTSimpleDeclSpecifier) trailNode;
			ICASTSimpleDeclSpecifier decl = (ICASTSimpleDeclSpecifier) node;
			
			return isSimpleDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.isRestrict() 	== decl.isRestrict()
				&& trailDecl.isComplex() 	== decl.isComplex()
				&& trailDecl.isImaginary() 	== decl.isImaginary()
				&& trailDecl.isLongLong() 	== decl.isLongLong();
		} else if (trailNode instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier trailDecl = (IASTSimpleDeclSpecifier) trailNode;
			IASTSimpleDeclSpecifier decl = (IASTSimpleDeclSpecifier) node;
			
			return isSimpleDeclSpecifierEquals(trailDecl, decl);
		} else if (trailNode instanceof ICPPASTNamedTypeSpecifier) {
			ICPPASTNamedTypeSpecifier trailDecl = (ICPPASTNamedTypeSpecifier) trailNode;
			ICPPASTNamedTypeSpecifier decl = (ICPPASTNamedTypeSpecifier) node;
			
			
			boolean isSame = isDeclSpecifierEquals(trailDecl, decl)
								&& isSameNamedTypeSpecifierName(trailDecl, decl)
								&& trailDecl.isTypename() 	== decl.isTypename()
								&& trailDecl.isExplicit() 	== decl.isExplicit()
								&& trailDecl.isFriend() 	== decl.isFriend()
								&& trailDecl.isVirtual() 	== decl.isVirtual();
			return isSame;
		} else if (trailNode instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier trailDecl = (IASTNamedTypeSpecifier) trailNode;
			IASTNamedTypeSpecifier decl = (IASTNamedTypeSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& isSameNamedTypeSpecifierName(trailDecl, decl);
		} else if (trailNode instanceof IASTElaboratedTypeSpecifier) {
			IASTElaboratedTypeSpecifier trailDecl = (IASTElaboratedTypeSpecifier) trailNode;
			IASTElaboratedTypeSpecifier decl = (IASTElaboratedTypeSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.getKind() 	== decl.getKind();
		} else if (trailNode instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier trailDecl = (IASTCompositeTypeSpecifier) trailNode;
			IASTCompositeTypeSpecifier decl = (IASTCompositeTypeSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.getKey() 	== decl.getKey();
		} else if (trailNode instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier trailDecl = (ICPPASTDeclSpecifier) trailNode;
			ICPPASTDeclSpecifier decl = (ICPPASTDeclSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.isExplicit() 	== decl.isExplicit()
				&& trailDecl.isFriend() 	== decl.isFriend()
				&& trailDecl.isVirtual() 	== decl.isVirtual();
		} else if (trailNode instanceof ICASTDeclSpecifier) {
			ICASTDeclSpecifier trailDecl = (ICASTDeclSpecifier) trailNode;
			ICASTDeclSpecifier decl = (ICASTDeclSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl)
				&& trailDecl.isRestrict() 	== decl.isRestrict();
		} else if (trailNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier trailDecl = (IASTDeclSpecifier) trailNode;
			IASTDeclSpecifier decl = (IASTDeclSpecifier) node;
			
			return isDeclSpecifierEquals(trailDecl, decl);
		} else {
			//is same
			return true;
		}
	}

	private boolean isDeclaratorEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator trailFunc = (IASTStandardFunctionDeclarator) trailNode;
			IASTStandardFunctionDeclarator func = (IASTStandardFunctionDeclarator) node;
			
			return trailFunc.takesVarArgs() == func.takesVarArgs();
		} else if (trailNode instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator trailFunc = (ICPPASTFunctionDeclarator) trailNode;
			ICPPASTFunctionDeclarator func = (ICPPASTFunctionDeclarator) node;
			
			return trailFunc.isConst() == func.isConst()
				&& trailFunc.isPureVirtual() == func.isPureVirtual()
				&& trailFunc.isVolatile() == func.isVolatile();
		} else {
			//same type
			return true;
		}
	}

	private boolean isDeclarationEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTASMDeclaration) {
			IASTASMDeclaration trailASMDecl = (IASTASMDeclaration) trailNode;
			IASTASMDeclaration asmDecl = (IASTASMDeclaration) node;
			
			return trailASMDecl.getAssembly().equals(asmDecl.getAssembly());
		} else if (trailNode instanceof IGPPASTExplicitTemplateInstantiation) {
			IGPPASTExplicitTemplateInstantiation trailTempl = (IGPPASTExplicitTemplateInstantiation) trailNode;
			IGPPASTExplicitTemplateInstantiation templ = (IGPPASTExplicitTemplateInstantiation) node;
			
			return trailTempl.getModifier() == templ.getModifier();
		} else if (trailNode instanceof ICPPASTLinkageSpecification) {
			ICPPASTLinkageSpecification trailLink = (ICPPASTLinkageSpecification) trailNode;
			ICPPASTLinkageSpecification link = (ICPPASTLinkageSpecification) node;
			
			return trailLink.getLiteral().equals(link.getLiteral());
		} else if (trailNode instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration trailTempl = (ICPPASTTemplateDeclaration) trailNode;
			ICPPASTTemplateDeclaration templ = (ICPPASTTemplateDeclaration) node;
			
			return trailTempl.isExported() == templ.isExported();
		} else if (trailNode instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration trailUsing = (ICPPASTUsingDeclaration) trailNode;
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) node;
			
			return trailUsing.isTypename() == using.isTypename();
		} else if (trailNode instanceof ICPPASTVisibilityLabel) {
			ICPPASTVisibilityLabel trailVisibility = (ICPPASTVisibilityLabel) trailNode;
			ICPPASTVisibilityLabel visibility = (ICPPASTVisibilityLabel) node;
			
			return trailVisibility.getVisibility() == visibility.getVisibility();
		} else {
			//same type
			return true;
		}
	}

	private boolean isPointerOperatorEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IGPPASTPointer) {
			IGPPASTPointer trailGPointer = (IGPPASTPointer) trailNode;
			IGPPASTPointer gPointer = (IGPPASTPointer) node;
			
			return trailGPointer.isConst() == gPointer.isConst()
				&& trailGPointer.isRestrict() == gPointer.isRestrict()
				&& trailGPointer.isVolatile() == gPointer.isVolatile();
		} else if (trailNode instanceof ICASTPointer) {
			ICASTPointer trailCPointer = (ICASTPointer) trailNode;
			ICASTPointer cPointer = (ICASTPointer) node;
			
			return trailCPointer.isConst() == cPointer.isConst()
				&& trailCPointer.isRestrict() == cPointer.isRestrict()
				&& trailCPointer.isVolatile() == cPointer.isVolatile(); 
		} else if (trailNode instanceof IASTPointer) {
			IASTPointer trailCPointer = (IASTPointer) trailNode;
			IASTPointer cPointer = (IASTPointer) node;
			
			return trailCPointer.isConst() == cPointer.isConst()
				&& trailCPointer.isVolatile() == cPointer.isVolatile(); 
		} else {
			//same type
			return true;
		}
	}

	private boolean isStatementEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof ICPPASTCatchHandler) {
			ICPPASTCatchHandler trailCatch = (ICPPASTCatchHandler) trailNode;
			ICPPASTCatchHandler nodeCatch = (ICPPASTCatchHandler) node;
			
			return trailCatch.isCatchAll() == nodeCatch.isCatchAll();
		}
		//same type
		return true;
	}

	private boolean isExpressionEquals(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTBinaryExpression) {
			IASTBinaryExpression trailExpr = (IASTBinaryExpression) trailNode;
			IASTBinaryExpression expr = (IASTBinaryExpression) node;
			
			return trailExpr.getOperator() == expr.getOperator();
		} else if (trailNode instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference trailFieldRef = (ICPPASTFieldReference) trailNode;
			ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) node;
			
			return trailFieldRef.isPointerDereference() == fieldRef.isPointerDereference()
				&& trailFieldRef.isTemplate() == fieldRef.isTemplate();
		} else if (trailNode instanceof IASTFieldReference) {
			IASTFieldReference trailFieldRef = (IASTFieldReference) trailNode;
			IASTFieldReference fieldRef = (IASTFieldReference) node;
			
			return trailFieldRef.isPointerDereference() == fieldRef.isPointerDereference();
		} else if (trailNode instanceof IASTLiteralExpression) {
			IASTLiteralExpression trailLiteral = (IASTLiteralExpression) trailNode;
			IASTLiteralExpression literal = (IASTLiteralExpression) node;
			
			return trailLiteral.getKind() == literal.getKind() && trailLiteral.toString().equals(literal.toString());
		} else if (trailNode instanceof IASTUnaryExpression) {
			IASTUnaryExpression trailExpr = (IASTUnaryExpression) trailNode;
			IASTUnaryExpression expr = (IASTUnaryExpression) node;
			
			return  trailExpr.getOperator() == expr.getOperator();
		} else if (trailNode instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression trailIdExpr = (IASTTypeIdExpression) trailNode;
			IASTTypeIdExpression idExpr = (IASTTypeIdExpression) node;
			
			return trailIdExpr.getTypeId() == idExpr.getTypeId();
		} else if (trailNode instanceof ICPPASTDeleteExpression) {
			ICPPASTDeleteExpression trailDelete = (ICPPASTDeleteExpression) trailNode;
			ICPPASTDeleteExpression delete = (ICPPASTDeleteExpression) node;
			
			return trailDelete.isGlobal() == delete.isGlobal() && trailDelete.isVectored() == delete.isVectored();
		} else if (trailNode instanceof ICPPASTNewExpression) {
			ICPPASTNewExpression trailNew = (ICPPASTNewExpression) trailNode;
			ICPPASTNewExpression nodeNew = (ICPPASTNewExpression) node;
			
			return trailNew.isGlobal() == nodeNew.isGlobal() && trailNew.isNewTypeId() == nodeNew.isNewTypeId();
		} else if (trailNode instanceof ICPPASTSimpleTypeConstructorExpression) {
			ICPPASTSimpleTypeConstructorExpression trailConsExpr = (ICPPASTSimpleTypeConstructorExpression) trailNode;
			ICPPASTSimpleTypeConstructorExpression consExpr = (ICPPASTSimpleTypeConstructorExpression) node;
			
			return trailConsExpr.getSimpleType() == consExpr.getSimpleType();
		} else if (trailNode instanceof ICPPASTTypenameExpression) {
			ICPPASTTypenameExpression trailTypenameExpr = (ICPPASTTypenameExpression) trailNode;
			ICPPASTTypenameExpression typenameExpr = (ICPPASTTypenameExpression) node;
			
			return trailTypenameExpr.isTemplate() == typenameExpr.isTemplate();
		} else {
//			same type
			return true;
		}
	}

	private boolean isSameNamedTypeSpecifierName(IASTNamedTypeSpecifier trailDecl, IASTNamedTypeSpecifier decl) {
		return trailDecl.getName().getRawSignature().equals(decl.getName().getRawSignature());
	}

	private Class<?>[] getInterfaces(IASTNode node) {
		Class<?>[] interfaces = node.getClass().getInterfaces();
		List<Class<?>> interfaceList = Arrays.asList(interfaces);
		Class<?>[] returnArray = new Class[interfaceList.size()];
		return interfaceList.toArray(returnArray);
	}
	
	private boolean isDeclSpecifierEquals(IASTDeclSpecifier trailDeclSpeci, IASTDeclSpecifier declSpeci){
		return  trailDeclSpeci.isConst() 	== declSpeci.isConst()
		&& trailDeclSpeci.isInline() 		== declSpeci.isInline()
		&& trailDeclSpeci.isVolatile() 		== declSpeci.isVolatile()
		&& trailDeclSpeci.getStorageClass() == declSpeci.getStorageClass();
	}

	private boolean isSimpleDeclSpecifierEquals(IASTSimpleDeclSpecifier trailDeclSpeci, IASTSimpleDeclSpecifier declSpeci){
		return isDeclSpecifierEquals(trailDeclSpeci, declSpeci)
		&& trailDeclSpeci.isLong() 			== declSpeci.isLong()
		&& trailDeclSpeci.isShort() 		== declSpeci.isShort()
		&& trailDeclSpeci.isSigned() 		== declSpeci.isSigned()
		&& trailDeclSpeci.isUnsigned() 		== declSpeci.isUnsigned()
		&& trailDeclSpeci.getType() 		== declSpeci.getType();
	}
	
	private boolean isNameEquals(TrailName trailName, IASTName name) {
		int actCount = namesCounter.getObject().intValue();
		if(names.containsKey(name.getRawSignature())){
			Integer nameId = names.get(name.getRawSignature());
			actCount = nameId.intValue();
		} else {
			++actCount;
			namesCounter.setObject(Integer.valueOf(actCount));
			names.put(name.getRawSignature(), namesCounter.getObject());
		}

		if(actCount != trailName.getNameNumber()){
			return false;
		} 

		IBinding bind = name.resolveBinding();
		IASTName[] declNames = name.getTranslationUnit().getDeclarationsInAST(bind);
		if(declNames.length > 0){
			IASTNode tmpNode = ASTHelper.getDeclarationForNode(declNames[0]);

			if(tmpNode != null){
				if(trailName.isGloballyQualified()){
					//global Node
					if(tmpNode.equals(trailName.getDeclaration())){
						return true;
					}
				} else {
					//localNode
					IASTDeclSpecifier decl = ASTHelper.getDeclarationSpecifier(tmpNode);
					IASTDeclSpecifier trailDecl = trailName.getDeclSpecifier();
					
					IASTDeclarator declarator = ASTHelper.getDeclaratorForNode(declNames[0]);
					IASTDeclarator trailDeclarator = ASTHelper.getDeclaratorForNode(trailName.getDeclaration());

					IASTPointerOperator[] pointerOperators1 = declarator.getPointerOperators();
					IASTPointerOperator[] pointerOperators2 = trailDeclarator.getPointerOperators();
					
					if(trailDecl != null && decl != null 
							&& decl.getStorageClass() == trailDecl.getStorageClass()
							&& ASTHelper.samePointers(pointerOperators1, pointerOperators2, this)){
						if (decl instanceof IASTSimpleDeclSpecifier
								&& trailDecl instanceof IASTSimpleDeclSpecifier) {
							IASTSimpleDeclSpecifier simpleDecl = (IASTSimpleDeclSpecifier) decl;
							IASTSimpleDeclSpecifier simpleTrailDecl = (IASTSimpleDeclSpecifier) trailDecl;
							if(simpleDecl.getType() == simpleTrailDecl.getType()){
								return true;
							} 
						} else if (decl instanceof IASTNamedTypeSpecifier
								&& trailDecl instanceof IASTNamedTypeSpecifier) {

							IASTNamedTypeSpecifier namedDecl = (IASTNamedTypeSpecifier) decl;
							IASTNamedTypeSpecifier trailNamedDecl = (IASTNamedTypeSpecifier) trailDecl;
							if(namedDecl.getName().getRawSignature().equals(trailNamedDecl.getName().getRawSignature())){
								return true;
							}

						}
					}	
				}
			}
		}
		return false;
	}
}
