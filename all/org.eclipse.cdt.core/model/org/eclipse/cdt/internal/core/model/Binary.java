package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IBinary;

public class Binary extends CFile implements IBinary {

	public Binary(ICElement parent, IFile file) {
		super(parent, file);
	}

	public Binary(ICElement parent, IPath path) {
		super (parent, path);
	}

	public Binary(ICElement parent, IFile file, String name) {
		super(parent, file, name);
	}


	public boolean isReadOnly () {
		return true;
	}

	public boolean isBinary() {
		return true;
	}

	public boolean hasDebug () {
		return ((BinaryInfo)getElementInfo()).hasDebug();
	}

	public boolean isExecutable() {
		return ((BinaryInfo)getElementInfo()).isExecutable();
	}

	public boolean isObject() {
		return ((BinaryInfo)getElementInfo()).isObject();
	}

	public boolean isSharedLib() {
		return ((BinaryInfo)getElementInfo()).isSharedLib();
	}

	public String [] getNeededSharedLibs() {
		return ((BinaryInfo)getElementInfo()).getNeededSharedLibs();
	}

	public String getCPU() {
		return ((BinaryInfo)getElementInfo()).getCPU();
	}

	public long getText() {
		return ((BinaryInfo)getElementInfo()).getText();
	}

	public long getData() {
		return ((BinaryInfo)getElementInfo()).getData();
	}

	public long getBSS() {
		return ((BinaryInfo)getElementInfo()).getBSS();
	}

	public String getSoname() {
		return ((BinaryInfo)getElementInfo()).getSoname();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinary#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		return  ((BinaryInfo)getElementInfo()).isLittleEndian();
	}

	public CElementInfo createElementInfo() {
		return new BinaryInfo(this);
	}

}
