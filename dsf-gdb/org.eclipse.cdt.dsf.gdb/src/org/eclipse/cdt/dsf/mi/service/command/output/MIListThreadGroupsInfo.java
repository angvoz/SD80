/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * GDB/MI thread group parsing.
 * 
 *  The description field can be different depending on the target we are connected to.
 *
 *  -list-thread-groups --available:
 *  ^done,groups=[{id="161",type="process",description="name: JIM_InstallerProcess, type 555481, locked: N, system: N, state: Idle"},
 *                {id="162",type="process",description="name: JIM_TcpSetupHandlerProcess, type 555505, locked: N, system: N, state: Idle"},
 *                {id="165",type="process",description="name: JUnitProcess2_PT, type 1094608, locked: N, system: N, state: Idle"},
 *                {id="166",type="process",description="name: JUnitProcess_PT, type 1094605, locked: N, system: N, state: Idle"}]
 *
 *          	  {id="3602",type="process",description="/usr/sbin/dhcdbd --system",user="root"}
 *  -list-thread-groups: 
 *  ^done,groups=[{id="162",type="process",pid="162"}]
 *
 *  list-thread-groups GROUPID, in the case of a running thread or a stopped thread:
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT (Ready) 1030373359 44441",frame={level="0",addr="0x00000000",func="??",args=[]},state="stopped"}]
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT Idle 981333916 42692",state="running"}]
 *      
 * Example of outputs by version on Linux
 * 
 * GDB 7.0
 *  
 *  (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[]
 *
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="19386",type="process",pid="19386"}]
 * 
 * -list-thread-groups 19386
 * ^done,threads=[{id="1",target-id="process 19386",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped"}]
 * 
 * -list-thread-groups --available 
 * ^done,groups=[{id="19371",type="process",description="gdb.7.0 -i mi testing/a.out",user="lmckhou"},{id="19386",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19413",type="process",description="sleep 5",user="lmckhou"}]
 * 
 * GDB 7.1
 * 
 * (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[{id="0",type="process",pid="0"}]
 * 
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="19424",type="process",pid="19424",cores=["3"]}]
 * 
 * -list-thread-groups 19424
 * ^done,threads=[{id="1",target-id="process 19424",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped",core="3"}]
 * 
 * -list-thread-groups --available
 * ^done,groups=[{id="19418",type="process",description="gdb.7.1 -i mi testing/a.out",user="lmckhou"},{id="19424",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19438",type="process",description="sleep 5",user="lmckhou"}]
 * 
 * GDB 7.2
 * 
 * (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[{id="i1",type="process",executable="/local/lmckhou/testing/a.out"}]
 * 
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="i1",type="process",pid="19451",executable="/local/lmckhou/testing/a.out",cores=["2"]}]
 * 
 * -list-thread-groups i1
 * ^done,threads=[{id="1",target-id="process 19451",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped",core="2"}]
 * 
 * -list-thread-groups --available
 * ^done,groups=[{id="19445",type="process",description="gdb.7.2 -i mi testing/a.out",user="lmckhou"},{id="19451",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19462",type="process",description="sleep 5",user="lmckhou"}]
 *
 * @since 1.1
 */
public class MIListThreadGroupsInfo extends MIInfo {
	
	public interface IThreadGroupInfo {
		String getGroupId();
		String getPid();
		String getName();
		String getDesciption();
	}
	
	@Immutable
	private static class ThreadGroupInfo implements IThreadGroupInfo {
		final String fGroupId;
		final String fDescription;
		final String fName;
		final String fPid;
		
		public ThreadGroupInfo(String id, String description, String pid) {
			fGroupId = id;
			fDescription = description;
			fPid = pid;

			fName = parseName(fDescription);
		}
		
		private static String parseName(String desc) {
			String name = ""; //$NON-NLS-1$

			// Find the string "name: " followed by the smallest set of characters that
			// is followed by a comma, or by the end of the line.
			Pattern pattern = Pattern.compile("name: (.*?)(, |$)", Pattern.MULTILINE); //$NON-NLS-1$
        	Matcher matcher = pattern.matcher(desc);
        	if (matcher.find()) {
        		name = matcher.group(1);
        	} else {
        		// If we didn't get the form "name: " then we expect to have the form
        		// "/usr/sbin/dhcdbd --system"
        		name = desc.split("\\s", 2)[0]; //$NON-NLS-1$
        	}

			return name;
		}
		
		public String getGroupId() { return fGroupId; }
		public String getPid() { return fPid; }

		public String getName() { return fName;	}

		public String getDesciption() { return fDescription; }
	}
	
	
	private IThreadGroupInfo[] fGroupList;
	private MIThreadInfoInfo fThreadInfo;
	
    public MIListThreadGroupsInfo(MIOutput out) {
        super(out);
        parse();
	}
	
	public IThreadGroupInfo[] getGroupList() { return fGroupList; }
	public MIThreadInfoInfo getThreadInfo() { return fThreadInfo; }
	
	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("groups")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseGroups((MIList)val);
						}
					} else if (var.equals("threads")) { //$NON-NLS-1$
						// Re-use the MIThreadInfoInfo parsing
						fThreadInfo = new MIThreadInfoInfo(out);
					}
				}
			}
		}
		if (fGroupList == null) {
			fGroupList = new IThreadGroupInfo[0];
		}
		if (fThreadInfo == null) {
			fThreadInfo = new MIThreadInfoInfo(null);
		}
	}

	private void parseGroups(MIList list) {
		MIValue[] values = list.getMIValues();
		fGroupList = new IThreadGroupInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			String id, desc, pid;
			id = desc = pid = "";//$NON-NLS-1$
			
			for (MIResult result : results) {
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						id = str.trim();
					}
				} else if (var.equals("description")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						desc = str.trim();

					}
				} else if (var.equals("pid")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						pid = str.trim();
					}
				}
			}
			// In the case of -list-thread-groups --available, the pid field is not present, but the
			// pid is used as the main id.  To know we are in this case, we check that we have
			// a description, that only happens for -list-thread-groups --available
			// We must check this because with GDB 7.2, there will be no pid field as a result
			// of -list-thread-groups, if no process is actually running yet.
			if (pid.equals("") && !desc.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				pid = id;
			}
			fGroupList[i] = new ThreadGroupInfo(id, desc, pid);
		}
	}
}
