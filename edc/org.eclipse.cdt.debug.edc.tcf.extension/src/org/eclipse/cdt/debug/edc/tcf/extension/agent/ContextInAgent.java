/*******************************************************************************
 * Copyright (c) 2009,2010 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. Oct 19, 2009
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.services.IRunControl;

/**
 * Context in TCF agent.<br>
 * Please see documents in org.eclipse.tm.tcf.docs for more.
 */
public abstract class ContextInAgent  {

	/**
	 * Context that has its own registers, e.g. a thread is such context but
	 * process is not.
	 * @since 2.0
	 */
	public interface IRegisterOwnerContext {};
	
	/**
	 * Context representing a run control entity, e.g. a thread or a process.
	 * Register context is an example that's not run control context.
	 * @since 2.0
	 */
	public interface IRunControlContext {};
	
    private final Map<String, Object> properties;
    
    /**
     * List of IDs of child contexts.
     */
    private List<String>	children = new ArrayList<String>();

    /**
     * Construct a new context. 
     * 
     * @param props initial properties for the context. Can be null.
     */
    public ContextInAgent(Map<String, Object> props) {
    	if (props == null)
    		properties = new HashMap<String, Object>();
    	else
    		// Make a copy 
        	properties = new HashMap<String, Object>(props);
    }

    public void addChild(ContextInAgent c) {
    	children.add(c.getID());
    }
    
    public void removeChild(ContextInAgent c) {
    	children.remove(c.getID());
    }

    public void removeChild(String id) {
    	children.remove(id);
    }

    /**
     * Clear children list of this context.
     */
    public void clearChildren() {
    	children.clear();
    }
    
    /**
     * Get list of child contexts.
     * @return a copied list of IDs of child contexts.
     */
    public List<String> getChildren() {
    	List<String> ret = new ArrayList<String>();
    	ret.addAll(children);
    	return ret;
    }

    /**
     * Get reference to internal properties of the context.
     * 
     * @return
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getID() {
        return (String)properties.get(IRunControl.PROP_ID);
    }

    public String getParentID() {
        return (String)properties.get(IRunControl.PROP_PARENT_ID);
    }

    public boolean isContainer() {
        Boolean b = (Boolean)properties.get(IRunControl.PROP_IS_CONTAINER);
        return b != null && b.booleanValue();
    }

    public boolean hasState() {
        Boolean b = (Boolean)properties.get(IRunControl.PROP_HAS_STATE);
        return b != null && b.booleanValue();
    }

    public boolean canResume(int mode) {
        if (properties.containsKey(IRunControl.PROP_CAN_RESUME)) {
            int b = ((Number)properties.get(IRunControl.PROP_CAN_RESUME)).intValue();
            return (b & (1 << mode)) != 0;
        }
        return false;
    }

    public boolean canCount(int mode) {
        if (properties.containsKey(IRunControl.PROP_CAN_COUNT)) {
            int b = ((Number)properties.get(IRunControl.PROP_CAN_COUNT)).intValue();
            return (b & (1 << mode)) != 0;
        }
        return false;
    }

    public boolean canSuspend() {
        Boolean b = (Boolean)properties.get(IRunControl.PROP_CAN_SUSPEND);
        return b != null && b.booleanValue();
    }

    public boolean canTerminate() {
        Boolean b = (Boolean)properties.get(IRunControl.PROP_CAN_TERMINATE);
        return b != null && b.booleanValue();
    }

    public String toString() {
        return "[ContextInAgent " + properties.toString() + "]";
    }
}

