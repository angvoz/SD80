/*
 * myheader.h
 *
 *  Created on: Jan 27, 2010
 *      Author: eswartz
 */

#ifndef MYCODE_H_
#define MYCODE_H_

static void myfunc() {
	int x = 122;
	Foo foo;
	foo.x = x;
	foo.next = &foo;
}

#endif /* MYCODE_H_ */
