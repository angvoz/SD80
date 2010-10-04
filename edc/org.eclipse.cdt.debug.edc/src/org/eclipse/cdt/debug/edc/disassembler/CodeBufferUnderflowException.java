package org.eclipse.cdt.debug.edc.disassembler;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * exists to help identify the precise CoreException being thrown by
 * instruction parsers so that the disassemblers can perform error
 * recovery more gracefully.  <br>extends CoreException so that
 * existing handlers can catch it without having to be modified.
 * @since 2.0
 */
public class CodeBufferUnderflowException extends CoreException {
	private static final long serialVersionUID = 2725920360107613447L;

	/**
	 * exists to help identify the precise CoreException being
	 * thrown by instruction parsers so that the disassemblers
	 * can perform error recovery more gracefully.
	 * @param t the original thrown object, for reference as necessary
	 */
	public CodeBufferUnderflowException(Throwable t) {
		super(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
						 " end of code buffer reached.", t));
	}
}
