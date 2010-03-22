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
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
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
		
		if (castType == null) {
			IASTTypeId typeId = castExpr.getTypeId();
			castType = fInterpreter.getTypeEngine().getTypeForTypeId(typeId);
		}
		
		IVariableLocation location = value.getValueLocation();
		
		OperandValue castValue = new OperandValue(castType);
		Number origValue = value.getValue();
		Number castedValue = origValue;
		
		// when casting to primtive type, reduce or zero/sign extend
		if (castType instanceof ICPPBasicType) {
			ICPPBasicType cppType = (ICPPBasicType) castType;
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
		}
		
		castValue.setValue(castedValue);
		castValue.setValueLocation(location);
		
		/*
		// The "view as array" support supports an array cast.
		// We need to dereference the pointer first.
		// e.g.  char* str = "foo";  --> (char[])(str
		IType origType = TypeUtils.getStrippedType(value.getValueType());
		if (origType instanceof IPointerType) {
			BigInteger addr = value.getValueLocation().readValue(((IPointerType) origType).getByteSize());
			location = VariableLocationFactory.createMemoryVariableLocation(
					fInterpreter.getServicesTracker(), fInterpreter.getContext(), addr);
			castValue.setValueLocation(location);
		}
		*/
		
		fInterpreter.push(castValue);
	}
}
