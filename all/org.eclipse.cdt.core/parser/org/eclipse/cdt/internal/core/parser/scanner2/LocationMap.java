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
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class LocationMap implements ILocationResolver, IScannerPreprocessorLog {

   /**
    * @author jcamelon
    */
   protected static class _InclusionStatement extends ScannerASTNode implements
         IASTPreprocessorIncludeStatement {

      private final char[] path;

      /**
       * @param cs
       */
      public _InclusionStatement(char[] cs) {
         this.path = cs;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement#getPath()
       */
      public String getPath() {
         return new String(path);
      }

   }

   /**
    * @author jcamelon
    */
   public static class ASTFunctionMacro extends ScannerASTNode implements
         IASTPreprocessorFunctionStyleMacroDefinition {

      private IASTName name;
      private String   expansion;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroDefinition#getParameters()
       */
      public IASTFunctionStyleMacroParameter[] getParameters() {
         if (parameters == null)
            return IASTFunctionStyleMacroParameter.EMPTY_PARAMETER_ARRAY;
         removeNullParameters();
         return parameters;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroDefinition#addParameter(org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter)
       */
      public void addParameter(IASTFunctionStyleMacroParameter parm) {
         if (parameters == null) {
            parameters = new IASTFunctionStyleMacroParameter[DEFAULT_PARMS_LIST_SIZE];
            currentIndex = 0;
         }
         if (parameters.length == currentIndex) {
            IASTFunctionStyleMacroParameter[] old = parameters;
            parameters = new IASTFunctionStyleMacroParameter[old.length * 2];
            for (int i = 0; i < old.length; ++i)
               parameters[i] = old[i];
         }
         parameters[currentIndex++] = parm;
      }

      private void removeNullParameters() {
         int nullCount = 0;
         for (int i = 0; i < parameters.length; ++i)
            if (parameters[i] == null)
               ++nullCount;
         if (nullCount == 0)
            return;
         IASTFunctionStyleMacroParameter[] old = parameters;
         int newSize = old.length - nullCount;
         parameters = new IASTFunctionStyleMacroParameter[newSize];
         for (int i = 0; i < newSize; ++i)
            parameters[i] = old[i];
         currentIndex = newSize;
      }

      private int                               currentIndex            = 0;
      private IASTFunctionStyleMacroParameter[] parameters              = null;
      private static final int                  DEFAULT_PARMS_LIST_SIZE = 2;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getName()
       */
      public IASTName getName() {
         return name;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setName(org.eclipse.cdt.core.dom.ast.IASTName)
       */
      public void setName(IASTName name) {
         this.name = name;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getExpansion()
       */
      public String getExpansion() {
         return expansion;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setExpansion(java.lang.String)
       */
      public void setExpansion(String exp) {
         this.expansion = exp;
      }

   }

   public static class ScannerASTNode extends ASTNode {
      private IASTNode        parent;
      private ASTNodeProperty property;

      public IASTNode getParent() {
         return parent;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
       */
      public ASTNodeProperty getPropertyInParent() {
         return property;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNode#setParent(org.eclipse.cdt.core.dom.ast.IASTNode)
       */
      public void setParent(IASTNode parent) {
         this.parent = parent;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.cdt.core.dom.ast.IASTNodeProperty)
       */
      public void setPropertyInParent(ASTNodeProperty property) {
         this.property = property;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNode#getTranslationUnit()
       */
      public IASTTranslationUnit getTranslationUnit() {
         if (this instanceof IASTTranslationUnit)
            return (IASTTranslationUnit) this;
         IASTNode node = getParent();
         while (!(node instanceof IASTTranslationUnit) && node != null) {
            node = node.getParent();
         }
         return (IASTTranslationUnit) node;
      }

   }

   /**
    * @author jcamelon
    */
   public static class ScannerASTName extends ScannerASTNode implements
         IASTName {
      private final char[] name;

      public ScannerASTName(char[] n) {
         this.name = n;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
       */
      public IBinding resolveBinding() {
         // TODO Auto-generated method stub
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTName#toCharArray()
       */
      public char[] toCharArray() {
         return name;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#toString()
       */
      public String toString() {
         return new String(name);
      }
   }

   /**
    * @author jcamelon
    */
   public static class ASTObjectMacro extends ScannerASTNode implements
         IASTPreprocessorObjectStyleMacroDefinition {

      private IASTName name;
      private String   expansion;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getName()
       */
      public IASTName getName() {
         return name;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setName(org.eclipse.cdt.core.dom.ast.IASTName)
       */
      public void setName(IASTName name) {
         this.name = name;

      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getExpansion()
       */
      public String getExpansion() {
         return expansion;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setExpansion(java.lang.String)
       */
      public void setExpansion(String exp) {
         this.expansion = exp;
      }

   }

   public static class Location implements IASTNodeLocation {
      private final int nodeOffset;
      private final int nodeLength;

      /**
       * @param offset
       * @param length
       */
      public Location(int offset, int length) {
         nodeOffset = offset;
         nodeLength = length;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeOffset()
       */
      public int getNodeOffset() {
         return nodeOffset;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeLength()
       */
      public int getNodeLength() {
         return nodeLength;
      }

   }

   public static class ASTFunctionMacroParameter extends ScannerASTNode
         implements IASTFunctionStyleMacroParameter {
      private String value;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter#getParameter()
       */
      public String getParameter() {
         return value;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter#setParameter(java.lang.String)
       */
      public void setParameter(String value) {
         this.value = value;
      }

   }

   public static class FileLocation extends Location implements
         IASTFileLocation {

      private String fileName;

      /**
       * @param length
       * @param offset
       * @param tu_filename
       *  
       */
      public FileLocation(char[] tu_filename, int offset, int length) {
         super(offset, length);
         fileName = new String(tu_filename);
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getFileName()
       */
      public String getFileName() {
         return fileName;
      }

   }

   protected static class _Context {
      /**
       * @param startOffset
       * @param endOffset
       */
      public _Context(_CompositeContext parent, int startOffset, int endOffset) {
         this.context_directive_start = startOffset;
         this.context_directive_end = endOffset;
         this.context_ends = endOffset;
         this.parent = parent;
      }

      public final int                context_directive_start;
      public final int                context_directive_end;
      public int                      context_ends;
      private final _CompositeContext parent;

      public _CompositeContext getParent() {
         return parent;
      }
   }

   protected static class _CompositeContext extends _Context {

      public _CompositeContext(_CompositeContext parent, int startOffset,
            int endOffset) {
         super(parent, startOffset, endOffset);
      }

      private static final int DEFAULT_SUBCONTEXT_ARRAY_SIZE = 8;

      protected List           subContexts                   = Collections.EMPTY_LIST;

      public List getSubContexts() {
         return subContexts;
      }

      public void addSubContext(_Context c) {
         if (subContexts == Collections.EMPTY_LIST)
            subContexts = new ArrayList(DEFAULT_SUBCONTEXT_ARRAY_SIZE);
         subContexts.add(c);
      }

      /**
       * @return
       */
      public boolean hasSubContexts() {
         return subContexts != Collections.EMPTY_LIST;
      }

   }

   protected static class _Inclusion extends _CompositeContext {
      public final CodeReader reader;

      public _Inclusion(_CompositeContext parent, CodeReader reader,
            int startOffset, int endOffset) {
         super(parent, startOffset, endOffset);
         this.reader = reader;
      }
   }

   protected static class _TranslationUnit extends _CompositeContext {
      public final CodeReader reader;

      /**
       * @param startOffset
       * @param endOffset
       */
      public _TranslationUnit(CodeReader reader) {
         super(null, 0, 0);
         this.reader = reader;
      }
   }

   protected static class _MacroDefinition extends _Context {

      /**
       * @param parent
       * @param startOffset
       * @param endOffset
       * @param nameOffset
       *           TODO
       */
      public _MacroDefinition(_CompositeContext parent, int startOffset,
            int endOffset, char[] name, int nameOffset, char[] expansion) {
         super(parent, startOffset, endOffset);
         this.name = name;
         this.expansion = expansion;
         this.nameOffset = nameOffset;
      }

      public final char[] name;
      public final char[] expansion;
      public final int    nameOffset;
   }

   protected static class _ObjectMacroDefinition extends _MacroDefinition {
      /**
       * @param parent
       * @param startOffset
       * @param endOffset
       * @param name
       *           TODO
       * @param expansion
       *           TODO
       */
      public _ObjectMacroDefinition(_CompositeContext parent, int startOffset,
            int endOffset, char[] name, int nameOffset, char[] expansion) {
         super(parent, startOffset, endOffset, name, nameOffset, expansion);
      }

   }

   protected static class _FunctionMacroDefinition extends _MacroDefinition {

      public final char[][] parms;

      /**
       * @param parent
       * @param startOffset
       * @param endOffset
       * @param parameters
       */
      public _FunctionMacroDefinition(_CompositeContext parent,
            int startOffset, int endOffset, char[] name, int nameOffset,
            char[] expansion, char[][] parameters) {
         super(parent, startOffset, endOffset, name, nameOffset, expansion);
         this.parms = parameters;
      }
   }

   protected static class _Problem extends _Context {
      public final IASTProblem problem;

      /**
       * @param parent
       * @param startOffset
       * @param endOffset
       */
      public _Problem(_CompositeContext parent, int startOffset, int endOffset,
            IASTProblem problem) {
         super(parent, startOffset, endOffset);
         this.problem = problem;
      }

   }

   protected _TranslationUnit  tu;
   protected _CompositeContext currentContext;

   /**
    *  
    */
   public LocationMap() {
   }

   private static final IASTProblem[]                      EMPTY_PROBLEMS_ARRAY          = new IASTProblem[0];
   private static final IASTNodeLocation[]                 EMPTY_LOCATION_ARRAY          = new IASTNodeLocation[0];
   private static final IASTPreprocessorMacroDefinition[]  EMPTY_MACRO_DEFINITIONS_ARRAY = new IASTPreprocessorMacroDefinition[0];
   private static final IASTPreprocessorIncludeStatement[] EMPTY_INCLUDES_ARRAY          = new IASTPreprocessorIncludeStatement[0];

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getMacroDefinitions()
    */
   public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
      List contexts = new ArrayList(8);
      LocationMap.collectContexts(V_MACRODEFS, tu, contexts);
      if (contexts.isEmpty())
         return EMPTY_MACRO_DEFINITIONS_ARRAY;
      IASTPreprocessorMacroDefinition[] result = new IASTPreprocessorMacroDefinition[contexts
            .size()];
      for (int i = 0; i < contexts.size(); ++i) 
         result[i] = createASTMacroDefinition((_MacroDefinition) contexts.get(i));
      

      return result;
   }

   /**
    * @param d
    * @return
    */
   protected IASTPreprocessorMacroDefinition createASTMacroDefinition(_MacroDefinition d) {
      IASTPreprocessorMacroDefinition r = null;
      if (d instanceof _ObjectMacroDefinition)
         r = new ASTObjectMacro();
      else if (d instanceof _FunctionMacroDefinition) {
         IASTPreprocessorFunctionStyleMacroDefinition f = new ASTFunctionMacro();
         char[][] parms = ((_FunctionMacroDefinition) d).parms;
         for (int j = 0; j < parms.length; ++j) {
            IASTFunctionStyleMacroParameter parm = new ASTFunctionMacroParameter();
            parm.setParameter(new String(parms[j]));
            f.addParameter(parm);
            parm.setParent(f);
            parm
                  .setPropertyInParent(IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER);
         }
         r = f;
      }

      IASTName name = new ScannerASTName(d.name);
      name.setPropertyInParent(IASTPreprocessorMacroDefinition.MACRO_NAME);
      name.setParent(r);
      r.setName(name);
      r.setExpansion(new String(d.expansion));
      ((ScannerASTNode) r).setOffsetAndLength(d.context_directive_start,
            d.context_directive_end - d.context_directive_start);
      return r;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getIncludeDirectives()
    */
   public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
      List contexts = new ArrayList(8);
      collectContexts(V_INCLUSIONS, tu, contexts);
      if (contexts.isEmpty())
         return EMPTY_INCLUDES_ARRAY;
      IASTPreprocessorIncludeStatement[] result = new IASTPreprocessorIncludeStatement[contexts
            .size()];
      for (int i = 0; i < contexts.size(); ++i) {
         _Inclusion inc = ((_Inclusion) contexts.get(i));
         result[i] = new _InclusionStatement(inc.reader.filename);
         ((ScannerASTNode) result[i]).setOffsetAndLength(
               inc.context_directive_start, inc.context_directive_end
                     - inc.context_directive_start);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getAllPreprocessorStatements()
    */
   public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getLocations(int,
    *      int)
    */
   public IASTNodeLocation[] getLocations(int offset, int length) {
      if (tu == null)
         return EMPTY_LOCATION_ARRAY;
      _Context c = findContextForOffset(offset);
      if (c.context_ends >= offset + length)
         return createSoleLocation(c, offset, length);

      return EMPTY_LOCATION_ARRAY;
   }

   /**
    * @param c
    * @param offset
    * @param length
    * @return
    */
   protected IASTNodeLocation[] createSoleLocation(_Context c, int offset,
         int length) {
      IASTNodeLocation[] result = new IASTNodeLocation[1];
      if (c instanceof _TranslationUnit) {
         result[0] = new FileLocation(((_TranslationUnit) c).reader.filename,
               reconcileOffset(c, offset), length);
         return result;
      }
      if (c instanceof _Inclusion) {
         result[0] = new FileLocation(((_Inclusion) c).reader.filename,
               reconcileOffset(c, offset), length);
         return result;
      }
      return EMPTY_LOCATION_ARRAY;
   }

   /**
    * @param c
    * @param offset
    * @return
    */
   protected static int reconcileOffset(_Context c, int offset) {
      int subtractOff = 0;
      if (c instanceof _CompositeContext) {
         List subs = ((_CompositeContext) c).getSubContexts();
         for (int i = 0; i < subs.size(); ++i) {
            _Context subC = (_Context) subs.get(i);
            if (subC.context_ends < offset)
               subtractOff += subC.context_ends - subC.context_directive_end;
            else
               break;
         }
      }
      return offset - c.context_directive_end - subtractOff;
   }

   /**
    * @param offset
    * @return
    */
   protected _Context findContextForOffset(int offset) {
      return findContextForOffset(tu, offset);
   }

   protected static _Context findContextForOffset(_CompositeContext context,
         int offset) {
      if (!context.hasSubContexts()) {
         if (context.context_ends >= offset)
            return context;
         return null;
      }
      List subContexts = context.getSubContexts();
      //check first
      _Context bestContext = (_Context) subContexts.get(0);
      if (bestContext.context_directive_end > offset)
         return context;

      for (int i = 1; i < subContexts.size(); ++i) {
         _Context nextContext = (_Context) subContexts.get(i);
         if (nextContext.context_directive_end < offset)
            bestContext = nextContext;
         else
            break;
      }

      if (bestContext.context_ends < offset)
         return context;

      if (bestContext instanceof _CompositeContext)
         return findContextForOffset((_CompositeContext) bestContext, offset);
      return bestContext;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getLocation(int)
    */
   public IASTNodeLocation getLocation(int offset) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startTranslationUnit()
    */
   public void startTranslationUnit(CodeReader tu_reader) {
      tu = new _TranslationUnit(tu_reader);
      currentContext = tu;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endTranslationUnit(int)
    */
   public void endTranslationUnit(int offset) {
      tu.context_ends = offset;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startInclusion(char[],
    *      int)
    */
   public void startInclusion(CodeReader reader, int offset, int endOffset) {
      _Inclusion i = new _Inclusion(currentContext, reader, offset, endOffset);
      currentContext.addSubContext(i);
      currentContext = i;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endInclusion(char[],
    *      int)
    */
   public void endInclusion(int offset) {
      ((_Inclusion) currentContext).context_ends = offset;
      currentContext = currentContext.getParent();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterObjectStyleMacroExpansion(char[],
    *      char[], int)
    */
   public void enterObjectStyleMacroExpansion(char[] name, char[] expansion,
         int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitObjectStyleMacroExpansion(char[],
    *      int)
    */
   public void exitObjectStyleMacroExpansion(char[] name, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterFunctionStyleExpansion(char[],
    *      char[][], char[], int)
    */
   public void enterFunctionStyleExpansion(char[] name, char[][] parameters,
         char[] expansion, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitFunctionStyleExpansion(char[],
    *      int)
    */
   public void exitFunctionStyleExpansion(char[] name, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineObjectStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro,
    *      int, int, int, int)
    */
   public void defineObjectStyleMacro(ObjectStyleMacro m, int startOffset,
         int nameOffset, int nameEndOffset, int endOffset) {
      currentContext.addSubContext(new _ObjectMacroDefinition(currentContext,
            startOffset, endOffset, m.name, nameOffset, m.expansion));
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineFunctionStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro,
    *      int, int, int, int)
    */
   public void defineFunctionStyleMacro(FunctionStyleMacro m, int startOffset,
         int nameOffset, int nameEndOffset, int endOffset) {
      currentContext
            .addSubContext(new _FunctionMacroDefinition(currentContext,
                  startOffset, endOffset, m.name, nameOffset, m.expansion,
                  removeNullArguments( m.arglist)));
   }

   /**
    * @param arglist
    * @return
    */
   private char [][] removeNullArguments(char[][] arglist) {
      int nullCount = 0;
      for (int i = 0; i < arglist.length; ++i)
         if (arglist[i] == null)
            ++nullCount;
      if (nullCount == 0)
         return arglist;
      char [][] old = arglist;
      int newSize = old.length - nullCount;
      arglist = new char [newSize][];
      for (int i = 0; i < newSize; ++i)
         arglist[i] = old[i];
      return arglist;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIf(int,
    *      int)
    */
   public void encounterPoundIf(int startOffset, int endOffset, boolean taken) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundPragma(int,
    *      int)
    */
   public void encounterPoundPragma(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundError(int,
    *      int)
    */
   public void encounterPoundError(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIfdef(int,
    *      int)
    */
   public void encounterPoundIfdef(int startOffset, int endOffset, boolean taken) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundUndef(int,
    *      int)
    */
   public void encounterPoundUndef(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElse(int,
    *      int)
    */
   public void encounterPoundElse(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElif(int,
    *      int)
    */
   public void encounterPoundElif(int startOffset, int endOffset, boolean taken) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundEndIf(int,
    *      int)
    */
   public void encounterPoundEndIf(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getTranslationUnitPath()
    */
   public String getTranslationUnitPath() {
      return new String(tu.reader.filename);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#cleanup()
    */
   public void cleanup() {
      // TODO Auto-generated method stub
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getScannerProblems()
    */
   public IASTProblem[] getScannerProblems() {
      List contexts = new ArrayList(8);
      LocationMap.collectContexts(V_PROBLEMS, tu, contexts);
      if (contexts.isEmpty())
         return EMPTY_PROBLEMS_ARRAY;
      IASTProblem[] result = new IASTProblem[contexts.size()];
      for (int i = 0; i < contexts.size(); ++i)
         result[i] = ((_Problem) contexts.get(i)).problem;

      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterIProblem(org.eclipse.cdt.core.parser.IProblem)
    */
   public void encounterProblem(IASTProblem problem) {
      ScannerASTProblem p = (ScannerASTProblem) problem;
      _Problem pr = new _Problem(currentContext, p.getOffset(), p.getOffset()
            + p.getLength(), problem);
      pr.context_ends = p.getOffset() + p.getLength();
   }

   protected static final int V_ALL        = 1;
   protected static final int V_INCLUSIONS = 2;
   protected static final int V_PROBLEMS   = 3;
   protected static final int V_MACRODEFS  = 4;

   protected static void collectContexts(int key, _Context source, List result) {
      switch (key) {
         case V_ALL:
            result.add(source);
            break;
         case V_INCLUSIONS:
            if (source instanceof _Inclusion)
               result.add(source);
            break;
         case V_PROBLEMS:
            if (source instanceof _Problem)
               result.add(source);
            break;
         case V_MACRODEFS:
            if (source instanceof _MacroDefinition)
               result.add(source);
            break;
      }
      if (source instanceof _CompositeContext) {
         List l = ((_CompositeContext) source).getSubContexts();
         for (int i = 0; i < l.size(); ++i)
            collectContexts(key, (_Context) l.get(i), result);
      }
   }

}