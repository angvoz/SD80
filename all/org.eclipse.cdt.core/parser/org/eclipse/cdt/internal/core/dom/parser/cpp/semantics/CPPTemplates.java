/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedefSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown;

/**
 * @author aniefer
 */
public class CPPTemplates {

	public static IASTName getTemplateParameterName(ICPPASTTemplateParameter param) {
		if (param instanceof ICPPASTSimpleTypeTemplateParameter)
			return ((ICPPASTSimpleTypeTemplateParameter) param).getName();
		else if (param instanceof ICPPASTTemplatedTypeTemplateParameter)
			return ((ICPPASTTemplatedTypeTemplateParameter) param).getName();
		else if (param instanceof ICPPASTParameterDeclaration)
			return ((ICPPASTParameterDeclaration) param).getDeclarator().getName();
		return null;
	}

	private static ICPPTemplateDefinition getContainingTemplate(ICPPASTTemplateParameter param) {
		IASTNode parent = param.getParent();
		IBinding binding = null;
		if (parent instanceof ICPPASTTemplateDeclaration) {
//			IASTName name = getTemplateName((ICPPASTTemplateDeclaration) parent);
//			if (name != null) {
//				if (name instanceof ICPPASTTemplateId && !(name.getParent() instanceof ICPPASTQualifiedName))
//					name = ((ICPPASTTemplateId) name).getTemplateName();
//
//				binding = name.resolveBinding();
//			}
			ICPPASTTemplateDeclaration[] templates = new ICPPASTTemplateDeclaration[] { (ICPPASTTemplateDeclaration) parent };

			while (parent.getParent() instanceof ICPPASTTemplateDeclaration) {
				parent = parent.getParent();
				templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.append(ICPPASTTemplateDeclaration.class, templates, parent);
			}
			templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.trim(ICPPASTTemplateDeclaration.class, templates);

			ICPPASTTemplateDeclaration templateDeclaration = templates[0];
			IASTDeclaration decl = templateDeclaration.getDeclaration();
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();

			IASTName name = null;
			if (decl instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
				if (dtors.length == 0) {
					IASTDeclSpecifier spec = simpleDecl.getDeclSpecifier();
					if (spec instanceof ICPPASTCompositeTypeSpecifier) {
						name = ((ICPPASTCompositeTypeSpecifier) spec).getName();
					} else if (spec instanceof ICPPASTElaboratedTypeSpecifier) {
						name = ((ICPPASTElaboratedTypeSpecifier) spec).getName();
					}
				} else {
					IASTDeclarator dtor = dtors[0];
					while (dtor.getNestedDeclarator() != null)
						dtor = dtor.getNestedDeclarator();
					name = dtor.getName();
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
				while (dtor.getNestedDeclarator() != null)
					dtor = dtor.getNestedDeclarator();
				name = dtor.getName();
			}
			if (name == null)
				return null;

			if (name instanceof ICPPASTQualifiedName) {
				int idx = templates.length;
				int i = 0;
				IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
				for (int j = 0; j < ns.length; j++) {
					if (ns[j] instanceof ICPPASTTemplateId) {
						++i;
						if (i == idx) {
							binding = ((ICPPASTTemplateId) ns[j]).getTemplateName().resolveBinding();
							break;
						}
					}
				}
				if (binding == null)
					binding = ns[ns.length - 1].resolveBinding();
			} else {
				binding = name.resolveBinding();
			}
		} else if (parent instanceof ICPPASTTemplatedTypeTemplateParameter) {
			ICPPASTTemplatedTypeTemplateParameter templatedParam = (ICPPASTTemplatedTypeTemplateParameter) parent;
			binding = templatedParam.getName().resolveBinding();
		}
		return  (binding instanceof ICPPTemplateDefinition) ? (ICPPTemplateDefinition) binding : null;
	}

	public static IBinding createBinding(ICPPASTTemplateParameter templateParameter) {
		ICPPTemplateDefinition template = getContainingTemplate(templateParameter);

		IBinding binding = null;
		if (template instanceof CPPTemplateTemplateParameter) {
			binding = ((CPPTemplateTemplateParameter) template).resolveTemplateParameter(templateParameter);
		} else if (template instanceof CPPTemplateDefinition) {
			binding = ((CPPTemplateDefinition) template).resolveTemplateParameter(templateParameter);
		} else if (template != null) {
			IASTName name = CPPTemplates.getTemplateParameterName(templateParameter);
			binding = name.getBinding();

			if (binding == null) {
				ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) templateParameter.getParent();
				ICPPASTTemplateParameter[] ps = templateDecl.getTemplateParameters();

				int i = 0;
				for (; i < ps.length; i++) {
					if (templateParameter == ps[i])
						break;
				}

				try {
					ICPPTemplateParameter[] params = template.getTemplateParameters();
					if (i < params.length) {
						binding = params[i];
						name.setBinding(binding);
					}
				} catch (DOMException e) {
				}
			}
		}

	    return binding;
	}

	static public ICPPScope getContainingScope(IASTNode node) {
		while (node != null) {
			if (node instanceof ICPPASTTemplateParameter) {
				IASTNode parent = node.getParent();
				if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				}
			}
			node = node.getParent();
		}

		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static IBinding createBinding(ICPPASTTemplateId id) {
		IASTNode parent = id.getParent();
		int segment = -1;
		if (parent instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) parent).getNames();
			segment = (ns[ns.length - 1] == id) ? 1 : 0;
			parent = parent.getParent();
		}

		IASTNode decl = parent.getParent();
		while (!(decl instanceof IASTDeclaration))
			decl = decl.getParent();
		decl = decl.getParent();

		if (decl instanceof ICPPASTExplicitTemplateInstantiation &&
				parent instanceof ICPPASTElaboratedTypeSpecifier && segment != 0) {
		    return createClassExplicitInstantiation((ICPPASTElaboratedTypeSpecifier) parent);
		} else if (((parent instanceof ICPPASTElaboratedTypeSpecifier &&
				decl instanceof ICPPASTTemplateDeclaration) ||
				parent instanceof ICPPASTCompositeTypeSpecifier) &&
				segment != 0) {
			return createClassSpecialization((ICPPASTDeclSpecifier) parent);
		} else if (parent instanceof ICPPASTFunctionDeclarator && segment != 0) {
			return createFunctionSpecialization(id);
		}

		//a reference: class or function template?
		IBinding template = null;
		if (parent instanceof ICPPASTNamedTypeSpecifier ||
				parent instanceof ICPPASTElaboratedTypeSpecifier ||
				parent instanceof ICPPASTBaseSpecifier ||
				segment == 0) {
			//class template
			IASTName templateName = id.getTemplateName();
			template = templateName.resolveBinding();
			if (template instanceof ICPPClassTemplatePartialSpecialization) {
				//specializations are selected during the instantiation, start with the primary template
				try {
					template = ((ICPPClassTemplatePartialSpecialization) template).getPrimaryClassTemplate();
				} catch (DOMException e) {
					return e.getProblem();
				}
			} else if (template instanceof ICPPSpecialization && !(template instanceof ICPPTemplateDefinition)) {
				template = ((ICPPSpecialization) template).getSpecializedBinding();
			}

			if (template != null && template instanceof ICPPInternalTemplateInstantiator) {
				IASTNode[] args = id.getTemplateArguments();
				IType[] types = CPPTemplates.createTypeArray(args);
				template = ((ICPPInternalTemplateInstantiator) template).instantiate(types);
				return CPPSemantics.postResolution(template, id);
			}
		} else {
			//functions are instantiated as part of the resolution process
			template = CPPVisitor.createBinding(id);
			if (template instanceof ICPPTemplateInstance) {
				IASTName templateName = id.getTemplateName();
				templateName.setBinding(((ICPPTemplateInstance) template).getTemplateDefinition());
			}
		}

		return template;
	}

	protected static IBinding createClassExplicitInstantiation(ICPPASTElaboratedTypeSpecifier elabSpec) {
	    IASTName name = elabSpec.getName();
	    if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
	    ICPPASTTemplateId id = (ICPPASTTemplateId) name;
	    IBinding template = id.getTemplateName().resolveBinding();
		if (!(template instanceof ICPPClassTemplate))
			return null;  //TODO: problem?

		ICPPClassTemplate classTemplate = (ICPPClassTemplate) template;
		IType[] args = createTypeArray(id.getTemplateArguments());
		if (classTemplate instanceof ICPPInternalTemplateInstantiator) {
		    IBinding binding = ((ICPPInternalTemplateInstantiator) classTemplate).instantiate(args);
		    return binding;
		}
		return null;
	}

	protected static IBinding createClassSpecialization(ICPPASTDeclSpecifier compSpec) {
		IASTName name = null;
		if (compSpec instanceof ICPPASTElaboratedTypeSpecifier)
			name = ((ICPPASTElaboratedTypeSpecifier) compSpec).getName();
		else if (compSpec instanceof ICPPASTCompositeTypeSpecifier)
			name = ((ICPPASTCompositeTypeSpecifier) compSpec).getName();
		else
			return null;

		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
		ICPPASTTemplateId id = (ICPPASTTemplateId) name;

		IBinding binding = id.getTemplateName().resolveBinding();
		if (!(binding instanceof ICPPClassTemplate))
			return null;  //TODO: problem?

		ICPPClassTemplate template = (ICPPClassTemplate) binding;

		IBinding spec = null;
		ICPPASTTemplateDeclaration templateDecl = getTemplateDeclaration(id);
		if (templateDecl instanceof ICPPASTTemplateSpecialization) {
			//specialization
			ICPPTemplateParameter[] templateParams = null;
			try {
				templateParams = template.getTemplateParameters();
			} catch (DOMException e) {
				return e.getProblem();
			}
			IType[] args = createTypeArray(id.getTemplateArguments());
			ObjectMap argMap = new ObjectMap(templateParams.length);
			if (templateParams.length != args.length) {
				return null; //TODO problem
			}
			for (int i = 0; i < templateParams.length; i++) {
				argMap.put(templateParams[i], args[i]);
			}
			spec = ((ICPPInternalTemplateInstantiator) template).getInstance(args);
			if (spec == null) {
				ICPPScope scope = (ICPPScope) CPPVisitor.getContainingScope(id);
				spec = new CPPClassSpecialization(binding, scope, argMap);
				if (template instanceof ICPPInternalTemplate) {
					((ICPPInternalTemplate) template).addSpecialization(args, (ICPPSpecialization) spec);
				}
			}
			if (spec instanceof ICPPInternalBinding) {
				IASTNode parent = id.getParent();
				while (!(parent instanceof IASTDeclSpecifier))
					parent = parent.getParent();
				if (parent instanceof IASTElaboratedTypeSpecifier)
					((ICPPInternalBinding) spec).addDeclaration(id);
				else if (parent instanceof IASTCompositeTypeSpecifier)
					((ICPPInternalBinding) spec).addDefinition(id);
			}
			return spec;
		}
		//else partial specialization
		//CPPClassTemplate template = (CPPClassTemplate) binding;
		ICPPClassTemplatePartialSpecialization[] specializations = null;
		try {
			specializations = template.getPartialSpecializations();
		} catch (DOMException e) {
		}
		if (specializations != null) {
			for (int i = 0; i < specializations.length; i++) {
				if (isSameTemplate(specializations[i], id)) {
					spec = specializations[i];
					break;
				}
			}
		}

		if (spec != null) {
			if (spec instanceof ICPPInternalBinding)
				((ICPPInternalBinding) spec).addDefinition(id);
			return spec;
		}

		spec = new CPPClassTemplatePartialSpecialization(id);
		if (template instanceof ICPPInternalClassTemplate)
			((ICPPInternalClassTemplate) template).addPartialSpecialization((ICPPClassTemplatePartialSpecialization) spec);
		return spec;
	}

	protected static IBinding createFunctionSpecialization(IASTName name) {
		LookupData data = new LookupData(name);
		data.forceQualified = true;
		ICPPScope scope = (ICPPScope) CPPVisitor.getContainingScope(name);
		if (scope instanceof ICPPTemplateScope) {
			try {
				scope = (ICPPScope) scope.getParent();
			} catch (DOMException e) {
			}
		}
		try {
			CPPSemantics.lookup(data, scope);
		} catch (DOMException e) {
			return e.getProblem();
		}

		ICPPFunctionTemplate function = resolveTemplateFunctions((Object[]) data.foundItems, name);
		if (function == null)
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
		if (function instanceof IProblemBinding)
			return function;

		if (name instanceof ICPPASTTemplateId) {
			((ICPPASTTemplateId) name).getTemplateName().setBinding(function);
		}
		IASTNode parent = name.getParent();
		while (parent instanceof IASTName)
			parent = parent.getParent();

		IASTParameterDeclaration[] ps = ((ICPPASTFunctionDeclarator) parent).getParameters();
		Object[] map_types;
		try {
			map_types = deduceTemplateFunctionArguments(function, ps, data.templateArguments);
		} catch (DOMException e) {
			return e.getProblem();
		}
		if (map_types != null) {
		    while (!(parent instanceof IASTDeclaration))
				parent = parent.getParent();

		    ICPPSpecialization spec = null;
		    if (parent.getParent() instanceof ICPPASTExplicitTemplateInstantiation) {
		    	spec = ((ICPPInternalTemplateInstantiator) function).getInstance((IType[]) map_types[1]);
		    	if (spec == null)
		    		spec = (ICPPSpecialization) CPPTemplates.createInstance(scope, function, (ObjectMap) map_types[0], (IType[]) map_types[1]);
		    } else {
		    	spec = ((ICPPInternalTemplateInstantiator) function).getInstance((IType[]) map_types[1]);
		    	if (spec == null) {
		    		if (function instanceof ICPPConstructor)
		    			spec = new CPPConstructorSpecialization(function, scope, (ObjectMap) map_types[0]);
					else if (function instanceof ICPPMethod)
						spec = new CPPMethodSpecialization(function, scope, (ObjectMap) map_types[0]);
					else
						spec = new CPPFunctionSpecialization(function, scope, (ObjectMap) map_types[0]);
		    	}

		    	if (spec instanceof ICPPInternalBinding) {
					if (parent instanceof IASTSimpleDeclaration)
						((ICPPInternalBinding) spec).addDeclaration(name);
					else if (parent instanceof IASTFunctionDefinition)
						((ICPPInternalBinding) spec).addDefinition(name);
		    	}
		    }
		    if (function instanceof ICPPInternalTemplate)
		    	((ICPPInternalTemplate) function).addSpecialization((IType[]) map_types[1], spec);
		    return spec;
		}
		//TODO problem?
		return null;
	}

	static protected ICPPFunctionTemplate resolveTemplateFunctions(Object[] items, IASTName name) {
		if (items == null)
			return null;
		ICPPFunctionTemplate[] templates = null;
		IBinding temp = null;
		for (int i = 0; i < items.length; i++) {
			Object o = items[i];

	        if (o instanceof IASTName) {
	            temp = ((IASTName) o).resolveBinding();
	            if (temp == null)
	                continue;
	        } else if (o instanceof IBinding) {
	            temp = (IBinding) o;
	        } else {
	            continue;
	        }

			if (temp instanceof ICPPTemplateInstance)
				temp = ((ICPPTemplateInstance) temp).getTemplateDefinition();
			if (temp instanceof ICPPFunctionTemplate)
				templates = (ICPPFunctionTemplate[]) ArrayUtil.append(ICPPFunctionTemplate.class, templates, temp);
		}

		if (templates == null)
			return null;

		IType[] templateArguments = null;

		if (name instanceof ICPPASTTemplateId) {
			templateArguments = createTypeArray(((ICPPASTTemplateId) name).getTemplateArguments());
		}
		int numArgs = (templateArguments != null) ? templateArguments.length : 0;


		if (name.getParent() instanceof IASTName)
			name = (IASTName) name.getParent();
		IASTNode n = name.getParent();
		if (n instanceof ICPPASTQualifiedName) {
			n = n.getParent();
		}
		ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) n;
		IType[] functionParameters = createTypeArray(fdtor.getParameters());

		ICPPFunctionTemplate result = null;
		outer: for (int i = 0; i < templates.length && templates[i] != null; i++) {
			ICPPFunctionTemplate tmpl = templates[i];

			ObjectMap map = ObjectMap.EMPTY_MAP;
			try {
				map = deduceTemplateArguments(tmpl, functionParameters);
			} catch (DOMException e) {
			}

			if (map == null)
				continue;
			ICPPTemplateParameter[] params = null;
			try {
				params = tmpl.getTemplateParameters();
			} catch (DOMException e) {
				continue;
			}

			int numParams = params.length;
			IType arg = null;
			for (int j = 0; j < numParams; j++) {
				ICPPTemplateParameter param = params[j];
				if (j < numArgs) {
					arg = templateArguments[j];
				} else {
					arg = null;
				}
				if (map.containsKey(param)) {
					IType t = (IType) map.get(param);
					if (arg == null) {
						arg = t;
					} else if (!t.isSameType(arg)) {
						continue outer;
					}
				} else if (arg == null || !matchTemplateParameterAndArgument(param, arg, map)) {
					continue outer;
				}
			}
			//made it this far, its a match
			if (result != null) {
				return new CPPFunctionTemplate.CPPFunctionTemplateProblem(name, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray());
			}
			result = tmpl;
		}

		return result;
	}

	/**
	 * return Object[] of { ObjectMap, IType[] }
	 * @param primaryTemplate
	 * @param ps
	 * @param specArgs
	 * @return
	 * @throws DOMException
	 */
	static protected Object[] deduceTemplateFunctionArguments(ICPPFunctionTemplate primaryTemplate,
			IASTParameterDeclaration[] ps, IASTNode[] specArgs) throws DOMException
	{
		ICPPTemplateParameter[] templateParameters = primaryTemplate.getTemplateParameters();
		IType[] arguments = createTypeArray(specArgs);
		IType[] result = new IType[templateParameters.length];

		ObjectMap map = null;

		if (arguments.length == result.length) {
			map = new ObjectMap(result.length);
			for (int i = 0; i < templateParameters.length; i++) {
				result[i] = arguments[i];
				map.put(templateParameters, arguments[i]);
			}
			return new Object[] { map, result };
		}

		//else need to deduce some arguments
		IType[] paramTypes = createTypeArray(ps);
		map = deduceTemplateArguments(primaryTemplate, paramTypes);
		if (map != null) {
			for (int i = 0; i < templateParameters.length; i++) {
				ICPPTemplateParameter param = templateParameters[i];
				IType arg = null;
				if (i < arguments.length) {
					arg = arguments[i];
					map.put(param, arg);
				} else if (map.containsKey(param)) {
					arg = (IType) map.get(param);
				}

				if (arg == null || !matchTemplateParameterAndArgument(param, arg, map))
					return null;

				result[i] = arg;
			}
			return new Object[] { map, result };
		}

		return null;
	}

	/**
	 * @param decl
	 * @param arguments
	 * @return
	 */
	public static IBinding createInstance(ICPPScope scope, IBinding decl, ObjectMap argMap, IType[] args) {
		ICPPTemplateInstance instance = null;
		if (decl instanceof ICPPClassType) {
			instance = new CPPClassInstance(scope, decl, argMap, args);
		} else if (decl instanceof ICPPConstructor) {
			instance = new CPPConstructorInstance(scope, decl, argMap, args);
		} else if (decl instanceof ICPPMethod) {
			instance = new CPPMethodInstance(scope, decl, argMap, args);
		} else if (decl instanceof ICPPFunction) {
			instance = new CPPFunctionInstance(scope, decl, argMap, args);
		}
		return instance;
	}

	public static ICPPSpecialization createSpecialization(ICPPScope scope, IBinding decl, ObjectMap argMap) {
		ICPPSpecialization spec = null;
		if (decl instanceof ICPPClassTemplate) {
			spec = new CPPClassTemplateSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPClassType) {
			spec = new CPPClassSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPField) {
			spec = new CPPFieldSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPFunctionTemplate) {
			if (decl instanceof ICPPConstructor)
				spec = new CPPConstructorTemplateSpecialization(decl, scope, argMap);
			else if (decl instanceof ICPPMethod)
				spec = new CPPMethodTemplateSpecialization(decl, scope, argMap);
			else
				spec = new CPPFunctionTemplateSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPConstructor) {
			spec = new CPPConstructorSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPMethod) {
			spec = new CPPMethodSpecialization(decl, scope, argMap);
		} else if (decl instanceof ICPPFunction) {
			spec = new CPPFunctionSpecialization(decl, scope, argMap);
		} else if (decl instanceof ITypedef) {
		    spec = new CPPTypedefSpecialization(decl, scope, argMap);
		}
		return spec;
	}

	/**
	 * @param type
	 * @param arguments
	 */
	public static IType instantiateType(IType type, ObjectMap argMap) {
		if (argMap == null)
			return type;

		IType newType = type;
		IType temp = null;
		if (type instanceof IFunctionType) {
			IType ret = null;
			IType[] params = null;
			try {
				ret = instantiateType(((IFunctionType) type).getReturnType(), argMap);
				IType[] ps = ((IFunctionType) type).getParameterTypes();
				params = new IType[ps.length];
				for (int i = 0; i < params.length; i++) {
					temp = instantiateType(ps[i], argMap);
					params[i] = temp;
				}
			} catch (DOMException e) {
			}
			newType = new CPPFunctionType(ret, params, ((ICPPFunctionType) type).isConst(),
					((ICPPFunctionType) type).isVolatile());
		} else if (type instanceof ITypedef) {
			// Typedef requires special treatment (bug 213861).
			try {
				ITypedef typedef = (ITypedef) type;
				newType = new CPPTypedefSpecialization(typedef, (ICPPScope)	typedef.getScope(), argMap);
			} catch (DOMException e) {
				return type;
			}
		} else if (type instanceof ITypeContainer) {
			try {
				temp = ((ITypeContainer) type).getType();
			} catch (DOMException e) {
				return type;
			}
			newType = instantiateType(temp, argMap);
			if (newType != temp) {
				temp = (IType) type.clone();
				((ITypeContainer) temp).setType(newType);
				newType = temp;
			} else {
				newType = type;
			}
		} else if (type instanceof ICPPTemplateParameter && argMap.containsKey(type)) {
			newType = (IType) argMap.get(type);
		} else if (type instanceof ICPPInternalDeferredClassInstance) {
			newType = ((ICPPInternalDeferredClassInstance) type).instantiate(argMap);
		} else if (type instanceof ICPPInternalUnknown) {
		    IBinding binding;
            try {
                binding = ((ICPPInternalUnknown) type).resolveUnknown(argMap);
            } catch (DOMException e) {
                binding = e.getProblem();
            }
            if (binding instanceof IType)
		        newType = (IType) binding;
		}

		return newType;
	}

	public static ICPPASTTemplateDeclaration getTemplateDeclaration(IASTName name) {
		if (name == null) return null;

		IASTNode parent = name.getParent();
		while (parent instanceof IASTName) {
		    parent = parent.getParent();
		}
		if (parent instanceof IASTDeclSpecifier) {
		    parent = parent.getParent();
		} else {
			while (parent instanceof IASTDeclarator) {
			    parent = parent.getParent();
			}
		}
		if (parent instanceof IASTDeclaration && parent.getParent() instanceof ICPPASTTemplateDeclaration) {
		    parent = parent.getParent();
		} else {
			return null;
		}

		if (parent instanceof ICPPASTTemplateDeclaration) {
		    ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) parent;
		    while (templateDecl.getParent() instanceof ICPPASTTemplateDeclaration)
		        templateDecl = (ICPPASTTemplateDeclaration) templateDecl.getParent();

			IASTName[] ns = null;
			if (name instanceof ICPPASTQualifiedName) {
				ns = ((ICPPASTQualifiedName) name).getNames();
				name = ns[ns.length - 1];
			} else if (name.getParent() instanceof ICPPASTQualifiedName) {
				ns = ((ICPPASTQualifiedName) name.getParent()).getNames();
			}
			if (ns != null) {
				IASTDeclaration currDecl = templateDecl;
				for (int j = 0; j < ns.length; j++) {
					if (ns[j] == name) {
						if (ns[j] instanceof ICPPASTTemplateId || j + 1 == ns.length) {
							if (currDecl instanceof ICPPASTTemplateDeclaration)
								return (ICPPASTTemplateDeclaration) currDecl;
							return null;
						}
					}
					if (ns[j] instanceof ICPPASTTemplateId) {
						if (currDecl instanceof ICPPASTTemplateDeclaration)
							currDecl = ((ICPPASTTemplateDeclaration) currDecl).getDeclaration();
						else
							return null; //??? this would imply bad ast or code
					}
				}
			} else {
				while (templateDecl.getDeclaration() instanceof ICPPASTTemplateDeclaration) {
					templateDecl = (ICPPASTTemplateDeclaration) templateDecl.getDeclaration();
				}
				return templateDecl;
			}
		}
		return  null;
	}

	public static IASTName getTemplateName(ICPPASTTemplateDeclaration templateDecl) {
	    if (templateDecl == null) return null;

	    ICPPASTTemplateDeclaration decl = templateDecl;
		while (decl.getParent() instanceof ICPPASTTemplateDeclaration)
		    decl = (ICPPASTTemplateDeclaration) decl.getParent();

		IASTDeclaration nestedDecl = templateDecl.getDeclaration();
		while (nestedDecl instanceof ICPPASTTemplateDeclaration) {
			nestedDecl = ((ICPPASTTemplateDeclaration) nestedDecl).getDeclaration();
		}

		IASTName name = null;
		if (nestedDecl instanceof IASTSimpleDeclaration) {
		    IASTSimpleDeclaration simple = (IASTSimpleDeclaration) nestedDecl;
		    if (simple.getDeclarators().length == 1) {
				IASTDeclarator dtor = simple.getDeclarators()[0];
				while (dtor.getNestedDeclarator() != null)
					dtor = dtor.getNestedDeclarator();
		        name = dtor.getName();
		    } else if (simple.getDeclarators().length == 0) {
		        IASTDeclSpecifier spec = simple.getDeclSpecifier();
		        if (spec instanceof ICPPASTCompositeTypeSpecifier)
		            name = ((ICPPASTCompositeTypeSpecifier) spec).getName();
		        else if (spec instanceof ICPPASTElaboratedTypeSpecifier)
		            name = ((ICPPASTElaboratedTypeSpecifier) spec).getName();
		    }
		} else if (nestedDecl instanceof IASTFunctionDefinition) {
		    IASTDeclarator declarator = ((IASTFunctionDefinition) nestedDecl).getDeclarator();
		    while (declarator.getNestedDeclarator() != null) {
		    	declarator= declarator.getNestedDeclarator();
		    }
			name = declarator.getName();
		}
		if (name != null) {
		    if (name instanceof ICPPASTQualifiedName) {
				IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
				IASTDeclaration currDecl = decl;
				for (int j = 0; j < ns.length; j++) {
					if (ns[j] instanceof ICPPASTTemplateId || j + 1 == ns.length) {
						if (currDecl == templateDecl) {
							return ns[j];
						}
						if (currDecl instanceof ICPPASTTemplateDeclaration) {
							currDecl = ((ICPPASTTemplateDeclaration) currDecl).getDeclaration();
						} else {
							return null;
						}
					}
				}
		    } else {
		        return name;
		    }
		}

		return  null;
	}

	private static class ClearBindingAction extends CPPASTVisitor {
		public ObjectSet<IBinding> bindings = null;
		public ClearBindingAction(ObjectSet<IBinding> bindings) {
			shouldVisitNames = true;
			shouldVisitStatements = true;
			this.bindings = bindings;
		}
		@Override
		public int visit(IASTName name) {
			if (name.getBinding() != null) {
				IBinding binding = name.getBinding();
				boolean clear = bindings.containsKey(name.getBinding());
				if (!clear && binding instanceof ICPPTemplateInstance) {
					IType[] args = ((ICPPTemplateInstance) binding).getArguments();
					for (int i = 0; i < args.length; i++) {
						if (args[i] instanceof IBinding) {
							if(bindings.containsKey((IBinding)args[i])) {
								clear = true;
								break;
							}
						}
					}
				}
				if (clear) {
					if (binding instanceof ICPPInternalBinding)
						((ICPPInternalBinding) binding).removeDeclaration(name);
					name.setBinding(null);
				}
			}
			return PROCESS_CONTINUE;
		}
		@Override
		public int visit(IASTStatement statement) {
			return PROCESS_SKIP;
		}
	}
	/**
	 * @param definition
	 * @param declarator
	 * @return
	 */
	public static boolean isSameTemplate(ICPPTemplateDefinition definition, IASTName name) {
		ICPPTemplateParameter[] defParams = null;
		try {
			defParams = definition.getTemplateParameters();
		} catch (DOMException e1) {
			return false;
		}
		ICPPASTTemplateDeclaration templateDecl = getTemplateDeclaration(name);
		if (templateDecl == null)
			return false;

		ICPPASTTemplateParameter[] templateParams = templateDecl.getTemplateParameters();
		if (defParams.length != templateParams.length)
			return false;

		ObjectSet<IBinding> bindingsToClear = null;
		for (int i = 0; i < templateParams.length; i++) {
			IASTName tn = getTemplateParameterName(templateParams[i]);
			if (tn.getBinding() != null)
				return (tn.getBinding() == defParams[i]);
			if (bindingsToClear == null)
				bindingsToClear = new ObjectSet<IBinding>(templateParams.length);
			tn.setBinding(defParams[i]);
			if (defParams[i] instanceof ICPPInternalBinding)
				((ICPPInternalBinding) defParams[i]).addDeclaration(tn);
			bindingsToClear.put(defParams[i]);
		}

		boolean result = false;
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTFunctionDeclarator) {
			try {
				IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) parent).getParameters();
				IParameter[] ps = ((ICPPFunction) definition).getParameters();
				if (ps.length == params.length) {
					int i = 0;
					for (; i < ps.length; i++) {
						IType t1 = CPPVisitor.createType(params[i].getDeclarator());
						IType t2 = ps[i].getType();
						if (! t1.isSameType(t2)) {
							break;
						}
					}
					if (i == ps.length)
						result = true;
				}
			} catch (DOMException e) {
			}
		} else if (parent instanceof IASTDeclSpecifier) {
			if (name instanceof ICPPASTTemplateId) {
				if (definition instanceof ICPPClassTemplatePartialSpecialization) {
					ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) definition;
					IASTNode[] args = ((ICPPASTTemplateId) name).getTemplateArguments();
					IType[] specArgs = null;
					try {
						specArgs = spec.getArguments();
					} catch (DOMException e) {
						result = false;
					}
					if (specArgs != null && args.length == specArgs.length) {
						int i = 0;
						for (; i < args.length; i++) {
							IType t1 = specArgs[i];
							IType t2 = CPPVisitor.createType(args[i]);
							if (t1 != null && t2 != null && t1.isSameType(t2))
								continue;
							break;
						}
						result = (i == args.length);
					}
				}
			} else {
				result = CharArrayUtils.equals(definition.getNameCharArray(), name.toCharArray());
			}
		}

		if (bindingsToClear != null && !result) {
			ClearBindingAction action = new ClearBindingAction(bindingsToClear);
			templateDecl.accept(action);
		}

		return result;
	}

	static public IType[] createTypeArray(Object[] params) {
		if (params == null)
			return IType.EMPTY_TYPE_ARRAY;

		if (params instanceof IType[])
			return (IType[]) params;

		IType[] result = new IType[params.length];
		for (int i = 0; i < params.length; i++) {
		    if (params[i] instanceof IASTNode) {
				result[i] = CPPVisitor.createType((IASTNode) params[i]);
			} else if (params[i] instanceof IParameter) {
				try {
					result[i] = ((IParameter) params[i]).getType();
				} catch (DOMException e) {
					result[i] = e.getProblem();
				}
			}
		}
		return result;
	}

	static protected IFunction[] selectTemplateFunctions(
			ObjectSet<IFunction> templates,
			Object[] functionArguments, IASTName name) {
		
		if (templates == null || templates.size() == 0)
			return null;

		IFunction[] instances = null;

		int size = templates.size();

		int numTemplateArgs = 0;
		IASTNode[] templateArguments = null;
		if (name instanceof ICPPASTTemplateId)	{
			templateArguments = ((ICPPASTTemplateId) name).getTemplateArguments();
			numTemplateArgs = templateArguments.length;
		}

		IType[] fnArgs = createTypeArray(functionArguments);

		outer: for (int idx = 0; idx < size; idx++) {
			ICPPFunctionTemplate template = (ICPPFunctionTemplate) templates.keyAt(idx);

			ObjectMap map = null;
			try {
				map = deduceTemplateArguments(template, fnArgs);
			} catch (DOMException e) {
				continue;
			}
			if (map == null)
				continue;

			ICPPTemplateParameter[] templateParams = null;
			try {
				templateParams = template.getTemplateParameters();
			} catch (DOMException e1) {
				continue outer;
			}
			int numTemplateParams = templateParams.length;

			IType[] instanceArgs = null;
			for (int i = 0; i < numTemplateParams; i++) {
				IType arg = (i < numTemplateArgs) ? CPPVisitor.createType(templateArguments[i]) : null;
				IType mapped = (IType) map.get(templateParams[i]);

				if (arg != null && mapped != null) {
					if (arg.isSameType(mapped))
						instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, arg);
					else
						continue outer;
				} else if (arg == null && mapped == null) {
				    IType def = null;
				    try {
					    if (templateParams[i] instanceof ICPPTemplateTypeParameter) {
	                        def = ((ICPPTemplateTypeParameter) templateParams[i]).getDefault();
					    } else if (templateParams[i] instanceof ICPPTemplateTemplateParameter) {
					        def = ((ICPPTemplateTemplateParameter) templateParams[i]).getDefault();
					    } else if (templateParams[i] instanceof ICPPTemplateNonTypeParameter) {
					        def = CPPVisitor.getExpressionType(((ICPPTemplateNonTypeParameter) templateParams[i]).getDefault());
					    }
				    } catch (DOMException e) {
				        continue outer;
				    }
				    if (def != null) {
				        if (def instanceof ICPPTemplateParameter) {
				            for (int j = 0; j < i; j++) {
                                if (templateParams[j] == def) {
                                    def = instanceArgs[j];
                                }
                            }
				        }
				        instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, def);
				    } else {
				        continue outer;
				    }
				} else {
					instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, (arg != null) ? arg : mapped);
				}
			}
			instanceArgs  = (IType[]) ArrayUtil.trim(IType.class, instanceArgs);
			ICPPSpecialization temp = (ICPPSpecialization) ((ICPPInternalTemplateInstantiator) template).instantiate(instanceArgs);
			if (temp != null)
				instances = (IFunction[]) ArrayUtil.append(IFunction.class, instances, temp);
		}

		return (IFunction[]) ArrayUtil.trim(IFunction.class, instances);
	}

	/**
	 *
	 * @param template
	 * @param args
	 * @return
	 *
	 * A type that is specified in terms of template parameters (P) is compared with an actual
	 * type (A), and an attempt is made to find template argument vaules that will make P,
	 * after substitution of the deduced values, compatible with A.
	 * @throws DOMException
	 */
	static private ObjectMap deduceTemplateArguments(ICPPFunctionTemplate template, IType[] arguments) throws DOMException{
		ICPPFunction function = (ICPPFunction) template;
		IParameter[] functionParameters = null;
		try {
			functionParameters = function.getParameters();
		} catch (DOMException e) {
			return null;
		}
		if (arguments == null /*|| functionParameters.length != arguments.length*/) {
			return null;
		}

		int numParams = functionParameters.length;
		int numArgs = arguments.length;
		ObjectMap map = new ObjectMap(numParams);
		for (int i = 0; i < numArgs && i < numParams; i++) {
			if (!deduceTemplateArgument(map, functionParameters[i].getType(), arguments[i])) {
				return null;
			}
		}

		return map;
	}

	/**
	 * 14.8.2.1-2 If P is a cv-qualified type, the top level cv-qualifiers of P's type are ignored for type
	 * deduction.  If P is a reference type, the type referred to by P is used for Type deduction.
	 * @param pSymbol
	 * @return
	 */
	static private IType getParameterTypeForDeduction(IType pType) {
		IType result = pType;
		try {
			if (pType instanceof IQualifierType) {
				result = ((IQualifierType) pType).getType();
			} else if (pType instanceof ICPPReferenceType) {
				result = ((ICPPReferenceType) pType).getType();
			} else if (pType instanceof  CPPPointerType) {
				result = ((CPPPointerType) pType).stripQualifiers();
			}
		} catch (DOMException e) {
			result = e.getProblem();
		}
		return result;
	}

	/**
	 * 14.8.2.1-2
	 * if P is not a reference type
	 * - If A is an array type, the pointer type produced by the array-to-pointer conversion is used instead
	 * - If A is a function type, the pointer type produced by the function-to-pointer conversion is used instead
	 * - If A is a cv-qualified type, the top level cv-qualifiers are ignored for type deduction
	 * @param aInfo
	 * @return
	 */
	static private IType getArgumentTypeForDeduction(IType aType, boolean pIsAReferenceType) {
		if (aType instanceof ICPPReferenceType) {
		    try {
                aType = ((ICPPReferenceType) aType).getType();
            } catch (DOMException e) {
            }
		}
		IType result = aType;
		if (!pIsAReferenceType) {
			try {
				if (aType instanceof IArrayType) {
					result = new CPPPointerType(((IArrayType) aType).getType());
				} else if (aType instanceof IFunctionType) {
					result = new CPPPointerType(aType);
				} else if (aType instanceof IQualifierType) {
					result = ((IQualifierType) aType).getType();
				} else if (aType instanceof CPPPointerType) {
					result = ((CPPPointerType) aType).stripQualifiers();
				}
			} catch (DOMException e) {
				result = e.getProblem();
			}
		}

		return result;
	}

	static private boolean expressionsEquivalent(IASTExpression p, IASTExpression a) {
		if (p == null)
			return true;

		if (p instanceof IASTLiteralExpression && a instanceof IASTLiteralExpression) {
			return p.toString().equals(a.toString ());
		}
		return false;
	}
	static public boolean deduceTemplateArgument(ObjectMap map, IType p, IType a) throws DOMException {
		boolean pIsAReferenceType = (p instanceof ICPPReferenceType);
		p = getParameterTypeForDeduction(p);
		a = getArgumentTypeForDeduction(a, pIsAReferenceType);

		if (p instanceof IBasicType) {
			if (p.isSameType(a) && a instanceof IBasicType) {
				return expressionsEquivalent(((IBasicType) p).getValue(), ((IBasicType) a).getValue());
			}
		} else {
			while (p != null) {
				while (a instanceof ITypedef)
					a = ((ITypedef) a).getType();
				if (p instanceof IBasicType) {
					return p.isSameType(a);
				} else if (p instanceof ICPPPointerToMemberType) {
					if (!(a instanceof ICPPPointerToMemberType))
						return false;

					if (!deduceTemplateArgument(map, ((ICPPPointerToMemberType) p).getMemberOfClass(), ((ICPPPointerToMemberType) a).getMemberOfClass()))
						return false;

					p = ((ICPPPointerToMemberType) p).getType();
					p = ((ICPPPointerToMemberType) a).getType();
				} else if (p instanceof IPointerType) {
					if (!(a instanceof IPointerType)) {
						return false;
					}
					p = ((IPointerType) p).getType();
					a = ((IPointerType) a).getType();
				} else if (p instanceof IQualifierType) {
					if (!(a instanceof IQualifierType))
						return false;
					a = ((IQualifierType) a).getType(); //TODO a = strip qualifiers from p out of a
					p = ((IQualifierType) p).getType();
				} else if (p instanceof IFunctionType) {
					if (!(a instanceof IFunctionType))
						return false;
					if (!deduceTemplateArgument(map, ((IFunctionType) p).getReturnType(), ((IFunctionType) a).getReturnType()))
						return false;
					IType[] pParams = ((IFunctionType) p).getParameterTypes();
					IType[] aParams = ((IFunctionType) a).getParameterTypes();
					if (pParams.length != aParams.length)
						return false;
					for (int i = 0; i < pParams.length; i++) {
						if (!deduceTemplateArgument(map, pParams[i], aParams[i]))
							return false;
					}
					return true;
				} else if (p instanceof ICPPTemplateParameter) {
					if (map.containsKey(p)) {
						IType current = (IType) map.get(p);
						return current.isSameType(a);
					}
					if (a == null)
						return false;
					map.put(p, a);
					return true;
				} else if (p instanceof ICPPTemplateInstance) {
					if (!(a instanceof ICPPTemplateInstance))
						return false;
					ICPPTemplateInstance pInst = (ICPPTemplateInstance) p;
					ICPPTemplateInstance aInst = (ICPPTemplateInstance) a;

					IType[] pArgs = createTypeArray(pInst.getArguments());
					ObjectMap aMap = aInst.getArgumentMap();
					if (aMap != null && !(aInst.getTemplateDefinition() instanceof ICPPClassTemplatePartialSpecialization)) {
						ICPPTemplateParameter[] aParams = aInst.getTemplateDefinition().getTemplateParameters();
						if (pArgs.length != aParams.length)
							return false;
						for (int i = 0; i < pArgs.length; i++) {
							IType t = (IType) aMap.get(aParams[i]);
							if (t == null || !deduceTemplateArgument(map, pArgs[i], t))
								return false;
						}
					} else {
						IType[] aArgs = createTypeArray(aInst.getArguments());
						if (aArgs.length != pArgs.length)
							return false;
						for (int i = 0; i < pArgs.length; i++) {
							if (!deduceTemplateArgument(map, pArgs[i], aArgs[i]))
								return false;
						}
					}
					return true;
				} else {
					return p.isSameType(a);
				}
			}
		}

		return false;
	}

	/**
	 * transform a function template for use in partial ordering, as described in the
	 * spec 14.5.5.2-3
	 * @param template
	 * @return
	 * -for each type template parameter, synthesize a unique type and substitute that for each
	 * occurrence of that parameter in the function parameter list
	 * -for each non-type template parameter, synthesize a unique value of the appropriate type and
	 * substitute that for each occurrence of that parameter in the function parameter list
	 * for each template template parameter, synthesize a unique class template and substitute that
	 * for each occurrence of that parameter in the function parameter list
	 * @throws DOMException
	 */

	static private IType[] createArgsForFunctionTemplateOrdering(ICPPFunctionTemplate template) throws DOMException{
		ICPPTemplateParameter[] paramList = template.getTemplateParameters();
		int size = paramList.length;
		IType[] args = new IType[size];
		for (int i = 0; i < size; i++) {
			ICPPTemplateParameter param = paramList[i];
			if (param instanceof ICPPTemplateNonTypeParameter) {
				IType t = ((ICPPTemplateNonTypeParameter) param).getType();
				if (t instanceof CPPBasicType) {
					CPPASTLiteralExpression exp = new CPPASTLiteralExpression();
					exp.setValue(String.valueOf(i));
					CPPBasicType temp = (CPPBasicType) t.clone();
					temp.setValue(exp);
					args[i] = temp;
				}
			} else {
				args[i] = new CPPBasicType(-1, 0);
			}
		}
		return args;
	}

	static protected int orderTemplateFunctions(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2) throws DOMException {
		//Using the transformed parameter list, perform argument deduction against the other
		//function template
		IType[] args = createArgsForFunctionTemplateOrdering(f1);
		ICPPFunction function = (ICPPFunction) ((ICPPInternalTemplateInstantiator) f1).instantiate(args);

		ObjectMap m1 = null;
		if (function != null)
			m1 = deduceTemplateArguments(f2, function.getType().getParameterTypes());

		args = createArgsForFunctionTemplateOrdering(f2);
		function = (ICPPFunction) ((ICPPInternalTemplateInstantiator) f2).instantiate(args);

		ObjectMap m2 = null;
		if (function != null)
			m2 = deduceTemplateArguments(f1, function.getType().getParameterTypes());

		//The transformed  template is at least as specialized as the other iff the deduction
		//succeeds and the deduced parameter types are an exact match
		//A template is more specialized than another iff it is at least as specialized as the
		//other template and that template is not at least as specialized as the first.
		boolean d1 = (m1 != null);
		boolean d2 = (m2 != null);

		if (d1 && d2 || !d1 && !d2)
			return 0;
		else if (d1 && !d2)
			return 1;
		else
			return -1;
	}

	static public ICPPTemplateDefinition matchTemplatePartialSpecialization(ICPPClassTemplate template, IType[] args) throws DOMException{
		if (template == null) {
			return null;
		}

		ICPPClassTemplatePartialSpecialization[] specializations = template.getPartialSpecializations();
		int size = (specializations != null) ? specializations.length : 0;
		if (size == 0) {
			return template;
		}

		ICPPClassTemplatePartialSpecialization bestMatch = null, spec = null;
		boolean bestMatchIsBest = true;
		IType[] specArgs = null;
		for (int i = 0; i < size; i++) {
			spec = specializations[i];
			specArgs = spec.getArguments();
			if (specArgs == null || specArgs.length != args.length) {
				continue;
			}

			int specArgsSize = specArgs.length;
			ObjectMap map = new ObjectMap(specArgsSize);
			IType t1 = null, t2 = null;

			boolean match = true;
			for (int j = 0; j < specArgsSize; j++) {
				t1 = specArgs[j];
				t2 = args[j];

				if (!deduceTemplateArgument(map, t1, t2)) {
					match = false;
					break;
				}
			}
			if (match) {
				int compare = orderSpecializations(bestMatch, spec);
				if (compare == 0) {
					bestMatchIsBest = false;
				} else if (compare < 0) {
					bestMatch = spec;
					bestMatchIsBest = true;
				}
			}
		}

		//14.5.4.1 If none of the specializations is more specialized than all the other matching
		//specializations, then the use of the class template is ambiguous and the program is ill-formed.
		if (!bestMatchIsBest) {
			//TODO problem
			return new CPPTemplateDefinition.CPPTemplateProblem(null, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, null);
		}

		return bestMatch;
	}

	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @return
	 * @throws DOMException
	 */
	static private int orderSpecializations(ICPPClassTemplatePartialSpecialization spec1, ICPPClassTemplatePartialSpecialization spec2) throws DOMException {
		if (spec1 == null) {
			return -1;
		}

		//to order class template specializations, we need to transform them into function templates
		ICPPFunctionTemplate template1 = null, template2 = null;

		if (spec1 instanceof ICPPClassType) {
			template1 = classTemplateSpecializationToFunctionTemplate(spec1);
			template2 = classTemplateSpecializationToFunctionTemplate(spec2);
		} else if (spec1 instanceof ICPPFunction) {
			template1 = (ICPPFunctionTemplate) spec1;
			template2 = (ICPPFunctionTemplate) spec2;
		}

		return orderTemplateFunctions(template1, template2);
	}

	public static final class CPPImplicitFunctionTemplate extends CPPFunctionTemplate {
		IParameter[] functionParameters = null;
		ICPPTemplateParameter[] templateParameters = null;
		/**
		 * @param name
		 */
		public CPPImplicitFunctionTemplate(ICPPTemplateParameter[] templateParameters, IParameter[] functionParameters) {
			super(null);
			this.functionParameters = functionParameters;
			this.templateParameters = templateParameters;
		}
		@Override
		public IParameter[] getParameters() {
			return functionParameters;
		}
		@Override
		public ICPPTemplateParameter[] getTemplateParameters() {
			return templateParameters;
		}
		@Override
		public IScope getScope() {
			return null;
		}
		@Override
		public IFunctionType getType() {
			if (type == null) {
				type = CPPVisitor.createImplicitFunctionType(new CPPBasicType(IBasicType.t_void, 0), functionParameters);
			}
			return type;
		}
	}
	/**
	 * transform the class template to a function template as described in the spec
	 * 14.5.4.2-1
	 * @param template
	 * @return IParameterizedSymbol
	 * the function template has the same template parameters as the partial specialization and
	 * has a single function parameter whose type is a class template specialization with the template
	 * arguments of the partial specialization
	 */
	static private ICPPFunctionTemplate classTemplateSpecializationToFunctionTemplate(ICPPClassTemplatePartialSpecialization specialization) {
		if (!(specialization instanceof ICPPClassType))
			return null;

		ICPPTemplateDefinition template = specialization;
		IType[] args = null;
		try {
			args = specialization.getArguments();
		} catch (DOMException e1) {
			return null;
		}

		IType paramType = (IType) ((ICPPInternalTemplateInstantiator) template).instantiate(args);
		IParameter[] functionParameters = new IParameter[] { new CPPParameter(paramType) };

		try {
			return new CPPImplicitFunctionTemplate(specialization.getTemplateParameters(), functionParameters);
		} catch (DOMException e) {
			return null;
		}
	}

	static private boolean isValidArgument(ICPPTemplateParameter param, IType argument) {
		//TODO
		return true;
	}

	static protected boolean matchTemplateParameterAndArgument(ICPPTemplateParameter param, IType argument, ObjectMap map) {
		if (!isValidArgument(param, argument)) {
			return false;
		}
		if (param instanceof ICPPTemplateTypeParameter) {
			return true;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (!(argument instanceof ICPPTemplateDefinition))
				return false;

			ICPPTemplateParameter[] pParams = null, aParams = null;
			try {
				pParams = ((ICPPTemplateTemplateParameter) param).getTemplateParameters();
				aParams = ((ICPPTemplateDefinition) argument).getTemplateParameters();
			} catch (DOMException e) {
				return false;
			}


			int size = pParams.length;
			if (aParams.length != size) {
				return false;
			}

			for (int i = 0; i < size; i++) {
				if ((pParams[i] instanceof ICPPTemplateTypeParameter && !(aParams[i] instanceof ICPPTemplateTypeParameter)) ||
					(pParams[i] instanceof ICPPTemplateTemplateParameter && !(aParams[i] instanceof ICPPTemplateTemplateParameter)) ||
					(pParams[i] instanceof ICPPTemplateNonTypeParameter && !(aParams[i] instanceof ICPPTemplateNonTypeParameter)))
				{
					return false;
				}
			}

			return true;
		} else {
			try {
				IType pType = ((ICPPTemplateNonTypeParameter) param).getType();
				if (map != null && pType != null && map.containsKey(pType)) {
					pType = (IType) map.get(pType);
				}

				//14.1s8 function to pointer and array to pointer conversions
				if (pType instanceof IFunctionType)
			    {
					pType = new CPPPointerType(pType);
			    } else if (pType instanceof IArrayType) {
			    	try {
			    		pType = new CPPPointerType(((IArrayType) pType).getType());
					} catch (DOMException e) {
						pType = e.getProblem();
					}
				}
				Cost cost = Conversions.checkStandardConversionSequence(argument, pType, false);

				if (cost == null || cost.rank == Cost.NO_MATCH_RANK) {
					return false;
				}
			} catch(DOMException e) {
				return false;
			}
		}
		return true;
	}

	public static IBinding instantiateWithinClassTemplate(ICPPClassTemplate template) throws DOMException {
		IType[] args = null;
		if (template instanceof ICPPClassTemplatePartialSpecialization) {
			args = ((ICPPClassTemplatePartialSpecialization) template).getArguments();
		} else {
			ICPPTemplateParameter[] templateParameters = template.getTemplateParameters();
			args = new IType[templateParameters.length];
			for (int i = 0; i < templateParameters.length; i++) {
				if (templateParameters[i] instanceof IType) {
					args[i] = (IType) templateParameters[i];
				} else if (templateParameters[i] instanceof ICPPTemplateNonTypeParameter) {
					args[i] = ((ICPPTemplateNonTypeParameter) templateParameters[i]).getType();
				}
			}
		}

		if (template instanceof ICPPInternalTemplateInstantiator) {
			return ((ICPPInternalTemplateInstantiator) template).instantiate(args);
		}
		return template;
	}

	public static boolean typeContainsTemplateParameter(IType t) {
		if (t instanceof ICPPTemplateParameter)
			return true;
		t = SemanticUtil.getUltimateType(t, false);
		return (t instanceof ICPPTemplateParameter);
	}

	public static IBinding instantiateTemplate(ICPPTemplateDefinition template, IType[] arguments,
			ObjectMap specializedArgs) {
		ICPPTemplateParameter[] parameters = null;
		try {
			parameters = template.getTemplateParameters();
		} catch (DOMException e1) {
			return e1.getProblem();
		}

		int numParams = (parameters != null) ? parameters.length : 0;
		int numArgs = arguments.length;

		if (numParams == 0) {
			return null;
		}

		ObjectMap map = new ObjectMap(numParams);
		ICPPTemplateParameter param = null;
		IType arg = null;
		IType[] actualArgs = new IType[numParams];
		boolean argsContainTemplateParameters = false;

		for (int i = 0; i < numParams; i++) {
			arg = null;
			param = parameters[i];

			if (i < numArgs) {
				arg = arguments[i];
			} else {
				IType defaultType = null;
				try {
					if (param instanceof ICPPTemplateTypeParameter)
						defaultType = ((ICPPTemplateTypeParameter) param).getDefault();
					else if (param instanceof ICPPTemplateTemplateParameter)
						defaultType = ((ICPPTemplateTemplateParameter) param).getDefault();
					else if (param instanceof ICPPTemplateNonTypeParameter)
						defaultType = CPPVisitor.getExpressionType(((ICPPTemplateNonTypeParameter) param).getDefault());
				} catch (DOMException e) {
					defaultType = e.getProblem();
				}
				if (defaultType != null) {
					if (defaultType instanceof ICPPTemplateParameter) {
						if (map.containsKey(defaultType)) {
							arg = (IType) map.get(defaultType);
						}
					} else if (defaultType instanceof ICPPInternalDeferredClassInstance) {
						// A default template parameter may be depend on a previously defined
						// parameter: template<typename T1, typename T2 = A<T1> > class B {};
						arg = ((ICPPInternalDeferredClassInstance) defaultType).instantiate(map);
					} else {
					    arg = defaultType;
					}
				} else {
					//TODO problem
					return null;
				}
			}

			if (CPPTemplates.matchTemplateParameterAndArgument(param, arg, map)) {
				if (!param.equals(arg)) {
					map.put(param, arg);
				}
				actualArgs[i] = arg;
				if (typeContainsTemplateParameter(arg)) {
					argsContainTemplateParameters = true;
				}
			} else {
				//TODO problem
				return null;
			}
		}

		if (map.isEmpty()) {
			map = null;
		}
		if (argsContainTemplateParameters) {
			return ((ICPPInternalTemplateInstantiator) template).deferredInstance(map, arguments);
		}

		ICPPSpecialization instance = ((ICPPInternalTemplateInstantiator) template).getInstance(actualArgs);
		if (instance != null) {
			return instance;
		}

		if (specializedArgs != null) {
			for (int i = 0; i < specializedArgs.size(); i++) {
				map.put(specializedArgs.keyAt(i), specializedArgs.getAt(i));
			}
		}

		ICPPScope scope = null;
		try {
			scope = (ICPPScope) template.getScope();
		} catch (DOMException e) {
			return e.getProblem();
		}
		instance = (ICPPTemplateInstance) CPPTemplates.createInstance(scope, template, map, arguments);
		if (template instanceof ICPPInternalTemplate)
			((ICPPInternalTemplate) template).addSpecialization(arguments, instance);

		return instance;
	}

	/**
	 * Returns an array of specialized bases. The bases will be specialized versions of
	 * the template instances associated specialized bindings bases.
	 * binding.
	 * @param classInstance
	 * @return
	 * @throws DOMException
	 */
	public static ICPPBase[] getBases(ICPPTemplateInstance classInstance) throws DOMException {
		assert classInstance instanceof ICPPClassType;
		ICPPBase[] pdomBases = ((ICPPClassType) classInstance.getTemplateDefinition()).getBases();

		if (pdomBases != null) {
			ICPPBase[] result = null;

			for (int i = 0; i < pdomBases.length; i++) {
				ICPPBase origBase = pdomBases[i];
				ICPPBase specBase = (ICPPBase) ((ICPPInternalBase) origBase).clone();
				IBinding origClass = origBase.getBaseClass();
				if (origClass instanceof IType) {
					IType specClass = CPPTemplates.instantiateType((IType) origClass, classInstance.getArgumentMap());
					specClass = SemanticUtil.getUltimateType(specClass, true);
					if (specClass instanceof IBinding) {
						((ICPPInternalBase) specBase).setBaseClass((IBinding) specClass);
					}
					result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBase);
				}
			}

			return (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
		}

		return new ICPPBase[0];
	}
}
