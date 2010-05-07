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

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.ValueVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.core.runtime.CoreException;

/**
 * This operator directly casts the value of an expression without going through
 * "*(type*)&expr".  
 */
public class OperatorCastValue extends CompoundInstruction {
	private IASTCastExpression castExpr;
	private IType castType;

	public OperatorCastValue(int start, IType castType) {
		super(start);
		this.castType = castType;
	}
	
	public OperatorCastValue(int start, IASTCastExpression castExpr) {
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
		
		if (location == null)
			location = new ValueVariableLocation(value.getBigIntValue());
		
		Number castedValue = castValue.getValueByType(castType, location);
		
		castValue.setValue(castedValue);
		castValue.setValueLocation(location);
	
		fInterpreter.push(castValue);
	}

	public IType getCastType(TypeEngine typeEngine) throws CoreException {
		if (castType == null) {
			IASTTypeId typeId = castExpr.getTypeId();
			castType = typeEngine.getTypeForTypeId(typeId);
		}
		return castType;
	}
}
