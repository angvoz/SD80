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
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.RangeList;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;

public abstract class Scope implements IScope {

	protected String name;
	protected IAddress lowAddress;
	protected IAddress highAddress;
	protected IScope parent;
	protected List<IScope> children = new ArrayList<IScope>();
	protected List<IVariable> variables = new ArrayList<IVariable>();
	protected List<IEnumerator> enumerators = new ArrayList<IEnumerator>();
	protected boolean childrenSorted = false;
	protected IRangeList rangeList;

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

	/**
	 * Tell whether the address range for the scope is empty.
	 * @return flag
	 */
	public boolean hasEmptyRange() {
		return (lowAddress == null || highAddress == null)
		|| (lowAddress.isZero() && highAddress.isZero())
		|| (lowAddress.getValue().longValue() == -1 && highAddress.isZero()); // TODO: remove this case
	}

	/**
	 * Return the list of non-contiguous ranges for this scope.
	 * @return list or <code>null</code>
	 */
	public IRangeList getRangeList() {
		return rangeList;
	}

	public void setLowAddress(IAddress lowAddress) {
		this.lowAddress = lowAddress;
	}

	public void setHighAddress(IAddress highAddress) {
		this.highAddress = highAddress;
	}

	public void setRangeList(IRangeList ranges) {
		this.rangeList = ranges;
		setLowAddress(new Addr32(rangeList.getLowAddress()));
		setHighAddress(new Addr32(rangeList.getHighAddress()));
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

	static final Comparator<IScope> sortByAddress = new Comparator<IScope>() {

		public int compare(IScope o1, IScope o2) {
			return o1.getLowAddress().compareTo(o2.getLowAddress());
		}
	};
	
	public IScope getScopeAtAddress(IAddress linkAddress) {
		if (!childrenSorted) {
			// Comparable<> is not defined to work the way we want; be explicit with the sorter
			Collections.sort(children, sortByAddress);
			childrenSorted = true;
		}

		// see if it's in this scope
		if (linkAddress.compareTo(lowAddress) >= 0 && linkAddress.compareTo(highAddress) < 0) {
			int insertion = Collections.binarySearch(children, linkAddress);
			if (insertion >= 0) {
				IScope s = children.get(insertion).getScopeAtAddress(linkAddress);
				if (s != null)	// found in subscopes
					return s;
				// Not found in subscope.
				// One case I've seen: with RVCT 2.2 compiled Symbian GUI template project,
				// some lexical blocks has highAddress smaller than lowAddress, resulting in
				// no lexical block found for the address.
				// In such case, we just go on return this scope.
			}
			else if (insertion != -1) {
				insertion = -insertion - 1;

				IScope child = children.get(insertion - 1);
				if (linkAddress.compareTo(child.getHighAddress()) < 0) {
					return child.getScopeAtAddress(linkAddress);
				}
			}
			
			// No matches? This could be due to the fact that ranges are not
			// guaranteed to be strictly non-overlapping (as far as low and high
			// ranges go) -- especially if a child uses a range list.
			// Our binary search above may have found a candidate which happens
			// to contain the range, but other children may *also* contain the
			// range.
			// Try harder.
			// TODO: make a unified TreeMap<Long, IScope> to avoid fallback behavior
			for (IScope child : children) {
				if (linkAddress.compareTo(child.getLowAddress()) >= 0 
						&& linkAddress.compareTo(child.getHighAddress()) < 0) {
					IRangeList childRange = child.getRangeList();
					if (childRange != null) {
						if (childRange.isInRange(linkAddress.getValue().longValue())) {
							IScope candidate = child.getScopeAtAddress(linkAddress);
							if (candidate != null)
								return candidate;
						}
					} else {
						// may yet be between the low and high addresses
						IScope candidate = child.getScopeAtAddress(linkAddress);
						if (candidate != null)
							return candidate;
					}
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
		childrenSorted = false;
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
		if (rangeList != null) {
			builder.append("ranges="); //$NON-NLS-1$
			builder.append(rangeList);
		} else {
			builder.append("lowAddress="); //$NON-NLS-1$
			builder.append(lowAddress.toHexAddressString());
			builder.append(", highAddress="); //$NON-NLS-1$
			builder.append(highAddress.toHexAddressString());
		}
		builder.append(", "); //$NON-NLS-1$
		if (name != null) {
			builder.append("name="); //$NON-NLS-1$
			builder.append(name);
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	public void fixupRanges(IAddress baseAddress) {
		// compile unit scopes not generated by the compiler so
		// figure it out from the functions
		IAddress newLowAddress = Addr64.MAX;
		IAddress newHighAddress = Addr64.ZERO;
		boolean any = false;
		
		for (IScope kid : getChildren()) {
			// the compiler may generate (bad) low/high pc's which are not
			// in the actual module space for some functions. to work
			// around this, only honor addresses that are above the
			// actual link address
			if (kid.hasEmptyRange()) {
				continue;
			}
			
			if (kid.getLowAddress().compareTo(baseAddress) > 0) {
				if (kid.getLowAddress().compareTo(newLowAddress) < 0) {
					newLowAddress = kid.getLowAddress();
					any = true;
				}

				if (kid.getHighAddress().compareTo(newHighAddress) > 0) {
					newHighAddress = kid.getHighAddress();
					any = true;
				}
			}
		}

		if (any) {
			//System.out.println("Needed to fix up ranges for " + getName());
			lowAddress = newLowAddress; 
			highAddress = newHighAddress;
			rangeList = null;
		} else {
			if (lowAddress == null) {
				lowAddress = highAddress = Addr32.ZERO;
			}
		}
	}
	
	/**
	 * Merge the code range(s) from the given scope into this one.
	 * @param scope
	 */
	protected void mergeScopeRange(IScope scope) {
		if (hasEmptyRange()) {
			// copy range
			if (scope.getRangeList() != null) {
				setRangeList(scope.getRangeList());
			} else {
				setLowAddress(scope.getLowAddress());
				setHighAddress(scope.getHighAddress());
			}
		} else {
			if (scope.getLowAddress() != null && scope.getLowAddress().compareTo(lowAddress) < 0
					&& !scope.getLowAddress().isZero()) 		// ignore random 0 entries 
			{
				if (rangeList != null) {
					if (scope.getRangeList() != null) {
						// TODO: merge properly
						rangeList = null;
					} else {
						((RangeList)rangeList).addLowRange(scope.getLowAddress().getValue().longValue());
					}
				}
				lowAddress = scope.getLowAddress();
			}
			if (scope.getHighAddress() != null && scope.getHighAddress().compareTo(highAddress) > 0) {
				if (rangeList != null) {
					if (scope.getRangeList() != null) {
						// TODO: merge properly
						rangeList = null;
					} else {
						((RangeList)rangeList).addHighRange(scope.getHighAddress().getValue().longValue());
					}
				}
				highAddress = scope.getHighAddress();
			}	
		}
	}
	
	protected void addLineInfoToParent(IScope scope) {
		IScope cu = parent;
		while (cu != null) {
			if (cu instanceof ICompileUnitScope && cu.getParent() instanceof IModuleScope) {
				IModuleScope module = (IModuleScope) cu.getParent();
				ModuleLineEntryProvider provider = (ModuleLineEntryProvider) module.getModuleLineEntryProvider();
				provider.addCompileUnitChild((ICompileUnitScope) cu, scope);
				break;
			}
			cu = cu.getParent();
		}
	}
}
