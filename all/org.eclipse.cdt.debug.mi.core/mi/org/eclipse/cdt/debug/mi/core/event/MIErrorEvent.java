/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MILogStreamOutput;
import org.eclipse.cdt.debug.mi.core.output.MIOOBRecord;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MIStreamRecord;
import org.eclipse.cdt.debug.mi.core.output.MIValue;



/**
 * (gdb)
 * &"warning: Cannot insert breakpoint 2:\n"
 * &"Cannot access memory at address 0x8020a3\n"
 * 30^error,msg=3D"Cannot access memory at address 0x8020a3"=20
 */
public class MIErrorEvent extends MIStoppedEvent {

	String msg = "";
	String log = "";
	MIOOBRecord[] oobs;

	public MIErrorEvent(MIResultRecord rr, MIOOBRecord[] o) {
		super(rr);
		oobs = o;
		parse();
	}

	public String getMessage() {
		return msg;
	}

	public String getLogMessage() {
		return log;
	}

	void parse () {
		MIResultRecord rr = getMIResultRecord();
		if (rr != null) {
			MIResult[] results = rr.getMIResults();
			if (results != null) {
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();
					String str = "";
					if (value instanceof MIConst) {
						str = ((MIConst)value).getString();
					}

					if (var.equals("msg")) {
						msg = str;
					}
				}
			}
			if (oobs != null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < oobs.length; i++) {
					if (oobs[i] instanceof MILogStreamOutput) {
						MIStreamRecord o = (MIStreamRecord)oobs[i];
						sb.append(o.getString());
					}
				}
				log = sb.toString();
			}
		}
	}
}
