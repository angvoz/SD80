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
#ifndef AGENTUTILS_H
#define AGENTUTILS_H

#include <string>

#ifdef WIN32
#include "StdAfx.h"
#endif

/*
 * Various utilities.
 */
class AgentUtils {
public:
	template<typename T> static std::string ToString(const T& value);

	static std::string IntToString(int value);
	static std::string BoolToString(bool value);
	static std::string IntToHexString(unsigned long value);
	static unsigned long HexStringToInt(const std::string& str);

	static std::string makeUTF8String(const std::wstring& wstring);
	static std::string makeUTF8String(const std::string& wstring);
	static std::string GetFileNameFromPath(const std::string& path);

#ifdef WIN32
	static std::string makeString(LPTSTR stringBuffer);
	static std::wstring makeWideString(const char* stringbuffer);
#endif

	static void SwapBytes(char * buf, int size);

private:
	AgentUtils(void);
	~AgentUtils(void);


};
#endif
