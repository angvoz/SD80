/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.FileBasePluginTest;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.core.resources.IFile;

/**
 * @author jcamelon
 */
public class DOMLocationInclusionTests extends FileBasePluginTest {

   private static final IScannerInfo       SCANNER_INFO = new ScannerInfo();
   private static final IParserLogService  NULL_LOG     = new NullLogService();
   private static final ICodeReaderFactory factory      = CDOM
                                                              .getInstance()
                                                              .getCodeReaderFactory(
                                                                    CDOM.PARSE_SAVED_RESOURCES);

   /**
    * @param name
    * @param className
    */
   public DOMLocationInclusionTests(String name) {
      super(name, DOMLocationInclusionTests.class);
   }

   protected IASTTranslationUnit parse(IFile code, ParserLanguage language)
         throws Exception {
      InputStream stream = code.getContents();
      IScanner scanner = new DOMScanner(new CodeReader(code.getLocation()
            .toOSString(), stream), SCANNER_INFO, ParserMode.COMPLETE_PARSE,
            language, NULL_LOG, getScannerConfig(language), factory);
      ISourceCodeParser parser = null;
      if (language == ParserLanguage.CPP) {
         parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE,
               NULL_LOG, new GPPParserExtensionConfiguration());
      } else {
         parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE,
               NULL_LOG, new GCCParserExtensionConfiguration());
      }
      stream.close();
      IASTTranslationUnit parseResult = parser.parse();

      if (parser.encounteredError())
         throw new ParserException("FAILURE"); //$NON-NLS-1$

      if (language == ParserLanguage.C) {
         IASTProblem[] problems = CVisitor.getProblems(parseResult);
         assertEquals(problems.length, 0);
      } else if (language == ParserLanguage.CPP) {
         IASTProblem[] problems = CPPVisitor.getProblems(parseResult);
         assertEquals(problems.length, 0);
      }

      return parseResult;
   }

   /**
    * @param language
    * @return
    */
   private IScannerExtensionConfiguration getScannerConfig(
         ParserLanguage language) {
      if (language == ParserLanguage.CPP)
         return new GPPScannerExtensionConfiguration();
      return new GCCScannerExtensionConfiguration();
   }

   /**
    * @param pathEndsWith
    *           TODO
    * @param offset
    * @param length
    * @param declarator
    */
   private void assertSoleFileLocation(IASTNode n, String pathEndsWith,
         int offset, int length) {
      IASTNodeLocation[] locations = n.getNodeLocations();
      assertEquals(locations.length, 1);
      IASTFileLocation nodeLocation = (IASTFileLocation) locations[0];
      assertTrue(nodeLocation.getFileName().endsWith(pathEndsWith));
      assertEquals(nodeLocation.getNodeOffset(), offset);
      assertEquals(nodeLocation.getNodeLength(), length);
   }

   public void testSimpleInclusion() throws Exception {
      String foo = "int FOO;"; //$NON-NLS-1$
      String code = "int bar;\n#include \"foo.h\"\n"; //$NON-NLS-1$

      importFile("foo.h", foo); //$NON-NLS-1$
      IFile cpp = importFile("code.cpp", code); //$NON-NLS-1$

      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(cpp, p); //$NON-NLS-1$
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 2);
         IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
         IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
         assertSoleFileLocation(bar,
               "code.cpp", code.indexOf("int"), code.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

         assertSoleFileLocation(FOO,
               "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
   }
   
   public void testSimpleInclusion2() throws Exception {
      String foo = "int FOO;"; //$NON-NLS-1$
      String code = "int bar;\n#include \"foo.h\"\nfloat byob;\n"; //$NON-NLS-1$

      importFile("foo.h", foo); //$NON-NLS-1$
      IFile cpp = importFile("code.cpp", code); //$NON-NLS-1$

      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(cpp, p); //$NON-NLS-1$
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 3);
         IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
         IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
         IASTSimpleDeclaration byob = (IASTSimpleDeclaration) declarations[2];
         assertSoleFileLocation(bar,
               "code.cpp", code.indexOf("int"), code.indexOf("r;") + 2 - code.indexOf( "int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
         assertSoleFileLocation(FOO,
               "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1 - foo.indexOf( "int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
         assertSoleFileLocation( byob, "code.cpp", code.indexOf( "float"), code.indexOf( "b;") + 2 - code.indexOf( "float") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
   }
   

   public static Test suite() {
      TestSuite suite = new TestSuite(DOMLocationInclusionTests.class);
      suite.addTest(new DOMLocationInclusionTests("cleanupProject")); //$NON-NLS-1$
      return suite;
   }
}