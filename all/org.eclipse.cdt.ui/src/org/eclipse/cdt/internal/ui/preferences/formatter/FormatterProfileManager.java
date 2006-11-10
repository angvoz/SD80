/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;

public class FormatterProfileManager extends ProfileManager {
	
	public final static String KANDR_PROFILE= "org.eclipse.cdt.ui.default.kandr_profile"; //$NON-NLS-1$
	public final static String ALLMAN_PROFILE= "org.eclipse.cdt.ui.default.allman_profile"; //$NON-NLS-1$
	public final static String GNU_PROFILE= "org.eclipse.cdt.ui.default.gnu_profile"; //$NON-NLS-1$
	public final static String WHITESMITHS_PROFILE= "org.eclipse.cdt.ui.default.whitesmites_profile"; //$NON-NLS-1$

	public final static String DEFAULT_PROFILE= KANDR_PROFILE;
	
	private final static KeySet[] KEY_SETS= new KeySet[] {
		new KeySet(CCorePlugin.PLUGIN_ID, new ArrayList(DefaultCodeFormatterConstants.getDefaultSettings().keySet())),
		new KeySet(CUIPlugin.PLUGIN_ID, Collections.EMPTY_LIST)	
	};
	
	private final static String PROFILE_KEY= PreferenceConstants.FORMATTER_PROFILE;
	private final static String FORMATTER_SETTINGS_VERSION= "formatter_settings_version";  //$NON-NLS-1$

	public FormatterProfileManager(List profiles, IScopeContext context, PreferencesAccess preferencesAccess, IProfileVersioner profileVersioner) {
	    super(addBuiltinProfiles(profiles, profileVersioner), context, preferencesAccess, profileVersioner, KEY_SETS, PROFILE_KEY, FORMATTER_SETTINGS_VERSION);
    }
	
	private static List addBuiltinProfiles(List profiles, IProfileVersioner profileVersioner) {
		final Profile kandrProfile= new BuiltInProfile(KANDR_PROFILE, FormatterMessages.ProfileManager_kandr_profile_name, getKandRSettings(), 2, profileVersioner.getCurrentVersion(), profileVersioner.getProfileKind()); 
		profiles.add(kandrProfile);
		final Profile allmanProfile= new BuiltInProfile(ALLMAN_PROFILE, FormatterMessages.ProfileManager_allman_profile_name, getAllmanSettings(), 2, profileVersioner.getCurrentVersion(), profileVersioner.getProfileKind()); 
		profiles.add(allmanProfile);
		final Profile gnuProfile= new BuiltInProfile(GNU_PROFILE, FormatterMessages.ProfileManager_gnu_profile_name, getGNUSettings(), 2, profileVersioner.getCurrentVersion(), profileVersioner.getProfileKind()); 
		profiles.add(gnuProfile);
		final Profile whitesmithsProfile= new BuiltInProfile(WHITESMITHS_PROFILE, FormatterMessages.ProfileManager_whitesmiths_profile_name, getWhitesmithsSettings(), 2, profileVersioner.getCurrentVersion(), profileVersioner.getProfileKind()); 
		profiles.add(whitesmithsProfile);
		return profiles;
	}
	
	/** 
	 * @return Returns the default settings.
	 */
	public static Map getDefaultSettings() {
		return DefaultCodeFormatterConstants.getDefaultSettings();
	}

	/** 
	 * @return Returns the K&R settings.
	 */
	public static Map getKandRSettings() {
		return DefaultCodeFormatterConstants.getKandRSettings();
	}

	/** 
	 * @return Returns the ANSI settings.
	 */
	public static Map getAllmanSettings() {
		return DefaultCodeFormatterConstants.getAllmanSettings();
	}

	/** 
	 * @return Returns the GNU settings.
	 */
	public static Map getGNUSettings() {
		return DefaultCodeFormatterConstants.getGNUSettings();
	}

	/** 
	 * @return Returns the Whitesmiths settings.
	 */
	public static Map getWhitesmithsSettings() {
		return DefaultCodeFormatterConstants.getWhitesmithsSettings();
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager#getSelectedProfileId(org.eclipse.core.runtime.preferences.IScopeContext)
     */
	protected String getSelectedProfileId(IScopeContext instanceScope) { 
		String profileId= instanceScope.getNode(CUIPlugin.PLUGIN_ID).get(PROFILE_KEY, null);
		if (profileId == null) {
			// request from bug 129427
			profileId= new DefaultScope().getNode(CUIPlugin.PLUGIN_ID).get(PROFILE_KEY, null);
			// fix for bug 89739
//			if (DEFAULT_PROFILE.equals(profileId)) { // default default: 
//				IEclipsePreferences node= instanceScope.getNode(CCorePlugin.PLUGIN_ID);
//				if (node != null) {
//					String tabSetting= node.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, null);
//					if (CCorePlugin.SPACE.equals(tabSetting)) {
//						profileId= TAB_WIDTH_8_PROFILE;
//					}
//				}
//			}
		}
	    return profileId;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager#getDefaultProfile()
     */
    public Profile getDefaultProfile() {
	    return getProfile(DEFAULT_PROFILE);
    }
    
}
