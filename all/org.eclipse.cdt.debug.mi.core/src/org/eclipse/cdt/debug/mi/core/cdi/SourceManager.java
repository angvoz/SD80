/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBDerivedType;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Instruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.MixedInstruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ArrayType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.BoolType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.CharType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DerivedType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DoubleType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.EnumType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FloatType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FunctionType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IntType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongLongType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.PointerType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ReferenceType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ShortType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.StructType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.Type;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.VoidType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.WCharType;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataDisassemble;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowDirectories;
import org.eclipse.cdt.debug.mi.core.command.MIPType;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MIDataDisassembleInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIPTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;


/**
 */
public class SourceManager extends SessionObject implements ICDISourceManager {

	boolean autoupdate;
	GDBTypeParser gdbTypeParser;

	public SourceManager(Session session) {
		super(session);
		autoupdate = false;
		gdbTypeParser = new GDBTypeParser();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addSourcePaths(String[])
	 */
	public void addSourcePaths(String[] dirs) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(dirs);
		try {
			mi.postCommand(dir);
			dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getSourcePaths()
	 */
	public String[] getSourcePaths() throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowDirectories dir = factory.createMIGDBShowDirectories();
		try {
			mi.postCommand(dir);
			MIGDBShowDirectoriesInfo info = dir.getMIGDBShowDirectoriesInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(String, int, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(session.getCurrentTarget(), asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(String, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		return getInstructions(filename, linenum, -1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(long, long)
	 */
	public ICDIInstruction[] getInstructions(long start, long end) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x";
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(session.getCurrentTarget(), asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(String, int, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(session.getCurrentTarget(), srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(String, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		return getMixedInstructions(filename, linenum, -1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(long, long)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(long start, long end) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x";
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(session.getCurrentTarget(), srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#update()
	 */
	public void update() throws CDIException {
	}


	public Type getType(ICDITarget target, String name) throws CDIException {
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();

		// Parse the string.
		GDBType gdbType = gdbTypeParser.parse(typename);
		Type type = null;

		for (Type aType = null; gdbType != null; type = aType) {
			if (gdbType instanceof GDBDerivedType) {
				switch(gdbType.getType()) {
					case GDBType.ARRAY:
						int d = ((GDBDerivedType)gdbType).getDimension();
						aType = new ArrayType(target, typename, d);
					break;
					case GDBType.FUNCTION:
						aType = new FunctionType(target, typename);
					break;
					case GDBType.POINTER:
						aType = new PointerType(target, typename);
					break;
					case GDBType.REFERENCE:
						aType = new ReferenceType(target, typename);
					break;
				}
				gdbType = ((GDBDerivedType)gdbType).getChild();
			} else {
				aType = toCDIType(target, gdbType.toString());
				gdbType = null;
			}
			if (type instanceof DerivedType) {
				((DerivedType)type).setComponentType(aType);
			}
		}

		if (type != null) {
			return type;
		}
		throw new CDIException("Unknown type");
	}
	
	Type toCDIType(ICDITarget target, String name) throws CDIException {
		// Check the derived types and agregate types
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();

		// Check the primitives.
		if (typename.equals("char")) {
			return new CharType(target, typename);
		} else if (typename.equals("wchar_t")) {
			return new WCharType(target, typename);
		} else if (typename.equals("short")) {
			return new ShortType(target, typename);
		} else if (typename.equals("int")) {
			return new IntType(target, typename);
		} else if (typename.equals("long")) {
			return new LongType(target, typename);
		} else if (typename.equals("unsigned")) {
			return new IntType(target, typename, true);
		} else if (typename.equals("signed")) {
			return new IntType(target, typename);
		} else if (typename.equals("bool")) {
			return new BoolType(target, typename);
		} else if (typename.equals("_Bool")) {
			return new BoolType(target, typename);
		} else if (typename.equals("float")) {
			return new FloatType(target, typename);
		} else if (typename.equals("double")) {
			return new DoubleType(target, typename);
		} else if (typename.equals("void")) {
			return new VoidType(target, typename);
		} else if (typename.equals("enum")) {
			return new EnumType(target, typename);
		} else if (typename.equals("union")) {
			return new StructType(target, typename);
		} else if (typename.equals("struct")) {
			return new StructType(target, typename);
		} else if (typename.equals("class")) {
			return new StructType(target, typename);
		}

		StringTokenizer st = new StringTokenizer(typename);
		int count = st.countTokens();

		if (count == 2) {
			String first = st.nextToken();
			String second = st.nextToken();

			// ISOC allows permutations:
			// "signed int" and "int signed" are equivalent
			boolean isUnsigned =  (first.equals("unsigned") || second.equals("unsigned"));
			boolean isSigned =    (first.equals("signed") || second.equals("signed"));
			boolean isChar =      (first.equals("char") || second.equals("char"));
			boolean isInt =       (first.equals("int") || second.equals("int"));
			boolean isLong =      (first.equals("long") || second.equals("long"));
			boolean isShort =     (first.equals("short") || second.equals("short"));
			boolean isLongLong =  (first.equals("long") && second.equals("long"));
			
			boolean isDouble =    (first.equals("double") || second.equals("double"));
			boolean isFloat =     (first.equals("float") || second.equals("float"));
			boolean isComplex =   (first.equals("complex") || second.equals("complex") ||
			                       first.equals("_Complex") || second.equals("_Complex"));
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary"));

			boolean isStruct =     first.equals("struct");
			boolean isClass =      first.equals("class");
			boolean isUnion =      first.equals("union");
			boolean isEnum =       first.equals("enum");

			if (isChar && (isSigned || isUnsigned)) {
				return new CharType(target, typename, isUnsigned);
			} else if (isShort && (isSigned || isUnsigned)) {
				return new ShortType(target, typename, isUnsigned);
			} else if (isInt && (isSigned || isUnsigned)) {
				return new IntType(target, typename, isUnsigned);
			} else if (isLong && (isInt || isSigned || isUnsigned)) {
				return new LongType(target, typename, isUnsigned);
			} else if (isLongLong) {
				return new LongLongType(target, typename);
			} else if (isDouble && (isLong || isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			} else if (isFloat && (isComplex || isImaginery)) {
				return new FloatType(target, typename, isComplex, isImaginery);
			} else if (isStruct) {
				return new StructType(target, typename);
			} else if (isClass) {
				return new StructType(target, typename);
			} else if (isUnion) {
				return new StructType(target, typename);
			} else if (isEnum) {
				return new EnumType(target, typename);
			}
		} else if (count == 3) {
			// ISOC allows permutation. replace short by: long or short
			// "unsigned short int", "unsigned int short"
			// "short unsigned int". "short int unsigned"
			// "int unsinged short". "int short unsigned"
			//
			// "unsigned long long", "long long unsigned"
			// "signed long long", "long long signed"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();

			boolean isSigned =    (first.equals("signed") || second.equals("signed") || third.equals("signed"));
			boolean unSigned =    (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned"));
			boolean isInt =       (first.equals("int") || second.equals("int") || third.equals("int"));
			boolean isLong =      (first.equals("long") || second.equals("long") || third.equals("long"));
			boolean isShort =     (first.equals("short") || second.equals("short") || third.equals("short"));
			boolean isLongLong =  (first.equals("long") && second.equals("long")) ||
			                       (second.equals("long") && third.equals("long"));
			boolean isDouble =    (first.equals("double") || second.equals("double") || third.equals("double"));
			boolean isComplex =   (first.equals("complex") || second.equals("complex") || third.equals("complex") ||
			                       first.equals("_Complex") || second.equals("_Complex") || third.equals("_Complex"));
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary") || third.equals("_Imaginary"));


			if (isShort && isInt && (isSigned || unSigned)) {
				return new ShortType(target, typename, unSigned);
			} else if (isLong && isInt && (isSigned || unSigned)) {
				return new LongType(target, typename, unSigned);
			} else if (isLongLong && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			} else if (isDouble && isLong && (isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			}
		} else if (count == 4) {
			// ISOC allows permutation:
			// "unsigned long long int", "unsigned int long long"
			// "long long unsigned int". "long long int unsigned"
			// "int unsigned long long". "int long long unsigned"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();
			String fourth = st.nextToken();

			boolean unSigned = (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned") || fourth.equals("unsigned"));
			boolean isSigned = (first.equals("signed") || second.equals("signed") || third.equals("signed") || fourth.equals("signed"));
			boolean isInt =    (first.equals("int") || second.equals("int") || third.equals("int") || fourth.equals("int"));
			boolean isLongLong =   (first.equals("long") && second.equals("long"))
				|| (second.equals("long") && third.equals("long"))
				|| (third.equals("long") && fourth.equals("long"));

			if (isLongLong && isInt && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			}
		}
		throw new CDIException("Unknown type");
	}

	public String getDetailTypeName(String typename) throws CDIException {
		try {
			Session session = (Session)getSession();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIPType ptype = factory.createMIPType(typename);
			mi.postCommand(ptype);
			MIPTypeInfo info = ptype.getMIPtypeInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			return info.getType();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
