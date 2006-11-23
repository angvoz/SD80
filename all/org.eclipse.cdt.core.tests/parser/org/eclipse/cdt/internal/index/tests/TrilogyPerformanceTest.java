/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * aftodo - it would be nice to have this as a real performance test
 * 
 * n.b. this is intentionally not added to any test suite at the moment
 */
public class TrilogyPerformanceTest extends IndexTestBase {
	ICProject cproject;

	public TrilogyPerformanceTest() {
		super("TrilogyPerformance");
	}

	protected void setUp() throws Exception {
		Bundle b = CTestPlugin.getDefault().getBundle();
		if (cproject == null) {
			cproject= createProject(true, "resources/indexTests/trilogy");
		}
	}

	protected void tearDown() throws Exception {
		cproject.getProject().delete(true, new NullProgressMonitor());
	}

	// you must have the Windows SDK installed and the INETSDK env var setup
	public void testIndexTrilogyPerformanceTimes() throws CoreException {
		if(Platform.getOS().equals(Platform.OS_WIN32)) { 
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
			TestScannerProvider.sIncludes = new String[]{EnvironmentReader.getEnvVar("INETSDK")+"\\Include"};
			try {
				CCoreInternals.getPDOMManager().setIndexAllFiles(cproject, true);
				long start = System.currentTimeMillis();
				CCorePlugin.getPDOMManager().reindex(cproject);
				assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
				System.out.println("Took: "+(System.currentTimeMillis() - start));
				IIndex index= CCorePlugin.getIndexManager().getIndex(cproject);
				IBinding[] binding = index.findBindings(Pattern.compile("IXMLElementCollection"), false, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, binding.length);
			} finally {
				TestScannerProvider.sIncludes = null;
			}
		}
	}
}