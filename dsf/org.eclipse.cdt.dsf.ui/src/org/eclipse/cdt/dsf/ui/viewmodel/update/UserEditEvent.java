/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.HashSet;
import java.util.Set;

/**
 * An event representing a user editing of the data in the viewer.  Typically, when 
 * a viewer is configured to be in a manual update mode, if user edits a value, the 
 * viewer should still update at least the value that the user editor.  This event
 * is used to accomplish that behavior.
 * 
 * @since 1.0
 */
public class UserEditEvent {
    private final Set<Object> fElements;
    
    public UserEditEvent(Object element) {
        fElements = new HashSet<Object>();
        fElements.add(element);
    }

    public UserEditEvent(Set<Object> elements) {
        fElements = elements;
    }
    
    public Set<Object> getElements() {
        return fElements;
    }
}