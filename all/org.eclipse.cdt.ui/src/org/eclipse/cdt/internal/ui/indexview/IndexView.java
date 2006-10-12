/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.ui.IndexLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Doug Schaefer
 *
 */
public class IndexView extends ViewPart implements PDOM.IListener, IElementChangedListener {

	private TreeViewer viewer;
//	private DrillDownAdapter drillDownAdapter;
	private ToggleLinkingAction toggleLinkingAction;
	private IndexAction countSymbolsAction;
	private IndexAction discardExternalDefsAction;
	private IndexAction openDefinitionAction;
	private IndexAction findDeclarationsAction;
	private IndexAction findReferencesAction;
	Filter filter = new Filter();
	public boolean isLinking = false;
	
	public void toggleExternalDefs() {
		if (!filter.showExternalDefs) {
			viewer.addFilter(filter);
		} else {
			viewer.removeFilter(filter);
		}
		filter.showExternalDefs = ! filter.showExternalDefs;
	}
	
	public void toggleLinking() {
		isLinking = ! isLinking;
		if (isLinking) {
			openDefinitionAction.run();
		}
	}
	
	/**
	 * Handles selection changed in viewer. Updates global actions. Links to
	 * editor (if option enabled)
	 */
	void handleSelectionChanged(SelectionChangedEvent event) {
//		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//		updateStatusLine(selection);
//		updateActionBars(selection);
		if (isLinking) {
			openDefinitionAction.run();
		}
	}
	
	private static class Filter extends ViewerFilter {
		public boolean showExternalDefs = false;
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof PDOMBinding) {
				PDOMBinding binding = (PDOMBinding)element;
				try {
					PDOMName name = binding.getFirstReference();
					if (name == null)
						name = binding.getFirstDeclaration();
					if (name == null)
						name = binding.getFirstDefinition();
					if (name == null)
						return false;
					
					IASTFileLocation location = name.getFileLocation();
					IPath path = new Path(location.getFileName());
					Object input = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
					if (input == null)
						return false;
					return true;
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
					return true;
				}
			}
			else
				return true;
		}
	}
	
	private class Counter implements IPDOMVisitor {
		public int count;
		public boolean visit(IPDOMNode node) throws CoreException {
			++count;
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
	}
	
	private static class Children implements IPDOMVisitor {
		private int index;
		private IPDOMNode[] nodes;
		public Children(IPDOMNode[] nodes) {
			this.nodes = nodes;
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			nodes[index++] = node;
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
	}
	
	private static class HasChildren implements IPDOMVisitor {
		public boolean hasChildren;
		public boolean visit(IPDOMNode node) throws CoreException {
			hasChildren = true;
			throw new CoreException(Status.OK_STATUS);
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
	}
	
	static PDOMBinding[] trim(PDOMBinding []binding) {
		int len;
		for (len = 0; len < binding.length; len++)
			if(binding[len] == null) {
				PDOMBinding [] newBinding = new PDOMBinding [len];
				System.arraycopy(binding, 0, newBinding, 0, len);
				return newBinding;
			}
		return binding;
	}
	
	private class IndexContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			try {
				if (parentElement instanceof ICProject) {
					PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM((ICProject)parentElement);
					PDOMLinkage[] linkages= pdom.getLinkages();
					if (linkages.length == 1) {
						// Skip linkages in hierarchy if there is only one
						return getChildren(linkages[0]);
					}
					return linkages;
				} else if (parentElement instanceof IPDOMNode) {
					IPDOMNode node = (IPDOMNode)parentElement;
					Counter counter = new Counter();
					node.accept(counter);
					IPDOMNode[] children = new IPDOMNode[counter.count];
					Children childrener = new Children(children);
					node.accept(childrener);
					return children;
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			// TODO should really figure this out
			return null;
		}

		public boolean hasChildren(Object element) {
			try {
				if (element instanceof ICProject) {
					PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM((ICProject)element);
					PDOMLinkage[] linkages = pdom.getLinkages();
					if (linkages.length == 0)
						return false;
					else if (linkages.length == 1)
						// Skipping linkages if only one
						return hasChildren(linkages[0]);
					else
						return true;
				} else if (element instanceof IPDOMNode) {
					HasChildren hasChildren = new HasChildren();
					try {
						((IPDOMNode)element).accept(hasChildren);
					} catch (CoreException e) {
						if (e.getStatus() != Status.OK_STATUS)
							throw e;
					}
					return hasChildren.hasChildren;
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			try {
				if (inputElement instanceof ICModel) {
					ICModel model = (ICModel)inputElement;
					ICProject[] projects = model.getCProjects();
					Arrays.sort(projects, new Comparator() {
						public int compare(Object arg0, Object arg1) {
							String name0 = ((ICProject)arg0).getElementName();
							String name1 = ((ICProject)arg1).getElementName();
							return name0.compareToIgnoreCase(name1);
						}
					});
					return projects;
				}
			} catch (CModelException e) {
				CUIPlugin.getDefault().log(e);
			}
			
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer = new TreeViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new IndexContentProvider());
		viewer.setLabelProvider(new IndexLabelProvider());
		
		ICModel model = CoreModel.getDefault().getCModel();
		viewer.setInput(model);
		try {
			ICProject[] projects = model.getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(projects[i]); 
					pdom.addListener(this);
			}
			viewer.setChildCount(model, projects.length);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		CoreModel.getDefault().addElementChangedListener(this);
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// Menu
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            private void hideMenuItems(IMenuManager manager) {
            }

            public void menuAboutToShow(IMenuManager manager) {
                IndexView.this.fillContextMenu(manager);
                hideMenuItems(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
        
        getSite().setSelectionProvider(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
	}
	
	public void dispose() {
		super.dispose();
		ICModel model = CoreModel.getDefault().getCModel();
		try {
			ICProject[] projects = model.getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(projects[i]); 
					pdom.removeListener(this);
			}
			viewer.setChildCount(model, projects.length);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		CoreModel.getDefault().removeElementChangedListener(this);
	}
	
	private void makeActions() {
		countSymbolsAction = new CountNodeAction(viewer);
		discardExternalDefsAction = new DiscardExternalDefsAction(viewer, this);
		toggleLinkingAction = new ToggleLinkingAction(this);
		openDefinitionAction = new OpenDefinitionAction(viewer);
		findDeclarationsAction = new FindDeclarationsAction(viewer);
		findReferencesAction = new FindReferencesAction(viewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IndexView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		if (countSymbolsAction.valid())
			manager.add(countSymbolsAction);
		if (discardExternalDefsAction.valid())
			manager.add(discardExternalDefsAction);
		if (openDefinitionAction.valid())
			manager.add(openDefinitionAction);
		if (findDeclarationsAction.valid())
			manager.add(findDeclarationsAction);
		if (findReferencesAction.valid())
			manager.add(findReferencesAction);
		//manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openDefinitionAction.run();
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		//fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
//		drillDownAdapter.addNavigationActions(manager);
		manager.add(toggleLinkingAction);
		manager.add(discardExternalDefsAction);
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void handleChange(PDOM pdom) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});
	}

	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;

		// TODO we'll get fancier when we do a virtual tree.
		processDelta(event.getDelta());
	}
	
	private void processDelta(ICElementDelta delta) {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);
			break;
		case ICElement.C_PROJECT:
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				try {
					PDOM pdom = ((PDOM)CCorePlugin.getPDOMManager().getPDOM((ICProject)delta.getElement()));
					pdom.addListener(this);
					handleChange(pdom);
				} catch (CoreException e) {
				}
				break;
			case ICElementDelta.REMOVED:
				handleChange(null);
				break;
			}
		}
	}
	
}