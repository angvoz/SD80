package org.eclipse.cdt.internal.ui.browser.cbrowsing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;

public class CBrowsingPerspectiveFactory implements IPerspectiveFactory {
	
	/*
	 * XXX: This is a workaround for: http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static ICElement fgCElementFromAction;
	
	/**
	 * Constructs a new Default layout engine.
	 */
	public CBrowsingPerspectiveFactory() {
		super();
	}

	/**
	 * @see IPerspectiveFactory#createInitialLayout
	 */
	public void createCViewInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder1.addView(CUIPlugin.CVIEW_ID);
		folder1.addView(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.FOLDER_ACTION_SET_ID);
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(CUIPlugin.CVIEW_ID);
		layout.addShowInPart(IPageLayout.ID_RES_NAV);

		// new actions - C project creation wizard
		layout.addNewWizardShortcut(CUIPlugin.CLASS_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FILE_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FOLDER_WIZARD_ID);
	}

	public void createInitialLayout(IPageLayout layout) {
		if (stackBrowsingViewsVertically())
			createVerticalLayout(layout);
		else
			createHorizontalLayout(layout);
		
		// action sets
		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.FOLDER_ACTION_SET_ID);
//		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - java
//		layout.addShowViewShortcut(CUIPlugin.ID_TYPE_HIERARCHY);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(CUIPlugin.ID_PROJECTS_VIEW);
		layout.addShowViewShortcut(CUIPlugin.ID_NAMESPACES_VIEW);
		layout.addShowViewShortcut(CUIPlugin.ID_TYPES_VIEW);
		layout.addShowViewShortcut(CUIPlugin.ID_MEMBERS_VIEW);
//		layout.addShowViewShortcut(CUIPlugin.ID_SOURCE_VIEW);
//		layout.addShowViewShortcut(CUIPlugin.ID_JAVADOC_VIEW);

		// views - search		
		layout.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);

		// views - debugging
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		
		// new actions - C project creation wizard
		layout.addNewWizardShortcut(CUIPlugin.CLASS_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FILE_WIZARD_ID);
		layout.addNewWizardShortcut(CUIPlugin.FOLDER_WIZARD_ID);
	}

	private void createVerticalLayout(IPageLayout layout) {
		String relativePartId= IPageLayout.ID_EDITOR_AREA;
		int relativePos= IPageLayout.LEFT;
		
		IPlaceholderFolderLayout placeHolderLeft= layout.createPlaceholderFolder("left", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
//		placeHolderLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY); 
		placeHolderLeft.addPlaceholder(IPageLayout.ID_OUTLINE);
		placeHolderLeft.addPlaceholder(CUIPlugin.CVIEW_ID);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		if (shouldShowProjectsView()) {
			layout.addView(CUIPlugin.ID_PROJECTS_VIEW, IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA);
			relativePartId= CUIPlugin.ID_PROJECTS_VIEW;
			relativePos= IPageLayout.BOTTOM;
		}				
		if (shouldShowNamespacesView()) {
			layout.addView(CUIPlugin.ID_NAMESPACES_VIEW, relativePos, (float)0.25, relativePartId);
			relativePartId= CUIPlugin.ID_NAMESPACES_VIEW;
			relativePos= IPageLayout.BOTTOM;
		}
		layout.addView(CUIPlugin.ID_TYPES_VIEW, relativePos, (float)0.33, relativePartId);
		layout.addView(CUIPlugin.ID_MEMBERS_VIEW, IPageLayout.BOTTOM, (float)0.50, CUIPlugin.ID_TYPES_VIEW);
		
		IPlaceholderFolderLayout placeHolderBottom= layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderBottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		placeHolderBottom.addPlaceholder(SearchUI.SEARCH_RESULT_VIEW_ID);
		placeHolderBottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		placeHolderBottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);		
//		placeHolderBottom.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
//		placeHolderBottom.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
	}

	private void createHorizontalLayout(IPageLayout layout) {
		String relativePartId= IPageLayout.ID_EDITOR_AREA;		
		int relativePos= IPageLayout.TOP;
		
		if (shouldShowProjectsView()) {
			layout.addView(CUIPlugin.ID_PROJECTS_VIEW, IPageLayout.TOP, (float)0.25, IPageLayout.ID_EDITOR_AREA);
			relativePartId= CUIPlugin.ID_PROJECTS_VIEW;
			relativePos= IPageLayout.RIGHT;
		}
		if (shouldShowNamespacesView()) {
			layout.addView(CUIPlugin.ID_NAMESPACES_VIEW, relativePos, (float)0.25, relativePartId);
			relativePartId= CUIPlugin.ID_NAMESPACES_VIEW;
			relativePos= IPageLayout.RIGHT;
		}
		layout.addView(CUIPlugin.ID_TYPES_VIEW, relativePos, (float)0.33, relativePartId);
		layout.addView(CUIPlugin.ID_MEMBERS_VIEW, IPageLayout.RIGHT, (float)0.50, CUIPlugin.ID_TYPES_VIEW);
		
		IPlaceholderFolderLayout placeHolderLeft= layout.createPlaceholderFolder("left", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
//		placeHolderLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY); 
		placeHolderLeft.addPlaceholder(IPageLayout.ID_OUTLINE);
		placeHolderLeft.addPlaceholder(CUIPlugin.CVIEW_ID);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		IPlaceholderFolderLayout placeHolderBottom= layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderBottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		placeHolderBottom.addPlaceholder(SearchUI.SEARCH_RESULT_VIEW_ID);
		placeHolderBottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		placeHolderBottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);		
//		placeHolderBottom.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
//		placeHolderBottom.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
	}
	
	private boolean shouldShowProjectsView() {
		return true;
//		RETURN FGCELEMENTFROMACTION == NULL || FGCELEMENTFROMACTION.GETELEMENTTYPE() == ICELEMENT.C_MODEL;
	}

	private boolean shouldShowNamespacesView() {
		return true;
//		if (fgCElementFromAction == null)
//			return true;
//		int type= fgCElementFromAction.getElementType();
//		return type == ICElement.C_MODEL || type == ICElement.C_PROJECT;
////		return type == ICElement.C_MODEL || type == ICElement.C_PROJECT || type == ICElement.PACKAGE_FRAGMENT_ROOT;
	}

	private boolean stackBrowsingViewsVertically() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.BROWSING_STACK_VERTICALLY);
	}

	/*
	 * XXX: This is a workaround for: http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static void setInputFromAction(IAdaptable input) {
		if (input instanceof ICElement)
			fgCElementFromAction= (ICElement)input;
		else
			fgCElementFromAction= null;
	}
}


