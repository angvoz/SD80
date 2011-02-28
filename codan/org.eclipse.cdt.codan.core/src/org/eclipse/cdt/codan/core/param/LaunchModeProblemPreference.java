/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.IOException;
import java.io.StreamTokenizer;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;

/**
 * Problem preference for launch type of the checker
 *
 * @since 2.0
 */
public class LaunchModeProblemPreference extends MapProblemPreference {
	/**
	 * Propery key
	 */
	public static final String KEY = "launchModes"; //$NON-NLS-1$

	@Override
	public String getKey() {
		return KEY;
	}

	/**
	 * constructor
	 */
	public LaunchModeProblemPreference() {

	}

	/**
	 * @return true if values has not been set
	 */
	@Override
	public boolean isDefault(){
		CheckerLaunchMode[] values = CheckerLaunchMode.values();
		for (int i = 0; i < values.length; i++) {
			CheckerLaunchMode checkerLaunchMode = values[i];
			if (getChildDescriptor(checkerLaunchMode.name())!=null) return false;
		}
		return true;
	}

	/**
	 * @param checkerLaunchMode - launch mode
	 * @param value - value to set for the mode
	 * @return preference
	 */
	public IProblemPreference addLaunchMode(CheckerLaunchMode checkerLaunchMode, boolean value) {
		BasicProblemPreference desc = new BasicProblemPreference(checkerLaunchMode.name(), checkerLaunchMode.name(),
				PreferenceType.TYPE_BOOLEAN);
		IProblemPreference desc1 = addChildDescriptor(desc);
		desc1.setValue(value);
		return desc1;
	}

	/**
	 * @param mode
	 * @return true if this mode enabled for this preference
	 */
	public boolean isRunningInMode(CheckerLaunchMode mode) {
		if (getChildDescriptor(mode.name())==null) {
			if (mode == CheckerLaunchMode.RUN_ON_INC_BUILD)
				return isRunningInMode(CheckerLaunchMode.RUN_ON_FULL_BUILD);
			return true; // default is true
		}
		Object value = getChildValue(mode.name());
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return false;
	}

	/**
	 * @param mode
	 * @param value
	 */
	public void setRunningMode(CheckerLaunchMode mode, boolean value) {
		if (getChildDescriptor(mode.name()) == null) {
			addLaunchMode(mode, value);
		} else {
			setChildValue(mode.name(), value);
		}
	}

	/**
	 * @return true if all modes are enabled (or this is parent mode)
	 */
	public boolean isAllEnabled() {
		return isRunningInMode(CheckerLaunchMode.RUN_AS_YOU_TYPE) && isRunningInMode(CheckerLaunchMode.RUN_ON_DEMAND)
				&& isRunningInMode(CheckerLaunchMode.RUN_ON_FULL_BUILD);
	}

	/**
	 * @return true if all modes are disabled
	 */
	public boolean isAllDisabled() {
		return !isRunningInMode(CheckerLaunchMode.RUN_AS_YOU_TYPE) && !isRunningInMode(CheckerLaunchMode.RUN_ON_DEMAND)
				&& !isRunningInMode(CheckerLaunchMode.RUN_ON_FULL_BUILD);
	}

	@Override
	protected IProblemPreference importChildValue(String key, StreamTokenizer tokenizer) throws IOException {
		IProblemPreference desc = getChildDescriptor(key);
		if (desc==null) {
			CheckerLaunchMode mode = CheckerLaunchMode.valueOf(key);
			if (mode==null)
				throw new IllegalArgumentException(key);
			desc = addLaunchMode(mode, true);
		}
		if (desc != null && desc instanceof AbstractProblemPreference) {
			((AbstractProblemPreference) desc).importValue(tokenizer);
			setChildValue(key, desc.getValue());
		}
		return desc;
	}
}
