/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The translation unit represents a compilable unit of source.
 * 
 * @author Doug Schaefer
 */
public interface IASTTranslationUnit extends IASTNode, IAdaptable {

	/**
	 * <code>OWNED_DECLARATION</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTDeclaration</code>'s.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"IASTTranslationUnit.OWNED_DECLARATION - IASTDeclaration for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>SCANNER_PROBLEM</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTProblem</code>.
	 */
	public static final ASTNodeProperty SCANNER_PROBLEM = new ASTNodeProperty(
			"IASTTranslationUnit.SCANNER_PROBLEM - IASTProblem (scanner caused) for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>PREPROCESSOR_STATEMENT</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTPreprocessorStatement</code>.
	 */
	public static final ASTNodeProperty PREPROCESSOR_STATEMENT = new ASTNodeProperty(
			"IASTTranslationUnit.PREPROCESSOR_STATEMENT - IASTPreprocessorStatement for IASTTranslationUnit"); //$NON-NLS-1$
    
	/**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Add declaration to translation unit. 
	 * 
	 * @param declaration <code>IASTDeclaration</code>
	 */
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * This returns the global scope for the translation unit.
	 * 
	 * @return the global scope
	 */
	public IScope getScope();

	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IName nodes that declare the binding.
	 * These may be part of the AST or are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IName nodes for the binding's declaration
	 */
	public IName[] getDeclarations(IBinding binding);
    
	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IASTName nodes that declare the binding.
	 * These are part of the AST no declarations are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDeclarationsInAST(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IName nodes that define the binding.
	 * These may be part of the AST or are pulled in from the index.
     *  
     * @param binding
     * @return the definition of the IBinding
     */
    public IName[] getDefinitions(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IASTName nodes that define the binding.
	 * These are part of the AST no definitions are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDefinitionsInAST(IBinding binding);

	/**
	 * Returns the list of references in this translation unit to the given
	 * binding. This list contains the IName nodes that represent a use of
	 * the binding. They may be part of the AST or pulled in from the index.
	 * 
	 * @param binding
	 * @return List of IASTName nodes representing uses of the binding
	 */
	public IASTName[] getReferences(IBinding binding);
	
	
	/**
	 * Returns an IASTNodeSelector object for finding nodes by file offsets.
	 * The object is suitable for working in one of the files that is part of
	 * the translation unit.
	 * @param filePath file of interest, as returned by {@link IASTFileLocation#getFileName()},
	 * or <code>null</code> to specify the root source of the translation-unit.
	 * @return an IASTNodeSelector.
	 * @since 5.0
	 */
	public IASTNodeSelector getNodeSelector(String filePath);
 
	/**
	 * @deprecated use {@link #getNodeSelector(String)}, instead.
	 */
	@Deprecated
	public IASTNode selectNodeForLocation(String path, int offset, int length);

	/**
	 * Get the macro definitions encountered in parsing this translation unit. The result will not contain
	 * definitions for built-in macros.
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * macro definitions contained therein are not returned.
	 */
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions();

	/**
	 * Get built-in macro definitions used when parsing this translation unit.
	 * This includes macros obtained from the index. 
	 */
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * Get the include directives encountered in parsing this translation unit. This will also contain directives
	 * used for handling the gcc-options -imacros and -include.
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * include directives contained therein are not returned.
	 */
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives();

	/**
	 * Get all preprocessor statements. 
	 * In case the information for a header-file is pulled in from the index,
	 * preprocessing statements contained therein are not returned.
	 */
	public IASTPreprocessorStatement[] getAllPreprocessorStatements();

	/**
	 * Returns an array with all macro expansions of this translation unit.
	 */
	public IASTPreprocessorMacroExpansion[] getMacroExpansions();
	
	/**
	 * Get all preprocessor and scanner problems.
	 * @return <code>IASTProblem[]</code>
	 */
	public IASTProblem[] getPreprocessorProblems();

	/**
	 * Fast access to the count of preprocessor problems to support statistics.
	 */
	public int getPreprocessorProblemsCount();

	/**
	 * Get the translation unit's full path.  
	 * @return String representation of path.
	 */
	public String getFilePath();
    
    /**
     * Flatten the node locations provided into a single file location.  
     * 
     * @param nodeLocations <code>IASTNodeLocation</code>s to flatten
     * @return null if not possible, otherwise, a file location representing where the macros are. 
     */
    public IASTFileLocation flattenLocationsToFile( IASTNodeLocation [] nodeLocations );
    
    /**
     * @deprecated names for macro expansions are nested inside of {@link IASTPreprocessorMacroExpansion}.
     */
    @Deprecated
	public static final ASTNodeProperty EXPANSION_NAME = new ASTNodeProperty(
    "IASTTranslationUnit.EXPANSION_NAME - IASTName generated for macro expansions."); //$NON-NLS-1$

    public static final ASTNodeProperty MACRO_EXPANSION = new ASTNodeProperty(
    "IASTTranslationUnit.MACRO_EXPANSION - IASTPreprocessorMacroExpansion node for macro expansions."); //$NON-NLS-1$

    
    public static interface IDependencyTree
    {
        public String getTranslationUnitPath();
        
        public static interface IASTInclusionNode
        {
            public IASTPreprocessorIncludeStatement getIncludeDirective();
            public IASTInclusionNode [] getNestedInclusions();
        }
        
        public IASTInclusionNode [] getInclusions();
    }
    
    /**
     * Return the dependency tree for the translation unit. 
	 * <p>
	 * In case the information for a header-file is pulled in from the index,
	 * dependencies contained therein are not part of the dependency tree.
     */
    public IDependencyTree getDependencyTree();

	/**
	 * @param offset
	 * @return
	 */
	public String getContainingFilename(int offset);
    
    
    /**
     * @return
     */
    public ParserLanguage getParserLanguage();
    
    /**
     * Return the Index associated with this translation unit.
     * 
     * @return the Index for this translation unit
     */
    public IIndex getIndex();
    
    /**
     * Set the Index to be used for this translation unit.
     * 
     * @param index
     */
    public void setIndex(IIndex index);
    
	/**
	 * In case the ast was created in a way that supports comment parsing,
	 * all comments of the translation unit are returned. Otherwise an
	 * empty array will be supplied.
	 * 
	 * @return <code>IASTComment[]</code>
	 * @since 4.0
	 */
	public IASTComment[] getComments();
	
	
	/**
	 * Returns the linkage this ast was parsed in
	 */
	public ILinkage getLinkage();
	
	/**
	 * Returns whether this ast represents a header file.
	 */
	public boolean isHeaderUnit();

	/**
	 * Sets whether this ast represents a header file.
	 */
	public void setIsHeaderUnit(boolean headerUnit);
}
