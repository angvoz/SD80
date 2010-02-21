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

class Base1
{
	public:	
		int x;
		
		Base1();		
		void show1();		
		void commonBaseFunctionName();		
};

//Default Constructor
Base1::Base1()
{
	x = 0;
}

void Base1::show1()
{
	char Base1Show[] = "Base1"; char a = Base1Show[0]; Base1Show[0]=a;		
}

void Base1::commonBaseFunctionName()
{
	char FromBase1[] = "From Base1"; char a = FromBase1[0]; FromBase1[0]=a;	 
}

class Base2
{
	public:
		void show2();		
		void commonBaseFunctionName();
};

void Base2::show2()
{
	char Base2Show[] = "Base2";	 char a = Base2Show[0]; Base2Show[0] = a;	
}

void Base2::commonBaseFunctionName()
{
	char FromBase2[] = "From Base2"; char a = FromBase2[0]; FromBase2[0] = a;	
}

class Derv1 : public Base1 , public Base2
{
	public:
		void show3();		
		virtual void commonBaseFunctionName();
};

void Derv1::show3()
{
	char Derv1Show[] = "Derv1"; char a = Derv1Show[0]; Derv1Show[0]=a;
}

void Derv1::commonBaseFunctionName()
{
	Base1::commonBaseFunctionName();	//step into
	Base2::commonBaseFunctionName();	//step into
}

class Derv2 : public Derv1
{
	public:
		void show4();		
		virtual void commonBaseFunctionName();
};

void Derv2::show4()
{
	char Derv2Show[] = "Derv2"; char a = Derv2Show[0]; Derv2Show[0]=a;
}

void Derv2::commonBaseFunctionName()
{
	Base1::commonBaseFunctionName();	//step into
	Base2::commonBaseFunctionName();	//step into
}

class Derv3 : public Base1
{
	public:
		void show5();
};

void Derv3::show5()
{
	char Derv3Show[] = "Derv3"; char a = Derv3Show[0]; Derv3Show[0]=a;
}	

class Derv4 : public Base1
{
	public:
		void show6();
};

void Derv4::show6()
{
	char Derv4Show[] = "Derv4"; char a = Derv4Show[0]; Derv4Show[0]=a;
}

class MI : public Derv3, public Derv4
{
	public:
	
		MI();
		MI(int i);
		int foo();
				
//	private:
		int i;

};

//Default Constructor
MI::MI()
{
	i = 10;
}

//function foo
int MI::foo()
{	i++; return i;}									
