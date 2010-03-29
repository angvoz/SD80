package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.ISingleOutgoing;

/**
 * Abstract impl of basic block with single outgoing arc
 * 
 */
public abstract class AbstractSingleOutgoingNode extends AbstractBasicBlock
		implements ISingleOutgoing {
	private IBasicBlock next;

	public AbstractSingleOutgoingNode(IBasicBlock next) {
		super();
		this.next = next;
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(next);
	}

	public int getOutgoingSize() {
		return 1;
	}

	public IBasicBlock getOutgoing() {
		return next;
	}

	public void setOutgoing(IBasicBlock exit) {
		if (this.next != null)
			throw new IllegalArgumentException(
					"Cannot modify already exiting connector"); //$NON-NLS-1$
		this.next = exit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock#addOutgoing
	 * (org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock)
	 */
	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}
}
