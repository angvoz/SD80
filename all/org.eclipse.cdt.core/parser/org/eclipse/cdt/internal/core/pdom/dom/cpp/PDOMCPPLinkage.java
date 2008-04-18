/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPLinkage extends PDOMLinkage implements IIndexCPPBindingConstants {
	public PDOMCPPLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_NAME, CPP_LINKAGE_NAME.toCharArray());
	}

	public String getLinkageName() {
		return CPP_LINKAGE_NAME;
	}

	public int getLinkageID() {
		return CPP_LINKAGE_ID;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return LINKAGE;
	}

	// Binding types
	
	private class ConfigureTemplate implements Runnable {
		ICPPTemplateDefinition template;
		
		public ConfigureTemplate(ICPPTemplateDefinition template) {
			this.template = template;
		}
		
		public void run() {
			try {
				ICPPTemplateParameter[] params = template.getTemplateParameters();
				for (int i = 0; i < params.length; i++) {
					if (params[i] != null && !(params[i] instanceof ProblemBinding)) {
						addBinding(params[i], null);
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				template = null;
			}
		}
	}
	
	private class ConfigurePartialSpecialization implements Runnable {
		PDOMCPPClassTemplatePartialSpecialization partial;
		ICPPClassTemplatePartialSpecialization binding;
		
		public ConfigurePartialSpecialization(PDOMCPPClassTemplatePartialSpecialization partial,
				ICPPClassTemplatePartialSpecialization binding) {
			this.partial = partial;
			this.binding = binding;
		}
		
		public void run() {
			try {
				IType[] args = binding.getArguments();
				for (IType arg : args) {
					partial.addArgument(arg);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				partial = null;
				binding = null;
			}
		}
	}
	
	private class ConfigureFunctionTemplate implements Runnable {
		PDOMCPPFunctionTemplate template;
		ICPPFunction function;
		
		public ConfigureFunctionTemplate(PDOMCPPFunctionTemplate template, ICPPFunction binding) {
			this.template = template;
			this.function = binding;
		}
		
		public void run() {
			try {
				template.initData((ICPPFunctionType) function.getType(), function.getParameters());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				template = null;
				function = null;
			}
		}
	}
	
	List<Runnable> postProcesses = new ArrayList<Runnable>();
	
	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null)
			return null;
		
		IBinding binding = name.resolveBinding();

		if (binding == null || binding instanceof IProblemBinding) {
			// Can't tell what it is
			return null;
		}

		if (binding instanceof IParameter)
			// Skip parameters (TODO and others I'm sure)
			return null;

		PDOMBinding pdomBinding = addBinding(binding, name);
		if (pdomBinding instanceof PDOMCPPClassType || pdomBinding instanceof PDOMCPPClassSpecialization) {
			if (binding instanceof ICPPClassType && name.isDefinition()) {
				addImplicitMethods(pdomBinding, (ICPPClassType) binding);
			}
		}
		
		handlePostProcesses();
		
		return pdomBinding;
	}

	@Override
	public PDOMBinding addBinding(IBinding binding, IASTName fromName) throws CoreException {
		// assign names to anonymous types.
		binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(binding);
		if (binding == null) 
			return null;
		
		final PDOMNode parent= getAdaptedParent(binding, true);
		if (parent == null)
			return null;
		
		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding != null) {
			if (shouldUpdate(pdomBinding, fromName)) {
				pdomBinding.update(this, fromName.getBinding());
			}
		} else {
			try {
				if (binding instanceof ICPPSpecialization) {
					IBinding specialized= ((ICPPSpecialization)binding).getSpecializedBinding();
					addBinding(specialized, null);
				}
		
				pdomBinding = addBinding(parent, binding);
				if ((pdomBinding instanceof PDOMCPPClassInstance || pdomBinding instanceof PDOMCPPDeferredClassInstance) && binding instanceof ICPPClassType) {
					// Add instantiated constructors to the index (bug 201174).
					addConstructors(pdomBinding, (ICPPClassType) binding);
					if(SemanticUtil.ENABLE_224364) {
						addConversionOperators(pdomBinding, (ICPPClassType) binding);
					}
				}
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
		return pdomBinding;
	}

	private void addConstructors(PDOMBinding pdomBinding, ICPPClassType binding)
			throws DOMException, CoreException {
		for(ICPPConstructor ctr : binding.getConstructors())
			addBinding(ctr, null);
	}
	
	private void addConversionOperators(PDOMBinding pdomBinding, ICPPClassType binding)
	throws DOMException, CoreException {
		for(ICPPMethod conv : SemanticUtil.getDeclaredConversionOperators(binding))
			addBinding(conv, null);
	}

	private boolean shouldUpdate(PDOMBinding pdomBinding, IASTName fromName) throws CoreException {
		if (fromName != null) {
			if (fromName.isReference()) {
				return false;
			}
			if (pdomBinding instanceof ICPPMember) {
				IASTNode node= fromName.getParent();
				while (node != null) {
					if (node instanceof IASTCompositeTypeSpecifier) {
						return true;
					}
					node= node.getParent();
				}
				return false;
			}
			if (fromName.isDefinition()) {
				return true;
			}
			return !pdomBinding.hasDefinition();
		}
		return false;
	}

	private PDOMBinding addBinding(PDOMNode parent, IBinding binding) throws CoreException, DOMException {
		PDOMBinding pdomBinding= null;

		if (binding instanceof ICPPSpecialization) {
			IBinding specialized = ((ICPPSpecialization)binding).getSpecializedBinding();
			PDOMBinding pdomSpecialized= adaptBinding(specialized);
			if (pdomSpecialized == null)
				return null;
			if (binding instanceof ICPPDeferredTemplateInstance) {
				if (binding instanceof ICPPFunction && pdomSpecialized instanceof ICPPFunctionTemplate) {
					pdomBinding = new PDOMCPPDeferredFunctionInstance(pdom,
							parent, (ICPPFunction) binding, pdomSpecialized);	
				} else if (binding instanceof ICPPClassType && pdomSpecialized instanceof ICPPClassTemplate) {
					pdomBinding = new PDOMCPPDeferredClassInstance(pdom,
							parent, (ICPPClassType) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPTemplateInstance) {
				if (binding instanceof ICPPConstructor && pdomSpecialized instanceof ICPPConstructor) {
					pdomBinding = new PDOMCPPConstructorInstance(pdom, parent,
							(ICPPConstructor) binding, pdomSpecialized);
				} else if (binding instanceof ICPPMethod && pdomSpecialized instanceof ICPPMethod) {
					pdomBinding = new PDOMCPPMethodInstance(pdom, parent,
							(ICPPMethod) binding, pdomSpecialized);
				} else if (binding instanceof ICPPFunction && pdomSpecialized instanceof ICPPFunction) {
					pdomBinding = new PDOMCPPFunctionInstance(pdom, parent,
							(ICPPFunction) binding, pdomSpecialized);
				} else if (binding instanceof ICPPClassType && pdomSpecialized instanceof ICPPClassType) {
					pdomBinding = new PDOMCPPClassInstance(pdom, parent,
							(ICPPClassType) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization &&
					pdomSpecialized instanceof PDOMCPPClassTemplate) {
				pdomBinding = new PDOMCPPClassTemplatePartialSpecialization(
						pdom, parent, (ICPPClassTemplatePartialSpecialization) binding,
						(PDOMCPPClassTemplate) pdomSpecialized);
			} else if (binding instanceof ICPPField) {
				pdomBinding = new PDOMCPPFieldSpecialization(pdom, parent,
						(ICPPField) binding, pdomSpecialized);
			} else if (binding instanceof ICPPFunctionTemplate) {
				if (binding instanceof ICPPConstructor) {
					pdomBinding = new PDOMCPPConstructorTemplateSpecialization(
							pdom, parent, (ICPPConstructor) binding, pdomSpecialized);
				} else if (binding instanceof ICPPMethod) {
					pdomBinding = new PDOMCPPMethodTemplateSpecialization(
							pdom, parent, (ICPPMethod) binding, pdomSpecialized);
				} else if (binding instanceof ICPPFunction) {
					pdomBinding = new PDOMCPPFunctionTemplateSpecialization(
							pdom, parent, (ICPPFunctionTemplate) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPConstructor) {
				pdomBinding = new PDOMCPPConstructorSpecialization(pdom, parent,
						(ICPPConstructor) binding, pdomSpecialized);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding = new PDOMCPPMethodSpecialization(pdom, parent,
						(ICPPMethod) binding, pdomSpecialized);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding = new PDOMCPPFunctionSpecialization(pdom, parent,
						(ICPPFunction) binding, pdomSpecialized);
			} else if (binding instanceof ICPPClassTemplate) {
				pdomBinding = new PDOMCPPClassTemplateSpecialization(pdom, parent,
						(ICPPClassTemplate) binding, pdomSpecialized);
			} else if (binding instanceof ICPPClassType) {
				pdomBinding = new PDOMCPPClassSpecialization(pdom, parent,
						(ICPPClassType) binding, pdomSpecialized);
			} else if (binding instanceof ITypedef) {
				pdomBinding = new PDOMCPPTypedefSpecialization(pdom, parent,
						(ITypedef) binding, pdomSpecialized);
			}
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter) {
				pdomBinding = new PDOMCPPTemplateTypeParameter(pdom, parent, (ICPPTemplateTypeParameter)binding);
			} 
// TODO other template parameter types
//			else if (binding instanceof ICPPTemplateTemplateParameter) {
//				pdomBinding = new PDOMCPPTemplateTemplateParameter(pdom, parent, (ICPPTemplateTemplateParameter)binding);
//			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
//				pdomBinding = new PDOMCPPTemplateNonTypeParameter(pdom, parent, (ICPPTemplateNonTypeParameter)binding);
//			}
		} else if (binding instanceof ICPPField) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPField(pdom, parent, (ICPPField) binding);
			}
		} else if (binding instanceof ICPPVariable) {
			if (!(binding.getScope() instanceof CPPBlockScope)) {
				ICPPVariable var= (ICPPVariable) binding;
				pdomBinding = new PDOMCPPVariable(pdom, parent, var);
			}
		} else if (binding instanceof ICPPFunctionTemplate) {
			if (binding instanceof ICPPConstructor) {
				pdomBinding= new PDOMCPPConstructorTemplate(pdom, parent, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding= new PDOMCPPMethodTemplate(pdom, parent, (ICPPMethod) binding);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding= new PDOMCPPFunctionTemplate(pdom, parent, (ICPPFunctionTemplate) binding);
			}
		} else if (binding instanceof ICPPConstructor) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPConstructor(pdom, parent, (ICPPConstructor)binding);
			}
		} else if (binding instanceof ICPPMethod) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPMethod(pdom, parent, (ICPPMethod)binding);
			}
		} else if (binding instanceof ICPPFunction) {
			pdomBinding = new PDOMCPPFunction(pdom, parent, (ICPPFunction) binding, true);
		} else if (binding instanceof ICPPClassTemplate) {
			pdomBinding= new PDOMCPPClassTemplate(pdom, parent, (ICPPClassTemplate) binding);
		} else if (binding instanceof ICPPClassType) {
			pdomBinding= new PDOMCPPClassType(pdom, parent, (ICPPClassType) binding);
		} else if (binding instanceof ICPPNamespaceAlias) {
			pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, (ICPPNamespaceAlias) binding);
		} else if (binding instanceof ICPPNamespace) {
			pdomBinding = new PDOMCPPNamespace(pdom, parent, (ICPPNamespace) binding);
		} else if (binding instanceof ICPPUsingDeclaration) {
			pdomBinding = new PDOMCPPUsingDeclaration(pdom, parent, (ICPPUsingDeclaration) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCPPEnumeration(pdom, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
			PDOMBinding pdomEnumeration = adaptBinding(enumeration);
			if (pdomEnumeration instanceof PDOMCPPEnumeration) {
				pdomBinding = new PDOMCPPEnumerator(pdom, parent, (IEnumerator) binding,
						(PDOMCPPEnumeration)pdomEnumeration);
			}
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCPPTypedef(pdom, parent, (ITypedef)binding);
		}

		if (pdomBinding != null) {
			pdomBinding.setLocalToFileRec(getLocalToFileRec(parent, binding));
			parent.addChild(pdomBinding);
			afterAddBinding(pdomBinding);
		}
		
		pushPostProcesses(pdomBinding, binding);
		
		return pdomBinding;
	}
	
	private void pushPostProcesses(PDOMBinding pdomBinding, IBinding binding) throws CoreException, DOMException {
		if (pdomBinding instanceof PDOMCPPClassTemplatePartialSpecialization &&
				binding instanceof ICPPClassTemplatePartialSpecialization) {
			PDOMCPPClassTemplatePartialSpecialization pdomSpec = (PDOMCPPClassTemplatePartialSpecialization) pdomBinding;
			ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) binding;
			pushPostProcess(new ConfigurePartialSpecialization(pdomSpec, spec));
		} 
		if (pdomBinding instanceof PDOMCPPFunctionTemplate && binding instanceof ICPPFunction) {
			PDOMCPPFunctionTemplate pdomTemplate = (PDOMCPPFunctionTemplate) pdomBinding;
			ICPPFunction function = (ICPPFunction) binding;
			pushPostProcess(new ConfigureFunctionTemplate(pdomTemplate, function));
		}
		if (pdomBinding instanceof ICPPTemplateDefinition && binding instanceof ICPPTemplateDefinition) {
			ICPPTemplateDefinition template = (ICPPTemplateDefinition) binding;
			pushPostProcess(new ConfigureTemplate(template));
		}
	}
	
	private void addImplicitMethods(PDOMBinding type, ICPPClassType binding) throws CoreException {
		try {
			IScope scope = binding.getCompositeScope();
			if (scope instanceof ICPPClassScope) {
				ICPPMethod[] implicit= ((ICPPClassScope) scope).getImplicitMethods();
				for (ICPPMethod method : implicit) {
					PDOMBinding pdomBinding= adaptBinding(method);
					if (pdomBinding == null) {
						addBinding(type, method);
					} else if (!pdomBinding.hasDefinition()) {
						pdomBinding.update(this, method);
					}
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public int getBindingType(IBinding binding) {
		if (binding instanceof ICPPSpecialization) {
			if (binding instanceof ICPPDeferredTemplateInstance) {
				if (binding instanceof ICPPFunction) {
					return CPP_DEFERRED_FUNCTION_INSTANCE;
				} else if (binding instanceof ICPPClassType) {
					return CPP_DEFERRED_CLASS_INSTANCE;
				}
			} else if (binding instanceof ICPPTemplateInstance) {
				if (binding instanceof ICPPConstructor) {
					return CPP_CONSTRUCTOR_INSTANCE;
				} else if (binding instanceof ICPPMethod) {
					return CPP_METHOD_INSTANCE;
				} else if (binding instanceof ICPPFunction) {
					return CPP_FUNCTION_INSTANCE;
				} else if (binding instanceof ICPPClassType) {
					return CPP_CLASS_INSTANCE;		
				}
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
				return CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
			} else if (binding instanceof ICPPField) {
				return CPP_FIELD_SPECIALIZATION;
		    } else if (binding instanceof ICPPFunctionTemplate) {
				if (binding instanceof ICPPConstructor) {
					return CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION;
				} else if (binding instanceof ICPPMethod) {
					return CPP_METHOD_TEMPLATE_SPECIALIZATION;
				} else if (binding instanceof ICPPFunction) {
					return CPP_FUNCTION_TEMPLATE_SPECIALIZATION;
				}
			} else if (binding instanceof ICPPConstructor) {
				return CPP_CONSTRUCTOR_SPECIALIZATION;
			} else if (binding instanceof ICPPMethod) {
				return CPP_METHOD_SPECIALIZATION;
			} else if (binding instanceof ICPPFunction) {
				return CPP_FUNCTION_SPECIALIZATION;
			} else if (binding instanceof ICPPClassTemplate) {
				return CPP_CLASS_TEMPLATE_SPECIALIZATION;
			} else if (binding instanceof ICPPClassType) {
				return CPP_CLASS_SPECIALIZATION;
			} else if (binding instanceof ITypedef) {
				return CPP_TYPEDEF_SPECIALIZATION;
			}
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter) {
				return CPP_TEMPLATE_TYPE_PARAMETER;
			}
// TODO other template parameter types
//			else if (binding instanceof ICPPTemplateTemplateParameter)
//				return CPP_TEMPLATE_TEMPLATE_PARAMETER;
//			else if (binding instanceof ICPPTemplateNonTypeParameter)
//				return CPP_TEMPLATE_NON_TYPE_PARAMETER;
		} else if (binding instanceof ICPPField) {
			// this must be before variables
			return CPPFIELD;
		} else if (binding instanceof ICPPVariable) {
			return CPPVARIABLE;
		} else if (binding instanceof ICPPFunctionTemplate) {
			// this must be before functions
			if (binding instanceof ICPPConstructor) {
				return CPP_CONSTRUCTOR_TEMPLATE;
			} else if (binding instanceof ICPPMethod) {
				return CPP_METHOD_TEMPLATE;
			} else if (binding instanceof ICPPFunction) {
				return CPP_FUNCTION_TEMPLATE;
			}
		} else if (binding instanceof ICPPConstructor) {
			// before methods
			return CPP_CONSTRUCTOR;
		} else if (binding instanceof ICPPMethod) {
			// this must be before functions
			return CPPMETHOD;
		} else if (binding instanceof ICPPFunctionType) {
			return CPP_FUNCTION_TYPE;
		} else if (binding instanceof ICPPFunction) {
			return CPPFUNCTION;
		} else if (binding instanceof ICPPClassTemplate) {
			// this must be before class type
			return CPP_CLASS_TEMPLATE;
		} else if (binding instanceof ICPPClassType) {
			return CPPCLASSTYPE;
		} else if (binding instanceof ICPPNamespaceAlias) {
			return CPPNAMESPACEALIAS;
		} else if (binding instanceof ICPPNamespace) {
			return CPPNAMESPACE;
		} else if (binding instanceof ICPPUsingDeclaration) {
			return CPP_USING_DECLARATION;
		} else if (binding instanceof IEnumeration) {
			return CPPENUMERATION;
		} else if (binding instanceof IEnumerator) {
			return CPPENUMERATOR;
		} else if (binding instanceof ITypedef) {
			return CPPTYPEDEF;
		}
			
		return 0;
	}

	/**
	 * Find the equivalent binding, or binding placeholder within this PDOM
	 */
	@Override
	public PDOMBinding doAdaptBinding(IBinding binding) throws CoreException {
		PDOMNode parent = getAdaptedParent(binding, false);
		if (parent == this) {
			int localToFileRec= getLocalToFileRec(null, binding);
			return CPPFindBinding.findBinding(getIndex(), this, binding, localToFileRec);
		}
		if (parent instanceof PDOMCPPNamespace) {
			int localToFileRec= getLocalToFileRec(parent, binding);
			return CPPFindBinding.findBinding(((PDOMCPPNamespace)parent).getIndex(), this, binding,
					localToFileRec);
		}
		if (parent instanceof IPDOMMemberOwner) {
			int localToFileRec= getLocalToFileRec(parent, binding);
			return CPPFindBinding.findBinding(parent, this, binding, localToFileRec);
		}
		return null;
	}

	/**
	 * Adapts the parent of the given binding to an object contained in this linkage. May return 
	 * <code>null</code> if the binding cannot be adapted or the binding does not exist and addParent
	 * is set to <code>false</code>.
	 * @param binding the binding to adapt
	 * @return <ul>
	 * <li> null - skip this binding (don't add to pdom)
	 * <li> this - for global scope
	 * <li> a PDOMBinding instance - parent adapted binding
	 * </ul>
	 * @throws CoreException
	 */
 	private final PDOMNode getAdaptedParent(IBinding binding, boolean addParent) throws CoreException {
 		try {
 			IBinding scopeBinding = null;
 			if (binding instanceof ICPPTemplateInstance) {
 				scopeBinding = ((ICPPTemplateInstance)binding).getTemplateDefinition();
 			} else {
 				IScope scope = binding.getScope();
 				if (binding instanceof IIndexBinding) {
 					IIndexBinding ib= (IIndexBinding) binding;
 					// don't adapt file local bindings from other fragments to this one.
 					if (ib.isFileLocal()) {
 						return null;
 					}
 					if (scope == null) {
 						if (binding instanceof ICPPInternalUnknownClassType) {
 							if (binding instanceof PDOMBinding) 
 								return addaptOrAddBinding(addParent, ((PDOMBinding) binding).getParentBinding());

 							// what if we have a composite binding??
 							return null;
 						}	

 						// in an index the null scope represents global scope.
 						return this;
 					}
 				}
 				if (scope == null) {
 					if (binding instanceof ICPPSpecialization) {
 						if (((ICPPSpecialization) binding).getSpecializedBinding() instanceof IIndexBinding) {
 							return this;
 						}
 					}
 					return null;
 				}
 
 				if (scope instanceof IIndexScope) {
 					if (scope instanceof CompositeScope) { // we special case for performance
 						return addaptOrAddBinding(addParent, ((CompositeScope)scope).getRawScopeBinding());
 					} else {
 						return addaptOrAddBinding(addParent, ((IIndexScope) scope).getScopeBinding());
 					}
 				}
 
 				// the scope is from the ast
 				if (scope instanceof ICPPTemplateScope &&
 						!(binding instanceof ICPPTemplateParameter || binding instanceof ICPPTemplateInstance)) {
 					scope = scope.getParent();
 					if (scope == null) {
 						return null;
 					}
 				}
 
 				while (scope instanceof ICPPNamespaceScope) {
 					IName name= scope.getScopeName();
 					if (name != null && name.toCharArray().length == 0) {
 						// skip unnamed namespaces
 						scope= scope.getParent();
 					} else {
 						break;
 					}
 				}
 
 				IASTNode scopeNode = ASTInternal.getPhysicalNodeOfScope(scope);
 				if (scopeNode instanceof IASTCompoundStatement) {
 					return null;
 				} else if (scopeNode instanceof IASTTranslationUnit) {
 					return this;
 				} else {
 					if (scope instanceof ICPPClassScope) {
 						scopeBinding = ((ICPPClassScope)scope).getClassType();
 					} else {
 						IName scopeName = scope.getScopeName();
 						if (scopeName instanceof IASTName) {
 							scopeBinding = ((IASTName) scopeName).resolveBinding();
 						}
 					}
 				}
 			}
 			if (scopeBinding != null && scopeBinding != binding) 
 				return addaptOrAddBinding(addParent, scopeBinding);
 			
 		} catch (DOMException e) {
 			throw new CoreException(Util.createStatus(e));
 		}
 		return null;
 	}

	private PDOMBinding addaptOrAddBinding(boolean add, IBinding binding)	throws CoreException {
		if (add) 
			return addBinding(binding, null);

		return adaptBinding(binding);
	}

	@Override
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IProblemBinding) {
			return null;
		}
		if (type instanceof IGPPBasicType) {
			IGPPBasicType gbt= (IGPPBasicType) type;
			IType typeof;
			try {
				typeof = gbt.getTypeofType();
				if (typeof != null) {
					return addType(parent, typeof);
				}
			} catch (DOMException e) {
			}
			return new PDOMGPPBasicType(pdom, parent, gbt);
		}
		if (type instanceof ICPPBasicType) {
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType) type);
		}
		if (type instanceof ICPPFunctionType) {
			return new PDOMCPPFunctionType(pdom, parent, (ICPPFunctionType) type);
		}
		if (type instanceof ICPPClassType) {
			return addBinding((ICPPClassType) type, null);
		}
		if (type instanceof IEnumeration) {
			return addBinding((IEnumeration) type, null);
		}
		if (type instanceof ITypedef) {
			return addBinding((ITypedef) type, null);
		}
		if (type instanceof ICPPReferenceType) {
			return new PDOMCPPReferenceType(pdom, parent,
					(ICPPReferenceType) type);
		}
		if (type instanceof ICPPPointerToMemberType) {
			return new PDOMCPPPointerToMemberType(pdom, parent,
					(ICPPPointerToMemberType) type);
		}
		if (type instanceof ICPPTemplateTypeParameter) {
			return addBinding((ICPPTemplateTypeParameter) type, null);
		}

		return super.addType(parent, type); 
	}

	private void handlePostProcesses() {
		while (!postProcesses.isEmpty()) {
			popPostProcess().run();
		}
	}
	
	private void pushPostProcess(Runnable process) {
		postProcesses.add(postProcesses.size(), process);
	}
	
	private Runnable popPostProcess() {
		return postProcesses.remove(postProcesses.size() - 1);
	}
	
	@Override
	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(pdom, record);
		case CPPFIELD:
			return new PDOMCPPField(pdom, record);
		case CPP_CONSTRUCTOR:
			return new PDOMCPPConstructor(pdom, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		case CPP_USING_DECLARATION:
			return new PDOMCPPUsingDeclaration(pdom, record);
		case GPPBASICTYPE:
			return new PDOMGPPBasicType(pdom, record);
		case CPPBASICTYPE:
			return new PDOMCPPBasicType(pdom, record);
		case CPPPARAMETER:
			return new PDOMCPPParameter(pdom, record);
		case CPPENUMERATION:
			return new PDOMCPPEnumeration(pdom, record);
		case CPPENUMERATOR:
			return new PDOMCPPEnumerator(pdom, record);
		case CPPTYPEDEF:
			return new PDOMCPPTypedef(pdom, record);
		case CPP_POINTER_TO_MEMBER_TYPE:
			return new PDOMCPPPointerToMemberType(pdom, record);
		case CPP_REFERENCE_TYPE:
			return new PDOMCPPReferenceType(pdom, record);
		case CPP_FUNCTION_TEMPLATE:
			return new PDOMCPPFunctionTemplate(pdom, record);
		case CPP_METHOD_TEMPLATE:
			return new PDOMCPPMethodTemplate(pdom, record);
		case CPP_CONSTRUCTOR_TEMPLATE:
			return new PDOMCPPConstructorTemplate(pdom, record);
		case CPP_CLASS_TEMPLATE:
			return new PDOMCPPClassTemplate(pdom, record);
		case CPP_CLASS_TEMPLATE_PARTIAL_SPEC:
			return new PDOMCPPClassTemplatePartialSpecialization(pdom, record);
		case CPP_FUNCTION_INSTANCE:
			return new PDOMCPPFunctionInstance(pdom, record);
		case CPP_METHOD_INSTANCE:
			return new PDOMCPPMethodInstance(pdom, record);
		case CPP_CONSTRUCTOR_INSTANCE:
			return new PDOMCPPConstructorInstance(pdom, record);
		case CPP_DEFERRED_FUNCTION_INSTANCE:
			return new PDOMCPPDeferredFunctionInstance(pdom, record);
		case CPP_CLASS_INSTANCE:
			return new PDOMCPPClassInstance(pdom, record);
		case CPP_DEFERRED_CLASS_INSTANCE:
			return new PDOMCPPDeferredClassInstance(pdom, record);
		case CPP_TEMPLATE_TYPE_PARAMETER:
			return new PDOMCPPTemplateTypeParameter(pdom, record);
// TODO other template parameter types
//		case CPP_TEMPLATE_TEMPLATE_PARAMETER:
//			return new PDOMCPPTemplateTemplateParameter(pdom, record);
//		case CPP_TEMPLATE_NON_TYPE_PARAMETER:
//			return new PDOMCPPTemplateNonTypeParameter(pdom, record);
		case CPP_FIELD_SPECIALIZATION:
			return new PDOMCPPFieldSpecialization(pdom, record);
		case CPP_FUNCTION_SPECIALIZATION:
			return new PDOMCPPFunctionSpecialization(pdom, record);
		case CPP_METHOD_SPECIALIZATION:
			return new PDOMCPPMethodSpecialization(pdom, record);
		case CPP_CONSTRUCTOR_SPECIALIZATION:
			return new PDOMCPPConstructorSpecialization(pdom, record);
		case CPP_CLASS_SPECIALIZATION:
			return new PDOMCPPClassSpecialization(pdom, record);
		case CPP_FUNCTION_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPFunctionTemplateSpecialization(pdom, record);
		case CPP_METHOD_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPMethodTemplateSpecialization(pdom, record);
		case CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPConstructorTemplateSpecialization(pdom, record);
		case CPP_CLASS_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPClassTemplateSpecialization(pdom, record);
		case CPP_TYPEDEF_SPECIALIZATION:
			return new PDOMCPPTypedefSpecialization(pdom, record);
		case CPP_FUNCTION_TYPE:
			return new PDOMCPPFunctionType(pdom, record);
		case CPP_PARAMETER_SPECIALIZATION:
			return new PDOMCPPParameterSpecialization(pdom, record);
		default:
			return super.getNode(record);
		}
	}

	@Override
	public IBTreeComparator getIndexComparator() {
		return new CPPFindBinding.CPPBindingBTreeComparator(pdom);
	}

	@Override
	public void onCreateName(PDOMFile file, IASTName name, PDOMName pdomName) throws CoreException {
		super.onCreateName(file, name, pdomName);
		
		IASTNode parentNode= name.getParent();
		if (parentNode instanceof ICPPASTQualifiedName) {
		    IASTName[] ns = ((ICPPASTQualifiedName) parentNode).getNames();
		    if (name != ns[ns.length - 1]) {
		    	return;
		    }
	    	parentNode = parentNode.getParent();
		}
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				ICPPASTBaseSpecifier baseNode= (ICPPASTBaseSpecifier) parentNode;
				PDOMBinding derivedClassBinding= derivedClassName.getBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType) derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(),
							baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization) derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(),
							baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				}
			}
		}
		else if (parentNode instanceof ICPPASTUsingDirective) {
			IScope container= CPPVisitor.getContainingScope(name);
			try {
				boolean doit= false;
				PDOMCPPNamespace containerNS= null;
				
				IASTNode node= ASTInternal.getPhysicalNodeOfScope(container);
				if (node instanceof IASTTranslationUnit) {
					doit= true;
				}
				else if (node instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition nsDef= (ICPPASTNamespaceDefinition) node;
					IASTName nsContainerName= nsDef.getName();
					if (nsContainerName != null) {
						PDOMBinding binding= adaptBinding(nsContainerName.resolveBinding());
						if (binding instanceof PDOMCPPNamespace) {
							containerNS= (PDOMCPPNamespace) binding;
							doit= true;
						}
					}
				}
				if (doit) {
					int rec= file.getFirstUsingDirectiveRec();
					PDOMCPPUsingDirective ud= new PDOMCPPUsingDirective(this, rec, containerNS,
							pdomName.getBinding());
					file.setFirstUsingDirectiveRec(ud.getRecord());
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage#getUsingDirectives()
	 */
	@Override
	public ICPPUsingDirective[] getUsingDirectives(PDOMFile file) throws CoreException {
		int rec= file.getFirstUsingDirectiveRec();
		if (rec == 0) {
			return ICPPUsingDirective.EMPTY_ARRAY;
		}
		LinkedList<ICPPUsingDirective> uds= new LinkedList<ICPPUsingDirective>();
		do {
			PDOMCPPUsingDirective ud= new PDOMCPPUsingDirective(this, rec);
			uds.addFirst(ud);
			rec= ud.getPreviousRec();
		}
		while (rec != 0);
		return uds.toArray(new ICPPUsingDirective[uds.size()]);
	}

	@Override
	public void onDeleteName(PDOMName pdomName) throws CoreException {
		super.onDeleteName(pdomName);
		
		if (pdomName.isBaseSpecifier()) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				PDOMBinding derivedClassBinding= derivedClassName.getBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				}
			}
		}
	}

	@Override
	protected PDOMFile getLocalToFile(IBinding binding) throws CoreException {
		if (pdom instanceof WritablePDOM) {
			final WritablePDOM wpdom= (WritablePDOM) pdom;
			PDOMFile file= null;
			if (binding instanceof ICPPUsingDeclaration) {
				String path= ASTInternal.getDeclaredInOneFileOnly(binding);
				if (path != null) {
					file= wpdom.getFileForASTPath(getLinkageID(), path);
				}
			} else if (binding instanceof ICPPNamespaceAlias) {
				String path= ASTInternal.getDeclaredInSourceFileOnly(binding, false);
				if (path != null) {
					file= wpdom.getFileForASTPath(getLinkageID(), path);
				}
			}
			if (file == null && !(binding instanceof IIndexBinding)) {
				IScope scope;
				try {
					scope= binding.getScope();
					if (scope instanceof ICPPNamespaceScope) {
						IName name= scope.getScopeName();
						if (name instanceof IASTName && name.toCharArray().length == 0) {
							IASTName astName= (IASTName) name;
							IBinding parentBinding= astName.resolveBinding();
							String path= ASTInternal.getDeclaredInSourceFileOnly(parentBinding, false);
							if (path != null) {
								file= wpdom.getFileForASTPath(getLinkageID(), path);
							}
						}
					}
				} catch (DOMException e) {
				}
			}
			if (file != null) {
				return file;
			}
		} 
		if (binding instanceof ICPPMember) {
			return null;
		}
		return super.getLocalToFile(binding);
	}
}
