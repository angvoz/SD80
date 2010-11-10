package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class LanguageSettingsEntriesLabelProvider extends LabelProvider implements IFontProvider, ITableLabelProvider /*, IColorProvider*/{
	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex==0 && (element instanceof ICLanguageSettingEntry)) {
			return LanguageSettingsImages.getImage((ICLanguageSettingEntry) element);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
			switch (columnIndex) {
			case 0:
				return entry.getName();
			case 1:
				if (entry.getKind() == ICSettingEntry.MACRO) {
					return entry.getValue();
				}
				return null;
			}
		} else if (columnIndex == 0) {
			return element.toString();
		}
		
		return null;
	}
	
	public Font getFont(Object element) {
		if (element instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
			if (entry.isBuiltIn())
				return null;
			if (entry.isReadOnly())
				return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
			// normal
			return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
		return null;
	}
}