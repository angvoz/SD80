/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Nov 6, 2003
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ParameterizedSymbol extends ContainerSymbol implements IParameterizedSymbol {

	protected ParameterizedSymbol( ParserSymbolTable table, String name ){
		super( table, name );
	}
	
	protected ParameterizedSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	protected ParameterizedSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	public Object clone(){
		ParameterizedSymbol copy = (ParameterizedSymbol)super.clone();
			
		copy._parameterList = ( _parameterList != Collections.EMPTY_LIST ) ? (List) ((ArrayList)_parameterList).clone() : _parameterList;
		
		if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE )
			copy._parameterMap	= ( _parameterMap != Collections.EMPTY_MAP ) ? (Map) ((TreeMap) _parameterMap).clone() : _parameterMap;
		else 
			copy._parameterMap	= ( _parameterMap != Collections.EMPTY_MAP ) ? (Map) ((HashMap) _parameterMap).clone() : _parameterMap;
			
		return copy;	
	}
	
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() ){
			return null;
		}
		
		ParameterizedSymbol newParameterized = (ParameterizedSymbol) super.instantiate( template, argMap );

		if( _returnType != null ){
			if( _returnType.isType( TypeInfo.t_templateParameter ) ){
				if( argMap.containsKey( _returnType ) ){
					newParameterized.setReturnType( getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME ) );
					newParameterized.getReturnType().setTypeInfo( (TypeInfo) argMap.get( _returnType ) );
					newParameterized.getReturnType().setInstantiatedSymbol( _returnType );
				}
			} else {
				if( _returnType instanceof IDeferredTemplateInstance )
					template.registerDeferredInstatiation( newParameterized, _returnType, ITemplateSymbol.DeferredKind.RETURN_TYPE, argMap );
				else
					newParameterized.setReturnType( _returnType.instantiate( template, argMap ) );
			}
		}
		
		//handle template parameter lists in TemplateSymbol, only do function parameter lists here.
		if( !isType( TypeInfo.t_template ) ){
			List params = getParameterList();
			int size = params.size();
			
			newParameterized.getParameterList().clear();
			newParameterized.getParameterMap().clear();
			
			ISymbol param = null, newParam = null;
			
			for( int i = 0; i < size; i++ ){
				param = (ISymbol) params.get(i);
				newParam = param.instantiate( template, argMap );
				
				newParameterized.addParameter( newParam );
			}	
		}
		
		return newParameterized;	
	}
	
	public void instantiateDeferredReturnType( ISymbol returnType, ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		setReturnType( returnType.instantiate( template, argMap ) );
	}
	
	
	public void prepareForParameters( int numParams ){
		if( _parameterList == Collections.EMPTY_LIST ){
			_parameterList = new ArrayList( numParams );
		} else {
			((ArrayList)_parameterList).ensureCapacity( numParams );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParameter( ISymbol param ){
		if( _parameterList == Collections.EMPTY_LIST)
			_parameterList = new ArrayList(8);

		_parameterList.add( param );
		
		String name = param.getName();
		if( name != null && !name.equals(ParserSymbolTable.EMPTY_NAME) )
		{
			if( _parameterMap == Collections.EMPTY_MAP ){
				if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE )
					_parameterMap = new TreeMap( new SymbolTableComparator() );
				else 
					_parameterMap = new HashMap( );
			}
			
			if( !_parameterMap.containsKey( name ) )
				_parameterMap.put( name, param );
		}
		
		param.setContainingSymbol( this );
		param.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
//		Command command = new AddParameterCommand( this, param );
//		getSymbolTable().pushCommand( command );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, int, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( TypeInfo.eType type, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME);
				
		TypeInfo t = param.getTypeInfo();
		t.setTypeInfo( info );
		t.setType( type );
		t.addPtrOperator( ptrOp );
		t.setHasDefault( hasDefault );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( ISymbol typeSymbol, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME);
		
		TypeInfo nfo = param.getTypeInfo();
		nfo.setTypeInfo( info );
		nfo.setType( TypeInfo.t_type );
		nfo.setTypeSymbol( typeSymbol );
		nfo.addPtrOperator( ptrOp );
		nfo.setHasDefault( hasDefault );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterMap()
	 */
	public Map getParameterMap(){
		return _parameterMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterList()
	 */
	public List getParameterList(){
		return _parameterList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setParameterList(java.util.List)
	 */
//	public void setParameterList( List list ){
//		_parameterList = new LinkedList( list );	
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSameParameters(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public boolean hasSameParameters( IParameterizedSymbol function ){
		if( function.getType() != getType() ){
			return false;	
		}
	
		int size = ( getParameterList() == null ) ? 0 : getParameterList().size();
		int fsize = ( function.getParameterList() == null ) ? 0 : function.getParameterList().size();
		if( fsize != size ){
			return false; 
		}
		if( fsize == 0 )
			return true;
	
		List params = getParameterList();
		List functionParams = function.getParameterList();
	
		TypeInfo info = null;
		TypeInfo fInfo = null;
	
		for( int i = 0; i < size; i++ ){
			ISymbol p = (ISymbol) params.get(i);
			ISymbol pf = (ISymbol) functionParams.get(i);
			
			info = p.getTypeInfo();
			fInfo = pf.getTypeInfo();
			
			//parameters that differ only in the use of equivalent typedef types are equivalent.
			info = ParserSymbolTable.getFlatTypeInfo( info, true );
			fInfo = ParserSymbolTable.getFlatTypeInfo( fInfo, true );
			
			for( TypeInfo nfo = info; nfo != null; nfo = fInfo ){
				//an array declaration is adjusted to become a pointer declaration
				//only the second and subsequent array dimensions are significant in parameter types
				ListIterator ptrs = nfo.getPtrOperators().listIterator(); 
				if( ptrs.hasNext() ){
					PtrOp op = (PtrOp) ptrs.next();
					if( op.getType() == PtrOp.t_array ){
						ptrs.remove();
						ptrs.add( new PtrOp( PtrOp.t_pointer, op.isConst(), op.isVolatile() ) );
					}
				}
				
				//a function type is adjusted to become a pointer to function type
				if( nfo.isType( TypeInfo.t_type ) && nfo.getTypeSymbol() != null && 
					nfo.getTypeSymbol().isType( TypeInfo.t_function ) )
				{
					if( nfo.getPtrOperators().size() == 0 ){
						nfo.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
					}
				}

				//const and volatile type-specifiers are ignored (only the outermost level)
				if( nfo.getPtrOperators().size() == 0 ){
					nfo.setBit( false, TypeInfo.isConst );
					nfo.setBit( false, TypeInfo.isVolatile );
				} else {
					PtrOp op = (PtrOp) nfo.getPtrOperators().get( nfo.getPtrOperators().size() - 1 );
					op.setConst( false );
					op.setVolatile( false );
				}
				
				if( nfo == fInfo ) 
					break;
			}
			
			boolean equals = info.equals( fInfo );

			info.release();
			fInfo.release();
			
			if( ! equals )
				return false;
		}
	
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setReturnType(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setReturnType( ISymbol type ){
		_returnType = type;
		_returnType.setContainingSymbol( this );
		_returnType.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getReturnType()
	 */
	public ISymbol getReturnType(){
		return _returnType;
	}

	public void setHasVariableArgs( boolean var ){
		_hasVarArgs = var;
	}
	
	public boolean hasVariableArgs( ){
		return _hasVarArgs;
	}

//	static private class AddParameterCommand extends Command{
//		public AddParameterCommand( IParameterizedSymbol container, ISymbol parameter ){
//			_decl = container;
//			_param = parameter;
//		}
//		
//		public void undoIt(){
//			_decl.getParameterList().remove( _param );
//			
//			String name = _param.getName();
//			if( name != null && !name.equals( ParserSymbolTable.EMPTY_NAME) )
//			{	
//				_decl.getParameterMap().remove( name );
//			}
//		}
//		
//		private IParameterizedSymbol _decl;
//		private ISymbol _param;
//	}
	
	
	private 	List	_parameterList = Collections.EMPTY_LIST;	//have my cake
	private 	Map			_parameterMap  = Collections.EMPTY_MAP;	//and eat it too
	private 	ISymbol		_returnType;
	private 	boolean		_hasVarArgs = false;	//whether or not this function has variable arguments
}
