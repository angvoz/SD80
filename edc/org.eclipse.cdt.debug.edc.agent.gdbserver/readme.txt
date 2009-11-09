/*******************************************************************************
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. Apr 28, 2009
 *******************************************************************************/

This is a TCF agent that provides debugger related services by 
talking with given gdbserver instance using GDB Serial Remote Protocol.

Let's try to keep it as a stand-alone Java application so that it
is light-weight and can easily be started on different nodes in the network. 
Currently it depends on these Eclipse plugins:
	* org.eclipse.cdt.debug.edc.tcf.extension
	* org.eclipse.tm.tcf
which in turns depends on
	* org.eclipse.osgi
	* org.eclipse.equinox.*
	* org.eclipse.core.runtime
	* org.eclipse.core.jobs
	* org.eclipse.core.contenttype
	