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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.math.BigInteger;
import java.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IRegisterVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class LocationExpression implements ILocationProvider {

	protected final IStreamBuffer location;
	protected final int addressSize;
	protected final IScope scope;

	public LocationExpression(IStreamBuffer location, int addressSize, IScope scope) {
		this.location = location;
		this.addressSize = addressSize;
		this.scope = scope;
	}

	public IVariableLocation getLocation(DsfServicesTracker tracker, IFrameDMContext context, IAddress forLinkAddress) {

		if (location == null) {
			return null;
		}
		
		// This is intentionally small.  No existing code that I've seen has more 
		// than a sprinkling of operands, much less ones that push. 
		IVariableLocation[] opStack = new IVariableLocation[8];
		int opStackPtr = 0;
				
		location.position(0);
		
		try {
			while (location.hasRemaining()) {
				byte opcodeB = location.get();
				int opcode = 0xFF & opcodeB;

				if (opcode >= DwarfConstants.DW_OP_lit0 && opcode <= DwarfConstants.DW_OP_lit31) {
					opStack[opStackPtr++] = new MemoryVariableLocation(tracker, 
							context, 
							BigInteger.valueOf(opcode - DwarfConstants.DW_OP_lit0), true);
				}
				else if (opcode >= DwarfConstants.DW_OP_reg0 && opcode <= DwarfConstants.DW_OP_reg31) {
					opStack[opStackPtr++] = new RegisterVariableLocation(tracker, context, null, (opcode - DwarfConstants.DW_OP_reg0));
				}
				else if (opcode >= DwarfConstants.DW_OP_breg0 && opcode <= DwarfConstants.DW_OP_breg31) {
					RegisterVariableLocation loc = new RegisterVariableLocation(tracker, context, null, (opcode - DwarfConstants.DW_OP_breg0));
					try {
						BigInteger value = loc.readValue(addressSize);
						
						long offset = DwarfInfoReader.read_signed_leb128(location);
						opStack[opStackPtr++] = new MemoryVariableLocation(tracker, context,
								value.add(BigInteger.valueOf(offset)), true);
					} catch (CoreException e) {
						return new InvalidVariableLocation(e.getMessage());
					}
				} else {

					switch (opcode) {
					case DwarfConstants.DW_OP_nop:
						// ignore
						break;
						
					case DwarfConstants.DW_OP_addr: /* Constant address. */
						// this is not a runtime address
						long addrValue = DwarfInfoReader.readAddress(location, addressSize);
						opStack[opStackPtr++] = new MemoryVariableLocation(tracker, context, 
								BigInteger.valueOf(addrValue), false);
						break;

					case DwarfConstants.DW_OP_deref: {
						ensureStack(opStackPtr, 1);
						try {
							BigInteger addr = opStack[opStackPtr - 1].readValue(addressSize);
							IVariableLocation loc = new MemoryVariableLocation(tracker, context,
									addr, true);
							opStack[opStackPtr - 1] = loc;
						} catch (CoreException e) {
							return new InvalidVariableLocation(e.getMessage());
						}
						break;
					}

					case DwarfConstants.DW_OP_plus_uconst: {
						ensureStack(opStackPtr, 1);
						long offset = DwarfInfoReader.read_unsigned_leb128(location);
						opStack[opStackPtr-1] = opStack[opStackPtr-1].addOffset(offset);
						break;
					}
					
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
						return new InvalidVariableLocation(MessageFormat.format(
								DwarfMessages.NotImplementedFormat, DwarfMessages.LocationExpression_DW_OP + opcode));

					case DwarfConstants.DW_OP_regx: /* Unsigned LEB128 register. */
						long regNum = DwarfInfoReader.read_unsigned_leb128(location);
						opStack[opStackPtr++] = new RegisterVariableLocation(tracker, context, null, ((int) regNum));
						break;
						
					case DwarfConstants.DW_OP_fbreg: /* Signed LEB128 offset. */
						long offset = DwarfInfoReader.read_signed_leb128(location);
						
						IFunctionScope functionScope = null;
						functionScope = getFunctionScope(forLinkAddress);
						
						IVariableLocation framePtrLoc = functionScope.getFrameBaseLocation().getLocation(tracker,
								context, forLinkAddress);
						if (framePtrLoc != null) {
							if (framePtrLoc instanceof InvalidVariableLocation)
								return framePtrLoc;

							// first resolve the frame base value and then add
							// the offset
							if (framePtrLoc instanceof IRegisterVariableLocation) {
								BigInteger frame = framePtrLoc.readValue(addressSize);
								
								opStack[opStackPtr++] = new MemoryVariableLocation(tracker, context, 
										frame.add(BigInteger.valueOf(offset)), true);
							} else {
								opStack[opStackPtr++] = framePtrLoc.addOffset(offset);
							}
						}
						
						break;

					case DwarfConstants.DW_OP_piece: 
						/*
						* ULEB128 size of piece
						* addressed.
						* TODO: GCC emits this for long long (is a composition operator
						* that combines values -- this may tax the IVariableLocation concept)
						*/
						assert (false);
						return new InvalidVariableLocation(MessageFormat.format(DwarfMessages.NotImplementedFormat,
								DwarfMessages.LocationExpression_MultiRegisterVariable));
						
					case DwarfConstants.DW_OP_bregx: /*
													 * ULEB128 register followed
													 * by SLEB128 off.
													 */
					case DwarfConstants.DW_OP_deref_size: /*
														 * 1-byte size of data
														 * retrieved.
														 */
					case DwarfConstants.DW_OP_xderef_size: /*
															 * 1-byte size of
															 * data retrieved.
															 */
					case DwarfConstants.DW_OP_push_object_address:
					case DwarfConstants.DW_OP_call2:
					case DwarfConstants.DW_OP_call4:
					case DwarfConstants.DW_OP_call_ref:
						assert (false);
						return new InvalidVariableLocation(MessageFormat.format(DwarfMessages.NotImplementedFormat,
								DwarfMessages.LocationExpression_DW_OP + opcode));

					default:
						assert (false);
						return new InvalidVariableLocation(MessageFormat.format(DwarfMessages.InternalErrorFormat,
								DwarfMessages.LocationExpression_UnexpectedOperand + opcode));
					}

				}

			}
		} catch (CoreException e) {
			return new InvalidVariableLocation(e.getMessage());
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		if (opStackPtr != 1) {
			assert(false);
			return new InvalidVariableLocation(MessageFormat.format(DwarfMessages.InternalErrorFormat,
			DwarfMessages.LocationExpression_BadStackSize));
		}
			
		return opStack[0];
	}

	/**
	 */
	private void ensureStack(int opStackPtr, int needed) throws CoreException {
		if (opStackPtr < needed) {
			throw EDCDebugger.newCoreException(MessageFormat.format(
					DwarfMessages.InternalErrorFormat,
					MessageFormat.format("expected {0} stack operands but had {1}", needed, opStackPtr)));
		}
	}

	/**
	 * @param forLinkAddress
	 * @param functionScope
	 * @return
	 * @throws CoreException
	 */
	private IFunctionScope getFunctionScope(IAddress forLinkAddress) throws CoreException {
		IFunctionScope functionScope = null;
		
		if (scope instanceof IFunctionScope) {
			functionScope = (IFunctionScope) scope;
		} else {
			IScope parent = scope.getParent();
			while (parent != null && !(parent instanceof IFunctionScope)) {
				parent = parent.getParent();
			}

			if (parent == null) {
				throw EDCDebugger.newCoreException("No function scope for " + scope + " at " + forLinkAddress.toHexAddressString());
			} else {
				functionScope = (IFunctionScope) parent;
			}
		}

		// inlined functions may be nested
		while (functionScope.getParent() instanceof IFunctionScope)
			functionScope = (IFunctionScope) functionScope.getParent();
		return functionScope;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider#isLocationKnown(org.eclipse.cdt.core.IAddress)
	 */
	public boolean isLocationKnown(IAddress forLinkAddress) {
		// an expression has a static lifetime
		return true;
	}
	
	public IScope getScope() {
		return scope;
	}
}
