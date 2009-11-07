/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;

public abstract class Scope implements IScope {

	protected String name;
	protected IAddress lowAddress;
	protected IAddress highAddress;
	protected IScope parent;
	protected List<IScope> children = new ArrayList<IScope>();
	protected List<IVariable> variables = new ArrayList<IVariable>();
	protected List<IEnumerator> enumerators = new ArrayList<IEnumerator>();
	protected boolean childrenSorted = false;

	public Scope(String name, IAddress lowAddress, IAddress highAddress, IScope parent) {
		this.name = name;
		this.lowAddress = lowAddress;
		this.highAddress = highAddress;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public IAddress getLowAddress() {
		return lowAddress;
	}

	public IAddress getHighAddress() {
		return highAddress;
	}

	public IScope getParent() {
		return parent;
	}

	public Collection<IScope> getChildren() {
		return Collections.unmodifiableCollection(children);
	}

	public Collection<IVariable> getVariables() {
		return Collections.unmodifiableCollection(variables);
	}

	public Collection<IEnumerator> getEnumerators() {
		return Collections.unmodifiableCollection(enumerators);
	}

	public IScope getScopeAtAddress(IAddress linkAddress) {
		if (!childrenSorted) {
			Collections.sort(children);
			childrenSorted = true;
		}

		// see if it's in this scope
		if (linkAddress.compareTo(lowAddress) >= 0 && linkAddress.compareTo(highAddress) < 0) {
			int insertion = Collections.binarySearch(children, linkAddress);
			if (insertion >= 0) {
				return children.get(insertion).getScopeAtAddress(linkAddress);
			}

			if (insertion != -1) {
				insertion = -insertion - 1;

				IScope child = children.get(insertion - 1);
				if (linkAddress.compareTo(child.getHighAddress()) < 0) {
					return child.getScopeAtAddress(linkAddress);
				}
			}

			// not found in a sub scope so return this scope
			return this;
		}

		return null;
	}

	/**
	 * Adds the given scope as a child of this scope
	 * 
	 * @param scope
	 */
	public void addChild(IScope scope) {
		children.add(scope);
	}

	/**
	 * Adds the given variable to this scope
	 * 
	 * @param variable
	 */
	public void addVariable(IVariable variable) {
		variables.add(variable);
	}

	/**
	 * Adds the given variable to this scope
	 * 
	 * @param variable
	 */
	public void addEnumerator(IEnumerator enumerator) {
		enumerators.add(enumerator);
	}

	public int compareTo(Object o) {
		if (o instanceof IScope) {
			return lowAddress.compareTo(((IScope) o).getLowAddress());
		} else if (o instanceof IAddress) {
			return lowAddress.compareTo(o);
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Scope ["); //$NON-NLS-1$
		builder.append("lowAddress="); //$NON-NLS-1$
		builder.append(lowAddress.toHexAddressString());
		builder.append(", highAddress="); //$NON-NLS-1$
		builder.append(highAddress.toHexAddressString());
		builder.append(", "); //$NON-NLS-1$
		if (name != null) {
			builder.append("name="); //$NON-NLS-1$
			builder.append(name);
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
