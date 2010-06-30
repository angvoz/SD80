
These sources are synchronized from the upstream project:

URL: http://dev.eclipse.org/svnroot/dsdp/org.eclipse.tm.tcf/trunk/agent
Repository Root: http://dev.eclipse.org/svnroot/dsdp/org.eclipse.tm.tcf
Repository UUID: 6a79697e-3843-0410-8446-a9668620458d
Revision: 1003

Please avoid modifying sources outside of #ifdef WIN32 blocks.
If possible, use mdep.c/mdep.h to isolate Symbian-specific code by
redefining a loc_* function/macro or renaming a function/macro which
is problematic in MinGW.

For any significant changes, please file bugs against the "Target Platform"
product, "TCF" component in Eclipse and make patches to send to them.

Current open patches:

--  none
 
===============

To merge with changes from upstream:

-- Check out or update sources from the URL above.

-- Execute "svn diff -r<...> > path/to/changes.patch" (substituting the Revision: number above)

-- Apply patch to the tcf_agent directory:

	patch -p0 -N < path/to/changes.patch

-- (Or manually sync the directories.)

-- Be sure to add any new files and remove any deleted files before 
committing to CVS.	

