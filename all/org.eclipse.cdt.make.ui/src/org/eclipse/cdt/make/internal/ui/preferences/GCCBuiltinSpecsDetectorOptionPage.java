/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.internal.ui.newui.LanguageSettingsProviderTab.ProviderReference;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.util.TableLayoutComposite;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.DialogsMessages;
import org.eclipse.cdt.ui.dialogs.RegularExpressionStatusDialog;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

import com.ibm.icu.text.MessageFormat;

/**
 * Options page for RegexErrorParser in Error Parsers Tab of properties/preferences.
 *
 * @since 5.2
 */
public final class GCCBuiltinSpecsDetectorOptionPage extends AbstractCOptionPage {
	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_DELETE = 1;
	private static final int BUTTON_MOVEUP = 2;
	private static final int BUTTON_MOVEDOWN = 3;

	private static final String[] BUTTONS = new String[] {
			AbstractCPropertyTab.ADD_STR,
			AbstractCPropertyTab.DEL_STR,
			AbstractCPropertyTab.MOVEUP_STR,
			AbstractCPropertyTab.MOVEDOWN_STR,
		};

	private static final String OOPS = "OOPS"; //$NON-NLS-1$

	private Table fTable;
	private TableViewer fTableViewer;
	private Button[] fButtons = null;

	private RegexErrorParser fErrorParser;
	private ProviderReference fProviderReference;
	private boolean fEditable;

	private Text inputCommand;
	
//	private StatusMessageLine fStatusLine;
	private Button runOnceRadioButton;
	private Button runEveryBuildRadioButton;

	/**
	 * Provides generic implementation for overridden methods.
	 * One purpose is to make it easier for subclasses to operate with {@link RegexErrorPattern},
	 * another is to provide content assist.
	 *
	 */
	private abstract class RegexPatternEditingSupport extends EditingSupport {
		private final TableViewer tableViewer;

		/**
		 * Constructor.
		 *
		 * @param viewer - table viewer where to provide editing support.
		 * @param isFindStyle - if "find" or "replace" style for potential content assist,
		 *     see {@link FindReplaceDocumentAdapterContentProposalProvider}.
		 */
		public RegexPatternEditingSupport(TableViewer viewer, boolean isFindStyle) {
			super(viewer);
			tableViewer = viewer;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
		 */
		@Override
		protected boolean canEdit(Object element) {
			return fEditable;
		}

		/**
		 * The intention of RegexPatternEditingSupport is to provide Regex content assist
		 * during in-table editing. However having problems with mouse selection and
		 * {@link ContentAssistCommandAdapter} using {@link FindReplaceDocumentAdapterContentProposalProvider}
		 * is removed for time being. See bug 288982 for more details.
		 *
		 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
		 */
		@Override
		protected CellEditor getCellEditor(Object element) {
			CellEditor editor = new TextCellEditor(tableViewer.getTable());
			return editor;
		}

		/**
		 * Get value from {@link RegexErrorPattern}. This is column-specific value.
		 *
		 * @param regexErrorPattern - pattern to query.
		 * @return retrieved value
		 */
		abstract protected Object getFromPattern(RegexErrorPattern regexErrorPattern);

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
		 */
		@Override
		protected Object getValue(Object element) {
			if (element instanceof RegexErrorPattern) {
				RegexErrorPattern regexErrorPattern = (RegexErrorPattern) element;
				return getFromPattern(regexErrorPattern);
			}
			return OOPS;
		}

		/**
		 * Set value into one of the pattern's field. Which field - it's column-specific.
		 *
		 * @param regexErrorPattern - pattern to set the field
		 * @param value - value to set
		 */
		abstract protected void setToPattern(RegexErrorPattern regexErrorPattern, String value);

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof RegexErrorPattern && (value instanceof String)) {
				String stringValue = (String) value;
				RegexErrorPattern errorPattern = (RegexErrorPattern) element;
				setToPattern(errorPattern, stringValue);
				tableViewer.update(element, null);
			}
		}
	}

	@Override
	public void init(Object providerRef) {
		// must be ProviderReference
		fProviderReference = (ProviderReference) providerRef;
//		fEditable = isEditable;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
//		Composite optionsPageComposite = new Composite(composite, SWT.NULL);
		fEditable = parent.isEnabled();

		final Composite composite = new Composite(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 1;
			layout.marginHeight = 1;
			layout.marginRight = 1;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Dialog.applyDialogFont(composite);
			
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			composite.setLayoutData(gd);
		}


		Group groupRun = new Group(composite, SWT.SHADOW_ETCHED_IN);
//		groupRun.setText("Language Settings Provider Options");

		GridLayout gridLayoutRun = new GridLayout();
//		GridLayout gridLayoutRun = new GridLayout(2, true);
//		gridLayoutRun.makeColumnsEqualWidth = false;
//		gridLayoutRun.marginRight = -10;
//		gridLayoutRun.marginLeft = -4;
		groupRun.setLayout(gridLayoutRun);
//		GridData gdRun = new GridData(GridData.FILL_HORIZONTAL);
//		gdRun.horizontalSpan = 2;
//		groupRun.setLayoutData(gdRun);

		AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) fProviderReference.getProvider();
		{
			runOnceRadioButton = new Button(groupRun, SWT.RADIO);
			runOnceRadioButton.setText("Run only once"); //$NON-NLS-1$
			//		    b1.setToolTipText(UIMessages.getString("EnvironmentTab.3")); //$NON-NLS-1$
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			runOnceRadioButton.setLayoutData(gd);
			runOnceRadioButton.setSelection(provider.isRunOnce());
			runOnceRadioButton.setEnabled(fEditable);
		}
		{
			runEveryBuildRadioButton = new Button(groupRun, SWT.RADIO);
			runEveryBuildRadioButton.setText("Activate on every build"); //$NON-NLS-1$
			runEveryBuildRadioButton.setSelection(!provider.isRunOnce());
			runEveryBuildRadioButton.setEnabled(fEditable);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			runEveryBuildRadioButton.setLayoutData(gd);
		}

		// Compiler specs command
		{
			Label label = ControlFactory.createLabel(composite, "Command to get compiler specs:");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			label.setLayoutData(gd);
			label.setEnabled(fEditable);
//		Label newLabel = new Label(composite, SWT.NONE);
////		((GridData) newLabel.getLayoutData()).horizontalSpan = 1;
//		newLabel.setText("Command to get compiler specs:");
		}

		{
			inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
			String customParameter = provider.getCustomParameter();
			inputCommand.setText(customParameter!=null ? customParameter : "");
			inputCommand.setEnabled(fEditable);
		}

		{
			Button button = ControlFactory.createPushButton(composite, "Browse...");
			button.setEnabled(fEditable);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent evt) {
//					handleAddr2LineButtonSelected();
					//updateLaunchConfigurationDialog();
				}

			});

		}

//		{
//			final Button button = new Button(composite, SWT.PUSH);
//			button.setFont(parent.getFont());
//			String text = fProvider.isEmpty() ? "Run Now (TODO)" : "Clear";
//			button.setText(text);
////			button.addSelectionListener(this);
//			GridData data = new GridData();
//			data.horizontalSpan = 2;
////			data.horizontalAlignment = GridData.BEGINNING;
////			data.widthHint = 60;
//			button.setLayoutData(data);
//			// TODO
//			button.setEnabled(fEditable && !fProvider.isEmpty());
//
//			button.addSelectionListener(new SelectionAdapter() {
//
//				@Override
//				public void widgetSelected(SelectionEvent evt) {
//					if (fProvider.isEmpty()) {
//						// TODO
//					} else {
//						fProvider.clear();
//					}
//					// TODO
//					button.setEnabled(fEditable && !fProvider.isEmpty());
//					String text = fProvider.isEmpty() ? "Run Now (TODO)" : "Clear";
//					button.setText(text);
//					button.pack();
//				}
//
//			});
//
//		}

//		// Compiler specs command
//		{
//			Label label = ControlFactory.createLabel(composite, "Parsing rules:");
//			GridData gd = new GridData();
//			gd.horizontalSpan = 2;
//			label.setLayoutData(gd);
////		Label newLabel = new Label(composite, SWT.NONE);
//////		((GridData) newLabel.getLayoutData()).horizontalSpan = 1;
////		newLabel.setText("Command to get compiler specs:");
//		}


//		createPatternsTable(group, composite);








//		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
//		group.setText(DialogsMessages.RegexErrorParserOptionPage_Title);
//
//		GridLayout gridLayout = new GridLayout(2, true);
//		gridLayout.makeColumnsEqualWidth = false;
//		gridLayout.marginRight = -10;
//		gridLayout.marginLeft = -4;
//		group.setLayout(gridLayout);
//		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		Composite composite = new Composite(group, SWT.NONE);
//		GridLayout layout = new GridLayout();
//		layout.numColumns = 2;
//		layout.marginWidth = 1;
//		layout.marginHeight = 1;
//		layout.marginRight = 1;
//		composite.setLayout(layout);
//		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
//		Dialog.applyDialogFont(composite);
//
//		if (!fEditable)
//			createLinkToPreferences(composite);
//
//		createPatternsTable(group, composite);
//
//		if (fEditable) {
//			createButtons(composite);
//		}

//		// Status line
//		if (fEditable) {
//			fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);
//			IStatus status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, "Note that currently the options are applied to provider directly (FIXME)");
//			fStatusLine.setErrorStatus(status);
//		}

		setControl(composite);
	}

	private static RegexErrorPattern newDummyPattern() {
		return new RegexErrorPattern(null, null, null, null, null, IMarker.SEVERITY_ERROR, true);
	}

	private static String severityToString(int severity) {
		switch (severity) {
		case IMarkerGenerator.SEVERITY_INFO:
			return DialogsMessages.RegexErrorParserOptionPage_SeverityInfo;
		case IMarkerGenerator.SEVERITY_WARNING:
			return DialogsMessages.RegexErrorParserOptionPage_SeverityWarning;
		case IMarkerGenerator.SEVERITY_ERROR_BUILD:
		case IMarkerGenerator.SEVERITY_ERROR_RESOURCE:
			return DialogsMessages.RegexErrorParserOptionPage_SeverityError;
		}
		return DialogsMessages.RegexErrorParserOptionPage_SeverityIgnore;
	}

	private void initializeTable() {
		RegexErrorPattern[] errorParserPatterns = fErrorParser!=null
			? errorParserPatterns = fErrorParser.getPatterns()
			: new RegexErrorPattern[0];

		int len = errorParserPatterns.length;
		int newLen = len;

		RegexErrorPattern[] tablePatterns = new RegexErrorPattern[newLen];
		System.arraycopy(errorParserPatterns, 0, tablePatterns, 0, len);

		fTableViewer.setInput(tablePatterns);
		fTableViewer.refresh();
	}

	private void createPatternsTable(Group group, Composite parent) {
		TableLayoutComposite tableLayouter = new TableLayoutComposite(parent, SWT.NONE);
		tableLayouter.addColumnData(new ColumnWeightData(10, true)); // severity
		tableLayouter.addColumnData(new ColumnWeightData(40, true)); // pattern
		tableLayouter.addColumnData(new ColumnWeightData(10, true)); // file
		tableLayouter.addColumnData(new ColumnWeightData(10, true)); // line
		tableLayouter.addColumnData(new ColumnWeightData(15, true)); // description
		tableLayouter.addColumnData(new ColumnWeightData(10, true)); // eat line

		int style= SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		if (fEditable) {
			style = style | SWT.FULL_SELECTION;
		}
		fTable = new Table(tableLayouter, style);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);
		fTable.setEnabled(fEditable);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = SWTUtil.getTableHeightHint(fTable, 10);
		gridData.widthHint = new PixelConverter(group).convertWidthInCharsToPixels(100);
		tableLayouter.setLayoutData(gridData);
		fTable.setLayout(new TableLayout());

		fTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		fTableViewer = new TableViewer(fTable);
		fTableViewer.setUseHashlookup(true);
		fTableViewer.setContentProvider(new ArrayContentProvider());

		createSeverityColumn();
		createPatternColumn();
		createFileColumn();
		createLineColumn();
		createDescriptionColumn();
		createEatLineColumn();

		initializeTable();
	}

	private void createSeverityColumn() {
		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_SeverityColumn);
		columnViewer.getColumn().setResizable(true);
		columnViewer.getColumn().setToolTipText(DialogsMessages.RegexErrorParserOptionPage_TooltipSeverity);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {
			final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

			@Override
			public Image getImage(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regexErrorPattern = (RegexErrorPattern) element;
					switch (regexErrorPattern.getSeverity()) {
					case IMarkerGenerator.SEVERITY_INFO:
						return images.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
					case IMarkerGenerator.SEVERITY_WARNING:
						return images.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
					case IMarkerGenerator.SEVERITY_ERROR_BUILD:
					case IMarkerGenerator.SEVERITY_ERROR_RESOURCE:
						return images.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					case RegexErrorPattern.SEVERITY_SKIP:
							return images.getImage(ISharedImages.IMG_ELCL_REMOVE_DISABLED);
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					return severityToString(regex.getSeverity());
				}
				return severityToString(RegexErrorPattern.SEVERITY_SKIP);
			}
		});
		columnViewer.setEditingSupport(new EditingSupport(fTableViewer) {
			final String[] severityComboBoxArray = new String[] {
					severityToString(IMarkerGenerator.SEVERITY_ERROR_RESOURCE),
					severityToString(IMarkerGenerator.SEVERITY_WARNING),
					severityToString(IMarkerGenerator.SEVERITY_INFO),
					severityToString(RegexErrorPattern.SEVERITY_SKIP),
				};

			private int severityToIndex(int severity) {
				String strSeverity = severityToString(severity);
				for (int i = 0; i < severityComboBoxArray.length; i++) {
					if (strSeverity.equals(severityComboBoxArray[i]))
						return i;
				}
				return 0;
			}

			private int indexToSeverity(int index) {
				String strCombo = severityComboBoxArray[index];
				for (int i = 0; i < severityComboBoxArray.length; i++) {
					if (severityToString(i).equals(strCombo))
						return i;
				}
				return RegexErrorPattern.SEVERITY_SKIP;
			}

			@Override
			protected boolean canEdit(Object element) {
				return (element instanceof RegexErrorPattern) && fEditable;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(fTableViewer.getTable(), severityComboBoxArray, SWT.READ_ONLY);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof RegexErrorPattern)
					return severityToIndex(((RegexErrorPattern) element).getSeverity());
				return RegexErrorPattern.SEVERITY_SKIP;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof RegexErrorPattern && (value instanceof Integer)) {
					((RegexErrorPattern) element).setSeverity(indexToSeverity(((Integer) value).intValue()));
					fTableViewer.update(element, null);
				}
			}

		});
	}

	private void createPatternColumn() {
		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_Pattern_Column);
		columnViewer.getColumn().setResizable(true);
		columnViewer.getColumn().setToolTipText(DialogsMessages.RegexErrorParserOptionPage_TooltipPattern);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					String pattern = regex.getPattern();
					return pattern;
				}
				return OOPS;
			}
		});

		columnViewer.setEditingSupport(new RegexPatternEditingSupport(fTableViewer, true) {
			@Override
			protected Object getFromPattern(RegexErrorPattern regexErrorPattern) {
				return regexErrorPattern.getPattern();
			}

			@Override
			protected void setToPattern(RegexErrorPattern regexErrorPattern, String value) {
				if (!fEditable)
					return;
				try{
					regexErrorPattern.setPattern(value);
				} catch (Exception e) {
					// to avoid recursive edits. the dialog is needed to ensure valid pattern on losing focus.
					// this looks ugly and likely incorrect
					fEditable = false;
					RegularExpressionStatusDialog dialog= new RegularExpressionStatusDialog(getShell(), value);
					if (dialog.open() == Window.OK) {
						regexErrorPattern.setPattern(dialog.getValue());
					}
					fEditable = true;
				}
			}
		});
	}

	private void createFileColumn() {
		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_FileColumn);
		columnViewer.getColumn().setToolTipText(DialogsMessages.RegexErrorParserOptionPage_TooltipFile);
		columnViewer.getColumn().setResizable(true);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					return regex.getFileExpression();
				}
				return OOPS;
			}
		});
		columnViewer.setEditingSupport(new RegexPatternEditingSupport(fTableViewer, false) {
			@Override
			protected Object getFromPattern(RegexErrorPattern regexErrorPattern) {
				return regexErrorPattern.getFileExpression();
			}

			@Override
			protected void setToPattern(RegexErrorPattern regexErrorPattern, String value) {
				regexErrorPattern.setFileExpression(value);
			}
		});
	}

	private void createLineColumn() {
		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_LineColumn);
		columnViewer.getColumn().setResizable(true);
		columnViewer.getColumn().setToolTipText(DialogsMessages.RegexErrorParserOptionPage_TooltipLine);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					return regex.getLineExpression();
				}
				return OOPS;
			}
		});
		columnViewer.setEditingSupport(new RegexPatternEditingSupport(fTableViewer, false) {
			@Override
			protected Object getFromPattern(RegexErrorPattern regexErrorPattern) {
				return regexErrorPattern.getLineExpression();
			}

			@Override
			protected void setToPattern(RegexErrorPattern regexErrorPattern, String value) {
				regexErrorPattern.setLineExpression(value);
			}
		});
	}

	private void createDescriptionColumn() {
		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_DescriptionColumn);
		columnViewer.getColumn().setResizable(true);
		columnViewer.getColumn().setToolTipText(DialogsMessages.RegexErrorParserOptionPage_TooltipDescription);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					return regex.getDescriptionExpression();
				}
				return OOPS;
			}
		});
		columnViewer.setEditingSupport(new RegexPatternEditingSupport(fTableViewer, false) {
			@Override
			protected Object getFromPattern(RegexErrorPattern regexErrorPattern) {
				return regexErrorPattern.getDescriptionExpression();
			}

			@Override
			protected void setToPattern(RegexErrorPattern regexErrorPattern, String value) {
				regexErrorPattern.setDescriptionExpression(value);
			}
		});
	}

	private void createEatLineColumn() {
		final String EAT_NO = DialogsMessages.RegexErrorParserOptionPage_ConsumeNo;
		final String EAT_YES = DialogsMessages.RegexErrorParserOptionPage_ConsumeYes;

		final String[] eatLineComboBoxArray = new String[] { EAT_YES, EAT_NO, };
		final int EAT_YES_INDEX = 0;
		final int EAT_NO_INDEX = 1;

		TableViewerColumn columnViewer = new TableViewerColumn(fTableViewer, SWT.NONE);
		columnViewer.getColumn().setText(DialogsMessages.RegexErrorParserOptionPage_EatColumn);
		columnViewer.getColumn().setResizable(true);

		String message = MessageFormat.format(DialogsMessages.RegexErrorParserOptionPage_TooltipConsume, new Object[] { EAT_NO });
		columnViewer.getColumn().setToolTipText(message);
		columnViewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof RegexErrorPattern) {
					RegexErrorPattern regex = (RegexErrorPattern) element;
					if (!regex.isEatProcessedLine())
						return EAT_NO;
				}
				return EAT_YES;
			}
		});
		columnViewer.setEditingSupport(new EditingSupport(fTableViewer) {
			@Override
			protected boolean canEdit(Object element) {
				return (element instanceof RegexErrorPattern) && fEditable;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(fTableViewer.getTable(), eatLineComboBoxArray, SWT.READ_ONLY);
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof RegexErrorPattern)
					if (!((RegexErrorPattern) element).isEatProcessedLine())
						return EAT_NO_INDEX;
				return EAT_YES_INDEX;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if ((element instanceof RegexErrorPattern) && (value instanceof Integer)) {
					((RegexErrorPattern) element).setEatProcessedLine((Integer) value != EAT_NO_INDEX);
					fTableViewer.update(element, null);
				}
			}

		});
	}

	private void createButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(new GridLayout(1, false));

		fButtons = new Button[BUTTONS.length];
		for (int i = 0; i < BUTTONS.length; i++) {
			fButtons[i] = new Button(composite, SWT.PUSH);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gridData.minimumWidth = 80;

			if (BUTTONS[i] != null) {
				fButtons[i].setText(BUTTONS[i]);
				fButtons[i].setEnabled(false);
			} else {
				// no button, but placeholder
				fButtons[i].setVisible(false);
				fButtons[i].setEnabled(false);
				gridData.heightHint = 10;
			}

			fButtons[i].setLayoutData(gridData);
			fButtons[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					for (int i=0; i<fButtons.length; i++) {
						if (fButtons[i].equals(event.widget)) {
							buttonPressed(i);
							return;
						}
					}
				}
			});
		}
		updateButtons();
	}

	private void updateButtons() {
		if (fButtons!=null) {
			int pos = fTable.getSelectionIndex();
			int count = fTable.getItemCount();
			int last = count-1;
			boolean selected = pos>=0 && pos<=last;

			fButtons[BUTTON_ADD].setEnabled(true);
			fButtons[BUTTON_DELETE].setEnabled(selected);
			fButtons[BUTTON_MOVEUP].setEnabled(selected && pos != 0);
			fButtons[BUTTON_MOVEDOWN].setEnabled(selected && pos != last);
		}
	}

	private void buttonPressed (int button) {
		switch (button) {
		case BUTTON_ADD:
			addErrorPattern();
			break;
		case BUTTON_DELETE:
			deleteErrorPattern();
			break;
		case BUTTON_MOVEUP:
			moveItem(true);
			break;
		case BUTTON_MOVEDOWN:
			moveItem(false);
			break;
		default:
			break;
		}
		updateButtons();
	}

	private void addErrorPattern() {
		int pos = fTable.getSelectionIndex();
		int last = fTable.getItemCount()-1;
		if (pos<0 || pos>last)
			pos = last;

		int newPos = pos + 1;
		fTableViewer.insert(newDummyPattern(), newPos);
		fTable.setSelection(newPos);
	}

	private void deleteErrorPattern() {
		int pos = fTable.getSelectionIndex();
		int last = fTable.getItemCount()-1;

		if (pos>=0 && pos<=last) {
			fTableViewer.remove(fTableViewer.getElementAt(pos));
			fTable.setSelection(pos);
		}
	}

	private void moveItem(boolean up) {
		int pos = fTable.getSelectionIndex();
		int count = fTable.getItemCount();
		int last = count-1;
		boolean selected = pos>=0 && pos<=last;

		if (!selected || (up && pos==0) || (!up && pos==last))
			return;

		Object item = fTableViewer.getElementAt(pos);
		fTableViewer.remove(item);
		int newPos = up ? pos-1 : pos+1;
		fTableViewer.insert(item, newPos);
		fTable.setSelection(newPos);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (fProviderReference!=null) {
			AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) fProviderReference.getProvider();
			if (provider!=null) {
//				fProvider.setRunOnce(runOnceRadioButton.getSelection());
				provider.setRunOnce(!runEveryBuildRadioButton.getSelection());

				String command = inputCommand.getText();
				provider.setCustomParameter(command);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		// ErrorParsTas.performDefaults() will do all the work
	}
}
