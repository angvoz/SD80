/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 */
public class OperatorCast extends CompoundInstruction {

	private final IASTCastExpression castExpr;
	private IType castType;

	public OperatorCast(int start, IASTCastExpression castExpr) {
		super(start);
		this.castExpr = castExpr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		OperandValue value = fInterpreter.pop();
		if (value.getStringValue() != null)
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorCast_CannotCastString);
		
		castType = getCastType(fInterpreter.getTypeEngine());
		
		IVariableLocation location = value.getValueLocation();
		
		OperandValue castValue = new OperandValue(castType);
		Number origValue = value.getValue();
		Number castedValue = origValue;
		
		if (castType instanceof ICPPBasicType) {
			ICPPBasicType cppType = (ICPPBasicType) castType;
			// when casting to primtive integral type, reduce or zero/sign extend
			if (cppType.getBaseType() == ICPPBasicType.t_char || 
					cppType.getBaseType() == ICPPBasicType.t_int ||
					cppType.getBaseType() == ICPPBasicType.t_wchar_t ||
					cppType.getBaseType() == ICPPBasicType.t_bool) {
				switch (cppType.getByteSize()) {
				case 1:
					if (cppType.isSigned())
						castedValue = origValue.byteValue();
					else
						castedValue = origValue.byteValue() & 0xff;
					break;
				case 2:
					if (cppType.isSigned())
						castedValue = origValue.shortValue();
					else
						castedValue = origValue.shortValue() & 0xfffff;
					break;
				case 4:
					if (cppType.isSigned())
						castedValue = origValue.intValue();
					else
						castedValue = origValue.intValue() & 0xffffffffL;
					break;
				case 8:
					if (cppType.isSigned())
						castedValue = BigInteger.valueOf(origValue.longValue());
					else
						castedValue = BigInteger.valueOf(origValue.longValue()).and(Mask8Bytes);
					break;
				}
			} else if (cppType.getBaseType() == ICPPBasicType.t_float ||
					cppType.getBaseType() == ICPPBasicType.t_double) {
				// and be sure integers promoted to floats, if needed
				switch (cppType.getByteSize()) {
				case 4:
					castedValue = Float.valueOf(origValue.longValue());
					break;
				case 8:
				case 12:
					castedValue = Double.valueOf(origValue.longValue());
					break;
				}
			}
		}
		
		castValue.setValue(castedValue);
		castValue.setValueLocation(location);
		
		fInterpreter.push(castValue);
	}

	/**
	 * @return 
	 * @throws CoreException
	 */
	public IType getCastType(TypeEngine typeEngine) throws CoreException {
		if (castType == null) {
			IASTTypeId typeId = castExpr.getTypeId();
			castType = typeEngine.getTypeForTypeId(typeId);
		}
		return castType;
	}

	public IASTCastExpression getCastExpr() {
		return castExpr;
	}
}
