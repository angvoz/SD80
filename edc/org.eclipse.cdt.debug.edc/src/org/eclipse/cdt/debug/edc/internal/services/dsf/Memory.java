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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.protocol.IService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Memory extends AbstractEDCService implements IEDCMemory, ICachingService, ISnapshotContributor,
		IDSFServiceUsingTCF  {

	private org.eclipse.tm.tcf.services.IMemory tcfMemoryService;
	private Map<String, MemoryCache> memoryCaches;
	private long tcfTimeout;

	private class MemoryChangedEvent extends AbstractDMEvent<IMemoryDMContext> implements IMemoryChangedEvent {
		IAddress[] addresses;

		public MemoryChangedEvent(IMemoryDMContext context, IAddress[] addresses) {
			super(context);
			this.addresses = addresses;
		}

		public IAddress[] getAddresses() {
			return addresses;
		}
	}

	public Memory(DsfSession session) {
		super(session, new String[] { IEDCMemory.class.getName(), IMemory.class.getName(), Memory.class.getName(),
				ISnapshotContributor.class.getName() });
		setTCFTimeout(15 * 1000); // Fifteen seconds
	}

	private MemoryCache getMemoryCache(IMemoryDMContext memoryDMC) {
		assert memoryDMC instanceof IEDCDMContext;
		MemoryCache cache = memoryCaches.get(((IEDCDMContext) memoryDMC).getID());
		if (cache == null) {
			cache = new MemoryCache(getTargetEnvironmentService().getMemoryCacheMinimumBlockSize());
			memoryCaches.put(((IEDCDMContext) memoryDMC).getID(), cache);
		}
		return cache;
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);

		// create memory cache
		memoryCaches = new HashMap<String, MemoryCache>();

		getSession().addServiceEventListener(this, null);
	}

	public MemoryByte[] getMemory(final IMemoryDMContext context, final IAddress address, final long offset,
			final int word_size, final int count) throws CoreException {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { address.toHexAddressString(), offset, word_size, count })); }

		// Validate the context
		if (context == null) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
							"Unknown context type", null)); //$NON-NLS-1$);
		}

		// Validate the word size
		if (word_size < 1) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
					"Word size not supported (< 1)", null)); //$NON-NLS-1$
		}

		// Validate the byte count
		if (count < 0) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Invalid word count (< 0)", null)); //$NON-NLS-1$
		}

		// everything OK

		// NOTE: We normalize word_size and count to read 1-byte words for this implementation
		MemoryByte[] memoryBytes = getMemoryCache(context).getMemory(tcfMemoryService, context, address.add(offset), 1, count * word_size,
				getTCFTimeout());
		// hide breakpoints inserted in the memory by debugger
		Breakpoints bpService = getService(Breakpoints.class);
		bpService.removeBreakpointFromMemoryBuffer(address.add(offset), memoryBytes);
		if (RunControl.timeStepping())
			System.out.println("Time since stepping start: " + 
				((System.currentTimeMillis() - RunControl.getSteppingStartTime()) / 1000.0));

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
		return memoryBytes;
	}

	public IStatus getMemory(IEDCExecutionDMC context, IAddress address,
			ArrayList<MemoryByte> memBuffer, int count, int word_size) {
		try {
			MemoryByte[] memArray = getMemory(context, address, 0, count, word_size);
			memBuffer.addAll(Arrays.asList(memArray));
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
					"Error reading memory from: " + address.toHexAddressString(), null);
		}
		return Status.OK_STATUS;
	}

	public void flushCache(IDMContext context) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { context })); }

		if (isSnapshot())
			return;

		if (context == null) {
			// reset every cache in each context
			for (String key : memoryCaches.keySet()) {
				memoryCaches.get(key).reset();
			}
		} else {
			IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
			if (memoryCaches.containsKey(((IEDCDMContext) memoryDMC).getID())) {
				// We do not want to use the call to getMemoryCache() here.
				// This is because:
				// 1- if there is not an entry already , we do not want to
				// automatically
				// create one, just to call reset() on it.
				// 2- if memoryDMC == null, we do not want to create a cache
				// entry for which the key is 'null'
				memoryCaches.get(((IEDCDMContext) memoryDMC).getID()).reset();
			}
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	public IStatus setMemory(IMemoryDMContext context, IAddress address, int word_size, int count, byte[] buffer) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { address.toHexAddressString(), count })); }

		final IStatus[] ret = new IStatus[] { Status.OK_STATUS };

		setMemory(context, address, 0, word_size, count, buffer, new RequestMonitor(ImmediateExecutor
				.getInstance(), null) {

			@Override
			protected void handleFailure() {
				ret[0] = getStatus();
			}
		});

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(ret[0])); }
		return ret[0];
	}

	public void tcfServiceReady(IService service) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { service })); }
		tcfMemoryService = (org.eclipse.tm.tcf.services.IMemory) service;

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { e.getClass() })); }
		flushCache(e.getDMContext());
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { e.getClass() })); }
		if (e.getReason() != StateChangeReason.STEP) {
			flushCache(e.getDMContext());
		}
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExpressionChangedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { e.getClass() })); }

		// Get the context and expression service handle
		final IExpressionDMContext context = e.getDMContext();
		IExpressions expressionService = getService(IExpressions.class);

		// Get the variable information and update the corresponding memory
		// locations
		if (expressionService != null) {
			expressionService.getExpressionAddressData(context, new DataRequestMonitor<IExpressionDMAddress>(
					getExecutor(), null) {
				@Override
				protected void handleSuccess() {
					// Figure out which memory area was modified
					IExpressionDMAddress expression = getData();
					final int count = expression.getSize();
					Object expAddress = expression.getAddress();
					final Addr64 address;
					if (expAddress instanceof Addr64)
						address = (Addr64) expAddress;
					else if (expAddress instanceof IAddress)
						address = new Addr64(((IAddress) expAddress).getValue());
					else
						return; // not a valid memory address

					final IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
					boolean modified = getMemoryCache(memoryDMC).refreshMemory(tcfMemoryService, memoryDMC, address, 0,
							1, count, new RequestMonitor(getExecutor(), null), getTCFTimeout());
					if (modified) {
						// we've modified cache - send an event
						IAddress[] addresses = new IAddress[count];
						for (int i = 0; i < count; i++) {
							addresses[i] = address.add(i);
						}
						getSession().dispatchEvent(new MemoryChangedEvent(memoryDMC, addresses), getProperties());
					}
				}
			});
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	private static final String MEMORY_CONTEXT = "memory_context";
	private static final String MEMORY = "memory";
	private static final String CONTEXT_ID = "context_id";

	public void loadSnapshot(Element element) throws Exception {
		memoryCaches = new HashMap<String, MemoryCache>();
		NodeList contextElements = element.getElementsByTagName(MEMORY_CONTEXT);

		int numContexts = contextElements.getLength();
		for (int i = 0; i < numContexts; i++) {
			Element contextElement = (Element) contextElements.item(i);
			String contextID = contextElement.getAttribute(CONTEXT_ID);
			MemoryCache cache = new MemoryCache(getTargetEnvironmentService().getMemoryCacheMinimumBlockSize());
			cache.loadSnapshot(contextElement);
			memoryCaches.put(contextID, cache);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor#takeShapshot(org.eclipse.cdt.debug.edc.snapshot.IAlbum, org.w3c.dom.Document, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Element takeSnapshot(IAlbum album, Document document, IProgressMonitor monitor) {
		Element memoryElement = document.createElement(MEMORY);
		SubMonitor progress = SubMonitor.convert(monitor, memoryCaches.keySet().size() * 1000);
		progress.subTask("Memory");
		for (String key : memoryCaches.keySet()) {
			MemoryCache cache = memoryCaches.get(key);
			Element memoryCacheElement = document.createElement(MEMORY_CONTEXT);
			memoryCacheElement.setAttribute(CONTEXT_ID, key);
			memoryCacheElement.appendChild(cache.takeSnapshot(album, document, progress.newChild(1000)));
			memoryElement.appendChild(memoryCacheElement);
		}
		return memoryElement;
	}

	public void setTCFTimeout(long msecs) {
		tcfTimeout = msecs;
	}

	public long getTCFTimeout() {
		return tcfTimeout;
	}
	
	// Implementation of org.eclipse.cdt.dsf.debug.service.IMemory
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IMemory#fillMemory(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, org.eclipse.cdt.core.IAddress, long, int, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void fillMemory(final IMemoryDMContext context, final IAddress address, final long offset, final int word_size, final int count,
			final byte[] pattern, final RequestMonitor rm) {
		asyncExec(new Runnable() {
			
			public void run() {
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { address.toHexAddressString(), offset, word_size, count })); }

				// Validate the context
				if (context == null) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
									"Unknown context type", null)); //$NON-NLS-1$;
					rm.done();
					return;
				}

				// Validate the word size
				if (word_size < 1) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
							"Word size not supported (< 1)", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Validate the repeat count
				if (count < 0) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"Invalid repeat count (< 0)", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Validate the pattern
				if (pattern.length < 1) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"Empty pattern", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Create an aggregate buffer so we can write in 1 shot
				final int length = pattern.length;
				final byte[] buffer = new byte[count * length];
				for (int i = 0; i < count; i++) {
					System.arraycopy(pattern, 0, buffer, i * length, length);
				}

				// All is clear: go for it
				// NOTE: We normalize word_size and count to read 1-byte words for this implementation
				try {
					getMemoryCache(context).setMemory(tcfMemoryService, context, address, offset, 1, count * length * word_size,
							buffer, getTCFTimeout());
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					rm.setStatus(e.getStatus());
				}
				rm.done();
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
			}
			
		}, rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IMemory#getMemory(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, org.eclipse.cdt.core.IAddress, long, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getMemory(final IMemoryDMContext context, final IAddress address, final long offset,
			final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> drm) {

		asyncExec(new Runnable() {
			
			public void run() {
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { address.toHexAddressString(), offset, word_size, count })); }
				// NOTE: We normalize word_size and count to read 1-byte words for this implementation
				try {
					MemoryByte[] memoryBytes = getMemory(context, address, offset, word_size, count);
					drm.setData(memoryBytes);
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					drm.setStatus(e.getStatus());
				}
				drm.done();
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
			}
			
		}, drm);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IMemory#setMemory(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, org.eclipse.cdt.core.IAddress, long, int, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void setMemory(final IMemoryDMContext context, final IAddress address, final long offset,
			final int word_size, final int count, final byte[] buffer, final RequestMonitor rm) {

		asyncExec(new Runnable() {
			
			public void run() {
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { address.toHexAddressString(), offset, word_size, count })); }

				// Validate the context
				if (context == null) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
									"Unknown context type", null)); //$NON-NLS-1$);
					rm.done();
					return;
				}

				// Validate the word size
				// NOTE: We only accept 1 byte words for this implementation
				if (word_size != 1) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
							"Word size not supported (!= 1)", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Validate the byte count
				if (count < 0) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"Invalid word count (< 0)", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Validate the buffer size
				if (buffer.length < count) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"Buffer too short", null)); //$NON-NLS-1$
					rm.done();
					return;
				}
				// everything ok
				// NOTE: We normalize word_size and count to read 1-byte words for this implementation
				try {
					getMemoryCache(context).setMemory(tcfMemoryService, context, address, offset, word_size, count, buffer, getTCFTimeout());
					if (rm.isSuccess()) {
						// we've modified memory - send an event
						IAddress[] addresses = new IAddress[count];
						for (int i = 0; i < count; i++) {
							addresses[i] = address.add(offset + i);
						}
						getSession().dispatchEvent(new MemoryChangedEvent(context, addresses), getProperties());
					}
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					rm.setStatus(e.getStatus());
				}
				rm.done();
				if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
			}
			
		}, rm);

	}

}
