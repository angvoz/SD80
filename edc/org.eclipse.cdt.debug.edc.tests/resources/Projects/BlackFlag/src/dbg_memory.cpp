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

void dbg_memory_local();
void dbg_memory_global();
void dbg_memory_derivedTypes();
void dbg_memory_symbianTypes();

void dbg_memory_local()
{
// Local Variables
	char lchar = 'a';
#ifdef _Floating_Point_Support_
	double ldouble = 1.0;
	float lfloat = 2.0;
#endif /* _Floating_Point_Support_ */
	int lint = 3;
	long llong = 4;
#ifdef _Floating_Point_Support_
	long double llongdouble = 5.0;
#endif /* _Floating_Point_Support_ */
	SCHAR lschar = 'b';
	short lshort = 6;
	SINT lsint = 7;
	SLONG lslong = 8;
#ifdef __MSL_LONGLONG_SUPPORT__
	SLONGLONG lslonglong = 9;
#endif
	SSHORT lsshort = 10;
	UCHAR luchar = 11;
	UINT luint = 12;
	ULONG lulong = 13;
#ifdef __MSL_LONGLONG_SUPPORT__
	ULONGLONG lulonglong = 14;
#endif
	USHORT lushort = 15;
}

void dbg_memory_global()
{
	int dummyvar=0;
// Global Variables
 	gchar = 'c';
#ifdef _Floating_Point_Support_
	gdouble = 1.5;
	gfloat = 2.5;
#endif /* _Floating_Point_Support_ */
	gint = 3;
	glong = 4;
#ifdef _Floating_Point_Support_
	glongdouble = 5.5;
#endif /* _Floating_Point_Support_ */
	gschar = 'd';
	gshort = 6;
	gsint = 7;
	gslong = 8;
#ifdef __MSL_LONGLONG_SUPPORT__
	gslonglong = 9;
#endif
	gsshort = 10;
	guchar = 11;
	guint = 12;
	gulong = 13;
#ifdef __MSL_LONGLONG_SUPPORT__
	gulonglong = 14;
#endif
	gushort = 15;
}

void dbg_memory_derivedTypes()
{
// derived types
	struct_type lstruct;

	lstruct.achar = 'a';
	lstruct.auchar = 1;
	lstruct.aschar = 'b';
	lstruct.ashort = 2;
	lstruct.aushort = 3;
	lstruct.asshort = 4;
	lstruct.aint = 5;
	lstruct.auint = 6;
	lstruct.asint = 7;
	lstruct.along = 8;
	lstruct.aulong = 9;
	lstruct.aslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__
	lstruct.aulonglong = 11;
	lstruct.aslonglong = 12;
#endif /* __MSL_LONGLONG_SUPPORT__ */
#ifdef _Floating_Point_Support_
	lstruct.afloat = 13.0;
	lstruct.adouble = 14.0;
	lstruct.alongdouble = 15.0;
#endif /* _Floating_Point_Support_ */

	gstruct = lstruct;
}

void dbg_memory()
{
	dbg_memory_local();
	dbg_memory_global();
	dbg_memory_derivedTypes();
}
/**************************************************************************************************
File:			dbg_memory.c

Libs Used:		n/a
---------------------------------------------------------------------------------------------------
PRECONDITIONS/ASSUMPTIONS

OBJECTIVE

PRECONDITION
1. From the Window menu show the memory view if it is not shown already
2. Close any existing memory monitor
3. Open the Disassembly view

PROCEDURE

Add Memory Monitor:
Simple locals
1. Set a breakpoint at line stopper = 1; and hit Resume. For every local variable click on the "Add Memory Monitor" icon to add memory monitor. Since you have many variables, use the different ways of adding memory monitors
	- Type the name of the variable (prefixed with '&')
	- Copy and paste (prefixed with '&')
	- Select from the history list
2. Check the value of the variable inside the memory monitor and compare it with the expected value as seen inside the variables view
(If the variable name was not prefixed with '&', the resulted monitor will use the value of the variable as the address need to be monitored)

Simple globals
3. Set a breakpoint at line stopper = 2; and hit Resume. For every global add memory monitor. Use the different ways of adding memory monitors
	- Type the name of the variable (prefixed with '&')
	- Copy and paste (prefixed with '&')
	- Select from the history list
4. Check the value of the variable inside the memory monitor and compare it with the expected value as seen inside the variables view

local and global derived types
5. Set a breakpoint at line stopper = 3; and hit Resume. add memory monitor for lstruct, gstruct and every struct member of both. Use the different ways of adding memory monitors
	- Type the name of the variable (prefixed with '&')
	- Copy and paste (prefixed with '&')
	- Select from the history list
6. Check the value of the variable inside the memory monitor and compare it with the expected value as seen inside the variables view

Address
7. Click on the "Add Memory Monitor" icon and type a valid address (prefixe it with "0x") and compare the contents with the code shown inside the Disassembly view for the same address

Function name
8. Click on the "Add Memory Monitor" icon and type the function name and compare the contents with the code shown inside the Disassembly view for the same address

Undefined identifier
9. Click on the "Add Memory Monitor" icon and type some dummy identifier that is not a name of any variable or function

New Memory View:
10. For every variable add new memory view. In addition to the "Hex", add "ASCII", "Signed Integer" and "Unsigned Integer"
11. In some cases use the shift key to select more than one rendering
12. In some cases use the Ctrl key to select more than one rendering
13. For every view check the value of the variable

Toggle Memory Mointors Pane:
14. Click on the "Toggle Memory Mointors Pane" icon to remove the memory monitor pane and click on it one more time to toggle it back

Link Memory Rendering Panes:
15. Highlight a single memory monitor (&lint for example) then add 2 more memory renderings (ASCII and Signed for example). Click on the "Link Memory Rendering Panes" icon and then click on a memory cell other than the current highlighted one. Now switch between all the 3 rendering panes of the same monitor and notice that the same memory cell you highlighted is highlighted in the other 2 rendering panes. Right-click on the memory rendering pane and select "Restore to the base address" and switch between all 3 rendering panes and notice that all 3 of them restored to the base address


Add Renderings:
16. Click on the "Add Rendering(s)" icon and choose one formate for the same variable
17. Click on the "Add Rendering(s)" icon and from the history list choose another variable

Remove Rendering:
18. Click on the "Remove Rendering" icon to remove the current rendering

Remove Memory Monitor:
19. Highlight a single variable inside the memory monitors pane and click on the "Remove Memory Monitor" icon
20. Using the shift key or Ctrl key, highlight couple of variables of each type (Simple locals, Simple globals ...etc) and for each, click on the "Remove Memory Monitor" icon

Remove All:
21. Click on the "Remove All" icon to remove all memory monitors

CHECKS

Add Memory Monitor:
1. New memory monitor should be added for every local variable
2. The value of the local variable inside the memory monitor should match the value of the variable inside the variables view
3. New memory monitor should be added for every global variable
4. The value inside the memory monitor should match the value inside the variables view
5. New memory monitor should be added for every local and global struct and struct member
6. The value inside the memory monitor should match the value oinside the variables view
7. New memory monitor should be added for the entered address and the contents should match the code as shown inside the Disassembly view
8. New memory monitor should be added for the function specified and the contents should match the code as shown inside the Disassembly view
9. No memory monitor should be added for undefined identifier and you should get an error dialogue

New Memory View:
10. Once click on the "New Memory View" icon, a new memory view is added to the right of the existing memory views
11. New memory views are added for all the different renderings you have selected using the shift key
12. New memory views are added for all the different renderings you have selected using the Ctrl key
13. The value should match the value inside the variables view and in the right fiormat (Hex, ASCII ...etc)

Toggle Memory Mointors Pane:
14. If you have the Memory Monitors pane open, it should disappear once you click on the icon and it should show back once you toggle the icon back

Link Memory Rendering Panes:
15. As long as the "Link Memory Rendering Panes" icon is on, all rendering panes of the same memory monitor should be syncronized

Add Renderings:
16. New memory view should be added for that variable and of the chosen format
17. New memory view should be added for the chosen variable and of the chosen format

Remove Rendering:
18. The current memory rendering should be removed

Remove Memory Monitor:
19. That single memory monitor should be removed
20. The highlighted mutliple memory monitors should all be removed

Remove All:
21. All memory monitors should be removed

*************************************************************************************************************/

