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

import java.text.MessageFormat;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.Stack.EnumeratorDMC;
import org.eclipse.cdt.debug.edc.services.Stack.IVariableEnumeratorContext;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.services.Stack.VariableDMC;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class EvaluateID extends SimpleInstruction {

	private final String name;

	/**
	 * Constructor for ID (number + variable name) evaluate instruction
	 * 
	 * @param idExpression
	 */
	public EvaluateID(IASTIdExpression idExpression) {
		IASTName lookupName;

		if (idExpression.getName() instanceof ICPPASTQualifiedName) {
			// the name has the form namespace::...::variable
			final ICPPASTQualifiedName qualifiedName = (ICPPASTQualifiedName) idExpression.getName();
			lookupName = qualifiedName.getLastName();
		} else {
			lookupName = idExpression.getName();
		}

		name = new String(lookupName.getLookupKey());
	}
	
	/**
	 * Constructor for lookup of a specific literal name
     * (presumably a local, like "this")
     *
     * @param name the literal name
	 */
	public EvaluateID(String name) {
		this.name = name;
	}

	/**
	 * Resolve a variable ID
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {

		IDMContext context = getContext();

		if (!(context instanceof StackFrameDMC))
			throw EDCDebugger.newCoreException(MessageFormat.format(ASTEvalMessages.EvaluateID_CannotResolveName, name));

		StackFrameDMC frame = (StackFrameDMC) context;
		DsfServicesTracker servicesTracker = frame.getDsfServicesTracker();
		IEDCModules modules = servicesTracker.getService(IEDCModules.class);

		// check by name for a variable or enumerator
		IVariableEnumeratorContext variableOrEnumerator = frame.findVariableOrEnumeratorByName(name, false);
		VariableDMC variable = variableOrEnumerator instanceof VariableDMC ?
									(VariableDMC)variableOrEnumerator : null;
		EnumeratorDMC enumerator = variableOrEnumerator instanceof EnumeratorDMC ?
									(EnumeratorDMC)variableOrEnumerator : null;

		// This may be called on debugger shutdown, in which case the "modules" 
		// service may have been shutdown.
		if (variable != null && modules != null) {
			IVariableLocation valueLocation = null;
			ILocationProvider provider = variable.getVariable().getLocationProvider();
			IEDCModuleDMContext module = frame.getModule();
			if (module != null && provider != null) {
				valueLocation = provider.getLocation(servicesTracker, frame, module.toLinkAddress(frame.getInstructionPtrAddress()));
			}
			if (valueLocation == null) {
				// unhandled
				valueLocation = new InvalidVariableLocation(MessageFormat.format(ASTEvalMessages.EvaluateID_NameHasNoLocation, variable.getName()));
			}
			// create a VariableWithValue and push on the stack
			VariableWithValue varWval = new VariableWithValue(servicesTracker, frame, variable.getVariable());
			varWval.setValueLocation(valueLocation);
			push(varWval);
			return;
		}

		if (enumerator != null) {
			// TODO: map IEnumerator to an IEnumeration and use the real type
			pushNewValue(fInterpreter.getTypeEngine().getIntegerTypeOfSize(4, true), 
					enumerator.getEnumerator().getValue());
			return;
		}

		// did not find a variable or an enumerator to match the expression
		throw EDCDebugger.newCoreException(
				MessageFormat.format(ASTEvalMessages.EvaluateID_VariableNotFound, name));
	}

}
