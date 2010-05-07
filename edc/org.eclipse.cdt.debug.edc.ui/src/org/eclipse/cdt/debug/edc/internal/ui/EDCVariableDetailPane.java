/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

@SuppressWarnings("restriction")
public class EDCVariableDetailPane extends AbstractEDCDetailPane {
	
	public class SetNumberFormatAction extends Action {

		public String currentFormat;
	    private List<String> formatList;

		private class SingleNumberFormatAction extends Action {
	        private final String formatId;
	        SingleNumberFormatAction(String formatId) {
	            super(FormattedValueVMUtil.getFormatLabel(formatId), AS_RADIO_BUTTON);
	            this.formatId = formatId;
	        }

	        @Override
	        public void run() {
	            if (isChecked()) {
	                currentFormat = formatId;
	                if (isInView())
	                	redisplay();
	            }
	        }
	    }
	 
		public SetNumberFormatAction() {
			super("Number Format", IAction.AS_DROP_DOWN_MENU);
			formatList = new ArrayList<String>();
			setMenuCreator(new IMenuCreator() {
				public Menu getMenu(Menu parent) {
					Menu menu = new Menu(parent);
					menu.addMenuListener(new MenuAdapter() {
						@Override
						public void menuShown(MenuEvent e) {
							fillMenu((Menu) e.widget);
						}

						private void fillMenu(Menu menu) {
							// dispose old items
							for (MenuItem item : menu.getItems())
								item.dispose();
							// create new items
							for (String formatId : formatList) {
								SingleNumberFormatAction action = new SingleNumberFormatAction(formatId);
								if (formatId.equals(currentFormat))
									action.setChecked(true);
								ActionContributionItem item = new ActionContributionItem(action);
								item.fill(menu, -1);
							}
						}
					});
					return menu;
				}
				
				public Menu getMenu(Control parent) {
					return null;
				}
				
				public void dispose() {
				}
			});
		}

		public void setSelection(IStructuredSelection selection) {
			formatList.clear();
			IFormattedDataDMContext context = getContextFromSelection(selection);
			if (context == null)
				return;
			String[] formats = createSyncVariableDataAccess(context).getSupportedFormats(context);
			if (formats == null)
				return;
			formatList.addAll(Arrays.asList(formats));
			if (formatList.remove(IFormattedValues.NATURAL_FORMAT)) // put natural first, if exists
				formatList.add(0, IFormattedValues.NATURAL_FORMAT);
			if ((currentFormat == null || !formatList.contains(currentFormat)) && !formatList.isEmpty())
				currentFormat = formatList.get(0);
		}
		
		@Override
		public boolean isEnabled() {
			return !formatList.isEmpty();
		}
		
		public String getCurrentFormat() {
			if (currentFormat == null)
				currentFormat = IFormattedValues.NATURAL_FORMAT; // default if we get here
			
			return currentFormat;
		}
	}

	public abstract class DetailJob extends Job {
		protected SyncVariableDataAccess syncVariableDataAccess;

		public DetailJob(String name, IFormattedDataDMContext context) {
			super(name);
			syncVariableDataAccess = createSyncVariableDataAccess(context);
		}

	}

	public class DisplayDetailJob extends DetailJob {

		private final IFormattedDataDMContext context;

		public DisplayDetailJob(IFormattedDataDMContext context) {
			super("compute variable details", context);
			this.context = context;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				final String text = syncVariableDataAccess.getFormattedValue(context, getCurrentFormat());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						document.set(text == null ? "" : text); //$NON-NLS-1$
					}
				});
			}
			catch (Throwable t) {
				return EDCDebugUI.newErrorStatus(IDsfStatusConstants.REQUEST_FAILED, "could not compute variable details", t);
			}
			return Status.OK_STATUS;
		}

		@Override
		protected void canceling() {
			super.canceling();
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public class SetValueJob extends DetailJob {

		private final IFormattedDataDMContext context;
		private final String newValue;
	
		public SetValueJob(IFormattedDataDMContext context, String newValue) {
			super("set variable value", context);
			this.context = context;
			this.newValue = newValue;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				syncVariableDataAccess.writeVariable(context, newValue, getCurrentFormat());
			}
			catch (Throwable t) {
				return EDCDebugUI.newErrorStatus(IDsfStatusConstants.REQUEST_FAILED, "set variable value", t);
			}
			return Status.OK_STATUS;
		}
	}

	public static final String ID = "EDCDetailPane";
	public static final String DESCRIPTION = "EDC detail pane";
	public static final String NAME = "EDC Variable Details";
	
	protected static final String NUMBER_FORMAT_ACTION = "setNumberFormat"; //$NON-NLS-1$
	private SetNumberFormatAction setNumberFormatAction;
	
	@Override
	protected Job createDisplayDetailJob(IStructuredSelection selection) {
		IFormattedDataDMContext context = getContextFromSelection(selection);
		if (context != null) {
			return new DisplayDetailJob(context);
		}
		return null;
	}

	@Override
	protected boolean canSetValue(IStructuredSelection selection) {
		IFormattedDataDMContext context = getContextFromSelection(selection);
		if (context != null) {
			return createSyncVariableDataAccess(context).canWriteExpression(context);
		}
		return false;
	}

	@Override
	protected Job createSetValueJob(IStructuredSelection selection, String newValue) {
		IFormattedDataDMContext context = getContextFromSelection(selection);
		if (context != null) {
			return new SetValueJob(context, newValue);
		}
		return null;
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		setNumberFormatAction = new SetNumberFormatAction();
		setAction(NUMBER_FORMAT_ACTION, setNumberFormatAction);
	}
	
	@Override
	protected void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(new Separator());
		menu.add(getAction(NUMBER_FORMAT_ACTION));
	}
	
	public IFormattedDataDMContext getContextFromSelection(IStructuredSelection selection) {
		if (selection != null && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IAdaptable) {
				return (IFormattedDataDMContext) ((IAdaptable) element).getAdapter(IFormattedDataDMContext.class);
			}
		}
		return null;
	}

	@Override
	public void display(IStructuredSelection selection) {
		setNumberFormatAction.setSelection(selection);
		super.display(selection);
	}

	protected String getCurrentFormat() {
		return setNumberFormatAction.getCurrentFormat();
	}

	public String getID() {
		return ID;
	}

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return DESCRIPTION;
	}
}
