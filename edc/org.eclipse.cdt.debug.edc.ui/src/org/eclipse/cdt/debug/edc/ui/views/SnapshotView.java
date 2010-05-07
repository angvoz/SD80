/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.ISnapshotAlbumEventListener;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeColumnViewerLabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SnapshotView extends ViewPart implements ISnapshotAlbumEventListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.cdt.debug.edc.ui.views.SnapshotView";

	public static final String CONTEXT_ID = "org.eclipse.cdt.debug.edc.ui.context.SnapshotView";

	private static final ImageDescriptor SNAPSHOT_NODE_IMGDESC = AbstractUIPlugin
			.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
					"/icons/obj16/snapshot_node.png"); //$NON-NLS-1$
	private static final ImageDescriptor SNAPSHOT_CURRENT_NODE_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/obj16/snapshot_current_node.png"); //$NON-NLS-1$
	private static final ImageDescriptor ALBUM_NODE_IMGDESC = AbstractUIPlugin
			.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
					"/icons/obj16/album_node.png"); //$NON-NLS-1$
	private static final ImageDescriptor ALBUM_NODE_RECORDING_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/obj16/album_node_recording.png"); //$NON-NLS-1$
	private static final ImageDescriptor ALBUM_NODE_PLAYBACK_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/obj16/album_node_playback.png"); //$NON-NLS-1$
	private static final ImageDescriptor ALBUM_NODE_LIVE_RECORDING_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/obj16/album_node_live_recording.png"); //$NON-NLS-1$
	private static final ImageDescriptor ALBUM_NODE_ERROR_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/obj16/album_node_error.png"); //$NON-NLS-1$
	private static final ImageDescriptor PLAY_SNAPSHOT_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/etool16/play_snapshots.gif"); //$NON-NLS-1$

	public static final String SNAPSHOT_VIEW_ID = "org.eclipse.cdt.debug.edc.ui.views.SnapshotView";

	private static final Image ALBUM_NODE_IMAGE = ALBUM_NODE_IMGDESC
			.createImage();
	private static final Image ALBUM_NODE_RECORDING_IMAGE = ALBUM_NODE_RECORDING_IMGDESC
	.createImage();
	private static final Image ALBUM_NODE_PLAYBACK_IMAGE = ALBUM_NODE_PLAYBACK_IMGDESC
	.createImage();
	private static final Image ALBUM_NODE_LIVE_RECORDING_IMAGE = ALBUM_NODE_LIVE_RECORDING_IMGDESC
	.createImage();
	private static final Image ALBUM_NODE_ERROR_IMAGE = ALBUM_NODE_ERROR_IMGDESC
	.createImage();
	private static final Image SNAPSHOT_NODE_IMAGE = SNAPSHOT_NODE_IMGDESC
			.createImage();
	private static final Image SNAPSHOT_CURRENT_NODE_IMAGE = SNAPSHOT_CURRENT_NODE_IMGDESC
	.createImage();

	private TreeViewer viewer;
	private Action refreshAction;
	private Action launchAction;
	private Action importAction;
	private IContextActivation contextActivation;

	// private Action propertiesAction;

	class ViewLabelProvider extends ColumnLabelProvider {

		public String getText(Object obj) {
			TreeNode node = (TreeNode) obj;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				return ((IAlbum) value).getDisplayName();
			} else if (value instanceof Snapshot) {
				if (((Snapshot) value).getSnapshotDisplayName().length() == 0) {
					return ((Snapshot) value).getSnapshotFileName();
				} else {
					return ((Snapshot) value).getSnapshotDisplayName();
				}
			} else {
				return value.toString();
			}
		}

		public Image getImage(Object obj) {
			TreeNode node = (TreeNode) obj;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				IAlbum album = (IAlbum)value;
				if (album.getSnapshots().size() == 0){
					return ALBUM_NODE_ERROR_IMAGE;
				} else {
					if (album.isRecording() && Album.isSnapshotSession(album.getSessionID())){
						return ALBUM_NODE_LIVE_RECORDING_IMAGE;
					} else if (album.isRecording()){
						return ALBUM_NODE_RECORDING_IMAGE;
					} else if (Album.isSnapshotSession(album.getSessionID())){
						return ALBUM_NODE_PLAYBACK_IMAGE;
					} else {
						return ALBUM_NODE_IMAGE; 
					}
				}
			} else if (value instanceof Snapshot) {
				Snapshot snap = (Snapshot)value;
				IAlbum album = snap.getAlbum();
				if (Album.isSnapshotSession(album.getSessionID())){
					EDCLaunch launch = EDCLaunch.getLaunchForSession(album.getSessionID());
					int currIndex = launch.getAlbum().getCurrentSnapshotIndex();
					if (snap.equals(album.getSnapshots().get(currIndex))){
						return SNAPSHOT_CURRENT_NODE_IMAGE;
					} else {
						return SNAPSHOT_NODE_IMAGE;
					}
				} else {
					return SNAPSHOT_NODE_IMAGE;
				}
			}
			return null;
		}
	}

	public void createPartControl(Composite parent) {
		
		Album.addSnapshotAlbumEventListener(this);
		
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		TreeViewerColumn albumColumn = new TreeViewerColumn(viewer, SWT.LEFT);
		albumColumn.setLabelProvider(new TreeColumnViewerLabelProvider(
				new ViewLabelProvider()));
		albumColumn.getColumn().setText("Album");
		albumColumn.setEditingSupport(new NameEditingSupport(albumColumn
				.getViewer()));
		ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(
				albumColumn.getViewer()) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION &&
						getAlbumsFromSnapshotProject().size() == 0){
					importAction.run();
				}
				
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
			}
		};

		TreeViewerEditor.create(viewer, activationStrategy,
				ColumnViewerEditor.DEFAULT);

		TreeViewerColumn dateColumn = new TreeViewerColumn(viewer, SWT.LEFT);
		dateColumn.setLabelProvider(new DateLabelProvider());
		dateColumn.getColumn().setText("Date");

		TreeViewerColumn locationCol = new TreeViewerColumn(viewer, SWT.LEFT);
		locationCol.setLabelProvider(new LocationLabelProvider());
		locationCol.getColumn().setText("Location");
		locationCol.setEditingSupport(new LocationEditingSupport(locationCol.getViewer()));

		viewer.setContentProvider(new TreeNodeContentProvider());
		viewer.setInput(loadAlbums());
		viewer.getTree().setHeaderVisible(true);

		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Object value1 = ((TreeNode) e1).getValue();
				Object value2 = ((TreeNode) e2).getValue();
				if (value1 instanceof IAlbum)
					return ((IAlbum) value1).getDisplayName().compareToIgnoreCase(
							((IAlbum) value1).getDisplayName());
				else if (value1 instanceof Snapshot)
					return ((Snapshot) value1).getSnapshotFileName()
							.compareToIgnoreCase(
									((Snapshot) value2).getSnapshotFileName());

				return 0;
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				boolean enabled = false;
				if (!selection.isEmpty()) {
					enabled = true;
					TreeNode node = (TreeNode) ((IStructuredSelection) selection)
							.getFirstElement();
					Object value = node.getValue();

					if (value instanceof IAlbum) {
						IAlbum album = (IAlbum)value;
						enabled = !album.isRecording() && !Album.isSnapshotSession(album.getSessionID());
					} else if (value instanceof Snapshot) {
						Snapshot snapshot = (Snapshot)value;
						enabled = !snapshot.getAlbum().isRecording() && !Album.isSnapshotSession(snapshot.getAlbum().getSessionID());
					}
				}

				launchAction.setEnabled(enabled);
			}
		});

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"org.eclipse.cdt.debug.edc.ui.views");
		packColumns();
		makeActions();
		hookContextMenu();
		contributeToActionBars();

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
    	if (ctxService != null) {
    		contextActivation= ctxService.activateContext(CONTEXT_ID);
    	}
    	
		// This is needed to support the selection-based enablement expression
		// we specify in the plugin xml for the Delete command
    	getSite().setSelectionProvider(viewer);
	}

	private void packColumns() {
		TreeColumn[] columns = viewer.getTree().getColumns();
		for (TreeColumn column : columns) {
			column.pack();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private class DateLabelProvider extends ColumnLabelProvider {

		public String getText(Object obj) {

			TreeNode node = (TreeNode) obj;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				return new Date(((IAlbum) value).getLocation().toFile()
						.lastModified()).toString();
			} else if (value instanceof Snapshot) {
				if (((Snapshot) value).getCreationDate() != null) {
					return ((Snapshot) value).getCreationDate();
				}
			}

			return "";
		}
	}

	private class LocationLabelProvider extends ColumnLabelProvider {

		public String getText(Object obj) {
			TreeNode node = (TreeNode) obj;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				return ((IAlbum) value).getLocation().toOSString();
			} else if (value instanceof Snapshot){
				Snapshot snap = ((Snapshot) value);
				if (snap.getReferenceLocationSourceFile().length() > 0 && snap.getReferenceLocationLineNumber() > 0){
					return snap.getReferenceLocationSourceFile() + ":" + snap.getReferenceLocationLineNumber();
				}
			}

			return "";
		}
	}

	private class LocationEditingSupport extends EditingSupport {
		private CellEditor editor;

		public LocationEditingSupport(ColumnViewer viewer) {
			super(viewer);
			editor = new TextCellEditor(((TreeViewer) viewer).getTree());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			TreeNode node = (TreeNode) element;
			Object nodeValue = node.getValue();
			if (nodeValue instanceof IAlbum) {
				return ((IAlbum) nodeValue).getLocation().toOSString();
			} else if (nodeValue instanceof Snapshot){
				Snapshot snap = (Snapshot) nodeValue;
				if (snap.getReferenceLocationSourceFile().length() > 0 && snap.getReferenceLocationLineNumber() > 0){
					return snap.getReferenceLocationSourceFile() + ":" + snap.getReferenceLocationLineNumber();
				}
			}

			return "";
		}

		@Override
		protected void setValue(Object element, Object value) {
			// does not allow changing album/snapshot location
		}
	}

	private class NameEditingSupport extends EditingSupport {
		private TextCellEditor editor;

		private NameEditingSupport(ColumnViewer viewer) {
			super(viewer);
			editor = new TextCellEditor((Composite) viewer.getControl(),
					SWT.BORDER);
		}

		@Override
		protected boolean canEdit(Object element) {
			if ((((TreeNode) element).getValue() instanceof IAlbum)
					|| (((TreeNode) element).getValue() instanceof Snapshot))
				return true;

			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			TreeNode node = (TreeNode) element;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				return ((IAlbum) value).getDisplayName();
			} else if (value instanceof Snapshot) {
				return ((Snapshot) value).getSnapshotDisplayName();
			}

			return null;
		}

		@Override
		protected void setValue(Object element, Object newValue) {
			TreeNode node = (TreeNode) element;
			Object value = node.getValue();
			if (value instanceof IAlbum) {
				((Album)value).updateSnapshotMetaData(newValue.toString(), null);
			} else if (value instanceof Snapshot) {
				if (!((Snapshot) value).getSnapshotDisplayName().equals(
						newValue.toString())) {
					((Snapshot) value).setSnapshotDisplayName(newValue
							.toString());
					((Snapshot) value).getAlbum().updateSnapshotMetaData(null,
							(Snapshot) value);
				}
			}
			viewer.refresh();
		}
	}

	private void makeActions() {
		// refresh the snapshot tree
		refreshAction = new Action() {
			public void run() {
				try {
					Object[] expanded = viewer.getExpandedElements();
					// TODO: loadAlbums is too heavy. We need to make sure we only load albums
					// that have changed. For now we are loading everything again on a refresh
					// which will get to be slow.
					TreeNode[] newTree = loadAlbums();
					viewer.setInput(newTree);
					packColumns();
					if (viewer.getTree().getItems().length == 1){
						viewer.expandAll();
					} else
					{
						// Expand any albums recording or in playback mode...
						for (Object newNode : newTree){
							if (newNode instanceof TreeNode){
								if (((TreeNode) newNode).getValue() instanceof IAlbum){
									IAlbum album = (IAlbum)((TreeNode) newNode).getValue();
									if (album.isRecording() || Album.isSnapshotSession(album.getSessionID())){
										viewer.setExpandedState(((TreeNode) newNode), true);
									}
								}
							}
						}
						// ...then expand any nodes that were already expanded
						for (Object expandedNode : expanded){
							for (Object newNode : newTree){
								if (newNode instanceof TreeNode){
									if (((TreeNode) newNode).getValue() instanceof IAlbum){
										IAlbum album = (IAlbum)((TreeNode) newNode).getValue();
										if (album.isRecording()){
											viewer.setExpandedState(((TreeNode) newNode), true);
											break;
										}
										Object t1 = ((TreeNode) newNode).getValue();
										Object t2 = ((TreeNode) expandedNode).getValue();
										String newAlbumName =  ((IAlbum)t1).getDisplayName();
										String oldAlbumName =  ((IAlbum)t2).getDisplayName();
										if ( newAlbumName.equals(oldAlbumName) ){
											viewer.setExpandedState(((TreeNode) newNode), true);
											break;
										}
									}
								}	
								
							}
						}
					}
					viewer.refresh();
					
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refreshes snapshot information");
		refreshAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
						"/icons/etool16/refresh.gif")); //$NON-NLS-1$

		// launch a snapshot
		launchAction = new Action() {
			public void run() {
				try {
					TreeNode node = (TreeNode) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					Object value = node.getValue();
					if (value instanceof IAlbum) {
						SnapshotUtils.launchAlbumSession((Album) value);
					} else if (value instanceof Snapshot) {
						// launch selected snapshot, set proper index in album 						// first
						Album album = ((Snapshot) value).getAlbum();
						int index = album.getIndexOfSnapshot((Snapshot) value);
						SnapshotUtils.launchAlbumSession(((Snapshot) value)
								.getAlbum());
						album.setCurrentSnapshotIndex(index);
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		};
		launchAction.setText("Launch Snapshot");
		launchAction.setToolTipText("Launches the selected snapshot");
		launchAction.setImageDescriptor(PLAY_SNAPSHOT_IMGDESC); //$NON-NLS-1$

		// properties of a snapshot
		// propertiesAction = new Action() {
		// public void run() {
		// try {
		// // TODO: Do it!
		// MessageDialog.openError(viewer.getControl().getShell(), "TODO",
		// "Not yet implemented.");
		// } catch (Exception x) {
		// x.printStackTrace();
		// }
		// }
		// };
		// propertiesAction.setText("Snapshot properties");
		// propertiesAction.setToolTipText("View information about the snapshot");
		// propertiesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		// getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		// compare snapshots
		// compareAction = new Action() {
		// public void run() {
		// try {
		// // TODO: Do it!
		// MessageDialog.openError(viewer.getControl().getShell(), "TODO",
		// "Not yet implemented.");
		// } catch (Exception x) {
		// x.printStackTrace();
		// }
		// }
		// };
		// compareAction.setText("Compare snapshots");
		// compareAction.setToolTipText("Compares two selected snapshots");
		// compareAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		// getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		// delete album or snapshot

		// import album
		importAction = new Action() {
			public void run() {
				try {

					FileDialog fd = new FileDialog(viewer.getControl()
							.getShell());
					fd.setFilterExtensions(new String[] { "*.dsa" });
					String fileSelected = fd.open();
					if (fileSelected != null) {
						IProject project = SnapshotUtils.getSnapshotsProject();
						IFile f = project.getFile(new File(fileSelected)
								.getName());
						f.createLink(Path.fromOSString(fileSelected),
								IResource.ALLOW_MISSING_LOCAL
										| IResource.BACKGROUND_REFRESH, null);
						f.refreshLocal(IResource.DEPTH_ONE, null);
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
						refreshAction.run();
					}

				} catch (CoreException x) {
					ErrorDialog.openError(viewer.getControl().getShell(), "Import Error",
							"Failed to import snapshot.", x.getStatus()); 
				} catch (Exception x) {
					ErrorDialog.openError(viewer.getControl().getShell(), "Import Error",
							null, EDCDebugUI.dsfRequestFailedStatus("Failed to import snapshot.", x));
				}
			}
		};
		importAction.setText("Import album");
		importAction.setToolTipText("Import an existing snapshot album");
		importAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));

	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(launchAction);
		manager.add(importAction);

		launchAction.setEnabled(false);

		manager.add(new Separator());
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SnapshotView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(launchAction);
		ISelection selection = viewer.getSelection();
		if (selection.isEmpty())
			return;
		TreeNode node = (TreeNode) ((IStructuredSelection) selection)
				.getFirstElement();
		Object value = node.getValue();
		boolean enabled = false;
		if (value instanceof IAlbum) {
			launchAction.setText("Launch Album");
			IAlbum album = (IAlbum)value;
			enabled = !Album.isSnapshotSession(album.getSessionID());
		} else if (value instanceof Snapshot) {
			launchAction.setText("Launch Snapshot");
			Snapshot snapshot = (Snapshot)value;
			enabled = !Album.isSnapshotSession(snapshot.getAlbum().getSessionID());
		}
		
		launchAction.setEnabled(enabled);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(importAction);
		manager.add(launchAction);
		// manager.add(compareAction);
		// manager.add(propertiesAction);
		manager.add(new Separator());
	}

	private TreeNode[] loadAlbums() {

		List<Album> albumList = getAlbumsFromSnapshotProject();
		List<TreeNode> albumNodes = new ArrayList<TreeNode>();

		if (albumList.size() == 0) {
			TreeNode emptyAlbum = new TreeNode(
					"Click the “Camera” button in the Debug View to create a new Album or double-click here to import an existing one.");
			albumNodes.add(emptyAlbum);
			return (TreeNode[]) albumNodes.toArray(new TreeNode[albumNodes
					.size()]);
		}

		for (IAlbum a : albumList) {
			TreeNode albumNode = new TreeNode(a);
			List<Snapshot> snaps = a.getSnapshots();
			List<TreeNode> snapshotNodes = new ArrayList<TreeNode>();

			for (Snapshot s : snaps) {
				snapshotNodes.add(new TreeNode(s));
			}

			for (TreeNode node : snapshotNodes) {
				node.setParent(albumNode);
			}
			albumNode.setChildren((TreeNode[]) snapshotNodes
					.toArray(new TreeNode[snapshotNodes.size()]));
			albumNodes.add(albumNode);
		}

		return (TreeNode[]) albumNodes.toArray(new TreeNode[albumNodes.size()]);
	}

	private List<Album> getAlbumsFromSnapshotProject() {
		List<Album> albumList = new ArrayList<Album>();

		// See if the default project exists
		String defaultProjectName = "Snapshots";
		ICProject cProject = CoreModel.getDefault().getCModel().getCProject(defaultProjectName);

		if (cProject == null || !cProject.exists()) {
			return albumList;
		}

		// Get all .dsa files from Snapshots project
		try {
			IResource[] resources = cProject.getProject().members();
			for (IResource resource : resources) {

				try {
					if (resource.getType() != IResource.FOLDER && resource.getFullPath().getFileExtension().equalsIgnoreCase("dsa") && resource.exists()) {
						Album album = Album.getAlbumByLocation(resource.getRawLocation());// ??????
						if (album == null) {
							album = new Album();
							album.setLocation(resource.getRawLocation());
							album.loadAlbumMetada(true);
						}
						albumList.add(album);
					}

				} catch (Exception e) {
					// ignored
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return albumList;
	}
	
	/**
	 * Recursively delete a directory and it's contents
	 * @param dir
	 * @return
	 */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

	@Override
	public void dispose() {
		if (contextActivation != null) {
			IContextService ctxService = (IContextService)getSite().getService(IContextService.class);
	    	if (ctxService != null) {
	    		ctxService.deactivateContext(contextActivation);
	    	}
		}

	}

	/**
	 * Called by the Delete command handler after it has removed an album or
	 * snapshot
	 */
	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getControl().isDisposed())
					refreshAction.run();
			}
		});

	}

	private void revealSnapshot(final Snapshot snapshot) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getControl().isDisposed()) {
					TreeNode[] nodes = (TreeNode[]) viewer.getInput();
					Album album = snapshot.getAlbum();
					for (TreeNode treeNode : nodes) {
						if (treeNode.getValue().equals(album))
						{
							TreeNode[] children = treeNode.getChildren();
							for (TreeNode snapNode : children) {
								if (snapNode.getValue().equals(snapshot))
								{
									viewer.reveal(snapNode);
									break;
								}
							}
							break;
						}
					}
				}
			}
		});
	}
	
	public void snapshotCreated(final Album album, Snapshot snapshot,
			DsfSession session, StackFrameDMC stackFrame) {
		refresh();
		revealSnapshot(snapshot);
	}

	public void snapshotOpened(Snapshot snapshot) {
		refresh();
		revealSnapshot(snapshot);
	}

	public void snapshotSessionEnded(Album album, DsfSession session) {
		refresh();
	}

}
