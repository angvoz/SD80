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
package org.eclipse.cdt.debug.edc.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.launch.IEDCLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class TestUtils {

	public interface Condition {

		boolean isConditionValid();

	}

	public static final long DEFAULT_WAIT_TIMEOUT = 60000;
	public static final long DEFAULT_WAIT_INTERVAL = 10;

	public static void wait(Condition condition) throws InterruptedException {
		wait(condition, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
	}

	public static void waitOnExecutorThread(DsfSession session, Condition condition) throws Exception {
		waitOnExecutorThread(session, condition, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
	}

	public static void wait(Condition condition, long timeout) throws InterruptedException {
		wait(condition, timeout, DEFAULT_WAIT_INTERVAL);
	}

	public static void waitOnExecutorThread(DsfSession session, Condition condition, long timeout) throws Exception {
		waitOnExecutorThread(session, condition, timeout, DEFAULT_WAIT_INTERVAL);
	}

	static public class ConditionQuery extends Query<Boolean> {

		private final Condition condition;

		public ConditionQuery(Condition condition) {
			super();
			this.condition = condition;
		}

		@Override
		protected void execute(DataRequestMonitor<Boolean> rm) {
			rm.setData(condition.isConditionValid());
			rm.done();
		}
	};

	public static void waitOnExecutorThread(DsfSession session, final Condition condition, final long timeout,
			final long interval) throws Exception {

		long limit = System.currentTimeMillis() + timeout;

		ConditionQuery conditionRunnable = new ConditionQuery(condition);
		session.getExecutor().execute(conditionRunnable);
		boolean conditionValid = conditionRunnable.get();

		while (!conditionValid) {
			Thread.sleep(interval);
			if (System.currentTimeMillis() > limit)
				throw new AssertionError();
			conditionRunnable = new TestUtils.ConditionQuery(condition);
			session.getExecutor().execute(conditionRunnable);
			conditionValid = conditionRunnable.get();
		}

	}

	public static void wait(Condition condition, long timeout, long interval) throws InterruptedException {
		long limit = System.currentTimeMillis() + timeout;
		while (!condition.isConditionValid()) {
			Display display = Display.getCurrent();
			if (display != null && !display.readAndDispatch())
				display.sleep();
			Thread.sleep(interval);
			if (System.currentTimeMillis() > limit)
				throw new AssertionError();
		}
	}

	public static String[] getFileNamesByExtension(final String folderName, final String fileExtension) {
		File folder = new File(folderName);

		String[] files = folder.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (fileExtension == null || fileExtension.length() == 0)
					return true;
				return name.endsWith(fileExtension);
			}

		});

		return files;
	}

	public static String[] getFileFullNamesByExtension(final String folderName, final String fileExtension) {
		String[] files = getFileNamesByExtension(folderName, fileExtension);

		for (int i = 0; i < files.length; i++) {
			files[i] = folderName + File.separatorChar + files[i];
		}

		return files;
	}

	public static boolean stringCompare(String s1, String s2, boolean ignoreCase, boolean ignoreWhite, boolean ignore0x) {
		if (ignoreWhite) {
			s1 = s1.replaceAll(" ", "").replaceAll("\t", "");
			s2 = s2.replaceAll(" ", "").replaceAll("\t", "");
		}

		if (ignore0x) {
			s1 = s1.replaceAll("0x", "");
			s2 = s2.replaceAll("0x", "");
		}

		if (ignoreCase)
			return s1.equalsIgnoreCase(s2);
		else
			return s1.equals(s2);
	}

	public static void showPerspective(String perspective) throws WorkbenchException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		workbench.showPerspective(perspective, activeWindow);
	}

	public static void disableDebugPerspectiveSwitchPrompt() {
		if (null == Display.getCurrent() || null == Display.getCurrent().getActiveShell()) // in case test is run in headless mode.
			return;
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		store.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.ALWAYS);
	}

	public static EDCLaunch createLaunchForAlbum(String albumName) throws Exception {
		String res_folder = EDCTestPlugin.projectRelativePath("resources/Snapshots");
		return createLaunchForAlbum(albumName, res_folder);
	}

	public static EDCLaunch createLaunchForAlbum(String albumName, String res_folder) throws Exception {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationWorkingCopy configuration = lm.getLaunchConfigurationType(
				"org.eclipse.cdt.debug.edc.snapshot").newInstance(null, "TestAlbumLaunch");
		IPath dsaPath = new Path(res_folder);
		dsaPath = dsaPath.append(albumName);

		configuration.setAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, dsaPath.toOSString());

		final EDCLaunch[] launchHolder = new EDCLaunch[1];

		ILaunchListener listener = new ILaunchListener() {

			public void launchRemoved(ILaunch launch) {
			}

			public void launchChanged(ILaunch launch) {
			}

			public void launchAdded(ILaunch launch) {
				if (launch instanceof EDCLaunch) {
					EDCLaunch edcLaunch = (EDCLaunch) launch;
					launchHolder[0] = edcLaunch;
				}
			}
		};
		lm.addLaunchListener(listener);
		ILaunch launch = configuration.doSave().launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), true);
		if (launch == null)
			return null;
		
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				return launchHolder[0] != null;
			}
		});

		lm.removeLaunchListener(listener);
		
		Assert.assertNotNull(launchHolder[0]);
		return launchHolder[0];
	}

	public static DsfSession waitForSession(final EDCLaunch launch) throws InterruptedException {
		final DsfSession sessionHolder[] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				DsfSession session = launch.getSession();
				if (session == null)
					return false;

				sessionHolder[0] = session;
				return true;
			}
		});
		return sessionHolder[0];
	}

	public static IEDCExecutionDMC waitForExecutionDMC(final DsfSession session) throws Exception {
		final IEDCExecutionDMC contextHolder[] = { null };
		TestUtils.waitOnExecutorThread(session, new Condition() {
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService == null)
					return false;
				ExecutionDMC rootDMC = runControlService.getRootDMC();
				if (rootDMC == null)
					return false;
				IEDCExecutionDMC[] processes = rootDMC.getChildren();
				if (processes.length == 0)
					return false;

				contextHolder[0] = processes[0];
				return true;
			}

		});
		return contextHolder[0];
	}

	public static IFrameDMContext waitForStackFrame(final DsfSession session, final IEDCExecutionDMC threadDMC)
			throws Exception {
		return waitForStackFrame(session, threadDMC, 0);
	}

	public static IFrameDMContext waitForStackFrame(final DsfSession session, final IEDCExecutionDMC threadDMC, final int level)
			throws Exception {
		final IFrameDMContext frameHolder[] = { null };
		TestUtils.waitOnExecutorThread(session, new Condition() {
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Stack stackService = servicesTracker.getService(Stack.class);
				if (stackService == null)
					return false;
				IFrameDMContext[] frames = stackService.getFramesForDMC(threadDMC, 0, IStack.ALL_FRAMES);
				if (frames.length > level) {
					frameHolder[0] = frames[level];
					return true;
				}
				return false;
			}
			
		});
		return frameHolder[0];
	}
	
	
	public static ExecutionDMC waitForSuspendedThread(final DsfSession session) throws Exception {
		final ExecutionDMC contextHolder[] = { null };
		TestUtils.waitOnExecutorThread(session, new Condition() {
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService == null)
					return false;
				ExecutionDMC rootDMC = runControlService.getRootDMC();
				if (rootDMC == null)
					return false;
				ExecutionDMC[] processes = rootDMC.getChildren();
				if (processes.length == 0)
					return false;

				for (ExecutionDMC process : processes) {
					ExecutionDMC[] threads = process.getChildren();
					for (ExecutionDMC thread : threads) {
						if (thread.isSuspended()) {
							contextHolder[0] = thread;
							return true;
						}
					}
				}
				return contextHolder[0] != null;
			}

		});
		return contextHolder[0];
	}

	/**
	 * Get only the formatted value of an expression.
	 * @param session
	 * @param frame
	 * @param expr
	 * @return
	 * @throws Exception
	 * @throws ExecutionException
	 */
	public static String getExpressionValue(final DsfSession session, final IDMContext frame, final String expr)
			throws Exception, ExecutionException {

		IEDCExpression expression = getExpressionDMC(session, frame, expr);
		String formatted = getFormattedExpressionValue(session, frame, expression);
		
		return formatted;
	}
	
	/**
	 * Get an evaluated expression context.
	 * @param session
	 * @param frame
	 * @param expr
	 * @return
	 * @throws Exception
	 * @throws ExecutionException
	 */
	public static IEDCExpression getExpressionDMC(final DsfSession session, final IDMContext frame, final String expr)
		throws Exception, ExecutionException {
		
		Query<IEDCExpression> runnable = new Query<IEDCExpression>() {
			
			@Override
			protected void execute(DataRequestMonitor<IEDCExpression> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Expressions expressionsService = servicesTracker.getService(Expressions.class);
				IEDCExpression expression = (IEDCExpression) expressionsService.createExpression(frame, expr);
				expression.evaluateExpression();
				rm.setData(expression);
				rm.done();
			}
		};
		
		session.getExecutor().execute(runnable);
		
		return runnable.get();
	}


	/**
	 * Get the formatted string value of an expression context.
	 * @param session
	 * @param frame
	 * @param expression
	 * @return
	 * @throws Exception
	 * @throws ExecutionException
	 */
	public static String getFormattedExpressionValue(final DsfSession session, final IDMContext frame, final IEDCExpression expression)
		throws Exception, ExecutionException {
		
		Query<String> runnable = new Query<String>() {
		
			@Override
			protected void execute(DataRequestMonitor<String> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Expressions expressionsService = servicesTracker.getService(Expressions.class);
				FormattedValueDMContext fvc = expressionsService.getFormattedValueContext(expression,
						IFormattedValues.NATURAL_FORMAT);
				FormattedValueDMData formattedValue = expression.getFormattedValue(fvc);
				IType exprType = TypeUtils.getStrippedType(expression.getEvaluatedType());
				IVariableValueConverter customValue = 
					FormatExtensionManager.instance().getVariableValueConverter(exprType);
				if (customValue != null) {
					FormattedValueDMData customFormattedValue = null;
					try {
						customFormattedValue = new FormattedValueDMData(customValue.getValue(expression));
						formattedValue = customFormattedValue;
					}
					catch (CoreException e) {
						// Checked exception like failure in reading memory.
						// Pass the error to the RM so that it would show up in UI. 
						rm.setStatus(e.getStatus());
						rm.done();
						return;
					}
					catch (Throwable t) {
					}
				}
				
				rm.setData(formattedValue.getFormattedValue());
				rm.done();
			}
		};
		
		session.getExecutor().execute(runnable);
		
		return runnable.get();
	}

	/**
	 * Get a casted expression.
	 * @param session
	 * @param frame
	 * @param expr
	 * @return casted expression DMC
	 * @throws Exception
	 * @throws ExecutionException
	 */
	public static ICastedExpressionDMContext getCastedExpressionValue(final DsfSession session, final IDMContext frame, final String expr, final String type)
			throws Exception, ExecutionException {

		IEDCExpression expression = getExpressionDMC(session, frame, expr);
		
		CastInfo castInfo = new CastInfo(type);
		
		ICastedExpressionDMContext castedDMC = getCastedExpressionDMC(session, frame, expression, castInfo);
		
		return castedDMC;
	}
	
	/**
	 * @param session
	 * @param frame
	 * @param expression
	 * @param castInfo
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static ICastedExpressionDMContext getCastedExpressionDMC(
			final DsfSession session, final IDMContext frame, final IExpressionDMContext expression,
			final CastInfo castInfo) throws InterruptedException, ExecutionException {
		Query<ICastedExpressionDMContext> runnable = new Query<ICastedExpressionDMContext>() {
			
			@Override
			protected void execute(DataRequestMonitor<ICastedExpressionDMContext> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				IExpressions2 expressionsService = servicesTracker.getService(IExpressions2.class);
				rm.setData(expressionsService.createCastedExpression(expression, castInfo));
				rm.done();
			}
		};
		
		session.getExecutor().execute(runnable);
		
		return runnable.get();
	}

	/**
	 * Get an evaluated expression context.
	 * @param session
	 * @param frame
	 * @param expr
	 * @return
	 * @throws Exception
	 * @throws ExecutionException
	 */
	public static IExpressionDMContext[] getSubExpressionDMCs(final DsfSession session, final IDMContext frame, 
			final IExpressionDMContext expr)
		throws Exception, ExecutionException {
		
		Query<IExpressionDMContext[]> runnable = new Query<IExpressionDMContext[]>() {
			
			@Override
			protected void execute(DataRequestMonitor<IExpressionDMContext[]> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Expressions expressionsService = servicesTracker.getService(Expressions.class);
				expressionsService.getSubExpressions(expr, rm);
			}
		};
		
		session.getExecutor().execute(runnable);
		
		return runnable.get();
	}

	public static DsfServicesTracker getDsfServicesTracker(final DsfSession session) {
		return new DsfServicesTracker(EDCTestPlugin.getBundleContext(), session.getId());
	}

	/** Tell if a given launcher is available.  Useful when a snapshot test depends on an internal
	 * launch type.
	 * @param id the id of the org.eclipse.debug.core.launchConfigurationTypes launchConfigurationType
	 * @return true if found
	 */
	public static boolean hasLaunchConfiguationType(String id) {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(id) != null;
	}

	/**
	 * Tell whether there is a TCF agent launcher available with the given ID.
	 * @param reqdLauncher
	 * @return
	 */
	public static boolean hasTCFAgentLauncher(String id) {
		TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
		ITCFAgentLauncher[] registered = tcfServiceManager.getRegisteredAgents(IRunControl.NAME, Collections.<String, String>emptyMap());
		for (ITCFAgentLauncher launcher : registered) {
			if (launcher.getClass().getName().equals(id))
				return true;
		}
		return false;
	}

	public static void shutdownDebugSession(EDCLaunch launch, final DsfSession session) {
		final Boolean done[] = new Boolean[] {false};
		
		// shutdown the launch
		if (launch != null) {
			// terminating the launch will cause the session to end, but wait for
			// it to end to prevent multiple launches from tests existing at the
			// same time which can cause some weird behavior
			DsfSession.addSessionEndedListener(new DsfSession.SessionEndedListener() {
				
				public void sessionEnded(DsfSession se) {
					if (session == se) {
						done[0] = true;
					}
				}
			});
			
			try {
				launch.terminate();
			} catch (DebugException de) {
			}
			launch = null;
			
			while (! done[0]) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
