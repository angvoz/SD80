/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Wind River Systems, Inc.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 */
public class AsmTextTools {
	
    private class PreferenceListener implements IPropertyChangeListener, Preferences.IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            adaptToPreferenceChange(event);
        }
        public void propertyChange(Preferences.PropertyChangeEvent event) {
            adaptToPreferenceChange(new PropertyChangeEvent(event.getSource(), event.getProperty(), event.getOldValue(), event.getNewValue()));
        }
    }
    
	/** The color manager -- use the same as for C code */
	private CColorManager fColorManager;
	/** The Asm source code scanner */
	private AsmCodeScanner fCodeScanner;
	/** The ASM multiline comment scanner */
	private CCommentScanner fMultilineCommentScanner;
	/** The ASM singleline comment scanner */
	private CCommentScanner fSinglelineCommentScanner;
	/** The ASM string scanner */
	private SingleTokenCScanner fStringScanner;
	/** The ASM preprocessor scanner */
	private AsmPreprocessorScanner fPreprocessorScanner;
	
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
    /** The core preference store */
    private Preferences fCorePreferenceStore;		
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
	/**
	 * Creates a new Asm text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public AsmTextTools(IPreferenceStore store) {
        this(store, null);
    }
    
    /**
     * Creates a new Asm text tools collection and eagerly creates 
     * and initializes all members of this collection.
     */
    public AsmTextTools(IPreferenceStore store, Preferences coreStore) {
		if(store == null) {
			store = CUIPlugin.getDefault().getPreferenceStore();
		}
        
		fColorManager= new CColorManager();
		fCodeScanner= new AsmCodeScanner(fColorManager, store);
		fPreprocessorScanner= new AsmPreprocessorScanner(fColorManager, store);

        fMultilineCommentScanner= new CCommentScanner(fColorManager, store, coreStore, ICColorConstants.C_MULTI_LINE_COMMENT);
        fSinglelineCommentScanner= new CCommentScanner(fColorManager, store, coreStore, ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(fColorManager, store, ICColorConstants.C_STRING);

        fPreferenceStore = store;
		store.addPropertyChangeListener(fPreferenceListener);
        
        fCorePreferenceStore= coreStore;
        if (fCorePreferenceStore != null) {
            fCorePreferenceStore.addPropertyChangeListener(fPreferenceListener);
        }
    }
	
	/**
	 * Creates a new Asm text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public AsmTextTools() {
		this((IPreferenceStore)null);
	}
	/**
	 * Disposes all members of this tools collection.
	 */
	public void dispose() {
		
		fCodeScanner= null;
		
		fMultilineCommentScanner= null;
		fSinglelineCommentScanner= null;
		fStringScanner= null;
		
		if (fColorManager != null) {
			fColorManager.dispose();
			fColorManager= null;
		}
		
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
			fPreferenceStore= null;
            
            if (fCorePreferenceStore != null) {
                fCorePreferenceStore.removePropertyChangeListener(fPreferenceListener);
                fCorePreferenceStore= null;
            }
            
			fPreferenceListener= null;
		}
	}
	
	/**
	 * Gets the color manager.
	 */
	public CColorManager getColorManager() {
		return fColorManager;
	}
	
	/**
	 * Gets the code scanner used.
	 */
	public RuleBasedScanner getCodeScanner() {
		return fCodeScanner;
	}
		
	/**
	 * Returns a scanner which is configured to scan multiline comments.
	 *
	 * @return a multiline comment scanner
	 */
	public RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan singleline comments.
	 *
	 * @return a singleline comment scanner
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}
	
	/**
	 * Returns a scanner which is configured to scan strings.
	 *
	 * @return a string scanner
	 */
	public RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Asm preprocessor directives.
	 *
	 * @return an Asm preprocessor directives scanner
	 */
	public RuleBasedScanner getPreprocessorScanner() {
		return fPreprocessorScanner;
	}

	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one its contained components.
	 * 
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return  fCodeScanner.affectsBehavior(event) ||
					fMultilineCommentScanner.affectsBehavior(event) ||
					fSinglelineCommentScanner.affectsBehavior(event) ||
					fStringScanner.affectsBehavior(event) ||
					fPreprocessorScanner.affectsBehavior(event);
	}
	
	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	protected void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
		if (fPreprocessorScanner.affectsBehavior(event))
			fPreprocessorScanner.adaptToPreferenceChange(event);
	}
		
}
