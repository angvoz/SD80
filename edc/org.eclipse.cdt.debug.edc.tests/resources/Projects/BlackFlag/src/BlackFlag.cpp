/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

#include "dbg_typedefs.h"
#include "dbg_prototypes.h"
#include "dbg_globals.h"

int main() {
	dbg_breakpoints();
	dbg_derived_types();
	dbg_expressions();
	dbg_memory();
	dbg_rtti();
	dbg_simple_types();
	dbg_stack_crawl();
	dbg_stepping();
	dbg_stress_test();
	dbg_variables();
	dbg_watchpoints();
	dbg_binary_tree();
  	dbg_deepCopy();
  	dbg_friend();
  	dbg_linked_lists();
  	dbg_multipleInheritance();
	dbg_pointers();
	dbg_singleInheritance();
	dbg_virtualInheritance();

	dbg_program_control();

	return 0;
}
