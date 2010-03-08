//============================================================================
// Name        : SimpleCpp.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include "myheader.h"

Foo globalFoo;

#include "mycode.h"

#include "Templates.h"

List<char> gstring;

int doit() {
	myfunc();
	
	makelist();
	
	gstring.add('7');
	return globalFoo.x + gstring.length();
}


#ifdef __SYMBIAN32__
void E32Main() {
	doit();
}
#else
int main() {
	return doit();
}
#endif
