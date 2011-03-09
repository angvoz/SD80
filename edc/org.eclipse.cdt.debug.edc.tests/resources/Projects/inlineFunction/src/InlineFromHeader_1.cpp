#include "inline_func.h"

int inline_from_header_1() {
	int a = 10;

	a++;
	a = inline_func1(a);
	a++;

	return a;
}

