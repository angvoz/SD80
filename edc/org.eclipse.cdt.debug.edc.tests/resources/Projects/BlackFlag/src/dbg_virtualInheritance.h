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

class Base
{
private:
	int data;
public:
	Base();
	void show();
};

//default constructor
Base::Base():data(1){}

void Base::show()
{	//always happens!
	char baseShow[] = "Base"; char a = baseShow[0]; baseShow[0]=a;		
}

class Derv1 : public Base
{
public:
	void show();
};

void Derv1::show()
{	//Never get here!
	char derv1Show[] = "Derv1"; char a = derv1Show[0]; derv1Show[0]=a;		
}

class Derv2 : public Base
{
public:
	void show();	
};

void Derv2::show()
{	//Never get here!
	char derv2Show[] = "Derv2"; char a = derv2Show[0]; derv2Show[0]=a;		
}

class VBase
{
private:
	int data;
public:
	VBase();
	virtual void show();
};

//default constructor
VBase::VBase():data(1) {}

void VBase::show()
{
	char VBaseShow[] = "VBase"; char a = VBaseShow[0]; VBaseShow[0]=a;	
}

class VDerv1 : public VBase
{
public:
	void show();		
};

void VDerv1::show()
{
	char VDerv1Show[] = "VDerv1"; char a = VDerv1Show[0]; VDerv1Show[0]=a;	
}

class VDerv2 : public VBase
{
public:
	void show();
};

void VDerv2::show()
{
	char VDerv2Show[] = "VDerv2"; char a = VDerv2Show[0]; VDerv2Show[0]=a;	
}
