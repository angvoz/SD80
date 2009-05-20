/*******************************************************************************
 * Copyright (c) 2007 - 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Anna Dushistova(MontaVista) - Hardware Debugging: Host name or ip address not saving in the debug configuration, bug 241279
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import java.io.File;

import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Doug Schaefer, Adrian Petrescu
 *
 */
public class GDBJtagDebuggerTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "Debugger";
	
	private CommandFactoryDescriptor[] cfDescs;
	
	private Text gdbCommand;
	private Combo commandFactory;
	private Combo miProtocol;
	private Button verboseMode;
	private Button useRemote;
	private Text ipAddress;
	private Text portNumber;
	private Combo jtagDevice;
	private String savedJtagDevice;

	
	public String getName() {
		return TAB_NAME;
	}

	public Image getImage() {
		return GDBJtagImages.getDebuggerTabImage();
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		
		Group group = new Group(comp, SWT.NONE);
		layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.gdbSetupGroup_Text"));
		
		createCommandControl(group);
		createCommandFactoryControl(group);
		createProtocolControl(group);
		createVerboseModeControl(group);
		
		createRemoteControl(comp);
	}

	private void browseButtonSelected(String title, Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		if (str != null)
			text.setText(str);
	}
	
	private void variablesButtonSelected(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
				getShell());
		if (dialog.open() == StringVariableSelectionDialog.OK) {
			text.append(dialog.getVariableExpression());
		}
	}
	
	private void createCommandControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		comp.setLayoutData(gd);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandLabel"));
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		
		gdbCommand = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gdbCommand.setLayoutData(gd);
		gdbCommand.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("GDBJtagDebuggerTab.gdbCommandBrowse_Title"), gdbCommand);
			}
		});
		
		button = new Button(comp, SWT.NONE);
		button.setText(Messages.getString("GDBJtagDebuggerTab.gdbCommandVariable"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				variablesButtonSelected(gdbCommand);
			}
		});
	}
	
	private void createCommandFactoryControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.commandFactoryLabel"));
		
		commandFactory = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		// Get the command sets
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		cfDescs = cfManager.getDescriptors(IGDBJtagConstants.DEBUGGER_ID);
		for (int i = 0; i < cfDescs.length; ++i) {
			commandFactory.add(cfDescs[i].getName());
		}
		
		commandFactory.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				commandFactoryChanged();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createProtocolControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.miProtocolLabel"));
		
		miProtocol = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		miProtocol.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void commandFactoryChanged() {
		int currsel = miProtocol.getSelectionIndex();
		String currProt = null;
		if (currsel >= 0)
			currProt = miProtocol.getItem(currsel);
		miProtocol.removeAll();
		int cfsel = commandFactory.getSelectionIndex();
		if (cfsel >= 0) {
			String[] protocols = cfDescs[cfsel].getMIVersions();
			for (int i = 0; i < protocols.length; ++i) {
				miProtocol.add(protocols[i]);
				if (currProt != null && protocols[i].equals(currProt))
					miProtocol.select(i);
			}
		}
		if (miProtocol.getSelectionIndex() < 0 && miProtocol.getItemCount() > 0)
			miProtocol.select(0);
	}
	
	private void createVerboseModeControl(Composite parent) {
		verboseMode = new Button(parent, SWT.CHECK);
		verboseMode.setText(Messages.getString("GDBJtagDebuggerTab.verboseModeLabel"));
		verboseMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createRemoteControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagDebuggerTab.remoteGroup_Text"));
		
		useRemote = new Button(group, SWT.CHECK);
		useRemote.setText(Messages.getString("GDBJtagDebuggerTab.useRemote_Text"));
		useRemote.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useRemoteChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.jtagDeviceLabel"));
		
		jtagDevice = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.
			getInstance().getGDBJtagDeviceContribution();
		for (int i = 0; i < availableDevices.length; i++) {
			jtagDevice.add(availableDevices[i].getDeviceName());
		}
		
		jtagDevice.select(0);
		jtagDevice.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateDeviceIpPort(jtagDevice.getText());
				updateLaunchConfigurationDialog();
			}
		});
		
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.ipAddressLabel"));
		ipAddress = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 100;
		ipAddress.setLayoutData(gd);
		ipAddress.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("GDBJtagDebuggerTab.portNumberLabel"));
		portNumber = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 100;
		portNumber.setLayoutData(gd);
		portNumber.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
			}
		});
		portNumber.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * @param text
	 */
	protected void updateDeviceIpPort(String selectedDeviceName) {
		if (selectedDeviceName.equals(savedJtagDevice)) {
			return;
		}
		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance()
				.getGDBJtagDeviceContribution();
		IGDBJtagDevice selectedDevice = null;
		for (int i = 0; i < availableDevices.length; i++) {
			String name = availableDevices[i].getDeviceName();
			if (name.equals(selectedDeviceName)) {
				selectedDevice = availableDevices[i].getDevice();
				if (selectedDevice != null) {
					String ip = selectedDevice.getDefaultIpAddress();
					ipAddress.setText(ip);
					String port = selectedDevice.getDefaultPortNumber();
					portNumber.setText(port);
					updateLaunchConfigurationDialog();
					break;
				}
			}
		}
	}

	private void useRemoteChanged() {
		boolean enabled = useRemote.getSelection();
		jtagDevice.setEnabled(enabled);
		ipAddress.setEnabled(enabled);
		portNumber.setEnabled(enabled);
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String gdbCommandAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
			gdbCommand.setText(gdbCommandAttr);
			
			CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
			CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
			String commandFactoryAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
			int cfid = 0;
			for (int i = 0; i < cfDescs.length; ++i)
				if (cfDescs[i].getName().equals(commandFactoryAttr)) {
					cfid = i;
					break;
				}
			commandFactory.select(cfid); // populates protocol list too

			String miProtocolAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
			int n = miProtocol.getItemCount();
			for (int i = 0; i < n; ++i) {
				if (miProtocol.getItem(i).equals(miProtocolAttr)) {
					miProtocol.select(i);
				}
			}
			
			boolean verboseModeAttr = configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
			verboseMode.setSelection(verboseModeAttr);

			boolean useRemoteAttr = configuration.getAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
			useRemote.setSelection(useRemoteAttr);
			useRemoteChanged();
			
			String ipAddressAttr = configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, IGDBJtagConstants.DEFAULT_IP_ADDRESS);
			ipAddress.setText(ipAddressAttr);
			
			int portNumberAttr = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, IGDBJtagConstants.DEFAULT_PORT_NUMBER);
			portNumber.setText(String.valueOf(portNumberAttr));
			
			savedJtagDevice = configuration.getAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, "");
			for (int i = 0; i < jtagDevice.getItemCount(); i++) {
				if (jtagDevice.getItem(i).equals(savedJtagDevice)) {
					jtagDevice.select(i);
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbCommand.getText().trim());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, commandFactory.getText());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, miProtocol.getText());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, verboseMode.getSelection());
		savedJtagDevice = jtagDevice.getText();
		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, savedJtagDevice);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, useRemote.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, ipAddress.getText().trim());
		try {
			configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, Integer.parseInt(portNumber.getText().trim()));
		} catch (NumberFormatException e) {
			configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, 0);
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);
		configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, IGDBJtagConstants.DEFAULT_IP_ADDRESS);
		configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, IGDBJtagConstants.DEFAULT_PORT_NUMBER);
	}

}
