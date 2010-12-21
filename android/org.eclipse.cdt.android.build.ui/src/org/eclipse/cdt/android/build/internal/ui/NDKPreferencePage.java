package org.eclipse.cdt.android.build.internal.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NDKPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Text text;

	/**
	 * Create the preference page.
	 */
	public NDKPreferencePage() {
	}

	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		Group grpNdkLocation = new Group(container, SWT.NONE);
		grpNdkLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpNdkLocation.setText("NDK Location");
		grpNdkLocation.setLayout(new GridLayout(2, false));
		
		text = new Text(grpNdkLocation, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button = new Button(grpNdkLocation, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String dir = new DirectoryDialog(NDKPreferencePage.this.getShell()).open();
				if (dir != null)
					text.setText(dir);
			}
		});
		button.setText("Browse...");

		return container;
	}

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		return super.performOk();
	}
	
}
