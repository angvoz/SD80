/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.core.search.matching;


import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FriendPattern extends ClassDeclarationPattern {

	/**
	 * @param name
	 * @param containers
	 * @param searchFor
	 * @param limit
	 * @param mode
	 * @param caseSensitive
	 */
	public FriendPattern(char[] name, char[][] containers, SearchFor searchFor, LimitTo limit, int mode, boolean caseSensitive) {
		super(name, containers, searchFor, limit, mode, caseSensitive);
	}
	
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestTypePrefix(
				searchFor,
				getLimitTo(),
				simpleName,
				qualifications,
				_matchMode,
				_caseSensitive
		);
	}
	
	protected boolean matchIndexEntry() {
	    if( decodedType != FRIEND_SUFFIX ){
			return false;
		}
	    
		return super.matchIndexEntry();
	}
	
	public int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit ){
		
		if (!( node instanceof IASTClassSpecifier )) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		IASTClassSpecifier tempNode = (IASTClassSpecifier) node;
		Iterator i = tempNode.getFriends();
		
		boolean matchFlag=false;
		String[] fullName=null;
		while (i.hasNext()){
			Object friend =  i.next();
			String[] baseFullyQualifiedName = null;
			if (friend instanceof IASTClassSpecifier)
			{
				IASTClassSpecifier classSpec = (IASTClassSpecifier) friend;
				baseFullyQualifiedName = classSpec.getFullyQualifiedName();
	
				//check name, if simpleName == null, its treated the same as "*"	
				if( simpleName != null && !matchesName( simpleName, classSpec.getName().toCharArray() ) ){
					continue;
				}
			}	
			else if (friend instanceof IASTElaboratedTypeSpecifier ){
			    IASTElaboratedTypeSpecifier elabType = (IASTElaboratedTypeSpecifier) friend;
			    baseFullyQualifiedName = elabType.getFullyQualifiedName();
			    
				//check name, if simpleName == null, its treated the same as "*"	
				if( simpleName != null && !matchesName( simpleName, elabType.getName().toCharArray() ) ){
					continue;
				}
			}
			
			if (baseFullyQualifiedName != null){
				char [][] qualName = new char [ baseFullyQualifiedName.length - 1 ][];
				for( int j = 0; j < baseFullyQualifiedName.length - 1; j++ ){
					qualName[j] = baseFullyQualifiedName[j].toCharArray();
				}
				//check containing scopes
				if( !matchQualifications( qualifications, qualName ) ){
					continue;
				}
				
				matchFlag = true;
				break;
			}
		}
		

		
		if (matchFlag)
			return ACCURATE_MATCH;
		
		return IMPOSSIBLE_MATCH;
	}
	
	
}
