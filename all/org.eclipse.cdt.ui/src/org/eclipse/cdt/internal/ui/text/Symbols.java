/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

/**
 * Symbols for the heuristic C/C++ scanner.
 */
public interface Symbols {
	int TokenEOF= -1;
	int TokenLBRACE= 1;
	int TokenRBRACE= 2;
	int TokenLBRACKET= 3;
	int TokenRBRACKET= 4;
	int TokenLPAREN= 5;
	int TokenRPAREN= 6;
	int TokenSEMICOLON= 7;
	int TokenOTHER= 8;
	int TokenCOLON= 9;
	int TokenQUESTIONMARK= 10;
	int TokenCOMMA= 11;
	int TokenEQUAL= 12;
	int TokenLESSTHAN= 13;
	int TokenGREATERTHAN= 14;
	int TokenDOT= 15;
	int TokenMINUS= 16;
	int TokenIF= 109;
	int TokenDO= 1010;
	int TokenFOR= 1011;
	int TokenTRY= 1012;
	int TokenCASE= 1013;
	int TokenELSE= 1014;
	int TokenBREAK= 1015;
	int TokenCATCH= 1016;
	int TokenWHILE= 1017;
	int TokenRETURN= 1018;
	int TokenSTATIC= 1019;
	int TokenSWITCH= 1020;
	int TokenGOTO= 1021;
	int TokenDEFAULT= 1022;
	int TokenPRIVATE= 1023;
	int TokenPROTECTED= 1024;
	int TokenPUBLIC= 1025;
	int TokenNEW= 1026;
	int TokenDELETE= 1027; 
	int TokenCLASS= 1028;
	int TokenSTRUCT= 1029;
	int TokenENUM= 1030;
	int TokenIDENT= 2000;
}
