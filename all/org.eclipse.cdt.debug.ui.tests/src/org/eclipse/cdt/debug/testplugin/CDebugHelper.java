package org.eclipse.cdt.debug.testplugin;

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.*;
import org.eclipse.cdt.debug.mi.core.*;
import org.eclipse.core.runtime.Path;


/**
 * Helper methods to set up a Debug session.
 */
public class CDebugHelper {
	
	

	/**
	 * Creates a ICDISession.
	 */	
	public static ICDISession createSession(String exe) throws IOException, MIException  {
		MIPlugin mi;
        ICDISession session;
        String os = System.getProperty("os.name");
        String exename;
        mi=MIPlugin.getDefault();
        
        exename=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.debug.ui.tests").find(new Path("/")).getFile();
        exename+="core/org/eclipse/cdt/debug/core/tests/resources/";
        os=os.toLowerCase();
        /* We need to get the correct executable to execute
         */
        if (os.indexOf("windows")!=-1)
            exename+="win/"+ exe +".exe";
        else if (os.indexOf("qnx")!=-1) 
            exename+="qnx/" + exe;
        else if (os.indexOf("linux")!=-1)
            exename+="linux/"+exe;
        else if (os.indexOf("sol")!=-1) 
            exename+="sol/" + exe;
        else
           return(null);
        session=mi.createCSession(null, null, null, exename);
		return(session);
	}
	

}

