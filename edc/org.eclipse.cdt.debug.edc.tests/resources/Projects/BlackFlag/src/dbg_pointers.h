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

typedef struct {
	UCHAR		unsigned_char; 		//unsigned char
	SCHAR		signed_char; 		//signed char
	char 		c_char;				//char	
	short		short_int;			//short
	USHORT		unsigned_short;		//unsigned short
	SSHORT		signed_short;		//signed short
	ULONG		unsigned_long;		//unsigned long
	SLONG		signed_long;		//signed long
	long		long_int;			//long
#ifdef __MSL_LONGLONG_SUPPORT__	
	ULONGLONG	unsigned_longlong;	//unsigned long long
	SLONGLONG	signed_longlong;	//signed long long
#endif // __MSL_LONGLONG_SUPPORT__ 
	int			i_int;				//int
	UINT		unsigned_int;		//unsigned int
	SINT		signed_int;			//signed int
#ifdef _Floating_Point_Support_
	float		f_float;			//float
	double		d_double;			//double
	long double long_double;		//long double
#endif // _Floating_Point_Support_ 	
} StructType;	

class ClassType {
//used to test pointers to data types in a class
public:
	int			getInt() const;
	void		setInt(int);
	ClassType();
	ClassType(StructType s);
private:	
	UCHAR		unsigned_char; 		//unsigned char
	SCHAR		signed_char; 		//signed char
	char 		c_char;				//char	
	short		short_int;			//short
	USHORT		unsigned_short;		//unsigned short
	SSHORT		signed_short;		//signed short
	ULONG		unsigned_long;		//unsigned long
	SLONG		signed_long;		//signed long
	long		long_int;			//long
#ifdef __MSL_LONGLONG_SUPPORT__	
	ULONGLONG	unsigned_longlong;	//unsigned long long
	SLONGLONG	signed_longlong;	//signed long long
#endif // __MSL_LONGLONG_SUPPORT__ 
	int			i_int;				//int
	UINT		unsigned_int;		//unsigned int
	SINT		signed_int;			//signed int
#ifdef _Floating_Point_Support_
	float		f_float;			//float
	double		d_double;			//double
	long double long_double;		//long double
#endif // _Floating_Point_Support_ 	
	
	UCHAR 		* p_uchar;
	SCHAR 		* p_schar;
	char 		* p_char;
	short  		* p_short;
	USHORT  	* p_ushort;
	SSHORT 		* p_sshort;
	ULONG 		* p_ulong;
	SLONG 		* p_slong;
	long 		* p_long;
#ifdef __MSL_LONGLONG_SUPPORT__		
	ULONGLONG   * p_ulonglong;
	SLONGLONG 	* p_slonglong;
#endif // __MSL_LONGLONG_SUPPORT__ 	
	int 		* p_int;
	UINT  		* p_uint;
	SINT  		* p_sint;
#ifdef _Floating_Point_Support_
	float		* p_float;
	double 		* p_double;
	long double * p_long_double;
#endif // _Floating_Point_Support_ 		
};

ClassType::ClassType()
{
	unsigned_char 	= '1';
	signed_char 	= '2';
	c_char			= '3';
	short_int		= 4;
	unsigned_short	= 5;
	signed_short  	= 6;
	unsigned_long  	= 7;
	signed_long  	= 8;
	long_int		= 9;
#ifdef __MSL_LONGLONG_SUPPORT__	
	unsigned_longlong = 10;
	signed_longlong = 11;
#endif // __MSL_LONGLONG_SUPPORT__ 
	i_int 			= 12;
	unsigned_int 	= 13;
	signed_int		= 14;
#ifdef _Floating_Point_Support_
	f_float 		= 15.5;
	d_double 		= 16.6;
	long_double 	= 17.7;	
#endif // _Floating_Point_Support_ 	
	
	p_uchar 		= &unsigned_char;
	p_schar 		= &signed_char;
	p_char 			= &c_char;
	p_short 		= &short_int;
	p_ushort 		= &unsigned_short;
	p_sshort 		= &signed_short;
	p_ulong			= &unsigned_long;
	p_slong			= &signed_long;
	p_long			= &long_int;
#ifdef __MSL_LONGLONG_SUPPORT__		
	p_ulonglong		= &unsigned_longlong;
	p_slonglong		= &signed_longlong;
#endif // __MSL_LONGLONG_SUPPORT__ 	
	p_int			= &i_int;
	p_uint			= &unsigned_int;
	p_sint			= &signed_int;
#ifdef _Floating_Point_Support_
	p_float			= &f_float;
	p_double		= &d_double;
	p_long_double	= &long_double;
#endif // _Floating_Point_Support_ 	
};

ClassType::ClassType(StructType s)
{
	unsigned_char 	= s.unsigned_char;
	signed_char 	= s.signed_char;
	c_char			= s.c_char;
	short_int		= s.short_int;
	unsigned_short	= s.unsigned_short;
	signed_short  	= s.signed_short;
	unsigned_long  	= s.unsigned_long;
	signed_long  	= s.signed_long;
	long_int		= s.long_int;
#ifdef __MSL_LONGLONG_SUPPORT__	
	unsigned_longlong = s.unsigned_longlong;
	signed_longlong = s.signed_longlong;
#endif // __MSL_LONGLONG_SUPPORT__ 
	i_int 			= s.i_int;
	unsigned_int 	= s.unsigned_int;
	signed_int		= s.signed_int;
#ifdef _Floating_Point_Support_
	f_float 		= s.f_float;
	d_double 		= s.d_double;
	long_double 	= s.long_double;	
#endif // _Floating_Point_Support_ 	
	
	p_uchar 		= &s.unsigned_char;
	p_schar 		= &s.signed_char;
	p_char 			= &s.c_char;
	p_short 		= &s.short_int;
	p_ushort 		= &s.unsigned_short;
	p_sshort 		= &s.signed_short;
	p_ulong			= &s.unsigned_long;
	p_slong			= &s.signed_long;
	p_long			= &s.long_int;
#ifdef __MSL_LONGLONG_SUPPORT__		
	p_ulonglong		= &s.unsigned_longlong;
	p_slonglong		= &s.signed_longlong;
#endif // __MSL_LONGLONG_SUPPORT__ 	
	p_int			= &s.i_int;
	p_uint			= &s.unsigned_int;
	p_sint			= &s.signed_int;
#ifdef _Floating_Point_Support_
	p_float			= &s.f_float;
	p_double		= &s.d_double;
	p_long_double	= &s.long_double;
#endif // _Floating_Point_Support_ 		
};

class Class1 {
//used to test pointers to objects
public:
	Class1(int newValue );
	int			getInt() const;
	void		setInt(int newValue);
private:
	int i_int;			
};	

Class1::Class1(int newValue)
{
	i_int = newValue;
}

int Class1::getInt() const
{
	return i_int;
}

void Class1::setInt(int newValue)
{
	i_int = newValue;
	int dummyVar=0; //dummy line of code
}

class Class2 {
//used to test pointers to objects
public:
	Class2();
	int			getInt() const;
	void		setInt(int newValue);
private:
	int i_int;			
};

Class2::Class2()
{
	i_int=100;
}

int Class2::getInt() const
{
	return i_int;
}

void Class2::setInt(int newValue)
{
	i_int = newValue;
	int dummyVar=0; //dummy line of code
}

class Class3 {
//used to test pointers to objects with data on the heap
public:
	Class3();
	Class3( Class3& p );
	~Class3();
	int	getInt() const;
	void setInt(int newValue);
private:
	int  * i_int;		
};	

Class3::Class3()
{
	i_int = new int(100);
}

Class3::Class3( Class3& p)
{
	i_int = new int;
	*i_int = p.getInt();
}

Class3::~Class3()
{
	delete i_int;
}

int Class3::getInt() const
{
	return *i_int;
}

void Class3::setInt(int newValue)
{
	*i_int = newValue;
	int dummyVar=0; //dummy line of code
}

class Class4 {
//used to test pointers to objects
public:
	Class4( int, Class1* );
	int			getInt() const;
	void		setInt(int newValue);
private:
	int i_int;		
	Class1 *ptr;	
};	

Class4::Class4(int newValue, Class1 *newptr) 
{ 
	i_int = newValue;  
	ptr = newptr;	
}

int Class4::getInt() const
{
	return i_int;
}

void Class4::setInt(int newValue)
{
	i_int = newValue;
}

void fill_struct(StructType* s);

void fill_struct(StructType* s)
{
	s->unsigned_char = '1'; 		
	s->signed_char = '2'; 		
	s->c_char = '2';				
	s->short_int = 3;			
	s->unsigned_short = 3;		
	s->signed_short = 3;		
	s->unsigned_long = 4;		
	s->signed_long = 4;		
	s->long_int = 4;			
#ifdef __MSL_LONGLONG_SUPPORT__	
	s->unsigned_longlong = 4;	
	s->signed_longlong = 5;	
#endif // __MSL_LONGLONG_SUPPORT__ 
	s->i_int = 5;				
	s->unsigned_int = 5;		
	s->signed_int = 5;			
#ifdef _Floating_Point_Support_
	s->f_float = 5;			
	s->d_double = 6;			
	s->long_double = 6;		
#endif // _Floating_Point_Support_ 	
}
