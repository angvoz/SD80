/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Look up bindings in BTree objects and IPDOMNode objects
 */
public class FindBinding {
	public static class DefaultBindingBTreeComparator implements IBTreeComparator {
		protected PDOM pdom;
		public DefaultBindingBTreeComparator(PDOM pdom) {
			this.pdom = pdom;
		}
		public int compare(int record1, int record2) throws CoreException {
			IString nm1 = PDOMNamedNode.getDBName(pdom, record1);
			IString nm2 = PDOMNamedNode.getDBName(pdom, record2);
			return nm1.compare(nm2);
		}
	}

	public static PDOMBinding findBinding(BTree btree, final PDOM pdom, final char[]name, final int[] constants) throws CoreException {
		final PDOMBinding[] result = new PDOMBinding[1];
		btree.accept(new IBTreeVisitor() {
			public int compare(int record) throws CoreException {
				IString nm1 = PDOMNamedNode.getDBName(pdom, record);
				return nm1.compare(name);
			}
			public boolean visit(int record) throws CoreException {
				result[0] = pdom.getBinding(record);
				return false;
			}
		});
		return result[0];
	}

	public static PDOMBinding findBinding(IPDOMNode node, final PDOM pdom, final char[]name, final int[] constants) {
		final PDOMBinding[] result = new PDOMBinding[1];
		try {
			node.accept(new IPDOMVisitor() {
				public boolean visit(IPDOMNode node) throws CoreException {
					if(node instanceof PDOMNamedNode) {
						PDOMNamedNode nnode = (PDOMNamedNode) node;
						if(nnode.hasName(name)) {
							int constant = nnode.getNodeType();
							for(int i=0; i<constants.length; i++) {
								if(constant==constants[i]) {
									result[0] = (PDOMBinding) node;
									throw new CoreException(Status.OK_STATUS);
								}
							}
						}
					}
					return false; /* do not visit children of node */
				}
				public void leave(IPDOMNode node) throws CoreException {}					
			});
		} catch(CoreException ce) {
			if(ce.getStatus().getCode()==IStatus.OK) {
				return result[0];
			} else {
				CCorePlugin.log(ce);
			}
		}
		return null;
	}
}

