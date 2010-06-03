/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
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
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.RangeList;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IRangeList;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.debug.edc.symbols.IRangeList.Entry;
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
	private TreeMap<IRangeList.Entry, IScope> addressToScopeMap;
	
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

	public IScope getScopeAtAddress(IAddress linkAddress) {
		// see if it's in this scope
		if (linkAddress.compareTo(lowAddress) >= 0 && linkAddress.compareTo(highAddress) < 0) {
			
			ensureScopeRangeLookup();
			
			long addr = linkAddress.getValue().longValue();
			IRangeList.Entry addressEntry = new IRangeList.Entry(addr, addr);
			SortedMap<Entry,IScope> tailMap = addressToScopeMap.tailMap(addressEntry);
			
			if (tailMap.isEmpty())
				return this;
			
			IScope child = tailMap.values().iterator().next();
			if (linkAddress.compareTo(child.getLowAddress()) >= 0 
					&& linkAddress.compareTo(child.getHighAddress()) < 0) {
				return child.getScopeAtAddress(linkAddress);
			}
			
			return this;
		}

		return null;
	}

	/**
	 * Make sure our mapping of address range to scope is valid. 
	 */
	private void ensureScopeRangeLookup() {
		if (addressToScopeMap == null) {
			addressToScopeMap = new TreeMap<Entry, IScope>();
			
			for (IScope scope : children) {
				addScopeRange(scope);
			}
			//System.out.println("Mapping for " + getName()+ ": "+ addressToScopeMap.size() + " entries");
		}
	}

	/**
	 * @param scope
	 */
	private void addScopeRange(IScope scope) {
		IRangeList ranges = scope.getRangeList();
		if (ranges != null) {
			for (IRangeList.Entry entry : ranges) {
				addressToScopeMap.put(entry, scope);
			}
		} else {
			addressToScopeMap.put(new IRangeList.Entry(
						scope.getLowAddress().getValue().longValue(), 
						scope.getHighAddress().getValue().longValue()),
					scope);
		}
	}

	/**
	 * Adds the given scope as a child of this scope
	 * 
	 * @param scope
	 */
	public void addChild(IScope scope) {
		children.add(scope);
		if (addressToScopeMap != null) {
			addScopeRange(scope);
		}
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

	/**
	 * 
	 */
	public void dispose() {
		for (IScope scope : children)
			scope.dispose();
		children.clear();
		for (IVariable var : variables)
			var.dispose();
		variables.clear();
		enumerators.clear();
		if (addressToScopeMap != null)
			addressToScopeMap.clear();
		rangeList = null;
	}
}
