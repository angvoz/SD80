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
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.protocol.IService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Memory extends AbstractEDCService implements IEDCMemory, ICachingService, ISnapshotContributor,
		IDSFServiceUsingTCF  {

	private org.eclipse.tm.tcf.services.IMemory tcfMemoryService;
	private Map<String, MemoryCache> memoryCaches;

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

	public void fillMemory(IMemoryDMContext context, IAddress address, long offset, int word_size, int count,
			byte[] pattern, final RequestMonitor rm) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { address.toHexAddressString(), offset, word_size, count }); }

		// Validate the context
		if (context == null) {
			rm
					.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
							"Unknown context type", null)); //$NON-NLS-1$;
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
		int length = pattern.length;
		byte[] buffer = new byte[count * length];
		for (int i = 0; i < count; i++) {
			System.arraycopy(pattern, 0, buffer, i * length, length);
		}

		// All is clear: go for it
		getMemoryCache(context).setMemory(tcfMemoryService, context, address, offset, word_size, count * length,
				buffer, rm);

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	public void getMemory(final IMemoryDMContext context, final IAddress address, final long offset,
			final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> drm) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { address.toHexAddressString(), offset, word_size, count }); }

		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
							"Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		// Validate the word size
		// NOTE: We only accept 1 byte words for this implementation
		if (word_size != 1) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
					"Word size not supported (!= 1)", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		// Validate the byte count
		if (count < 0) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Invalid word count (< 0)", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		// everything ok
		getMemoryCache(context).getMemory(tcfMemoryService, context, address.add(offset), word_size, count,
				new DataRequestMonitor<MemoryByte[]>(ImmediateExecutor.getInstance(), drm) {
			@Override
			protected void handleSuccess() {
				// hide breakpoints inserted in the memory by debugger
				MemoryByte[] data = getData();

				Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
				bpService.removeBreakpointFromMemoryBuffer(address.add(offset), data);

				drm.setData(data);
				drm.done();
			}
		});

		if (RunControl.timeStepping())
			System.out.println("Time since stepping start: " + 
				((System.currentTimeMillis() - RunControl.getSteppingStartTime()) / 1000.0));

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	public void setMemory(final IMemoryDMContext context, final IAddress address, final long offset,
			final int word_size, final int count, final byte[] buffer, final RequestMonitor rm) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { address.toHexAddressString(), offset, word_size, count }); }

		// Validate the context
		if (context == null) {
			rm
					.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
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
		getMemoryCache(context).setMemory(tcfMemoryService, context, address, offset, word_size, count, buffer, rm);
		if (rm.isSuccess()) {
			// we've modified memory - send an event
			IAddress[] addresses = new IAddress[count];
			for (int i = 0; i < count; i++) {
				addresses[i] = address.add(offset + i);
			}
			getSession().dispatchEvent(new MemoryChangedEvent(context, addresses), getProperties());
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	public void flushCache(IDMContext context) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context }); }

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

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCMemory#getMemory(org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC, org.eclipse.cdt.core.IAddress, java.util.ArrayList, int, int)
	 */
	public IStatus getMemory(IEDCExecutionDMC context, IAddress address, final ArrayList<MemoryByte> memBuffer, int count,
			int word_size) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { address.toHexAddressString(), count }); }

		final IStatus[] ret = new IStatus[] { Status.OK_STATUS };

		getMemory(context, address, 0, word_size, count, new DataRequestMonitor<MemoryByte[]>(ImmediateExecutor
				.getInstance(), null) {

			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					MemoryByte[] resultMem = getData();
					memBuffer.addAll(Arrays.asList(resultMem));
				} else {
					ret[0] = getStatus();
				}
			}
		});

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(ret[0]); }
		return ret[0];
	}
	
	public IStatus setMemory(IMemoryDMContext context, IAddress address, int word_size, int count, byte[] buffer) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { address.toHexAddressString(), count }); }

		final IStatus[] ret = new IStatus[] { Status.OK_STATUS };

		setMemory(context, address, 0, word_size, count, buffer, new RequestMonitor(ImmediateExecutor
				.getInstance(), null) {

			@Override
			protected void handleFailure() {
				ret[0] = getStatus();
			}
		});

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(ret[0]); }
		return ret[0];
	}

	public void tcfServiceReady(IService service) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { service }); }
		tcfMemoryService = (org.eclipse.tm.tcf.services.IMemory) service;

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { e.getClass() }); }
		flushCache(e.getDMContext());
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { e.getClass() }); }
		if (e.getReason() != StateChangeReason.STEP) {
			flushCache(e.getDMContext());
		}
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExpressionChangedDMEvent e) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { e.getClass() }); }

		// Get the context and expression service handle
		final IExpressionDMContext context = e.getDMContext();
		IExpressions expressionService = getServicesTracker().getService(IExpressions.class);

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
							1, count, new RequestMonitor(getExecutor(), null));
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

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
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

	public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
		Element memoryElement = document.createElement(MEMORY);
		for (String key : memoryCaches.keySet()) {
			MemoryCache cache = memoryCaches.get(key);
			Element memoryCacheElement = document.createElement(MEMORY_CONTEXT);
			memoryCacheElement.setAttribute(CONTEXT_ID, key);
			memoryCacheElement.appendChild(cache.takeShapshot(album, document, monitor));
			memoryElement.appendChild(memoryCacheElement);
		}
		return memoryElement;
	}
}
