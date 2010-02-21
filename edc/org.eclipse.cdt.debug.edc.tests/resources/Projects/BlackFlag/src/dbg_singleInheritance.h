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

class Person {
public:
	Person();
	Person(int a, int h, int w);
	Person(int h, int w);
	
	int		Getage() const;
	int		Getheight() const;
	int		Getweight() const;

	void	Setage(int);
	void	Setheight(int);
	void	Setweight(int);
	
private:

	int 	_age;
	int 	_height;
	int 	_weight;
};

//default constructor
Person::Person():_age(20),_height(72),_weight(150){}

//overloaded constructor
Person::Person (int d, int m, int y) {

	Setage(d);
	Setheight(m);
	Setweight(y);
}

//overloaded constructor
Person::Person (int d, int m) {

	Setage(d);
	Setheight(m);
	_weight = 150;
}

int Person::Getage() const
{
	return _age;
}

int Person::Getheight() const
{
	return _height;
}

int Person::Getweight() const
{
	return _weight;
}

void Person::Setweight(int newValue)
{
	_weight = newValue;
}

void Person::Setheight(int newValue)
{
	_height = newValue;
}

void Person::Setage(int newValue)
{
	_age = newValue;
}

class Employee : public Person
{
public:

	Employee ();
	Employee (double);
	Employee (int, int, int, double);
	
	double	Getsalary () const;
	void	Setsalary (double newvalue);
	
private:
	
	double _salary;
	
};

Employee::Employee():Person() { Setsalary(0); }

Employee::Employee (double s):Person()
{	
	Setsalary(s);
}

Employee::Employee (int a, int b, int c, double s):Person(a,b,c)
{	
	Setsalary(s);
}

void Employee::Setsalary(double newvalue)
{

	_salary = newvalue;	
}

double Employee::Getsalary () const 
{

	//Verify this will expand to Person (base class)
	//and _salary.  Person will expand to show
	//it's private member variables.

	return _salary;
}

