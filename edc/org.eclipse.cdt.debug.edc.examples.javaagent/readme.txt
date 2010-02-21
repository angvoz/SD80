/*******************************************************************************
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. April, 2009
 *******************************************************************************/

This is an example TCF agent written in Java.

============================================================================
How to run the agent
============================================================================

It's recommended you start the JUnit test
	org.eclipse.cdt.debug.edc.tests.ManualTCFAgentTest
before start the agent. The unit test will tell you whether a new agent is detected
and what services are provided by the agent.

There are two ways to run the agent.

1. Run it as an Eclipse plugin. 
   (NOTE 2009-12-09: This is disabled by default. Enable the menu in plugin.xml)
   
   Run/debug the workspace as Eclipse application. Then in the target workbench, 
   select menu "EDC->Start Agent" to launch the agent. Select "EDC -> Stop Agent"
   to stop it.

2. Run it as a stand-alone Java application in one of the two ways.
   1) In Eclipse select file TCFAgentInJava and run it as a Java application.
   2) Export the project as a "Runnable JAR file", which will produce a jar file,
      say, agent.jar.  Then run it from command line:
           java -jar agent.jar 

============================================================================
Source Package Description
============================================================================

1. org.eclipse.cdt.debug.edc.examples.javaagent
   - contains code for the agent.
   - Implementation highlights:
     * Create a ServerTCP to listen for connection
     * Create and register IServiceProvider object indicating to client that 
       we are offering a new service called "CalculatorService". 

2. org.eclipse.cdt.debug.edc.examples.javaagent.remote
   - contains code for TCF client to request service from the agent.
   - You can refer to the unit test mentioned above for how to use those code in
     client.
   - Implementation highlights:
     * Provide the ICalculator service API to the client.
     * Provide the service proxy to the client. Client side needs to make sure
       this is called:
       		CalculatorServiceProxy.registerProxyForClient()
       See comment in the code for detail.
