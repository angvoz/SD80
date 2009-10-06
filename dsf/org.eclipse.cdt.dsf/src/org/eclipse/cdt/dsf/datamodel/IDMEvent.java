/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

/**
 * Base interface for events that signify changes in the data model. The only
 * thing all such events must have in common is that they reference an
 * {@link IDMContext}
 * 
 * @param <V>
 *            Data Model context type that is affected by this event.
 * 
 * @since 1.0
 */
public interface IDMEvent <V extends IDMContext> {
    V getDMContext();
}
