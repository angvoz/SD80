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

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMLocationTests extends AST2BaseTest {

   private static final String _TEXT_ = "<text>"; //$NON-NLS-1$
   public void testBaseCase() throws ParserException {
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse("int x;", p); //$NON-NLS-1$
         IASTDeclaration declaration = tu.getDeclarations()[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), 6);
         IASTNodeLocation[] tuLocations = tu.getNodeLocations();
         assertEquals(tuLocations.length, nodeLocations.length);
         assertEquals(fileLocation.getFileName(),
               ((IASTFileLocation) tuLocations[0]).getFileName()); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), tuLocations[0]
               .getNodeOffset());
         assertEquals(fileLocation.getNodeLength(), tuLocations[0]
               .getNodeLength());
      }
   }

   public void testSimpleDeclaration() throws ParserException {
      String code ="int xLen5, * yLength8, zLength16( int );"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 1);
         IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarations[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), code.indexOf( ";") + 1); //$NON-NLS-1$
         IASTDeclarator[] declarators = declaration.getDeclarators();
         assertEquals( declarators.length, 3 );
         for( int i = 0; i < 3; ++i )
         {
            IASTDeclarator declarator = declarators[i];
            switch( i )
            {
               case 0:
                  assertSoleLocation( declarator, code.indexOf( "xLen5"), "xLen5".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 1:
                  assertSoleLocation( declarator, code.indexOf( "* yLength8"), "* yLength8".length()); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 2:
                  assertSoleLocation( declarator, code.indexOf( "zLength16( int )"), "zLength16( int )".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
            }
         }
         
      }
   }

   
   public void testSimpleObjectStyleMacroDefinition() throws Exception {
      String code ="/* hi */\n#define FOOT 0x01\n\n"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 0);
         IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
         assertNotNull( macros );
         assertEquals( macros.length, 1 );
         assertSoleLocation( macros[0], code.indexOf( "#"), code.indexOf( "0x01") + 4 - code.indexOf( "#")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         assertTrue( macros[0] instanceof IASTPreprocessorObjectStyleMacroDefinition );
         assertEquals( macros[0].getName().toString(), "FOOT" ); //$NON-NLS-1$
         assertEquals( macros[0].getExpansion(), "0x01"); //$NON-NLS-1$
      }
   }
   

   public void testSimpleFunctionStyleMacroDefinition() throws Exception {
      String code = "#define FOOBAH( WOOBAH ) JOHN##WOOBAH\n\n"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 0);
         IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
         assertNotNull( macros );
         assertEquals( macros.length, 1 );
         assertTrue( macros[0] instanceof IASTPreprocessorFunctionStyleMacroDefinition );
         assertSoleLocation( macros[0], code.indexOf( "#define"), code.indexOf( "##WOOBAH") + 8 - code.indexOf( "#define")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$s
         assertEquals( macros[0].getName().toString(), "FOOBAH" ); //$NON-NLS-1$
         assertEquals( macros[0].getExpansion(), "JOHN##WOOBAH"); //$NON-NLS-1$
         IASTFunctionStyleMacroParameter [] parms = ((IASTPreprocessorFunctionStyleMacroDefinition)macros[0]).getParameters();
         assertNotNull( parms );
         assertEquals( parms.length, 1 );
         assertEquals( parms[0].getParameter(), "WOOBAH" ); //$NON-NLS-1$
      }
      
   }
   
   /**
    * @param declarator
    * @param offset
    * @param length
    */
   private void assertSoleLocation(IASTNode n, int offset, int length) {
      IASTNodeLocation [] locations = n.getNodeLocations();
      assertEquals( locations.length, 1 );
      IASTNodeLocation nodeLocation = locations[0];
      assertEquals( nodeLocation.getNodeOffset(), offset );
      assertEquals( nodeLocation.getNodeLength(), length );
   }
   
   public void testBug83664() throws Exception {
       String code = "int foo(x) int x; {\n 	return x;\n   }\n"; //$NON-NLS-1$
       IASTTranslationUnit tu = parse( code, ParserLanguage.C );
       IASTDeclaration [] declarations = tu.getDeclarations();
       assertEquals( declarations.length, 1 );
       IASTFunctionDefinition definition = (IASTFunctionDefinition) declarations[0];
       IASTFunctionDeclarator declarator = definition.getDeclarator();
       assertSoleLocation( declarator, code.indexOf( "foo" ), code.indexOf( "int x;") + 6 - code.indexOf( "foo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
       assertEquals( body.getStatements().length,  1 );
       IASTReturnStatement returnStatement= (IASTReturnStatement) body.getStatements()[0];
       IASTIdExpression expression = (IASTIdExpression) returnStatement.getReturnValue();
       assertSoleLocation( expression, code.indexOf( "return ") + "return ".length(), 1 ); //$NON-NLS-1$ //$NON-NLS-2$
   }
   

   public void testElaboratedTypeSpecifier() throws ParserException {
      String code = "/* blah */ struct A anA; /* blah */"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
         IASTElaboratedTypeSpecifier elabType = (IASTElaboratedTypeSpecifier) declaration.getDeclSpecifier();
         assertSoleLocation( elabType, code.indexOf( "struct"), "struct A".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   
   public void testBug83852() throws Exception {
      String code = "/* blah */ typedef short jc;  int x = 4;  jc myJc = (jc)x; "; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration [] declarations = tu.getDeclarations();
         assertEquals( 3, declarations.length );
         for( int i = 0; i < 3; ++i )
         {
            IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declarations[i];
            int start = 0, length = 0;
            switch( i )
            {
               case 0:
                  start = code.indexOf( "typedef"); //$NON-NLS-1$
                  length = "typedef short jc;".length(); //$NON-NLS-1$
                  break;
               case 1:
                  start = code.indexOf( "int x = 4;"); //$NON-NLS-1$
                  length = "int x = 4;".length(); //$NON-NLS-1$
                  break;
               case 2:
                  start = code.indexOf( "jc myJc = (jc)x;"); //$NON-NLS-1$
                  length = "jc myJc = (jc)x;".length(); //$NON-NLS-1$
                  break;
            }
            assertSoleLocation( decl, start, length );
         }
         IASTInitializerExpression initializer = (IASTInitializerExpression) ((IASTSimpleDeclaration)declarations[2]).getDeclarators()[0].getInitializer();
         IASTCastExpression castExpression = (IASTCastExpression) initializer.getExpression();
         IASTTypeId typeId = castExpression.getTypeId();
         assertSoleLocation( typeId, code.indexOf( "(jc)") + 1, "jc".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }
   
   public void testBug83853() throws ParserException {
      String code = "int f() {return (1?0:1);	}"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p);
         IASTFunctionDefinition definition = (IASTFunctionDefinition) tu.getDeclarations()[0];
         IASTCompoundStatement statement = (IASTCompoundStatement) definition.getBody();
         IASTReturnStatement returnStatement = (IASTReturnStatement) statement.getStatements()[0];
         IASTUnaryExpression unaryExpression = (IASTUnaryExpression) returnStatement.getReturnValue();
         assertEquals( unaryExpression.getOperator(), IASTUnaryExpression.op_bracketedPrimary );
         IASTConditionalExpression conditional = (IASTConditionalExpression) unaryExpression.getOperand();
         assertSoleLocation( conditional,code.indexOf( "1?0:1"), "1?0:1".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      
   }
   

}