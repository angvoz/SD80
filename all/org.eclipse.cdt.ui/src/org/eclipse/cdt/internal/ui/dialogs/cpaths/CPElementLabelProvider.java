/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

class CPElementLabelProvider extends LabelProvider {

	private String fNewLabel, fCreateLabel;
	private ImageDescriptor fIncludeIcon, fMacroIcon, fLibWSrcIcon, fLibIcon;
	private ImageDescriptor fFolderImage, fOutputImage, fProjectImage, fContainerImage;
	private boolean bShowExported;
	private ImageDescriptorRegistry fRegistry;

	public CPElementLabelProvider() {
		this(true);
	}
	
	public CPElementLabelProvider(boolean showExported) {
		fNewLabel = CPathEntryMessages.getString("CPElementLabelProvider.new"); //$NON-NLS-1$
		fCreateLabel = CPathEntryMessages.getString("CPElementLabelProvider.willbecreated"); //$NON-NLS-1$
		fRegistry = CUIPlugin.getImageDescriptorRegistry();

		fLibIcon = CPluginImages.DESC_OBJS_ARCHIVE;
		fLibWSrcIcon = CPluginImages.DESC_OBJS_ARCHIVE_WSRC;
		fIncludeIcon = CPluginImages.DESC_OBJS_INCLUDES_FOLDER;
		fMacroIcon = CPluginImages.DESC_OBJS_MACRO;
		fFolderImage = CPluginImages.DESC_OBJS_SOURCE_ROOT;
		fOutputImage = CPluginImages.DESC_OBJS_CONTAINER;
		fContainerImage = CPluginImages.DESC_OBJS_LIBRARY;

		IWorkbench workbench = CUIPlugin.getDefault().getWorkbench();

		fProjectImage = workbench.getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		bShowExported = showExported;
	}

	public String getText(Object element) {
		if (element instanceof CPElement) {
			return getCPElementText((CPElement)element);
		} else if (element instanceof CPElementAttribute) {
			return getCPElementAttributeText((CPElementAttribute)element);
		} else if (element instanceof IPathEntry) {
			return getCPElementText(CPElement.createFromExisting((IPathEntry)element, null));
		} else if (element instanceof CPElementGroup) {
			return (getCPContainerGroupText((CPElementGroup)element));
		}
		return super.getText(element);
	}

	private String getCPContainerGroupText(CPElementGroup group) {
		switch (group.getEntryType()) {
			case IPathEntry.CDT_INCLUDE :
				return CPathEntryMessages.getString("CPElementLabelProvider.Includes"); //$NON-NLS-1$
			case IPathEntry.CDT_MACRO :
				return CPathEntryMessages.getString("CPElementLabelProvider.PreprocessorSymbols"); //$NON-NLS-1$
			case IPathEntry.CDT_LIBRARY :
				return CPathEntryMessages.getString("CPElementLabelProvider.Libraries"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	public String getCPElementAttributeText(CPElementAttribute attrib) {
		String notAvailable = CPathEntryMessages.getString("CPElementLabelProvider.none"); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		String key = attrib.getKey();
		if (key.equals(CPElement.SOURCEATTACHMENT)) {
			buf.append(CPathEntryMessages.getString("CPElementLabelProvider.source_attachment.label")); //$NON-NLS-1$
			IPath path = (IPath)attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(getPathString(path, path.getDevice() != null));
			} else {
				buf.append(notAvailable);
			}
		} else if (key.equals(CPElement.SOURCEATTACHMENTROOT)) {
			buf.append(CPathEntryMessages.getString("CPElementLabelProvider.source_attachment_root.label")); //$NON-NLS-1$
			IPath path = (IPath)attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(path.toString());
			} else {
				buf.append(notAvailable);
			}
		}
		if (key.equals(CPElement.EXCLUSION)) {
			buf.append(CPathEntryMessages.getString("CPElementLabelProvider.exclusion_filter.label")); //$NON-NLS-1$
			IPath[] patterns = (IPath[])attrib.getValue();
			if (patterns != null && patterns.length > 0) {
				for (int i = 0; i < patterns.length; i++) {
					if (i > 0) {
						buf.append(CPathEntryMessages.getString("CPElementLabelProvider.exclusion_filter_separator")); //$NON-NLS-1$
					}
					buf.append(patterns[i].toString());
				}
			} else {
				buf.append(notAvailable);
			}
		}
		return buf.toString();
	}

	public String getCPElementText(CPElement cpentry) {
		IPath path = cpentry.getPath();
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_LIBRARY :
				{
					StringBuffer str = new StringBuffer( ((IPath)cpentry.getAttribute(CPElement.LIBRARY)).toOSString());
					addBaseString(cpentry, str);
					addExport(cpentry, str);
					return str.toString();
				}
			case IPathEntry.CDT_PROJECT :
				return path.lastSegment();
			case IPathEntry.CDT_INCLUDE :
				{
					IPath incPath = ((IPath)cpentry.getAttribute(CPElement.INCLUDE));
					StringBuffer str = new StringBuffer(incPath.toOSString());
					addBaseString(cpentry, str);
					addExport(cpentry, str);
					return str.toString();
				}
			case IPathEntry.CDT_MACRO :
				{
					StringBuffer str = new StringBuffer((String)cpentry.getAttribute(CPElement.MACRO_NAME) + "=" //$NON-NLS-1$
							+ (String)cpentry.getAttribute(CPElement.MACRO_VALUE));
					addBaseString(cpentry, str);
					addExport(cpentry, str);
					return str.toString();
				}
			case IPathEntry.CDT_CONTAINER :
				{
					StringBuffer str = new StringBuffer(path.toString());
					try {
						IPathEntryContainer container = CoreModel.getPathEntryContainer(cpentry.getPath(), cpentry.getCProject());
						if (container != null) {
							str.setLength(0);
							str.append(container.getDescription());
						}
					} catch (CModelException e) {
					}
					addExport(cpentry, str);
					return str.toString();
				}
			case IPathEntry.CDT_SOURCE :
			case IPathEntry.CDT_OUTPUT :
				{
					StringBuffer buf = new StringBuffer(path.makeRelative().toString());
					IResource resource = cpentry.getResource();
					if (resource != null && !resource.exists()) {
						buf.append(' ');
						if (cpentry.isMissing()) {
							buf.append(fCreateLabel);
						} else {
							buf.append(fNewLabel);
						}
					}
					return buf.toString();
				}
			default :
		// pass
		}
		return CPathEntryMessages.getString("CPElementLabelProvider.unknown_element.label"); //$NON-NLS-1$
	}
	private void addExport(CPElement cpentry, StringBuffer str) {
		if (bShowExported && cpentry.isExported()) {
			str.append(' ');
			str.append(CPathEntryMessages.getString("CPElementLabelProvider.export.label")); //$NON-NLS-1$
		}
	}

	private void addBaseString(CPElement cpentry, StringBuffer str) {
		IPath baseRef = (IPath)cpentry.getAttribute(CPElement.BASE_REF);
		if (!baseRef.isEmpty()) {
			str.append(" - ("); //$NON-NLS-1$
			if (baseRef.isAbsolute()) {
				//				str.append("From project ");
				str.append(baseRef);
			} else {
				//				str.append("From contribution ");
				IPathEntryContainer container;
				try {
					container = CoreModel.getPathEntryContainer(baseRef, cpentry.getCProject());
					if (container != null) {
						str.append(container.getDescription());
					}
				} catch (CModelException e1) {
				}
			}
			str.append(')');
		} else {
			IPath path = (IPath)cpentry.getAttribute(CPElement.BASE);
			if (!path.isEmpty()) {
				if (!path.hasTrailingSeparator()) {
					path = path.addTrailingSeparator();
				}
				str.insert(0, path.toOSString());
			}
		}

	}

	private String getPathString(IPath path, boolean isExternal) {
		//		if (ArchiveFileFilter.isArchivePath(path)) {
		//			IPath appendedPath = path.removeLastSegments(1);
		//			String appended = isExternal ? appendedPath.toOSString() :
		// appendedPath.makeRelative().toString();
		//			return
		// CPathEntryMessages.getFormattedString("CPListLabelProvider.twopart",
		// //$NON-NLS-1$
		//					new String[] { path.lastSegment(), appended});
		//		} else {
		return isExternal ? path.toOSString() : path.makeRelative().toString();
		//		}
	}

	private ImageDescriptor getCPElementBaseImage(CPElement cpentry) {
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_OUTPUT :
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				} else {
					return fOutputImage;
				}
			case IPathEntry.CDT_SOURCE :
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				} else {
					return fFolderImage;
				}
			case IPathEntry.CDT_LIBRARY :
				IPath path = (IPath)cpentry.getAttribute(CPElement.SOURCEATTACHMENT);
				if (path == null || path.isEmpty()) {
					return fLibIcon;
				}
				return fLibWSrcIcon;
			case IPathEntry.CDT_PROJECT :
				return fProjectImage;
			case IPathEntry.CDT_CONTAINER :
				return fContainerImage;
			case IPathEntry.CDT_INCLUDE :
				return fIncludeIcon;
			case IPathEntry.CDT_MACRO :
				return fMacroIcon;
			default :
				return null;
		}
	}

	private static final Point SMALL_SIZE = new Point(16, 16);

	public Image getImage(Object element) {
		if (element instanceof CPElement) {
			CPElement cpentry = (CPElement)element;
			ImageDescriptor imageDescriptor = getCPElementBaseImage(cpentry);
			if (imageDescriptor != null) {
				if (cpentry.isMissing()) {
					imageDescriptor = new CElementImageDescriptor(imageDescriptor, CElementImageDescriptor.WARNING, SMALL_SIZE);
				}
				return fRegistry.get(imageDescriptor);
			}
		} else if (element instanceof CPElementAttribute) {
			String key = ((CPElementAttribute)element).getKey();
			if (key.equals(CPElement.SOURCEATTACHMENT)) {
				return fRegistry.get(CPluginImages.DESC_OBJS_SOURCE_ATTACH_ATTRIB);
			} else if (key.equals(CPElement.EXCLUSION)) {
				return CPluginImages.get(CPluginImages.IMG_OBJS_EXCLUDSION_FILTER_ATTRIB);
			}
		} else if (element instanceof IPathEntry) {
			return getImage(CPElement.createFromExisting((IPathEntry)element, null));
		} else if (element instanceof CPElementGroup) {
			switch ( ((CPElementGroup)element).getEntryType()) {
				case IPathEntry.CDT_INCLUDE :
					return CPluginImages.get(CPluginImages.IMG_OBJS_INCLUDES_CONTAINER);
				case IPathEntry.CDT_MACRO :
					return fRegistry.get(fMacroIcon);
				case IPathEntry.CDT_LIBRARY :
					return CPluginImages.get(CPluginImages.IMG_OBJS_LIBRARY);
			}
		}
		return null;
	}
}