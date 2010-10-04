package org.eclipse.cdt.debug.edc.internal.windows.ui;

import org.eclipse.cdt.debug.edc.ui.EDCAdapterFactory;
import org.eclipse.cdt.debug.edc.windows.launch.WindowsLaunch;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public class WindowsAdapterFactory extends EDCAdapterFactory {

	private static final ImageDescriptor LAUNCH_NODE_IMGDESC = AbstractUIPlugin
	.imageDescriptorFromPlugin(WindowsDebuggerUI.PLUGIN_ID,
			"/icons/obj16/launch.png"); //$NON-NLS-1$

	@Override
	protected Object createLabelProvider() {
        return new IElementLabelProvider() {
			
			public void update(ILabelUpdate[] updates) {
				for (ILabelUpdate iLabelUpdate : updates) {
					Object element = iLabelUpdate.getElement();
					if (element != null && element instanceof WindowsLaunch)
					{
						WindowsLaunch wLaunch = (WindowsLaunch) element;
						iLabelUpdate.setLabel(wLaunch.getDescription(), 0);
						iLabelUpdate.setImageDescriptor(LAUNCH_NODE_IMGDESC, 0);
					}
					iLabelUpdate.done();
				}
			}
		};
    }

}
