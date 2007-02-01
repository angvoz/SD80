/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Symbian - Repeatedly index classTests test project to detect a particular race condition
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.NullProgressMonitor;


/**
 * Test case for a race condition from Bugzilla#157992
 */
public class RaceCondition157992Test extends PDOMTestBase {
	public void testRepeatedly() throws Exception {
		int successes = 0, noTrials = 100;
		
		for(int i=0; i<noTrials; i++) {
			ICProject project = createProject("classTests");
			PDOM pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
			pdom.acquireReadLock();	

			IBinding[] Bs = pdom.findBindings(Pattern.compile("B"), true, IndexFilter.ALL, new NullProgressMonitor());
			if(Bs.length==1)
				successes++;

			pdom.releaseReadLock();
		}
		
		String msg = "Same indexer on same project produces different results."
			+ "Failure rate of "+(noTrials-successes)+" failures in "+noTrials+" tests";
		assertTrue("Non-race-condition failure", successes!=0);
		assertTrue(msg, successes == noTrials);
	}
}
