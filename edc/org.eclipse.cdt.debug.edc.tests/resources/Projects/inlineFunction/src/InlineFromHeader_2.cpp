#include "inline_func.h"

int inline_from_header_2() {
	int b = 10;

	b++;
	b = inline_func2(b);
	b++;

	return b;
}

