package org.eclipse.cdt.core.parser.c99.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPSpecTest;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.preprocessoradapter.GPPAdaptedLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.runtime.CoreException;

public class CPPAdapterSpecTests extends AST2CPPSpecTest {

	public CPPAdapterSpecTests() { }
	public CPPAdapterSpecTests(String name) { super(name); }

	private static class NameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		
		public int visit( IASTName name ){
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
	}
	
		
	@Override
	protected IASTTranslationUnit parse(String code, ParserLanguage lang,
			boolean checkBindings, int expectedProblemBindings)
			throws ParserException {
		
		System.out.println("my parser");
		
        ScannerInfo scannerInfo = new ScannerInfo();
        CodeReader codeReader = new CodeReader(code.toCharArray());
        
        IASTTranslationUnit tu;
		try {
			tu = GPPAdaptedLanguage.getDefault().getASTTranslationUnit(codeReader, scannerInfo, null, null, new NullLogService());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		// resolve all bindings
		if (checkBindings) {
			if ( lang == ParserLanguage.CPP ) {
				CPPNameResolver res = new CPPNameResolver();
		        tu.accept( res );
				if (res.numProblemBindings != expectedProblemBindings )
					throw new ParserException("Expected " + expectedProblemBindings + " problems, encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (lang == ParserLanguage.C ) {
				CNameResolver res = new CNameResolver();
		        tu.accept( res );
				if (res.numProblemBindings != expectedProblemBindings )
					throw new ParserException("Expected " + expectedProblemBindings + " problems, encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

        //if( parser2.encounteredError() && expectNoProblems )
       //     throw new ParserException( "FAILURE"); //$NON-NLS-1$
         
        if( lang == ParserLanguage.C)
        {
			if (CVisitor.getProblems(tu).length != 0) {
				throw new ParserException (" CVisitor has AST Problems " ); //$NON-NLS-1$
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException (" C TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }
        else if ( lang == ParserLanguage.CPP)
        {
			if (CPPVisitor.getProblems(tu).length != 0) {
				throw new ParserException (" CPPVisitor has AST Problems " ); //$NON-NLS-1$
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException (" CPP TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }
        
        return tu;
	}
	
	static protected class CNameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
	
	static protected class CPPNameResolver extends CPPASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
}
