/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Discovery helper interface
 */

#ifndef D_discovery_help
#define D_discovery_help

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Create default discovery master server that
 * supports only basic services needed for discovery.
 */
extern void create_default_discovery_master(void);

#ifdef __cplusplus
}
#endif

#endif