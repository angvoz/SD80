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

#ifndef RUNCONTROLSERVICE_H_
#define RUNCONTROLSERVICE_H_

#include "TCFService.h"

/*
 * Constants copied from TCF IRunControl.java
 */
enum TCFResumeMode {
	RM_RESUME = 0,

	/**
	 * Step over a single instruction.
	 * If the instruction is a function call then don't stop until the function returns.
	 */
	RM_STEP_OVER = 1,

	/**
	 * Step a single instruction.
	 * If the instruction is a function call then stop at first instruction of the function.
	 */
	RM_STEP_INTO = 2,

	/**
	 * Step over a single source code line.
	 * If the line contains a function call then don't stop until the function returns.
	 */
	RM_STEP_OVER_LINE = 3,

	/**
	 * Step a single source code line.
	 * If the line contains a function call then stop at first line of the function.
	 */
	RM_STEP_INTO_LINE = 4,

	/**
	 * Run until control returns from current function.
	 */
	RM_STEP_OUT = 5,

	/**
	 * Start running backwards.
	 * Execution will continue until suspended by command or breakpoint.
	 */
	RM_REVERSE_RESUME = 6,

	/**
	 * Reverse of RM_STEP_OVER - run backwards over a single instruction.
	 * If the instruction is a function call then don't stop until get out of the function.
	 */
	RM_REVERSE_STEP_OVER = 7,

	/**
	 * Reverse of RM_STEP_INTO.
	 * This effectively "un-executes" the previous instruction
	 */
	RM_REVERSE_STEP_INTO = 8,

	/**
	 * Reverse of RM_STEP_OVER_LINE.
	 * Resume backward execution of given context until control reaches an instruction that belongs
	 * to a different source line.
	 * If the line contains a function call then don't stop until get out of the function.
	 * Error is returned if line number information not available.
	 */
	RM_REVERSE_STEP_OVER_LINE = 9,

	/**
	 * Reverse of RM_STEP_INTO_LINE,
	 * Resume backward execution of given context until control reaches an instruction that belongs
	 * to a different line of source code.
	 * If a function is called, stop at the beginning of the last line of the function code.
	 * Error is returned if line number information not available.
	 */
	RM_REVERSE_STEP_INTO_LINE = 10,

	/**
	 * Reverse of RM_STEP_OUT.
	 * Resume backward execution of the given context until control reaches the point where the current function was called.
	 */
	RM_REVERSE_STEP_OUT = 11,

	/**
	 * Step over instructions until PC is outside the specified range.
	 * If any function call within the range is considered to be in range.
	 */
	RM_STEP_OVER_RANGE = 12,

	/**
	 * Step instruction until PC is outside the specified range for any reason.
	 */
	RM_STEP_INTO_RANGE = 13,

	/**
	 * Reverse of RM_STEP_OVER_RANGE
	 */
	RM_REVERSE_STEP_OVER_RANGE = 14,

	/**
	 * Reverse of RM_STEP_INTO_RANGE
	 */
	RM_REVERSE_STEP_INTO_RANGE = 15
};

struct Channel;
struct Protocol;

class RunControlService: public TCFService {
public:
	RunControlService(Protocol * proto);
	~RunControlService(void);

	const char* GetName();

	static void command_get_context(const char * token, Channel * c);
	static void command_get_children(const char * token, Channel * c);
	static void command_get_state(const char * token, Channel * c);
	static void command_resume(const char * token, Channel * c);
	static void command_suspend(const char * token, Channel * c);
	static void command_terminate(const char * token, Channel * c);
};

#endif
