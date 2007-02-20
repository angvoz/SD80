package org.eclipse.cdt.managedbuilder.ui.newui;

import java.util.ArrayList;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.ui.newui.INewCfgDialog;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewCfgDialog implements INewCfgDialog {
	private static final String PREFIX = "NewConfiguration";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ERROR = PREFIX + ".error";	//$NON-NLS-1$	
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String GROUP = LABEL + ".group";	//$NON-NLS-1$
	private static final String DUPLICATE = ERROR + ".duplicateName";	//$NON-NLS-1$	
	private static final String CASE = ERROR + ".caseName";	//$NON-NLS-1$	
	private static final String INVALID = ERROR + ".invalidName";	//$NON-NLS-1$	
	private static final String DESCRIPTION = LABEL + ".description";	//$NON-NLS-1$
	private static final String NULL = "[null]"; //$NON-NLS-1$	
	// Widgets
	private Text configName;
	private Text configDescription;
	private Combo cloneConfigSelector;
	private Combo realConfigSelector;
	private Button b_clone;
	private Button b_real;	
	private Label statusLabel;

	/** Default configurations defined in the toolchain description */
	private ICProjectDescription des;
	private IConfiguration[] cfgds;
	private IConfiguration[] rcfgs;
	private IConfiguration parentConfig; 
	private String newName;
	private String newDescription;
	private String title;
	
	protected Shell parentShell;

	private class LocalDialog extends Dialog {
		LocalDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		/* (non-Javadoc)
		 * Method declared on Dialog. Cache the name and base config selections.
		 * We don't have to worry that the index or name is wrong because we 
		 * enable the OK button IFF those conditions are met.
		 */
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				newName = configName.getText().trim();
				newDescription = configDescription.getText().trim();
				if (b_clone.getSelection()) 
					parentConfig = cfgds[cloneConfigSelector.getSelectionIndex()];
				else  // real cfg
					parentConfig = rcfgs[realConfigSelector.getSelectionIndex()];
				newConfiguration();
			} else {
				newName = null;
				newDescription = null;
				parentConfig = null;
			}
			super.buttonPressed(buttonId);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (title != null)
				shell.setText(title);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			configName.setFocus();
			if (configName != null) {
				configName.setText(newName);
			}
			setButtons();
		}
		
		protected Control createDialogArea(Composite parent) {

			Composite composite = new Composite(parent, SWT.NULL);
			composite.setFont(parent.getFont());
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Create a group for the name & description

			final Group group1 = new Group(composite, SWT.NONE);
			group1.setFont(composite.getFont());
			GridLayout layout1 = new GridLayout(3, false);
			group1.setLayout(layout1);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			group1.setLayoutData(gd);

			// Add a label and a text widget for Configuration's name
			final Label nameLabel = new Label(group1, SWT.LEFT);
			nameLabel.setFont(parent.getFont());
			nameLabel.setText(NewUIMessages.getResourceString(NAME));
					
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = false;
			nameLabel.setLayoutData(gd);

			configName = new Text(group1, SWT.SINGLE | SWT.BORDER);
			configName.setFont(group1.getFont());
			configName.setText(newName);
			configName.setFocus();
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 2;
			gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			configName.setLayoutData(gd);
			configName.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setButtons();
				}
			});
			
//			 Add a label and a text widget for Configuration's description
	        final Label descriptionLabel = new Label(group1, SWT.LEFT);
	        descriptionLabel.setFont(parent.getFont());
	        descriptionLabel.setText(NewUIMessages.getResourceString(DESCRIPTION));

	        gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = false;
	        descriptionLabel.setLayoutData(gd);
	        configDescription = new Text(group1, SWT.SINGLE | SWT.BORDER);
	        configDescription.setFont(group1.getFont());
			configDescription.setText(newDescription);
			configDescription.setFocus();
			
	        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
	        gd.horizontalSpan = 2;
	        gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
	        configDescription.setLayoutData(gd);
			
			final Group group = new Group(composite, SWT.NONE);
			group.setFont(composite.getFont());
			group.setText(NewUIMessages.getResourceString(GROUP));
			GridLayout layout = new GridLayout(2, false);
			group.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			group.setLayoutData(gd);

			b_clone = new Button(group, SWT.RADIO);
			b_clone.setText(Messages.getString("NewCfgDialog.0")); //$NON-NLS-1$
			gd = new GridData(GridData.BEGINNING);
			b_clone.setLayoutData(gd);
			b_clone.setSelection(true);
			b_clone.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setButtons();		
				}
			});				

			cloneConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cloneConfigSelector.setFont(group.getFont());
			cloneConfigSelector.setItems(getConfigNamesAndDescriptions(cfgds, false));
			int index = cloneConfigSelector.indexOf(newName);
			cloneConfigSelector.select(index < 0 ? 0 : index);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			cloneConfigSelector.setLayoutData(gd);
			cloneConfigSelector.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setButtons();		
				}
			});	
			
			b_real = new Button(group, SWT.RADIO);
			b_real.setText(Messages.getString("NewCfgDialog.1")); //$NON-NLS-1$
			gd = new GridData(GridData.BEGINNING);
			b_real.setLayoutData(gd);
			b_real.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setButtons();		
				}
			});	

			realConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			realConfigSelector.setFont(group.getFont());
			realConfigSelector.setItems(getConfigNamesAndDescriptions(rcfgs, true));
			index = realConfigSelector.indexOf(newName);
			realConfigSelector.select(index < 0 ? 0 : index);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			realConfigSelector.setLayoutData(gd);
			realConfigSelector.setEnabled(false);
			realConfigSelector.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setButtons();		
				}
			});	

			statusLabel = new Label(composite, SWT.CENTER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			statusLabel.setLayoutData(gd);
			statusLabel.setFont(composite.getFont());
			statusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));

			return composite;
		}
		
		/* (non-Javadoc)
		 * Update the status message and button state based on the input selected
		 * by the user
		 * 
		 */
		private void setButtons() {
			String s = null;
			String currentName = configName.getText(); 
			// Trim trailing whitespace
			while (currentName.length() > 0 && Character.isWhitespace(currentName.charAt(currentName.length()-1))) {
				currentName = currentName.substring(0, currentName.length()-1);
			}
			// Make sure that the name is at least one character in length
			if (currentName.length() == 0) {
				// No error message, but cannot select OK
				s = "";	//$NON-NLS-1$
			} else if (cfgds.length == 0) {
				s = "";	//$NON-NLS-1$
				// Make sure the name is not a duplicate
			} else if (isDuplicateName(currentName)) {
				s = NewUIMessages.getFormattedString(DUPLICATE, currentName);
			} else if (isSimilarName(currentName)) {
				s = NewUIMessages.getFormattedString(CASE, currentName);
			} else if (!validateName(currentName)) {
				// TODO Create a decent I18N string to describe this problem
				s = NewUIMessages.getFormattedString(INVALID, currentName);
			} 
			if (statusLabel == null) return;
			Button b = getButton(IDialogConstants.OK_ID);
			if (s != null) {
				statusLabel.setText(s);
				statusLabel.setVisible(true);
				if (b != null) b.setEnabled(false);
			} else {
				statusLabel.setVisible(false);
				if (b != null) b.setEnabled(true);
			}
			
			cloneConfigSelector.setEnabled(b_clone.getSelection());
			realConfigSelector.setEnabled(b_real.getSelection());
		}

}

	public int open() {
		if (parentShell == null) return 1;
		LocalDialog dlg = new LocalDialog(parentShell);		
		return dlg.open();
	}
	
	/**
	 */
	public NewCfgDialog() {
		newName = new String();
		newDescription = new String();
	}
	
	public void setShell(Shell shell) {
		parentShell = shell;
	}	
	
	public void setProject(ICProjectDescription prj) {
		des = prj;
		ICConfigurationDescription[] descs = des.getConfigurations(); 
		cfgds = new IConfiguration[descs.length]; 
		ArrayList lst = new ArrayList();
		for (int i = 0; i < descs.length; ++i) {
			cfgds[i] = ManagedBuildManager.getConfigurationForDescription(descs[i]);
			IConfiguration cfg = cfgds[i];
			for(; cfg != null && !cfg.isExtensionElement(); cfg = cfg.getParent());
			if (cfg != null && !lst.contains(cfg)) lst.add(cfg);
		}
		rcfgs = (IConfiguration[])lst.toArray(new IConfiguration[lst.size()]);
	}

	public void setTitle(String _title) {
		title = _title;
	}
	

	private String [] getConfigNamesAndDescriptions(IConfiguration[] arr, boolean check) {
		String [] names = new String[arr.length];
		for (int i = 0; i < arr.length; ++i)
			names[i] = getNameAndDescription(arr[i]);

		if (check) {
			boolean doubles = false;
			for (int i=0; i<names.length; i++) {
				for (int j=0; j<names.length; j++) {
					if (i != j && names[i].equals(names[j])) {
						doubles = true;
						break;
					}
				}
			}
			if (doubles) {
				for (int i=0; i<names.length; i++) {
					IToolChain tc = arr[i].getToolChain();
					String s = (tc == null) ? NULL : tc.getName();
					names[i] = names[i] + " : " + s; //$NON-NLS-1$ 
				}			
			}
		}
		return names; 
	}
	
	private String getNameAndDescription(IConfiguration cfg) {
		String name = cfg.getName();
		if (name == null) name = NULL;
		if ( (cfg.getDescription() == null) || cfg.getDescription().equals(""))	//$NON-NLS-1$
			return name;
		else
			return name + "( " + cfg.getDescription() +" )";	//$NON-NLS-1$	//$NON-NLS-2$
	}

	protected boolean isDuplicateName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equals(newName)) 
				return true;
		}
		return false;
	}

	protected boolean isSimilarName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equalsIgnoreCase(newName))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * Checks the argument for leading whitespaces and invalid directory name characters. 
	 * @param name
	 * @return <I>true</i> is the name is a valid directory name with no whitespaces
	 */
	private boolean validateName(String name) {
		// Names must be at least one character in length
		if (name.trim().length() == 0)
			return false;
		
		// Iterate over the name checking for bad characters
		char[] chars = name.toCharArray();
		// No whitespaces at the start of a name
		if (Character.isWhitespace(chars[0])) {
			return false;
		}
		for (int index = 0; index < chars.length; ++index) {
			// Config name must be a valid dir name too, so we ban "\ / : * ? " < >" in the names
			if (!Character.isLetterOrDigit(chars[index])) {
				switch (chars[index]) {
				case '/':
				case '\\':
				case ':':
				case '*':
				case '?':
				case '\"':
				case '<':
				case '>':
					return false;
				default:
					break;
				}
			}
		}
		return true;
	}
	
	/**
	 * Create a new configuration, using the values currently set in 
	 * the dialog.
	 */
	private void newConfiguration() {
		String id = ManagedBuildManager.calculateChildId(parentConfig.getId(), null);
		IManagedProject imp = ManagedBuildManager.getBuildInfo(des.getProject()).getManagedProject();
		if (imp == null || !(imp instanceof ManagedProject)) return;
		ManagedProject mp = (ManagedProject) imp;
		try {
			Configuration config = new Configuration(mp, (Configuration)parentConfig, id, false, true);
			CConfigurationData data = config.getConfigurationData();
			ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			config.setConfigurationDescription(cfgDes);
			config.exportArtifactInfo();
			config.setName(newName);
			config.setDescription(newDescription);
		} catch (CoreException e) {
			System.out.println("Cannot create config\n"+ e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
}
