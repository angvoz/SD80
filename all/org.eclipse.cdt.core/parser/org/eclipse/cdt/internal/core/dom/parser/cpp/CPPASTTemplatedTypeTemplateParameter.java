/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Template template parameter
 */
public class CPPASTTemplatedTypeTemplateParameter extends ASTNode implements
        ICPPASTTemplatedTypeTemplateParameter, IASTAmbiguityParent {

    private ICPPASTTemplateParameter [] fNestedParameters = null;
    private boolean fIsParameterPack;
    private IASTName fName;
    private IASTExpression fDefaultValue;

    public CPPASTTemplatedTypeTemplateParameter() {
	}

	public CPPASTTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue) {
		setName(name);
		setDefaultValue(defaultValue);
	}
	
	public CPPASTTemplatedTypeTemplateParameter copy() {
		return copy(CopyStyle.withoutLocations);
	}

	public CPPASTTemplatedTypeTemplateParameter copy(CopyStyle style) {
		CPPASTTemplatedTypeTemplateParameter copy = new CPPASTTemplatedTypeTemplateParameter();
		copy.setName(fName == null ? null : fName.copy(style));
		copy.setDefaultValue(fDefaultValue == null ? null : fDefaultValue.copy(style));
		copy.fIsParameterPack = fIsParameterPack;
		for (ICPPASTTemplateParameter param : getTemplateParameters())
			copy.addTemplateParameter(param == null ? null : param.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	public ICPPASTTemplateParameter[] getTemplateParameters() {
		if (fNestedParameters == null)
			return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
		fNestedParameters = (ICPPASTTemplateParameter[]) ArrayUtil.trim(ICPPASTTemplateParameter.class, fNestedParameters);
		return fNestedParameters;
	}

	@Deprecated
	public void addTemplateParamter(ICPPASTTemplateParameter param) {
		addTemplateParameter(param);
	}

	public void addTemplateParameter(ICPPASTTemplateParameter param) {
		assertNotFrozen();
		if (param != null) {
			fNestedParameters = (ICPPASTTemplateParameter[]) ArrayUtil.append(ICPPASTTemplateParameter.class,
					fNestedParameters, param);
			param.setParent(this);
			param.setPropertyInParent(PARAMETER);
		}
	}

	
    public void setIsParameterPack(boolean val) {
    	assertNotFrozen();
    	fIsParameterPack= val;
	}

	public boolean isParameterPack() {
		return fIsParameterPack;
	}

	public IASTName getName() {
        return fName;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.fName =name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(PARAMETER_NAME);
		}
    }

    public IASTExpression getDefaultValue() {
        return fDefaultValue;
    }

    public void setDefaultValue(IASTExpression expression) {
        assertNotFrozen();
        this.fDefaultValue = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(DEFAULT_VALUE);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
    	if (action.shouldVisitTemplateParameters) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        ICPPASTTemplateParameter [] ps = getTemplateParameters();
		for (int i = 0; i < ps.length; i++) {
			if (!ps[i].accept(action))
				return false;
		}
		if (fName != null && !fName.accept(action))
			return false;
		if (fDefaultValue != null && !fDefaultValue.accept(action))
			return false;
        
		if (action.shouldVisitTemplateParameters && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == fName )
			return r_declaration;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == fDefaultValue )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            fDefaultValue  = (IASTExpression) other;
        }
    }
}
