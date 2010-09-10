package org.eclipse.cdt.debug.edc.windows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * @since 2.0
 */
public class WindowsOSDataProxy implements IWindowsOSData {

	private final IChannel channel;

	public WindowsOSDataProxy(IChannel channel) {
		this.channel = channel;
	}

	public String getName() {
		return NAME;
	}

	public IToken getThreads(final DoneGetThreads done) {
		return new Command(channel, this, "getThreads", new Object[] {}) {
			@SuppressWarnings("unchecked")
			@Override
			public void done(Exception error, Object[] args) {
				List<Map<String, Object>> threads = new ArrayList<Map<String, Object>>();
				if (error == null) {
					assert args.length == 2;
					error = toError(args[0]);
					threads = (List<Map<String, Object>>) args[1];
				}
				done.doneGetThreads(token, error, threads);
			}
		}.token;
	}

}
