/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryElement;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryModule extends Parent implements IBinaryModule {

	IPath path;

	public BinaryModule(Binary parent, IPath p) {
		super(parent, p.lastSegment(), ICElement.C_VCONTAINER);
		path = p;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryModule#getBinaryElements()
	 */
	public IBinaryElement[] getBinaryElements() throws CModelException {
		ICElement[] e = getChildren();
		IBinaryElement[] b = new IBinaryElement[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getAddress()
	 */
	public long getAddress() throws CModelException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getBinary()
	 */
	public IBinary getBinary() {
		return (IBinary)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	protected CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return path;
	}

}
