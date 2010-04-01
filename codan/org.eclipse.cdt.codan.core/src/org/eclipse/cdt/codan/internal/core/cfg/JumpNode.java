package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IJumpNode;

/**
 * Jump node is node that connects unusual control pass, such as goto, break and
 * continue
 * 
 */
public class JumpNode extends AbstractSingleIncomingNode implements IJumpNode {
	private IConnectorNode jump;
	private boolean backward;

	public JumpNode() {
		super();
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(jump);
	}

	public int getOutgoingSize() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.IJumpNode#getJumpNode()
	 */
	public IConnectorNode getJumpNode() {
		return jump;
	}

	public IBasicBlock getOutgoing() {
		return jump;
	}

	public boolean isBackwardArc() {
		return backward;
	}

	public void setJump(IConnectorNode jump, boolean backward) {
		if (this.jump != null)
			throw new IllegalArgumentException(
					"Cannot modify exiting connector"); //$NON-NLS-1$
		this.jump = jump;
		this.backward = backward;
	}

	public void setBackward(boolean backward) {
		this.backward = backward;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		if (node == jump)
			return;
		throw new UnsupportedOperationException();
	}
}
