package org.eclipse.cdt.debug.edc.services;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.services.messages"; //$NON-NLS-1$
	public static String AbstractEDCService_0;
	public static String AbstractEDCService_1;
	public static String AbstractEDCService_2;
	public static String AbstractEDCService_3;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
