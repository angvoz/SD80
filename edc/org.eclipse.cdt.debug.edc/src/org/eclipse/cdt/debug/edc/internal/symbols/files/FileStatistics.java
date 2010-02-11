/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.internal.symbols.files;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Statistics tracking for file operations (used for debugging and unit tests)
 */
public class FileStatistics {
	/** if set, dump info to console at runtme */
	public static boolean DEBUG = false;
	/** # of executables opened in session */
	public static int executablesOpened;
	/** # of executables currently open in session */
	public static int executablesOpen;
	/** amount of memory for buffers currently allocated on heap */ 
	public static long currentHeapAllocatedBuffers;
	/** amount of memory for buffers currently allocated in memory maps */
	/** amount of memory for buffers ever allocated on heap */ 
	public static long totalHeapAllocatedBuffers;
	public static long currentMemoryMappedBuffers;
	/** amount of memory for buffers ever allocated in memory maps */ 
	public static long totalMemoryMappedBuffers;
	
	/** Log interesting information */ 
	public static void log(String line) {
		if (DEBUG)
			System.out.println(line);
	}
	
	private static String now() {
		Calendar cal = Calendar.getInstance();
	    DateFormat sdf = SimpleDateFormat.getTimeInstance();
	    return sdf.format(cal.getTime());
	}
	
	public static void dump() {
		System.out.println("File statistics at " + now() + ":");
		System.out.println("\t# executables opened: " + executablesOpened);
		System.out.println("\t# executables still open: " + executablesOpen);
		System.out.println("\tcurrent heap buffer allocation: " + currentHeapAllocatedBuffers);
		System.out.println("\ttotal heap buffer allocation: " + totalHeapAllocatedBuffers);
		System.out.println("\tcurrent memory mapped buffer allocation: " + currentMemoryMappedBuffers);
		System.out.println("\ttotal memory mapped buffer allocation: " + totalMemoryMappedBuffers);
	}
}
