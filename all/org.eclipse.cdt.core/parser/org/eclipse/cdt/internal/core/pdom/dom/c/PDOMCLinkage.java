/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.bid.IBindingIdentityFactory;
import org.eclipse.cdt.internal.core.dom.bid.ILocalBindingIdentity;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.FindBindingByLinkageConstant;
import org.eclipse.cdt.internal.core.pdom.dom.FindEquivalentBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 */
class PDOMCLinkage extends PDOMLinkage {

	public PDOMCLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCLinkage(PDOM pdom) throws CoreException {
		super(pdom, C_LINKAGE_ID, C_LINKAGE_ID.toCharArray()); 
	}

	public int getNodeType() {
		return LINKAGE;
	}
	
	public String getID() {
		return C_LINKAGE_ID;
	}
	
	public static final int CVARIABLE = PDOMLinkage.LAST_NODE_TYPE + 1;
	public static final int CFUNCTION = PDOMLinkage.LAST_NODE_TYPE + 2;
	public static final int CSTRUCTURE = PDOMLinkage.LAST_NODE_TYPE + 3;
	public static final int CFIELD = PDOMLinkage.LAST_NODE_TYPE + 4;
	public static final int CENUMERATION = PDOMLinkage.LAST_NODE_TYPE + 5;
	public static final int CENUMERATOR = PDOMLinkage.LAST_NODE_TYPE + 6;
	public static final int CTYPEDEF = PDOMLinkage.LAST_NODE_TYPE + 7;
	public static final int CPARAMETER = PDOMLinkage.LAST_NODE_TYPE + 8;
	public static final int CBASICTYPE = PDOMLinkage.LAST_NODE_TYPE + 9;

	public ILanguage getLanguage() {
		return new GCCLanguage();
	}

		

	public PDOMBinding addName(IASTName name, PDOMFile file) throws CoreException {
		if (name == null)
			return null;

		char[] namechars = name.toCharArray();
		if (namechars == null || name.toCharArray().length == 0)
			return null;

		IBinding binding = name.resolveBinding();
		if (binding == null || binding instanceof IProblemBinding)
			// can't tell what it is
			return null;

		if (binding instanceof IParameter)
			// skip parameters
			return null;

		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding == null) {
			PDOMNode parent = getAdaptedParent(binding);
			if (parent == null)
				return null;

			if (binding instanceof IParameter)
				return null; // skip parameters
			else if (binding instanceof IField) { // must be before IVariable
				if (parent instanceof IPDOMMemberOwner)
					pdomBinding = new PDOMCField(pdom, (IPDOMMemberOwner)parent, name);
			} else if (binding instanceof IVariable)
				pdomBinding = new PDOMCVariable(pdom, parent, name);
			else if (binding instanceof IFunction)
				pdomBinding = new PDOMCFunction(pdom, parent, name);
			else if (binding instanceof ICompositeType)
				pdomBinding = new PDOMCStructure(pdom, parent, name);
			else if (binding instanceof IEnumeration)
				pdomBinding = new PDOMCEnumeration(pdom, parent, name);
			else if (binding instanceof IEnumerator) {
				try {
					IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
					PDOMBinding pdomEnumeration = adaptBinding(enumeration);
					if (pdomEnumeration instanceof PDOMCEnumeration)
						pdomBinding = new PDOMCEnumerator(pdom, parent, name,
								(PDOMCEnumeration)pdomEnumeration);
				} catch (DOMException e) {
					throw new CoreException(Util.createStatus(e));
				}
			} else if (binding instanceof ITypedef)
				pdomBinding = new PDOMCTypedef(pdom, parent, name, (ITypedef)binding);
			
			if(pdomBinding!=null) {
				parent.addChild(pdomBinding);
			}
		}

		if (pdomBinding != null)
			new PDOMName(pdom, name, file, pdomBinding);

		return pdomBinding;
	}

	protected int getBindingType(IBinding binding) {
		if (binding instanceof IField)
			// This needs to be before variable
			return CFIELD;
		else if (binding instanceof IVariable)
			return CVARIABLE;
		else if (binding instanceof IFunction)
			return CFUNCTION;
		else if (binding instanceof ICompositeType)
			return CSTRUCTURE;
		else if (binding instanceof IEnumeration)
			return CENUMERATION;
		else if (binding instanceof IEnumerator)
			return CENUMERATOR;
		else if (binding instanceof ITypedef)
			return CTYPEDEF;
		else
			return 0;
	}

	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
			// so if the binding is from another pdom it has to be adapted. 
		}

		
		FindEquivalentBinding visitor = new FindEquivalentBinding(this, binding);
		PDOMNode parent = getAdaptedParent(binding);

		if (parent == this) {
			getIndex().accept(visitor);
			return visitor.getResult();
		} else if (parent instanceof IPDOMMemberOwner) {
			IPDOMMemberOwner owner = (IPDOMMemberOwner)parent;
			try {
				owner.accept(visitor);
			} catch (CoreException e) {
				if (e.getStatus().equals(Status.OK_STATUS))
					return visitor.getResult();
				else
					throw e;
			}
		}
		
		return null;
	}

	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CVARIABLE:
			return new PDOMCVariable(pdom, record);
		case CFUNCTION:
			return new PDOMCFunction(pdom, record);
		case CSTRUCTURE:
			return new PDOMCStructure(pdom, record);
		case CFIELD:
			return new PDOMCField(pdom, record);
		case CENUMERATION:
			return new PDOMCEnumeration(pdom, record);
		case CENUMERATOR:
			return new PDOMCEnumerator(pdom, record);
		case CTYPEDEF:
			return new PDOMCTypedef(pdom, record);
		case CPARAMETER:
			return new PDOMCParameter(pdom, record);
		case CBASICTYPE:
			return new PDOMCBasicType(pdom, record);
		}

		return super.getNode(record);
	}

	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		int constant;
		IASTNode parent = name.getParent();
		if (parent instanceof IASTIdExpression) {			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				constant = CFUNCTION;
			} else {
				constant = CVARIABLE;
			}
		} else if (parent instanceof ICASTElaboratedTypeSpecifier) {
			constant = CSTRUCTURE;
		} else {
			return null;
		}
		
		FindBindingByLinkageConstant finder = new FindBindingByLinkageConstant(this, name.toCharArray(), constant);
		getIndex().accept(finder);
		return finder.getResult();
	}

	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof ICBasicType) {
			return new PDOMCBasicType(pdom, parent, (ICBasicType)type);
		} else if (type instanceof ICompositeType) {
			FindEquivalentBinding feb = new FindEquivalentBinding(this,(ICompositeType)type);
			getIndex().accept(feb);
			if(feb.getResult()!=null) {
				return feb.getResult();
			}
		}
		return super.addType(parent, type); 
	}
	
	public ILocalBindingIdentity getLocalBindingIdentity(IBinding b) throws CoreException {
		return new CBindingIdentity(b, this);
	}
	
	public IBindingIdentityFactory getBindingIdentityFactory() {
		return this;
	}
}
