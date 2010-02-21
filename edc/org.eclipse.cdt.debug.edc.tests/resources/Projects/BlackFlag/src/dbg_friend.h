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

class beta;                                     //forward declaration

class alpha
{
    private:
        int data;
    public:
    	alpha();       
        friend int frifunc(alpha, beta);        //friend function
};

alpha::alpha()									//default constructor
{
	data = 3;
}
    
class beta
{
    private:
       int data;
    public:
       beta();
       friend int frifunc(alpha, beta);			//friend function
};

beta::beta()									//default constructor
{
	data = 7;
}

int frifunc( alpha a, beta b )
{
 	return( a.data + b.data );
} 
    
    
class first
{
    private:
        int data1;
    public:
    	first();        
        friend class second;
};

first::first()									//default constructor
{
	data1 = 99;
}
    
class second
{
    private:    
    	int i;
    	int j;
    	int k;
    public:
    	second();
    	void func1(first* a);
    	void func2(first* a);
};

second::second()								//default constructor
{
	i = 0;
	j = 0;
	k = 0;
}

void second::func1(first* a)
{
	i = a->data1;
}        
        
void second::func2(first* a)
{
	j = a->data1;
    k = 101;
    a->data1 = k;
}

class USDollar
{
	friend USDollar operator+(USDollar&, USDollar&);	//friend operator overload
	friend USDollar operator+ (USDollar& s1, int dol);	//friend operator overload
	friend USDollar operator+ (int cen, USDollar& s1);	//friend operator overload
	friend USDollar& operator++(USDollar&);				//friend operator overload	
	
	public:
		USDollar(unsigned int d, unsigned int c);
		
	private:
		unsigned int dollars;
		unsigned int cents;
};

USDollar::USDollar(unsigned int d, unsigned int c)	//Constructor
{
	dollars = d;
	cents = c;
	while (cents >= 100)
	{
		dollars++;
		cents -= 100;
	}
}

//Overloading the '+' operator to add dollars to dollars and 
//cents to cents.  The constructor is then called for object d
//and if cents >= 100, then add 1 to dollars and subtract 100
//from cents

USDollar operator+ (USDollar& s1, USDollar& s2)
{
	unsigned int cents = s1.cents + s2.cents;
	unsigned int dollars = s1.dollars + s2.dollars;
	USDollar d(dollars, cents);
	return d;
}

//Overloading the '+' operator to allow C++ data type to be added
//to a class object.  Adds dollars to dollars.  Note: You cannot 
//give the '+' operator more than two arguments, hence cents is not
//included in this overloaded operator and must be done separately.
//This also accounts for (object, data type) order.

USDollar operator+ (USDollar& s1, int dol)
{
	unsigned int cents = s1.cents;
	unsigned int dollars = s1.dollars + dol;
	USDollar d(dollars, cents);
	return d;
}

//Overloading the '+' operator to allow a class object to be added
//to C++ data type.  Adds cents to cents.  Note: You cannot 
//give the '+' operator more than two arguments, hence dollars is not
//included in this overloaded operator and must be done separately.
//This also accounts for (data type, object) order.


USDollar operator+ (int cen, USDollar& s1)
{
	unsigned int cents = cen + s1.cents;
	unsigned int dollars = s1.dollars;
	USDollar d(dollars, cents);
	return d;
}

//Overloading the '++' operator to increment cents only. 
//If cents >= 100 then add 1 to dollars and subtract 100
//from cents.

USDollar& operator++ (USDollar& s)
{
	s.cents++;
	if (s.cents >= 100)
		{
			s.cents -= 100;
			s.dollars++;
		}
	return s;
}

