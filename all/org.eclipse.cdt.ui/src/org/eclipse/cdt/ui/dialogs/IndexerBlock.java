/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * @author Bogdan Gheorghe
 */

/**
 * This <code>IndexerBlock2</code> is used in the <code>MakeProjectWizardOptionPage</code> and
 * the <code>NewManagedProjectOptionPage</code> to display the indexer options during the creation of
 * a new project.
 */

public class IndexerBlock extends AbstractCOptionPage {

	private static final String INDEXER_LABEL = CUIPlugin.getResourceString("BaseIndexerBlock.label" ); //$NON-NLS-1$
	private static final String INDEXER_DESCRIPTION = CUIPlugin.getResourceString("BaseIndexerBlock.desc"); //$NON-NLS-1$
	private static final String INDEXER_COMBO_LABEL = CUIPlugin.getResourceString("BaseIndexerBlock.comboLabel"); //$NON-NLS-1$
	
    private Combo 					indexersComboBox;
    private HashMap 				indexerPageMap;
    private List                    indexerPageList;
    private String 					selectedIndexerId = null;
	private Composite 				parentComposite;
    private ICOptionPage 		 	currentPage;
    
	String initialSelected;
	
    public IndexerBlock(){
		super(INDEXER_LABEL);
		setDescription(INDEXER_DESCRIPTION);
		initializeIndexerPageMap();
	}
    
    /**
     * Create a profile page only on request
     */
    protected static class IndexerPageConfiguration {

        ICOptionPage page;
        IConfigurationElement element;

        public IndexerPageConfiguration(IConfigurationElement _element) {
            element = _element;
        }

        public ICOptionPage getPage() throws CoreException {
            if (page == null) {
                page = (ICOptionPage) element.createExecutableExtension("class"); //$NON-NLS-1$
            }
            return page;
        }
        public String getName() {
            return element.getAttribute("name"); //$NON-NLS-1$
        }
        public String getIndexerID(){
        	return element.getAttribute("indexerID"); //$NON-NLS-1$
        }
    }

    public void createControl(Composite parent) {
        
        Composite composite = ControlFactory.createComposite(parent, 1);
        Font font = parent.getFont();
		GridLayout layout=  ((GridLayout)composite.getLayout());
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.verticalSpacing= 0;
		layout.horizontalSpacing= GridData.FILL_HORIZONTAL;
		composite.setLayoutData(null);
		composite.setFont(font);
		setControl(composite);
      
        Composite scComp = ControlFactory.createComposite(composite, 1);
        ((GridLayout)scComp.getLayout()).marginHeight = 0;
        ((GridLayout)scComp.getLayout()).marginTop = 5;
        scComp.setFont(font);
	        
        // Create a group for discovered indexer's UI
        if (createIndexerControls(scComp)) {
          // create a composite for discovery profile options
            Composite indexPageComposite = ControlFactory.createComposite(composite, 1);
            indexPageComposite.setFont(font);
            indexPageComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
            indexPageComposite.setLayout(new TabFolderLayout());
            // Must set the composite parent to super class.
            parentComposite = indexPageComposite;
            
            setPage();
           
        }
        parent.layout(true);
    }
	/**
	 * 
	 */
	private void setPage() {
	
        String profileId = getCurrentIndexPageId();
        
        //If no indexer has been selected, return
        if (profileId == null)
        	return;
        
        ICOptionPage page = getIndexerPage(profileId);
        if (page != null) {
            if (page.getControl() == null) {
                page.setContainer(getContainer());
                page.createControl(parentComposite);
                parentComposite.layout(true);
            }
            
            if (currentPage != null){
                currentPage.setVisible(false);
            }
            
            page.setVisible(true);
        }
		
		setCurrentPage(page);
		
		if (page instanceof AbstractIndexerPage){
			((AbstractIndexerPage) page).loadPreferences();
		}
	}

	/**
	 * @param page
	 */
	private void setCurrentPage(ICOptionPage page) {
		currentPage = page;
	}

	protected String getCurrentIndexPageId() {
        String selectedIndexPageName = getSelectedIndexerID();
        
        if (selectedIndexPageName == null)
        	return null;
        
        
        String selectedIndexPageId = getIndexerPageId(selectedIndexPageName);

        return selectedIndexPageId;
    }
	/**
     * @param scComp
     * @param numberOfColumns
     * @return
     */
    private boolean createIndexerControls(Composite parent) {
        Group group= ControlFactory.createGroup(parent,INDEXER_COMBO_LABEL,2);
       
       // Add discovered indexers combo box
        indexersComboBox = ControlFactory.createSelectCombo(group,"", ""); //$NON-NLS-1$ //$NON-NLS-2$
  	
        //Add combo box listener
        indexersComboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPage();
			}
		});
		//Make sure that the combo box takes up two cells
        GridData gridData = (GridData) indexersComboBox.getLayoutData();
		gridData.verticalSpan=2;
		
        // fill the combobox and set the initial value
        for (Iterator items = getIndexerPageIdList().iterator(); items.hasNext();) {
            String profileId = (String)items.next();
            String pageName = getIndexerPageName(profileId);
            if (pageName != null) {
                indexersComboBox.add(pageName);
            }
        }

		//See what the preferred indexer is
		String indexerId = CCorePlugin.getPDOMManager().getDefaultIndexerId();
		String preferredIndexer = getIndexerPageName(indexerId);
        String[] indexerList = indexersComboBox.getItems();
        int selectedIndex = 0;
        for (int i=0; i<indexerList.length; i++){
        	if (indexerList[i].equals(preferredIndexer))
        		selectedIndex = i;
        }
        
        indexersComboBox.select(selectedIndex);
    
        return true;
    }
    
    /**
     * Adds all the contributed Indexer Pages to a map
     */
    private void initializeIndexerPageMap() {
        indexerPageMap = new HashMap(5);
        indexerPageList = new ArrayList(5);
        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.getPluginId(), "IndexerPage"); //$NON-NLS-1$
        IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals("indexerUI")) { //$NON-NLS-1$
                String id = infos[i].getAttribute("indexerID"); //$NON-NLS-1$
                indexerPageMap.put(id, new IndexerPageConfiguration(infos[i]));
                indexerPageList.add(id);
            }
        }
    }

    
    /**
     * Returns the contributed Indexer Pages as a list
     */
    protected List getIndexerPageIdList() {
        return indexerPageList;
    }
  
    /**
     * Returns the page name for the given id
     * @param profileId 
     * @return String 
     */
    protected String getIndexerPageName(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            return configElement.getName();
        }
        return null;
    }
    /**
     * Returns the indexer page id for the give name
     * @param indexerPageName 
     * @return String 
     */
    public String getIndexerPageId(String indexerPageName) {
        for (Iterator I = indexerPageMap.keySet().iterator(); I.hasNext();) {
            String indexerPageId = (String) I.next();
            String tempPageName = getIndexerPageName(indexerPageId);
            if (indexerPageName.equals(tempPageName)) {
                return indexerPageId;
            }
        }
        return null;
    }
    
    protected ICOptionPage getIndexerPage(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            try {
                return configElement.getPage();
            } catch (CoreException e) {
            }
        }
        return null;
    }
    
    /**
     * Returns the indexer id for the given name
     * @param profileId 
     * @return String 
     */
    protected String getIndexerIdName(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            return configElement.getIndexerID();
        }
        return null;
    }
    
    public void performApply(IProgressMonitor monitor) throws CoreException {
		//Get the currently selected indexer from the UI
		String indexerName = getSelectedIndexerID();
		//If no indexer has been selected, return
		if (indexerName == null)
			return; 
		
		//Match up the selected indexer in the UI to a corresponding 
		//contributed Indexer Page ID
		String indexerPageID = getIndexerPageId(indexerName);
		
		if (indexerPageID == null)
			return;
	
		//Get the id of the indexer that goes along with this UI page - this gets persisted
		final String indexerID = getIndexerIdName(indexerPageID);
    	//
    	if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(CUIMessages.getString("IndexerOptions.task.savingAttributes"), 2); //$NON-NLS-1$
		final String selected = indexerID;
		
		if (indexerID != null) {
			ICOptionContainer container = getContainer();
			final IProject project = (container != null)
				? container.getProject()
				: ((AbstractIndexerPage) currentPage).getCurrentProject();
		
			if ( project != null) {
				ICProject cproject = CoreModel.getDefault().create(project);
				IPDOMManager manager = CCorePlugin.getPDOMManager();
				if (!indexerID.equals(manager.getIndexerId(cproject)))
					manager.setIndexerId(cproject, indexerID);
				if (currentPage != null && currentPage.getControl() != null) {
					currentPage.performApply(new SubProgressMonitor(monitor, 1));
				}
			} else {
				if (initialSelected == null || !selected.equals(initialSelected)) {
					
					// First clean out the old indexer settings
					String oldId = CCorePlugin.getPDOMManager().getDefaultIndexerId();
					ICOptionPage tempPage = getIndexerPage(oldId);
					if (tempPage instanceof AbstractIndexerPage)
						((AbstractIndexerPage) tempPage).removePreferences();
							
					CCorePlugin.getPDOMManager().setDefaultIndexerId(indexerID);
				}
				monitor.worked(1);
				// Give a chance to the contributions to save.
				ICOptionPage page = currentPage;
				if (page != null && page.getControl() != null) {
					page.performApply(new SubProgressMonitor(monitor, 1));
				}
			
			}
			initialSelected = selected;
		}
		monitor.done();
    }

    /**
     * Persists BasicIndexerBlock settings to disk and allows current indexer page to persist settings
     * This is needed since we need to pass in the project if we are trying to save changes made to the 
     * property page.
     */
    public void persistIndexerSettings(ICProject project, IProgressMonitor monitor) throws CoreException{
    	if (currentPage instanceof AbstractIndexerPage)
    		((AbstractIndexerPage)currentPage).setCurrentProject(project);
    	
    	this.performApply(monitor);		
    }
	
	public void resetIndexerPageSettings(ICProject project){
		if (currentPage instanceof AbstractIndexerPage)
    		((AbstractIndexerPage)currentPage).setCurrentProject(project);
		
		this.performDefaults();
	}

    public void performDefaults() {
		//Give a chance to the contributions to perform defaults.
		ICOptionPage page = currentPage;
		if (page != null && page.getControl() != null) {
			page.performDefaults();
		}
    }

	/**
	 * @return
	 */
	public boolean isIndexEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param oldIndexerID
	 * @param project
	 */
	public void setIndexerID(String indexerID, ICProject project) {
		//Get the corresponding text for the given indexer id
		selectedIndexerId = getIndexerPageName(indexerID);
		//Store the currently selected indexer id
		initialSelected = indexerID;
		
		if (selectedIndexerId == null){
			CCorePlugin.getDefault().getPluginPreferences().setValue(CCorePlugin.PREF_INDEXER, PDOMNullIndexer.ID);
			selectedIndexerId = PDOMNullIndexer.ID;
		}
		
		//Set the appropriate indexer in the combo box
		indexersComboBox.setText(selectedIndexerId);
		//Load the appropriate page
		setPage();	
		//Give the contributed page a chance to initialize
		if (currentPage instanceof AbstractIndexerPage){
			((AbstractIndexerPage) currentPage).initialize(project);
		}
	}
	 
	public String getSelectedIndexerID(){
		String indexerID = null;
		
		int selIndex = indexersComboBox.getSelectionIndex();
		
		//If no indexer has been selected return
		if (selIndex != -1)
		 indexerID = indexersComboBox.getItem(selIndex);
		
		return indexerID;
	}
	
	public IProject getProject() {
		ICOptionContainer container = getContainer();
		if (container != null){
			return container.getProject();
		} else {
			return ((AbstractIndexerPage)currentPage).getCurrentProject();
		}
	
	}
	
}
