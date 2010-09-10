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

package org.eclipse.cdt.debug.edc.services;

import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.tm.tcf.services.IRegisters;

/**
 * DSF service that provides data peculiar to the environment the debugger is
 * targeting. The environment here includes such things as target hardware, OS
 * if any, and UI preferences for the debugger. <br>
 * This service is supposed to be called by other DSF services so that they can
 * be target independent as much as possible.
 */
public interface ITargetEnvironment extends IDsfService {

	public final static String ARCH_X86 = "x86";
	public final static String ARCH_ARM = "ARM";

	public final static String OS_UNKNOWN = "unknown";
	public final static String OS_WIN32 = "win32";
	public final static String OS_LINUX = "linux";
	public final static String OS_SYMBIAN = "symbian";

	/**
	 * Get the string name of the target system architecture.
	 * 
	 * @return string name which is one of the predefined ARCH_xx string in
	 *         {@link ITargetEnvironment}. Cannot be null.
	 */
	public String getArchitecture();

	/**
	 * Get the string name of the target operating system (if any).
	 * 
	 * @return string name which is one of the predefined OS_xx string in
	 *         {@link ITargetEnvironment}. Cannot be null.
	 *         {@link ITargetEnvironment#OS_UNKNOWN} if the OS is unknown. Empty
	 *         string if there is no OS running.
	 */
	public String getOS();

	/**
	 * Get sizes of all basic C/C++ data types.
	 * 
	 * @return list of sizes, in bytes, of C/C++ data types. The returned map
	 *         should have TypeUtils#BASIC_TYPE_XXX constants for its keys, and
	 *         type sizes for their values
	 */
	public Map<Integer, Integer> getBasicTypeSizes();

	/**
	 * Get size of pointer data type
	 * 
	 * @return list of sizes, in bytes, of C/C++ data types
	 */
	public int getPointerSize();

	/**
	 * Get size of an enumeration data type
	 * 
	 * @return list of sizes, in bytes, of C/C++ data types
	 */
	public int getEnumSize();

	/**
	 * Get whether plain "char" is signed
	 * 
	 * @return whether "plain" char is signed
	 */
	public boolean isCharSigned();

	/**
	 * Get ID of program counter (PC) register. E.g. for X86, it could be "EIP". <br>
	 * <br>
	 * (This is temporarily needed with our current TCF {@link ISimpleRegisters}
	 * service. After we implement the TCF {@link IRegisters} service, this can
	 * be removed.)
	 * 
	 * @return string representation of the register ID.
	 */
	public String getPCRegisterID();

	/**
	 * Get length in bytes of the longest instruction in target architecture.
	 * This return value does not have to be precise, but must be larger than
	 * size of the longest instruction.
	 * 
	 * @return
	 */
	public int getLongestInstructionLength();

	/**
	 * Get breakpoint instruction that is used to set software breakpoint.<br>
	 * <br>
	 * For architecture like x86 the breakpoint instruction is invariant
	 * ("int 3" or "0xcc") thus the arguments are ignored. But for processor
	 * like ARM the instruction varies depending on processor mode (ARM or
	 * THUMB) in give context.
	 * 
	 * @param context
	 *            the runtime context, usually a process.
	 * @param address
	 *            runtime absolute address where to set a breakpoint.
	 * 
	 * @return byte array of the instruction.
	 */
	public byte[] getBreakpointInstruction(IDMContext context, IAddress address);

	/**
	 * Allows for modification or addition of target specific breakpoint
	 * properties before breakpoints are set. Note that this applies to TCF
	 * breakpoint service breakpoints.
	 * 
	 * @param context
	 *            the runtime context, usually a process.
	 * @param address
	 *            runtime absolute address where to set a breakpoint.
	 * @param properties
	 *            properties map
	 */
	public void updateBreakpointProperties(IDMContext context, IAddress address, Map<String, Object> properties);

	/**
	 * Is the target processor in little-endian ?
	 * 
	 * @param context
	 *            context for which the check is based on. For most cases this
	 *            argument can be ignored.
	 * @return
	 */
	public boolean isLittleEndian(IDMContext context);

	/**
	 * Get disassembler for the target.
	 * 
	 * @return {@link IDisassembler} object. Can be null which means
	 *         disassembler is not implemented yet for the target.
	 */
	public IDisassembler getDisassembler();

	/**
	 * Get address expression evaluator for the target.
	 * 
	 * @return {@link IAddressExpressionEvaluator} object. null means it's not
	 *         available yet for the target.
	 */
	public IAddressExpressionEvaluator getAddressExpressionEvaluator();

	/**
	 * Get minimum size of a memory block that is read and stored in memory
	 * cache. For instance, if the size is 64 bytes, then for a memory read
	 * request that requests 4 bytes, the debugger will read and cache 64 bytes.
	 * Different targets may prefer different size.
	 * 
	 * @return size in bytes. zero (0) means to just cache the number of bytes
	 *         actually requested.
	 */
	public int getMemoryCacheMinimumBlockSize();
	
	/**
	 * Get value of any given property.<br>
	 * <br>
	 * This generic API allows getting any new target property without adding
	 * new API that breaks backward compatibility.
	 * 
	 * @param propertyKey
	 * @return
	 */
	public String getProperty(String propertyKey);
}
