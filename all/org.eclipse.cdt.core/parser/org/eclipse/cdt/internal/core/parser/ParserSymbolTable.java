/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v05. html
 *
 * Contributors: 
 * Rational Software - Initial API and implementation
 *
***********************************************************************/


package org.eclipse.cdt.internal.core.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class ParserSymbolTable {

	/**
	 * Constructor for ParserSymbolTable.
	 */
	public ParserSymbolTable() {
		super();
		_compilationUnit = new Declaration();
		try{
			_compilationUnit.setType( TypeInfo.t_namespace );
		} catch ( ParserSymbolTableException e ){
			/*shouldn't happen*/
		}
		
		push( _compilationUnit );
	}

	public void push( Declaration obj ){
		if( _contextStack.empty() == false && obj.getContainingScope() == null ){
			obj.setContainingScope( (Declaration) _contextStack.peek() );
		}
		
		_contextStack.push( obj );
	}
	
	public Declaration pop(){
		return (Declaration) _contextStack.pop();
	}
	
	public Declaration peek(){
		return (Declaration) _contextStack.peek();
	}
	
	public Declaration getCompilationUnit(){
			return _compilationUnit;
	}
		
	public Declaration Lookup( String name ) throws ParserSymbolTableException {
		LookupData data = new LookupData( name, -1 );
		
		Lookup( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data ); 
	}
	
	public Declaration ElaboratedLookup( int type, String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, type );
		
		Lookup( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data ); 
	}

	/**
	 * Method LookupNestedNameSpecifier.
	 * @param name
	 * @return Declaration
	 * The name of a class or namespace member can be referred to after the ::
	 * scope resolution operator applied to a nested-name-specifier that
	 * nominates its class or namespace.  During the lookup for a name preceding
	 * the ::, object, function and enumerator names are ignored.  If the name
	 * is not a class-name or namespace-name, the program is ill-formed
	 */
	
	public Declaration LookupNestedNameSpecifier( String name ) throws ParserSymbolTableException {
		return LookupNestedNameSpecifier( name, (Declaration) _contextStack.peek() );
	}

	private Declaration LookupNestedNameSpecifier(String name, Declaration inDeclaration ) throws ParserSymbolTableException{		
		Declaration foundDeclaration = null;
		
		LookupData data = new LookupData( name, TypeInfo.t_namespace );
		data.upperType = TypeInfo.t_union;
		
		LookupInContained( data, inDeclaration );
		
		if( data.foundItems != null ){
			foundDeclaration = ResolveAmbiguities( data );//, data.foundItems );
		}
				
		if( foundDeclaration == null && inDeclaration._containingScope != null ){
			foundDeclaration = LookupNestedNameSpecifier( name, inDeclaration._containingScope );
		}
			
		return foundDeclaration;
	}
	
	/**
	 * LookupMemberForDefinition
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * In a definition for a namespace member in which the declarator-id is a
	 * qualified-id, given that the qualified-id for the namespace member has
	 * the form "nested-name-specifier unqualified-id", the unqualified-id shall
	 * name a member of the namespace designated by the nested-name-specifier.
	 * 
	 * ie:
	 * you have this:
	 * namespace A{    
	 *    namespace B{       
	 *       void  f1(int);    
	 *    }  
	 *    using  namespace B; 
	 * }
	 * 
	 * if you then do this 
	 * void A::f1(int) { ... } //ill-formed, f1 is not a member of A
	 * but, you can do this (Assuming f1 has been defined elsewhere)
	 * A::f1( 1 );  //ok, finds B::f1
	 * 
	 * ie, We need a seperate lookup function for looking up the member names
	 * for a definition.
	 */
	public Declaration LookupMemberForDefinition( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		data.qualified = true;
	
		LookupInContained( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data );
	}
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 */
	public Declaration QualifiedLookup( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		data.qualified = true;
		Lookup( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data ); 
	}
	
	/**
	 * 
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	public Declaration QualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_function );
		data.qualified = true;
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
		
		Lookup( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data ); 
	}
	
	/**
	 * MemberFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * Member lookup really proceeds as an unqualified lookup, but doesn't
	 * include argument dependant scopes
	 */
	public Declaration MemberFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_function );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
			
		Lookup( data, (Declaration) _contextStack.peek() );
		return ResolveAmbiguities( data ); 
	}
	
	/**
	 * UnqualifiedFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 3.4.2-1 When an unqualified name is used as the post-fix expression in a
	 * function call, other namespaces not consdiered during the usual
	 * unqualified lookup may be searched.
	 * 
	 * 3.4.2-2 For each argument type T in the function call, there is a set of
	 * zero or more associated namespaces and a set of zero or more associated
	 * classes to be considered.
	 * 
	 * If the ordinary unqualified lookup of the name find the declaration of a
	 * class member function, the associated namespaces and classes are not
	 * considered.  Otherwise, the set of declarations found by the lookup of
	 * the function name is the union of the set of declarations found using
	 * ordinary unqualified lookup and the set of declarations found in the
	 * namespaces and classes associated with the argument types.
	 */
	public Declaration UnqualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		//figure out the set of associated scopes first, so we can remove those that are searched
		//during the normal lookup to avoid doing them twice
		HashSet associated = new HashSet();
		
		//collect associated namespaces & classes.
		int size = ( parameters == null ) ? 0 : parameters.size();
		Iterator iter = ( parameters == null ) ? null : parameters.iterator();
		
		TypeInfo param = null;
		Declaration paramType = null;
		for( int i = size; i > 0; i-- ){
			param = (TypeInfo) iter.next();
			paramType = getFlatTypeInfo( param ).getTypeDeclaration();
			
			getAssociatedScopes( paramType, associated );
			
			//if T is a pointer to a data member of class X, its associated namespaces and classes
			//are those associated with the member type together with those associated with X
			if( param.getPtrOperator() != null && 
			   (param.getPtrOperator().equals("*") || param.getPtrOperator().equals("[]")) &&
			 	paramType._containingScope.isType( TypeInfo.t_class, TypeInfo.t_union ) )
			{
				getAssociatedScopes( paramType._containingScope, associated );
			}
		}
		
		LookupData data = new LookupData( name, TypeInfo.t_function );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
		data.associated = associated;
		
		Lookup( data, (Declaration) _contextStack.peek() );
		
		Declaration found = ResolveAmbiguities( data );
		
		//if we haven't found anything, or what we found is not a class member, consider the 
		//associated scopes
		if( found == null || found._containingScope.getType() != TypeInfo.t_class ){
			if( found != null ){
				data.foundItems.add( found );
			}
									
			Declaration decl;
			Declaration temp;

			//dump the hash to an array and iterate over the array because we
			//could be removing items from the collection as we go and we don't
			//want to get ConcurrentModificationExceptions			
			Object [] scopes = associated.toArray();
			
			size = associated.size();

			for( int i = 0; i < size; i++ ){
				decl  = (Declaration) scopes[ i ];
				if( associated.contains( decl ) ){
					data.qualified = true;
					data.ignoreUsingDirectives = true;
					Lookup( data, decl );
				}
			}
			
			found = ResolveAmbiguities( data );
		}
		
		return found;
	}

	/**
	 * LookupForFriendship
	 * @param name
	 * @return Declaration
	 * 7.3.1.2-3 When looking for a prior declaration of a class or a function
	 * declared as a friend, scopes outside the innermost enclosing namespace
	 * scope are not considered.
	 * 11.4-9 If a friend declaration appears in a local class and the name
	 * specified is an unqualified name, a prior declaration is looked up
	 * without considering scopes that are outside the innermost enclosing non-
	 * class scope.
	 */
	private Declaration LookupForFriendship( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		
		Declaration decl = (Declaration) _contextStack.peek();
		boolean inClass = (decl.getType() == TypeInfo.t_class);
		
		Declaration enclosing = decl._containingScope;
		while( enclosing != null && (inClass ? enclosing.getType() != TypeInfo.t_class
											  :	enclosing.getType() == TypeInfo.t_namespace) )
		{                                        		
			enclosing = enclosing._containingScope;
		}

		data.stopAt = enclosing;
		
		Lookup( data, (Declaration) _contextStack.peek() );
		
		return ResolveAmbiguities( data ); 
	}
		
	public void addUsingDirective( Declaration namespace ) throws ParserSymbolTableException{
		if( namespace.getType() != TypeInfo.t_namespace ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
			
		Declaration declaration = (Declaration) _contextStack.peek();
		
		if( declaration._usingDirectives == null ){
			declaration._usingDirectives = new LinkedList(); 
		}
		
		declaration._usingDirectives.add( namespace );
	}
	
	/**
	 * addUsingDeclaration
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.3-9  The entity declared by a using-declaration shall be known in the
	 * context using it according to its definition at the point of the using-
	 * declaration.  Definitions added to the namespace after the using-
	 * declaration are not considered when a use of the name is made.
	 * 
	 * 7.3.3-4 A using-declaration used as a member-declaration shall refer to a
	 * member of a base class of the class being defined, shall refer to a
	 * member of an anonymous union that is a member of a base class of the
	 * class being defined, or shall refer to an enumerator for an enumeration
	 * type that is a member of a base class of the class being defined.
	 */
	public Declaration addUsingDeclaration( String name ) throws ParserSymbolTableException {
		return addUsingDeclaration( name, null );
	}
	
	public Declaration addUsingDeclaration( String name, Declaration declContext ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		
		if( declContext != null ){				
			push( declContext );
			data.qualified = true;
			Lookup( data, (Declaration) _contextStack.peek() );
			pop();
		} else {
			Lookup( data, (Declaration) _contextStack.peek() );
		}
		
		//figure out which declaration we are talking about, if it is a set of functions,
		//then they will be in data.foundItems (since we provided no parameter info);
		Declaration obj = ResolveAmbiguities( data );
		
		if( data.foundItems == null ){
			throw new ParserSymbolTableException();				
		}

		Declaration clone = null;

		//if obj != null, then that is the only object to consider, so size is 1,
		//otherwise we consider the foundItems set				
		int size = ( obj == null ) ? data.foundItems.size() : 1;
		Iterator iter = data.foundItems.iterator();
		for( int i = size; i > 0; i-- ){
			obj = ( obj != null && size == 1 ) ? obj : (Declaration) iter.next();
			
			if( okToAddUsingDeclaration( obj, (Declaration) _contextStack.peek() ) ){
				clone = (Declaration) obj.clone(); //7.3.3-9
				addDeclaration( clone );
			} else {
				throw new ParserSymbolTableException();
			}
		}
		
		return ( size == 1 ) ? clone : null;
	}
	
	public void addDeclaration( Declaration obj ) throws ParserSymbolTableException{
		
		Declaration containing = (Declaration) _contextStack.peek();
			
		//handle enumerators
		if( obj.getType() == TypeInfo.t_enumerator ){
			//a using declaration of an enumerator will not be contained in a
			//enumeration.
			if( containing.getType() == TypeInfo.t_enumeration ){
				//Following the closing brace of an enum-specifier, each enumerator has the type of its 
				//enumeration
				obj.setTypeDeclaration( containing );
				//Each enumerator is declared in the scope that immediately contains the enum-specifier	
				containing = containing.getContainingScope();
			}
		}
		
		Map declarations = containing.getContainedDeclarations();
		
		boolean unnamed = obj.getName().equals( "" );
		
		Object origObj = null;
		
		obj.setContainingScope( containing );

		if( declarations == null ){
			declarations = containing.createContained();
		} else {
			//does this name exist already?
			origObj = declarations.get( obj.getName() );
		}
		
		if( origObj != null )
		{
			Declaration origDecl = null;
			LinkedList  origList = null;
		
			if( origObj.getClass() == Declaration.class ){
				origDecl = (Declaration)origObj;
			} else if( origObj.getClass() == LinkedList.class ){
				origList = (LinkedList)origObj;
			} else {
				throw new ParserSymbolTableException();
			}
			
			if( unnamed || (origList == null) ? isValidOverload( origDecl, obj ) : isValidOverload( origList, obj ) ){
				if( origList == null ){
					origList = new LinkedList();
					origList.add( origDecl );
					origList.add( obj );
				
					declarations.remove( obj );
					declarations.put( obj.getName(), origList );
				} else	{
					origList.add( obj );
					//origList is already in _containedDeclarations
				}
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		} else {
			declarations.put( obj.getName(), obj );
		}
		
		//take care of the this pointer
		TypeInfo type = obj._typeInfo;
		if( type.isType( TypeInfo.t_function ) && !type.checkBit( TypeInfo.isStatic ) ){
			addThis( obj );
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.1.2-3 If a friend declaration in a non-local class first declares a
	 * class or function, the friend class or function is a member of the
	 * innermost enclosing namespace.
	 * 
	 * TBD: if/when the parser symbol table starts caring about visibility
	 * (public/protected/private) we will need to do more to record friendship.
	 */
	public Declaration addFriend( String name ) throws ParserSymbolTableException{
		Declaration friend = LookupForFriendship( name  );
		
		if( friend == null ){
			friend = new Declaration( name );
			friend.setNeedsDefinition( true );
			
			Declaration decl = (Declaration) _contextStack.peek();
			Declaration containing = decl._containingScope;
			//find innermost enclosing namespace
			while( containing != null && containing.getType() != TypeInfo.t_namespace ){
				containing = containing._containingScope;
			}
			
			Declaration namespace = (containing == null ) ? _compilationUnit : containing; 
			push( namespace );
			addDeclaration( friend );
			pop(); 
		}
			
		return friend;
	}
	
	/**
	 * 
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 9.3.2-1 In the body of a nonstatic member function... the type of this of
	 * a class X is X*.  If the member function is declared const, the type of
	 * this is const X*, if the member function is declared volatile, the type
	 * of this is volatile X*....
	 */
	private void addThis( Declaration obj ) throws ParserSymbolTableException{
		TypeInfo type = obj._typeInfo;
		if( !type.isType( TypeInfo.t_function ) || type.checkBit( TypeInfo.isStatic ) ){
			return;
		}
		
		if( obj._containingScope.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
			//check to see if there is already a this object, since using declarations
			//of function will have them from the original declaration
			LookupData data = new LookupData( "this", -1 );
			LookupInContained( data, obj );
			//if we didn't find "this" then foundItems will still be null, no need to actually
			//check its contents 
			if( data.foundItems == null ){
				Declaration thisObj = new Declaration("this");
				thisObj.setType( TypeInfo.t_type );
				thisObj.setTypeDeclaration( obj._containingScope );
				thisObj.setCVQualifier( obj.getCVQualifier() );
				thisObj.setPtrOperator("*");
				
				push( obj );
				addDeclaration( thisObj );
				pop();
			}
		}		
	}
	
	/**
	 * Lookup the name from LookupData starting in the inDeclaration
	 * @param data
	 * @param inDeclaration
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	static private void Lookup( LookupData data, Declaration inDeclaration ) throws ParserSymbolTableException
	{
		if( data.type != -1 && data.type < TypeInfo.t_class && data.upperType > TypeInfo.t_union ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
		
		Declaration decl = null;					//the return value
		LinkedList transitives = new LinkedList();	//list of transitive using directives
		
		//if this name define in this scope?
		LookupInContained( data, inDeclaration );
		
		if( !data.ignoreUsingDirectives ){
			//check nominated namespaces
			//the transitives list is populated in LookupInNominated, and then 
			//processed in ProcessDirectives
			
			data.visited.clear(); //each namesapce is searched at most once, so keep track
			
			LookupInNominated( data, inDeclaration, transitives );

			//if we are doing a qualified lookup, only process using directives if
			//we haven't found the name yet (and if we aren't ignoring them). 
			if( !data.qualified || data.foundItems == null ){
				ProcessDirectives( inDeclaration, data, transitives );
				
				if( inDeclaration._usingDirectives != null ){
					ProcessDirectives( inDeclaration, data, inDeclaration._usingDirectives );
				}
							
				while( data.usingDirectives != null && data.usingDirectives.get( inDeclaration ) != null ){
					transitives.clear();
					
					LookupInNominated( data, inDeclaration, transitives );
	
					if( !data.qualified || data.foundItems == null ){
						ProcessDirectives( inDeclaration, data, transitives );
					}
				}
			}
		}
		
		if( data.foundItems != null || data.stopAt == inDeclaration ){
			return;
		}
			
		//if we still havn't found it, check any parents we have
		data.visited.clear();	//each virtual base class is searched at most once	
		decl = LookupInParents( data, inDeclaration );
		
		//there is a resolveAmbiguities inside LookupInParents, which means if we found
		//something the foundItems set will be non-null, but empty.  So, add the decl into
		//the foundItems set
		if( decl != null ){
			data.foundItems.add( decl );	
		}
					
		//if still not found, check our containing scope.			
		if( data.foundItems == null && inDeclaration._containingScope != null ){ 
			Lookup( data, inDeclaration._containingScope );
		}

		return;
	}
	
	/**
	 * function LookupInNominated
	 * @param data
	 * @param transitiveDirectives
	 * @return List
	 * 
	 * for qualified:
	 *  3.4.3.2-2 "let S be the set of all declarations of m in X
	 * and in the transitive closure of all namespaces nominated by using-
	 * directives in X and its used namespaces, except that using-directives are
	 * ignored in any namespace, including X, directly containing one or more
	 * declarations of m."
	 * 
	 * for unqualified:
	 * 7.3.4-2 The using-directive is transitive: if a scope contains a using
	 * directive that nominates a second namespace that itself contains using-
	 * directives, the effect is as if the using-directives from the second
	 * namespace also appeared in the first.
	 */
	static private void LookupInNominated( LookupData data, Declaration declaration, LinkedList transitiveDirectives ) throws ParserSymbolTableException{
		//if the data.usingDirectives is empty, there is nothing to do.
		if( data.usingDirectives == null ){
			return;
		}
			
		//local variables
		LinkedList  directives = null; //using directives association with declaration
		Iterator    iter = null;
		Declaration decl = null;
		
		boolean foundSomething = false;
		int size = 0;
		
		directives = (LinkedList) data.usingDirectives.remove( declaration );
		
		if( directives == null ){
			return;
		}
		
		iter = directives.iterator();
		size = directives.size();
		for( int i = size; i > 0; i-- ){
			decl = (Declaration) iter.next();

			//namespaces are searched at most once
			if( !data.visited.contains( decl ) ){
				data.visited.add( decl );
				
				foundSomething = LookupInContained( data, decl );
													
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( (!data.qualified || !foundSomething ) && decl._usingDirectives != null ){
					//name wasn't found, add transitive using directives for later consideration
					transitiveDirectives.addAll( decl._usingDirectives );
				}
			}
		}
		
		return;
	}
	
	/**
	 * function LookupInContained
	 * @param data
	 * @return List
	 * @throws ParserSymbolTableException
	 * 
	 * Look for data.name in our collection _containedDeclarations
	 */
	private static boolean LookupInContained( LookupData data, Declaration lookIn ) throws ParserSymbolTableException{
		boolean foundSomething = false;
		Declaration temp  = null;
		Object obj = null;
	
		if( data.associated != null ){
			//we are looking in lookIn, remove it from the associated scopes list
			data.associated.remove( lookIn );
		}
		
		Map declarations = lookIn.getContainedDeclarations();
		if( declarations == null )
			return foundSomething;
		
		obj = declarations.get( data.name );
	
		if( obj == null ){
			//not found
			return foundSomething;
		}
		
	 	//the contained declarations map either to a Declaration object, or to a list
	 	//of declaration objects.
		if( obj.getClass() == Declaration.class ){	
			if( ((Declaration)obj).isType( data.type, data.upperType ) ){
				if( data.foundItems == null ){
					data.foundItems = new HashSet();
				}
				data.foundItems.add( obj );
				foundSomething = true;
			}
		} else {
			//we have to filter on type so can't just add the list whole to the fount set
			LinkedList objList = (LinkedList)obj;
			Iterator iter  = objList.iterator();
			int size = objList.size();
					
			for( int i = 0; i < size; i++ ){
				temp = (Declaration) iter.next();
		
				if( temp.isType( data.type, data.upperType ) ){
					if( data.foundItems == null ){
						data.foundItems = new HashSet();
					}
					data.foundItems.add(temp);
					foundSomething = true;
				} 
			}
		}

		return foundSomething;
	}
	
	/**
	 * 
	 * @param data
	 * @param lookIn
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	private static Declaration LookupInParents( LookupData data, Declaration lookIn ) throws ParserSymbolTableException{
		LinkedList scopes = lookIn.getParentScopes();
		boolean foundSomething = false;
		Declaration temp = null;
		Declaration decl = null;
		
		Iterator iterator = null;
		Declaration.ParentWrapper wrapper = null;
		
		if( scopes == null )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new HashSet();
		
		data.inheritanceChain.add( lookIn );
		
		iterator = scopes.iterator();
			
		int size = scopes.size();
	
		for( int i = size; i > 0; i-- )
		{
			wrapper = (Declaration.ParentWrapper) iterator.next();
			if( !wrapper.isVirtual || !data.visited.contains( wrapper.parent ) ){
				if( wrapper.isVirtual ){
					data.visited.add( wrapper.parent );
				}
				
				//if the inheritanceChain already contains the parent, then that 
				//is circular inheritance
				if( ! data.inheritanceChain.contains( wrapper.parent ) ){
					//is this name define in this scope?
					LookupInContained( data, wrapper.parent );
					temp = ResolveAmbiguities( data );
					if( temp == null ){
						temp = LookupInParents( data, wrapper.parent );
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_CircularInheritance );
				}
				
			}	
			
			if( temp != null && temp.isType( data.type ) ){

				if( decl == null  ){
					decl = temp;
				} else if ( temp != null ) {
					//it is not ambiguous if temp & decl are the same thing and it is static
					//or an enumerator
					TypeInfo type = temp._typeInfo;
					
					if( decl == temp && ( type.checkBit( TypeInfo.isStatic ) || type.isType( TypeInfo.t_enumerator ) ) ){
						temp = null;
					} else {
						throw( new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ) );
					}
	
				}
			} else {
				temp = null;	//reset temp for next iteration
			}
		}
	
		data.inheritanceChain.remove( lookIn );

		return decl;	
	}
	
	/**
	 * function isValidOverload
	 * @param origDecl
	 * @param newDecl
	 * @return boolean
	 * 
	 * 3.3.7 "A class name or enumeration name can be hidden by the name of an
	 * object, function or enumerator declared in the same scope"
	 * 
	 * 3.4-1 "Name lookup may associate more than one declaration with a name if
	 * it finds the name to be a function name"
	 */
	private static boolean isValidOverload( Declaration origDecl, Declaration newDecl ){
		int origType = origDecl.getType();
		int newType  = newDecl.getType();
		
		if( (origType >= TypeInfo.t_class && origType <= TypeInfo.t_enumeration) && //class name or enumeration ...
			( newType == TypeInfo.t_type || (newType >= TypeInfo.t_function && newType <= TypeInfo.typeMask) ) ){
				
			return true;
		}
		//if the origtype is not a class-name or enumeration name, then the only other
		//allowable thing is if they are both functions.
		return isValidFunctionOverload( origDecl, newDecl );
	}
	
	private static boolean isValidOverload( LinkedList origList, Declaration newDecl ){
		if( origList.size() == 1 ){
			return isValidOverload( (Declaration)origList.getFirst(), newDecl );
		} else if ( origList.size() > 1 ){

			//the first thing can be a class-name or enumeration name, but the rest
			//must be functions.  So make sure the newDecl is a function before even
			//considering the list
			if( newDecl.getType() != TypeInfo.t_function ){
				return false;
			}
			
			Iterator iter = origList.iterator();
			Declaration decl = (Declaration) iter.next();
			boolean valid = (( decl.getType() >= TypeInfo.t_class && decl.getType() <= TypeInfo.t_enumeration ) ||
							  isValidFunctionOverload( decl, newDecl ));
			
			while( valid && iter.hasNext() ){
				decl = (Declaration) iter.next();
				valid = isValidFunctionOverload( decl, newDecl );
			}
			
			return valid;
		}
		
		//empty list, return true
		return true;
	}
	
	private static boolean isValidFunctionOverload( Declaration origDecl, Declaration newDecl ){
		if( origDecl.getType() != TypeInfo.t_function || newDecl.getType() != TypeInfo.t_function ){
			return false;
		}
		
		if( origDecl.hasSameParameters( newDecl ) ){
			//functions with the same name and same parameter types cannot be overloaded if any of them
			//is static
			if( origDecl._typeInfo.checkBit( TypeInfo.isStatic ) || newDecl._typeInfo.checkBit( TypeInfo.isStatic ) ){
				return false;
			}
			
			//if none of them are static, then the function can be overloaded if they differ in the type
			//of their implicit object parameter.
			if( origDecl.getCVQualifier() != newDecl.getCVQualifier() ){
				return true;
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param data
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * Resolve the foundItems set down to one declaration and return that
	 * declaration.  
	 * If we successfully resolve, then the data.foundItems list will be
	 * cleared.  If however, we were not able to completely resolve the set,
	 * then the data.foundItems set will be left with those items that
	 * survived the partial resolution and we will return null.  (currently,
	 * this case applies to when we have overloaded functions and no parameter
	 * information)
	 * 
	 * NOTE: data.parameters == null means there is no parameter information at
	 * all, when looking for functions with no parameters, an empty list must be
	 * provided in data.parameters.
	 */
	static private Declaration ResolveAmbiguities( LookupData data ) throws ParserSymbolTableException{
		Declaration decl = null;
		Declaration obj	= null;
		Declaration cls = null;
		
		if( data.foundItems == null ){
			return null;
		}
		
		int size = data.foundItems.size(); 
		Iterator iter = data.foundItems.iterator();
		
		boolean needDecl = true;
		
		if( size == 0){
			return null;
		} else if (size == 1) {
			decl = (Declaration) iter.next();
			//if it is a function we need to check its parameters
			if( !decl.isType( TypeInfo.t_function ) ){
				data.foundItems.clear();
				return decl;
			}
			needDecl = false;
		} 
		
		LinkedList functionList = null;	

		for( int i = size; i > 0; i-- ){
			//if we
			if( needDecl ){
				decl = (Declaration) iter.next();
			} else {
				needDecl = true;
			}
			
			if( decl.isType( TypeInfo.t_function ) ){
				if( functionList == null){
					functionList = new LinkedList();
				}
				functionList.add( decl );
			} else {
				//if this is a class-name, other stuff hides it
				if( decl.isType( TypeInfo.t_class, TypeInfo.t_enumeration ) ){
					if( cls == null ) {
						cls = decl;
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ); 
					}
				} else {
					//an object, can only have one of these
					if( obj == null ){
						obj = decl;	
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ); 
					}
				}
			}
			
			decl = null;
		}
	
		data.foundItems.clear();
		
		int numFunctions = ( functionList == null ) ? 0 : functionList.size();
		
		boolean ambiguous = false;
		
		if( cls != null ){
			//the class is only hidden by other stuff if they are from the same scope
			if( obj != null && cls._containingScope != obj._containingScope ){
				ambiguous = true;	
			}
			if( functionList != null ){
				Iterator fnIter = functionList.iterator();
				Declaration fn = null;
				for( int i = numFunctions; i > 0; i-- ){
					fn = (Declaration) fnIter.next();
					if( cls._containingScope != fn._containingScope ){
						ambiguous = true;
						break;
					}
				}
			}
		}
		
		if( obj != null && !ambiguous ){
			if( numFunctions > 0 ){
				ambiguous = true;
			} else {
				return obj;
			}
		} else if( numFunctions > 0 ) {
			if( data.parameters == null ){
				//we have no parameter information, if we only have one function, return
				//that, otherwise we can't decide between them
				if( numFunctions == 1){
					return (Declaration) functionList.getFirst();
				} else {
					data.foundItems.addAll( functionList );
					return null;
				}
			} else {
				return ResolveFunction( data, functionList );
			}
		}
		
		if( ambiguous ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
		} else {
			return cls;
		}
	}

	static private Declaration ResolveFunction( LookupData data, LinkedList functions ) throws ParserSymbolTableException{
		 
		ReduceToViable( data, functions );
		
		int numSourceParams = ( data.parameters == null ) ? 0 : data.parameters.size();
		int numFns = functions.size();
		
		if( numSourceParams == 0 ){
			//no parameters
			//if there is only 1 viable function, return it, if more than one, its ambiguous
			if( numFns == 0 ){
				return null;
			} else if ( numFns == 1 ){
				return (Declaration)functions.getFirst();
			} else{
				throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
			}
		}
		
		Declaration bestFn = null;				//the best function
		Declaration currFn = null;				//the function currently under consideration
		Cost [] bestFnCost = null;				//the cost of the best function
		Cost [] currFnCost = null;				//the cost for the current function
				
		Iterator iterFns = functions.iterator();
		Iterator sourceParams = null;
		Iterator targetParams = null;
		
		int numTargetParams = 0;
		int numParams = 0;
		int comparison;
		Cost cost = null;
		Cost temp = null;
		
		TypeInfo source = null;
		TypeInfo target = null;
		 
		boolean hasWorse = false;
		boolean hasBetter = false;
		boolean ambiguous = false;
		boolean currHasAmbiguousParam = false;
		boolean bestHasAmbiguousParam = false;

		for( int i = numFns; i > 0; i-- ){
			currFn = (Declaration) iterFns.next();
			
			sourceParams = data.parameters.iterator();
			targetParams = currFn._parameters.iterator();
			
			//number of parameters in the current function
			numTargetParams = currFn._parameters.size();
			
			//we only need to look at the smaller number of parameters
			//(a larger number in the Target means default parameters, a larger
			//number in the source means ellipses.)
			numParams = ( numTargetParams < numSourceParams ) ? numTargetParams : numSourceParams;
			
			if( currFnCost == null ){
				currFnCost = new Cost [ numParams ];	
			}
			
			comparison = 0;
			
			for( int j = 0; j < numParams; j++ ){
				source = ( TypeInfo )sourceParams.next();
				target = ( TypeInfo )targetParams.next();
				if( source.equals( target ) ){
					cost = new Cost( source, target );
					cost.rank = 0;	//exact match, no cost
				} else {
					cost = checkStandardConversionSequence( source, target );
					
					if( cost.rank == -1){
						temp = checkUserDefinedConversionSequence( source, target );
						if( temp != null ){
							cost = temp;
						}
					}
				}
				
				currFnCost[ j ] = cost;
			}
			
			
			hasWorse = false;
			hasBetter = false;
			
			for( int j = 0; j < numParams; j++ ){ 
				if( currFnCost[ j ].rank < 0 ){
					hasWorse = true;
					hasBetter = false;
					break;
				}
				
				currHasAmbiguousParam = ( currFnCost[ j ].userDefined == 1 );
				
				if( bestFnCost != null ){
					comparison = currFnCost[ j ].compare( bestFnCost[ j ] );
					hasWorse |= ( comparison < 0 );
					hasBetter |= ( comparison > 0 );
				} else {
					hasBetter = true;
				}
			}
				
			ambiguous |= ( hasWorse && hasBetter ) || ( !hasWorse && !hasBetter );
			
			if( !hasWorse ){
				if( hasBetter ){
					ambiguous = false;
					bestFnCost = currFnCost;
					bestHasAmbiguousParam = currHasAmbiguousParam;
					currFnCost = null;
					bestFn = currFn;
				}				
			}
		}

		if( ambiguous || bestHasAmbiguousParam ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
		}
						
		return bestFn;
	}
	
	static private void ReduceToViable( LookupData data, LinkedList functions ){
		int numParameters = ( data.parameters == null ) ? 0 : data.parameters.size();
		int num;	
			
		//Trim the list down to the set of viable functions
		Declaration function;
		Iterator iter = functions.iterator();
		while( iter.hasNext() ){
			function = (Declaration) iter.next();
			num = ( function._parameters == null ) ? 0 : function._parameters.size();
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
				continue;
			} 
			//A candidate function having fewer than m parameters is viable only if it has an 
			//ellipsis in its parameter list.
			else if( num < numParameters ) {
				//TBD ellipsis
				//not enough parameters, remove it
				iter.remove();		
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
				ListIterator listIter = function._parameters.listIterator( num );
				TypeInfo param;
				for( int i = num; i > ( numParameters - num + 1); i-- ){
					param = (TypeInfo)listIter.previous();
					if( !param.getHasDefault() ){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	/**
	 * function ProcessDirectives
	 * @param Declaration decl
	 * @param LookupData  data
	 * @param LinkedList  directives
	 * 
	 * Go through the directives and for each nominated namespace find the
	 * closest enclosing declaration for that namespace and decl, then add the
	 * nominated namespace to the lookup data for consideration when we reach
	 * the enclosing declaration.
	 */
	static private void ProcessDirectives( Declaration decl, LookupData data, LinkedList directives ){
		Declaration enclosing = null;
		Declaration temp = null;
		
		int size = directives.size();
		Iterator iter = directives.iterator();
	
		for( int i = size; i > 0; i-- ){
			temp = (Declaration) iter.next();
		
			//namespaces are searched at most once
			if( !data.visited.contains( temp ) ){
				enclosing = getClosestEnclosingDeclaration( decl, temp );
						
				//the data.usingDirectives is a map from enclosing declaration to 
				//a list of namespaces to consider when we reach that enclosing
				//declaration
				LinkedList list = (data.usingDirectives == null ) 
								? null
								: (LinkedList) data.usingDirectives.get( enclosing );
				if ( list == null ){
					list = new LinkedList();
					list.add( temp );
					if( data.usingDirectives == null ){
						data.usingDirectives = new HashMap();
					}
					data.usingDirectives.put( enclosing, list );
				} else {
					list.add( temp );
				}
			}
		}
	}
	
	/**
	 * function getClosestEnclosingDeclaration
	 * @param decl1
	 * @param decl2
	 * @return Declaration
	 * 
	 * 7.3.4-1 "During unqualified lookup, the names appear as if they were
	 * declared in the nearest enclosing namespace which contains both the
	 * using-directive and the nominated namespace"
	 * 
	 * TBD: Consider rewriting this iteratively instead of recursively, for
	 * performance
	 */
	static private Declaration getClosestEnclosingDeclaration( Declaration decl1, Declaration decl2 ){
		if( decl1 == decl2 ){ 
			return decl1;
		}
				
		if( decl1._depth == decl2._depth ){
			return getClosestEnclosingDeclaration( decl1._containingScope, decl2._containingScope );
		} else if( decl1._depth > decl2._depth ) {
			return getClosestEnclosingDeclaration( decl1._containingScope, decl2 );
		} else {
			return getClosestEnclosingDeclaration( decl1, decl2._containingScope );
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @param base
	 * @return int
	 * figure out if base is a base class of obj, and return the "distance" to
	 * the base class.
	 * ie:
	 *     A -> B -> C
	 * the distance from A to B is 1 and from A to C is 2. This distance is used
	 * to rank standard pointer conversions.
	 * 
	 * TBD: Consider rewriting iteratively for performance.
	 */
	static private int hasBaseClass( Declaration obj, Declaration base ){
		if( obj == base ){
			return 0;
		}
		
		if( obj._parentScopes != null ){	
			Declaration decl;
			Declaration.ParentWrapper wrapper;
			
			Iterator iter = obj._parentScopes.iterator();
			int size = obj._parentScopes.size();
			
			for( int i = size; i > 0; i-- ){
				wrapper = (Declaration.ParentWrapper) iter.next();	
				decl = wrapper.parent;
				
				if( decl == base ){
					return 1;
				} else {
					int n = hasBaseClass( decl, base );
					if( n > 0 ){
						return n + 1;
					}
				}
				
			}
		}
		
		return -1;
	}

	static private void getAssociatedScopes( Declaration decl, HashSet associated ){
		if( decl == null ){
			return;
		}
		//if T is a class type, its associated classes are the class itself,
		//and its direct and indirect base classes. its associated Namespaces are the 
		//namespaces in which its associated classes are defined	
		if( decl.getType() == TypeInfo.t_class ){
			associated.add( decl );
			getBaseClassesAndContainingNamespaces( decl, associated );
		} 
		//if T is a union or enumeration type, its associated namespace is the namespace in 
		//which it is defined. if it is a class member, its associated class is the member's
		//class
		else if( decl.getType() == TypeInfo.t_union || decl.getType() == TypeInfo.t_enumeration ){
			associated.add( decl._containingScope );
		}
	}
	
	static private void getBaseClassesAndContainingNamespaces( Declaration obj, HashSet classes ){
		if( obj._parentScopes != null ){
			if( classes == null ){
				return;
			}
			
			Iterator iter = obj._parentScopes.iterator();
			int size = obj._parentScopes.size();
			Declaration.ParentWrapper wrapper;
			Declaration base;
			
			for( int i = size; i > 0; i-- ){
				wrapper = (Declaration.ParentWrapper) iter.next();	
				base = (Declaration) wrapper.parent;	
				classes.add( base );
				if( base._containingScope.getType() == TypeInfo.t_namespace ){
					classes.add( base._containingScope );
				}
				
				getBaseClassesAndContainingNamespaces( base, classes );
			}
		}
	}

	static private boolean okToAddUsingDeclaration( Declaration obj, Declaration context ){
		boolean okToAdd = false;
			
		//7.3.3-4
		if( context.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
			//a member of a base class
			if( obj.getContainingScope().getType() == context.getType() ){
				okToAdd = ( hasBaseClass( context, obj.getContainingScope() ) > 0 );		
			} 
			//TBD : a member of an _anonymous_ union
			else if ( obj.getContainingScope().getType() == TypeInfo.t_union ) {
				Declaration union = obj.getContainingScope();
				okToAdd = ( hasBaseClass( context, union.getContainingScope() ) > 0 ); 
			}
			//an enumerator for an enumeration
			else if ( obj.getType() == TypeInfo.t_enumerator ){
				Declaration enumeration = obj.getContainingScope();
				okToAdd = ( hasBaseClass( context, enumeration.getContainingScope() ) > 0 );
			}
		} else {
			okToAdd = true;
		}	
		
		return okToAdd;
	}

	static private Cost lvalue_to_rvalue( TypeInfo source, TypeInfo target ) throws ParserSymbolTableException{
		//lvalues will have type t_type
		if( source.isType( TypeInfo.t_type ) ){
			source = getFlatTypeInfo( source );
		}
	
		String sourcePtr = source.getPtrOperator();
		String targetPtr = target.getPtrOperator();
		
		if( sourcePtr != null && sourcePtr.length() > 0 ){
			char sourcePtrArray [] = sourcePtr.toCharArray();
			if( sourcePtrArray[ 0 ] == '&' ){
				source.setPtrOperator( new String(sourcePtrArray, 1, sourcePtr.length() - 1 ) );
			}
		}
		
		if( targetPtr != null && targetPtr.length() > 0 ){
			char targetPtrArray [] = targetPtr.toCharArray();
			if( targetPtrArray[ 0 ] == '&' ){
				target.setPtrOperator ( new String( targetPtrArray, 1, targetPtr.length() - 1 ) );
			}
		}
		
		Cost cost = new Cost( source, target );
	
		return cost;
	}
	
	static private void qualificationConversion( Cost cost ){
		if(  cost.source.getCVQualifier() == cost.target.getCVQualifier() || 
			( cost.target.getCVQualifier() - cost.source.getCVQualifier()) > 1 )
		{
			cost.qualification = cost.target.getCVQualifier() + 1;
			cost.rank = 0;
		} else {
			cost.qualification = 0;
		}
	}
		
	/**
	 * 
	 * @param source
	 * @param target
	 * @return int
	 * 
	 * 4.5-1 char, signed char, unsigned char, short int or unsigned short int
	 * can be converted to int if int can represent all the values of the source
	 * type, otherwise they can be converted to unsigned int.
	 * 4.5-2 wchar_t or an enumeration can be converted to the first of the
	 * following that can hold it: int, unsigned int, long unsigned long.
	 * 4.5-4 bool can be promoted to int 
	 * 4.6 float can be promoted to double
	 */
	static private void promotion( Cost cost ){
		TypeInfo src = cost.source;
		TypeInfo trg = cost.target;
		 
		int mask = TypeInfo.isShort | TypeInfo.isLong | TypeInfo.isUnsigned;
		
		if( (src.isType( TypeInfo.t_bool, TypeInfo.t_float ) || src.isType( TypeInfo.t_enumeration )) &&
			(trg.isType( TypeInfo.t_int ) || trg.isType( TypeInfo.t_double )) )
		{
			if( src.getType() == trg.getType() && (( src.getTypeInfo() & mask) == (trg.getTypeInfo() & mask)) ){
				//same, no promotion needed
				return;	
			}
			
			if( src.isType( TypeInfo.t_float ) ){ 
				cost.promotion = trg.isType( TypeInfo.t_double ) ? 1 : 0;
			} else {
				cost.promotion = ( trg.isType( TypeInfo.t_int ) && trg.canHold( src ) ) ? 1 : 0;
			}
			
		} else {
			cost.promotion = 0;
		}
		
		cost.rank = (cost.promotion > 0 ) ? 1 : -1;
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @return int
	 * 
	 */
	static private void conversion( Cost cost ){
		TypeInfo src = cost.source;
		TypeInfo trg = cost.target;
		
		int temp;
		
		String tempStr = src.getPtrOperator();
		String srcPtr = ( tempStr == null ) ? new String("") : tempStr;
		
		tempStr = trg.getPtrOperator();
		String trgPtr = ( tempStr == null ) ? new String("") : tempStr;
		
		cost.conversion = 0;
		cost.detail = 0;
		
		if( !srcPtr.equals( trgPtr ) ){
			return;
		} 
		if( srcPtr.equals("*") ){
			Declaration srcDecl = src.isType( TypeInfo.t_type ) ? src.getTypeDeclaration() : null;
			Declaration trgDecl = trg.isType( TypeInfo.t_type ) ? trg.getTypeDeclaration() : null;

			if( srcDecl == null || (trgDecl == null && !trg.isType( TypeInfo.t_void )) ){
				return;	
			}
			
			//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
			//converted to an rvalue of type "pointer to cv void"
			if( trg.isType( TypeInfo.t_void ) ){
				cost.rank = 2;
				cost.conversion = 1;
				cost.detail = 2;
				return;	
			}
			
			cost.detail = 1;
			
			//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
			// to an rvalue of type "pointer to cv B", where B is a base class of D.
			if( srcDecl.isType( TypeInfo.t_class ) && trgDecl.isType( TypeInfo.t_class ) ){
				temp = hasBaseClass( srcDecl, trgDecl );
				cost.rank = 2;
				cost.conversion = ( temp > -1 ) ? temp : 0;
				cost.detail = 1;
				return;
			}
			
			//4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
			//derived class of B
			if( srcDecl._containingScope.isType( TypeInfo.t_class ) && trgDecl._containingScope.isType( TypeInfo.t_class ) ){
				temp = hasBaseClass( trgDecl._containingScope, srcDecl._containingScope );
				cost.rank = 2;
				cost.conversion = ( temp > -1 ) ? temp : 0;
				return;
			}
		} else {
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			if( src.isType( TypeInfo.t_bool, TypeInfo.t_int ) ||
				src.isType( TypeInfo.t_float, TypeInfo.t_double ) ||
				src.isType( TypeInfo.t_enumeration ) )
			{
				if( trg.isType( TypeInfo.t_bool, TypeInfo.t_int ) ||
					trg.isType( TypeInfo.t_float, TypeInfo.t_double ) )
				{
					cost.rank = 2;
					cost.conversion = 1;	
				}
			}
		}
	}
	
	static private Cost checkStandardConversionSequence( TypeInfo source, TypeInfo target ) throws ParserSymbolTableException {
		Cost cost = lvalue_to_rvalue( source, target );
		
		if( cost.source.equals( cost.target ) ){
			cost.rank = 0;
			return cost;
		}
	
		qualificationConversion( cost );
		
		//if we can't convert the qualifications, then we can't do anything
		if( cost.qualification == 0 ){
			return cost;
		}
		
		promotion( cost );
		if( cost.promotion > 0 || cost.rank > -1 ){
			return cost;
		}
		
		conversion( cost );
		
		return cost;	
	}
	
	static private Cost checkUserDefinedConversionSequence( TypeInfo source, TypeInfo target ) throws ParserSymbolTableException {
		Cost cost = null;
		Cost constructorCost = null;
		Cost conversionCost = null;

		Declaration targetDecl = null;
		Declaration sourceDecl = null;
		Declaration constructor = null;
		Declaration conversion = null;
		
		//constructors
		if( target.getType() == TypeInfo.t_type ){
			targetDecl = target.getTypeDeclaration();
			if( targetDecl.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
				LookupData data = new LookupData( "", TypeInfo.t_function );
				LinkedList params = new LinkedList();
				params.add( source );
				data.parameters = params;
				LookupInContained( data, targetDecl );
				constructor = ResolveAmbiguities( data );
			}
		}
		
		//conversion operators
		if( source.getType() == TypeInfo.t_type ){
			source = getFlatTypeInfo( source );
			sourceDecl = source.getTypeDeclaration();
			
			if( sourceDecl != null ){
				String name = target.toString();
				
				if( !name.equals("") ){
					LookupData data = new LookupData( "operator " + name, TypeInfo.t_function );
					LinkedList params = new LinkedList();
					data.parameters = params;
					
					LookupInContained( data, sourceDecl );
					conversion = ResolveAmbiguities( data );	
				}
			}
		}
		
		if( constructor != null ){
			constructorCost = checkStandardConversionSequence( new TypeInfo( TypeInfo.t_type, constructor._containingScope ), target );
		}
		if( conversion != null ){
			conversionCost = checkStandardConversionSequence( new TypeInfo( target.getType(), target.getTypeDeclaration() ), target );
		}
		
		//if both are valid, then the conversion is ambiguous
		if( constructorCost != null && constructorCost.rank != -1 && 
			conversionCost != null && conversionCost.rank != -1 )
		{
			cost = constructorCost;
			cost.userDefined = 1;
			cost.rank = 3;
		} else {
			if( constructorCost != null && constructorCost.rank != -1 ){
				cost = constructorCost;
				cost.userDefined = constructor.hashCode();
				cost.rank = 3;
			} else if( conversionCost != null && conversionCost.rank != -1 ){
				cost = conversionCost;
				cost.userDefined = conversion.hashCode();
				cost.rank = 3;
			} 			
		}
		
		return cost;
	}

	/**
	 * 
	 * @param decl
	 * @return TypeInfo
	 * @throws ParserSymbolTableException
	 * The top level TypeInfo represents modifications to the object and the
	 * remaining TypeInfo's represent the object.
	 */
	static private TypeInfo getFlatTypeInfo( TypeInfo topInfo ) throws ParserSymbolTableException {
		TypeInfo returnInfo = topInfo;
		TypeInfo info = null;
		
		if( topInfo.getType() == TypeInfo.t_type ){
			returnInfo = new TypeInfo();
			
			Declaration typeDecl = topInfo.getTypeDeclaration();
			
			info = topInfo.getTypeDeclaration().getTypeInfo();
			
			while( info.getType() == TypeInfo.t_type ){
				typeDecl = info.getTypeDeclaration();
				
				returnInfo.addCVQualifier( info.getCVQualifier() );
				returnInfo.addPtrOperator( info.getPtrOperator() );	
				
				info = info.getTypeDeclaration().getTypeInfo();
			}
			
			if( info.isType( TypeInfo.t_class, TypeInfo.t_enumeration ) ){
				returnInfo.setType( TypeInfo.t_type );
				returnInfo.setTypeDeclaration( typeDecl );
			} else {
				returnInfo.setTypeInfo( info.getTypeInfo() );
				returnInfo.setTypeDeclaration( null );
			}
			
			String ptrOp = returnInfo.getPtrOperator();
			returnInfo.setPtrOperator( topInfo.getInvertedPtrOperator() );
			
			if( ptrOp != null ){
				returnInfo.addPtrOperator( ptrOp );
			}
			
			returnInfo.setCVQualifier( info.getCVQualifier() );
			returnInfo.addCVQualifier( topInfo.getCVQualifier() );
		}
		
		return returnInfo;	
	}

	
		
	private Stack _contextStack = new Stack();
	private Declaration _compilationUnit;
	
	static private class LookupData
	{
		
		public String name;
		public Map usingDirectives; 
		public Set visited = new HashSet();	//used to ensure we don't visit things more than once
		
		public HashSet inheritanceChain;		//used to detect circular inheritance
		
		public LinkedList parameters;			//parameter info for resolving functions
		public HashSet associated;				//associated namespaces for argument dependant lookup
		public Declaration stopAt;				//stop looking along the stack once we hit this declaration
				 
		public int type = -1;
		public int upperType = 0;
		public boolean qualified = false;
		public boolean ignoreUsingDirectives = false;

		public HashSet foundItems = null;
		
		public LookupData( String n, int t ){
			name = n;
			type = t;
		}
	}
	
	static private class Cost
	{
		public Cost( TypeInfo s, TypeInfo t ){
			source = s;
			target = t;
		}
		
		public TypeInfo source;
		public TypeInfo target;
		
		public int lvalue;
		public int promotion;
		public int conversion;
		public int qualification;
		public int userDefined;
		public int rank = -1;
		public int detail;
		
		public int compare( Cost cost ){
			int result = 0;
			
			if( rank != cost.rank ){
				return cost.rank - rank;
			}
			
			if( userDefined != 0 || cost.userDefined != 0 ){
				if( userDefined == 0 || cost.userDefined == 0 ){
					return cost.userDefined - userDefined;
				} else {
					if( (userDefined == 1 || cost.userDefined == 1) ||
						(userDefined != cost.userDefined ) )
					{
						return 0;
					} 
					// else they are the same constructor/conversion operator and are ranked
					//on the standard conversion sequence
				}
			}
			
			if( promotion > 0 || cost.promotion > 0 ){
				result = cost.promotion - promotion;
			}
			if( conversion > 0 || cost.conversion > 0 ){
				if( detail == cost.detail ){
					result = cost.conversion - conversion;
				} else {
					result = cost.detail - detail;
				}
			}
			
			if( result == 0 ){
				result = cost.qualification - qualification;
			}
			 
			return result;
		}
	}

}
