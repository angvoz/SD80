/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 18, 2003
 */
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultLabelProvider extends LabelProvider {

	public static final int SHOW_NAME_ONLY		   = 0; 
	public static final int SHOW_ELEMENT_CONTAINER = 1;
	public static final int SHOW_CONTAINER_ELEMENT = 2;
	public static final int SHOW_PATH			   = 3;//default
	
	public static final String POTENTIAL_MATCH = CSearchMessages.getString("CSearchResultLabelProvider.potentialMatch"); //$NON-NLS-1$

	public CSearchResultLabelProvider(){
		_sortOrder = SHOW_PATH;
	}
	
	public Image getImage( Object element ) {
		IMatch match = null;
		
		if( element instanceof ISearchResultViewEntry ){
			ISearchResultViewEntry viewEntry = (ISearchResultViewEntry)element;
			IMarker marker = viewEntry.getSelectedMarker();
			try {
				match = (IMatch) marker.getAttribute( CSearchResultCollector.IMATCH );
			} catch (CoreException e) {
				return null;
			}
		} else if ( element instanceof IMatch ){
			match = (IMatch) element;
		}
		
		if( match == null )
			return null;
			
		ImageDescriptor imageDescriptor = null;
		
		switch( match.getElementType() ){
			case ICElement.C_CLASS:			imageDescriptor = CPluginImages.DESC_OBJS_CLASS;		break;
			case ICElement.C_STRUCT:		imageDescriptor = CPluginImages.DESC_OBJS_STRUCT;		break;
			case ICElement.C_UNION:			imageDescriptor = CPluginImages.DESC_OBJS_UNION;		break;
			case ICElement.C_NAMESPACE:		imageDescriptor = CPluginImages.DESC_OBJS_CONTAINER;	break;
			case ICElement.C_ENUMERATION:	imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATION;	break;
			case ICElement.C_MACRO:			imageDescriptor = CPluginImages.DESC_OBJS_MACRO;		break;
			case ICElement.C_FUNCTION:		imageDescriptor = CPluginImages.DESC_OBJS_FUNCTION;		break;
			case ICElement.C_VARIABLE:		imageDescriptor = CPluginImages.DESC_OBJS_FIELD;		break;
			case ICElement.C_ENUMERATOR:	imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATOR;	break;
			case ICElement.C_TYPEDEF:		imageDescriptor = CPluginImages.DESC_OBJS_TYPEDEF;		break;
			case ICElement.C_FIELD:		
			{
				switch( match.getVisibility() ){
					case ICElement.CPP_PUBLIC:	imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_FIELD;		break;
					case ICElement.CPP_PRIVATE:	imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_FIELD;	break;
					default:					imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_FIELD;	break;
				}
				break;
			}
			case ICElement.C_METHOD:
			{
				switch( match.getVisibility() ){
					case ICElement.CPP_PUBLIC:	imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_METHOD;	break;
					case ICElement.CPP_PRIVATE:	imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_METHOD;	break;
					default:					imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_METHOD;	break;
				}
				break;
			}
		}
		
		int flags = 0;
		if( match.isStatic()   ) flags |= CElementImageDescriptor.STATIC;
		if( match.isConst()    ) flags |= CElementImageDescriptor.CONSTANT;
		if( match.isVolatile() ) flags |= CElementImageDescriptor.VOLATILE;

		imageDescriptor = new CElementImageDescriptor( imageDescriptor, flags, SMALL_SIZE );

		Image image = CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor );
				
		return image;
	}
	
	public String getText( Object element ) {
		IMatch match = null;
		
		if( element instanceof ISearchResultViewEntry ){
			ISearchResultViewEntry viewEntry = (ISearchResultViewEntry) element;
		
			IMarker marker = viewEntry.getSelectedMarker();
		
			try {
				match = (IMatch) marker.getAttribute(CSearchResultCollector.IMATCH);
			} catch (CoreException e) {
				return null;
			}
		} else if( element instanceof IMatch ){
			match = (IMatch) element;
		}
		
		if( match == null )
			return null;
		
		IResource resource = match.getResource();
		
		String result = null;
		String path = (resource != null ) ? resource.getFullPath().toString() : "";
		
		switch( getOrder() ){
			case SHOW_NAME_ONLY:
				result = match.getName();
			case SHOW_ELEMENT_CONTAINER:
				if( !match.getParentName().equals("") )
					result = match.getName() + " - " + match.getParentName() + " ( " + path + " )";
				else
					result = match.getName() + " ( " + path + " )";
						
				break;
			case SHOW_PATH:
				result = path + " - " + match.getParentName()+ "::" + match.getName();
				break;				
			case SHOW_CONTAINER_ELEMENT:
				result = match.getParentName() + "::" + match.getName() + " ( " + path + " )";
				break;
		}
		
		return result;
	}
	
	public int getOrder(){
		return _sortOrder;
	}
	public void setOrder(int orderFlag) {
		_sortOrder = orderFlag;
	}
	
	private int _sortOrder;
	private int _textFlags;
	private int _imageFlags;
	
	private static final Point SMALL_SIZE= new Point(16, 16);
	
}