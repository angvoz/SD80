/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public abstract class PDOMBinding extends PDOMNamedNode implements IPDOMBinding {
	public static final PDOMBinding[] EMPTY_PDOMBINDING_ARRAY = {};

	private static final int FIRST_DECL_OFFSET   = PDOMNamedNode.RECORD_SIZE +  0; // size 4
	private static final int FIRST_DEF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 4; // size 4
	private static final int FIRST_REF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 8; // size 4
	private static final int LOCAL_TO_FILE		 = PDOMNamedNode.RECORD_SIZE + 12; // size 4
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 16;
	
	protected PDOMBinding(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent, name);
	}
	
	public PDOMBinding(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(PDOMBinding.class))
			return this;

		return null;
	}
	
	/**
	 * Is the binding as the record orphaned, i.e., has no declarations
	 * or references.
	 * Watch out, a binding may also be used in a type (e.g. pointer to class)
	 * 
	 * @param pdom
	 * @param record
	 * @return <code>true</code> if the binding is orphaned.
	 * @throws CoreException
	 */
	public static boolean isOrphaned(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) == 0
			&& db.getInt(record + FIRST_DEF_OFFSET) == 0
			&& db.getInt(record + FIRST_REF_OFFSET) == 0;
	}
	
	@Override
	public int getRecord() {
		return record;
	}

	public final boolean hasDeclaration() throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) != 0
			|| db.getInt(record + FIRST_DEF_OFFSET) != 0;
	}
	
	public void addDeclaration(PDOMName name) throws CoreException {
		PDOMName first = getFirstDeclaration();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDeclaration(name);
	}
	
	public void addDefinition(PDOMName name) throws CoreException {
		PDOMName first = getFirstDefinition();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDefinition(name);
	}
	
	public void addReference(PDOMName name) throws CoreException {
		PDOMName first = getFirstReference();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstReference(name);
	}
	
	public PDOMName getFirstDeclaration() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DECL_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDeclaration(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DECL_OFFSET, namerec);
	}
	
	public PDOMName getFirstDefinition() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DEF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDefinition(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DEF_OFFSET, namerec);
	}
	
	public PDOMName getFirstReference() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_REF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstReference(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_REF_OFFSET, namerec);
	}
	
	public final PDOMFile getLocalToFile() throws CoreException {
		final int filerec = getLocalToFileRec(pdom, record);
		return filerec == 0 ? null : new PDOMFile(pdom, filerec);
	}

	public final int getLocalToFileRec() throws CoreException {
		return pdom.getDB().getInt(record + LOCAL_TO_FILE);
	}

	public static int getLocalToFileRec(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + LOCAL_TO_FILE);
	}

	public final void setLocalToFileRec(int rec) throws CoreException {
		pdom.getDB().putInt(record + LOCAL_TO_FILE, rec);
	}

	public String getName() {
		try {
			return super.getDBName().getString();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return CharArrayUtils.EMPTY;
	}
	
	public IIndexScope getParent() {
		try {
			IBinding parent = getParentBinding();
			if (parent instanceof IIndexScope) {
				return (IIndexScope) parent;
			}
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}
	
	public final IIndexScope getScope() {
		// The parent node in the binding hierarchy is the scope. 
		try {
			IBinding parent= getParentBinding(); 
			if (parent instanceof ICPPClassType) {
				return (IIndexScope) ((ICPPClassType) parent).getCompositeScope();
			} else if (parent instanceof ICPPUnknownBinding) {
				return (IIndexScope) ((ICPPUnknownBinding) parent).getUnknownScope();
			}
			
			if (parent instanceof IIndexScope) {
				return (IIndexScope) parent;
			}
		} catch (DOMException e) {
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}
	
	public IIndexFragment getFragment() {
		return pdom;
	}

	@Override
	abstract protected int getRecordSize(); // superclass's implementation is no longer valid
	
	/* For debug purposes only.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return getName() + " " + getConstantNameForValue(getLinkageImpl(), getNodeType());  //$NON-NLS-1$
		} catch (CoreException ce) {
			return getName() + " " + getNodeType();  //$NON-NLS-1$
		}
	}
	
	/**
	 * For debug purposes only.
	 * @param linkage
	 * @param value
	 * @return String representation of <code>value</code>. 
	 */
	protected static String getConstantNameForValue(PDOMLinkage linkage, int value) {
		Class<? extends PDOMLinkage> c= linkage.getClass();
		Field[] fields= c.getFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				if ((field.getModifiers() & Modifier.STATIC) != 0) {
					if (int.class.equals(field.getType())) {
						int fvalue= field.getInt(null);
						if (fvalue == value)
							return field.getName();
					}
				}
			} catch (IllegalAccessException iae) {
				continue;
			} catch (IllegalArgumentException iae) {
				continue;
			}
		}
		return Integer.toString(value);
	}
	
	/**
     * Convenience method to shorten subclass file length
     */
	protected final void fail() {
		throw new PDOMNotImplementedError("in " + getClass().getCanonicalName()); //$NON-NLS-1$
	}
	
	public PDOMName getScopeName() {
		try {
			PDOMName name = getFirstDefinition();
			if (name == null)
				name = getFirstDeclaration();
			return name;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	final public String[] getQualifiedName() {
		List<String> result = new ArrayList<String>();
		try {
			PDOMNode node = this;
			while (node != null) {
				if (node instanceof PDOMBinding && !(node instanceof ICPPTemplateInstance)) {							
					result.add(0, ((PDOMBinding)node).getName());
				}
				node = node.getParentNode();
			}
			return result.toArray(new String[result.size()]);
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}
	
	final public boolean isFileLocal() throws CoreException {
		return pdom.getDB().getInt(record + LOCAL_TO_FILE) != 0;
	}

	public boolean hasDefinition() throws CoreException {
		return getFirstDefinition() != null;
	}

	/**
	 * Compares two binding fully qualified names. If b0 has
     * less segments than b1 then -1 is returned, if b0 has 
     * more segments than b1 then 1 is returned. If the segment
     * lengths are equal then comparison is lexicographical on each
     * component name, beginning with the most nested name and working
     * outward. 
     * If one of the bindings in the hierarchy is file-local it is treated as a different
     * binding.
     * The first non-zero comparison is returned as the result.
	 * @param b0
	 * @param b1
	 * @return <ul><li> -1 if b0 &lt; b1
	 * <li> 0 if b0 == b1
	 * <li> 1 if b0 &gt; b1
	 * </ul>
	 * @throws CoreException
	 */
	private static int comparePDOMBindingQNs(PDOMBinding b0, PDOMBinding b1) {
		try {
			int cmp = 0; 
			do {
				IString s0 = b0.getDBName(), s1 = b1.getDBName();
				cmp = s0.compare(s1, true);
				if (cmp == 0) {
					int l1= b0.getLocalToFileRec();
					int l2= b1.getLocalToFileRec();
					if (l1 != l2) {
						return l1 < l2 ? -1 : 1;
					}
					b0 = (PDOMBinding) b0.getParentBinding();
					b1 = (PDOMBinding) b1.getParentBinding();
					if (b0 == null || b1 == null) {
						cmp = b0 == b1 ? 0 : (b0 == null ? -1 : 1);
					}
				}
			} while(cmp == 0 && b1 != null && b0 != null);
			return cmp;
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
			return -1;
		}
	}

	/**
	 * Compares two PDOMBinding objects in accordance with 
	 * {@link IIndexFragmentBindingComparator#compare(IIndexFragmentBinding, IIndexFragmentBinding)}
	 * @param other
	 * @return comparison result, -1, 0, or 1.
	 */
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = comparePDOMBindingQNs(this, other);
		if (cmp == 0) {
			int t1 = getNodeType();
			int t2 = other.getNodeType();
			return t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
		}
		return cmp;
	}
	
	/**
     * Returns whether pdomCompareTo returns zero
     */
	public final boolean pdomEquals(PDOMBinding other) {
		return pdomCompareTo(other)==0;
	}
	
	public final int getBindingConstant() {
		return getNodeType();
	}

	/**
	 * The binding is reused by a declaration or definition, we may need to update modifiers.
	 * @throws CoreException 
	 */
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
	}
	
	@Override
	final public void delete(PDOMLinkage linkage) throws CoreException {
		assert false;
	}

	/**
	 * Bindings may set additional flags for their occurrences
	 * Return a combination of flags defined in {@link PDOMName}.
	 * @since 5.0
	 */
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		return 0;
	}
	
	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, null);
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) throws DOMException {
		return null;
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, null);
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix, IIndexFileSet fileSet)
			throws DOMException {
		return null;
	}
}