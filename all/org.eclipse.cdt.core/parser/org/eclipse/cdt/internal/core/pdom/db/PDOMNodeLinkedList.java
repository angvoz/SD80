/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a linked list
 * @author Doug Schaefer
 *
 */
public class PDOMNodeLinkedList {
	PDOM pdom;
	int offset;
	PDOMLinkage linkage;
	
	private static final int FIRST_MEMBER = 0;
	protected static final int RECORD_SIZE = 4;

	public PDOMNodeLinkedList(PDOM pdom, int offset, PDOMLinkage linkage) {
		this.pdom = pdom;
		this.offset = offset;
		this.linkage = linkage;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		ListItem firstItem = getFirstMemberItem();
		if (firstItem == null)
			return;
		
		ListItem item = firstItem;
		do {
			PDOMNode node = linkage.getNode(item.getItem());
			if (visitor.visit(node))
				node.accept(visitor);
			visitor.leave(node);
			item = item.getNext();
		} while (!item.equals(firstItem));
	}
	
	private ListItem getFirstMemberItem() throws CoreException {
		Database db = pdom.getDB();
		int item = db.getInt(offset + FIRST_MEMBER);
		return item != 0 ? new ListItem(db, item) : null;
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		Database db = pdom.getDB();
		ListItem firstMember = getFirstMemberItem();
		if (firstMember == null) {
			firstMember = new ListItem(db);
			firstMember.setItem(member.getRecord());
			firstMember.setNext(firstMember);
			firstMember.setPrev(firstMember);
			db.putInt(offset + FIRST_MEMBER, firstMember.getRecord());
		} else {
			ListItem newMember = new ListItem(db);
			newMember.setItem(member.getRecord());
			ListItem prevMember = firstMember.getPrev();
			prevMember.setNext(newMember);
			firstMember.setPrev(newMember);
			newMember.setPrev(prevMember);
			newMember.setNext(firstMember);
		}
	}
}
