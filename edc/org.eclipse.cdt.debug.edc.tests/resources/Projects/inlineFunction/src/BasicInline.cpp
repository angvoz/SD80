/*
 * BasicInline.cpp
 * 
 * Basic inline function.
 */

// for GCC.
inline int inline_func(int i) __attribute__((always_inline));

inline int inline_func(int i) {
	// During debug, set a breakpoint on this line. 
	i++;	// set a breakpoint here should top both invokation point.
	i++;
	return i;
}

//  Local Functions

int basic_inline() {
	int a = 10, b = 0;

	// Set a breakpoint in inline_func(), then step over code below.
	// You should see debugger stops in the inline_func().
	// If the disassembly view works fine, you should see inlined code
	// in the two calling points.
	//
	a = inline_func(a);
	b++;
	b++;
	b = inline_func(b);
	b++;
	a++;

	return a+b;
}

