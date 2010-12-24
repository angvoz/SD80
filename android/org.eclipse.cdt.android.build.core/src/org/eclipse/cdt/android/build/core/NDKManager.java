package org.eclipse.cdt.android.build.core;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class NDKManager {

	private static final String NDK_LOCATION = "ndkLocation";
	private static final String EMPTY = "";
	private static String ndkLocation;
	
	private static IEclipsePreferences getPrefs() {
		return new InstanceScope().getNode(Activator.getId());
	}
	
	public static String getNDKLocation() {
		if (ndkLocation == null) {
			ndkLocation = getPrefs().get(NDK_LOCATION, EMPTY);
		}
		return ndkLocation != EMPTY ? ndkLocation : EMPTY;
	}
	
	public static void setNDKLocation(String location) {
		IEclipsePreferences prefs = getPrefs();
		prefs.put(NDK_LOCATION, location);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
	
}
