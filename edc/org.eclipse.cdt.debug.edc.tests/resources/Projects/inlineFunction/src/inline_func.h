// Two inline functions, each is invoked in different cpp file.
inline int inline_func1(int i) __attribute__((always_inline)); // tell GCC to inline
inline int inline_func1(int i) {
	// During debug, set a breakpoint on this line. 
	i++;
	i++;
	return i;
}

inline int inline_func2(int m) __attribute__((always_inline)); // tell GCC to inline
inline int inline_func2(int m) {
	// During debug, set a breakpoint on this line. 
	m--;
	m--;
	return m;
}

