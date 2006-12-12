/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupManager;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Editor that lets you select a replacement for the missing source file
 * and modifies the source locator accordingly.
 *
 */
public class CSourceNotFoundEditor extends CommonSourceNotFoundEditor {

	public final String foundMappingsContainerName = "Found Mappings"; //$NON-NLS-1$
	
	private String missingFile;
	private ILaunch launch;
	private IDebugElement context;

	public CSourceNotFoundEditor() {
		super();
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
			super.init(site, input);
			Object artifact = this.getArtifact();
			if (artifact instanceof CSourceNotFoundElement)
			{
				CSourceNotFoundElement element = (CSourceNotFoundElement) artifact;
				missingFile = element.getFile();
				launch = element.getLaunch();
				context = element.getElement();
			}
			else
				missingFile = ""; //$NON-NLS-1$
	}

	protected String getText() {
		if (missingFile.length() > 0) {
			return MessageFormat.format(SourceLookupUIMessages.getString( "CSourceNotFoundEditor.0" ), new String[] { missingFile });  //$NON-NLS-1$
		}
		return super.getText();
	}

	protected void createButtons(Composite parent) {
		if (missingFile.length() > 0) {
			GridData data;
			Button button = new Button(parent, SWT.PUSH);
			data = new GridData();
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
			button.setLayoutData(data);
			button.setText(SourceLookupUIMessages.getString( "CSourceNotFoundEditor.1" )); //$NON-NLS-1$
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					locateFile();
				}
			});
		}
		super.createButtons(parent);
	}

	private void addSourceMapping(IPath missingPath, IPath newSourcePath) throws CoreException {
		String memento = null;
		String type = null;

		ILaunchConfigurationWorkingCopy configuration = launch.getLaunchConfiguration().getWorkingCopy();
		memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
		if (type == null) {
			type = configuration.getType().getSourceLocatorId();
		}
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ISourceLocator locator = launchManager.newSourceLocator(type);
		if (locator instanceof AbstractSourceLookupDirector) {
			AbstractSourceLookupDirector director = (AbstractSourceLookupDirector) locator;
			if (memento == null) {
				director.initializeDefaults(configuration);
			} else {
				director.initializeFromMemento(memento, configuration);
			}

			ArrayList containerList = new ArrayList(Arrays.asList(director.getSourceContainers()));

			boolean hasFoundMappings = false;

			MappingSourceContainer foundMappings = null;
			
			for (Iterator iter = containerList.iterator(); iter.hasNext() && !hasFoundMappings;) {
				ISourceContainer container = (ISourceContainer) iter.next();
				if (container instanceof MappingSourceContainer)
				{
					hasFoundMappings = container.getName().equals(foundMappingsContainerName);
					if (hasFoundMappings)
						foundMappings = (MappingSourceContainer) container;
				}
			}

			if (!hasFoundMappings) {
				foundMappings = new MappingSourceContainer(foundMappingsContainerName);
				foundMappings.init(director);
				containerList.add(foundMappings);
				director.setSourceContainers((ISourceContainer[]) containerList.toArray(new ISourceContainer[containerList.size()]));
			}
			
			foundMappings.addMapEntry(new MapEntrySourceContainer(missingPath, newSourcePath));
			configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, director.getMemento());
			configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, director.getId());
			configuration.doSave();

		}
	}
	
	protected void locateFile() {
		FileDialog dialog = new FileDialog(getEditorSite().getShell(), SWT.NONE);
		Path missingPath = new Path(missingFile);
		dialog.setFilterNames(new String[] {SourceLookupUIMessages.getString("CSourceNotFoundEditor.2")}); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {"*." + missingPath.getFileExtension()}); //$NON-NLS-1$
		String res = dialog.open();
		if (res != null) {
			Path newPath = new Path(res);
			
			if (newPath.lastSegment().equalsIgnoreCase(missingPath.lastSegment()))
			{
				
				if (missingPath.segmentCount() > 1)
				{
					int missingPathSegCount = missingPath.segmentCount() - 2;
					int newPathSegCount = newPath.segmentCount() - 2;
					while (missingPathSegCount >= 0 && newPathSegCount >= 0)
					{
						if (!newPath.segment(newPathSegCount).equalsIgnoreCase(missingPath.segment(missingPathSegCount)))
							break;
						newPathSegCount--;
						missingPathSegCount--;
					}
					IPath compPath = missingPath.removeLastSegments(missingPath.segmentCount() - missingPathSegCount - 1);
					IPath newSourcePath = newPath.removeLastSegments(newPath.segmentCount() - newPathSegCount - 1);
					try {
						addSourceMapping(compPath, newSourcePath);
					} catch (CoreException e) {}					
					
				}
				
				IWorkbenchPage page = getEditorSite().getPage();
				SourceLookupManager.getDefault().displaySource(context, page, true);
				closeEditor();
				
			}
		}
	}

}
