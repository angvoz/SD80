/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TemplateSymbol	extends ParameterizedSymbol	implements ITemplateSymbol {
	
	protected TemplateSymbol ( ParserSymbolTable table, String name ){
		super( table, name, TypeInfo.t_template );
	}
	
	protected TemplateSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	public Object clone(){
		TemplateSymbol copy = (TemplateSymbol)super.clone();

		copy._defnParameterMap = ( _defnParameterMap != Collections.EMPTY_MAP ) ? (Map)((HashMap) _defnParameterMap).clone() : _defnParameterMap;
		copy._instantiations = ( _instantiations != Collections.EMPTY_MAP ) ? (Map)((HashMap) _instantiations).clone() : _instantiations;
		
		return copy;	
	}
	
	public IContainerSymbol getTemplatedSymbol(){
		Iterator iter = getContentsIterator();
		if( iter.hasNext() ){
			IContainerSymbol contained = (IContainerSymbol) iter.next();
			return contained;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#instantiate(java.util.List)
	 */
	public ISymbol instantiate( List arguments ) throws ParserSymbolTableException{
		if( getType() != TypeInfo.t_template &&
				( getType() != TypeInfo.t_templateParameter || 
						getTypeInfo().getTemplateParameterType() != TypeInfo.t_template ) )
		{
			return null;
		}
		
		ITemplateSymbol template = TemplateEngine.matchTemplatePartialSpecialization( this, arguments );
				
		if( template != null && template instanceof ISpecializedSymbol ){
			return template.instantiate( arguments );	
		}
		
		if( template == null ){
			template = this;
		}
		
		List paramList = template.getParameterList();
		int numParams = ( paramList != null ) ? paramList.size() : 0;
		int numArgs = arguments.size();
		
		if( numParams == 0 ){
			return null;				
		}

		HashMap map = new HashMap();
		ISymbol param = null;
		TypeInfo arg = null;
		List actualArgs = new ArrayList( numParams );
		
		ISymbol templatedSymbol = template.getTemplatedSymbol();
		while( templatedSymbol != null && templatedSymbol.isTemplateInstance() ){
			templatedSymbol = templatedSymbol.getInstantiatedSymbol();
		}
		
		for( int i = 0; i < numParams; i++ ){
			param = (ISymbol) paramList.get(i);
			
			param = TemplateEngine.translateParameterForDefinition ( templatedSymbol, param, getDefinitionParameterMap() );
			
			if( i < numArgs ){
				arg = (TypeInfo) arguments.get(i);
				//If the argument is a template parameter, we can't instantiate yet, defer for later
				if( arg.isType( TypeInfo.t_type ) ){
					if( arg.getTypeSymbol() == null ) 
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
					else if( arg.getTypeSymbol().isType( TypeInfo.t_templateParameter ) )
						return deferredInstance( arguments );
				}
			} else {
				Object obj = param.getTypeInfo().getDefault();
				if( obj != null && obj instanceof TypeInfo ){
					arg = (TypeInfo) obj;
					if( arg.isType( TypeInfo.t_type ) && arg.getTypeSymbol().isType( TypeInfo.t_templateParameter ) ){
						if( map.containsKey( arg.getTypeSymbol() ) ){
							arg = (TypeInfo) map.get( arg.getTypeSymbol() );
						} else {
							throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
						}
					} else if( arg.isType( TypeInfo.t_type ) && arg.getTypeSymbol() instanceof IDeferredTemplateInstance ){
						IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) arg.getTypeSymbol();
						arg = new TypeInfo( arg );
						arg.setTypeSymbol( deferred.instantiate( this, map ) );
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );					
				}
			}
			
			if( TemplateEngine.matchTemplateParameterAndArgument( param, arg ) ){
				map.put( param, arg );
				actualArgs.add( arg );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
			}
		}
		
		IContainerSymbol instance = findInstantiation( actualArgs );
		if( instance != null ){
			return instance;
		} 
		if( template.isType( TypeInfo.t_templateParameter ) ){
			//template template parameter.  must defer instantiation
			return deferredInstance( arguments );
		} 
		
		IContainerSymbol symbol = template.getTemplatedSymbol(); 
		ISymbol temp = TemplateEngine.checkForTemplateExplicitSpecialization( template, symbol, actualArgs );
		symbol = (IContainerSymbol) ( temp != null ? temp : symbol);
			
		instance = (IContainerSymbol) symbol.instantiate( template, map );
		addInstantiation( instance, actualArgs );
		
		try{
			processDeferredInstantiations();
		} catch( ParserSymbolTableException e ){
			if( e.reason == ParserSymbolTableException.r_RecursiveTemplate ){
				//clean up some.
				removeInstantiation( instance );
			}
			throw e;
		}
		
		return instance;		
	}
	
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() ){
			return null;
		}
		
		TemplateSymbol newTemplate = (TemplateSymbol) super.instantiate( template, argMap );
		
		//we don't want to instantiate the template parameters, just the defaults if there are any
		List parameters = newTemplate.getParameterList();
		int size = parameters.size();
		ISymbol param = null;
		for( int i = 0; i < size; i++ ){
			param = (ISymbol) parameters.get(i);
			Object obj = param.getTypeInfo().getDefault();
			if( obj instanceof TypeInfo ){
				param.getTypeInfo().setDefault( TemplateEngine.instantiateTypeInfo( (TypeInfo) obj, template, argMap ) );
			}
		}	
		
		return newTemplate;
	}
	
	public void addParameter( ISymbol param ) {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}
	
	public void addTemplateParameter( ISymbol param ) throws ParserSymbolTableException {
		if( isType( TypeInfo.t_template ) || getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ){
			if( !isAllowableTemplateParameter( param ) ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateParameter );
			}
			modifyTemplateParameter( param );
		}
		
		super.addParameter( param );
	}
	
	private boolean isAllowableTemplateParameter( ISymbol param ) {
		if( !param.isType( TypeInfo.t_templateParameter ) )
			return false;
		
		if(  !getName().equals( ParserSymbolTable.EMPTY_NAME ) && param.getName().equals( getName() ) ){
			return false;
		}
		
		if( param.getTypeInfo().getTemplateParameterType() != TypeInfo.t_typeName &&
			param.getTypeInfo().getTemplateParameterType() != TypeInfo.t_template )
		{
			TypeInfo info = param.getTypeInfo();
			//a non-type template parameter shall have one of the following:
			//integral or enumeration type
			//pointer to object or pointer to function
			//reference to object or reference to function
			//pointer to member

			//14.1-7
			//A non-type template-parameter shall not be declared to have floating point, class or void type
			if( info.getPtrOperators().size() == 0 )
				if( info.getTemplateParameterType() == TypeInfo.t_float        ||
					info.getTemplateParameterType() == TypeInfo.t_double       ||
					info.getTemplateParameterType() == TypeInfo.t_class        ||
					info.getTemplateParameterType() == TypeInfo.t_struct       ||
					info.getTemplateParameterType() == TypeInfo.t_union        ||
					info.getTemplateParameterType() == TypeInfo.t_enumeration  ||
					info.getTemplateParameterType() == TypeInfo.t_void         )
				{
					return false;
				}
		}
		return true;			
	}
	
	private void modifyTemplateParameter( ISymbol param ){
		List ptrs = param.getPtrOperators();
		if( ptrs.size() > 0 ){
			PtrOp op = (PtrOp) ptrs.get( 0 );
			if( op.getType() == PtrOp.t_array ){
				op.setType( PtrOp.t_pointer );
			}
		} else if ( param.isType( TypeInfo.t_type ) && param.getTypeSymbol().isType( TypeInfo.t_function ) ){
			param.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		}
	}
	




	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSpecializations()
	 */
	public boolean hasSpecializations(){
		return !_specializations.isEmpty();
	}
	
	public void addExplicitSpecialization( ISymbol symbol, List args ) throws ParserSymbolTableException{
		
		List actualArgs = TemplateEngine.verifyExplicitArguments( this, args, symbol );
		
		if( _explicitSpecializations == Collections.EMPTY_MAP )
			_explicitSpecializations = new HashMap();
		
		Map specs = null;
		List key = null;
		
		Iterator iter = _explicitSpecializations.keySet().iterator();
		while( iter.hasNext() ){
			List list = (List) iter.next();
			if( list.equals( args ) ){
				key = list;
				break;
			}
		}
		
		if( key != null ){
			specs = (Map) _explicitSpecializations.get( key );
		} else {
			specs = new HashMap();
			_explicitSpecializations.put( new ArrayList( actualArgs ), specs );
		}
		
		ISymbol found = null;
		try{
			if( symbol.isType( TypeInfo.t_function ) || symbol.isType( TypeInfo.t_constructor ) ){
				List params = ((IParameterizedSymbol) symbol).getParameterList();
				int size = params.size();
				List fnArgs = new ArrayList( size );
				for( int i = 0; i < size; i++){
					fnArgs.add( ((ISymbol)params.get(i)).getTypeInfo() );
				}
				found = getTemplatedSymbol().lookupMethodForDefinition( symbol.getName(), fnArgs );
			} else {
				found = getTemplatedSymbol().lookupMemberForDefinition( symbol.getName() );
			}
		} catch (ParserSymbolTableException e) {
		}
		if( found == null && getTemplatedSymbol().getName().equals( symbol.getName() ) ){
			found = getTemplatedSymbol();
			
			IContainerSymbol instance = findInstantiation( actualArgs );
			if( instance != null ){
				_instantiations.remove( findArgumentsFor( instance ) );
			}
		}

		if( found != null ){
			//in defining the explicit specialization for a member function, the factory would have set 
			//the specialization as the definition of the original declaration, which it is not
			if( found.getTypeInfo().isForwardDeclaration() && found.getTypeSymbol() == symbol )
				found.setTypeSymbol( null );
			
			//TODO, once we can instantiate members as we need them instead of at the same time as the class
			//then found should stay as the instance, for now though, we need the original (not 100% correct
			//but the best we can do for now)
			while( found.isTemplateInstance() ){
				found = found.getInstantiatedSymbol();
			}
		}
		
		if( found != null ){
			symbol.setIsTemplateMember( true );
			symbol.setContainingSymbol( found.getContainingSymbol() );
			specs.put( found, symbol );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addSpecialization(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addSpecialization( ISpecializedSymbol spec ){
		if( _specializations == Collections.EMPTY_LIST )
			_specializations = new ArrayList(4);
		
		_specializations.add( spec );
		
		spec.setContainingSymbol( getContainingSymbol() );	
		spec.setPrimaryTemplate( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getSpecializations()
	 */
	public List getSpecializations() {
		return _specializations;
	}
	
	public void addInstantiation( IContainerSymbol instance, List args ){
		List key = new ArrayList( args );
		if( _instantiations == Collections.EMPTY_MAP ){
			_instantiations = new HashMap();
		}
		_instantiations.put( key, instance );
	}
	
	public IContainerSymbol findInstantiation( List arguments ){
		if( _instantiations == Collections.EMPTY_MAP ){
			return null;
		}
		
		//TODO: we could optimize this by doing something other than a linear search.
		Iterator iter = _instantiations.keySet().iterator();
		List args = null;
		while( iter.hasNext() ){
			args = (List) iter.next();
			
			if( args.equals( arguments ) ){
				return (IContainerSymbol) _instantiations.get( args );
			}
		}
		return null;
	}
	
	public List findArgumentsFor( IContainerSymbol instance ){
		if( instance == null || !instance.isTemplateInstance() )
			return null;
		
		ITemplateSymbol template = (ITemplateSymbol) instance.getInstantiatedSymbol().getContainingSymbol();
		if( template != this )
			return null;
		
		Iterator iter = _instantiations.keySet().iterator();
		while( iter.hasNext() ){
			List args = (List) iter.next();
			if( _instantiations.get( args ) == instance ){
				return args;
			}
		}
		
		return null;
	}
	
	public void removeInstantiation( IContainerSymbol symbol ){
		List args = findArgumentsFor( symbol );
		if( args != null ){
			_instantiations.remove( args );
		}
	}
	
	public Map getDefinitionParameterMap(){
		return _defnParameterMap;
	}
	
	protected void addToDefinitionParameterMap( ISymbol newSymbol, Map defnMap ){
		if( _defnParameterMap == Collections.EMPTY_MAP )
			_defnParameterMap = new HashMap();
		_defnParameterMap.put( newSymbol, defnMap );
	}
	
	public IDeferredTemplateInstance deferredInstance( List args ){
		return new DeferredTemplateInstance( getSymbolTable(), this, args );
	}

	public Map getExplicitSpecializations() {
		return _explicitSpecializations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#registerDeferredInstatiation(org.eclipse.cdt.internal.core.parser.pst.ParameterizedSymbol, org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol.DeferredKind)
	 */
	public void registerDeferredInstatiation( Object obj0, Object obj1, DeferredKind kind, Map argMap ) {
		if( _deferredInstantiations == Collections.EMPTY_LIST )
			_deferredInstantiations = new ArrayList(8);
		
		_deferredInstantiations.add( new Object [] { obj0, obj1, kind, argMap } );
	}

	public int getNumberDeferredInstantiations(){
		return _deferredInstantiations.size();
	}
	
	protected void processDeferredInstantiations() throws ParserSymbolTableException{
		if( _deferredInstantiations == Collections.EMPTY_LIST )
			return;
		
		if( _processingDeferred ){
			return;
		}
		_processingDeferred = true;
		int numDeferred = _deferredInstantiations.size();
		int numProcessed = 0;
		int loopCount = 0;
		while( numDeferred > numProcessed ){
			for( int i = numProcessed; i < numDeferred; i++ ){
				Object [] objs = (Object [])_deferredInstantiations.get(i);
				
				DeferredKind kind = (DeferredKind) objs[2];
				
				if( kind == DeferredKind.PARENT ){
					DerivableContainerSymbol d = (DerivableContainerSymbol) objs[0];
					d.instantiateDeferredParent( (ISymbol) objs[ 1 ], this, (Map) objs[3] );
				} else if( kind == DeferredKind.RETURN_TYPE ){
					ParameterizedSymbol p = (ParameterizedSymbol) objs[0];
					p.instantiateDeferredReturnType( (ISymbol) objs[1], this, (Map) objs[3] );
				} else if( kind == DeferredKind.TYPE_SYMBOL ){
					TemplateEngine.instantiateDeferredTypeInfo( (TypeInfo) objs[0], this, (Map) objs[3] );
				}
				numProcessed++;
			}
			numDeferred = _deferredInstantiations.size();
			if( ++loopCount > ParserSymbolTable.TEMPLATE_LOOP_THRESHOLD ){
				discardDeferredInstantiations();
				_processingDeferred = false;
				throw new ParserSymbolTableException( ParserSymbolTableException.r_RecursiveTemplate );
			}
		}
		_processingDeferred = false;
	}
	
	private void discardDeferredInstantiations(){
		int size = _deferredInstantiations.size();
		for( int i = 0; i < size; i++ ){
			Object [] objs = (Object []) _deferredInstantiations.get(i);
			
			DeferredKind kind = (DeferredKind) objs[2];
			
			if( kind == DeferredKind.PARENT ){
				DerivableContainerSymbol d = (DerivableContainerSymbol) objs[0];
				d.discardDeferredParent( (IDeferredTemplateInstance) objs[1], this, (Map) objs[3] );
			} else if( kind == DeferredKind.RETURN_TYPE ){
				ParameterizedSymbol p = (ParameterizedSymbol) objs[0];
				p.discardDeferredReturnType( (ISymbol) objs[1], this, (Map) objs[3] );
			} else if( kind == DeferredKind.TYPE_SYMBOL ){
				TemplateEngine.discardDeferredTypeInfo( (TypeInfo) objs[0], this, (Map) objs[3] );
			}
		}
		_deferredInstantiations.clear();
	}
	
	private		List  _specializations         = Collections.EMPTY_LIST;	//template specializations
	private     Map	  _explicitSpecializations = Collections.EMPTY_MAP;		//explicit specializations
	private		Map	  _defnParameterMap        = Collections.EMPTY_MAP;		//members could be defined with different template parameter names
	private 	Map	  _instantiations          = Collections.EMPTY_MAP;
	private     List  _deferredInstantiations  = Collections.EMPTY_LIST;	//used to avoid recursive loop
	private     boolean _processingDeferred = false;
		
	
}