/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;


import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class VariableWithValue extends OperandValue {
	final IVariable variable;
	private final DsfServicesTracker servicesTracker;
	private final StackFrameDMC frame;

	public VariableWithValue(DsfServicesTracker servicesTracker, StackFrameDMC frame, IVariable variable) {
		this(servicesTracker, frame, variable, false);
	}

	public VariableWithValue(DsfServicesTracker servicesTracker, StackFrameDMC frame, IVariable variable,
			boolean isBitField) {
		super(variable.getType(), isBitField);
		this.servicesTracker = servicesTracker;
		this.frame = frame;
		this.variable = variable;
	}
	
	public VariableWithValue(DsfServicesTracker servicesTracker, StackFrameDMC frame, IVariable variable, IType otherType) {
		super(otherType, false);
		this.servicesTracker = servicesTracker;
		this.frame = frame;
		this.variable = variable;
	}
	/**
	 * @return the servicesTracker
	 */
	public DsfServicesTracker getServicesTracker() {
		return servicesTracker;
	}
	/**
	 * @return the frame
	 */
	public StackFrameDMC getFrame() {
		return frame;
	}
	public IVariable getVariable() {
		return variable;
	}

	public Number getValue() throws CoreException {
		if (value == null) {
			IVariableLocation location = getValueLocation();
			IType varType = type;
			if (varType != null) {
				value = getValueByType(varType, location);
			} else {
				assert false;
			}
		}
		return value;
	}

	public IVariableLocation getValueLocation() {
		if (valueLocation == null) {
			ILocationProvider provider = variable.getLocationProvider();
			if (provider == null) {
				// ERROR
				valueLocation = new InvalidVariableLocation(ASTEvalMessages.VariableWithValue_CannotLocateVariable);
				return valueLocation;
			}
			IEDCModuleDMContext module = frame.getModule();
			valueLocation = provider.getLocation(servicesTracker, frame, module.toLinkAddress(frame.getInstructionPtrAddress()));
			if (valueLocation == null) {
				// unhandled
				valueLocation = new InvalidVariableLocation(ASTEvalMessages.VariableWithValue_CannotLocateVariable);
			}
		}
		return valueLocation;
	}
	
	public OperandValue copyWithType(IType otherType) {
		OperandValue value = new VariableWithValue(servicesTracker, frame, variable, otherType);
		value.stringValue = this.stringValue;
		value.valueLocation = this.valueLocation;
		return value;
	}
	
	public void setType(IType type) {
		this.type = type;
	}

}
