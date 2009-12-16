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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.Enumerator;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Stack extends AbstractEDCService implements IStack, ICachingService {

	public static final String STACK_FRAME = "stack_frame";

	private final Map<String, List<StackFrameDMC>> stackFrames = Collections
			.synchronizedMap(new HashMap<String, List<StackFrameDMC>>());

	public static class StackFrameData implements IFrameDMData {

		public final IAddress address;
		public final int level;
		public final String function;
		public final String module;
		private final String file;
		private final int lineNumber;

		StackFrameData(StackFrameDMC dmc) {
			level = dmc.getLevel();
			address = dmc.getIPAddress();
			module = dmc.getModuleName();
			file = dmc.getSourceFile();
			lineNumber = dmc.getLineNumber();
			function = dmc.getFunctionName();
		}

		public IAddress getAddress() {
			return address;
		}

		public String getFunction() {
			return function;
		}

		public int getLevel() {
			return level;
		}

		public String getFile() {
			return file;
		}

		public int getLine() {
			return lineNumber;
		}

		public int getColumn() {
			return 0;
		}

		public String getModule() {
			return module;
		}
	}

	public class StackFrameDMC extends DMContext implements IFrameDMContext, Comparable<StackFrameDMC>,
			ISnapshotContributor {

		public static final String PROP_LEVEL = "Level";
		public static final String BASE_ADDR = "Base_address";
		public static final String IP_ADDR = "Instruction_address";
		public static final String MODULE_NAME = "module_name";
		public static final String SOURCE_FILE = "source_file";
		public static final String FUNCTION_NAME = "function_name";
		public static final String LINE_NUMBER = "line_number";

		private final DsfServicesTracker dsfServicesTracker = getServicesTracker();
		private ExecutionDMC executionDMC;
		private int level;
		private IAddress baseAddress;
		private IAddress ipAddress;

		private String moduleName = "";
		private String sourceFile = "";
		private String functionName = "";
		private int lineNumber;
		private List<VariableDMC> locals;
		private List<IEnumerator> enumerators;
		private final Map<String, VariableDMC> localsByName = Collections
				.synchronizedMap(new HashMap<String, VariableDMC>());
		private final Map<String, IEnumerator> enumeratorsByName = Collections
				.synchronizedMap(new HashMap<String, IEnumerator>());

		public StackFrameDMC(final ExecutionDMC executionDMC, Map<String, Object> frameProperties) {
			super(Stack.this, new IDMContext[] { executionDMC }, frameProperties);
			this.executionDMC = executionDMC;
			this.level = (Integer) frameProperties.get(PROP_LEVEL);
			this.moduleName = (String) frameProperties.get(MODULE_NAME);

			Object base = frameProperties.get(BASE_ADDR);
			if (base instanceof Integer)
				this.baseAddress = new Addr64(base.toString());
			if (base instanceof Long)
				this.baseAddress = new Addr64(base.toString());
			if (base instanceof String) // the string should be hex string
				this.baseAddress = new Addr64((String) base, 16);

			Object ipAddr = frameProperties.get(IP_ADDR);
			if (ipAddr instanceof Integer)
				this.ipAddress = new Addr64(ipAddr.toString());
			if (ipAddr instanceof Long)
				this.ipAddress = new Addr64(ipAddr.toString());
			if (ipAddr instanceof String)
				this.ipAddress = new Addr64((String) ipAddr, 16);

			if (frameProperties.containsKey(SOURCE_FILE)) {
				this.sourceFile = (String) frameProperties.get(SOURCE_FILE);
				this.functionName = (String) frameProperties.get(FUNCTION_NAME);
				this.lineNumber = (Integer) frameProperties.get(LINE_NUMBER);
			} else {
				// compute the source location
				Symbols symbolsService = getServicesTracker().getService(Symbols.class);
				ILineEntry line = symbolsService.getLineEntryForAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (line != null) {
					sourceFile = line.getFilePath().toOSString();
					frameProperties.put(SOURCE_FILE, sourceFile);

					lineNumber = line.getLineNumber();
					frameProperties.put(LINE_NUMBER, lineNumber);
				}

				IFunctionScope scope = symbolsService
						.getFunctionAtAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (scope != null) {
					functionName = scope.getName();
					frameProperties.put(FUNCTION_NAME, functionName);
				}
			}
			properties.putAll(frameProperties);
		}

		public String getModuleName() {
			return moduleName;
		}

		public String getSourceFile() {
			return sourceFile;
		}

		public String getFunctionName() {
			return functionName;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public ExecutionDMC getExecutionDMC() {
			return executionDMC;
		}

		public IAddress getBaseAddress() {
			return baseAddress;
		}

		public IAddress getIPAddress() {
			return ipAddress;
		}

		public int getLevel() {
			return level;
		}

		public DsfServicesTracker getDsfServicesTracker() {
			return dsfServicesTracker;
		}

		@Override
		public String toString() {
			return baseToString() + ".frame[" + getID() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		public int compareTo(StackFrameDMC f) {
			if (level < f.level)
				return -1;
			if (level > f.level)
				return +1;
			return 0;
		}

		public Element takeShapshot(Album album, Document document, IProgressMonitor monitor) {
			Element contextElement = document.createElement(STACK_FRAME);
			contextElement.setAttribute(PROP_ID, this.getID());

			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);

			album.addFile(new Path(sourceFile));

			return contextElement;
		}

		public void loadSnapshot(Element element) {
			// TODO Auto-generated method stub

		}

		public IVariableDMContext[] getLocals() {
			if (locals == null) {
				locals = new ArrayList<VariableDMC>();
				Symbols symbolsService = getServicesTracker().getService(Symbols.class);
				IFunctionScope scope = symbolsService
						.getFunctionAtAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (scope != null) {
					for (IVariable variable : scope.getVariables()) {
						VariableDMC var = new VariableDMC(Stack.this, this, variable);
						locals.add(var);
						localsByName.put(var.getName(), var);
					}

					for (IVariable variable : scope.getParameters()) {
						VariableDMC var = new VariableDMC(Stack.this, this, variable);
						locals.add(var);
						localsByName.put(var.getName(), var);
					}
				}
			}
			return locals.toArray(new VariableDMC[locals.size()]);
		}

		public VariableDMC findLocalVariable(String id) {
			if (locals == null)
				getLocals();
			return localsByName.get(id);
		}

		public IEnumerator[] getEnumerators() {
			if (enumerators == null) {
				enumerators = new ArrayList<IEnumerator>();
				if (getServicesTracker() != null) {
					Symbols symbolsService = getServicesTracker().getService(Symbols.class);
					if (executionDMC != null && symbolsService != null) {
						IFunctionScope scope = symbolsService.getFunctionAtAddress(executionDMC.getSymbolDMContext(),
								ipAddress);
						if (scope != null) {
							Collection<IEnumerator> localEnumerators = scope.getEnumerators();
							for (IEnumerator enumerator : localEnumerators) {
								enumerators.add(enumerator);
								enumeratorsByName.put(enumerator.getName(), enumerator);
							}
						}
					}
				}
			}
			return enumerators.toArray(new Enumerator[enumerators.size()]);
		}

		public IEnumerator findEnumerator(String id) {
			if (enumerators == null)
				getEnumerators();
			return enumeratorsByName.get(id);
		}
	}

	public class VariableData implements IVariableDMData {

		private final String name;

		public VariableData(VariableDMC variableDMC) {
			name = variableDMC.getName();
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return "0";
		}

	}

	public class VariableDMC extends DMContext implements IVariableDMContext {

		public static final String PROP_LOCATION = "Location";
		private final IVariable variable;

		public VariableDMC(IDsfService service, StackFrameDMC frame, IVariable variable) {
			super(Stack.this, new IDMContext[] { frame }, variable.getName(), variable.getName());
			this.variable = variable;
		}

		public IVariable getVariable() {
			return variable;
		}
	}

	public Stack(DsfSession session, String[] classNames) {
		super(session, classNames);
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

	public void getArguments(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
		// never called by DSF. it expects arguments to be lumped in with
		// locals.
		rm.done();
	}

	public void getFrameData(IFrameDMContext frameDmc, DataRequestMonitor<IFrameDMData> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, frameDmc);
		rm.setData(new StackFrameData((StackFrameDMC) frameDmc));
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getFrames(IDMContext execContext, DataRequestMonitor<IFrameDMContext[]> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, execContext);
		rm.setData(new IFrameDMContext[0]);
		if (execContext instanceof ExecutionDMC) {
			rm.setData(getFramesForDMC((ExecutionDMC) execContext, 0, ALL_FRAMES));
		}
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getLocals(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
		StackFrameDMC frameContext = (StackFrameDMC) frameCtx;
		rm.setData(frameContext.getLocals());
		rm.done();
	}

	public void getStackDepth(IDMContext dmc, int maxDepth, DataRequestMonitor<Integer> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, new Object[] { dmc, maxDepth });
		rm.setData(getFramesForDMC((ExecutionDMC) dmc, 0, ALL_FRAMES).length);
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getTopFrame(IDMContext execContext, DataRequestMonitor<IFrameDMContext> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, execContext);
		IFrameDMContext[] frames = getFramesForDMC((ExecutionDMC) execContext, 0, ALL_FRAMES);

		if (frames.length == 0) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE,
					"No top stack frame available", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		rm.setData(frames[0]);
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getVariableData(IVariableDMContext variableDmc, DataRequestMonitor<IVariableDMData> rm) {
		rm.setData(new VariableData((VariableDMC) variableDmc));
		rm.done();
	}

	@SuppressWarnings("unchecked")
	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		if (dmc instanceof IFrameDMContext) {
			getFrameData((IFrameDMContext) dmc, (DataRequestMonitor<IFrameDMData>) rm);
		} else if (dmc instanceof IVariableDMContext) {
			getVariableData((IVariableDMContext) dmc, (DataRequestMonitor<IVariableDMData>) rm);
		} else
			rm.done();
	}

	public void getFrames(IDMContext execContext, int startIndex, int endIndex, DataRequestMonitor<IFrameDMContext[]> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE,
				new Object[] { execContext, startIndex, endIndex });
		if (execContext instanceof ExecutionDMC) {
			rm.setData(getFramesForDMC((ExecutionDMC) execContext, startIndex, endIndex));
		}
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public IFrameDMContext[] getFramesForDMC(ExecutionDMC context, int startIndex, int endIndex) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE,
				new Object[] { context, startIndex, endIndex });

		if (!context.isSuspended()) {
			return new IFrameDMContext[0];
		}

		boolean needsUpdate = false;
		synchronized (stackFrames) {
			List<StackFrameDMC> frames = stackFrames.get(context.getID());
			needsUpdate = frames == null;
		}
		if (needsUpdate)
			updateFrames(context);
		synchronized (stackFrames) {
			List<StackFrameDMC> frames = stackFrames.get(context.getID());
			if (endIndex >= startIndex && endIndex - startIndex + 1 < frames.size())
				frames = frames.subList(startIndex, endIndex + 1);
			IFrameDMContext[] result = frames.toArray(new IFrameDMContext[frames.size()]);
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, result);
			return result;
		}
	}

	private void updateFrames(ExecutionDMC context) {
		ArrayList<StackFrameDMC> frames = new ArrayList<StackFrameDMC>();
		List<Map<String, Object>> frameProperties = computeStackFrames(context);
		for (Map<String, Object> props : frameProperties) {
			frames.add(new StackFrameDMC(context, props));
		}
		stackFrames.put(context.getID(), frames);
	}

	protected abstract List<Map<String, Object>> computeStackFrames(ExecutionDMC context);

	public void loadFramesForContext(ExecutionDMC exeDmc, Element allFrames) throws Exception {
		flushCache(null);
		List<StackFrameDMC> frames = Collections.synchronizedList(new ArrayList<StackFrameDMC>());

		NodeList frameElements = allFrames.getElementsByTagName(STACK_FRAME);

		int numFrames = frameElements.getLength();
		for (int i = 0; i < numFrames; i++) {
			Element groupElement = (Element) frameElements.item(i);
			Element propElement = (Element) groupElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			SnapshotUtils.initializeFromXML(propElement, properties);

			StackFrameDMC frameDMC = new StackFrameDMC(exeDmc, properties);
			frameDMC.loadSnapshot(groupElement);
			frames.add(frameDMC);

		}
		stackFrames.put(exeDmc.getID(), frames);

	}

	public void flushCache(IDMContext context) {
		if (context != null && context instanceof DMContext) {
			stackFrames.remove(((DMContext) context).getID());
		} else {
			stackFrames.clear();
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		flushCache(e.getDMContext());
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		flushCache(e.getDMContext());
	}

}
