/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI thread list parsing.
&"info shared\n"
~"From        To          Syms Read   Shared Object Library\n"
~"0x40042fa0  0x4013ba9b  Yes         /lib/i686/libc.so.6\n"
~"0x40001db0  0x4001321c  Yes         /lib/ld-linux.so.2\n"

 */
public class MIInfoSharedLibraryInfo extends MIInfo {

	MIShared[] shared;
	boolean isUnixFormat = true;
	boolean hasProcessHeader = false;

	public MIInfoSharedLibraryInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MIShared[] getMIShared() {
		return shared;
	}

	void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the shared info
					parseShared(str.trim(), aList);
				}
			}
		}
		shared = new MIShared[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			shared[i] = (MIShared) aList.get(i);
		}
	}

	void parseShared(String str, List aList) {
		if (!hasProcessHeader) {
			// Process the header and choose a type.
			if (str.startsWith("DLL")) {
				isUnixFormat = false;
			}
			hasProcessHeader = true;
		} else if (isUnixFormat) {
			parseUnixShared(str, aList);
		} else {
			parseWinShared(str, aList);
		}
	}

	/**
	 * We do the parsing backward because on some Un*x system, the To or the From
	 * and even the "Sym Read" can be empty....
	 * @param str
	 * @param aList
	 */
	void parseUnixShared(String str, List aList) {
		if (str.length() > 0) {
			// Pass the header
			int index = -1;
			long from = 0;
			long to = 0;
			boolean syms = false;
			String name = "";

			for (int i = 0;(index = str.lastIndexOf(' ')) != -1 || i <= 3; i++) {
				if (index == -1) {
					index = 0;
				}
				String sub = str.substring(index).trim();
				// move to previous column
				str = str.substring(0, index).trim();
				switch (i) {
					case 0 :
						name = sub;
						break;
					case 1 :
						if (sub.equalsIgnoreCase("Yes")) {
							syms = true;
						}
						break;
					case 2 : // second column is "To"
						try {
							to = Long.decode(sub).longValue();
						} catch (NumberFormatException e) {
						}
						break;
					case 3 : // first column is "From"
						try {
							from = Long.decode(sub).longValue();
						} catch (NumberFormatException e) {
						}
						break;
				}
			}
			if (name.length() > 0) {
				MIShared s = new MIShared(from, to, syms, name);
				aList.add(s);
			}
		}
	}

	void parseWinShared(String str, List aList) {
		long from = 0;
		long to = 0;
		boolean syms = true;

		int index = str.lastIndexOf(' ');
		if (index > 0) {
			String sub = str.substring(index).trim();
			// Go figure they do not print the "0x" to indicate hexadecimal!!
			if (!sub.startsWith("0x")) {
				sub = "0x" + sub;
			}
			try {
				from = Long.decode(sub).longValue();
			} catch (NumberFormatException e) {
			}
			str = str.substring(0, index).trim();
		}
		MIShared s = new MIShared(from, to, syms, str.trim());
		aList.add(s);
	}
}
