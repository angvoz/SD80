/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.ui.CUIPlugin;

public class CUILabelProvider extends LabelProvider implements IColorProvider {
	
	protected CElementImageProvider fImageLabelProvider;
	protected StorageLabelProvider fStorageLabelProvider;
	
	private ArrayList<ILabelDecorator> fLabelDecorators;

	private int fImageFlags;
	private int fTextFlags;
	private Color fInactiveColor;
	private Color fDefaultColor;

	/**
	 * Creates a new label provider with default flags.
	 */
	public CUILabelProvider() {
		this(CElementBaseLabels.M_PARAMETER_TYPES, CElementImageProvider.OVERLAY_ICONS);
	}

	/**
	 * @param textFlags Flags defined in <code>CElementBaseLabels</code>.
	 * @param imageFlags Flags defined in <code>CElementImageProvider</code>.
	 */
	public CUILabelProvider(int textFlags, int imageFlags) {
		fImageLabelProvider= new CElementImageProvider();
		fLabelDecorators= null; 
		
		fStorageLabelProvider= new StorageLabelProvider();
		fImageFlags= imageFlags;
		fTextFlags= textFlags;
	}
	
	/**
	 * Adds a decorator to the label provider
	 */
	public void addLabelDecorator(ILabelDecorator decorator) {
		if (fLabelDecorators == null) {
			fLabelDecorators= new ArrayList<ILabelDecorator>(2);
		}
		fLabelDecorators.add(decorator);
	}
	
	/**
	 * Sets the textFlags.
	 * @param textFlags The textFlags to set
	 */
	public final void setTextFlags(int textFlags) {
		fTextFlags= textFlags;
	}

	/**
	 * Sets the imageFlags 
	 * @param imageFlags The imageFlags to set
	 */
	public final void setImageFlags(int imageFlags) {
		fImageFlags= imageFlags;
	}
	
	/**
	 * Gets the image flags.
	 * Can be overwriten by super classes.
	 * @return Returns a int
	 */
	public final int getImageFlags() {
		return fImageFlags;
	}

	/**
	 * Gets the text flags.
	 * @return Returns a int
	 */
	public final int getTextFlags() {
		return fTextFlags;
	}
	
	/**
	 * Evaluates the image flags for a element.
	 * Can be overwriten by super classes.
	 * @return Returns a int
	 */
	protected int evaluateImageFlags(Object element) {
		return getImageFlags();
	}

	/**
	 * Evaluates the text flags for a element. Can be overwriten by super classes.
	 * @return Returns a int
	 */
	protected int evaluateTextFlags(Object element) {
		return getTextFlags();
	}

	protected Image decorateImage(Image image, Object element) {
		if (fLabelDecorators != null && image != null) {
			for (int i= 0; i < fLabelDecorators.size(); i++) {
				ILabelDecorator decorator= fLabelDecorators.get(i);
				image= decorator.decorateImage(image, element);
			}
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see ILabelProvider#getImage
	 */
	@Override
	public Image getImage(Object element) {
		Image result= fImageLabelProvider.getImageLabel(element, evaluateImageFlags(element));
		if (result == null && (element instanceof IStorage)) {
			result= fStorageLabelProvider.getImage(element);
		}
		
		return decorateImage(result, element);
	}

	protected String decorateText(String text, Object element) {
		if (fLabelDecorators != null && text.length() > 0) {
			for (int i= 0; i < fLabelDecorators.size(); i++) {
				ILabelDecorator decorator= fLabelDecorators.get(i);
				text= decorator.decorateText(text, element);
			}
		}	
		return text;
	}


	/* (non-Javadoc)
	 * @see ILabelProvider#getText
	 */
	@Override
	public String getText(Object element) {
		String result= CElementLabels.getTextLabel(element, evaluateTextFlags(element));
		if (result.length() == 0 && (element instanceof IStorage)) {
			result= fStorageLabelProvider.getText(element);
		}
		
		return decorateText(result, element);
	}

	/* (non-Javadoc)
	 * @see IBaseLabelProvider#dispose
	 */
	@Override
	public void dispose() {
		if (fLabelDecorators != null) {
			for (int i= 0; i < fLabelDecorators.size(); i++) {
				ILabelDecorator decorator= fLabelDecorators.get(i);
				decorator.dispose();
			}
			fLabelDecorators= null;
		}
		fStorageLabelProvider.dispose();
		fImageLabelProvider.dispose();
	}
	
	/* (non-Javadoc)
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
		if (fLabelDecorators != null) {
			for (int i= 0; i < fLabelDecorators.size(); i++) {
				ILabelDecorator decorator= fLabelDecorators.get(i);
				decorator.addListener(listener);
			}
		}
		super.addListener(listener);	
	}

	/* (non-Javadoc)
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;	
	}

	/* (non-Javadoc)
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		if (fLabelDecorators != null) {
			for (int i= 0; i < fLabelDecorators.size(); i++) {
				ILabelDecorator decorator= fLabelDecorators.get(i);
				decorator.removeListener(listener);
			}
		}
		super.removeListener(listener);	
	}
	
	public static ILabelDecorator[] getDecorators(boolean errortick, ILabelDecorator extra) {
		if (errortick) {
			if (extra == null) {
				return new ILabelDecorator[] {};
			}
			return new ILabelDecorator[] { extra };
		}
		if (extra != null) {
			return new ILabelDecorator[] { extra };
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof ISourceReference) {
			ISourceReference sref= (ISourceReference)element;
			if (!sref.isActive()) {
				if (fInactiveColor == null && Display.getCurrent() != null) {
					fInactiveColor= CUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
					fDefaultColor= CUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
				}
				return fInactiveColor;
			}
		}
		return fDefaultColor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}	

}
