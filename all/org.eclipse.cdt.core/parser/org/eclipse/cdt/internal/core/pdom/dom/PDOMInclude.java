/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMInclude implements IIndexFragmentInclude {

	private final PDOM pdom;
	private final int record;
	
	private final int INCLUDES = 0;
	private final int INCLUDED_BY = 4;
	private final int INCLUDES_NEXT = 8;
	private final int INCLUDED_BY_NEXT = 12;
	private final int INCLUDED_BY_PREV = 16;
	private static final int NODE_OFFSET_OFFSET  = 20;
	private static final int NODE_LENGTH_OFFSET  = 24;
	private static final int FLAG_OFFSET 				 = 26;

	private static final int FLAG_SYSTEM_INCLUDE = 1;
	
	private final int RECORD_SIZE = 27;

	public PDOMInclude(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMInclude(PDOM pdom, IASTPreprocessorIncludeStatement include) throws CoreException {
		this.pdom = pdom;
		this.record = pdom.getDB().malloc(RECORD_SIZE);
		IASTName name= include.getName();
		IASTFileLocation loc= name.getFileLocation();
		setNameOffsetAndLength(loc.getNodeOffset(), (short) loc.getNodeLength());
		setFlag(encodeFlags(include));
	}
	
	private byte encodeFlags(IASTPreprocessorIncludeStatement include) {
		byte flags= 0;
		if (include.isSystemInclude()) {
			flags |= FLAG_SYSTEM_INCLUDE;
		}
		return flags;
	}

	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		// Remove us from the includedBy chain
		PDOMInclude prevInclude = getPrevInIncludedBy();
		PDOMInclude nextInclude = getNextInIncludedBy();
		if (prevInclude != null)
			prevInclude.setNextInIncludedBy(nextInclude);
		else
			((PDOMFile) getIncludes()).setFirstIncludedBy(nextInclude);
		
		if (nextInclude != null)
			nextInclude.setPrevInIncludedBy(prevInclude);
		
		// Delete our record
		pdom.getDB().free(record);
	}
	
	public IIndexFragmentFile getIncludes() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDES);
		return rec != 0 ? new PDOMFile(pdom, rec) : null;
	}
	
	public void setIncludes(PDOMFile includes) throws CoreException {
		int rec = includes != null ? includes.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDES, rec);
	}
	
	public IIndexFile getIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY);
		return rec != 0 ? new PDOMFile(pdom, rec) : null;
	}
	
	public void setIncludedBy(PDOMFile includedBy) throws CoreException {
		int rec = includedBy != null ? includedBy.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDED_BY, rec);
	}
	
	public PDOMInclude getNextInIncludes() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDES_NEXT);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setNextInIncludes(PDOMInclude include) throws CoreException {
		int rec = include != null ? include.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDES_NEXT, rec);
	}
	
	public PDOMInclude getNextInIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY_NEXT);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setNextInIncludedBy(PDOMInclude include) throws CoreException {
		int rec = include != null ? include.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDED_BY_NEXT, rec);
	}
	
	public PDOMInclude getPrevInIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY_PREV);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setPrevInIncludedBy(PDOMInclude include) throws CoreException {
		int rec = include != null ? include.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDED_BY_PREV, rec);
	}

	public String getIncludedByLocation() throws CoreException {
		return getIncludedBy().getLocation();
	}

	public String getIncludesLocation() throws CoreException {
		return getIncludes().getLocation();
	}

	public IIndexFragment getFragment() {
		return pdom;
	}
	
	private void setNameOffsetAndLength(int offset, short length) throws CoreException {
		pdom.getDB().putInt(record + NODE_OFFSET_OFFSET, offset);
		pdom.getDB().putShort(record + NODE_LENGTH_OFFSET, length);
	}
	
	private void setFlag(byte flag) throws CoreException {
		pdom.getDB().putByte(record + FLAG_OFFSET, flag);
	}
	
	private int getFlag() throws CoreException {
		return pdom.getDB().getByte(record + FLAG_OFFSET);
	}

	public boolean isSystemInclude() throws CoreException {
		return (getFlag() & FLAG_SYSTEM_INCLUDE) != 0;
	}
	
	public int getNameOffset() throws CoreException {
		return pdom.getDB().getInt(record + NODE_OFFSET_OFFSET);
	}
	
	public int getNameLength() throws CoreException {
		return pdom.getDB().getShort(record + NODE_LENGTH_OFFSET) & 0xffff;
	}			
}
