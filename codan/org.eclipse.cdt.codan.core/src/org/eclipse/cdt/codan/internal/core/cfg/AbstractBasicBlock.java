package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;

public abstract class AbstractBasicBlock implements IBasicBlock {
	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	static class OneElementIterator<V> implements Iterator<V> {
		private V o;

		public OneElementIterator(V o) {
			this.o = o;
		}

		public boolean hasNext() {
			return o != null;
		}

		public V next() {
			V x = o;
			o = null;
			return x;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public abstract void addOutgoing(IBasicBlock node);

	/**
	 * @return
	 */
	public String toStringData() {
		if (getData() == null)
			return "";
		return getData().toString();
	}
}
