/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.IAsmSourceLine;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.ui.CDebugUIMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;

import com.ibm.icu.text.MessageFormat;

/**
 * The CDI backend to the DSF disassembly view.
 *
 */
public class DisassemblyBackendCdi implements IDisassemblyBackend, IDebugEventSetListener {

	private IDisassemblyPartCallback fCallback;
	private ICThread fTargetContext;
	private String fCdiSessionId;
	private ICStackFrame fTargetFrameContext;
	private CDIDisassemblyRetrieval fDisassemblyRetrieval;
	/* The frame level as the disassembly callback expects it (0 = topmost frame) */
	private int fFrameLevel;

	public DisassemblyBackendCdi() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#init(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback)
	 */
	public void init(IDisassemblyPartCallback callback) {
		assert callback != null;
		fCallback = callback;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#hasDebugContext()
	 */
	public boolean hasDebugContext() {
		return fTargetContext != null;
	}

	/**
	 * Unlike DSF, CDI sessions don't have an ID. But to appease the DSF
	 * Disassembly view, we fabricate one.
	 * 
	 * @param debugElement
	 *            the debug element which represents the process being debugged
	 * @return the session ID
	 */
	private String getSessionId(ICDebugElement debugElement) {
		return "cdi-" + System.identityHashCode(debugElement.getDebugTarget()); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#setDebugContext(org.eclipse.core.runtime.IAdaptable)
	 */
	public SetDebugContextResult setDebugContext(IAdaptable context) {
		assert supportsDebugContext(context) : "caller should not have invoked us"; //$NON-NLS-1$
		
		SetDebugContextResult result = new SetDebugContextResult();
		result.sessionId = fCdiSessionId;	// initial value; may change

		ICDebugTarget cdiDebugTarget = (ICDebugTarget)((ICDebugElement)context).getDebugTarget();
		String cdiSessionId = getSessionId(cdiDebugTarget);
		
		fDisassemblyRetrieval = new CDIDisassemblyRetrieval(cdiDebugTarget);
		
		if (!cdiSessionId.equals(fCdiSessionId)) {
			fTargetContext = null;
			
			if (context instanceof ICStackFrame) {
				fTargetFrameContext = (ICStackFrame)context;
				fTargetContext = (ICThread)fTargetFrameContext.getThread();
				try {
					// CDI frame levels are ordered opposite of DSF. Frame 0 is the
					// root frame of the thread where in DSF it's the topmost frame
					// (where the PC is). Do a little math to flip reverse the value
					result.frameLevel = ((CStackFrame)((fTargetContext.getTopStackFrame()))).getLevel() -  fTargetFrameContext.getLevel();
				} catch (DebugException e) {
				}
			}
			fFrameLevel = result.frameLevel;
			
			if (fTargetContext != null) {
				result.sessionId = fCdiSessionId = cdiSessionId;
				result.contextChanged = true;
			}
		}
		else if (context instanceof ICStackFrame) {
			fTargetFrameContext = (ICStackFrame)context;
			ICThread newTargetContext = (ICThread)fTargetFrameContext.getThread();
			ICThread oldTargetContext = fTargetContext;
			fTargetContext = newTargetContext;
			if (oldTargetContext != null && newTargetContext != null) {
				result.contextChanged = !oldTargetContext.getDebugTarget().equals(newTargetContext.getDebugTarget());
			}
			try {
				// CDI frame levels are ordered opposite of DSF. Frame 0 is the
				// root frame of the thread where in DSF it's the topmost frame
				// (where the PC is). Do a little math to flip reverse the value
				result.frameLevel = ((CStackFrame)((fTargetContext.getTopStackFrame()))).getLevel() -  fTargetFrameContext.getLevel();
			} catch (DebugException e) {
			}
			fFrameLevel = result.frameLevel;
			if (!result.contextChanged) {
				fCallback.gotoFrame(result.frameLevel);
			}
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#supportsDebugContext(org.eclipse.core.runtime.IAdaptable)
	 */
	public boolean supportsDebugContext(IAdaptable context) {
		return supportsDebugContext_(context);
	}

	/**
	 * @param context
	 * @return
	 */
	public static boolean supportsDebugContext_(IAdaptable context) {
		return (context != null) && (context.getAdapter(ICDebugElement.class) != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#clearDebugContext()
	 */
	public void clearDebugContext() {
		fTargetContext= null;
		fCdiSessionId = null;
		fTargetFrameContext = null;
		fDisassemblyRetrieval = null;
		fFrameLevel = 0;
	}

	private class AddressRequest extends CRequest implements IDisassemblyRetrieval.AddressRequest {
		private BigInteger fAddress;
		public BigInteger getAddress() { return fAddress; }
		public void setAddress(BigInteger address) { fAddress = address; }
	};

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#retrieveFrameAddress(int)
	 */
	public void retrieveFrameAddress(final int targetFrame) {
		try {
			final IStackFrame[] stackFrames= fTargetContext.getStackFrames();
			if (stackFrames.length <= targetFrame) {
				fCallback.setUpdatePending(false);
				return;
			}
			IStackFrame stackFrame= stackFrames[targetFrame];
			fDisassemblyRetrieval.asyncGetFrameAddress(stackFrame, new AddressRequest() {
				@Override
				public void done() {
					fCallback.setUpdatePending(false);
					if (isSuccess()) {
						BigInteger address= getAddress();
						if (targetFrame == 0) {
							fCallback.updatePC(address);
						} else {
							fCallback.gotoFrame(targetFrame, address);
						}
					}
				}
			});
			
		} catch (DebugException exc) {
			DisassemblyUtils.internalError(exc);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameLevel()
	 */
	public int getFrameLevel() {
		return fFrameLevel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#isSuspended()
	 */
	public boolean isSuspended() {
		return fTargetContext != null && fTargetContext.isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameFile()
	 */
	public String getFrameFile() {
		return fTargetFrameContext != null ? fTargetFrameContext.getFile() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#getFrameLine()
	 */
	public int getFrameLine() {
		return fTargetFrameContext.getFrameLineNumber();		
	}

	private class DisassemblyRequest extends CRequest implements IDisassemblyRetrieval.DisassemblyRequest {
		private IDisassemblyBlock fBlock;
		public IDisassemblyBlock getDisassemblyBlock() { return fBlock;	}
		public void setDisassemblyBlock(IDisassemblyBlock block) { fBlock = block; }
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyBackend#retrieveDisassembly(java.math.BigInteger, java.math.BigInteger, java.lang.String, boolean, boolean, boolean, int, int, int)
	 */
	public void retrieveDisassembly(final BigInteger startAddress,
			BigInteger endAddress, String file, int lineNumber, int lines, final boolean mixed,
			final boolean showSymbols, final boolean showDisassembly, int linesHint) {
		
		if (fTargetContext == null || fTargetContext.isTerminated()) {
			return;
		}
		final BigInteger addressLength= BigInteger.valueOf(lines * 4);
		if (endAddress.subtract(startAddress).compareTo(addressLength) > 0) {
			endAddress= startAddress.add(addressLength);
		}
		final BigInteger finalEndAddress= endAddress;
		final IDisassemblyRetrieval.DisassemblyRequest disassemblyRequest= new DisassemblyRequest() {
			@Override
			public void done() {
				if (isSuccess() && getDisassemblyBlock() != null) {
					insertDisassembly(startAddress, getDisassemblyBlock(), mixed, showSymbols, showDisassembly);
				} else {
					final IStatus status= getStatus();
					if (status != null && !status.isOK()) {
						if (startAddress != null) {
							fCallback.doScrollLocked(new Runnable() {
								public void run() {
									fCallback.insertError(startAddress, status.getMessage());
								}
							});
						}
					}
					fCallback.setUpdatePending(false);
				}
			}
		};
		fDisassemblyRetrieval.asyncGetDisassembly(startAddress, finalEndAddress, file, lineNumber, lines, disassemblyRequest);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#insertSource(org.eclipse.jface.text.Position, java.math.BigInteger, java.lang.String, int)
	 */
	public Object insertSource(Position pos, BigInteger address,
			String file, int lineNumber) {
		ISourceLocator locator = fTargetContext.getLaunch().getSourceLocator();
		if (locator instanceof ISourceLookupDirector) {
			return ((ISourceLookupDirector)locator).getSourceElement(file);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#hasFrameContext()
	 */
	public boolean hasFrameContext() {
		return fTargetFrameContext != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyBackend#gotoSymbol(java.lang.String)
	 */
	public void gotoSymbol(String symbol) {
		if (fTargetFrameContext != null) {
			try {
				// This logic was lifted from CMemoryBlockRetrievalExtension.getExtendedMemoryBlock(String, Object)
				CStackFrame cstackFrame = (CStackFrame)fTargetFrameContext;
				ICDIExpression cdiExpression = cstackFrame.getCDITarget().createExpression(symbol);
				CExpression cdtExpression = new CExpression(cstackFrame, cdiExpression, null);
				IValue value = cdtExpression.getValue();
				if (value instanceof ICValue) {
					ICType type = ((ICValue)value).getType();
					if (type != null) {
						// get the address for the expression, allow all types
						String rawExpr = cdtExpression.getExpressionString();
						String voidExpr = "(void *)(" + rawExpr + ')'; //$NON-NLS-1$
						String attempts[] = { rawExpr, voidExpr };
						for (int i = 0; i < attempts.length; i++) {
							String expr = attempts[i];
							String addressStr = cstackFrame.evaluateExpressionToString(expr);
							if (addressStr != null) {
								try {
									final BigInteger address = (addressStr.startsWith("0x")) ? new BigInteger(addressStr.substring(2), 16) : new BigInteger(addressStr); //$NON-NLS-1$
									fCallback.asyncExec(new Runnable() {
										public void run() {
											fCallback.gotoAddress(address);
										}});
	
								} catch (NumberFormatException e) {
									if (i >= attempts.length) {
										throw new DebugException(new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, 
												MessageFormat.format(CDebugUIMessages.getString("DisassemblyBackendCdi_Symbol_Evaluation_Unusable"), new String[]{symbol}))); //$NON-NLS-1$
									}
								}
							}
						}
					}
				 }
				else {
					throw new DebugException(new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, 
							MessageFormat.format(CDebugUIMessages.getString("DisassemblyBackendCdi_Symbol_Didnt_Evaluate"), new String[]{symbol}))); //$NON-NLS-1$
				}
			}
			catch (final CDIException exc) {
				fCallback.asyncExec(new Runnable() {
					public void run() {
		                ErrorDialog.openError(fCallback.getSite().getShell(), 
		                		CDebugUIMessages.getString("DisassemblyBackendCdi_Error_Dlg_Title"),  //$NON-NLS-1$
		                		null, new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, exc.getLocalizedMessage()));
					}});
			}
			catch (final DebugException exc) {
				fCallback.asyncExec(new Runnable() {
					public void run() {
		                ErrorDialog.openError(fCallback.getSite().getShell(), 
		                		CDebugUIMessages.getString("DisassemblyBackendCdi_Error_Dlg_Title"), //$NON-NLS-1$
		                		null, exc.getStatus());
					}});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyBackend#retrieveDisassembly(java.lang.String, int, java.math.BigInteger, boolean, boolean, boolean)
	 */
	public void retrieveDisassembly(String file, int lines,
			BigInteger endAddress, final boolean mixed, final boolean showSymbols,
			final boolean showDisassembly) {
		final IDisassemblyRetrieval.DisassemblyRequest disassemblyRequest= new DisassemblyRequest() {
			@Override
			public void done() {
				if (isSuccess() && getDisassemblyBlock() != null) {
					insertDisassembly(null, getDisassemblyBlock(), mixed, showSymbols, showDisassembly);
				} else {
					final IStatus status= getStatus();
					if (status != null && !status.isOK()) {
						fCallback.asyncExec(new Runnable() {
							public void run() {
				                ErrorDialog.openError(fCallback.getSite().getShell(), "Error", null, getStatus()); //$NON-NLS-1$
							}
						});
					}
					fCallback.setUpdatePending(false);
				}
			}
		};

		assert !fCallback.getUpdatePending();
		fCallback.setUpdatePending(true);
		fDisassemblyRetrieval.asyncGetDisassembly(null, endAddress, file, 1, lines, disassemblyRequest);
	}
	
	private void insertDisassembly(BigInteger startAddress, IDisassemblyBlock disassemblyBlock, boolean mixed, boolean showSymbols, boolean showDisassembly) {
		if (!fCallback.hasViewer() || fCdiSessionId == null) {
			return;
		}
		
		if (!fCallback.getUpdatePending()) {
			assert false;
			return;
		}
		try {
			fCallback.lockScroller();
			
			final IDisassemblyDocument document = fCallback.getDocument(); // for convenience
			IAsmSourceLine[] srcLines= disassemblyBlock.getSourceLines();
			AddressRangePosition p = null;
			Object srcElement= disassemblyBlock.getSourceElement();
			for (int i = 0; i < srcLines.length; i++) {
				IAsmSourceLine srcLine= srcLines[i];
				
				// If the caller doesn't want mixed, set line number to -1 so we
				// create a pure disassembly position object
				int lineNumber= mixed ? srcLine.getLineNumber() - 1 : -1;
				
				IAsmInstruction[] instructions= srcLine.getInstructions();
				for (int j = 0; j < instructions.length; j++) {
					IAsmInstruction instruction = instructions[j];
					BigInteger address= instruction.getAdress().getValue();
					if (startAddress == null || startAddress.compareTo(BigInteger.ZERO) < 0) {
						startAddress = address;
						fCallback.setGotoAddressPending(address);
					}
					if (p == null || !p.containsAddress(address)) {
						p = fCallback.getPositionOfAddress(address);
					}
					if (p instanceof ErrorPosition && p.fValid) {
						p.fValid = false;
						document.addInvalidAddressRange(p);
					} else if (p == null) {
						return;
					} else if (p.fValid) {
						if (srcElement != null && lineNumber >= 0 || p.fAddressLength == BigInteger.ONE) {
							// override probably unaligned disassembly
							p.fValid = false;
							document.addInvalidAddressRange(p);
						} else {
							return;
						}
					}
					boolean hasSource= false;
					String compilationPath= null;
					if (srcElement != null && lineNumber >= 0) {
						if (srcElement instanceof LocalFileStorage) {
							compilationPath = ((LocalFileStorage)srcElement).getFullPath().toString();
						}
						else if (srcElement instanceof IFile) {
							compilationPath = ((IFile)srcElement).getLocation().toString();
						}
						else {
							assert false : "missing support for source element of type " + srcElement.getClass().toString(); //$NON-NLS-1$
						}
						if (compilationPath != null) {
							p = fCallback.insertSource(p, address, compilationPath, lineNumber);
							hasSource = fCallback.getStorageForFile(compilationPath) != null;
						}
						else {
							hasSource = false;
						}
					}
					// insert symbol label
					final String functionName= instruction.getFunctionName();
					if (functionName != null && functionName.length() > 0 && instruction.getOffset() == 0) {
						p = document.insertLabel(p, address, functionName, showSymbols && (!hasSource || showDisassembly));
					}
					// determine instruction byte length
					BigInteger instrLength= null;
					if (j < instructions.length - 1) {
						instrLength= instructions[j+1].getAdress().distanceTo(instruction.getAdress()).abs();
					} else if (i < srcLines.length - 1) {
						int nextSrcLineIdx= i+1;
						while (nextSrcLineIdx < srcLines.length) {
							IAsmInstruction[] nextInstrs= srcLines[nextSrcLineIdx].getInstructions();
							if (nextInstrs.length > 0) {
								instrLength= nextInstrs[0].getAdress().distanceTo(instruction.getAdress()).abs();
								break;
							}
							++nextSrcLineIdx;
						}
						if (nextSrcLineIdx >= srcLines.length) {
							break;
						}
					} else {
						if (instructions.length == 1 && srcLines.length == 1) {
							instrLength= p.fAddressLength;
						}
					}
					if (instrLength == null) {
						// cannot determine length of last instruction
						break;
					}
					final String opCode;
					// insert function name+offset instead of opcode bytes
					if (functionName != null && functionName.length() > 0) {
						opCode= functionName + '+' + instruction.getOffset();
					} else {
						opCode= ""; //$NON-NLS-1$
					}
					if (!showDisassembly && hasSource) {
						p = document.insertDisassemblyLine(p, address, instrLength.intValue(), opCode, "", compilationPath, lineNumber); //$NON-NLS-1$
					} else {
						p = document.insertDisassemblyLine(p, address, instrLength.intValue(), opCode, instruction.getInstructionText(), compilationPath, lineNumber); //$NON-NLS-1
					}
					if (p == null) {
						break;
					}
				}
			}
			
			
		} catch (BadLocationException e) {
			// should not happen
			DisassemblyUtils.internalError(e);
		} finally {
			fCallback.setUpdatePending(false);
			fCallback.updateInvalidSource();
			fCallback.unlockScroller();
			fCallback.doPending();
			fCallback.updateVisibleArea();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object eventSource = event.getSource();
				if ((eventSource instanceof CDebugTarget) && (getSessionId((CDebugTarget)eventSource).equals(fCdiSessionId))) {
					fCallback.handleTargetEnded();
					return;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend#evaluateExpression(java.lang.String)
	 */
	public String evaluateExpression(String expression) {
		// This is called to service text hovering. We either resolve the
		// expression or we don't. No error reporting needed.
		if (fTargetFrameContext != null) {
			try {
				// This logic was lifted from CMemoryBlockRetrievalExtension.getExtendedMemoryBlock(String, Object)
				CStackFrame cstackFrame = (CStackFrame)fTargetFrameContext;
				ICDIExpression cdiExpression = cstackFrame.getCDITarget().createExpression(expression);
				CExpression cdtExpression = new CExpression(cstackFrame, cdiExpression, null);
				IValue value = cdtExpression.getValue();
				if (value instanceof ICValue) {
					ICType type = ((ICValue)value).getType();
					if (type != null) {
						return cstackFrame.evaluateExpressionToString(cdtExpression.getExpressionString());
					}
				 }
			}
			catch (Exception exc) {
				
			}
		}
		return ""; //$NON-NLS-1$
	}
}
