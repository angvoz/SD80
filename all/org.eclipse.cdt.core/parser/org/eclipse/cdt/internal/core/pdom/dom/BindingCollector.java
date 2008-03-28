/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;

/**
 * Visitor to find bindings in a BTree or below a PDOMNode. Nested bindings are not visited.
 * @since 4.0
 */
public final class BindingCollector extends NamedNodeCollector {
	private IndexFilter filter;

	/**
	 * Collects all bindings with given name.
	 */
	public BindingCollector(PDOMLinkage linkage, char[] name) {
		this(linkage, name, null, false, true);
	}
		
	/**
	 * Collects all bindings with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 */
	public BindingCollector(PDOMLinkage linkage, char[] name, IndexFilter filter, boolean prefixLookup, boolean caseSensitive) {
		super(linkage, name, prefixLookup, caseSensitive);
		this.filter= filter;
	}
		
	@Override
	public boolean addNode(PDOMNamedNode tBinding) throws CoreException {
		if (tBinding instanceof PDOMBinding) {
			if (filter == null || filter.acceptBinding((IBinding) tBinding)) {
				return super.addNode(tBinding);
			}
		}
		return true; // look for more
	}
	
	public PDOMBinding[] getBindings() {
		List<PDOMNamedNode> bindings= getNodeList();
		return bindings.toArray(new PDOMBinding[bindings.size()]);
	}
}
