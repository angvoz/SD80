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
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
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
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
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
				IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
				PDOMBinding pdomEnumeration = adaptBinding(enumeration);
				if (pdomEnumeration instanceof PDOMCEnumeration)
				pdomBinding = new PDOMCEnumerator(pdom, parent, name,
						(PDOMCEnumeration)pdomEnumeration);
			} else if (binding instanceof ITypedef)
				pdomBinding = new PDOMCTypedef(pdom, parent, name, (ITypedef)binding);
		}
		
		if (pdomBinding != null)
			new PDOMName(pdom, name, file, pdomBinding);
		
		return pdomBinding;
	}

	private static final class FindBinding extends PDOMNamedNode.NodeFinder {
		PDOMBinding pdomBinding;
		final int desiredType;
		public FindBinding(PDOM pdom, char[] name, int desiredType) {
			super(pdom, name);
			this.desiredType = desiredType;
		}
		public boolean visit(int record) throws CoreException {
			if (record == 0)
				return true;
			PDOMBinding tBinding = pdom.getBinding(record);
			if (!tBinding.hasName(name))
				// no more bindings with our desired name
				return false;
			if (tBinding.getNodeType() != desiredType)
				// wrong type, try again
				return true;
			
			// got it
			pdomBinding = tBinding;
			return false;
		}
	}

	private static class FindBinding2 implements IPDOMVisitor {
		private PDOMBinding binding;
		private final char[] name;
		private final int[] desiredType;
		public FindBinding2(char[] name, int desiredType) {
			this(name, new int[] { desiredType });
		}
		public FindBinding2(char[] name, int[] desiredType) {
			this.name = name;
			this.desiredType = desiredType;
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof PDOMBinding) {
				PDOMBinding tBinding = (PDOMBinding)node;
				if (tBinding.hasName(name)) {
					int nodeType = tBinding.getNodeType();
					for (int i = 0; i < desiredType.length; ++i)
						if (nodeType == desiredType[i]) {
							// got it
							binding = tBinding;
							throw new CoreException(Status.OK_STATUS);
						}
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public PDOMBinding getBinding() { return binding; }
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
		
		PDOMNode parent = getAdaptedParent(binding);
		if (parent == this) {
			FindBinding visitor = new FindBinding(pdom, binding.getNameCharArray(), getBindingType(binding));
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof IPDOMMemberOwner) {
			FindBinding2 visitor = new FindBinding2(binding.getNameCharArray(), getBindingType(binding));
			IPDOMMemberOwner owner = (IPDOMMemberOwner)parent;
			try {
				owner.accept(visitor);
			} catch (CoreException e) {
				if (e.getStatus().equals(Status.OK_STATUS))
					return visitor.getBinding();
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
		}

		return super.getNode(record);
	}

	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		IASTNode parent = name.getParent();
		if (parent instanceof IASTIdExpression) {
			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CFUNCTION);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			} else {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CVARIABLE);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			}
		} else if (parent instanceof ICASTElaboratedTypeSpecifier) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CSTRUCTURE);
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		}
		return null;
	}
	
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
