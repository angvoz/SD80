/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

public class DOMCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(ITextViewer viewer,
											  int offset,
											  IWorkingCopy workingCopy,
											  ASTCompletionNode completionNode,
                                              String prefix,
											  List proposals) {
		
		if (completionNode == null) {
			return;
		}
		if(inPreprocessorDirective(viewer.getDocument(), offset)) {
			// add only macros
			addMacroProposals(viewer, offset, completionNode, prefix, proposals);
		} else {
			boolean handleMacros= false;
			IASTName[] names = completionNode.getNames();
			if (names == null || names.length == 0)
				// No names, not much we can do here
				return;
			
			// Find all bindings
			List allBindings = new ArrayList();
			for (int i = 0; i < names.length; ++i) {
				if (names[i].getTranslationUnit() == null)
					// The node isn't properly hooked up, must have backtracked out of this node
					continue;
				IBinding[] bindings = names[i].resolvePrefix();
				if (names[i].getParent() instanceof IASTIdExpression) {
					// handle macros only if there is a prefix
					handleMacros = prefix.length() > 0;
				}
				if (bindings != null)
					for (int j = 0; j < bindings.length; ++j) {
						IBinding binding = bindings[j];
						//if (!allBindings.contains(binding))
						// TODO I removed this check since equals in the IBinding tree is currently broken
						// It is returning true at times when I don't think it should (Bug 91577)
							allBindings.add(binding);
					}
			}
			
			Iterator iBinding = allBindings.iterator();
			while (iBinding.hasNext()) {
				IBinding binding = (IBinding)iBinding.next();
				handleBinding(binding, completionNode, offset, viewer, proposals);
			}

			if (handleMacros) {
				addMacroProposals(viewer, offset, completionNode, prefix, proposals);
			}
		}
	}

	private void addMacroProposals(ITextViewer viewer, int offset,
			ASTCompletionNode completionNode, String prefix, List proposals) {
		char[] prefixChars= prefix.toCharArray();
		IASTPreprocessorMacroDefinition[] macros = completionNode.getTranslationUnit().getMacroDefinitions();
		if (macros != null)
			for (int i = 0; i < macros.length; ++i)
				if (CharArrayUtils.equals(macros[i].getName().toCharArray(), 0, prefixChars.length, prefixChars, false))
					handleMacro(macros[i], completionNode, offset, viewer, proposals);
		macros = completionNode.getTranslationUnit().getBuiltinMacroDefinitions();
		if (macros != null)
			for (int i = 0; i < macros.length; ++i)
				if (CharArrayUtils.equals(macros[i].getName().toCharArray(), 0, prefixChars.length, prefixChars, false))
					handleMacro(macros[i], completionNode, offset, viewer, proposals);
	}

	/**
	 * Check if given offset is inside a preprocessor directive.
	 * 
	 * @param doc  the document
	 * @param offset  the offset to check
	 * @return <code>true</code> if offset is inside a preprocessor directive
	 */
	private boolean inPreprocessorDirective(IDocument doc, int offset) {
		if (offset > 0 && offset == doc.getLength()) {
		--offset;
		}
		try {
			return ICPartitions.C_PREPROCESSOR
					.equals(TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, offset, false));
		} catch (BadLocationException exc) {
		}
		return false;
	}

	protected void handleBinding(IBinding binding, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		if (binding instanceof IFunction)  {
			handleFunction((IFunction)binding, completionNode, offset, viewer, proposals);
		} else if (binding instanceof IVariable)  {
			handleVariable((IVariable) binding, completionNode, offset, viewer, proposals);
		}
		else
			proposals.add(createProposal(binding.getName(), binding.getName(), getImage(binding), completionNode, offset, viewer));
	}
	
	private void handleFunction(IFunction function, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		Image image = getImage(function);
		
		StringBuffer repStringBuff = new StringBuffer();
		repStringBuff.append(function.getName());
		repStringBuff.append('(');
		
		StringBuffer dispargs = new StringBuffer(); // for the displayString
        StringBuffer idargs = new StringBuffer();   // for the idString
		String returnTypeStr = null;
		try {
			IParameter[] params = function.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i) {
					IType paramType = params[i].getType();
					if (i > 0) {
                        dispargs.append(',');
                        idargs.append(',');
                    }

					dispargs.append(ASTTypeUtil.getType(paramType));
                    idargs.append(ASTTypeUtil.getType(paramType));
					String paramName = params[i].getName();
					if (paramName != null && paramName.length() > 0) {
						dispargs.append(' ');
						dispargs.append(paramName);
					}
				}
			
			if (function.takesVarArgs()) {
				if (params.length > 0) {
                    dispargs.append(',');
                    idargs.append(',');
                }
                dispargs.append("..."); //$NON-NLS-1$
                idargs.append("..."); //$NON-NLS-1$
			} else if (params.length == 0) { // force the void in
			    dispargs.append("void"); //$NON-NLS-1$
                idargs.append("void"); //$NON-NLS-1$
            }
			
			IFunctionType functionType = function.getType();
			if (functionType != null) {
				IType returnType = functionType.getReturnType();
				if (returnType != null)
					returnTypeStr = ASTTypeUtil.getType(returnType);
			}
		} catch (DOMException e) {
		}
        
        String dispargString = dispargs.toString();
        String idargString = idargs.toString();
		
        StringBuffer dispStringBuff = new StringBuffer(repStringBuff.toString());
		dispStringBuff.append(dispargString);
        dispStringBuff.append(')');
        if (returnTypeStr != null) {
            dispStringBuff.append(' ');
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuffer idStringBuff = new StringBuffer(repStringBuff.toString());
        idStringBuff.append(idargString);
        idStringBuff.append(')');
        String idString = idStringBuff.toString();
		
        repStringBuff.append(')');
        String repString = repStringBuff.toString();

        int repLength = completionNode.getLength();
        int repOffset = offset - repLength;
        CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, dispString, idString, 1, viewer);

		proposal.setCursorPosition(repString.length() - 1);
		
		if (dispargString.length() > 0) {
			CProposalContextInformation info = new CProposalContextInformation(image, dispString, dispargString);
			info.setContextInformationPosition(offset);
			proposal.setContextInformation(info);
		}
		
		proposals.add(proposal);
	}
	
	private void handleVariable(IVariable variable, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		StringBuffer repStringBuff = new StringBuffer();
		repStringBuff.append(variable.getName());
		
		String returnTypeStr = "<unknown>"; //$NON-NLS-1$
		try {
			IType varType = variable.getType();
			if (varType != null)
				returnTypeStr = ASTTypeUtil.getType(varType);
		} catch (DOMException e) {
		}
        
        StringBuffer dispStringBuff = new StringBuffer(repStringBuff.toString());
        if (returnTypeStr != null) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuffer idStringBuff = new StringBuffer(repStringBuff.toString());
        String idString = idStringBuff.toString();
		
        String repString = repStringBuff.toString();

        int repLength = completionNode.getLength();
        int repOffset = offset - repLength;
		Image image = getImage(variable);
        CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, dispString, idString, 1, viewer);
		proposals.add(proposal);
	}
	
	private void handleMacro(IASTPreprocessorMacroDefinition macro, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		String macroName = macro.getName().toString();
		Image image = getImage(CElementImageProvider.getMacroImageDescriptor());
		
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition functionMacro = (IASTPreprocessorFunctionStyleMacroDefinition)macro;
			
			StringBuffer repStringBuff = new StringBuffer();
			repStringBuff.append(macroName);
			repStringBuff.append('(');
			
			StringBuffer args = new StringBuffer();

			IASTFunctionStyleMacroParameter[] params = functionMacro.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i) {
					if (i > 0)
						args.append(", "); //$NON-NLS-1$
					args.append(params[i].getParameter());
				}
			String argString = args.toString();
			
			StringBuffer descStringBuff = new StringBuffer(repStringBuff.toString());
			descStringBuff.append(argString);
			descStringBuff.append(')');
			
			repStringBuff.append(')');
			String repString = repStringBuff.toString();
			String descString = descStringBuff.toString();
			
			CCompletionProposal proposal = createProposal(repString, descString, image, completionNode, offset, viewer);
			proposal.setCursorPosition(repString.length() - 1);
			
			if (argString.length() > 0) {
				CProposalContextInformation info = new CProposalContextInformation(image, descString, argString);
				info.setContextInformationPosition(offset);
				proposal.setContextInformation(info);
			}
			
			proposals.add(proposal);
		} else
			proposals.add(createProposal(macroName, macroName, image, completionNode, offset, viewer));
	}
	
	private CCompletionProposal createProposal(String repString, String dispString, Image image, ASTCompletionNode completionNode, int offset, ITextViewer viewer) {
		int repLength = completionNode.getLength();
		int repOffset = offset - repLength;
		return new CCompletionProposal(repString, repOffset, repLength, image, dispString, 1, viewer);
	}

	private Image getImage(ImageDescriptor desc) {
		return desc != null ? CUIPlugin.getImageDescriptorRegistry().get(desc) : null;
	}
	
	private Image getImage(IBinding binding) {
		ImageDescriptor imageDescriptor = null;
		
		try {
			if (binding instanceof ITypedef) {
				imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
			} else if (binding instanceof ICompositeType) {
				if (((ICompositeType)binding).getKey() == ICPPClassType.k_class || binding instanceof ICPPClassTemplate)
					imageDescriptor = CElementImageProvider.getClassImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
					imageDescriptor = CElementImageProvider.getStructImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
					imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
			} else if (binding instanceof ICPPMethod) {
				switch (((ICPPMethod)binding).getVisibility()) {
				case ICPPMember.v_private:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PRIVATE);
					break;
				case ICPPMember.v_protected:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PROTECTED);
					break;
				default:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PUBLIC);
					break;
				}
			} else if (binding instanceof IFunction) {
				imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof ICPPField) {
				switch (((ICPPField)binding).getVisibility()) {
				case ICPPMember.v_private:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PRIVATE);
					break;
				case ICPPMember.v_protected:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PROTECTED);
					break;
				default:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
					break;
				}
			} else if (binding instanceof IField) {
				imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
			} else if (binding instanceof IVariable) {
				imageDescriptor = CElementImageProvider.getVariableImageDescriptor();
			} else if (binding instanceof IEnumeration) {
				imageDescriptor = CElementImageProvider.getEnumerationImageDescriptor();
            } else if (binding instanceof IEnumerator) {
                imageDescriptor = CElementImageProvider.getEnumeratorImageDescriptor();
            } else if (binding instanceof ICPPNamespace) {
				imageDescriptor = CElementImageProvider.getNamespaceImageDescriptor();
			} else if (binding instanceof ICPPFunctionTemplate) {
				imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof ICPPUsingDeclaration) {
				ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
				if (delegates.length > 0)
					return getImage(delegates[0]);
			}
		} catch (DOMException e) {
		}
		
		return imageDescriptor != null
			? CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;
	}

}
