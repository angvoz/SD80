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
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.launch.IEDCLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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

	public static void disableDebugPerspectiveSwitchPrompt() {
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
		configuration.doSave().launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), true);

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

	public static IFrameDMContext waitForStackFrame(final DsfSession session, final ExecutionDMC threadDMC)
			throws Exception {
		final IFrameDMContext frameHolder[] = { null };
		TestUtils.waitOnExecutorThread(session, new Condition() {
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Stack stackService = servicesTracker.getService(Stack.class);
				if (stackService == null)
					return false;
				IFrameDMContext[] frames = stackService.getFramesForDMC(threadDMC, 0, IStack.ALL_FRAMES);
				if (frames.length > 0) {
					frameHolder[0] = frames[0];
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

	public static String getExpressionValue(final DsfSession session, final IDMContext frame, final String expr)
			throws Exception, ExecutionException {

		Query<String> runnable = new Query<String>() {

			@Override
			protected void execute(DataRequestMonitor<String> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				Expressions expressionsService = servicesTracker.getService(Expressions.class);
				ExpressionDMC expression = (ExpressionDMC) expressionsService.createExpression(frame, expr);
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

	public static DsfServicesTracker getDsfServicesTracker(final DsfSession session) {
		return new DsfServicesTracker(EDCTestPlugin.getBundleContext(), session.getId());
	}

}
