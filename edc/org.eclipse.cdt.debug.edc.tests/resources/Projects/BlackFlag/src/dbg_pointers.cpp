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
#include "dbg_pointers.h"

void dbg_pointers();

void arithmetic(int (*pf)( int, int));
long multiply(int, int);
int add(int, int);
int subtract(int, int);
const Class1 * GetValue(const Class1 * pobj);

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrBasics()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Initializing a pointer with the address of a variable works correctly.
2	Initialize the data value a pointer points by dereferencing another pointer.
3	Initialize a pointer by using the addition of two derefenced pointers.
4 	Void pointers work correctly	

PROCEDURE

1	Step over the initialization of pint1.
2	Step over the initialization of pint2.
3	Step over the initialization of pint3.
4	Step over the initialization of the vp. 
	Right click on the void pointer in the variables view. 
	Select "Cast To Type". 
	Type int* and click "Ok". 
    Expand the void pointer. 
5	Right click on the void pointer in the variables view. 
	Select "Cast To Type". 
	Type char* and click "Ok". 
6	Right click on the void pointer in the variables view. 
	Select "Cast To Type". 
	Type short* and click "Ok".   
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	The dereferenced value for pint1 is 55.
2	The dereferenced value for pint2 is 55.
3	The dereferenced value for pint3 is 110.
4	The dereferenced value of the void pointer vp is 55.
5	The dereferenced value of the void pointer vp is '7'.
6	The dereferenced value of the void pointer vp is 55.
-------------------------------------------------------------------------------------------------------------			
 *************************************************************************************************************/
void ptrBasics()
{
	volatile int int1=55;
	volatile int * pint1=&int1;
	
	volatile int * pint2 = new int;
	volatile int * pint3 = new int;
	
	*pint2 = *pint1;
	*pint3 = *pint1 + *pint2;
	
	volatile void * vp = (int *)&int1;
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToSimpleTypes()

Classes Used:	StructType, ClassType
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Fundamental variable types are displayed correctly when used as member of a class.
2 	Pointers to fundamental data types are displayed correctly when used as members of a class.
3	Pointer to a struct allows access to it's data members.
	
PROCEDURE

1	Step over the instantiation of classObj and expand it in the variable view.
2 	Set a breakpoint at the end of the function and hit Resume. 
	Expand the struct object, structObj,  in the variables view.  
3	Expand the pointer to struct object, pstructObj, in the variables view.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	There should be seventeen pointers to data types in the class object with values 
	from 1 to 17.  Each pointer's value should be the address of the data type. 
	The dereferenced pointer should have the value of the data type. 
2	There should be  seventeen data types struct object with values from 0 to 17. 
3	There should be  seventeen data types struct object with values from 0 to 17. 
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void ptrToSimpleTypes()
{
	//check pointers and void pointer to fundamental data 
	//types as members of a class.
	ClassType classObj;	
	
	//create a struct with fundamental data types as members
	StructType structObj;
	StructType * pstructObj;
	pstructObj = &structObj;
	
	//assign values to data types of struct using pointer to struct
	pstructObj->unsigned_char = '1';
	pstructObj->signed_char = '2'; 	
	pstructObj->c_char = '3';
	pstructObj->short_int = 4; 
	pstructObj->unsigned_short = 5;
	pstructObj->signed_short = 6;
	pstructObj->unsigned_long = 7;
	pstructObj->signed_long = 8;
	pstructObj->long_int=9;
#ifdef __MSL_LONGLONG_SUPPORT__		
	pstructObj->unsigned_longlong=10;
	pstructObj->signed_longlong=11;
#endif // __MSL_LONGLONG_SUPPORT__ 	
	pstructObj->i_int = 12;
	pstructObj->unsigned_int = 13;
	pstructObj->signed_int = 14;
#ifdef _Floating_Point_Support_	
	pstructObj->f_float = 15.5;
	pstructObj->d_double = 16.6;
	pstructObj->long_double = 17.7;
#endif // _Floating_Point_Support_ 	

} //set a breakpoint here

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToArray()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1	A pointer to an array allows access to array members using pointer arithmetic. 	
2	A pointer to an array of objects is displayed correctly.
	
PROCEDURE

1   Set a breakpoint on the closing bracket of the first for loop and press Resume.
	Expand the array on the stack, stackArray. 
	Expand the pointer to the array on the stack, pstackArray. 
2 	Press Resume.
3	Press Resume.
4	Press Resume.
5	Press Resume.
6 	Set a breakpoint on the closing bracket of the third for loop and press Resume.
7	Press Resume.
8	Press Resume.
9	Press Resume.
10	Press Resume.
11  Expand the array of Objects, objArray.
	Set a breakpoint on the closing bracket of the first for loop and press Resume.
12 	Press Resume.
13	Press Resume.
14	Press Resume.
15	Press Resume.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	The dereferenced value of pstackarray = stackarray[0] = 10; 
	The value of the pointer should equal the address of stackarray[0].
2	The dereferenced value of pstackarray = stackarray[1] = 11; 
	The value of the pointer should equal the address of stackarray[1].
3	Press Resume. The dereferenced value of pstackarray = stackarray[2] = 12; 
	The value of the pointer should equal the address of stackarray[2].
4	Press Resume. The dereferenced value of pstackarray = stackarray[3] = 13; 
	The value of the pointer should equal the address of stackarray[3].
5	Press Resume. The dereferenced value of pstackarray = stackarray[4] = 14; 
	The value of the pointer should equal the address of stackarray[4].
6	value = 0.
7	value = 1.
8	value = 2.
9	value = 3.
10 	value = 4.	
11	objArray[0].i_int = 0.
12	objArray[1].i_int = 1.
13	objArray[2].i_int = 2.
14	objArray[3].i_int = 3.
15	objArray[4].i_int = 4.	
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void ptrToArray()
{

	volatile int stackArray[5] = {10,11,12,13,14};	//array of integers ON THE STACK 
	volatile int * pstackArray = stackArray ;	//get the address of the array
	volatile int value;							//a local variable to store values	
	
	//using pointer arithmetic get the values of each element of the array
	for (volatile int i=0; i<5; i++)
	{
		pstackArray = stackArray+i;			//the pointer should increment by 4 (int is 4 bytes)
	}											//dereferenced value should loop from 0-4	
	
	int * pheapArray = new int[5];				//create an array ON THE HEAP
	
	//using pointer arithmetic initialize the array by dereferencing the pointer to the array
	for (volatile int j=0; j<5; j++)
	{
		*(pheapArray+j) = j;			
	}
	
	for (volatile int k=0; k<5; k++)
	{
		value = *(pheapArray++);				//value should loop from 0-4
	}
	
	Class2	objArray[5];						//array of objects ON THE HEAP
	Class2 * pobj;
	
	for (volatile int m=0; m<5; m++)
	{
		pobj = new Class2;
		pobj->setInt(m);
		objArray[m] = *pobj;
		delete pobj;
	}
} //set a breakpoint here

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToObject()

Classes Used:	Class1, Class2, Class3
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Pointer to object on the stack is displayed correctly.
2	Accessor/mutator methods work correctly for object on the stack.
3	Pointer to object on the heap is displayed correctly.
4	Accessor/mutator methods work correctly for object on the heap.
5	Pointer to object on the heap with data member on the heap is displayed correctly.
6	Accessor/mutator methods work correctly for objects on the heap with data member on the heap.
	
PROCEDURE

1	Step to method call pobj1->setInt(). Expand pobj1.
2	Step into the mutator method pobj1->setInt().
3	Step to the end of the function.
4	Step out and step over the accessor method value = pobj1->getInt().
5	Step to method call pobj2->setInt(). Expand pobj2.
6	Step into the mutator method pobj2->setInt(). 
7	Step to the end of the function.
8	Step out and step over the accessor method value = pobj2->getInt().
9	Step to method call pobj3->setInt(). Expand pobj3 and it's data member.
10	Step into the mutator method pobj3->setInt()
11	Step to the end of the function.
12	Step out and step over the accessor method value = pobj3->getInt().
13  Step to the line "ClassType classObj(structt);"
14  Step over line "ClassType classObj(structt);"
15  Step over line "ClassType* pclassObj = new ClassType; "
16  Step over line "ClassType* pcopy = pclassObj"
17  Step over line "pclassObj = &classObj;"
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	The data member value of the pointer, pobj1, should 100.
2	The PC cursor should in the setInt() method.
3	A this pointer should be displayed with the data member value set to 200.
4	The local variable, value = 200.
5	The data member value of the pointer, pobj2, should be 100.
6	The PC cursor should in the setInt() method.
7	A this pointer should be displayed with the member value set to 200.
8	The local variable, value, should be 200.
9	The data value of the pointer member should be 200.
10	The PC cursor should in the setInt() method.
11	A this pointer should be displayed with a member pointer. The data value of the 
	member pointer should be 200.
12	The local variable, value = 200.
13  The structt local variable, structt, should contain 17 data types with values from 1 to 6 
	(one value of 1, two values of 2, ..., five values of 5 and only 2 values of 6.
14  Check that the simple type members from classObj variable contain the same value as structt
 	variable. Also, the pointer members from classObj contain the addresses of the data members.  
15  In pclassObj should be  seventeen data types  with values from 0 to 17. 
	The pointer type members should contain the address of the corresponding simple type members.
16  pcopy variable should contain the same address as pclassObj 
17  pclassObj variable should have the address of object classObj.
    The value pointed by pclassObj is identical with classObj. 
    Check that the object pointed by  pclassObj has the same data members with
    the same values as classObj.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void ptrToObject()
{
	volatile int value;
	Class1 obj1(100);
	
	//check pointer to an object on the stack
	Class1 * pobj1;
	pobj1 = &obj1;
	
	//check accessor/mutators using pointer to an object
	obj1.setInt(200);
	value = pobj1->getInt();

	//check pointer to an object on the heap
	Class2 * pobj2 = new Class2;

	//check accessor/mutators using pointer to an object
	pobj2->setInt(200);
	value = pobj2->getInt();

	//test objects on the heap with data member on the heap
	Class3 * pobj3 = new Class3;
	
	//check accessor/mutators using pointer to an object
	pobj3->setInt(200);
	value = pobj3->getInt();
	
	delete pobj2;
	delete pobj3;
	
	StructType structt;
	fill_struct(&structt);
	ClassType classObj(structt);
	
	ClassType* pclassObj = new ClassType; //ON THE HEAP
	int dummyVar=0; //dummy line of code
	ClassType* pcopy = pclassObj; //needed when release memory
	
	pclassObj = &classObj;
	
	delete pcopy;
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToPtr()

Classes Used:	Class2
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	A pointer to a pointer is displayed correctly. 

NOTE: pointer to pointers are usually used for 2D arrays or transferring
	  an address of a pointer (to a function).
	
PROCEDURE

1	Step to the end of the function.
2	Right click on the void pointer and select "Cast To Type" and type in 'int *'. 
	Right click on the void pointer to pointer and select "Cast To Type" and type in 'int **'. 

-------------------------------------------------------------------------------------------------------------			
CHECKS

1   The value of the pointer pobj1 should contain the address of the object obj1. 
	The dereferenced pointer to pointer should contain the value of obj1 variable i_int.
	The value of the pointer to pointer ppobj1 should contain the pointer address.
	The dereferenced pointer to pointer should contain the value of obj1 variable i_int.
2   The dereferenced void pointer vptr should contain the value of obj1 variable i_int.
	The value of the void pointer to void pointer vvptr should contain the void 
	pointer address. The dereferenced void pointer to void pointer should contain 
	the value of obj1 variable i_int..
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void ptrToPtr()
{
	int i_int = 100;									
	Class2 obj1;								//a class object
	Class2 *pobj1;								//a pointer to a class object
	Class2 **ppobj1;							//a pointer to a pointer to a class object
	void * vptr;
	void ** vvptr;
	
	pobj1=&obj1;
	ppobj1=&pobj1;
	
	vptr = (int *)&i_int;
	vvptr = &vptr;
} //set a breakpoint here

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToClassAsDataMember()

Classes Used:	Class1, Class4
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	A class that contains a pointer to another class as a member
	variable is displayed correctly.
	
PROCEDURE

1	Step to the line "pClass1->setInt(300);". Expand Class4 object and it's pointer data member,ptr. 
2   Expand the Class1 pointer, pClass1, in the variables view.
3.  Step to the end of function and check the value dereferenced of ptr and the dereferenced value of pClass1 
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	The dereferenced value of ptr should be the same as the pClass1 data member value = 100.
2	The derefenced value of ptr and the dereferenced value of pClass1 should be 100 
	and their addresses should be the same.
3   The derefenced value of ptr and the dereferenced value of pClass1 should be 300 
	and their addresses should be the same.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void ptrToClassAsDataMember()
{
	Class1 * pClass1 = new Class1(100);
	Class4 class4Obj(100, pClass1);
	
	pClass1->setInt(300);
	class4Obj.setInt(200);	
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		ptrToFunction()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
A function called multiply exists for the function pointer to point to.
	
OBJECTIVE
	
1 	A function can be called via a pointer to the function which contains the address of the function.
2	A class method can be called via a pointer to the method which contains the address of the method.
	
PROCEDURE

1	Step over the assignment of the function pointer, pf=multiply. 
2 	Step Into the function pointer, product = (*pf)(5,5).
3	Step out of the multiply function and step to the line Class2 obj;
4	Step over the line (obj *pm)(1) and expand obj.
5	Step over the (pobj -> *pm)(2) and expand pobj.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	The PC cursor should be pointing to the beginning of the multiply function in the memory monitor.
2	The PC cursor should be pointing to the beginning of the multiply function.
3	The local variable, product, should now contain the return value, 25, of the multiply function
4	The data member value of obj should now be 1.
5	The value of the dereferenced pointer,pobj, should now be 2.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void ptrToFunction()
{		
	//declaration of function pointer which takes two ints and returns a long
	long (*pf)(int, int);	
			
	//declaration of a class method pointer which takes no parameters and returns an int
	//and points to setInt method of Class2								
	void (Class2::*pm)(int)	= &Class2::setInt;	
				
	volatile long product;			//local variable to hold the return of function multiply
	
	pf=multiply;					//pf now points to address of function multiply
	product =(*pf)(5,5);			//result should be 25

	//access a class function using a function pointer
	Class2 obj;
	Class2 *pobj = new Class2;
	
	(obj.*pm)(1);					//member variable of obj should now be 1
	(pobj->*pm)(2);					//member variable of pobj should now be 2
}

long multiply(int num1, int num2)
{
	return num1*num2;
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		arraysOfPtrs()

Classes Used:	Class2
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	To be able to display and debug an array of pointers.
	
PROCEDURE

1 	Set a breakpoint at the end of the function and run to it.
	Expand the array pointers and pointer to Class2. 			    
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	Each of the elements of the array should contain a unique pointer to a Class2 object with the 
	the value of each Class2 object equal to its position in the pointer array 
	(e.g. if it is position 0, the value will be 0, and so on).
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void arrayOfPtrs()
{
	Class2 * parray[5];
	Class2 * pClass2;
	
	for (volatile int i=0; i<5; i++)
	{
		pClass2 = new Class2;
		pClass2->setInt(i);
		parray[i]= pClass2;
	}
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		passAPtr()

Classes Used:	Class1
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	A pointer(to an object) can be passed as a parameter and the class methods can be accessed 
	using the pointer.
	
PROCEDURE

1	Step to the line GetValue(&obj) and expand the Class1 object. 
2	Step into the GetValue function and expand the pointer that was passed in.
3	Step out of the GetValue function and step to the end of the function.  
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	Class1 object's data member should have the value of 200.
2	The pointer should contain the address of the Class1 object and it's data member value should be 200.
3	The local variable, value, should be 200.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void passAPtr()
{
	volatile int value;
	
	//Instantiate an object and change it's data member value.
	Class1 obj(100);
	obj.setInt(200);
	
	//pass a pointer to a const object to the function GetValue
	GetValue( &obj);

	//check that the value passed from the function is 200.
	value = obj.getInt();
}

const Class1 * GetValue(const Class1 * pobj)
{
	//Now let's get the value and return it.
	pobj->getInt();
	return pobj;
}

/*************************************************************************************************************
File:			dbg_pointers.cpp	
Function:		passPtrToFunction()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS

A function, arithmetic, is created that is passed a function pointer. Two other functions, add & subtract, 
are created to be used as the functions that are pointed to.
	
OBJECTIVE
	
1 	A function that is passed as a parameter via a function pointer can be debugged and executed properly.
	
PROCEDURE

1	Step to the first call of the arithmetic function and step into.   
2	Step to the call to the function pointer, pf, and step into.
3	Step to the end of the arithmetic function.
4	Step out twice and step to the second call of the arithmetic function and step into.
5	Step to the call to the function pointer, pf, and step into.
6	Step to the end of the arithmetic function.
7	Step out twice.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1 	The PC cursor should be pointing to the beginning of the arithmetic function.
2	The PC cursor should be pointing to the beginning of the add function.
3	The local variable, sum, should have the value 6 which is the result of the add function.
4	The PC cursor should be pointing to the beginning of the arithmetic function.
5	The PC cursor should be pointing to the beginning of the subtract function.
6	The local variable, sum, should have the value 2 which is the result of the subtract function.
7	The PC cursor should be pointing to the call of the arithmetic function or the closing bracket.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/	
void passPtrToFunction()
{	
	//execute the functions add and subtract within arithmetic
	arithmetic(&add);
	arithmetic(&subtract);
}

void arithmetic(int (*pf)( int, int))
//pf is a pointer to a function which returns an int and takes two ints
{	
	volatile int sum = pf(4,2);					//pf points to function passed to arithmetic
}

int add(int num1, int num2)
{
	return num1+num2;
}

int subtract(int num1, int num2)
{
	return num1-num2;
}

void dbg_pointers()
{
	ptrBasics();
	ptrToSimpleTypes();
	ptrToArray();
	ptrToObject();	
	ptrToPtr();
	ptrToClassAsDataMember();
	ptrToFunction();	
	arrayOfPtrs();
	passAPtr();
	passPtrToFunction();
}
