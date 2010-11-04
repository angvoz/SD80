package org.eclipse.cdt.debug.edc.acpm;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * An ACPM cache for
 * {@link IMemory#getMemory(IMemoryDMContext, IAddress, long, int, int, DataRequestMonitor)}
 * 
 * @since 2.0
 */
public class MemoryRangeCache extends BaseRangeCache<MemoryByte> {
	
	/** See {@link #getAddress()} */
	final private IAddress fAddress;
	
	/** See {@link #getWordSize()} */
	final private int fWordSize;

	public MemoryRangeCache(IMemory service, IDMContext ctx, IAddress address, int wordSize) {
        super(service, ctx);
        fAddress = address;
        fWordSize = wordSize;
    }


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RangeCache#retrieve(long, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	protected void retrieve(long offset, int count, final DataRequestMonitor<List<MemoryByte>> rm) {
		((IMemory)fService).getMemory((IMemoryDMContext)fCtx, fAddress, offset, fWordSize, count, new DataRequestMonitor<MemoryByte[]>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				rm.setData(Arrays.asList(getData()));
				rm.done();
			}
		});
	}

	@DsfServiceEventHandler
	public void resumedEventHandler(IDMEvent<?> e) {
		// TODO: be more discriminating instead instead of calling our super
		// class for any event
		super.resumedEventHandler(e);
	}

	/** The base address range requests are relative to */
	public IAddress getAddress() {
		return fAddress;
	}

	/** The the size, in bytes, of an addressable item */
	public int getWordSize() {
		return fWordSize;
	}


	public boolean wasCanceled() {
		// TODO Auto-generated method stub
		return false;
	}
}
