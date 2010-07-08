/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * For marshalling types to byte arrays.
 */
public class TypeMarshalBuffer implements ITypeMarshalBuffer {
	public final static byte [] EMPTY= {0,0,0,0,0,0};
	public final static byte NULL_TYPE= 0;
	public final static byte INDIRECT_TYPE= (byte) -1;
	public final static byte BINDING_TYPE= (byte) -2;
	
	static {
		assert EMPTY.length == Database.TYPE_SIZE;
	}

	private final PDOMLinkage fLinkage;
	private int fPos;
	private byte[] fBuffer;

	/**
	 * Constructor for output buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage) {
		fLinkage= linkage;
	}

	/**
	 * Constructor for input buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage, byte[] data) {
		fLinkage= linkage;
		fBuffer= data;
	}

	public int getPosition() {
		return fPos;
	}

	public byte[] getBuffer() {
		return fBuffer;
	}

	public void marshalType(IType type) throws CoreException {
		if (type instanceof IBinding) {
			PDOMBinding pb= fLinkage.addTypeBinding((IBinding) type);
			if (pb == null) {
				putByte(NULL_TYPE);
			} else {
				putByte(BINDING_TYPE);
				putByte((byte) 0);
				putRecordPointer(pb.getRecord());
			}
		} else if (type instanceof ISerializableType) {
			((ISerializableType) type).marshal(this);
		} else {
			assert type == null : "Cannot serialize " + ASTTypeUtil.getType(type) + "(" + type.getClass().getName() + ")";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			putByte(NULL_TYPE);
		}
	}

	private void request(int i) {
		if (fBuffer == null) {
			if (i <= Database.TYPE_SIZE) {
				fBuffer= new byte[Database.TYPE_SIZE];
			} else {
				fBuffer= new byte[i];
			}
		} else {
			final int bufLen = fBuffer.length;
			int needLen = fPos + i;
			if (needLen > bufLen) {
				needLen= Math.max(needLen, 2 * bufLen);
				byte[] newBuffer= new byte[needLen];
				System.arraycopy(fBuffer, 0, newBuffer, 0, fPos);
				fBuffer= newBuffer;
			}
		}
	}

	public void putByte(byte b) {
		request(1);
		fBuffer[fPos++]= b;
	}

	public int getByte() throws CoreException {
		if (fPos+1 > fBuffer.length)
			throw unmarshallingError();
		return 0xff & fBuffer[fPos++];
	}

	public CoreException unmarshallingError() {
		return new CoreException(CCorePlugin.createStatus("Unmarshalling error")); //$NON-NLS-1$
	}
	public CoreException marshallingError() {
		return new CoreException(CCorePlugin.createStatus("Marshalling error")); //$NON-NLS-1$
	}

	public void putShort(short value) {
		request(2);
		fBuffer[fPos++]= (byte)(value >> 8);
		fBuffer[fPos++]= (byte)(value);
	}

	public int getShort() throws CoreException {
		if (fPos+2 > fBuffer.length)
			throw unmarshallingError();
		final int byte1 = 0xff & fBuffer[fPos++];
		final int byte2 = 0xff & fBuffer[fPos++];
		return (((byte1 << 8) | (byte2 & 0xff)));
	}

	private void putRecordPointer(long record) {
		request(Database.PTR_SIZE);
		Chunk.putRecPtr(record, fBuffer, fPos);
		fPos+= Database.PTR_SIZE;
	}

	private long getRecordPointer() throws CoreException {
		final int pos= fPos;
		fPos += Database.PTR_SIZE;
		if (fPos > fBuffer.length) {
			fPos= fBuffer.length;
			throw unmarshallingError();
		}
		return Chunk.getRecPtr(fBuffer, pos);
	}

	public IValue getValue() throws CoreException {
		int replen= getShort();
		int unknwonLen= getShort();
		
		char[] rep= new char[replen];
		for (int i = 0; i < replen; i++) {
			rep[i]= (char) getShort();
		}
		ICPPUnknownBinding[] unknown= new ICPPUnknownBinding[unknwonLen];
		for (int i = 0; i < unknwonLen; i++) {
			long ptr= getRecordPointer();
			IBinding b= fLinkage.getBinding(ptr);
			if (b instanceof ICPPUnknownBinding) {
				unknown[i]= (ICPPUnknownBinding) b;
			}
		}
		return Value.fromInternalRepresentation(rep, unknown);
	}

	public void putValue(IValue value) throws CoreException {
		char[] rep= value.getInternalExpression();
		IBinding[] unknown= value.getUnknownBindings();
		putShort((short) rep.length);
		putShort((short) unknown.length);

		int len= rep.length & 0xffff;
		for (int i = 0; i < len; i++) {
			putShort((short) rep[i]);
		}
		len= unknown.length & 0xffff;
		for (int i = 0; i < len; i++) {
			PDOMBinding uv = fLinkage.addUnknownValue(unknown[i]);
			putRecordPointer(uv != null ? uv.getRecord() : 0);
		}
	}

	public IType unmarshalType() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();
		
		byte firstByte= fBuffer[fPos];
		if (firstByte == BINDING_TYPE) {
			fPos+= 2;
			long rec= getRecordPointer();
			return (IType) fLinkage.getNode(rec);
		} else if (firstByte == 0) {
			fPos++;
			return null;
		}
		
		return fLinkage.unmarshalType(this);
	}
}
