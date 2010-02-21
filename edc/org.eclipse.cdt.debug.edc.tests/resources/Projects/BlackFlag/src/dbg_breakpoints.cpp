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

#include "dbg_prototypes.h"
#include "dbg_typedefs.h"

int foo();

int foo()
{
	int x;

	x = 1;
	/* This is a comment line. Try to set a breakpoint here */
	return 0;
}

void dbg_breakpoints()
{
	int i, k;
	int SetBp_1, SetBp_2, SetBp_3, SetBp_4, SetBp_5, SetBp_6;
	int RemoveBp_1, RemoveBp_2, RemoveBp_3, RemoveBp_4, RemoveBp_5;
	int DisableBp_1, DisableBp_2, DisableBp_3, DisableBp_4, DisableBp_5, DisableBp_6, DisableBp_7;
	int EnableBp_1, EnableBp_2, EnableBp_3, EnableBp_4, EnableBp_5, EnableBp_6, EnableBp_7,  EnableBp_8;
	int Bp_1, Bp_2, Bp_3, Bp_4, Bp_5, Bp_6, Bp_7, Bp_8, Bp_9, Bp_10, Bp_11, Bp_12;
	int Stopper;

	// Setting Breakpoints
	SetBp_1 = 0;
	SetBp_2 = 0;
	SetBp_3 = 0;
	SetBp_4 = 0;
	SetBp_5 = 0;
	SetBp_6 = 0;
	foo();

	// Removing Breakpoints
	RemoveBp_1 = 0;
	RemoveBp_2 = 0;
	RemoveBp_3 = 0;
	RemoveBp_4 = 0;
	RemoveBp_5 = 0;
	Stopper = 0;

	// Disabling Breakpoints
	DisableBp_1 = 0;
	DisableBp_2 = 0;
	DisableBp_3 = 0;
	DisableBp_4 = 0;
	DisableBp_5 = 0;
	DisableBp_6 = 0;
	DisableBp_7 = 0;
	Stopper = 1;

	// Enabling Breakpoints
	EnableBp_1 = 0;
	EnableBp_2 = 0;
	EnableBp_3 = 0;
	EnableBp_4 = 0;
	EnableBp_5 = 0;
	EnableBp_6 = 0;
	EnableBp_7 = 0;
	EnableBp_8 = 0;

	// Removing all breakpoints:
	Bp_1 = 0;
	Bp_2 = 0;
	Bp_3 = 0;
	Bp_4 = 0;
	Bp_5 = 0;
	Bp_6 = 0;
	Bp_7 = 0;
	Bp_8 = 0;
	Bp_9 = 0;

	// Conditional breakpoints
	i = 5;
	garray[5] = 100;
	garray[5] = 175;
	for (i=0; i<10; i++)
		k = i;

	// Skipping Breakpoints:
	Bp_10 = 0;
	Bp_11 = 0;
	Bp_12 = 0;
}

