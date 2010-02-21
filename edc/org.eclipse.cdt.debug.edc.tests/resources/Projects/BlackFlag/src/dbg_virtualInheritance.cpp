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
#include "dbg_virtualInheritance.h"

void dbg_virtualInheritance();

void dbg_virtualInheritance()
{
	Derv1 dobj1;			// Object of derived class 1
	Derv2 dobj2;			// Object of derived class 2
	Base* ptr;				// Pointer to base clase
	
	VDerv1 vdob1;			// Object of virtual derived class 1
	VDerv2 vdob2;			// Object of virtual derived class 2
	VBase* vptr;			// Pointer to virtual base clase
		
	
	ptr = &dobj1;			// Pointer now contains address of dobj1
	ptr -> show();			// Execute Base::show()
	
	ptr = &dobj2;			// Pointer now contains address of dobj2
	ptr -> show();			// Execute Base::show()
	
	vptr = &vdob1;			// Pointer now contains address of vdob1
	vptr -> show();			// Execute VDerv1::show()
	
	vptr = &vdob2;			// Pointer now contains address of vdob2
	vptr -> show();			// Execute VDerv2::show()
		
}

/*************************************************************************************************************
File:			dbg_virtualInheritance.cpp	
Function:		show()

Classes Used:	Base, Derv1, Derv2, VBase, VDerv1, VDerv2		
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Non-virtual functions in the base class will override functions of the same name in a derived class.
2 	Virtual functions in the base class will be overridden by functions of the same name in a derived class.
	   	
PROCEDURE

1	Step over all object instantiations and ptr/vptr invokations to the first call of ptr->show(). 
2	Step into ptr->show().
3	Step to the end of the show() function.
4	Step out of the show() function and step to second call to ptr->show().
5	Step into ptr->show().
6	Step to the end of the show() function.
7	Step out of the show() function and step to first call to vptr->show().
8	Step into vptr->show()
9	Step to the end of the show() function.
10	Step out of the show() function and step to second call to vptr->show().
11	Step into vptr->show()
12	Step to the end of the show() function.	
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	The ptr variable value should be the address of the object dobj1.
2	The PC arrow should be at the show() function.  
3	The local char array baseShow should be displayed with the value "Base".
4	The ptr variable value should be the address of the object dobj2.
5	The PC arrow should be at the show() function.  
6	The local char array baseShow should be displayed with the value "Base".
7	The vptr variable value should be the address of the object vdob1.
8	The PC arrow should be at the VDerv1Show() function.  
9	The local char array VDerv1Show should be displayed with the value "VDerv1".
10	The vptr variable value should be the address of the object vdob2.
11	The PC arrow should be at the VDerv2Show() function.  
12	The local char array VDerv2Show should be displayed with the value "VDerv2".
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
