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
package org.eclipse.cdt.debug.edc.x86.disassembler;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Address expression evaluator for x86.<br>
 * <br>
 * Examples expressions for x86:
 * 
 * <pre>
 *  *%ecx         : address in ECX register
 *  *0x831cffc    : address in memory address 0x831cffc
 *  *0x831cee8(,%eax,4) :
 *  *0x3(%edx)
 *  $0xfebc,$0x12345  : offset 0x12345 in segment 0xfebc (?)
 * </pre>
 */
public class AddressExpressionEvaluatorX86 implements IAddressExpressionEvaluator {

	public IAddress evaluate(IExecutionDMContext context, String expression, IRegisters regService, IMemory memService)
			throws CoreException {

		IAddress ret = null, memAddr = null;
		Registers edcRegService = (Registers) regService;

		if (expression.startsWith("*")) {
			String str = expression.substring(1);

			StringTokenizer tokenizer = new StringTokenizer(str, "(,)");
			int tokenCnt = tokenizer.countTokens();

			if (tokenCnt == 1) {
				// "*%eax" or "*0x80000"

				String token = tokenizer.nextToken();

				if (token.startsWith("%")) {
					String regName = token.substring(1).toUpperCase();

					String val = edcRegService.getRegisterValue(context, regName);

					ret = new Addr64(val, 16);
				} else { // must be a hex string like 0x80000
					memAddr = new Addr64(token.substring(2), 16);
					// read from the memAddr below.
				}
			} else if (tokenCnt >= 2 && tokenCnt <= 4) {
				// 1) *0x3(%edx)
				// 2) *0x831cee8(,%eax,4)
				// 3) *0x831cee8(%ebx,%eax,4)

				String token = tokenizer.nextToken();

				// Could offset be negative ?
				assert !token.startsWith("-");

				BigInteger offset = new BigInteger(token.substring(2), 16);

				String baseReg = null, indexReg = null;
				int scale = 1;

				if (tokenCnt == 2) {
					indexReg = tokenizer.nextToken().substring(1).toUpperCase();
					scale = 1;
				} else if (tokenCnt == 3) {
					indexReg = tokenizer.nextToken().substring(1).toUpperCase();
					scale = Integer.valueOf(tokenizer.nextToken());
				} else if (tokenCnt == 4) {
					baseReg = tokenizer.nextToken().substring(1).toUpperCase();
					indexReg = tokenizer.nextToken().substring(1).toUpperCase();
					scale = Integer.valueOf(tokenizer.nextToken());
				}

				long base = 0, index = 0;

				if (baseReg != null)
					base = Long.valueOf(edcRegService.getRegisterValue((IEDCExecutionDMC) context, baseReg), 16);
				if (indexReg != null)
					index = Long.valueOf(edcRegService.getRegisterValue((IEDCExecutionDMC) context, indexReg), 16);

				memAddr = new Addr64(offset).add(base + index * scale);
				// next read the memAddr to get the address we need.
			} else {
				// invalid
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, MessageFormat.format(
						"Jump address expression {0} is invalid.", expression)));
			}
		} else if (expression.startsWith("$")) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, MessageFormat.format(
					"Far jump address {0} is not supported yet.", expression)));
		}

		// Now read memory to get our final address
		//
		if (ret == null && memAddr != null) {
			ArrayList<MemoryByte> memBuffer = new ArrayList<MemoryByte>();

			IStatus st = ((IEDCMemory)memService).getMemory((IEDCExecutionDMC) context, memAddr, memBuffer, 4, 1);

			if (!st.isOK())
				throw new CoreException(st);

			byte[] bytes = new byte[memBuffer.size()];
			for (int i = 0; i < bytes.length; i++) {
				// check each byte
				if (!memBuffer.get(i).isReadable())
					throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, 
							("Cannot read memory at 0x" + memAddr.add(i).getValue().toString(16))));
				bytes[i] = memBuffer.get(i).getValue();
			}

			ret = new Addr64(new BigInteger(bytes));
		}

		return ret;
	}

}
