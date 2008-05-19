/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPClassTemplatePartialSpecialization extends
		PDOMCPPClassTemplate implements ICPPClassTemplatePartialSpecialization, ICPPSpecialization, IPDOMOverloader {
	
	private static final int ARGUMENTS = PDOMCPPClassTemplate.RECORD_SIZE + 0;
	private static final int SIGNATURE_HASH = PDOMCPPClassTemplate.RECORD_SIZE + 4;
	private static final int PRIMARY = PDOMCPPClassTemplate.RECORD_SIZE + 8;
	private static final int NEXT_PARTIAL = PDOMCPPClassTemplate.RECORD_SIZE + 12;
	
	/**
	 * The size in bytes of a PDOMCPPClassTemplatePartialSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassTemplate.RECORD_SIZE + 16;
	
	public PDOMCPPClassTemplatePartialSpecialization(PDOM pdom,
			PDOMNode parent, ICPPClassTemplatePartialSpecialization partial, PDOMCPPClassTemplate primary) throws CoreException {
		super(pdom, parent, partial);
		pdom.getDB().putInt(record + PRIMARY, primary.getRecord());
		primary.addPartial(this);
		
		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(partial);
			pdom.getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public PDOMCPPClassTemplatePartialSpecialization(PDOM pdom,
			int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	public int getSignatureHash() throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_HASH);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
	}

	public PDOMCPPClassTemplatePartialSpecialization getNextPartial() throws CoreException {
		int value = pdom.getDB().getInt(record + NEXT_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecialization(pdom, value) : null;
	}
	
	public void setNextPartial(PDOMCPPClassTemplatePartialSpecialization partial) throws CoreException {
		int value = partial != null ? partial.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_PARTIAL, value);
	}
	
	public ICPPClassTemplate getPrimaryClassTemplate() {
		try {
			return new PDOMCPPClassTemplate(pdom, pdom.getDB().getInt(record + PRIMARY));
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	public IBinding getSpecializedBinding() {
		return getPrimaryClassTemplate();
	}
	
	public void addArgument(IType type) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		PDOMNode typeNode = getLinkageImpl().addType(this, type);
		if (typeNode != null)
			list.addMember(typeNode);
	}
	
	private static class TemplateArgumentCollector implements IPDOMVisitor {
		private List<IType> args = new ArrayList<IType>();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof IType)
				args.add((IType) node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IType[] getTemplateArguments() {
			return args.toArray(new IType[args.size()]);
		}
	}
	
	public IType[] getArguments() {
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
			TemplateArgumentCollector visitor = new TemplateArgumentCollector();
			list.accept(visitor);
			
			return visitor.getTemplateArguments();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IType[0];
		}
	}
	
	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = super.pdomCompareTo(other);
		if(cmp==0) {
			if(other instanceof PDOMCPPClassTemplatePartialSpecialization) {
				try {
					PDOMCPPClassTemplatePartialSpecialization otherSpec = (PDOMCPPClassTemplatePartialSpecialization) other;
					int mySM = getSignatureHash();
					int otherSM = otherSpec.getSignatureHash();
					return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
				} catch(CoreException ce) {
					CCorePlugin.log(ce);
				}
			} else {
				throw new PDOMNotImplementedError();
			}
		}
		return cmp;
	}

	@Override
	public IBinding instantiate(IType[] args) {
		args= SemanticUtil.getSimplifiedTypes(args);
		
		ICPPSpecialization instance = getInstance( args );
		if( instance != null ){
			return instance;
		}
		
		IType [] specArgs = getArguments();
		if( specArgs.length != args.length ){
			return null;
		}
		
		boolean argsContainDependentType= false;
		ObjectMap argMap = new ObjectMap( specArgs.length );
		int numSpecArgs = specArgs.length;
		for( int i = 0; i < numSpecArgs; i++ ){
			IType spec = specArgs[i];
			IType arg = args[i];
			
			argsContainDependentType= argsContainDependentType || CPPTemplates.isDependentType(arg);
			try {
				if( !CPPTemplates.deduceTemplateArgument( argMap,  spec, arg ) )
					return null;
			} catch (DOMException e) {
				return null;
			}
		}

		if (argsContainDependentType) {
			return deferredInstance( argMap, args );
		}

		ICPPTemplateParameter [] params = getTemplateParameters();
		int numParams = params.length;
		for( int i = 0; i < numParams; i++ ){
			if( params[i] instanceof IType && !argMap.containsKey( params[i] ) )
				return null;
		}
		
		return CPPTemplates.createInstance( (ICPPScope) getScope(), this, argMap, args );
	}

	private static class NodeCollector implements IPDOMVisitor {
		private List<IPDOMNode> nodes = new ArrayList<IPDOMNode>();
		public boolean visit(IPDOMNode node) throws CoreException {
			nodes.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IPDOMNode[] getNodes() {
			return nodes.toArray(new IPDOMNode[nodes.size()]);
		}
	}
	
	public ObjectMap getArgumentMap() {
		try {
			PDOMNodeLinkedList argList = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
			ICPPTemplateParameter[] params;
			try {
				params = getPrimaryClassTemplate().getTemplateParameters();
			} catch (DOMException e) {
				return ObjectMap.EMPTY_MAP;
			}
			NodeCollector argVisitor = new NodeCollector();
			argList.accept(argVisitor);
			IPDOMNode[] argNodes = argVisitor.getNodes();
			
			ObjectMap map = new ObjectMap(params.length);
			for (int i = 0; i < params.length; i++) {
				map.put(params[i], argNodes[i]);
			}
			
			return map;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		if( type instanceof ICPPSpecialization ) {
	        	ICPPClassType ct1= (ICPPClassType) getSpecializedBinding();
	        	ICPPClassType ct2= (ICPPClassType) ((ICPPSpecialization)type).getSpecializedBinding();
	        	if(!ct1.isSameType(ct2))
	        		return false;
	        	
	        	ObjectMap m1 = getArgumentMap(), m2 = ((ICPPSpecialization)type).getArgumentMap();
	        	if( m1 == null || m2 == null || m1.size() != m2.size())
	        		return false;
	        	for( int i = 0; i < m1.size(); i++ ){
	        		IType t1 = (IType) m1.getAt( i );
	        		IType t2 = (IType) m2.getAt( i );
	        		if( t1 == null || ! t1.isSameType( t2 ) )
	        			return false;
	        	}
	        	return true;
	        }
		
		return false;
	}
}
