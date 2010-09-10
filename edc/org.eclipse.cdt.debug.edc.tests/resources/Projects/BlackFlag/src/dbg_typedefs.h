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
/* Typedefs */

#ifndef _INCLUDE_DBG_TYPEDEFS_H
#define _INCLUDE_DBG_TYPEDEFS_H

#define __MSL_LONGLONG_SUPPORT__
#define _Floating_Point_Support_
//#define _ARM_Support_

typedef	unsigned char		UCHAR;
typedef	signed char			SCHAR;
typedef	unsigned short		USHORT;
typedef	signed short		SSHORT;
typedef	unsigned int		UINT;
typedef	signed int			SINT;
typedef	unsigned long		ULONG;
typedef	signed long			SLONG;

#ifdef __MSL_LONGLONG_SUPPORT__
typedef	unsigned long long	ULONGLONG;
typedef	signed long long	SLONGLONG;
#endif /* __MSL_LONGLONG_SUPPORT__ */

typedef struct {
	char achar;
	UCHAR auchar;
	SCHAR aschar;
	short ashort;
	USHORT aushort;
	SSHORT asshort;
	int aint;
	UINT auint;
	SINT asint;
	long along;
	ULONG aulong;
	SLONG aslong;
#ifdef __MSL_LONGLONG_SUPPORT__
	ULONGLONG aulonglong;
	SLONGLONG aslonglong;
#endif /* __MSL_LONGLONG_SUPPORT__ */
#ifdef _Floating_Point_Support_
	float afloat;
	double adouble;
	long double alongdouble;
#endif /* _Floating_Point_Support_ */
} struct_type;

typedef struct {
	volatile unsigned x:1;
	volatile unsigned y:2;
	volatile unsigned z:3;
	volatile unsigned w:16;
} bitfield_type;

typedef union {
	volatile int x;
	volatile long y;
} union_type;

enum enum_type { zero, one, two, three, four };

/* Globals */
extern char gchar;
extern UCHAR guchar;
extern SCHAR gschar;
extern short gshort;
extern USHORT gushort;
extern SSHORT gsshort;
extern int gint;
extern UINT guint;
extern SINT gsint;
extern long glong;
extern ULONG gulong;
extern SLONG gslong;
#ifdef __MSL_LONGLONG_SUPPORT__
extern ULONGLONG gulonglong;
extern SLONGLONG gslonglong;
#endif /* __MSL_LONGLONG_SUPPORT__ */
#ifdef _Floating_Point_Support_
extern float gfloat;
extern double gdouble;
extern long double glongdouble;
#endif /* _Floating_Point_Support_ */

extern struct_type gstruct;
extern bitfield_type gbitfield;
extern union_type gunion;
extern enum enum_type genum;
extern int garray[40];
extern const char *gstring;

#endif
