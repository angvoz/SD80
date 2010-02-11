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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Registers;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IRegisterVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.utils.Addr64;

public class LocationExpression implements ILocationProvider {

	protected EDCDwarfReader reader;
	protected ByteBuffer location;
	protected int addressSize;
	protected IScope scope;

	public LocationExpression(EDCDwarfReader reader, byte[] locationData, int addressSize, IScope scope) {
		this.reader = reader;
		location = ByteBuffer.wrap(locationData);
		this.addressSize = addressSize;
		this.scope = scope;
	}

	public IVariableLocation getLocation(DsfServicesTracker tracker, IFrameDMContext context, IAddress forLinkAddress) {

		if (location != null) {
			location.position(0);
		}

		try {
			while (location != null && location.hasRemaining()) {
				byte opcodeB = location.get();
				int opcode = 0xFF & opcodeB;

				if (opcode >= DwarfConstants.DW_OP_lit0 && opcode <= DwarfConstants.DW_OP_lit31) {
					assert (false);
				}
				if (opcode >= DwarfConstants.DW_OP_reg0 && opcode <= DwarfConstants.DW_OP_reg31) {
					return new RegisterVariableLocation(null, opcode - DwarfConstants.DW_OP_reg0);
				}
				if (opcode >= DwarfConstants.DW_OP_breg0 && opcode <= DwarfConstants.DW_OP_breg31) {
					// TODO we need to get the register value from the stack
					// frame
					Registers registersService = tracker.getService(Registers.class);
					long regValue = Long.valueOf(registersService.getRegisterValue(((StackFrameDMC) context)
							.getExecutionDMC(), opcode - DwarfConstants.DW_OP_breg0), 16);
					IAddress regAddress = new Addr64(BigInteger.valueOf(regValue));
					long offset = reader.read_signed_leb128(location);
					return new MemoryVariableLocation(regAddress.add(offset), true);
				} else {

					switch (opcode) {

					case DwarfConstants.DW_OP_addr: /* Constant address. */
						// this is not a runtime address
						long addrValue = reader.readAddress(location, addressSize);
						return new MemoryVariableLocation(new Addr64(BigInteger.valueOf(addrValue)), false);

					case DwarfConstants.DW_OP_deref:
					case DwarfConstants.DW_OP_const1u: /*
														 * Unsigned 1-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const1s: /*
														 * Signed 1-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const2u: /*
														 * Unsigned 2-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const2s: /*
														 * Signed 2-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const4u: /*
														 * Unsigned 4-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const4s: /*
														 * Signed 4-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const8u: /*
														 * Unsigned 8-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_const8s: /*
														 * Signed 8-byte
														 * constant.
														 */
					case DwarfConstants.DW_OP_constu: /* Unsigned LEB128 constant. */
					case DwarfConstants.DW_OP_consts: /* Signed LEB128 constant. */
					case DwarfConstants.DW_OP_dup:
					case DwarfConstants.DW_OP_drop:
					case DwarfConstants.DW_OP_over:
					case DwarfConstants.DW_OP_pick: /* 1-byte stack index. */
					case DwarfConstants.DW_OP_swap:
					case DwarfConstants.DW_OP_rot:
					case DwarfConstants.DW_OP_xderef:
					case DwarfConstants.DW_OP_abs:
					case DwarfConstants.DW_OP_and:
					case DwarfConstants.DW_OP_div:
					case DwarfConstants.DW_OP_minus:
					case DwarfConstants.DW_OP_mod:
					case DwarfConstants.DW_OP_mul:
					case DwarfConstants.DW_OP_neg:
					case DwarfConstants.DW_OP_not:
					case DwarfConstants.DW_OP_or:
					case DwarfConstants.DW_OP_plus:
					case DwarfConstants.DW_OP_plus_uconst: /*
															 * Unsigned LEB128
															 * addend.
															 */
					case DwarfConstants.DW_OP_shl:
					case DwarfConstants.DW_OP_shr:
					case DwarfConstants.DW_OP_shra:
					case DwarfConstants.DW_OP_xor:
					case DwarfConstants.DW_OP_bra: /* Signed 2-byte constant. */
					case DwarfConstants.DW_OP_eq:
					case DwarfConstants.DW_OP_ge:
					case DwarfConstants.DW_OP_gt:
					case DwarfConstants.DW_OP_le:
					case DwarfConstants.DW_OP_lt:
					case DwarfConstants.DW_OP_ne:
					case DwarfConstants.DW_OP_skip: /* Signed 2-byte constant. */
						// TODO
						assert (false);
						break;

					case DwarfConstants.DW_OP_regx: /* Unsigned LEB128 register. */
						long regNum = reader.read_unsigned_leb128(location);
						return new RegisterVariableLocation(null, (int) regNum);

					case DwarfConstants.DW_OP_fbreg: /* Signed LEB128 register. */
						IFunctionScope functionScope = null;
						if (scope instanceof IFunctionScope) {
							functionScope = (IFunctionScope) scope;
						} else {
							IScope parent = scope.getParent();
							while (parent != null && !(parent instanceof IFunctionScope)) {
								parent = parent.getParent();
							}

							if (parent == null) {
								assert (false);
								return null;
							} else {
								functionScope = (IFunctionScope) parent;
							}
						}

						IVariableLocation framePtrLoc = functionScope.getFrameBaseLocation().getLocation(tracker,
								context, forLinkAddress);
						if (framePtrLoc != null) {
							// first resolve the frame base value and then add
							// the offset
							if (framePtrLoc instanceof IRegisterVariableLocation) {
								// TODO we need to get the register value from
								// the stack frame
								Registers registersService = tracker.getService(Registers.class);
								long regValue = Long.valueOf(registersService.getRegisterValue(
										((StackFrameDMC) context).getExecutionDMC(),
										((IRegisterVariableLocation) framePtrLoc).getRegisterID()), 16);
								IAddress regAddress = new Addr64(BigInteger.valueOf(regValue));
								long offset = reader.read_signed_leb128(location);
								try {
									regAddress = regAddress.add(offset);
								} catch (Exception e) {
									// probably at the opening bracket before
									// the frame is created so
									// we have a bogus register value
									return new InvalidVariableLocation(DwarfMessages.UnknownVariableAddress);
								}
								return new MemoryVariableLocation(regAddress, true);
							} else if (framePtrLoc instanceof IMemoryVariableLocation) {
								IAddress address = ((IMemoryVariableLocation) framePtrLoc).getAddress();
								long offset = reader.read_signed_leb128(location);
								return new MemoryVariableLocation(address.add(offset), true);
							} else
								assert (false);
						}
						break;

					case DwarfConstants.DW_OP_bregx: /*
													 * ULEB128 register followed
													 * by SLEB128 off.
													 */
					case DwarfConstants.DW_OP_piece: /*
													 * ULEB128 size of piece
													 * addressed.
													 */
					case DwarfConstants.DW_OP_deref_size: /*
														 * 1-byte size of data
														 * retrieved.
														 */
					case DwarfConstants.DW_OP_xderef_size: /*
															 * 1-byte size of
															 * data retrieved.
															 */
					case DwarfConstants.DW_OP_nop:
					case DwarfConstants.DW_OP_push_object_address:
					case DwarfConstants.DW_OP_call2:
					case DwarfConstants.DW_OP_call4:
					case DwarfConstants.DW_OP_call_ref:
						assert (false);
						break;

					default:
						assert (false);
						break;
					}

				}

			}
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider#isLocationKnown(org.eclipse.cdt.core.IAddress)
	 */
	public boolean isLocationKnown(IAddress forLinkAddress) {
		// no-op for old reader
		return true;
	}
}
