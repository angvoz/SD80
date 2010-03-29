package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Collections;
import java.util.Iterator;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IStartNode;

/**
 * Start node has no incoming, one outgoing and it is connect to function exits
 * 
 */
public class StartNode extends AbstractSingleOutgoingNode implements IStartNode {
	public StartNode(IBasicBlock next) {
		super(next);
	}

	@SuppressWarnings("unchecked")
	public Iterator<IBasicBlock> getIncomingIterator() {
		return Collections.EMPTY_LIST.iterator();
	}

	public int getIncomingSize() {
		return 0;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}
}
