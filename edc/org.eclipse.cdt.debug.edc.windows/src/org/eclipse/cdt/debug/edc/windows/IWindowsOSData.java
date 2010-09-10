package org.eclipse.cdt.debug.edc.windows;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * @since 2.0
 */
public interface IWindowsOSData extends IService {

	/**
	 * Name of the service
	 */
	String NAME = "WindowsOSData";

	/*
	 * Property identifiers for data returned by this service:
	 */

	/*
	 * Common to processes, threads, chunks and libraries
	 */
	public static final String PROP_NAME = "name";

	/*
	 * Common to processes and threads
	 */
	public static final String PROP_OS_ID = ProtocolConstants.PROP_OS_ID;	

	/*
	 * Common to threads and modules
	 */
	public static final String PROP_OWNING_PROCESS_NAME = "p_name"; // owning_process_name
	public static final String PROP_OWNING_PROCESS_OS_ID = "p_os_id"; // owning_process_os_id
	
	/*
	 * Thread properties
	 */
	public static final String PROP_THREAD_PRIORITY = "pri";

	IToken getThreads(DoneGetThreads done);

	/**
	 * Client call back interface for getThreads().
	 */
	interface DoneGetThreads {
		/**
		 * Called when thread data retrieval is done.
		 * @param error – error description if operation failed, null if succeeded.
		 * @param chunks – list of thread data.
		 * 
		 */
		void doneGetThreads(IToken token, Exception error, List<Map<String, Object>> threads);
	}

}
