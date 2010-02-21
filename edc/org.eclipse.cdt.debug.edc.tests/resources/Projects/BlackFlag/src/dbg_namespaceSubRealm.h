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

namespace SubRealm1
{

#include "dbg_namespaceDomains.h"

typedef struct {
	char achar;
	UCHAR auchar;
	SCHAR aschar;
	short ashort;
	USHORT aushort;
	SSHORT asshort;
	int aint;
	UINT auint;

} struct_type;

struct_type SubRealmStruct;

typedef struct {
	volatile unsigned x:1;
	volatile unsigned y:2;
	volatile unsigned z:3;
	volatile unsigned w:16;
} bitfield_type;

enum enum_type { zero, one, two, three, four };

}

namespace SubRealm2
{

#include "dbg_namespaceDomains.h"
	
typedef struct {

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

struct_type SubRealmStruct;

typedef struct {
	volatile unsigned x:1;
	volatile unsigned y:2;
	volatile unsigned z:3;
	volatile unsigned w:16;
} bitfield_type;

enum enum_type { zero, one, two, three, four };

}
