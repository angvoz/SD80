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


	static char sgchar;
#ifdef _Floating_Point_Support_
	static double sgdouble;
	static float sgfloat;	
#endif /* _Floating_Point_Support_ */
	static int sgint;
	static long sglong;
#ifdef _Floating_Point_Support_
	static long double sglongdouble;
#endif /* _Floating_Point_Support_ */
	static SCHAR sgschar;	
	static short sgshort;
	static SINT sgsint;
	static SLONG sgslong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	static SLONGLONG sgslonglong;
#endif
	static SSHORT sgsshort;
	static UCHAR sguchar;
	static UINT sguint;
	static ULONG sgulong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	static ULONGLONG sgulonglong;
#endif
	static USHORT sgushort;		

	volatile char vgchar;
#ifdef _Floating_Point_Support_
	volatile double vgdouble;
	volatile float vgfloat;	
#endif /* _Floating_Point_Support_ */
	volatile int vgint;
	volatile long vglong;
#ifdef _Floating_Point_Support_
	volatile long double vglongdouble;
#endif /* _Floating_Point_Support_ */
	volatile SCHAR vgschar;	
	volatile short vgshort;
	volatile SINT vgsint;
	volatile SLONG vgslong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	volatile SLONGLONG vgslonglong;
#endif
	volatile SSHORT vgsshort;
	volatile UCHAR vguchar;
	volatile UINT vguint;
	volatile ULONG vgulong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	volatile ULONGLONG vgulonglong;
#endif
	volatile USHORT vgushort;	
		
void dbg_simple_types()
{
//Locals
	char lchar = '1';
#ifdef _Floating_Point_Support_
	double ldouble = 2.2;
	float lfloat = 3.3;	
#endif /* _Floating_Point_Support_ */
	int lint = 4;
	long llong = 5;
#ifdef _Floating_Point_Support_
	long double llongdouble = 6.6;
#endif /* _Floating_Point_Support_ */
	SCHAR lschar = '7';	
	short lshort = 8;
	SINT lsint = 9;
	SLONG lslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	SLONGLONG lslonglong = 11;
#endif
	SSHORT lsshort = 12;
	UCHAR luchar = 13;
	UINT luint = 14;
	ULONG lulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	ULONGLONG lulonglong = 16;
#endif
	USHORT lushort = 17;	
	
//Static Locals
	static char slchar = '1';
#ifdef _Floating_Point_Support_
	static double sldouble = 2.2;
	static float slfloat = 3.3;	
#endif /* _Floating_Point_Support_ */
	static int slint = 4;
	static long sllong = 5;
#ifdef _Floating_Point_Support_
	static long double sllongdouble = 6.6;
#endif /* _Floating_Point_Support_ */
	static SCHAR slschar = '7';	
	static short slshort = 8;
	static SINT slsint = 9;
	static SLONG slslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	static SLONGLONG slslonglong = 11;
#endif
	static SSHORT slsshort = 12;
	static UCHAR sluchar = 13;
	static UINT sluint = 14;
	static ULONG slulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	static ULONGLONG slulonglong = 16;
#endif
	static USHORT slushort = 17;	

//Volatile Locals
	volatile char vlchar = '1';
#ifdef _Floating_Point_Support_
	volatile double vldouble = 2.2;
	volatile float vlfloat = 3.3;	
#endif /* _Floating_Point_Support_ */
	volatile int vlint = 4;
	volatile long vllong = 5;
#ifdef _Floating_Point_Support_
	volatile long double vllongdouble = 6.6;
#endif /* _Floating_Point_Support_ */
	volatile SCHAR vlschar = '7';	
	volatile short vlshort = 8;
	volatile SINT vlsint = 9;
	volatile SLONG vlslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	volatile SLONGLONG vlslonglong = 11;
#endif
	volatile SSHORT vlsshort = 12;
	volatile UCHAR vluchar = 13;
	volatile UINT vluint = 14;
	volatile ULONG vlulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	volatile ULONGLONG vlulonglong = 16;
#endif
	volatile USHORT vlushort = 17;	

//Globals
	gchar = '1';
#ifdef _Floating_Point_Support_
	gdouble = 2.2;
	gfloat = 3.3;	
#endif /* _Floating_Point_Support_ */
	gint = 4;
	glong = 5;
#ifdef _Floating_Point_Support_
	glongdouble = 6.6;
#endif /* _Floating_Point_Support_ */
	gschar = '7';	
	gshort = 8;
	gsint = 9;
	gslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	gslonglong = 11;
#endif
	gsshort = 12;
	guchar = 13;
	guint = 14;
	gulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	gulonglong = 16;
#endif
	gushort = 17;	
	
//Static Globals
	sgchar = slchar;
#ifdef _Floating_Point_Support_
	sgdouble = sldouble;
	sgfloat = slfloat;	
#endif /* _Floating_Point_Support_ */
	sgint = slint;
	sglong = sllong;
#ifdef _Floating_Point_Support_
	sglongdouble = sllongdouble;
#endif /* _Floating_Point_Support_ */
	sgschar = slschar;	
	sgshort = slshort;
	sgsint = slsint;
	sgslong = slslong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	sgslonglong = slslonglong;
#endif
	sgsshort = slsshort;
	sguchar = sluchar;
	sguint = sluint;
	sgulong = slulong;
#ifdef __MSL_LONGLONG_SUPPORT__	
	sgulonglong = slulonglong;
#endif
	sgushort = slushort;		

//Volatile Globals
	vgchar = '1';
#ifdef _Floating_Point_Support_
	vgdouble = 2.2;
	vgfloat = 3.3;	
#endif /* _Floating_Point_Support_ */
	vgint = 4;
	vglong = 5;
#ifdef _Floating_Point_Support_
	vglongdouble = 6.6;
#endif /* _Floating_Point_Support_ */
	vgschar = '7';	
	vgshort = 8;
	vgsint = 9;
	vgslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	vgslonglong = 11;
#endif
	vgsshort = 12;
	vguchar = 13;
	vguint = 14;
	vgulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	vgulonglong = 16;
#endif
	vgushort = 17;			
}	/* set breakpoint here */

/*************************************************************************************************************
File:			dbg_simple_types.c	

-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	Fundamental data types with global and local scope of type automatic, 
	volatile and static are displayed correctly in the Variables pane of 
	the Carbide IDE..
	
PROCEDURE

	Set a breakpoint on the closing bracket of the function and run to it.
-------------------------------------------------------------------------------------------------------------			
CHECKS

 	Local variables(automatic, volatile & static) and global variables
 	(automatic, volatile & global) are displayed in the Variables pane. 
 	There should 17 variables for each group with values going from 1 to 17.
 	
 	There should be no duplicates or unknown variables.

-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
	
