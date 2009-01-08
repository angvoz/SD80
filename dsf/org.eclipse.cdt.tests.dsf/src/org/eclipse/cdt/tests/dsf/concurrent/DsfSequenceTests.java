/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the Sequence object.
 */
public class DsfSequenceTests {
    TestDsfExecutor fExecutor;
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
    }   
    
    @After 
    public void shutdownExecutor() throws ExecutionException, InterruptedException {
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    @Test 
    public void simpleTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override
                public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override
                public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create, start, and wait for the sequence.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        Assert.assertTrue(!sequence.isDone());
        Assert.assertTrue(!sequence.isCancelled());
        
        fExecutor.execute(sequence);
        sequence.get();

        // Check the count
        Assert.assertTrue(stepCounter.fInteger == 2);
        
        // Check post conditions
        Assert.assertTrue(sequence.isDone());
        Assert.assertTrue(!sequence.isCancelled());
    }
    
    @Test (expected = ExecutionException.class)
    public void rollbackTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed and steps 
        // rolled back.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();
        final IntegerHolder rollBackCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null));  //$NON-NLS-1$
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create and start.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to bomplete.
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertTrue(stepCounter.fInteger == 2);
            // Only one step is rolled back, the first one.
            Assert.assertTrue(rollBackCounter.fInteger == 1);
            
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(!sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    /**
     * The goal of this test it to check that if an exception is thrown within
     * the Step.execute(), the step will return from the Future.get() method.
     */
    @Test (expected = ExecutionException.class)
    public void exceptionTest() throws InterruptedException, ExecutionException {
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    throw new Error("Exception part of unit test."); //$NON-NLS-1$
                }
            }
        };
        
        // Create and start.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to bomplete.
        try {
            sequence.get();
        } finally {
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(!sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    
    @Test (expected = CancellationException.class)
    public void cancelBeforeWaitingTest() throws InterruptedException, ExecutionException {
        // Create the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
                new Sequence.Step() { 
                    @Override public void execute(RequestMonitor requestMonitor) {
                        Assert.assertTrue("Sequence was cancelled, it should not be called.", false); //$NON-NLS-1$
                    }
                }
            };
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };

        // Cancel before invoking the sequence.
        sequence.cancel(false);

        Assert.assertTrue(!sequence.isDone());
        Assert.assertTrue(sequence.isCancelled());

        // Start the sequence
        fExecutor.execute(sequence);
        
        // Block and wait for sequence to bomplete.
        try {
            sequence.get();
        } finally {
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }
    

    @Test (expected = CancellationException.class)
    public void cancelFromStepTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed and steps 
        // rolled back.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();
        final IntegerHolder rollBackCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    
                    // Perform the cancel!
                    getSequence().cancel(false); 
                    
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create and start sequence with a delay.  Delay so that we call get() before
        // cancel is called.
        final Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.schedule(sequence, 1, TimeUnit.MILLISECONDS);

        // Block to retrieve data
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertTrue(stepCounter.fInteger == 2);
            // Both roll-backs should be performed since cancel does not take effect until
            // after the step is completed.
            Assert.assertTrue(rollBackCounter.fInteger == 2);
            
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }            
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }
    
    @Test (expected = CancellationException.class)
    public void cancelBeforeWithProgressManagerTest() throws InterruptedException, ExecutionException {
        // Create the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
                new Sequence.Step() { 
                    @Override public void execute(RequestMonitor requestMonitor) {
                        Assert.assertTrue("Sequence was cancelled, it should not be called.", false); //$NON-NLS-1$
                    }
                }
            };
        
        // Create the progress monitor that we will cancel.
        IProgressMonitor pm = new NullProgressMonitor();
        
        // Create the seqeunce with our steps.
        Sequence sequence = new Sequence(fExecutor, pm, "", "") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override public Step[] getSteps() { return steps; }
        };

        // Cancel the progress monitor before invoking the sequence.  Note 
        // that the state of the sequence doesn't change yet, because the 
        // sequence does not check the progress monitor until it is executed.
        pm.setCanceled(true);

        // Start the sequence
        fExecutor.execute(sequence);
        
        // Block and wait for sequence to bomplete.  Exception is thrown, 
        // which is expected.
        try {
            sequence.get();
        } finally {
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }
    }

    
}