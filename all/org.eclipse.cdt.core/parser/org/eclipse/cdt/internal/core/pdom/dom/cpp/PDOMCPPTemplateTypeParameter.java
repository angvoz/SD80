/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPTemplateTypeParameter extends PDOMCPPBinding implements IPDOMMemberOwner,
		ICPPTemplateTypeParameter, ICPPUnknownBinding, IIndexType {

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE + 0;	
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 4;

	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 8;
	private ICPPScope fUnknownScope;
	
	public PDOMCPPTemplateTypeParameter(PDOM pdom, PDOMNode parent,
			ICPPTemplateTypeParameter param) throws CoreException {
		super(pdom, parent, param.getNameCharArray());
		
		try {
			IType dflt = param.getDefault();
			if (dflt != null) {
				PDOMNode typeNode = getLinkageImpl().addType(this, dflt);
				if (typeNode != null) {
					pdom.getDB().putInt(record + DEFAULT_TYPE, typeNode.getRecord());
				}
			}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPTemplateTypeParameter(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_TYPE_PARAMETER;
	}
	
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
	
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
		
		if (type instanceof ICPPTemplateTypeParameter && !(type instanceof ProblemBinding)) {
			ICPPTemplateTypeParameter ttp= (ICPPTemplateTypeParameter) type;
			try {
				char[][] ttpName= ttp.getQualifiedNameCharArray();
				return hasQualifiedName(ttpName, ttpName.length - 1);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	public IType getDefault() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + DEFAULT_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public ICPPBinding getParameterOwner() throws CoreException {
		return (ICPPBinding) getParentBinding();
	}
	
	@Override
	public Object clone() { fail(); return null; }


	public ICPPScope getUnknownScope() {
		if (fUnknownScope == null) {
			fUnknownScope= new PDOMCPPUnknownScope(this, new CPPASTName(getNameCharArray()));
		}
		return fUnknownScope;
	}

	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap, ICPPScope instantiationScope) { 
		return null;
	}
	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}
	public ICPPUnknownBinding getUnknownContainerBinding() {
		return null;
	}
}
