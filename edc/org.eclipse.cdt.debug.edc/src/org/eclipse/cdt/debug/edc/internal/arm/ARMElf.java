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
package org.eclipse.cdt.debug.edc.internal.arm;

import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.elf.Elf;

/**
 * ARM Elf provides extra "mappings" in the symbol table to help distinguish
 * between ARM and Thumb code. These extra symbols are mapped to the same
 * address as the real symbol, but have a NULL type. The symbols of interest are
 * $a and $t, for ARM and Thumb modes respectively.
 */
public class ARMElf extends Elf {

	public ARMElf(String filename) throws IOException {
		super(filename);
	}

	public String getMappingSymbolAtAddress(IAddress address) throws IOException {

		// now use the start address of the symbol and try to find
		// the matching mapping entry
		Elf.Section symtab = getSectionByName(".symtab"); //$NON-NLS-1$

		int numSyms = 1;
		if (symtab.sh_entsize != 0) {
			numSyms = (int) symtab.sh_size / (int) symtab.sh_entsize;
		}
		long offset = symtab.sh_offset;
		for (int c = 0; c < numSyms; offset += symtab.sh_entsize, c++) {
			efile.seek(offset);
			Symbol symbol = new Symbol(symtab);
			switch (ehdr.e_ident[ELFhdr.EI_CLASS]) {
			case ELFhdr.ELFCLASS32: {
				byte[] addrArray = new byte[ELF32_ADDR_SIZE];

				symbol.st_name = efile.readIntE();
				efile.readFullyE(addrArray);
				symbol.st_value = new Addr32(addrArray);
				symbol.st_size = efile.readIntE();
				symbol.st_info = efile.readByte();
				symbol.st_other = efile.readByte();
				symbol.st_shndx = efile.readShortE();
			}
				break;
			case ELFhdr.ELFCLASS64: {
				byte[] addrArray = new byte[ELF64_ADDR_SIZE];

				symbol.st_name = efile.readIntE();
				symbol.st_info = efile.readByte();
				symbol.st_other = efile.readByte();
				symbol.st_shndx = efile.readShortE();
				efile.readFullyE(addrArray);
				symbol.st_value = new Addr64(addrArray);
				symbol.st_size = readUnsignedLong(efile);
			}
				break;
			case ELFhdr.ELFCLASSNONE:
			default:
				throw new IOException("Unknown ELF class " + ehdr.e_ident[ELFhdr.EI_CLASS]); //$NON-NLS-1$
			}

			if (symbol.st_info == 0) {
				if (symbol.st_value.equals(address)) {
					return symbol.toString();
				}
			}
		}

		return null;
	}
}
