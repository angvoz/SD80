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
#include "dbg_multipleInheritance.h"

void dbg_multipleInheritance();

//*************************************************************************************************************
//	File:			dbg_multipleInheritance.cpp	
//	Function:		show1(), show2(), show3()
//
//	Classes Used:	Base1, Base2, Derv1
//	Libs Used:		n/a
//-------------------------------------------------------------------------------------------------------------			
//	PRECONDITIONS/ASSUMPTIONS
//	
//	OBJECTIVE
//	
//	1 	All public functions declared in the base
//		class can be accessed from a derived class.
//	   	
//	PROCEDURE
//
//	1	Step to the function call pdv1 -> show1().
//	2	Step into pdv1 -> show1()
//	3	Step to the end of Base1::show1().
//	4	Step out of Base1::show1() and step into pdv1 -> show2()
//	5	Step to the end of Base2::show2().
//	6   Step out of Base2::show2() and step into pdv1 -> show3()
//	7	Step to the end of Derv1::show3().
//	8 	Step out of Derv1::show3().
//-------------------------------------------------------------------------------------------------------------			
//	CHECKS
//
//	1	The pdv1 variable value should be the address of the object dv1.
//	2	The PC arrow should be at the Base1::show1() function.  
//	3	The local variable Base1Show should be displayed 
//		with the value "Base1".
//	4	The PC arrow should be at the Base2::show2() function.    
//	5   The local variable Base2Show should be displayed 
//		with the value "Base2".
//	6	The PC arrow should be at the Derv1::show3() function.  
//	7	The local variable Derv1Show should be displayed 
//		with the value "Derv1".
//	8. 	The PC arrow should be at the closing bracket of 
//		multipleInheritance().
//-------------------------------------------------------------------------------------------------------------			
//*************************************************************************************************************

void multipleInheritance()
{
	Derv1 dobj1;					// Object of derived class 1	
	Derv1* pdv1;					// Pointer to derived class
		
	pdv1 = &dobj1;					// Pointer now contains address of dv1
	pdv1 -> show1();				// Execute show1(). Step into
	pdv1 -> show2();				// Execute show2(). Step into
	pdv1 -> show3();				// Execute show3(). Step into
}

//*************************************************************************************************************
//	File:			dbg_multipleInheritance.cpp	
//	Function:		show3(), show4(), commonBaseFunctionName()
//
//	Classes Used:	Base1, Base2, Derv1, Derv2	
//	Libs Used:		n/a
//-------------------------------------------------------------------------------------------------------------			
//	PRECONDITIONS/ASSUMPTIONS
//	
//	OBJECTIVE
//	
//	1	Chained public functions declared in the base 
//		classes can be accessed from one or more derived 
//		classes.
//	   	
//	PROCEDURE
//
//	1	Step to the function call pchainDv1 -> commonBaseFunctionName();
//	2	Step into pchainDv1 -> commonBaseFunctionName(); 
//	3	Step into Base1::commonBaseFunctionName(); 
//	4	Step to the end.
//	5	Step out.
//	6	Step into Base2::commonBaseFunctionName(); 
//	7	Step to the end.
//	8	Step out of Base2::commonBaseFunctionName() and
//		Derv1::commonBaseFunctionName() and step to the 
//		function call pchainDv2 -> show3();
//	9	Step into pchainDv2 -> show3(); 
//	10	Step to the end of the Derv1::show3() function.
//	11	Step out of show3() and step into pchainDv2 -> show4(); 
//	12	Step to the end of the show4() function.
//	13	Step out of Derv2::show4() and step into pchainDv2 -> commonBaseFunctionName(); 
//	14	Step into Base1::commonBaseFunctionName(); 
//	15	Step to the end.
//	16	Step out.
//	17	Step into Base2::commonBaseFunctionName(); 
//	18	Step to the end.
//	19 	Step out of Base2::commonBaseFunctionName() and
//		Derv2::commonBaseFunctionName()
//-------------------------------------------------------------------------------------------------------------			
//	CHECKS
//
//	1	The pchainDv1 variable value should be the address of 
//		the object chainDv1.
//	2	The PC arrow should be at Derv1::commonBaseFunctionName.  
//	3	The PC arrow should be at Base1::commonBaseFunctionName().  
//	4	The local variable FromBase1 should be displayed 
//		with the value "From Base1".
//	5	The PC arrow should be at function call Base2::commonBaseFunctionName.  
//	6	The PC arrow should be at Base2::commonBaseFunctionName().  
//	7	The local variable FromBase2 should be displayed 
//		with the value "From Base2".
//	8	The pchainDv2 variable value should be the address of 
//		the object chainDv2.
//	9	The PC arrow should be at Derv1::show3().  
//	10	The local variable Derv1Show should be displayed 
//		with the value "Derv1".
//	11	The PC arrow should be at Derv2::show4().  
//	12	The local variable Derv2Show should be displayed 
//		with the value "Derv2".
//	13	The PC arrow should be at Derv2::commonBaseFunctionName.  
//	14	The PC arrow should be at Base1::commonBaseFunctionName.  
//	15	The local variable FromBase1 should be displayed 
//		with the value "From Base1".
//	16	The PC arrow should be at function call Base2::commonBaseFunctionName.    
//	17	The PC arrow should be at Base2::commonBaseFunctionName().  
//	18	The local variable FromBase2 should be displayed 
//		with the value "From Base2".
//	19 	The PC arrow should be at the closing bracket of 
//		chainMultipleInheritance().
//----------------------------------------------------------------------------------------------			

void chainMultipleInheritance()
{
	Derv1 chainDv1;							// Object of derived class 1
	Derv1* pchainDv1;						// Pointer to derived class
	
	pchainDv1 = &chainDv1;					// Pointer now contains address of chainDv1
	pchainDv1 -> commonBaseFunctionName();	// Execute commonBaseFunctionName(). Step into
										
	Derv2 chainDv2;							// Object of derived class 2
	Derv2* pchainDv2;						// Pointer to derived class
	
	pchainDv2 = &chainDv2;					// Pointer now contains address of chainDv2
	pchainDv2 -> show3();					// Execute show3(). Step into
	pchainDv2 -> show4();					// Execute show4(). Step into
	pchainDv2 -> commonBaseFunctionName();	// Execute commonBaseFunctionName(). Step into
}

//*************************************************************************************************************
//	File:			dbg_multipleInheritance.cpp	
//	Function:		show1(), show5(), show6(),foo()
//
//	Classes Used:	Derv3, Derv4, MI	
//	Libs Used:		n/a
//-------------------------------------------------------------------------------------------------------------			
//	PRECONDITIONS/ASSUMPTIONS
//	
//	OBJECTIVE
//
//	1	Test derived class objects can access base class
//		correctly in multiple inheritance with replicated 
//		base classes (a multiple inherited class consisting 
//		of two or more classes which derive from a common base
//		class).
//	   	
//	PROCEDURE
//
//	1	Step to the assignment pMI -> Derv3 :: x = 9.
//	2	Step over pMI -> Derv3 :: x = 9;
//	3	Step over pMI -> Derv4 :: x = 10;
//	4	Step into pMI -> Derv3 :: show1(); 
//	5	Step to the end of show1().
//	6	Step out of Derv3::show1() and step into pMI -> Derv4::show1(); 
//	7	Step to the end of show1().
//	8   Step out of Derv4::show1() and step into pMI -> show5(); 
//	9	Step to the end of show5().
//	10	Step out of Derv3::show5() and step into pMI -> show6(); 
//	11	Step to the end of show6().
//	12	Step out of Derv4::show6() and step into pMI -> foo(); 
//	13	Step out foo() and over the initialization of the local variable i.
//
//-------------------------------------------------------------------------------------------------------------			
//	CHECKS
//
//	1	The pMI variable value should be the address of 
//		the object repBase.
//	2	repBase::Derv3::Base1.x should display the value 9.  
//		Derv3::Base1.x should display the value 9.
//	3	repBase::Derv4::Base1.x should display the value 10.  
//		Derv4::Base1.x should display the value 10.
//	4	The PC arrow should be at the show1() function.  
//	5	The local variable Base1Show should be displayed 
//		with the value "Base1".
//	6	The PC arrow should be at the show1() function.  
//	7	The local variable Base1Show should be displayed 
//		with the value "Base1".
//	8	The PC arrow should be at the show5() function.  
//	9	The local variable Derv3Show should be displayed 
//		with the value "Derv3".
//	10	The PC arrow should be at the show6() function.  
//	11	The local variable Derv4Show should be displayed 
//		with the value "Derv4".
//	12	The PC arrow should be at the foo() function.  
//	13	The variable i should be displayed with the value 11.
//-------------------------------------------------------------------------------------------------------------			
//*************************************************************************************************************

void repBaseMultipleInheritance()
{
	MI repBase;						// Object of derived class MI
	MI* pMI;						// Pointer to derived class
	
	pMI = &repBase;					// Pointer now contains address of repBase
	pMI -> Derv3 :: x = 9;			// Step over. repBase::Derv3::Base1.x == 9.  Derv3::Base1.x == 9.
	pMI -> Derv4 :: x = 10;			// Step over. repBase::Derv4::Base1.x == 10.  Derv4::Base1.x == 10.
	pMI -> Derv3 :: show1();		// Execute show1(). Step into
	pMI -> Derv4 :: show1();		// Execute show1(). Step into
	pMI -> show5();					// Execute show5(). Step into
	pMI -> show6();					// Execute show6(). Step into
	int i=pMI -> foo();				// Execute foo().   Step into
}

void dbg_multipleInheritance()
{
	multipleInheritance();			// Step into
	chainMultipleInheritance();		// Step into
	repBaseMultipleInheritance();	// Step into
}
