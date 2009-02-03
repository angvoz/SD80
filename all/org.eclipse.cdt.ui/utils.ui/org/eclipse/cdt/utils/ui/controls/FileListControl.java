/*******************************************************************************
 * Copyright (c) 2004, 2009 BitMethods Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BitMethods Inc - initial API and implementation
 *     Sascha Radike <sradike@ejectlag.com> - Support for workspace browsing and small improvements
 *******************************************************************************/
package org.eclipse.cdt.utils.ui.controls;


import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.ui.CDTUIImages;
import org.eclipse.cdt.ui.newui.CDTStatusInfo;
import org.eclipse.cdt.ui.newui.TypedCDTViewerFilter;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

/**
 * Instances of this class allow the user to add,remove, delete, moveup and movedown
 * the items in the list control.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */

public class FileListControl {

	// Browse type values
	//  (copied from IOption to fix the dependency on MBS ui plugins issue)
	private static final int BROWSE_NONE = 0;
	private static final int BROWSE_FILE = 1;
	private static final int BROWSE_DIR = 2;

	/**
	 * Multi-purpose dialog to prompt the user for a value, path, or file.
	 * 
	 * @since 2.0
	 */
	class SelectPathInputDialog extends InputDialog {
		private int type;
		/* True if user successfully set the text value by a browse dialog */
		private boolean fSetByBrowseDialog = false;
		

		/**
		 * @param parentShell
		 * @param dialogTitle
		 * @param dialogMessage
		 * @param initialValue
		 * @param validator
		 * @param browseType
		 */
		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator, int browseType) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			this.type = browseType;
		}
		
		/**
		 * Returns true if the value has been set by a browse dialog. 
		 */
		public boolean isValueSetByBrowse() {
			return fSetByBrowseDialog;
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			
			if (((type == BROWSE_DIR) || (type == BROWSE_FILE)
					) && (fWorkspaceSupport)) {

				/* Browse button for workspace folders/files */
				final Button workspaceButton = createButton(parent, 3, WORKSPACEBUTTON_NAME, false);
				workspaceButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent ev) {
						/* Before opening the browse dialog we try to convert the current
						 * path text to a valid workspace resource, so we can set it
						 * as initial selection in the dialog.
						 * 
						 * First we remove all double-quotes. Then the build macro provider
						 * will resolve all macros/variables (like workspace_loc, ...).
						 * 
						 * If the workspace location path is a prefix of our resolved path,
						 * we will remove that part and finally get a full path relative to the
						 * workspace. We can use that path to set the initially selected resource.
						 */
						
						String currentPathText;
						IPath path;
						
						currentPathText = getText().getText();
						if(contextInfo != null){
							/*
							try {
								currentPathText = 
									MacroResolver.resolveToString(currentPathText,
											new DefaultMacroSubstitutor(contextInfo ,
													"", //$NON-NLS-1$
													" ")); //$NON-NLS-1$
							} catch (BuildMacroException e) {
							}
							*/
						}

						/* Remove double quotes */
						currentPathText = currentPathText.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$

						/* Resolve variables */
						IStringVariableManager variableManager =
							VariablesPlugin.getDefault().getStringVariableManager();
						
						/* Remove workspace location prefix (if any) */
						path = new Path(currentPathText);
						
						/* Create workspace folder/file selection dialog and
						 * set initial selection */
						ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
								new WorkbenchLabelProvider(), new WorkbenchContentProvider());

		                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
		                dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
						
						if (type == BROWSE_DIR)	{
							IResource container = null;
							if(path.isAbsolute()){
								IContainer cs[] = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(path);
								if(cs != null && cs.length > 0)
									container = cs[0];
							}
							if(container == null && rc instanceof IContainer)
								container = rc;


							dialog.setInitialSelection(container);

							Class<?>[] filteredResources = {IContainer.class, IProject.class};
							dialog.addFilter(new TypedCDTViewerFilter(filteredResources));
							dialog.setTitle(WORKSPACE_DIR_DIALOG_TITLE); 
			                dialog.setMessage(WORKSPACE_DIR_DIALOG_MSG); 
						} else {
							IResource resource = null;
							if(path.isAbsolute()){
								resource= ResourceLookup.selectFileForLocation(path, null);
							}
							if(resource == null) resource = rc;

							dialog.setInitialSelection(resource);
							dialog.setValidator(new ISelectionStatusValidator() {
							    public IStatus validate(Object[] selection) {
							    	if (selection != null)
							    		if (selection.length > 0)
							    			if (!(selection[0] instanceof IFile))
							    				return new CDTStatusInfo(IStatus.ERROR, WORKSPACE_FILE_DIALOG_ERR);
							    	return new CDTStatusInfo();
							    }
							});
							dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE); 
			                dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG); 
						}
						
						/* Open dialog and process result. If a resource has
						 * been selected we create an absolute file system
						 * path for it based on the workspace_loc variable */
						if (dialog.open() == Window.OK) {
							fSetByBrowseDialog = true;
							
							IResource resource = (IResource) dialog.getFirstResult();
							
							if (resource != null) {
								getText().setText(variableManager.generateVariableExpression(WORKSPACELOC_VAR,
										resource.getFullPath().toString()));
							}
						}
							
						
					}
				});
			}
			
			if (type != BROWSE_NONE) {
				/* Browse button for external directories/files */
				final Button externalButton = createButton(parent, 4, FILESYSTEMBUTTON_NAME, false);
				externalButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent ev) {
						String currentName;
						String result;
						switch (type) {
							case BROWSE_DIR :
								DirectoryDialog dialog = new DirectoryDialog(getParentShell(), 
										SWT.OPEN|SWT.APPLICATION_MODAL);
								currentName = getText().getText();
								if(currentName != null && currentName.trim().length() != 0) {
									dialog.setFilterPath(currentName);
								}
								dialog.setMessage(FILESYSTEM_DIR_DIALOG_MSG);
								result = dialog.open();
								if(result != null) {
									fSetByBrowseDialog = true;
									getText().setText(result);
								}
								break;
							case BROWSE_FILE:
								FileDialog browseDialog = new FileDialog(getParentShell());
								currentName = getText().getText();
								if (currentName != null && currentName.trim().length() != 0) {
									browseDialog.setFilterPath(currentName);
								}
								result = browseDialog.open();
								if (result != null) {
									fSetByBrowseDialog = true;
									getText().setText(result);
								}
								break;
						}
					}
				});
			}
		}

	}

	/* Variable names */
	private static final String WORKSPACELOC_VAR = "workspace_loc"; //$NON-NLS-1$
	
	/* Names, messages and titles */
	private static final String WORKSPACEBUTTON_NAME = UIMessages.getString("FileListControl.button.workspace"); //$NON-NLS-1$
	private static final String FILESYSTEMBUTTON_NAME = UIMessages.getString("FileListControl.button.fs"); //$NON-NLS-1$

	private static final String ADD_STR = UIMessages.getString("FileListControl.add"); //$NON-NLS-1$
	private static final String DEL_STR = UIMessages.getString("FileListControl.delete"); //$NON-NLS-1$
	private static final String EDIT_STR = UIMessages.getString("FileListControl.edit"); //$NON-NLS-1$
	private static final String MOVEUP_STR = UIMessages.getString("FileListControl.moveup"); //$NON-NLS-1$
	private static final String MOVEDOWN_STR = UIMessages.getString("FileListControl.movedown"); //$NON-NLS-1$
	private static final String FILE_TITLE_ADD = UIMessages.getString("BrowseEntryDialog.file.title.add");	//$NON-NLS-1$
	private static final String DIR_TITLE_ADD = UIMessages.getString("BrowseEntryDialog.dir.title.add");	//$NON-NLS-1$
	private static final String FILE_TITLE_EDIT = UIMessages.getString("BrowseEntryDialog.file.title.edit");	//$NON-NLS-1$
	private static final String DIR_TITLE_EDIT = UIMessages.getString("BrowseEntryDialog.dir.title.edit");	//$NON-NLS-1$
	private static final String WORKSPACE_DIR_DIALOG_TITLE = UIMessages.getString("BrowseEntryDialog.wsp.dir.dlg.title");	//$NON-NLS-1$
	private static final String WORKSPACE_FILE_DIALOG_TITLE = UIMessages.getString("BrowseEntryDialog.wsp.file.dlg.title");	//$NON-NLS-1$
	private static final String WORKSPACE_DIR_DIALOG_MSG = UIMessages.getString("BrowseEntryDialog.wsp.dir.dlg.msg");	//$NON-NLS-1$
	private static final String WORKSPACE_FILE_DIALOG_MSG = UIMessages.getString("BrowseEntryDialog.wsp.file.dlg.msg");	//$NON-NLS-1$
	private static final String WORKSPACE_FILE_DIALOG_ERR = UIMessages.getString("BrowseEntryDialog.wsp.file.dlg.err");	//$NON-NLS-1$
	private static final String FILESYSTEM_DIR_DIALOG_MSG = UIMessages.getString("BrowseEntryDialog.fs.dir.dlg.msg");	//$NON-NLS-1$
	private static final String FILE_MSG = UIMessages.getString("BrowseEntryDialog.message.file");	//$NON-NLS-1$
	private static final String DIR_MSG = UIMessages.getString("BrowseEntryDialog.message.directory");	//$NON-NLS-1$
	private static final String TITLE = UIMessages.getString("BuildPropertyCommon.label.title");	//$NON-NLS-1$
	
	//toolbar
	private ToolBar toolBar;
	// toolbar items
	private ToolItem addItem, deleteItem, editItem, moveUpItem,
			moveDownItem;
	// title label
	private Label title;
	// images
//	private Image addImage, deleteImage, editImage, moveUpImage, moveDownImage;
//	private Composite composite;
	// list control
	private List list;
	private String compTitle;
	private SelectionListener selectionListener;
	private GridData tgdata, grid3, grid4, grid2;
	
	// The type of browse support that is required
	private int browseType;
	private IPath path;
	
	/* Workspace support */
	private boolean fWorkspaceSupport = false;
	private IVariableContextInfo contextInfo;
	private IResource rc;

	private java.util.List<IFileListChangeListener> listeners = new ArrayList<IFileListChangeListener>();
	private String oldValue[];
	
	//images
	private final Image IMG_ADD = CDTUIImages
			.get(CDTUIImages.IMG_FILELIST_ADD);
	private final Image IMG_DEL = CDTUIImages
			.get(CDTUIImages.IMG_FILELIST_DEL);
	private final Image IMG_EDIT = CDTUIImages
			.get(CDTUIImages.IMG_FILELIST_EDIT);
	private final Image IMG_MOVEUP = CDTUIImages
			.get(CDTUIImages.IMG_FILELIST_MOVEUP);
	private final Image IMG_MOVEDOWN = CDTUIImages
			.get(CDTUIImages.IMG_FILELIST_MOVEDOWN);
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param compTitle
	 * @param type
	 */
	public FileListControl(Composite parent, String compTitle, int type) {
		// Default to no browsing
		browseType = type;

		//file panel
		Composite filePanel = new Composite(parent, SWT.NONE);
		GridLayout form1 = new GridLayout();
		form1.numColumns = 1;
		form1.horizontalSpacing = 0;
		form1.verticalSpacing = 0;
		form1.marginHeight = 0;
		form1.marginWidth = 0;
		filePanel.setLayout(form1);
		filePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// title panel
		Composite titlePanel = new Composite(filePanel, SWT.BORDER);
		GridLayout titleform = new GridLayout(2, false);
		titleform.horizontalSpacing = 0;
		titleform.verticalSpacing = 0;
		titleform.marginHeight = 0;
		titleform.marginWidth = 0;
		titlePanel.setLayout(titleform);
		tgdata = new GridData(GridData.FILL_HORIZONTAL);
		tgdata.heightHint = IDialogConstants.BUTTON_BAR_HEIGHT;
		titlePanel.setLayoutData(tgdata);
		title = new Label(titlePanel, SWT.NONE | SWT.BOLD);
		this.compTitle = "  " + compTitle; //$NON-NLS-1$
		title.setText(this.compTitle);
		grid2 = new GridData(GridData.FILL_HORIZONTAL);
		title.setLayoutData(grid2);
		//button panel
		Composite buttonPanel = new Composite(titlePanel, SWT.NONE);
		GridLayout form2 = new GridLayout();
		form2.numColumns = 5;
		form2.horizontalSpacing = 0;
		form2.verticalSpacing = 0;
		form2.marginWidth = 0;
		form2.marginHeight = 0;
		buttonPanel.setLayout(form2);
		// toolbar
		toolBar = new ToolBar(buttonPanel, SWT.HORIZONTAL | SWT.RIGHT
				| SWT.FLAT);
		// add toolbar item
		addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setImage(IMG_ADD);
		addItem.setToolTipText(ADD_STR);
		addItem.addSelectionListener(getSelectionListener());
		// delete toolbar item
		deleteItem = new ToolItem(toolBar, SWT.PUSH);
		deleteItem.setImage(IMG_DEL);
		deleteItem.setToolTipText(DEL_STR);
		deleteItem.addSelectionListener(getSelectionListener());
		// edit toolbar item
		editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.setImage(IMG_EDIT);
		editItem.setToolTipText(EDIT_STR);
		editItem.addSelectionListener(getSelectionListener());
		// moveup toolbar item
		moveUpItem = new ToolItem(toolBar, SWT.PUSH);
		moveUpItem.setImage(IMG_MOVEUP);
		moveUpItem.setToolTipText(MOVEUP_STR);
		moveUpItem.addSelectionListener(getSelectionListener());
		// movedown toolbar item
		moveDownItem = new ToolItem(toolBar, SWT.PUSH);
		moveDownItem.setImage(IMG_MOVEDOWN);
		moveDownItem.setToolTipText(MOVEDOWN_STR);
		moveDownItem.addSelectionListener(getSelectionListener());
		grid3 = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END);
		buttonPanel.setLayoutData(grid3);
		// list control
		list = new List(filePanel, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		grid4 = new GridData(GridData.FILL_BOTH);
		// force the list to be no wider than the title bar
		Point preferredSize = titlePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
		grid4.widthHint = preferredSize.x;
		grid4.heightHint = preferredSize.y * 3;
		grid4.horizontalSpan = 2;
		list.setLayoutData(grid4);
		list.addSelectionListener(getSelectionListener());
		//Add a double-click event handler
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// Popup the editor on the selected item from the list
				editSelection();
			}
		});
		// Add a delete event handler
		list.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				// Is this the delete key
				if (e.keyCode == SWT.DEL) {
					removePressed();
				} else {
					super.keyPressed(e);
				}
			}
		});

		selectionChanged();
	}
	/**
	 * Set list values
	 * 
	 * @param listVal
	 */
	public void setList(String[] listVal) {
		if (list != null) {
			list.removeAll();
		}
		for (String element : listVal) {
			list.add(element);
		}
		checkNotificationNeeded();
	}
	
	public void addChangeListener(IFileListChangeListener listener){
		listeners.add(listener);
	}
	
	public void removeChangeListener(IFileListChangeListener listener){
		listeners.remove(listener);
	}

	public void checkNotificationNeeded(){
		String items[] = getItems();
		if(oldValue != null){
			if(oldValue.length == items.length){
				int i;
				for(i = 0; i < oldValue.length; i++){
					if(!oldValue[i].equals(items[i]))
						break;
				}
				if(i == oldValue.length)
					return;
			}
			String old[] = oldValue;
			System.arraycopy(items,0,oldValue = new String[items.length],0,items.length);
			notifyListeners(old,oldValue);
		} else{
			System.arraycopy(items,0,oldValue = new String[items.length],0,items.length);
		}
	}
	
	public void notifyListeners(String oldVal[], String newVal[]){
		for (IFileListChangeListener listener: listeners) {
			listener.fileListChanged(this,oldVal,newVal);
		}
	}
	
	/**
	 * Set selection
	 * 
	 * @param sel
	 */
	public void setSelection(int sel) {
		if (list.getItemCount() > 0)
			list.setSelection(sel);
		selectionChanged();
	}
	/**
	 * Set default selection
	 */
	public void setSelection() {
		if (list.getItemCount() > 0)
			list.setSelection(0);
	}
	/**
	 * removes all items from list control
	 */
	public void removeAll() {
		if (list != null){
			list.removeAll();
			checkNotificationNeeded();
		}
	}
	/**
	 * get list items
	 */
	public String[] getItems() {
		return list.getItems();
	}
	/**
	 * Create selection listener for buttons
	 */
	private void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addItem) {
					addPressed();
				} else if (widget == deleteItem) {
					removePressed();
				} else if (widget == moveUpItem) {
					upPressed();
				} else if (widget == moveDownItem) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				} else if (widget == editItem) {
					editSelection();
				}
			}
		};
	}
	/**
	 * Returns selection listener
	 * 
	 * @return
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}
	/**
	 * This method will be called when the add button is pressed
	 */
	private void addPressed() {
		// Prompt user for a new item
		String input = getNewInputObject();
		
		// Add it to the list
		if (input != null && input.length() > 0) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(input, index + 1);
				list.setSelection(index + 1);
			}
			else {
				list.add(input, 0);
				list.setSelection(0);
			}
			checkNotificationNeeded();
		}

		selectionChanged();
	}
	/**
	 * This method will be called when the remove button is pressed
	 */
	private void removePressed() {
		int index = list.getSelectionIndex();
		if (browseType == BROWSE_DIR || browseType == BROWSE_FILE) {
			String quest = UIMessages.getString("FileListControl.deletedialog.message"); //$NON-NLS-1$
			String title = UIMessages.getString("FileListControl.deletedialog.title"); //$NON-NLS-1$
			boolean delDir = MessageDialog.openQuestion(list.getShell(), title,
					quest);
			if (delDir && index != -1){
				list.remove(index);
				checkNotificationNeeded();
			}
		} else if (index != -1){
			list.remove(index);
			checkNotificationNeeded();
		}
		selectionChanged();
	}
	/**
	 * This method will be called when the move up button is pressed
	 */
	private void upPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String preList = list.getItem(index - 1);
		list.setItem(index - 1, curSelList);
		list.setItem(index, preList);
		list.setSelection(index - 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the move down button is pressed
	 */
	private void downPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String nextList = list.getItem(index + 1);
		list.setItem(index + 1, curSelList);
		list.setItem(index, nextList);
		list.setSelection(index + 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the edit button is pressed
	 */
	private void editSelection() {
		int index = list.getSelectionIndex();
		if (index != -1) {
			String selItem = list.getItem(index);
			if (selItem != null) {
				/* Use SelectPathInputDialog for IOption.BROWSE_DIR and
				 * IOption.BROWSE_FILE. Use simple input dialog otherwise.
				 */
				InputDialog dialog;
				if ((browseType == BROWSE_DIR) ||
						(browseType == BROWSE_FILE)) {

					String title;
					String message;
					if (browseType == BROWSE_DIR) {
						title = DIR_TITLE_EDIT;
						message = DIR_MSG;
					} else {
						title = FILE_TITLE_EDIT;
						message = FILE_MSG;
					}
					dialog =  new SelectPathInputDialog(getListControl().getShell(), title,
							message, selItem, null, browseType);
					
				} else {
					String title = UIMessages.getString("FileListControl.editdialog.title"); //$NON-NLS-1$
					dialog = new InputDialog(null, title, compTitle,
							selItem, null);
				}
					
				
				String newItem = null;
				if (dialog.open() == Window.OK) {
					newItem = dialog.getValue();
					
					/* If newItem is a directory or file path we need to
					 * double-quote it if required. We only do this if the user
					 * selected a new path using a browse button. If he/she simply
					 * edited the text, we skip this so the user can remove quotes if he/she
					 * wants to.
					 */
					if (dialog instanceof SelectPathInputDialog) {
						if (((SelectPathInputDialog) dialog).isValueSetByBrowse())
							newItem = doubleQuotePath(newItem);
					}
					
					if (newItem != null && !newItem.equals(selItem)) {
						list.setItem(index, newItem);
						checkNotificationNeeded();
						selectionChanged();
					}
				}
			}
		}
	}
	/**
	 * This method will be called when the list selection changed
	 */
	public void selectionChanged() {
		int index = list.getSelectionIndex();
		int size = list.getItemCount();
		deleteItem.setEnabled(size > 0);
		moveUpItem.setEnabled(size > 1 && index > 0);
		moveDownItem.setEnabled(size > 1 && index >= 0 && index < size - 1);
		editItem.setEnabled(size > 0);
	}
	/**
	 * Returns List control
	 */
	public List getListControl() {
		return list;
	}

	/**
	 * Sets the IPath of the project the field editor was
	 * created for.
	 * 
	 * @param path The path to the 
	 */
	public void setPath(IPath path) {
		this.path = path;
	}
	
	/**
	 * Set browseType
	 */
	public void setType(int type) {
		browseType = type;
	}

	/**
	 * Enable/Disable workspace support. If enabled, the workspace browse button
	 * will be visible in the SelectPathInputDialog.
	 * @param enable
	 */
	public void setWorkspaceSupport(boolean enable)	{
		fWorkspaceSupport = enable;
	}

	/**
	 * Set the field editor context.
	 */
	public void setContext(IVariableContextInfo info) {
		contextInfo = info;
		for(;info != null;info = info.getNext()){
			/*
			if(info.getContextType() == IBuildMacroProvider.CONTEXT_PROJECT){
				IManagedProject mngProj = (IManagedProject)info.getContextData();
				this.rc = mngProj.getOwner();
				break;
			}
			*/
		}
	}

	/**
	 * Returns the input dialog string
	 */
	private String getNewInputObject() {
		// Create a dialog to prompt for a new list item
		String input = null;
		String title = new String();
		String message = new String();
		String initVal = new String();
		
		if (browseType == BROWSE_DIR) {
			title = DIR_TITLE_ADD;
			message = DIR_MSG;
			initVal = (path == null ? initVal : path.toString());
		} else if (browseType == BROWSE_FILE) {
			title = FILE_TITLE_ADD;
			message = FILE_MSG;
			initVal = (path == null ? initVal : path.toString());
		} else {
			title = TITLE;
			message = compTitle;
		}
		
		// Prompt for value
		SelectPathInputDialog dialog = new SelectPathInputDialog(getListControl().getShell(), title, message, initVal, null, browseType);
		if (dialog.open() == Window.OK) {
			input = dialog.getValue();
		}
		
		/* Double-quote (if required) the text if it is a directory or file */
		if (input != null && input.length() > 0) {
			if (browseType == BROWSE_DIR ||
					browseType == BROWSE_FILE) {
				input = doubleQuotePath(input);
			}
		}

		return input;
	}
	
	public Label getLabelControl(){
		return title;
	}
	
	public void setEnabled(boolean enabled){
		title.setEnabled(enabled);
		toolBar.setEnabled(enabled);
		list.setEnabled(enabled);
	}
	
	/**
	 * Double-quotes a path name if it contains white spaces, backslahes
	 * or a macro/variable (We don't know if a macro will contain spaces, so we
	 * have to be on the safe side).
	 * @param pathName The path name to double-quote.
	 * @return
	 */
	private String doubleQuotePath(String pathName)	{
		/* Trim */
		pathName = pathName.trim();
		
		/* Check if path is already double-quoted */
		boolean bStartsWithQuote = pathName.startsWith("\""); //$NON-NLS-1$
		boolean bEndsWithQuote = pathName.endsWith("\""); //$NON-NLS-1$
		
		/* Check for spaces, backslashes or macros */ 
		int i = pathName.indexOf(" ") + pathName.indexOf("\\") //$NON-NLS-1$ //$NON-NLS-2$
			+ pathName.indexOf("${"); //$NON-NLS-1$
		
		/* If indexof didn't fail all three times, double-quote path */
		if (i != -3) {
			if (!bStartsWithQuote)
				pathName = "\"" + pathName; //$NON-NLS-1$
			if (!bEndsWithQuote)
				pathName = pathName + "\""; //$NON-NLS-1$
		}
		
		return pathName;
	}
}
