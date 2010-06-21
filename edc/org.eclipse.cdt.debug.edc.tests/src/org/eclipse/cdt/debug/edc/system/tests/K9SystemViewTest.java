/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.system.tests;

import java.util.List;

import org.eclipse.cdt.debug.edc.internal.ui.views.SystemDMContainer;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemVMContainer;
import org.eclipse.cdt.debug.edc.system.tests.K9SystemView.K9ViewModel;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class K9SystemViewTest {

	K9SystemView k9View;
	
	@Before
	public void setUp() throws Exception {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// Open the K9 System View
		        IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		        try {
					k9View = (K9SystemView)page.showView(K9SystemView.VIEW_ID);
				} catch (PartInitException e) {
					Assert.fail(e.getMessage());
				}
		        Assert.assertNotNull("Can't open K9 System View", k9View != null);
			}
		});
	}

	@Test
	public void testCreatePartControl() {
		List<TreeModelViewer> k9Viewers = k9View.getViewers();
		List<SystemVMContainer> rootContainers = k9View.getViewModel().getRootContainers();
		for (int i = 0; i < rootContainers.size(); i++) {
			Assert.assertEquals(rootContainers.get(i), k9Viewers.get(i).getInput());
		}

	}

	@Test
	public void testRefresh() throws InterruptedException {
		SystemDMContainer oldDogs = k9View.getAllDogs();
		k9View.getRefreshJob().schedule();
		k9View.getRefreshJob().join();
		Assert.assertNotSame(oldDogs, k9View.getAllDogs());
	}

	@Test
	public void testGetOverviewVMContainer() {
		 Assert.assertNotNull(((K9ViewModel)k9View.getViewModel()).getOverviewVMContainer());
	}

	@Test
	public void testGetBreedsVMContainer() {
		 Assert.assertNotNull(((K9ViewModel)k9View.getViewModel()).getBreedsVMContainer());
	}

	@Test
	public void testGetDogsVMContainer() {
		 Assert.assertNotNull(((K9ViewModel)k9View.getViewModel()).getDogsVMContainer());
	}

	@Test
	public void testGetToysVMContainer() {
		 Assert.assertNotNull(((K9ViewModel)k9View.getViewModel()).getToysVMContainer());
	}
	
	@Test
	public void testGetPresentationContext() {
		 Assert.assertNotNull(k9View.getPresentationContext());
	}

	@Test
	public void testSetRefreshInterval() {
		int interval = k9View.getRefreshInterval();
		k9View.setRefreshInterval(8000);
		Assert.assertEquals(8000, k9View.getRefreshInterval());
		k9View.setRefreshInterval(interval);
	}

	@Test
	public void testGetRefreshInterval() {
		Assert.assertEquals(5000, k9View.getRefreshInterval());
	}

	@Test
	public void testGetRefreshjob() {
		 Assert.assertNotNull(k9View.getRefreshJob());
	}

	@Test
	public void testGetFilterMatcher() {
		StringMatcher matcher = k9View.getFilterMatcher();
		Assert.assertTrue(matcher.match("Indy"));
	}

	@Test
	public void testCreateRefreshAction() {
		IActionBars bars = k9View.getViewSite().getActionBars();
		Assert.assertNotNull(bars.getToolBarManager().find("SYSTEM_REFRESH"));
	}

	@Test
	public void testContributeToActionBars() {
		IActionBars bars = k9View.getViewSite().getActionBars();
		Assert.assertNotNull(bars.getMenuManager().find("SYSTEM_REFRESH"));
	}

}
