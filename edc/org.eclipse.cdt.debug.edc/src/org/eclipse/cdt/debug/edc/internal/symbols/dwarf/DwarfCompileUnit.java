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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.symbols.CompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.LineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCDwarfReader.AttributeList;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DwarfCompileUnit extends CompileUnitScope {

	protected EDCDwarfReader reader;
	protected AttributeList attributes;

	public DwarfCompileUnit(EDCDwarfReader reader, IPath filePath, IAddress lowAddress, IAddress highAddress,
			AttributeList attributes) {
		super(filePath, reader, lowAddress, highAddress);

		this.reader = reader;
		this.attributes = attributes;
	}

	@Override
	protected Collection<ILineEntry> parseLineTable() {
		List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();

		try {
			ByteBuffer data = reader.getDwarfSection(EDCDwarfReader.DWARF_DEBUG_LINE);
			int stmtList = attributes.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
			if (data != null && stmtList >= 0) {
				data.position(stmtList);

				/*
				 * Read line table header:
				 * 
				 * total_length: 4 bytes (excluding itself) version: 2 prologue
				 * length: 4 minimum_instruction_len: 1 default_is_stmt: 1
				 * line_base: 1 line_range: 1 opcode_base: 1
				 * standard_opcode_lengths: (value of opcode_base)
				 */

				// Remember the CU line tables we've parsed.
				int length = reader.read_4_bytes(data) + 4;

				// Skip the following till "opcode_base"
				@SuppressWarnings("unused")
				int version = reader.read_2_bytes(data);
				@SuppressWarnings("unused")
				int prologue_length = reader.read_4_bytes(data);
				int minimum_instruction_length = data.get();
				boolean is_stmt = data.get() > 0;
				int line_base = data.get();
				int line_range = data.get();

				int opcode_base = data.get();
				byte[] opcodes = new byte[opcode_base - 1];
				data.get(opcodes);

				// Read in directories.
				//
				ArrayList<String> dirList = new ArrayList<String>();

				// Put the compilation directory of the CU as the first dir
				String compDir = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);
				dirList.add(compDir);

				String str, fileName;

				while (true) {
					str = DwarfHelper.readString(data);
					if (str.length() == 0)
						break;
					// If the directory is relative, append it to the CU dir
					IPath dir = new Path(str);
					if (!dir.isAbsolute())
						dir = new Path(compDir).append(str);
					dirList.add(dir.toString());
				}

				// Read file names
				//
				ArrayList<IPath> fileList = new ArrayList<IPath>();

				long leb128;
				while (true) {
					fileName = DwarfHelper.readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index
					leb128 = reader.read_unsigned_leb128(data);

					IPath fullPath = DwarfHelper.normalizeFilePath(dirList.get((int) leb128), fileName, reader
							.getSymbolFile());
					if (fullPath != null) {
						fileList.add(fullPath);
					}

					// Skip the followings
					//
					// modification time
					leb128 = reader.read_unsigned_leb128(data);

					// file size in bytes
					leb128 = reader.read_unsigned_leb128(data);
				}

				int info_line = 1;
				long info_address = 0;
				int info_flags = 0;
				long info_file = 1;
				int info_column = 0;
				@SuppressWarnings("unused")
				long info_ISA = 0;

				long lineInfoEnd = stmtList + length;
				while (data.position() < lineInfoEnd) {
					byte opcodeB = data.get();
					int opcode = 0xFF & opcodeB;

					if (opcode >= opcode_base) {
						info_line += (((opcode - opcode_base) % line_range) + line_base);
						info_address += (opcode - opcode_base) / line_range * minimum_instruction_length;
						if (is_stmt) {
							lineEntries.add(new LineEntry(fileList.get((int) info_file - 1), info_line, info_column,
									new Addr32(info_address), null));
						}
						info_flags &= ~(DwarfConstants.LINE_BasicBlock | DwarfConstants.LINE_PrologueEnd | DwarfConstants.LINE_EpilogueBegin);
					} else if (opcode == 0) {
						long op_size = reader.read_unsigned_leb128(data);
						int op_pos = data.position();
						int code = data.get();
						switch (code) {
						case DwarfConstants.DW_LNE_define_file: {
							fileName = DwarfHelper.readString(data);
							long dir = reader.read_unsigned_leb128(data);
							@SuppressWarnings("unused")
							long modTime = reader.read_unsigned_leb128(data);
							@SuppressWarnings("unused")
							long fileSize = reader.read_unsigned_leb128(data);
							IPath fullPath = DwarfHelper.normalizeFilePath(dirList.get((int) dir), fileName, reader
									.getSymbolFile());
							if (fullPath != null) {
								fileList.add(fullPath);
							}
							break;
						}
						case DwarfConstants.DW_LNE_end_sequence:
							info_flags |= DwarfConstants.LINE_EndSequence;

							if (lineEntries.size() > 0) {
								// this just marks the end of a line number
								// program sequence. use
								// its address to set the high address of the
								// last line entry
								lineEntries.get(lineEntries.size() - 1).setHighAddress(new Addr32(info_address));
							}

							// it also resets the state machine
							info_file = 1;
							info_line = 1;
							info_address = 0;
							info_flags = 0;
							info_column = 0;
							info_ISA = 0;
							break;

						case DwarfConstants.DW_LNE_set_address:
							info_address = reader.read_4_bytes(data);
							break;
						default:
							data.position((int) (data.position() + op_size - 1));
							break;
						}
						assert (data.position() == op_pos + op_size);
					} else {
						switch (opcode) {
						case DwarfConstants.DW_LNS_copy:
							if (is_stmt) {
								lineEntries.add(new LineEntry(fileList.get((int) info_file - 1), info_line,
										info_column, new Addr32(info_address), null));
							}
							info_flags &= ~(DwarfConstants.LINE_BasicBlock | DwarfConstants.LINE_PrologueEnd | DwarfConstants.LINE_EpilogueBegin);
							break;
						case DwarfConstants.DW_LNS_advance_pc:
							info_address += reader.read_unsigned_leb128(data) * minimum_instruction_length;
							break;
						case DwarfConstants.DW_LNS_advance_line:
							info_line += reader.read_signed_leb128(data);
							break;
						case DwarfConstants.DW_LNS_set_file:
							info_file = reader.read_unsigned_leb128(data);
							break;
						case DwarfConstants.DW_LNS_set_column:
							info_column = (int) reader.read_unsigned_leb128(data);
							break;
						case DwarfConstants.DW_LNS_negate_stmt:
							is_stmt = !is_stmt;
							break;
						case DwarfConstants.DW_LNS_set_basic_block:
							info_flags |= DwarfConstants.LINE_BasicBlock;
							break;
						case DwarfConstants.DW_LNS_const_add_pc:
							info_address += (255 - opcode_base) / line_range * minimum_instruction_length;
							break;
						case DwarfConstants.DW_LNS_fixed_advance_pc:
							info_address += reader.read_2_bytes(data);
							break;
						case DwarfConstants.DW_LNS_set_prologue_end:
							info_flags |= DwarfConstants.LINE_PrologueEnd;
							break;
						case DwarfConstants.DW_LNS_set_epilogue_begin:
							info_flags |= DwarfConstants.LINE_EpilogueBegin;
							break;
						case DwarfConstants.DW_LNS_set_isa:
							info_ISA = reader.read_unsigned_leb128(data);
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		// sort by start address
		Collections.sort(lineEntries);

		// fill in the end addresses as needed
		ILineEntry previousEntry = null;
		for (ILineEntry line : lineEntries) {
			if (previousEntry != null && previousEntry.getHighAddress() == null) {
				previousEntry.setHighAddress(line.getLowAddress());
			}

			previousEntry = line;
		}

		// the last line entry
		if (previousEntry != null) {
			previousEntry.setHighAddress(getHighAddress());
		}

		return lineEntries;
	}

	public void setLowAddress(IAddress address) {
		this.lowAddress = address;
	}

	public void setHighAddress(IAddress address) {
		this.highAddress = address;
	}

	public void setAttributes(AttributeList attributes) {
		this.attributes = attributes;
	}

	/**
	 * For compilers that don't generate compile unit scopes, e.g. GCCE with
	 * dlls, this fixes up the low and high addresses of the compile unit based
	 * on the function scopes
	 */
	public void fixupScopes() {
		if (lowAddress.getValue().longValue() == 0 && highAddress.getValue().longValue() == 0) {
			// compile unit scopes not generated by the compiler so
			// figure it out from the functions
			IAddress newLowAddress = new Addr64(BigInteger.valueOf(0xFFFFFFFFL));
			IAddress newHighAddress = new Addr64(BigInteger.valueOf(0));
			;

			for (IScope func : getChildren()) {
				// the compiler may generate (bad) low/high pc's which are not
				// in
				// the actual module space for some functions. to work around
				// this,
				// only honor addresses that are above the actual link address
				if (func.getLowAddress().compareTo(reader.getBaseLinkAddress()) > 0) {
					if (func.getLowAddress().compareTo(newLowAddress) < 0) {
						newLowAddress = func.getLowAddress();
					}

					if (func.getHighAddress().compareTo(newHighAddress) > 0) {
						newHighAddress = func.getHighAddress();
					}
				}
			}

			lowAddress = newLowAddress;
			highAddress = newHighAddress;
		}
	}
}
