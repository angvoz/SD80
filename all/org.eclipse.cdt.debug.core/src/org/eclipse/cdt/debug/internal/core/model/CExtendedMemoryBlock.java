/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.model; 

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.MemoryByte;

/**
 * Represents a memory block in the CDI model.
 */
public class CExtendedMemoryBlock extends CDebugElement implements IExtendedMemoryBlock, ICDIEventListener {

	private class CMemoryByte extends MemoryByte {

		/** 
		 * Constructor for CMemoryByte. 
		 */
		public CMemoryByte( byte v, byte f ) {
			this.value = v;
			this.flags = f;
		}

		/** 
		 * Constructor for CMemoryByte. 
		 */
		public CMemoryByte() {
		}
	}

	/**
	 * The address expression this memory block is based on.
	 */
	private String fExpression;

	/**
	 * The base address of this memory block.
	 */
	private BigInteger fBaseAddress;

	/**
	 * The underlying CDI memory block.
	 */
	private ICDIMemoryBlock fCDIBlock;

	/**
	 * The memory bytes values.
	 */
	private MemoryByte[] fBytes = null;

	private HashSet fChanges = new HashSet();

	/** 
	 * Constructor for CExtendedMemoryBlock. 
	 */
	public CExtendedMemoryBlock( CDebugTarget target, String expression, BigInteger baseAddress ) {
		super( target );
		fExpression = expression;
		fBaseAddress = baseAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getExpression()
	 */
	public String getExpression() throws DebugException {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getBigBaseAddress()
	 */
	public BigInteger getBigBaseAddress() {
		return fBaseAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getAddressSize()
	 */
	public int getAddressSize() {
		// TODO need a similar method for IAddress
		return 4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#isMemoryChangesManaged()
	 */
	public boolean isMemoryChangesManaged() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#setBaseAddress(java.math.BigInteger)
	 */
	public void setBaseAddress( BigInteger address ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getBytesFromOffset(long, long)
	 */
	public MemoryByte[] getBytesFromOffset( long offset, long length ) throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress( BigInteger address, long length ) throws DebugException {
		ICDIMemoryBlock cdiBlock = getCDIBlock();
		if ( cdiBlock == null || 
			 cdiBlock.getStartAddress().compareTo( address ) > 0 || 
			 cdiBlock.getStartAddress().add( BigInteger.valueOf( cdiBlock.getLength() ) ).compareTo( address.add( BigInteger.valueOf( length ) ) ) < 0 ) {
			synchronized( this ) {
				byte[] bytes = null;
				try {
					cdiBlock = getCDIBlock();
					if ( cdiBlock == null || 
						 cdiBlock.getStartAddress().compareTo( address ) > 0 || 
						 cdiBlock.getStartAddress().add( BigInteger.valueOf( cdiBlock.getLength() ) ).compareTo( address.add( BigInteger.valueOf( length ) ) ) < 0 ) {
						if ( cdiBlock != null ) {
							disposeCDIBlock();
							fBytes = null;
						}
						setCDIBlock( createCDIBlock( address, length ) );
					}
					bytes = getCDIBlock().getBytes();
				}
				catch( CDIException e ) {
					targetRequestFailed( e.getMessage(), null );
				}
				fBytes = new MemoryByte[bytes.length];
				for ( int i = 0; i < bytes.length; ++i ) {
					byte flags = MemoryByte.VALID;
					if ( hasChanged( getRealBlockAddress().add( BigInteger.valueOf( i ) ) ) )
						flags |= MemoryByte.CHANGED;
					fBytes[i] = new CMemoryByte( bytes[i], flags );
				}
			}
		}
		MemoryByte[] result = new MemoryByte[0];
		if ( fBytes != null ) {
			int offset = address.subtract( getRealBlockAddress() ).intValue();
			if ( offset >= 0 ) {
				int size = ( fBytes.length - offset >= length ) ? (int)length : fBytes.length - offset;
				if ( size > 0 ) {
					result = new MemoryByte[size];
					System.arraycopy( fBytes, offset, result, 0, size );
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#isBigEndian()
	 */
	public boolean isBigEndian() {
		IExecFileInfo info = (IExecFileInfo)getDebugTarget().getAdapter( IExecFileInfo.class );
		if ( info != null ) {
			return info.isLittleEndian();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#enable()
	 */
	public void enable() {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null ) {
			block.setFrozen( false );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#disable()
	 */
	public void disable() {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null ) {
			block.setFrozen( true );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#isEnabled()
	 */
	public boolean isEnabled() {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null ) {
			return !block.isFrozen();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#delete()
	 */
	public void delete() {
		fChanges.clear();
		ICDIMemoryBlock cdiBlock = getCDIBlock();
		if ( cdiBlock != null ) {
			try {
				((CDebugTarget)getDebugTarget()).getCDISession().getMemoryManager().removeBlock( cdiBlock );
			}
			catch( CDIException e ) {
				CDebugCorePlugin.log( e );
			}
			fCDIBlock = null;
		}
		getCDISession().getEventManager().removeEventListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock#getMemoryBlockRetrieval()
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return (IMemoryBlockRetrieval)getDebugTarget().getAdapter( IMemoryBlockRetrieval.class );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null )
				continue;
			if ( source.getTarget().equals( getCDITarget() ) ) {
				if ( event instanceof ICDIResumedEvent || event instanceof ICDIRestartedEvent ) {
					resetChanges();
				}
				else if ( event instanceof ICDIMemoryChangedEvent ) {
					if ( source instanceof ICDIMemoryBlock && source.equals( getCDIBlock() ) ) {
						handleChangedEvent( (ICDIMemoryChangedEvent)event );
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	public long getStartAddress() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	public long getLength() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
	 */
	public void setValue( long offset, byte[] bytes ) throws DebugException {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null ) {
			BigInteger base = getBigBaseAddress();
			BigInteger real = getRealBlockAddress();
			long realOffset = base.add( BigInteger.valueOf( offset ) ).subtract( real ).longValue();
			try {
				block.setValue( realOffset, bytes );
			}
			catch( CDIException e ) {
				targetRequestFailed( e.getDetailMessage(), null );
			}
		}
	}

	private ICDIMemoryBlock createCDIBlock( BigInteger address, long length ) throws CDIException {
		ICDIMemoryBlock block = ((CDebugTarget)getDebugTarget()).getCDISession().getMemoryManager().createMemoryBlock( address.toString(), (int)length );
		getCDISession().getEventManager().addEventListener( this );
		return block;
	}

	private void disposeCDIBlock() {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null ) {
			try {
				((CDebugTarget)getDebugTarget()).getCDISession().getMemoryManager().removeBlock( block );
			}
			catch( CDIException e ) {
				DebugPlugin.log( e );
			}
			setCDIBlock( null );
			getCDISession().getEventManager().removeEventListener( this );
		}
	}

	private ICDIMemoryBlock getCDIBlock() {
		return fCDIBlock;
	}

	private void setCDIBlock( ICDIMemoryBlock cdiBlock ) {
		fCDIBlock = cdiBlock;
	}

	private BigInteger getRealBlockAddress() {
		ICDIMemoryBlock block = getCDIBlock();
		return ( block != null ) ? block.getStartAddress() : BigInteger.ZERO;
	}

	private long getBlockSize() {
		ICDIMemoryBlock block = getCDIBlock();
		return ( block != null ) ? block.getLength() : 0;
	}

	private void handleChangedEvent( ICDIMemoryChangedEvent event ) {
		ICDIMemoryBlock block = getCDIBlock();
		if ( block != null && fBytes != null ) {
			MemoryByte[] memBytes = (MemoryByte[])fBytes.clone();
			try {
				BigInteger start = getRealBlockAddress();
				long length = block.getLength();
				byte[] newBytes = block.getBytes();
				BigInteger[] addresses = event.getAddresses();
				saveChanges( addresses );
				for ( int i = 0; i < addresses.length; ++i ) {
					fChanges.add( addresses[i] );
					if ( addresses[i].compareTo( start ) >= 0 && addresses[i].compareTo( start.add( BigInteger.valueOf( length ) ) ) < 0 ) {
						int index = addresses[i].subtract( start ).intValue();
						if ( index >= 0 && index < memBytes.length && index < newBytes.length ) {
							memBytes[index].flags |= MemoryByte.CHANGED;
							memBytes[index].value = newBytes[index];
						}
					}
				}
				fBytes = memBytes;
				fireChangeEvent( DebugEvent.CONTENT );
			}
			catch( CDIException e ) {
				DebugPlugin.log( e );
			}			
		}
	}

	private void saveChanges( BigInteger[] addresses ) {
		fChanges.addAll( Arrays.asList( addresses ) );
	}

	private boolean hasChanged( BigInteger address ) {
		return fChanges.contains( address );
	}

	private void resetChanges() {
		if ( fBytes != null ) {
			BigInteger[] changes = (BigInteger[])fChanges.toArray( new BigInteger[fChanges.size()] );
			for ( int i = 0; i < changes.length; ++i ) {
				BigInteger real = getRealBlockAddress();
				if ( real.compareTo( changes[i] ) <= 0 && real.add( BigInteger.valueOf( getBlockSize() ) ).compareTo( changes[i] ) > 0 ) {
					int index = changes[i].subtract( real ).intValue();
					if ( index >= 0 && index < fBytes.length ) {
						fBytes[index].flags &= ~MemoryByte.CHANGED; 
					}
				}
			}
		}
		fChanges.clear();
		fireChangeEvent( DebugEvent.CONTENT );
	}
}
