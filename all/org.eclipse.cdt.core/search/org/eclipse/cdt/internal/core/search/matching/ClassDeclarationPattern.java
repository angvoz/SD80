/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.LimitTo;
import org.eclipse.cdt.core.search.SearchFor;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 * @author aniefer
 */

public class ClassDeclarationPattern extends CSearchPattern {

//	public ClassDeclarationPattern( int matchMode, boolean caseSensitive ){
//		super( matchMode, caseSensitive, DECLARATIONS );
//	}
	
	public ClassDeclarationPattern( char[] name, char[][] containers, SearchFor searchFor, LimitTo limit, int mode, boolean caseSensitive ){
		super( mode, caseSensitive, limit );
		
		simpleName = caseSensitive ? name : CharOperation.toLowerCase( name );
		if( caseSensitive || containers == null ){
			qualifications = containers;
		} else {
			int len = containers.length;
			this.qualifications = new char[ len ][];
			for( int i = 0; i < len; i++ ){
				this.qualifications[i] = CharOperation.toLowerCase( containers[i] );
			}
		} 
		
		this.searchFor = searchFor;
		
		if( searchFor == CLASS ){
			classKind = ASTClassKind.CLASS;
		} else if( searchFor == STRUCT ) {
			classKind = ASTClassKind.STRUCT;
		} else if ( searchFor == ENUM ) {
			classKind = ASTClassKind.ENUM;
		} else if ( searchFor == UNION ) {
			classKind = ASTClassKind.UNION;
		} else {
			classKind = null;		
		}
		
	}
	
	public int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit ){
		if ( !( node instanceof IASTClassSpecifier )          &&
		     !( node instanceof IASTElaboratedTypeSpecifier ) &&
		     !( node instanceof IASTTypedefDeclaration )      &&
		     !( node instanceof IASTEnumerationSpecifier)     )
		{
			return IMPOSSIBLE_MATCH;
		} else if( searchFor != TYPE && ((searchFor == TYPEDEF) ^ (node instanceof IASTTypedefDeclaration)) ) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		char[] nodeName = null;
		if (node instanceof IASTElaboratedTypeSpecifier)
		{
			nodeName = ((IASTElaboratedTypeSpecifier)node).getNameCharArray();
		}
		else if( node instanceof IASTOffsetableNamedElement )
		{
			nodeName = ((IASTOffsetableNamedElement)node).getNameCharArray();
		} else {
			return IMPOSSIBLE_MATCH;
		}
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName ) ){
			return IMPOSSIBLE_MATCH;
		}

		if( node instanceof IASTQualifiedNameElement ){
			char [][] qualName = ((IASTQualifiedNameElement) node).getFullyQualifiedNameCharArrays();
			//check containing scopes
			if( !matchQualifications( qualifications, qualName, true ) ){
				return IMPOSSIBLE_MATCH;
			}
		}
		
		//check type
		if( classKind != null ){
			if( node instanceof IASTClassSpecifier ){
				IASTClassSpecifier clsSpec = (IASTClassSpecifier) node;
				return ( classKind == clsSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} else if (node instanceof IASTEnumerationSpecifier){
				return ( classKind == ASTClassKind.ENUM ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} else if (node instanceof IASTElaboratedTypeSpecifier ){
				IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) node;
				return ( classKind == elabTypeSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			}
		}
		
		return ACCURATE_MATCH;
	}
	
	public char [] getName() {
		return simpleName;
	}
	public char[] [] getContainingTypes () {
		return qualifications;
	}
	public ASTClassKind getKind(){
		return classKind;
	}

	protected char[] 	  simpleName;
	protected char[][]  qualifications;
	protected ASTClassKind classKind;
	protected SearchFor    searchFor;
	
	protected char[] decodedSimpleName;
	private char[][] decodedContainingTypes;
	protected int decodedType;

	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths,IndexInput input, ICSearchScope scope) throws IOException {
		boolean isClass = decodedType == IIndex.TYPE_CLASS;
		
		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				//For each file, create a new search match for each offset occurrence
				for (int j=0; j<offsets[i].length; j++){
					BasicSearchMatch match = new BasicSearchMatch();
					match.setName(new String(this.decodedSimpleName));
					//Get qualified names as strings
					if (decodedContainingTypes != null){
						String[] qualifiedName = new String[decodedContainingTypes.length];
						for (int k=0; k<this.decodedContainingTypes.length; k++){
							qualifiedName[k] =new String(this.decodedContainingTypes[k]);
						}
						match.setQualifiedName(qualifiedName);
					}
					//Decode the offsetse
					//Offsets can either be IIndex.LINE or IIndex.OFFSET 
					match.setLocatable(getMatchLocatable(offsets[i][j],offsetLengths[i][j]));
					
					match.setParentName(""); //$NON-NLS-1$
					
					switch (decodedType) {
					case IIndex.TYPE_CLASS: 
						match.setType(ICElement.C_CLASS);
						break;
					case IIndex.TYPE_STRUCT :
						match.setType(ICElement.C_STRUCT);
						break;
					case IIndex.TYPE_UNION :
						match.setType(ICElement.C_UNION);
						break;
					case IIndex.TYPE_ENUM :
						match.setType(ICElement.C_ENUMERATION);
						break;
					case IIndex.TYPE_TYPEDEF :
						match.setType(ICElement.C_TYPEDEF);
						break;
					}
					                                 
				    IFile tempFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
					if (tempFile != null && tempFile.exists())
						match.setResource(tempFile);
					else {
						IPath tempPath = PathUtil.getWorkspaceRelativePath(file.getPath());
						match.setPath(tempPath);
						match.setReferringElement(tempPath);
					}
					requestor.acceptSearchMatch(match);
				}
			}
		}
	}

	protected void resetIndexInfo(){
		decodedType = 0;
		decodedSimpleName = null;
		decodedContainingTypes = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {		
		this.decodedType = entryResult.getKind();
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
		String []missmatch = entryResult.getEnclosingNames();
		if(missmatch != null) {
			this.decodedContainingTypes = new char[missmatch.length][];
			for (int i = 0; i < missmatch.length; i++)
				this.decodedContainingTypes[i] = missmatch[i].toCharArray();
		}
	}

	public char[] indexEntryPrefix() {
		return Index.bestTypePrefix(
				searchFor,
				getLimitTo(),
				simpleName,
				qualifications,
				_matchMode,
				_caseSensitive
		);
	}

	protected boolean matchIndexEntry() {
		//check type matches
		if( classKind == null ){
			if( searchFor == TYPEDEF && decodedType != IIndex.TYPE_TYPEDEF ){
				return false;
			}
		} else if( classKind == ASTClassKind.CLASS ) {
			if( decodedType != IIndex.TYPE_CLASS ){
				return false;
			} 
		} else if ( classKind == ASTClassKind.STRUCT ) {
			if( decodedType != IIndex.TYPE_STRUCT){
				return false;
			}
		} else if ( classKind == ASTClassKind.UNION ) {
			if( decodedType != IIndex.TYPE_UNION){
				return false;
			}
		} else if ( classKind == ASTClassKind.ENUM ) {
			if( decodedType != IIndex.TYPE_ENUM ) {
				return false;
			}
		}
		
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		if( !matchQualifications( qualifications, decodedContainingTypes ) ){
			return false;
		}
		
		return true;
	}

}
