/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
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
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;

public class CustomFormatDetailPane extends AbstractEDCDetailPane {
	
	public abstract class CustomDetailJob extends Job {
		public CustomDetailJob(String name) {
			super(name);
		}

		protected IEDCExpression expressionDMC;
		protected IVariableValueConverter customConverter;
		
		protected void setExpressionDMC(IEDCExpression expressionDMC) {
			this.expressionDMC = (IEDCExpression) expressionDMC;
		}
		
		protected void setCustomConverter(IVariableValueConverter customConverter) {
			this.customConverter = customConverter;
		}
	}
	
	public class DisplayDetailJob extends CustomDetailJob {
		private class GetCustomValueQuery extends Query<String> {

			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				try {
					rm.setData(customConverter.getValue(expressionDMC));
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
			setExpressionDMC(EDCDetailPaneFactory.getExpressionFromSelectedElement(selection.getFirstElement()));
			setCustomConverter(getCustomConverter(expressionDMC));
		}
	
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			GetCustomValueQuery customValueQuery = new GetCustomValueQuery();
			try {
				expressionDMC.getExecutor().execute(customValueQuery);
			} catch (RejectedExecutionException e) {
				// is shutting down
				return Status.CANCEL_STATUS;
			}
			String text;
			try {
				text = customValueQuery.get();
			} catch (final Throwable e) {
				text = MessageFormat.format("Could not get custom value: {0}", e.getMessage());
			}
			final String _text = text;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					document.set(_text == null ? "" : _text); //$NON-NLS-1$
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

	public class SetValueJob extends CustomDetailJob {
		private class SetCustomValueQuery extends Query<Object> {

			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				try {
					customConverter.setValue(expressionDMC, newValue);
				} catch (Throwable t) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, t.getMessage()));
				}
				finally {
					rm.done();
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
			setExpressionDMC(EDCDetailPaneFactory.getExpressionFromSelectedElement(selection.getFirstElement()));
			setCustomConverter(getCustomConverter(expressionDMC));
			this.newValue = newValue;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SetCustomValueQuery setValueQuery = new SetCustomValueQuery();
			expressionDMC.getExecutor().execute(setValueQuery);
			try {
				setValueQuery.get();
			} catch (final Throwable e) {
				return new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, 
						MessageFormat.format("Could not set custom value: {0}", e.getMessage()));
			}
			return Status.OK_STATUS;
		}
	}
	
	public static final String ID = "CustomFormatDetailPane";
	public static final String DESCRIPTION = "A detail pane to display custom formats";
	public static final String NAME = "Custom Format Details";
	
	public static IVariableValueConverter getCustomConverter(IEDCExpression expression) {
		IType exprType = TypeUtils.getStrippedType(expression.getEvaluatedType());
		IVariableValueConverter customConverter = 
			FormatExtensionManager.instance().getDetailValueConverter(exprType);
		return customConverter;
	}

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
		IEDCExpression expression = EDCDetailPaneFactory.getExpressionFromSelectedElement(selection.getFirstElement());
		EDCLaunch launch = EDCLaunch.getLaunchForSession(expression.getSessionId());
		if (launch.isSnapshotLaunch())
			return false;
		IVariableValueConverter customConverter = getCustomConverter(expression);
		return customConverter.canEditValue();
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
		// TODO add edit value action
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(new Separator());
		// TODO add edit value action
	}

	public String getDescription() {
		return DESCRIPTION;
	}

	public String getID() {
		return ID;
	}

	public String getName() {
		return NAME;
	}

	@Override
	protected boolean isEditingEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		IEDCExpression expression = EDCDetailPaneFactory.getExpressionFromSelectedElement(selection.getFirstElement());
		if (expression == null)
			return false;
		IVariableValueConverter customConverter = getCustomConverter(expression);
		return customConverter.canEditValue();
	}
}