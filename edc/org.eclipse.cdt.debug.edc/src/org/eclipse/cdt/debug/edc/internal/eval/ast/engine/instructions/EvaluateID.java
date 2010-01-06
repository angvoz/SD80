/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.EnumeratorDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.IVariableEnumeratorContext;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.VariableDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class EvaluateID extends SimpleInstruction {

	private final IASTIdExpression idExpression;

	/**
	 * Constructor for ID (number + variable name) evaluate instruction
	 * 
	 * @param ID
	 */
	public EvaluateID(IASTIdExpression expression) {
		idExpression = expression;
	}

	/**
	 * Resolve a variable ID
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {

		Object context = getContext();

		if (!(context instanceof StackFrameDMC))
			return;

		IASTName lookupName;

		if (idExpression.getName() instanceof ICPPASTQualifiedName) {
			// the name has the form namespace::...::variable
			final ICPPASTQualifiedName qualifiedName = (ICPPASTQualifiedName) idExpression.getName();
			lookupName = qualifiedName.getLastName();
		} else {
			lookupName = idExpression.getName();
		}

		String name = new String(lookupName.getLookupKey());

		StackFrameDMC frame = (StackFrameDMC) context;
		DsfServicesTracker servicesTracker = frame.getDsfServicesTracker();
		Modules modules = servicesTracker.getService(Modules.class);

		// check by name for a variable or enumerator
		IVariableEnumeratorContext variableOrEnumerator = frame.findVariableOrEnumeratorByName(name, false);
		VariableDMC variable = variableOrEnumerator instanceof VariableDMC ?
									(VariableDMC)variableOrEnumerator : null;
		EnumeratorDMC enumerator = variableOrEnumerator instanceof EnumeratorDMC ?
									(EnumeratorDMC)variableOrEnumerator : null;

		// This may be called on debugger shutdown, in which case the "modules" 
		// service may have been shutdown.
		if (variable != null && modules != null) {
			Object valueLocation = new Object();
			ISymbolDMContext symContext = DMContexts.getAncestorOfType(frame, ISymbolDMContext.class);
			ILocationProvider provider = variable.getVariable().getLocationProvider();
			IAddress pcValue = frame.getIPAddress();
			ModuleDMC module = modules.getModuleByAddress(symContext, pcValue);
			IVariableLocation location = provider.getLocation(servicesTracker, frame, module.toLinkAddress(pcValue));
			if (location instanceof IMemoryVariableLocation) {
				IMemoryVariableLocation memoryLocation = (IMemoryVariableLocation) location;
				if (memoryLocation.isRuntimeAddress()) {
					valueLocation = memoryLocation.getAddress();
				} else {
					valueLocation = module.toRuntimeAddress(memoryLocation.getAddress());
				}
			} else {
				// either in a register or not live at the given address
				valueLocation = location;
			}
			setValueLocation(valueLocation);
			setValueType(variable.getVariable().getType());
			// create a VariableWithValue and push on the stack
			VariableWithValue varWval = new VariableWithValue(servicesTracker, frame, variable.getVariable());
			varWval.setValueLocation(valueLocation);
			push(varWval);
			return;
		}

		if (enumerator != null) {
			setValueLocation(""); //$NON-NLS-1$
			setValueType("long"); //$NON-NLS-1$
			push(new Long(enumerator.getEnumerator().getValue()));
			return;
		}

		// did not find a variable or an enumerator to match the expression
		InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.EvaluateID_VariableNotFound);
		push(invalidExpression);
		setLastValue(invalidExpression);
		setValueLocation(""); //$NON-NLS-1$
		setValueType(""); //$NON-NLS-1$
		return;
	}

}
