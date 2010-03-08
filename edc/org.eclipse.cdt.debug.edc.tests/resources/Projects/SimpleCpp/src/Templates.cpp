/*
 * Templates.cpp
 *
 *  Created on: Jan 28, 2010
 *      Author: eswartz
 */
#include "Templates.h"

static float runner;

void makelist() {
	List<float>* numbers = new List<float>();
	numbers->add(10);
	float zero = 0.;
	numbers->add(0/zero);
	numbers->add(1/zero);
	
	float sum = (*numbers)[0] + (*numbers)[1] + (*numbers)[2];
	runner += sum;
}
