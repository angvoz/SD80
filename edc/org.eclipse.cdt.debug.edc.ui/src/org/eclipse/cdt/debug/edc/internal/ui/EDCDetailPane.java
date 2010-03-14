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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.ui.EDCDebugUI;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
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
public class EDCDetailPane extends AbstractEDCDetailPane {
	
	public class SetNumberFormatAction extends Action {

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
			setEnabled(true);
			currentFormat = IFormattedValues.NATURAL_FORMAT;
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
							for (String formatId : FORMAT_IDS) {
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


		@Override
		public void run() {
			super.run();
		}
	}

	public class DisplayDetailJob extends DetailJob {
		private class GetValueQuery extends Query<String> {

			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				try {
					Expressions expressions = (Expressions) expressionDMC.getService();
					String value = expressions.getExpressionValue(expressionDMC, currentFormat);
					rm.setData(value);
				} catch (Throwable t) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, t.getMessage()));
				}
				finally {
					rm.done();
				}
			}
			
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				DisplayDetailJob.this.cancel();
				return super.cancel(mayInterruptIfRunning);
			}
		}
		
		public DisplayDetailJob(IStructuredSelection selection) {
			super("compute variable details");
			setExpressionDMC(getExpressionFromSelectedElement(selection.getFirstElement()));
		}
	
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			GetValueQuery getValueQuery = new GetValueQuery();
			expressionDMC.getService().getExecutor().execute(getValueQuery);
			String text;
			try {
				text = getValueQuery.get();
			} catch (final Throwable e) {
				text = MessageFormat.format("Could not get value: {0}", e.getMessage());
			}
			final String _text = text;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					document.set(_text);
				}
			});
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
		private class SetValueQuery extends Query<Object> {

			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				try {
					Expressions expressions = (Expressions) expressionDMC.getService();
					expressions.writeExpression(expressionDMC, newValue, currentFormat, rm); // will call rm.done()
				} catch (Throwable t) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, t.getMessage()));
				}
			}
			
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				SetValueJob.this.cancel();
				return super.cancel(mayInterruptIfRunning);
			}
		}
		
		private final String newValue;
	
		public SetValueJob(IStructuredSelection selection, String newValue) {
			super("set variable value");
			setExpressionDMC(getExpressionFromSelectedElement(selection.getFirstElement()));
			this.newValue = newValue;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SetValueQuery setValueQuery = new SetValueQuery();
			expressionDMC.getService().getExecutor().execute(setValueQuery);
			try {
				setValueQuery.get();
			} catch (final Throwable e) {
				return new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, 
						MessageFormat.format("Could not set custom value: {0}", e.getMessage()));
			}
			return Status.OK_STATUS;
		}
	}

	public static final String ID = "EDCDetailPane";
	public static final String DESCRIPTION = "EDC detail pane";
	public static final String NAME = "EDC Variable Details";
    private static final List<String> FORMAT_IDS = new ArrayList<String>(); 
    static {
    	FORMAT_IDS.add(IFormattedValues.NATURAL_FORMAT);
    	FORMAT_IDS.add(IFormattedValues.HEX_FORMAT);
    	FORMAT_IDS.add(IFormattedValues.DECIMAL_FORMAT);
    	FORMAT_IDS.add(IFormattedValues.OCTAL_FORMAT);
    	FORMAT_IDS.add(IFormattedValues.BINARY_FORMAT);
    }
	
	protected static final String NUMBER_FORMAT_ACTION = "setNumberFormat"; //$NON-NLS-1$
	public String currentFormat;
	
	@Override
	protected Job createDisplayDetailJob(IStructuredSelection selection) {
		if (selection.isEmpty())
			return null;
		return new DisplayDetailJob(selection);
	}

	@Override
	protected boolean canSetValue(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		final ExpressionDMC expressionDMC = getExpressionFromSelectedElement(selection.getFirstElement());
		Expressions expressions = (Expressions) expressionDMC.getService();
		return expressions.canWriteExpression(expressionDMC);
	}

	@Override
	protected Job createSetValueJob(IStructuredSelection selection, String newValue) {
		if (selection.isEmpty())
			return null;
		return new SetValueJob(selection, newValue);
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		setAction(NUMBER_FORMAT_ACTION, new SetNumberFormatAction());
	}
	
	@Override
	protected void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(new Separator());
		menu.add(getAction(NUMBER_FORMAT_ACTION));
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
