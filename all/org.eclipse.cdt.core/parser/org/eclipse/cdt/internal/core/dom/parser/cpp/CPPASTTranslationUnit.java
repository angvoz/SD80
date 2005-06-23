/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTPreprocessorSelectionResult;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IRequiresLocationInformation;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.InvalidPreprocessorNodeException;

/**
 * @author jcamelon
 */
public class CPPASTTranslationUnit extends CPPASTNode implements
        ICPPASTTranslationUnit, IRequiresLocationInformation, IASTAmbiguityParent {
    private IASTDeclaration[] decls = new IASTDeclaration[32];

    private ICPPNamespace binding = null;

    private ICPPScope scope = null;

    private ILocationResolver resolver;


    private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];

    private static final IASTNodeLocation[] EMPTY_PREPROCESSOR_LOCATION_ARRAY = new IASTNodeLocation[0];

    private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];

    private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];

    private static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];

    public void addDeclaration(IASTDeclaration d) {
        decls = (IASTDeclaration [])ArrayUtil.append( IASTDeclaration.class, decls, d );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
     */
    public IASTDeclaration[] getDeclarations() {
        if (decls == null)
            return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, decls );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
     */
    public IScope getScope() {
        if (scope == null) {
            scope = new CPPNamespaceScope(this);
			addBuiltinOperators(scope);
        }
		
        return scope;
    }
	
	private void addBuiltinOperators(IScope theScope) {
        // void
        IType cpp_void = new CPPBasicType(IBasicType.t_void, 0);
        // void *
        IType cpp_void_p = new GPPPointerType(new CPPQualifierType(new CPPBasicType(IBasicType.t_void, 0), false, false), new GPPASTPointer());
        // size_t // assumed: unsigned long int
        IType cpp_size_t = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_LONG & CPPBasicType.IS_UNSIGNED);

		// void * operator new (std::size_t);
        IBinding temp = null;
        IType[] newParms = new IType[1];
        newParms[0] = cpp_size_t;
        IFunctionType newFunctionType = new CPPFunctionType(cpp_void_p, newParms);
        IParameter[] newTheParms = new IParameter[1];
        newTheParms[0] = new CPPBuiltinParameter(newParms[0]);
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_NEW, theScope, newFunctionType, newTheParms, false);
        try {
			theScope.addBinding(temp);
        } catch (DOMException de) {}
		
		// void * operator new[] (std::size_t);
		temp = null;
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_NEW_ARRAY, theScope, newFunctionType, newTheParms, false);
        try {
			theScope.addBinding(temp);
        } catch (DOMException de) {}
		
		// void operator delete(void*);
        temp = null;
        IType[] deleteParms = new IType[1];
        deleteParms[0] = cpp_size_t;
        IFunctionType deleteFunctionType = new CPPFunctionType(cpp_void, deleteParms);
        IParameter[] deleteTheParms = new IParameter[1];
        deleteTheParms[0] = new CPPBuiltinParameter(deleteParms[0]);
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_DELETE, theScope, deleteFunctionType, deleteTheParms, false);
        try {
			theScope.addBinding(temp);
        } catch (DOMException de) {}
		
		// void operator delete[](void*);
		temp = null;
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_DELETE_ARRAY, theScope, deleteFunctionType, deleteTheParms, false);
        try {
			theScope.addBinding(temp);
        } catch (DOMException de) {}
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public IASTName[] getDeclarations(IBinding b) {
        if( b instanceof IMacroBinding )
        {
            if( resolver == null )
                return EMPTY_NAME_ARRAY;
            return resolver.getDeclarations( (IMacroBinding)b );
        }
        return CPPVisitor.getDeclarations( this, b );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDefinitions(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public IASTName[] getDefinitions(IBinding aBinding) {
        IASTName[] foundDefs = getDeclarations(aBinding);
        
        for(int i=0; i<foundDefs.length; i++) {
            if (!foundDefs[i].isDefinition())
                foundDefs[i] = null;
        }
        
        return (IASTName[])ArrayUtil.removeNulls(IASTName.class, foundDefs);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public IASTName[] getReferences(IBinding b) {
        if( b instanceof IMacroBinding )
        {
            if( resolver == null )
                return EMPTY_NAME_ARRAY;
            return resolver.getReferences( (IMacroBinding)b );
        }
    	return CPPVisitor.getReferences(this, b);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLocationInfo(int,
     *      int)
     */
    public IASTNodeLocation[] getLocationInfo(int offset, int length) {
        if (resolver == null)
            return EMPTY_PREPROCESSOR_LOCATION_ARRAY;
        return resolver.getLocations(offset, length);
    }

    private class CPPFindNodeForOffsetAction extends CPPASTVisitor {
    	{
    		shouldVisitNames          = true;
    		shouldVisitDeclarations   = true;
    		shouldVisitInitializers   = true;
    		shouldVisitParameterDeclarations = true;
    		shouldVisitDeclarators    = true;
    		shouldVisitDeclSpecifiers = true;
    		shouldVisitExpressions    = true;
    		shouldVisitStatements     = true;
    		shouldVisitTypeIds        = true;
    		shouldVisitEnumerators    = true;
    		shouldVisitBaseSpecifiers = true;
    		shouldVisitNamespaces     = true;
    	}
    	
    	IASTNode foundNode = null;
    	int offset = 0;
    	int length = 0;
    	
    	/**
		 * 
		 */
		public CPPFindNodeForOffsetAction(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
    	
    	public int processNode(IASTNode node) {
    		if (foundNode != null)
    			return PROCESS_ABORT;
    		
    		if (node instanceof ASTNode &&
    				((ASTNode)node).getOffset() == offset &&
    				((ASTNode)node).getLength() == length) {
    			foundNode = node;
    			return PROCESS_ABORT;
    		}
    		
    		// skip the rest of this node if the selection is outside of its bounds
			// TODO take out fix below for bug 86993 check for: !(node instanceof ICPPASTLinkageSpecification)
    		if (node instanceof ASTNode && !(node instanceof ICPPASTLinkageSpecification) &&
    				offset > ((ASTNode)node).getOffset() + ((ASTNode)node).getLength())
    			return PROCESS_SKIP;
    		
    		return PROCESS_CONTINUE;
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
    	 */
    	public int visit(IASTDeclaration declaration) {
    		// use declarations to determine if the search has gone past the offset (i.e. don't know the order the visitor visits the nodes)
			// TODO take out fix below for bug 86993 check for: !(declaration instanceof ICPPASTLinkageSpecification)
    		if (declaration instanceof ASTNode && !(declaration instanceof ICPPASTLinkageSpecification) && ((ASTNode)declaration).getOffset() > offset)
    			return PROCESS_ABORT;
    		
    		return processNode(declaration);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
    	 */
    	public int visit(IASTDeclarator declarator) {
    		int ret = processNode(declarator);
    		
    		IASTPointerOperator[] ops = declarator.getPointerOperators();
    		for(int i=0; i<ops.length; i++)
    			processNode(ops[i]);
    		
    		if (declarator instanceof IASTArrayDeclarator) {
    			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
    			for(int i=0; i<mods.length; i++)
    				processNode(mods[i]);
    		}
    		
    		if (declarator instanceof ICPPASTFunctionDeclarator) {
    			ICPPASTConstructorChainInitializer[] chainInit = ((ICPPASTFunctionDeclarator)declarator).getConstructorChain();
    			for(int i=0; i<chainInit.length; i++) {
    				processNode(chainInit[i]);
    			}
    			
    			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
    				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
    				for( int i = 0; i < catchHandlers.length; i++ ){
    					processNode(catchHandlers[i]);
    				}
    			}	
    		}
    		
    		return ret;
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDesignator(org.eclipse.cdt.core.dom.ast.c.ICASTDesignator)
    	 */
    	public int processDesignator(ICASTDesignator designator) {
    		return processNode(designator);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
    	 */
    	public int visit(IASTDeclSpecifier declSpec) {
    		return processNode(declSpec);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
    	 */
    	public int visit(IASTEnumerator enumerator) {
    		return processNode((IASTNode)enumerator);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
    	 */
    	public int visit(IASTExpression expression) {
    		return processNode(expression);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
    	 */
    	public int visit(IASTInitializer initializer) {
    		return processNode(initializer);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
    	 */
    	public int visit(IASTName name) {
    		if ( name.toString() != null )
    			return processNode(name);
    		return PROCESS_CONTINUE;
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
    	 */
    	public int visit(
    			IASTParameterDeclaration parameterDeclaration) {
    		return processNode(parameterDeclaration);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
    	 */
    	public int visit(IASTStatement statement) {
    		return processNode(statement);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
    	 */
    	public int visit(IASTTypeId typeId) {
    		return processNode(typeId);
    	}

    	public IASTNode getNode() {
    		return foundNode;
    	}
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
     */
    public IASTNode selectNodeForLocation(String path, int realOffset, int realLength) {
    	IASTNode node = null;
		ASTPreprocessorSelectionResult result = null;
		int globalOffset = 0;
		
		try {
			result = resolver.getPreprocessorNode(path, realOffset, realLength);
		} catch (InvalidPreprocessorNodeException ipne) {
			globalOffset = ipne.getGlobalOffset(); 
		}
    	
		if (result != null && result.getSelectedNode() != null) {
			node = result.getSelectedNode();
		} else {
			// use the globalOffset to get the node from the AST if it's valid
			globalOffset = result == null ? globalOffset : result.getGlobalOffset();
    		if (globalOffset >= 0) {
	    		CPPFindNodeForOffsetAction nodeFinder = new CPPFindNodeForOffsetAction(globalOffset, realLength);
	    		accept(nodeFinder);
	    		node = nodeFinder.getNode();
    		}
		}
    	
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroDefinitions()
     */
    public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
       IASTPreprocessorMacroDefinition [] result = resolver.getMacroDefinitions();
       return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getIncludeDirectives()
     */
    public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
       IASTPreprocessorIncludeStatement [] result = resolver.getIncludeDirectives();
       return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getAllPreprocessorStatements()
     */
    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
        if (resolver == null)
            return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
        IASTPreprocessorStatement [] result = resolver.getAllPreprocessorStatements();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
     */
    public void setLocationResolver(ILocationResolver resolver) {
        this.resolver = resolver;
        resolver.setRootNode( this );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit#resolveBinding()
     */
    public IBinding resolveBinding() {
        if (binding == null)
            binding = new CPPNamespace(this);
        return binding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getPreprocesorProblems()
     */
    public IASTProblem[] getPreprocessorProblems() {
        if (resolver == null)
            return EMPTY_PROBLEM_ARRAY;
        IASTProblem[] result = resolver.getScannerProblems();
        for (int i = 0; i < result.length; ++i) {
            IASTProblem p = result[i];
            p.setParent(this);
            p.setPropertyInParent(IASTTranslationUnit.SCANNER_PROBLEM);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getUnpreprocessedSignature(org.eclipse.cdt.core.dom.ast.IASTNodeLocation[])
     */
    public String getUnpreprocessedSignature(IASTNodeLocation[] locations) {
       if( resolver == null ) return EMPTY_STRING;
       return new String( resolver.getUnpreprocessedSignature(locations) );
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getFilePath()
	 */
	public String getFilePath() {
		if (resolver == null)
			return EMPTY_STRING;
		return new String(resolver.getTranslationUnitPath());
	}
	
    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitTranslationUnit){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTDeclaration [] ds = getDeclarations();
        for( int i = 0; i < ds.length; i++ ){
            if( !ds[i].accept( action ) ) return false;
        }
        return true;
    }
    
    public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations) {
        if( resolver == null )
            return null;
        return resolver.flattenLocations( nodeLocations );
    }

    public IASTName[] getMacroExpansions() {
        if( resolver == null )
            return EMPTY_NAME_ARRAY;
        return resolver.getMacroExpansions();
    }

    public IDependencyTree getDependencyTree() {
        if( resolver == null )
            return null;
        return resolver.getDependencyTree();
    }

	public String getContainingFilename(int offset) {
		if( resolver == null )
			return EMPTY_STRING;
		return resolver.getContainingFilename( offset );
	}
    
    public void replace(IASTNode child, IASTNode other) {
        if( decls == null ) return;
        for( int i = 0; i < decls.length; ++i )
        {
           if( decls[i] == null ) continue;
           if( decls[i] == child )
           {
               other.setParent( child.getParent() );
               other.setPropertyInParent( child.getPropertyInParent() );
               decls[i] = (IASTDeclaration) other;
           }
        }
    }

    public ParserLanguage getParserLanguage() {
        return ParserLanguage.CPP;
    }
}
