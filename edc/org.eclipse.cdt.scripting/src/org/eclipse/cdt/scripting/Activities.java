/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.scripting;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class Activities {
	
	static int nextActivityId;

	static Set<Integer> pendingActivityIds = new HashSet<Integer>();

	public static boolean isActivityDone(int id) {
		synchronized (pendingActivityIds) {
			return !pendingActivityIds.contains(id);
		}
	}
	
}
