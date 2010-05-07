/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.formatter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Manages format extensions
 */
public class FormatExtensionManager implements IVariableFormatManager {
	
	// Preferences
	public static final String VARIABLE_FORMATS_ENABLED = "variable_formats_enabled";
	public static final boolean VARIABLE_FORMATS_ENABLED_DEFAULT = true;

	/**
	 * A chooser implementation that always defers to any formatter that is not one of the default formatters
	 */
	private static final class DefaultFormatProviderChooser implements IVariableFormatProviderChooser {	
		private static final String DEFAULT_COMPOSITE = 
			"org.eclipse.cdt.debug.edc.formatter.default.composite"; //$NON-NLS-1$
		private static final String DEFAULT_ARRAY = 
			"org.eclipse.cdt.debug.edc.formatter.default.array"; //$NON-NLS-1$
		
		public String chooseDetailValueConverter(IType type, Collection<String> ids) {
			return chooseAnyBeforeDefault(ids);
		}

		public String chooseTypeContentProvider(IType type, Collection<String> ids) {
			return chooseAnyBeforeDefault(ids);
		}

		public String chooseVariableValueConverter(IType type, Collection<String> ids) {
			return chooseAnyBeforeDefault(ids);
		}

		private String chooseAnyBeforeDefault(Collection<String> ids) {
			if (ids.size() > 1) {
				for (String id : ids) {
					if (!id.equals(DEFAULT_COMPOSITE) && !id.equals(DEFAULT_ARRAY))
						return id;
				}
			}
			else if (ids.size() == 1)
				return ids.iterator().next();
			
			return null;
		}
	}
	
	public class FormatProviderExtension {
		private String label;
		private IVariableFormatProvider formatProvider;

		public FormatProviderExtension(String label, IVariableFormatProvider formatProvider) {
			this.label = label;
			this.formatProvider = formatProvider;
		}

		public String getLabel() {
			return label;
		}

		public IVariableFormatProvider getFormatProvider() {
			return formatProvider;
		}
	}

	private static IVariableFormatManager instance = 
		new FormatExtensionManager(new DefaultFormatProviderChooser());
	
	private Map<String, FormatProviderExtension> formatProviders;
	private IVariableFormatProviderChooser chooser;
	private boolean enabled = FormatExtensionManager.VARIABLE_FORMATS_ENABLED_DEFAULT;

	public static IVariableFormatManager instance() {
		return instance;
	}
	
	private FormatExtensionManager(IVariableFormatProviderChooser chooser) {
		readProviders();
		setFormatProviderChooser(chooser);
		IEclipsePreferences scope = new InstanceScope().getNode(EDCDebugger.PLUGIN_ID);
		enabled = scope.getBoolean(VARIABLE_FORMATS_ENABLED, FormatExtensionManager.VARIABLE_FORMATS_ENABLED_DEFAULT);
	}

	private void readProviders() {
		if (formatProviders != null)
			return;
		IConfigurationElement[] elements = 
			Platform.getExtensionRegistry().getConfigurationElementsFor(EDCDebugger.PLUGIN_ID + ".variableFormatProvider"); //$NON-NLS-1$
		for (IConfigurationElement element : elements) {
			try {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				String label = element.getAttribute("label"); //$NON-NLS-1$
				IVariableFormatProvider formatProvider = 
					(IVariableFormatProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
				if (formatProviders == null)
					formatProviders = new HashMap<String, FormatProviderExtension>();
				formatProviders.put(id, new FormatProviderExtension(label, formatProvider));
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError("Could not create formatting extension", e);
			}
		}
	}
	
	private interface Getter <T> {
		T get(IVariableFormatProvider fp, IType type);
		String choose(IType type, Collection<String> ids);
	}
	
	private<T> T getProvider(IType type, Getter<T> getter) {
		if (!enabled)
			return null;
		Map<String, T> providers = new HashMap<String, T>();
		for (Entry<String, FormatProviderExtension> entry : formatProviders.entrySet()) {
			IVariableFormatProvider fp = entry.getValue().getFormatProvider();
			T t = getter.get(fp, type);
			if (t != null)
				providers.put(entry.getKey(), t);
		}
		String id = getter.choose(type, providers.keySet());
		return providers.get(id);
	}
	
	
	public ITypeContentProvider getTypeContentProvider(IType type) {
		return getProvider(type, new Getter<ITypeContentProvider>() {
			public ITypeContentProvider get(IVariableFormatProvider fp, IType type) {
				return fp.getTypeContentProvider(type);
			}
			public String choose(IType type, Collection<String> ids) {
				return chooser.chooseTypeContentProvider(type, ids);
			}
		});
	}
	
	public IVariableValueConverter getVariableValueConverter(IType type) {
		return getProvider(type, new Getter<IVariableValueConverter>() {
			public IVariableValueConverter get(IVariableFormatProvider fp, IType type) {
				return fp.getVariableValueConverter(type);
			}
			public String choose(IType type, Collection<String> ids) {
				return chooser.chooseVariableValueConverter(type, ids);
			}
		});
	}
	
	public IVariableValueConverter getDetailValueConverter(IType type) {
		return getProvider(type, new Getter<IVariableValueConverter>() {
			public IVariableValueConverter get(IVariableFormatProvider fp, IType type) {
				return fp.getDetailValueConverter(type);
			}
			public String choose(IType type, Collection<String> ids) {
				return chooser.chooseDetailValueConverter(type, ids);
			}
		});
	}

	public void setFormatProviderChooser(IVariableFormatProviderChooser chooser) {
		this.chooser = chooser;
	}

	public String[] getVariableFormatProviderIds() {
		Set<String> keySet = formatProviders.keySet();
		return (String[]) keySet.toArray(new String[keySet.size()]);
	}
	
	public String getFormatProviderLabel(String id) {
		return formatProviders.get(id).getLabel();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
