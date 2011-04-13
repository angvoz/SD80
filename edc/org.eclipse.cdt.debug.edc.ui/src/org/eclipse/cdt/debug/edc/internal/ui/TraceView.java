/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class TraceView extends ViewPart implements Protocol.ChannelOpenListener {

	private static final String FILTER_HEARTBEATS = "filter_heartbeats"; //$NON-NLS-1$
	private static final String REUSE_TABS = "reuse_tabs"; //$NON-NLS-1$

	private Composite parent;
	private TabFolder tabs;
	private Label no_data;
	private final Map<TabItem, Page> tab2page = new HashMap<TabItem, Page>();
	private Action clearTabAction;
	private Action closeTabAction;
	private Action exportAction;
	private Action filterAction;
	private Action reuseAction;
	private IMemento memento;

	private class Page implements AbstractChannel.TraceListener {

		final AbstractChannel channel;

		private TabItem tab;
		private Text text;

		private final StringBuffer bf = new StringBuffer();
		private int bf_line_cnt = 0;
		private boolean closed;

		private final Thread update_thread = new Thread() {
			@Override
			public void run() {
				synchronized (Page.this) {
					while (!closed) {
						if (bf_line_cnt > 0) {
							Runnable r = new Runnable() {
								public void run() {
									String str = null;
									int cnt = 0;
									synchronized (Page.this) {
										str = bf.toString();
										cnt = bf_line_cnt;
										bf.setLength(0);
										bf_line_cnt = 0;
									}
									if (text == null)
										return;
									if (text.getLineCount() > 1000 - cnt) {
										String s = text.getText();
										int n = 0;
										int i = -1;
										while (n < cnt) {
											int j = s.indexOf('\n', i + 1);
											if (j < 0)
												break;
											i = j;
											n++;
										}
										if (i >= 0) {
											text.setText(s.substring(i + 1));
										}
									}
									text.append(str);
								}
							};
							getSite().getShell().getDisplay().asyncExec(r);
						}
						try {
							Page.this.wait(1000);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			}
		};

		Page(AbstractChannel channel) {
			this.channel = channel;
			update_thread.start();
		}

		public void dispose() {
			synchronized (this) {
				closed = true;
				update_thread.interrupt();
			}
			try {
				update_thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tab2page.remove(tab);
			tab.dispose();
			tab = null;
			text = null;
			if (tab2page.isEmpty())
				hideTabs();
		}

		public synchronized void onChannelClosed(Throwable error) {
			String msg = "";
			
			if (error == null) {
				msg = "Channel closed";
			} else {
				msg = "Channel terminated: " + error;
			}

			bf.append("\n<================= " + msg + " =================>\n");
			bf_line_cnt += 3;

			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateCloseAction();
				}
			});
		}

		public synchronized void onMessageReceived(char type, String token, String service, String name, byte[] data) {
			if (memento.getBoolean(FILTER_HEARTBEATS)) {
				if (name != null && name.contains("HeartBeat"))
					return;
			}

			try {
				bf.append("Time(ms): " + System.currentTimeMillis() + " Inp: " );
				bf.append(type);
				if (token != null) {
					bf.append(' ');
					bf.append(token);
				}
				if (service != null) {
					bf.append(' ');
					bf.append(service);
				}
				if (name != null) {
					bf.append(' ');
					bf.append(name);
				}
				if (data != null) {
					int i = 0;
					while (i < data.length) {
						int j = i;
						while (j < data.length && data[j] != 0)
							j++;
						bf.append(' ');
						bf.append(new String(data, i, j - i, "UTF8"));
						if (j < data.length && data[j] == 0)
							j++;
						i = j;
					}
				}
				bf.append('\n');
				bf_line_cnt++;
			} catch (UnsupportedEncodingException x) {
				x.printStackTrace();
			}
		}

		public synchronized void onMessageSent(char type, String token, String service, String name, byte[] data) {
			if (memento.getBoolean(FILTER_HEARTBEATS)) {
				if (name != null && name.contains("HeartBeat"))
					return;
			}

			try {
				bf.append("Time(ms): " + System.currentTimeMillis() + " Out: ");
				bf.append(type);
				if (token != null) {
					bf.append(' ');
					bf.append(token);
				}
				if (service != null) {
					bf.append(' ');
					bf.append(service);
				}
				if (name != null) {
					bf.append(' ');
					bf.append(name);
				}
				if (data != null) {
					int i = 0;
					while (i < data.length) {
						int j = i;
						while (j < data.length && data[j] != 0)
							j++;
						bf.append(' ');
						bf.append(new String(data, i, j - i, "UTF8"));
						if (j < data.length && data[j] == 0)
							j++;
						i = j;
					}
				}
				bf.append('\n');
				bf_line_cnt++;
			} catch (UnsupportedEncodingException x) {
				x.printStackTrace();
			}
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento == null)
			this.memento = XMLMemento.createWriteRoot("EDCTRACEVIEW"); //$NON-NLS-1$
		else
			this.memento = memento;

		Preferences p = getViewPreferences(); // never returns null
		this.memento.putBoolean(FILTER_HEARTBEATS, p.getBoolean(FILTER_HEARTBEATS, true));
		this.memento.putBoolean(REUSE_TABS, p.getBoolean(REUSE_TABS, true));
	}

	@Override
	public void saveState(IMemento memento) {
		if (this.memento == null || memento == null)
			return;
		this.memento.putString(FILTER_HEARTBEATS, filterAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		this.memento.putString(REUSE_TABS, reuseAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putMemento(this.memento);

		saveViewPreferences();
	}
	
	private void saveViewPreferences() {
		Preferences preferences = getViewPreferences();
		preferences.putBoolean(FILTER_HEARTBEATS, this.memento.getBoolean(FILTER_HEARTBEATS).booleanValue());
		preferences.putBoolean(REUSE_TABS, this.memento.getBoolean(REUSE_TABS).booleanValue());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// empty
		}
	}

	private Preferences getViewPreferences() {
		return InstanceScope.INSTANCE.getNode(EDCDebugUI.PLUGIN_ID);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		
		createActions();
		
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				IChannel[] arr = Protocol.getOpenChannels();
				for (IChannel c : arr)
					onChannelOpen(c);
				Protocol.addChannelOpenListener(TraceView.this);
			}
		});
		if (tab2page.size() == 0)
			hideTabs();
	}
	
	private void createActions() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		
		clearTabAction = new Action(null) {
			public void run() {
				Page currentPage = getCurrentPage();
				if (currentPage != null) {
					currentPage.text.setText(""); //$NON-NLS-1$
				}
			}
		};

		clearTabAction.setToolTipText("Clear the current tab");
		clearTabAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/etool16/clear_tab.gif")); //$NON-NLS-1$
		
		clearTabAction.setDisabledImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/dtool16/clear_tab.gif")); //$NON-NLS-1$

		toolbarManager.add(clearTabAction);

		closeTabAction = new Action(null) {
			public void run() {
				Page currentPage = getCurrentPage();
				if (currentPage != null) {
					currentPage.dispose();
				}
			}
		};

		closeTabAction.setToolTipText("Close the current tab");
		closeTabAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/etool16/close_tab.gif")); //$NON-NLS-1$
		
		closeTabAction.setDisabledImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/dtool16/close_tab.gif")); //$NON-NLS-1$

		toolbarManager.add(closeTabAction);

		exportAction = new Action(null) {
			public void run() {
				Page currentPage = getCurrentPage();
				if (currentPage != null) {
					handleExport(currentPage);
				}
			}
		};

		exportAction.setToolTipText("Export Log");
		exportAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/etool16/export_log.gif")); //$NON-NLS-1$
		
		exportAction.setDisabledImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
			"/icons/dtool16/export_log.gif")); //$NON-NLS-1$

		toolbarManager.add(exportAction);
		
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();

		filterAction = new Action("Filter heartbeats") { //$NON-NLS-1$
			public void run() {
				memento.putString(FILTER_HEARTBEATS, isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		filterAction.setChecked(memento.getString(FILTER_HEARTBEATS).equals("true")); //$NON-NLS-1$

		mgr.add(filterAction);

		reuseAction = new Action("Reuse tab for channel") { //$NON-NLS-1$
			public void run() {
				memento.putString(REUSE_TABS, isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		reuseAction.setChecked(memento.getString(REUSE_TABS).equals("true")); //$NON-NLS-1$

		mgr.add(reuseAction);
	}
	
	private Page getCurrentPage() {
		if (tabs != null) {
			int index = tabs.getSelectionIndex();
			if (index >= 0) {
				return tab2page.get(tabs.getSelection()[0]);
			}
		}
		return null;
	}

	@Override
	public void setFocus() {
		if (tabs != null)
			tabs.setFocus();
	}

	@Override
	public void dispose() {
		saveViewPreferences();
		
		final Page[] pages = tab2page.values().toArray(new Page[tab2page.size()]);
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				Protocol.removeChannelOpenListener(TraceView.this);
				for (Page p : pages)
					p.channel.removeTraceListener(p);
			}
		});
		for (Page p : pages)
			p.dispose();
		assert tab2page.isEmpty();
		if (tabs != null) {
			tabs.dispose();
			tabs = null;
		}
		if (no_data != null) {
			no_data.dispose();
			no_data = null;
		}
		super.dispose();
	}

	public void onChannelOpen(final IChannel channel) {
		if (!(channel instanceof AbstractChannel))
			return;
		AbstractChannel c = (AbstractChannel) channel;
		
		if (memento.getBoolean(REUSE_TABS)) {
			// see if the same channel is being opened again.  if so just clear
			// and reuse it.
			for (final Page page : tab2page.values()) {
				if (page.channel.getRemotePeer().equals(channel.getRemotePeer())) {
					c.addTraceListener(page);

					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							showTabs();

							// select the new tab automatically
							tabs.setSelection(page.tab);
							updateCloseAction();
						}
					});
					
					return;
				}
			}
		}

		IPeer rp = c.getRemotePeer();
		final String name = rp.getName();
		final String host = rp.getAttributes().get(IPeer.ATTR_IP_HOST);
		final String port = rp.getAttributes().get(IPeer.ATTR_IP_PORT);

		final Page p = new Page(c);
		c.addTraceListener(p);

		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				showTabs();
				p.tab = new TabItem(tabs, SWT.NONE);
				tab2page.put(p.tab, p);
				String title = name;
				if (host != null) {
					title += ", " + host;
					if (port != null) {
						title += ":" + port;
					}
				}
				p.tab.setText(title);
				p.text = new Text(tabs, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
				p.tab.setControl(p.text);
				p.text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				
				// select the new tab automatically
				tabs.setSelection(p.tab);
				updateCloseAction();
			}
		});
	}

	private void showTabs() {
		boolean b = false;
		if (no_data != null) {
			no_data.dispose();
			no_data = null;
			b = true;
		}
		if (tabs == null) {
			tabs = new TabFolder(parent, SWT.NONE);
			tabs.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					updateCloseAction();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			b = true;
		}
		if (b)
			parent.layout();
		
		clearTabAction.setEnabled(true);
		closeTabAction.setEnabled(true);
		exportAction.setEnabled(true);
	}

	private void updateCloseAction() {
		// disable the close action if the current tab
		// has an active channel so people don't accidentally
		// close it and have no way to get it back.
		Page page = getCurrentPage();
		if (page != null) {
			closeTabAction.setEnabled(page.channel.getState() == IChannel.STATE_CLOSED);
		}
	}

	private void hideTabs() {
		boolean b = false;
		if (tabs != null) {
			tabs.dispose();
			tabs = null;
			b = true;
		}
		if (!parent.isDisposed()) {
			if (no_data == null) {
				no_data = new Label(parent, SWT.NONE);
				no_data.setText("No open communication channels at this time.");
				b = true;
			}
			if (b)
				parent.layout();
		}
		
		clearTabAction.setEnabled(false);
		closeTabAction.setEnabled(false);
		exportAction.setEnabled(false);
	}

	private void handleExport(Page currentPage) {
		FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] {"*.log"}); //$NON-NLS-1$
		String path = dialog.open();
		if (path != null) {
			if (path.indexOf('.') == -1 && !path.endsWith(".log")) //$NON-NLS-1$
				path += ".log"; //$NON-NLS-1$
			File outputFile = new Path(path).toFile();
			if (outputFile.exists()) {
				String message = "File exists.  Would you like to overwrite it?";
				if (!MessageDialog.openQuestion(getViewSite().getShell(), "Export Log", message))
					return;
			}

			Writer out = null;
			try {
				out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"); //$NON-NLS-1$
			} catch (IOException ex) {
				return;
			}
			Reader in = new StringReader(currentPage.text.getText());
			copy(in, out);
			try {
				if (in != null)
					in.close();
			} catch (IOException e1) { // do nothing
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e1) { // do nothing
			}
		}
	}

	private void copy(Reader input, Writer output) {
		String line;
		BufferedReader reader = new BufferedReader(input);
		BufferedWriter writer = new BufferedWriter(output);
		try {
			while (reader.ready() && ((line = reader.readLine()) != null)) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) { // do nothing
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e1) { // do nothing
			}
			try
			{
				if (writer != null)
					writer.close();
			} catch (IOException e1) { // do nothing
			}
		}
	}
}
