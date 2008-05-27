/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author jcamelon
 */
public class CPPASTQualifiedName extends CPPASTNode implements
		ICPPASTQualifiedName, IASTCompletionContext {

	public CPPASTQualifiedName() {
	}

	public IBinding resolveBinding() {
		// The full qualified name resolves to the same thing as the last name
		removeNullNames();
		IASTName lastName = getLastName();
		return lastName == null ? null : lastName.resolveBinding();
	}

	public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}
    	
    	return null;
	}

	@Override
	public String toString() {
		return (signature == null) ? "" : signature; //$NON-NLS-1$
	}

	public void addName(IASTName name) {
		if (name != null) {
			names = (IASTName[]) ArrayUtil.append(IASTName.class, names, ++namesPos, name);
			name.setParent(this);
			name.setPropertyInParent(SEGMENT_NAME);
		}
	}

	private void removeNullNames() {
        names = (IASTName[]) ArrayUtil.removeNullsAfter(IASTName.class, names, namesPos);
	}

	private IASTName[] names = null;
	private int namesPos= -1;
	private boolean isFullyQualified;
	private String signature;


	public IASTName[] getNames() {
		if (names == null)
			return IASTName.EMPTY_NAME_ARRAY;
		removeNullNames();
		return names;
	}

	public IASTName getLastName() {
		if (names == null || names.length == 0)
			return null;
		
		return names[names.length - 1];
	}
	
	public char[] toCharArray() {
		if (names == null)
			return new char[0];
		removeNullNames();

		// count first
		int len = 0;
		for (int i = 0; i < names.length; ++i) {
			char[] n = names[i].toCharArray();
			if (n == null)
				return null;
			len += n.length;
			if (i != names.length - 1)
				len += 2;
		}

		char[] nameArray = new char[len];
		int pos = 0;
		for (int i = 0; i < names.length; i++) {
			char[] n = names[i].toCharArray();
			System.arraycopy(n, 0, nameArray, pos, n.length);
			pos += n.length;
			if (i != names.length - 1) {
				nameArray[pos++] = ':';
				nameArray[pos++] = ':';
			}
		}
		return nameArray;
	}

	public boolean isFullyQualified() {
		return isFullyQualified;
	}

	public void setFullyQualified(boolean isFullyQualified) {
		this.isFullyQualified = isFullyQualified;
	}


	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitNames) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		IASTName[] ns = getNames();
		for (int i = 0; i < ns.length; i++) {
			if (i == names.length - 1) {
				// pointer-to-member qualified names have a dummy name as the last part of the name, don't visit it
				if (names[i].toCharArray().length > 0 && !names[i].accept(action))
					return false;
			} else if (!names[i].accept(action))
				return false;
		}
		
		if (action.shouldVisitNames) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		
		return true;
	}
	
	public int getRoleOfName(boolean allowResolution) {
        IASTNode parent = getParent();
        if (parent instanceof IASTInternalNameOwner) {
        	return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
        }
        if (parent instanceof IASTNameOwner) {
            return ((IASTNameOwner) parent).getRoleForName(this);
        }
        return IASTNameOwner.r_unclear;
	}

	public boolean isDeclaration() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if (role == IASTNameOwner.r_reference) return false;
			return true;
		}
		return false;
	}

	public boolean isReference() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if (role == IASTNameOwner.r_reference) return true;
			return false;
		}
		return false;
	}

	public int getRoleForName(IASTName n) {
		IASTName[] namez = getNames();
		for(int i = 0; i < names.length; ++i)
			if (namez[i] == n)
			{
				if (i < names.length - 1)
					return r_reference;
				IASTNode p = getParent();
				if (i == names.length - 1 && p instanceof IASTNameOwner)
					return ((IASTNameOwner)p).getRoleForName(this);
				return r_unclear;
			}
		return r_unclear;
	}

	public IBinding getBinding() {
		removeNullNames();
		return names[names.length - 1].getBinding();
	}

	public void setBinding(IBinding binding) {
		removeNullNames();
		names[names.length - 1].setBinding(binding);
	}

	public boolean isConversionOrOperator() {
		IASTName[] nonNullNames = getNames(); // ensure no null names
		
		int len=nonNullNames.length;
		if (nonNullNames[len - 1] instanceof ICPPASTConversionName || nonNullNames[len - 1] instanceof ICPPASTOperatorName) {
			return true;
		}
		
		// check templateId's name
		if (nonNullNames[len - 1] instanceof ICPPASTTemplateId) {
			IASTName tempName = ((ICPPASTTemplateId)nonNullNames[len - 1]).getTemplateName();
			if (tempName instanceof ICPPASTConversionName || tempName instanceof ICPPASTOperatorName) {
				return true;
			}
		}
		
		return false;
	}
    
    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            if (role == IASTNameOwner.r_definition) return true;
            return false;
        }
        return false;
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		
		if (names.length - 2 >= 0) {
			IBinding binding = names[names.length - 2].resolveBinding();
			if (binding instanceof ICPPClassType) {
				ICPPClassType classType = (ICPPClassType) binding;
				final boolean isDeclaration = getParent().getParent() instanceof IASTSimpleDeclaration;
				List<IBinding> filtered = filterClassScopeBindings(classType, bindings, isDeclaration);
			
				if (isDeclaration && nameMatches(classType.getNameCharArray(),
						n.toCharArray(), isPrefix)) {
					try {
						ICPPConstructor[] constructors = classType.getConstructors();
						for (int i = 0; i < constructors.length; i++) {
							if (!constructors[i].isImplicit()) {
								filtered.add(constructors[i]);
							}
						}
					} catch (DOMException e) {
					}
				}
				
				return filtered.toArray(new IBinding[filtered.size()]);
			}
		}

		return bindings;
	}
	
	private List<IBinding> filterClassScopeBindings(ICPPClassType classType,
			IBinding[] bindings, final boolean isDeclaration) {
		List<IBinding> filtered = new ArrayList<IBinding>();
		
		try {
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i] instanceof IField) {
					IField field = (IField) bindings[i];
					if (!field.isStatic()) continue;
				} else if (bindings[i] instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) bindings[i];
					if (method.isImplicit()) continue;
					if (method.isDestructor() || method instanceof ICPPConstructor) {
						if (!isDeclaration) continue;
					} else if (!method.isStatic() && !isDeclaration) {
						continue;
					}
				} else if (bindings[i] instanceof ICPPClassType) {
					ICPPClassType type = (ICPPClassType) bindings[i];
					if (type.isSameType(classType)) continue;
				} else if (!(bindings[i] instanceof IEnumerator || bindings[i] instanceof IEnumeration) || isDeclaration) {
					continue;
				}
				
				filtered.add(bindings[i]);
			}
		} catch (DOMException e) {
		}
		
		return filtered;
	}
	
	private boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix)
			return CharArrayUtils.equals(potential, 0, name.length, name, true);
		return CharArrayUtils.equals(potential, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#getLinkage()
	 */
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
}
