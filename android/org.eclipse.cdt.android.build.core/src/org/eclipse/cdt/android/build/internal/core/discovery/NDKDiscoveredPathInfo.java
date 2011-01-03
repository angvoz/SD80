package org.eclipse.cdt.android.build.internal.core.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NDKDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IProject project;
	private long lastUpdate = IFile.NULL_STAMP;
	private IPath[] includePaths;
	private Map<String, String> symbols;
	private boolean needReindexing = false;
	
	// Keys for preferences
	public static final String LAST_UPDATE = "lastUpdate";
	
	public NDKDiscoveredPathInfo(IProject project) {
		this.project = project;
		load();
	}
	
	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public IPath[] getIncludePaths() {
		if (needReindexing) {
			// Call for a reindex
			// TODO this is probably a bug. a new include path should trigger reindexing anyway, no?
			// BTW, can't do this in the update since the indexer runs before this gets called
			CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));
			needReindexing = false;
		}
		return includePaths;
	}

	void setIncludePaths(List<String> pathStrings) {
		includePaths = new IPath[pathStrings.size()];
		int i = 0;
		for (String path : pathStrings)
			includePaths[i++] = new Path(path);
		needReindexing = true;
	}
	
	@Override
	public Map<String, String> getSymbols() {
		if (symbols == null)
			symbols = new HashMap<String, String>();
		return symbols;
	}

	void setSymbols(Map<String, String> symbols) {
		this.symbols = symbols;
	}
	
	@Override
	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	public void update(IProgressMonitor monitor) throws CoreException {
		if (!needUpdating())
			return;
		
		new NDKDiscoveryUpdater(this).runUpdate(monitor);
		
		if (includePaths != null && symbols != null) {
			recordUpdate();
			save();
		}
	}

	private boolean needUpdating() {
		if (lastUpdate == IFile.NULL_STAMP)
			return true;
		return project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp() > lastUpdate;
	}
	
	private void recordUpdate() {
		lastUpdate = project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp();
	}
	
	public void delete() {
		lastUpdate = IFile.NULL_STAMP;
	}
	
	private File getInfoFile() {
		File stateLoc = Activator.getDefault().getStateLocation().toFile();
		return new File(stateLoc, project.getName() + ".pathInfo");
	}
	
	private void save() {
		try {
			File infoFile = getInfoFile();
			infoFile.getParentFile().mkdirs();
			PrintStream out = new PrintStream(infoFile);
			
			// timestamp
			out.print("t,");
			out.print(lastUpdate);
			out.println();
			
			for (IPath include : includePaths) {
				out.print("i,");
				out.print(include.toPortableString());
				out.println();
			}
			
			for (Entry<String, String> symbol : symbols.entrySet()) {
				out.print("d,");
				out.print(symbol.getKey());
				out.print(",");
				out.print(symbol.getValue());
				out.println();
			}

			out.close();
		} catch (IOException e) {
			Activator.log(e);
		}
		
	}

	private void load() {
		try {
			File infoFile = getInfoFile();
			if (!infoFile.exists())
				return;
			
			long timestamp = IFile.NULL_STAMP;
			List<IPath> includes = new ArrayList<IPath>();
			Map<String, String> defines = new HashMap<String, String>();
			
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				switch (line.charAt(0)) {
				case 't':
					timestamp = Long.valueOf(line.substring(2));
					break;
				case 'i':
					includes.add(Path.fromPortableString(line.substring(2)));
					break;
				case 'd':
					int n = line.indexOf(',', 2);
					if (n == -1)
						defines.put(line.substring(2), "");
					else
						defines.put(line.substring(2, n), line.substring(n + 1));
					break;
				}
			}
			reader.close();
			
			lastUpdate = timestamp;
			includePaths = includes.toArray(new IPath[includes.size()]);
			symbols = defines;
		} catch (IOException e) {
			Activator.log(e);
		}
	}
}