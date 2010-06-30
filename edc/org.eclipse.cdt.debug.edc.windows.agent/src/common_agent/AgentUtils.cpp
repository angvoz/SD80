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
#ifdef WIN32
#include "StdAfx.h"
#endif

#include "AgentUtils.h"

#include <assert.h>
#include <stdio.h>
#include <sstream>
#include <iostream>

AgentUtils::AgentUtils(void) {
}

AgentUtils::~AgentUtils(void) {
}

template<typename T>
std::string AgentUtils::ToString(const T& value) {
	std::stringstream stringstream;
	stringstream << value;
	return stringstream.str();
}

std::string AgentUtils::IntToString(int value) {
	std::stringstream stringstream;
	stringstream << value;
	return stringstream.str();
}

std::string AgentUtils::BoolToString(bool value) {
	static std::string str_true = "true";
	static std::string str_false = "false";

	return value ? str_true : str_false;
}

std::string AgentUtils::IntToHexString(unsigned long value) {
	char buf[32];
	snprintf(buf, sizeof(buf), "%08lx", value);
	return buf;
}

unsigned long AgentUtils::HexStringToInt(const std::string& str) {
	unsigned long result;
	sscanf(str.c_str(), "%lx", &result);
	return result;
}

std::string AgentUtils::makeUTF8String(const std::string& string) {
	return string;
}

std::string AgentUtils::makeUTF8String(const std::wstring& wstring) {
	int bufferLength = 1024;
	char* buffer = new char[bufferLength];
#ifdef WIN32
	int result = WideCharToMultiByte(CP_UTF8, 0, wstring.c_str(), -1, buffer,
			bufferLength, NULL, NULL);
#else
	assert(false);
#endif
	return buffer;
}

std::string AgentUtils::GetFileNameFromPath(const std::string& path) {
	std::string fileName;
	int lastSlash = path.find_last_of("\\");
	if (lastSlash > 0)
		fileName = path.substr(lastSlash + 1);
	else
	    fileName = path;
	return fileName;
}

#ifdef WIN32

std::string AgentUtils::makeString(LPTSTR stringBuffer) {
#ifdef  UNICODE
	int bufferLength = 1024;
	char* buffer = new char[bufferLength];
	int result = WideCharToMultiByte(CP_UTF8, 0,stringBuffer, -1, buffer, bufferLength, NULL, NULL);
	return buffer;
#else
	return stringBuffer;
#endif
}

std::wstring AgentUtils::makeWideString(const char* stringbuffer) {
	int wideSize = MultiByteToWideChar(CP_UTF8, 0, stringbuffer, strlen(
			stringbuffer) + 1, NULL, 0);
	wchar_t* wideChars = new wchar_t[wideSize];
	MultiByteToWideChar(CP_UTF8, 0, stringbuffer, strlen(stringbuffer) + 1,
			wideChars, wideSize);
	std::wstring result = wideChars;
	delete[] wideChars;
	return result;
}

#endif

void AgentUtils::SwapBytes(char * buf, int size) {
    int i, j, n;
    char * p = buf;
    n = size >> 1;
    for (i = 0, j = size - 1; i < n; i++, j--) {
        char x = p[i];
        p[i] = p[j];
        p[j] = x;
    }
}
