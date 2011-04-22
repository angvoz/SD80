/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.ExclusionType;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The RefreshPolicyTab allows users to modify a project's refresh settings for each build.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author vkong
 * @since 8.0
 */
public class RefreshPolicyTab extends AbstractCPropertyTab {

	private final Image IMG_FOLDER = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FOLDER);
	private final Image IMG_FILE = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_FILE_OBJ);
	private final Image IMG_RESOURCE = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_FILE_FOLDER_OBJ);
	private final Image IMG_EXCEPTION = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_REFACTORING_ERROR);
	
	
	private final static int IDX_ADD_RESOURCE = 0;
	private final static int IDX_ADD_EXCEPTION = 1;
	private final static int IDX_EDIT = 2;
	private final static int IDX_DELETE = 3;
	
	private TreeViewer fTree;
	private RefreshScopeManager fManager;
	private IProject fProject;
	

	private ArrayList<_Entry> fSrc;
	private List<IResource> fResourcesToRefresh;
	private HashMap<IResource, List<RefreshExclusion>> fResourceToExclusionsMap = new HashMap<IResource, List<RefreshExclusion>>();
	
	
	public RefreshPolicyTab() {
		fManager = RefreshScopeManager.getInstance();		
	}
	
	private void loadInfo() {
		fResourcesToRefresh = new LinkedList<IResource>(fManager.getResourcesToRefresh(fProject));
		if (fResourcesToRefresh != null) {
			Iterator<IResource> iterator = fResourcesToRefresh.iterator();
			while (iterator.hasNext()) {
				IResource resource = iterator.next();				
				fResourceToExclusionsMap.put(resource, new LinkedList<RefreshExclusion>(fManager.getExclusions(resource)));
			}
		}
	}
	
	private List<RefreshExclusion> getExclusions(IResource resource) {
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		return fResourceToExclusionsMap.get(resource);
	}
	
	/**
	 * Wrapper for IResource/RefreshExclusion
	 */
	class _Entry {
		//if this is not a resource to refresh, resourceToRefresh will be null
		IResource resourceToRefresh = null; 
		
		//if this is not a refresh exclusion, exclusion will be null
		RefreshExclusion exclusion = null; 
		
		//if this is a refresh exclusion, parent is the Exceptions node this is a child of
		_Exception_Node parent = null; 
		
		// exceptions_node is the Exceptions node under this Entry, there should be a exceptions_node if this resource/refresh exclusion has nested exclusions
		_Exception_Node exceptions_node = null; 
		
		// if this is a refresh exclusion, exclusion_instances is a list of exclusion instances associated with this exclusion
		List<_Exclusion_Instance> exclusion_instances = new ArrayList<_Exclusion_Instance>(); 
		
		_Entry(IResource _ent) { 
			resourceToRefresh = _ent;
			if (getExclusions(resourceToRefresh) != null && getExclusions(resourceToRefresh).size() > 0)
				exceptions_node = new _Exception_Node(this);
		}
		
		_Entry(RefreshExclusion _ent, _Exception_Node parent) {			
			exclusion = _ent;
			this.parent = parent;
			if (exclusion.getNestedExclusions() != null && exclusion.getNestedExclusions().size() > 0) {
				exceptions_node = new _Exception_Node(this);				
			}
			if (exclusion.getExclusionInstances() != null && exclusion.getExclusionInstances().size() > 0) {
				Iterator<ExclusionInstance> iterator = exclusion.getExclusionInstances().iterator();
				while (iterator.hasNext()) {
					exclusion_instances.add(new _Exclusion_Instance(iterator.next(), this));
				}
			}
		}

		
		@Override
		public String toString() { 				
			if (isExclusion())
				return exclusion.getName();
			
			return resourceToRefresh.getFullPath().makeRelative().toString();
		} 
		
		public Object[] getChildren() {
			if (isExclusion()) {
				List children = new ArrayList(exclusion_instances);
				if (exceptions_node != null)
					children.add(exceptions_node);
				return children.toArray();
			}
			
			if (exceptions_node != null)
				return new Object[] {exceptions_node};
			
			return null;
		}
		
		public boolean isExclusion() {
			return parent != null;
		}
		
		public void addException(RefreshExclusion exclusion) {
			if (exceptions_node == null) {
				exceptions_node = new _Exception_Node(this);
			}
			exceptions_node.addException(exclusion);
		}
		
		public void updateException(RefreshExclusion exclusion) {
			List<ExclusionInstance> exclusionInstancesToAdd = exclusion.getExclusionInstances();
			Iterator<ExclusionInstance> iterator = exclusionInstancesToAdd.iterator();
			exclusion_instances.clear();

			while (iterator.hasNext()) {
				ExclusionInstance instanceToAdd = iterator.next();
				exclusion_instances.add(new _Exclusion_Instance(instanceToAdd, this));
			}
		}

		public void remove() {
			if (isExclusion()) {
				RefreshExclusion exclusionToRemove = exclusion;
					
				_Entry parentEntry = parent.parent;						
				if (parentEntry.isExclusion()) {
					parentEntry.exclusion.removeNestedExclusion(exclusionToRemove);
				} else {
					List<RefreshExclusion> exceptions = getExclusions(parentEntry.resourceToRefresh);
					exceptions.remove(exclusionToRemove);
				}
				
				//update tree						
				if (parent.exceptions.size() > 1) {
					parent.exceptions.remove(this);
				} else {
					parentEntry.exceptions_node = null;
				}
			} else { //this is a resource to refresh
				fResourceToExclusionsMap.remove(resourceToRefresh);
				fResourcesToRefresh.remove(resourceToRefresh);
				fSrc.remove(this);				
			}			
		}

	}
	
	class _Exception_Node {
		_Entry parent;	//can be IResource or RefreshExclusion - must not be null
		
		//list of refresh exclusions under this Exceptions node
		List <_Entry> exceptions = new ArrayList<_Entry>();
		
		_Exception_Node(_Entry ent) { 
			parent = ent;
			Iterator<RefreshExclusion> iterator = null;
			
			if (parent.isExclusion()) {
				if (parent.exclusion.getNestedExclusions() != null)
					iterator = parent.exclusion.getNestedExclusions().iterator();			
			} else {
				if (getExclusions(parent.resourceToRefresh) != null)
					iterator = getExclusions(parent.resourceToRefresh).iterator();
			}
			
			if (iterator != null) {
				while (iterator.hasNext()) {
					exceptions.add(new _Entry(iterator.next(), this));
				}
			}

		}
		
		public void addException(RefreshExclusion exclusion) {
			exceptions.add(new _Entry(exclusion, this));
			if (parent.isExclusion()) {
				parent.exclusion.addNestedExclusion(exclusion);			
			} else {
				List<RefreshExclusion> exclusions = getExclusions(parent.resourceToRefresh);
				if (exclusions == null) {
					exclusions = new LinkedList<RefreshExclusion>();
					fResourceToExclusionsMap.put(parent.resourceToRefresh, exclusions);
				}
				exclusions.add(exclusion);
			}
		}
		
		public Object[] getChildren() {
			return exceptions.toArray();
		}
		
		@Override
		public String toString() {
			return Messages.RefreshPolicyTab_exceptionsLabel;
		} 
	}
	
	/**
	 * Wrapper for ExclusionInstance
	 */
	class _Exclusion_Instance {
		_Entry parent; //the parent refresh exclusion
		ExclusionInstance instance = null;
		
		_Exclusion_Instance(ExclusionInstance instance, _Entry parent) { 
			this.parent = parent;
			this.instance = instance;
		}
		
		public Object[] getChildren() {
			return null;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return instance.getDisplayString();
		}

		public void remove() {
			parent.exclusion.removeExclusionInstance(instance);
			parent.exclusion_instances.remove(this);
			
			if (parent.exclusion_instances.size() < 1 && parent.exceptions_node == null) {
				parent.remove();
			}
		}	
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		fProject = page.getProject();
		loadInfo();
		initButtons(new String[] {
				Messages.RefreshPolicyTab_addResourceButtonLabel,
				Messages.RefreshPolicyTab_addExceptionButtonLabel,
				Messages.RefreshPolicyTab_editButtonLabel,
				Messages.RefreshPolicyTab_deleteButtonLabel}, 120);
		usercomp.setLayout(new GridLayout(1, false));
		
		
		Label topLabel = new Label(usercomp, SWT.NONE);
		topLabel.setText(Messages.RefreshPolicyTab_tabLabel);
		Group g1 = setupGroup(usercomp, Messages.RefreshPolicyTab_resourcesGroupLabel, 2, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		
		fSrc = new ArrayList<_Entry>();
		generateTreeContent(fProject);
		
		fTree = new TreeViewer(g1);
		fTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTree.getTree().getAccessible().addAccessibleListener(
            new AccessibleAdapter() {
                @Override
				public void getName(AccessibleEvent e) {
                	e.result = Messages.RefreshPolicyTab_resourcesTreeLabel;
                }
            }
        );
		
		fTree.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {				
				if (parentElement instanceof _Entry) {
					return ((_Entry) parentElement).getChildren();
				}
				if (parentElement instanceof _Exception_Node) {
					return ((_Exception_Node)parentElement).getChildren();
				}
				return null;
			}
			public Object getParent(Object element) {
				if (element instanceof _Entry)
					return ((_Entry)element).parent;
				if (element instanceof _Exception_Node)
					return ((_Exception_Node)element).parent;
				if (element instanceof _Exclusion_Instance)
					return ((_Exclusion_Instance)element).parent;
				return null;
			}
			public boolean hasChildren(Object element) {
				return (element instanceof _Entry || element instanceof _Exception_Node);
			}
			public Object[] getElements(Object inputElement) {
				return fSrc.toArray(new _Entry[fSrc.size()]);
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}});

		fTree.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof _Exception_Node)
					return IMG_EXCEPTION;
				else if (element instanceof _Entry) {
					_Entry entry = (_Entry) element;
					if (entry.isExclusion()) {
						return getImageForExclusionType(entry.exclusion.getExclusionType());
					}
					return getImageForResource(entry.resourceToRefresh);
				} 

				else if (element instanceof _Exclusion_Instance){
					return getImageForExclusionType(((_Exclusion_Instance) element).instance.getExclusionType());
				}
				else 
					return null;
			}
		});
		
		fTree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof TreeSelection) {
					Object sel = ((TreeSelection)selection).getFirstElement();
					if ( sel != null && sel instanceof _Exception_Node) {
						fTree.setSelection(null);
					} 
				}
				updateButtons();				
			}
		});
		
		fTree.setInput(fSrc);
		updateButtons();
		
	}
	
	private Image getImageForResource(IResource resource) {
		switch (resource.getType()) {
		case IResource.FILE:
			return IMG_FILE;
		case IResource.FOLDER:
		case IResource.PROJECT:
			return IMG_FOLDER;
		default:
			return IMG_RESOURCE;
		}
	}
		
	private Image getImageForExclusionType(ExclusionType exclusionType) {
		switch (exclusionType) {
		case FILE:
			return IMG_FILE;
		case FOLDER:
			return IMG_FOLDER;
		case RESOURCE:
		default:
			return IMG_RESOURCE;
		}		
	}

	private void generateTreeContent(IProject project) {
		Iterator<IResource> iterator = fResourcesToRefresh.iterator();
		while (iterator.hasNext()) {
			_Entry top = new _Entry(iterator.next());
			fSrc.add(top);
		}
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateButtons() {
		TreeItem[] sel = fTree.getTree().getSelection();
		buttonSetEnabled(IDX_ADD_RESOURCE, true);
    	buttonSetEnabled(IDX_ADD_EXCEPTION, sel.length == 1 && sel[0].getData() instanceof _Entry);
    	buttonSetEnabled(IDX_EDIT, sel.length == 1 && sel[0].getData() instanceof _Entry && ((_Entry) sel[0].getData()).isExclusion());
    	buttonSetEnabled(IDX_DELETE, sel.length == 1 && (sel[0].getData() instanceof _Entry || sel[0].getData() instanceof _Exclusion_Instance));    		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int x) {
		Shell shell = usercomp.getShell();
		TreeSelection selection = (TreeSelection) fTree.getSelection();
				
		switch (x) {
		case IDX_ADD_RESOURCE:
			//TODO: Phase one implementation - folders only - need to change this for Phase two
			ContainerSelectionDialog addResourceDialog = new ContainerSelectionDialog(shell, null, true, Messages.RefreshPolicyTab_addResourceDialogDescription);
			addResourceDialog.setTitle(Messages.RefreshPolicyTab_addResourceDialogTitle);
			if (addResourceDialog.open() == Window.OK) {
				Object[] result = addResourceDialog.getResult();
				for (int i = 0; i < result.length; i++) {
					 IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember((IPath) result[i]);
					_Entry newResource = new _Entry(resource);
					//update the model element in this tab
					fResourcesToRefresh.add(resource);
					//update tree
					fSrc.add(newResource);
				}
				fTree.refresh();
			}
			break;
			
		case IDX_ADD_EXCEPTION:
			if (selection == null)
				break;
			_Entry sel = (_Entry) selection.getFirstElement();
			RefreshPolicyExceptionDialog addExceptionDialog; 
			if (sel.isExclusion()) {
				addExceptionDialog = new RefreshPolicyExceptionDialog(shell, sel.exclusion, true);
			} else {
				addExceptionDialog = new RefreshPolicyExceptionDialog(shell, sel.resourceToRefresh, getExclusions(sel.resourceToRefresh), true);
			}
			if (addExceptionDialog.open() == Window.OK) {				
				RefreshExclusion newExclusion = addExceptionDialog.getResult();
				
				//update tree & the working copy of the model elements in this tab
				sel.addException(newExclusion);
				fTree.refresh();
			}                                                                                       
			fTree.refresh();
			break;
			
		case IDX_EDIT:	//can only edit a refresh exclusion
			if (selection == null)
				break;
			_Entry selectedExclusion = (_Entry) selection.getFirstElement();
			RefreshPolicyExceptionDialog editExceptionDialog;
			
			editExceptionDialog = new RefreshPolicyExceptionDialog(shell, selectedExclusion.exclusion, false);
			if (editExceptionDialog.open() == Window.OK) {				
				RefreshExclusion updatedExclusion = editExceptionDialog.getResult();
			
				//update tree
				selectedExclusion.updateException(updatedExclusion);
				fTree.refresh();
			}
			fTree.refresh();
			break;
			
		case IDX_DELETE:
			if (selection == null)
				break;
			if (selection.getFirstElement() instanceof _Entry) {
				_Entry sel1 = (_Entry) selection.getFirstElement();
				boolean remove = false;
				if (sel1.exceptions_node != null) {
					String question;
					if (sel1.isExclusion()) {
						question = Messages.RefreshPolicyTab_deleteConfirmationDialog_question_exception;
					} else {
						question = Messages.RefreshPolicyTab_deleteConfirmationDialog_question_resource;
								
					}
					if (MessageDialog.openQuestion(shell, Messages.RefreshPolicyTab_deleteConfirmationDialog_title, question)) {
						remove = true;
					}
				} else {
					remove = true;
				}
				if (remove) {
					//update tree & the working copy of the model elements in this tab
					sel1.remove();
					fTree.refresh();
				}		
			} else { //exclusion instance
				_Exclusion_Instance sel1 = (_Exclusion_Instance) selection.getFirstElement();
				
				//update tree & the working copy of the model elements in this tab
				sel1.remove();
				fTree.refresh();		
			}
				
			break;
			
		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performOK()
	 */
	@Override 
	protected void performOK() {
		fManager.setResourcesToRefresh(fProject, fResourcesToRefresh);
		Iterator<IResource> iterator = fResourcesToRefresh.iterator();
		while (iterator.hasNext()) {
			IResource resource = iterator.next();
			fManager.clearExclusions(resource);
			List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
			fManager.setExclusions(resource, exclusions);
		}
	}
}
