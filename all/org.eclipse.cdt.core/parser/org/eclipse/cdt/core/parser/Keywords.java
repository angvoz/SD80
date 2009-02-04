/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Keywords {

	public static final String CAST = "cast"; //$NON-NLS-1$
	public static final String ALIGNOF = "alignof"; //$NON-NLS-1$
	public static final String TYPEOF = "typeof"; //$NON-NLS-1$
	
	public static final String _BOOL = "_Bool"; //$NON-NLS-1$
	public static final String _COMPLEX = "_Complex"; //$NON-NLS-1$
	public static final String _IMAGINARY = "_Imaginary"; //$NON-NLS-1$
	public static final String AND = "and"; //$NON-NLS-1$
	public static final String AND_EQ = "and_eq"; //$NON-NLS-1$
	public static final String ASM = "asm"; //$NON-NLS-1$
	public static final String AUTO = "auto"; //$NON-NLS-1$
	public static final String BITAND = "bitand"; //$NON-NLS-1$
	public static final String BITOR = "bitor"; //$NON-NLS-1$
	public static final String BOOL = "bool"; //$NON-NLS-1$
	public static final String BREAK = "break"; //$NON-NLS-1$
	public static final String CASE = "case"; //$NON-NLS-1$
	public static final String CATCH = "catch"; //$NON-NLS-1$
	public static final String CHAR = "char"; //$NON-NLS-1$
	public static final String CLASS = "class"; //$NON-NLS-1$
	public static final String COMPL = "compl"; //$NON-NLS-1$
	public static final String CONST = "const"; //$NON-NLS-1$
	public static final String CONST_CAST = "const_cast"; //$NON-NLS-1$
	public static final String CONTINUE = "continue"; //$NON-NLS-1$
	public static final String DEFAULT = "default"; //$NON-NLS-1$
	public static final String DELETE = "delete"; //$NON-NLS-1$
	public static final String DO = "do"; //$NON-NLS-1$
	public static final String DOUBLE = "double"; //$NON-NLS-1$
	public static final String DYNAMIC_CAST = "dynamic_cast"; //$NON-NLS-1$
	public static final String ELSE = "else"; //$NON-NLS-1$
	public static final String ENUM = "enum"; //$NON-NLS-1$
	public static final String EXPLICIT = "explicit"; //$NON-NLS-1$
	public static final String EXPORT = "export"; //$NON-NLS-1$
	public static final String EXTERN = "extern"; //$NON-NLS-1$
	public static final String FALSE = "false"; //$NON-NLS-1$
	public static final String FLOAT = "float"; //$NON-NLS-1$
	public static final String FOR = "for"; //$NON-NLS-1$
	public static final String FRIEND = "friend"; //$NON-NLS-1$
	public static final String GOTO = "goto"; //$NON-NLS-1$
	public static final String IF = "if"; //$NON-NLS-1$
	public static final String INLINE = "inline"; //$NON-NLS-1$
	public static final String INT = "int"; //$NON-NLS-1$
	public static final String LONG = "long"; //$NON-NLS-1$
	public static final String LONG_LONG = "long long"; //$NON-NLS-1$
	public static final String MUTABLE = "mutable"; //$NON-NLS-1$
	public static final String NAMESPACE = "namespace"; //$NON-NLS-1$
	public static final String NEW = "new"; //$NON-NLS-1$
	public static final String NOT = "not"; //$NON-NLS-1$
	public static final String NOT_EQ = "not_eq"; //$NON-NLS-1$
	public static final String OPERATOR = "operator"; //$NON-NLS-1$
	public static final String OR = "or"; //$NON-NLS-1$
	public static final String OR_EQ = "or_eq"; //$NON-NLS-1$
	public static final String PRIVATE = "private"; //$NON-NLS-1$
	public static final String PROTECTED = "protected"; //$NON-NLS-1$
	public static final String PUBLIC = "public"; //$NON-NLS-1$
	public static final String REGISTER = "register"; //$NON-NLS-1$
	public static final String REINTERPRET_CAST = "reinterpret_cast"; //$NON-NLS-1$
	public static final String RESTRICT = "restrict"; //$NON-NLS-1$
	public static final String RETURN = "return"; //$NON-NLS-1$
	public static final String SHORT = "short"; //$NON-NLS-1$
	public static final String SIGNED = "signed"; //$NON-NLS-1$
	public static final String SIZEOF = "sizeof"; //$NON-NLS-1$
	public static final String STATIC = "static"; //$NON-NLS-1$
	public static final String STATIC_CAST = "static_cast"; //$NON-NLS-1$
	public static final String STRUCT = "struct"; //$NON-NLS-1$
	public static final String SWITCH = "switch"; //$NON-NLS-1$
	public static final String TEMPLATE = "template"; //$NON-NLS-1$
	public static final String THIS = "this"; //$NON-NLS-1$
	public static final String THROW = "throw"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String TRY = "try"; //$NON-NLS-1$
	public static final String TYPEDEF = "typedef"; //$NON-NLS-1$
	public static final String TYPEID = "typeid"; //$NON-NLS-1$
	public static final String TYPENAME = "typename"; //$NON-NLS-1$
	public static final String UNION = "union"; //$NON-NLS-1$
	public static final String UNSIGNED = "unsigned"; //$NON-NLS-1$
	public static final String USING = "using"; //$NON-NLS-1$
	public static final String VIRTUAL = "virtual"; //$NON-NLS-1$
	public static final String VOID = "void"; //$NON-NLS-1$
	public static final String VOLATILE = "volatile"; //$NON-NLS-1$
	public static final String WCHAR_T = "wchar_t"; //$NON-NLS-1$
	public static final String WHILE = "while"; //$NON-NLS-1$
	public static final String XOR = "xor"; //$NON-NLS-1$
	public static final String XOR_EQ = "xor_eq"; //$NON-NLS-1$

	
	public static final char[] c_BOOL = "_Bool".toCharArray(); //$NON-NLS-1$
	public static final char[] c_COMPLEX = "_Complex".toCharArray(); //$NON-NLS-1$
	public static final char[] c_IMAGINARY = "_Imaginary".toCharArray(); //$NON-NLS-1$
	public static final char[] cAND = "and".toCharArray(); //$NON-NLS-1$
	public static final char[] cAND_EQ = "and_eq".toCharArray(); //$NON-NLS-1$
	public static final char[] cASM = "asm".toCharArray(); //$NON-NLS-1$
	public static final char[] cAUTO = "auto".toCharArray(); //$NON-NLS-1$
	public static final char[] cBITAND = "bitand".toCharArray(); //$NON-NLS-1$
	public static final char[] cBITOR = "bitor".toCharArray(); //$NON-NLS-1$
	public static final char[] cBOOL = "bool".toCharArray(); //$NON-NLS-1$
	public static final char[] cBREAK = "break".toCharArray(); //$NON-NLS-1$
	public static final char[] cCASE = "case".toCharArray(); //$NON-NLS-1$
	public static final char[] cCATCH = "catch".toCharArray(); //$NON-NLS-1$
	public static final char[] cCHAR = "char".toCharArray(); //$NON-NLS-1$
	public static final char[] cCLASS = "class".toCharArray(); //$NON-NLS-1$
	public static final char[] cCOMPL = "compl".toCharArray(); //$NON-NLS-1$
	public static final char[] cCONST = "const".toCharArray(); //$NON-NLS-1$
	public static final char[] cCONST_CAST = "const_cast".toCharArray(); //$NON-NLS-1$
	public static final char[] cCONTINUE = "continue".toCharArray(); //$NON-NLS-1$
	public static final char[] cDEFAULT = "default".toCharArray(); //$NON-NLS-1$
	public static final char[] cDELETE = "delete".toCharArray(); //$NON-NLS-1$
	public static final char[] cDO = "do".toCharArray(); //$NON-NLS-1$
	public static final char[] cDOUBLE = "double".toCharArray(); //$NON-NLS-1$
	public static final char[] cDYNAMIC_CAST = "dynamic_cast".toCharArray(); //$NON-NLS-1$
	public static final char[] cELSE = "else".toCharArray(); //$NON-NLS-1$
	public static final char[] cENUM = "enum".toCharArray(); //$NON-NLS-1$
	public static final char[] cEXPLICIT = "explicit".toCharArray(); //$NON-NLS-1$
	public static final char[] cEXPORT = "export".toCharArray(); //$NON-NLS-1$
	public static final char[] cEXTERN = "extern".toCharArray(); //$NON-NLS-1$
	public static final char[] cFALSE = "false".toCharArray(); //$NON-NLS-1$
	public static final char[] cFLOAT = "float".toCharArray(); //$NON-NLS-1$
	public static final char[] cFOR = "for".toCharArray(); //$NON-NLS-1$
	public static final char[] cFRIEND = "friend".toCharArray(); //$NON-NLS-1$
	public static final char[] cGOTO = "goto".toCharArray(); //$NON-NLS-1$
	public static final char[] cIF = "if".toCharArray(); //$NON-NLS-1$
	public static final char[] cINLINE = "inline".toCharArray(); //$NON-NLS-1$
	public static final char[] cINT = "int".toCharArray(); //$NON-NLS-1$
	public static final char[] cLONG = "long".toCharArray(); //$NON-NLS-1$
	public static final char[] cMUTABLE = "mutable".toCharArray(); //$NON-NLS-1$
	public static final char[] cNAMESPACE = "namespace".toCharArray(); //$NON-NLS-1$
	public static final char[] cNEW = "new".toCharArray(); //$NON-NLS-1$
	public static final char[] cNOT = "not".toCharArray(); //$NON-NLS-1$
	public static final char[] cNOT_EQ = "not_eq".toCharArray(); //$NON-NLS-1$
	public static final char[] cOPERATOR = "operator".toCharArray(); //$NON-NLS-1$
	public static final char[] cOR = "or".toCharArray(); //$NON-NLS-1$
	public static final char[] cOR_EQ = "or_eq".toCharArray(); //$NON-NLS-1$
	public static final char[] cPRIVATE = "private".toCharArray(); //$NON-NLS-1$
	public static final char[] cPROTECTED = "protected".toCharArray(); //$NON-NLS-1$
	public static final char[] cPUBLIC = "public".toCharArray(); //$NON-NLS-1$
	public static final char[] cREGISTER = "register".toCharArray(); //$NON-NLS-1$
	public static final char[] cREINTERPRET_CAST = "reinterpret_cast".toCharArray(); //$NON-NLS-1$
	public static final char[] cRESTRICT = "restrict".toCharArray(); //$NON-NLS-1$
	public static final char[] cRETURN = "return".toCharArray(); //$NON-NLS-1$
	public static final char[] cSHORT = "short".toCharArray(); //$NON-NLS-1$
	public static final char[] cSIGNED = "signed".toCharArray(); //$NON-NLS-1$
	public static final char[] cSIZEOF = "sizeof".toCharArray(); //$NON-NLS-1$
	public static final char[] cSTATIC = "static".toCharArray(); //$NON-NLS-1$
	public static final char[] cSTATIC_CAST = "static_cast".toCharArray(); //$NON-NLS-1$
	public static final char[] cSTRUCT = "struct".toCharArray(); //$NON-NLS-1$
	public static final char[] cSWITCH = "switch".toCharArray(); //$NON-NLS-1$
	public static final char[] cTEMPLATE = "template".toCharArray(); //$NON-NLS-1$
	public static final char[] cTHIS = "this".toCharArray(); //$NON-NLS-1$
	public static final char[] cTHROW = "throw".toCharArray(); //$NON-NLS-1$
	public static final char[] cTRUE = "true".toCharArray(); //$NON-NLS-1$
	public static final char[] cTRY = "try".toCharArray(); //$NON-NLS-1$
	public static final char[] cTYPEDEF = "typedef".toCharArray(); //$NON-NLS-1$
	public static final char[] cTYPEID = "typeid".toCharArray(); //$NON-NLS-1$
	public static final char[] cTYPENAME = "typename".toCharArray(); //$NON-NLS-1$
	public static final char[] cUNION = "union".toCharArray(); //$NON-NLS-1$
	public static final char[] cUNSIGNED = "unsigned".toCharArray(); //$NON-NLS-1$
	public static final char[] cUSING = "using".toCharArray(); //$NON-NLS-1$
	public static final char[] cVIRTUAL = "virtual".toCharArray(); //$NON-NLS-1$
	public static final char[] cVOID = "void".toCharArray(); //$NON-NLS-1$
	public static final char[] cVOLATILE = "volatile".toCharArray(); //$NON-NLS-1$
	public static final char[] cWCHAR_T = "wchar_t".toCharArray(); //$NON-NLS-1$
	public static final char[] cWHILE = "while".toCharArray(); //$NON-NLS-1$
	public static final char[] cXOR = "xor".toCharArray(); //$NON-NLS-1$
	public static final char[] cXOR_EQ = "xor_eq".toCharArray(); //$NON-NLS-1$
		
	public static final char[] cpCOLONCOLON = "::".toCharArray(); //$NON-NLS-1$
	public static final char[] cpCOLON = ":".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSEMI = ";".toCharArray(); //$NON-NLS-1$
	public static final char[] cpCOMMA =	",".toCharArray(); //$NON-NLS-1$
	public static final char[] cpQUESTION = "?".toCharArray(); //$NON-NLS-1$
	public static final char[] cpLPAREN  = "(".toCharArray(); //$NON-NLS-1$
	public static final char[] cpRPAREN  = ")".toCharArray(); //$NON-NLS-1$
	public static final char[] cpLBRACKET = "[".toCharArray(); //$NON-NLS-1$
	public static final char[] cpRBRACKET = "]".toCharArray(); //$NON-NLS-1$
	public static final char[] cpLBRACE = "{".toCharArray(); //$NON-NLS-1$
	public static final char[] cpRBRACE = "}".toCharArray(); //$NON-NLS-1$
	public static final char[] cpPLUSASSIGN =	"+=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpINCR = 	"++".toCharArray(); //$NON-NLS-1$
	public static final char[] cpPLUS = 	"+".toCharArray(); //$NON-NLS-1$
	public static final char[] cpMINUSASSIGN =	"-=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpDECR = 	"--".toCharArray(); //$NON-NLS-1$
	public static final char[] cpARROWSTAR =	"->*".toCharArray(); //$NON-NLS-1$
	public static final char[] cpARROW = 	"->".toCharArray(); //$NON-NLS-1$
	public static final char[] cpMINUS = 	"-".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSTARASSIGN =	"*=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSTAR = 	"*".toCharArray(); //$NON-NLS-1$
	public static final char[] cpMODASSIGN =	"%=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpMOD = 	"%".toCharArray(); //$NON-NLS-1$
	public static final char[] cpXORASSIGN =	"^=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpXOR = 	"^".toCharArray(); //$NON-NLS-1$
	public static final char[] cpAMPERASSIGN =	"&=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpAND = 	"&&".toCharArray(); //$NON-NLS-1$
	public static final char[] cpAMPER =	"&".toCharArray(); //$NON-NLS-1$
	public static final char[] cpBITORASSIGN =	"|=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpOR = 	"||".toCharArray(); //$NON-NLS-1$
	public static final char[] cpBITOR =	"|".toCharArray(); //$NON-NLS-1$
	public static final char[] cpCOMPL =	"~".toCharArray(); //$NON-NLS-1$
	public static final char[] cpNOTEQUAL =	"!=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpNOT = 	"!".toCharArray(); //$NON-NLS-1$
	public static final char[] cpEQUAL =	"==".toCharArray(); //$NON-NLS-1$
	public static final char[] cpASSIGN ="=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSHIFTL =	"<<".toCharArray(); //$NON-NLS-1$
	public static final char[] cpLTEQUAL =	"<=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpLT = 	"<".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSHIFTRASSIGN =	">>=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSHIFTR = 	">>".toCharArray(); //$NON-NLS-1$
	public static final char[] cpGTEQUAL = 	">=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpGT = 	">".toCharArray(); //$NON-NLS-1$
	public static final char[] cpSHIFTLASSIGN =	"<<=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpELLIPSIS = 	"...".toCharArray(); //$NON-NLS-1$
	public static final char[] cpDOTSTAR = 	".*".toCharArray(); //$NON-NLS-1$
	public static final char[] cpDOT = 	".".toCharArray(); //$NON-NLS-1$
	public static final char[] cpDIVASSIGN =	"/=".toCharArray(); //$NON-NLS-1$
	public static final char[] cpDIV = 	"/".toCharArray(); //$NON-NLS-1$
	public static final char[] cpPOUND = "#".toCharArray(); //$NON-NLS-1$
	public static final char[] cpPOUNDPOUND = "##".toCharArray(); //$NON-NLS-1$
	
	// gcc extensions
	public static final char[] cpMIN = "<?".toCharArray(); //$NON-NLS-1$
	public static final char[] cpMAX = ">?".toCharArray(); //$NON-NLS-1$
	
	// preprocessor keywords
	public static final char[] cIFDEF = "ifdef".toCharArray(); //$NON-NLS-1$
	public static final char[] cIFNDEF = "ifndef".toCharArray(); //$NON-NLS-1$
	public static final char[] cELIF = "elif".toCharArray(); //$NON-NLS-1$
	public static final char[] cENDIF = "endif".toCharArray(); //$NON-NLS-1$
	public static final char[] cINCLUDE = "include".toCharArray(); //$NON-NLS-1$
	public static final char[] cDEFINE = "define".toCharArray(); //$NON-NLS-1$
	public static final char[] cUNDEF = "undef".toCharArray(); //$NON-NLS-1$
	public static final char[] cERROR = "error".toCharArray(); //$NON-NLS-1$
	public static final char[] cPRAGMA = "pragma".toCharArray(); //$NON-NLS-1$
	public static final char[] cLINE = "line".toCharArray(); //$NON-NLS-1$
	public static final char[] cDEFINED= "defined".toCharArray(); //$NON-NLS-1$
	
	public static final char[] cVA_ARGS= "__VA_ARGS__".toCharArray(); //$NON-NLS-1$
	
	// preprocessor extensions (supported by GCC)
	public static final char[] cINCLUDE_NEXT = "include_next".toCharArray(); //$NON-NLS-1$
	public static final char[] cIMPORT = "import".toCharArray(); //$NON-NLS-1$
	public static final char[] cIDENT = "ident".toCharArray(); //$NON-NLS-1$
	public static final char[] cSCCS = "sccs".toCharArray(); //$NON-NLS-1$
	public static final char[] cWARNING = "warning".toCharArray(); //$NON-NLS-1$
	public static final char[] cASSERT = "assert".toCharArray(); //$NON-NLS-1$
	public static final char[] cUNASSERT = "unassert".toCharArray(); //$NON-NLS-1$
	
	public static void addKeywordsC(CharArrayIntMap kw) {
		addCommon(kw);
		addC(kw);
	}

	public static void addKeywordsCpp(CharArrayIntMap kw) {
		addCommon(kw);
		addCpp(kw);
	}


	private static void addCommon(CharArrayIntMap words) {
        words.put(Keywords.cAUTO, IToken.t_auto);
        words.put(Keywords.cBREAK, IToken.t_break);
        words.put(Keywords.cCASE, IToken.t_case); 
        words.put(Keywords.cCHAR, IToken.t_char); 
        words.put(Keywords.cCONST, IToken.t_const); 
        words.put(Keywords.cCONTINUE, IToken.t_continue); 
        words.put(Keywords.cDEFAULT, IToken.t_default); 
        words.put(Keywords.cDO, IToken.t_do); 
        words.put(Keywords.cDOUBLE, IToken.t_double); 
        words.put(Keywords.cELSE, IToken.t_else); 
        words.put(Keywords.cENUM, IToken.t_enum); 
        words.put(Keywords.cEXTERN, IToken.t_extern); 
        words.put(Keywords.cFLOAT, IToken.t_float); 
        words.put(Keywords.cFOR, IToken.t_for); 
        words.put(Keywords.cGOTO, IToken.t_goto); 
        words.put(Keywords.cIF, IToken.t_if); 
        words.put(Keywords.cINLINE, IToken.t_inline); 
        words.put(Keywords.cINT, IToken.t_int); 
        words.put(Keywords.cLONG, IToken.t_long); 
        words.put(Keywords.cREGISTER, IToken.t_register); 
        words.put(Keywords.cRETURN, IToken.t_return); 
        words.put(Keywords.cSHORT, IToken.t_short); 
        words.put(Keywords.cSIGNED, IToken.t_signed); 
        words.put(Keywords.cSIZEOF, IToken.t_sizeof); 
        words.put(Keywords.cSTATIC, IToken.t_static); 
        words.put(Keywords.cSTRUCT, IToken.t_struct); 
        words.put(Keywords.cSWITCH, IToken.t_switch); 
        words.put(Keywords.cTYPEDEF, IToken.t_typedef); 
        words.put(Keywords.cUNION, IToken.t_union); 
        words.put(Keywords.cUNSIGNED, IToken.t_unsigned); 
        words.put(Keywords.cVOID, IToken.t_void); 
        words.put(Keywords.cVOLATILE, IToken.t_volatile); 
        words.put(Keywords.cWHILE, IToken.t_while); 
        words.put(Keywords.cASM, IToken.t_asm); 
	}
	
        // ANSI C keywords
	private static void addC(CharArrayIntMap ckeywords) {
        ckeywords.put(Keywords.cRESTRICT, IToken.t_restrict); 
        ckeywords.put(Keywords.c_BOOL, IToken.t__Bool); 
        ckeywords.put(Keywords.c_COMPLEX, IToken.t__Complex); 
        ckeywords.put(Keywords.c_IMAGINARY, IToken.t__Imaginary); 
	}
	
	private static void addCpp(CharArrayIntMap cppkeywords) {
        cppkeywords.put(Keywords.cBOOL, IToken.t_bool); 
        cppkeywords.put(Keywords.cCATCH, IToken.t_catch); 
        cppkeywords.put(Keywords.cCLASS, IToken.t_class); 
        cppkeywords.put(Keywords.cCONST_CAST, IToken.t_const_cast); 
        cppkeywords.put(Keywords.cDELETE, IToken.t_delete); 
        cppkeywords.put(Keywords.cDYNAMIC_CAST, IToken.t_dynamic_cast); 
        cppkeywords.put(Keywords.cEXPLICIT, IToken.t_explicit); 
        cppkeywords.put(Keywords.cEXPORT, IToken.t_export); 
        cppkeywords.put(Keywords.cFALSE, IToken.t_false); 
        cppkeywords.put(Keywords.cFRIEND, IToken.t_friend); 
        cppkeywords.put(Keywords.cMUTABLE, IToken.t_mutable); 
        cppkeywords.put(Keywords.cNAMESPACE, IToken.t_namespace); 
        cppkeywords.put(Keywords.cNEW, IToken.t_new); 
        cppkeywords.put(Keywords.cOPERATOR, IToken.t_operator); 
        cppkeywords.put(Keywords.cPRIVATE, IToken.t_private); 
        cppkeywords.put(Keywords.cPROTECTED, IToken.t_protected); 
        cppkeywords.put(Keywords.cPUBLIC, IToken.t_public); 
        cppkeywords.put(Keywords.cREINTERPRET_CAST, IToken.t_reinterpret_cast); 
        cppkeywords.put(Keywords.cSTATIC_CAST, IToken.t_static_cast); 
        cppkeywords.put(Keywords.cTEMPLATE, IToken.t_template); 
        cppkeywords.put(Keywords.cTHIS, IToken.t_this); 
        cppkeywords.put(Keywords.cTHROW, IToken.t_throw); 
        cppkeywords.put(Keywords.cTRUE, IToken.t_true); 
        cppkeywords.put(Keywords.cTRY, IToken.t_try); 
        cppkeywords.put(Keywords.cTYPEID, IToken.t_typeid); 
        cppkeywords.put(Keywords.cTYPENAME, IToken.t_typename); 
        cppkeywords.put(Keywords.cUSING, IToken.t_using); 
        cppkeywords.put(Keywords.cVIRTUAL, IToken.t_virtual); 
        cppkeywords.put(Keywords.cWCHAR_T, IToken.t_wchar_t); 

        // C++ operator alternative
        cppkeywords.put(Keywords.cAND, IToken.tAND); 
        cppkeywords.put(Keywords.cAND_EQ, IToken.tAMPERASSIGN); 
        cppkeywords.put(Keywords.cBITAND, IToken.tAMPER); 
        cppkeywords.put(Keywords.cBITOR, IToken.tBITOR); 
        cppkeywords.put(Keywords.cCOMPL, IToken.tBITCOMPLEMENT); 
        cppkeywords.put(Keywords.cNOT, IToken.tNOT); 
        cppkeywords.put(Keywords.cNOT_EQ, IToken.tNOTEQUAL); 
        cppkeywords.put(Keywords.cOR, IToken.tOR); 
        cppkeywords.put(Keywords.cOR_EQ, IToken.tBITORASSIGN); 
        cppkeywords.put(Keywords.cXOR, IToken.tXOR); 
        cppkeywords.put(Keywords.cXOR_EQ, IToken.tXORASSIGN); 
	}
	
	public static void addKeywordsPreprocessor(CharArrayIntMap ppKeywords) {
        // Preprocessor keywords
        ppKeywords.put(Keywords.cIF, IPreprocessorDirective.ppIf); 
        ppKeywords.put(Keywords.cIFDEF, IPreprocessorDirective.ppIfdef); 
        ppKeywords.put(Keywords.cIFNDEF, IPreprocessorDirective.ppIfndef); 
        ppKeywords.put(Keywords.cELIF, IPreprocessorDirective.ppElif); 
        ppKeywords.put(Keywords.cELSE, IPreprocessorDirective.ppElse); 
        ppKeywords.put(Keywords.cENDIF, IPreprocessorDirective.ppEndif); 
        ppKeywords.put(Keywords.cINCLUDE, IPreprocessorDirective.ppInclude); 
        ppKeywords.put(Keywords.cDEFINE, IPreprocessorDirective.ppDefine); 
        ppKeywords.put(Keywords.cUNDEF, IPreprocessorDirective.ppUndef); 
        ppKeywords.put(Keywords.cERROR, IPreprocessorDirective.ppError); 
        ppKeywords.put(Keywords.cPRAGMA, IPreprocessorDirective.ppPragma); 
        ppKeywords.put(Keywords.cLINE, IPreprocessorDirective.ppIgnore);
    }
}
