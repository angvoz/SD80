/*******************************************************************************
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. May, 2009
 *******************************************************************************/

This plugin is supposed to be an extension to org.eclipse.tm.tcf.core.

--------------- Note ! ----------------------------
As with org.eclipse.tm.tcf.core, this plugin should be dependent on as few 
eclipse components as possible so that it can be used by non eclipse based Java 
application, e.g. a stand-alone TCF agent in java. So watch out on adding 
dependencies to the plugin.
---------------------------------------------------

The plugin is a place for such content:

1. Definition of new TCF services, usually an interface class and a proxy class.
For instance, the ISimpleRegisters and SimpleRegistersProxy.

2. Utilities and common code for agents in java, e.g. AgentException and AgentUtils.

3. Copies of classes from org.eclipse.tm.tcf.core with our local change. E.g. ServerTCP. 
Such copies should be removed after our change is contributed back to TCF. If some day
we have bigger changes to TCF upstream, we may need a full copy of TCF plugins in local 
CVS.  