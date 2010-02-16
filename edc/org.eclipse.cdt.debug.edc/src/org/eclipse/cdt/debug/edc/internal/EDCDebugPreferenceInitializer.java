/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class EDCDebugPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		EDCDebugger.getDefault().getPreferenceStore().setDefault(Album.PREF_VARIABLE_CAPTURE_DEPTH, 5);
		EDCDebugger.getDefault().getPreferenceStore().setDefault(Album.PREF_CREATION_CONTROL, Album.CREATE_MANUAL);
	}

}
