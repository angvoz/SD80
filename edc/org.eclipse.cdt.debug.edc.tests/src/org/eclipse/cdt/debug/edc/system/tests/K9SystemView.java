/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.system.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.cdt.debug.edc.internal.ui.views.SystemDMContainer;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemDataModel;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemVMContainer;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemView;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemViewModel;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class K9SystemView extends SystemView {

	public static final String VIEW_ID = "org.eclipse.cdt.debug.edc.system.tests.K9SystemView";
	
	public class K9DataModel extends SystemDataModel {

		private SystemDMContainer allDogs = new SystemDMContainer();

		private SystemDMContainer allToys = new SystemDMContainer();

		@Override
		public void buildDataModel(IProgressMonitor monitor) throws CoreException {
			// Create the data model
			allDogs = new SystemDMContainer();
			{
				Map<String, Object> props = new HashMap<String, Object>();

				props.put("breed", "Border Terrier");
				new SystemDMContainer(allDogs, "Indy", props);
				props.put("breed", "Bouvier");
				new SystemDMContainer(allDogs, "Jessica", props);
				new SystemDMContainer(allDogs, "Emmalee", props);
				props.put("breed", "Italian Greyhound");
				new SystemDMContainer(allDogs, "Poppy", props);
				props.put("breed", "Wippet");
				new SystemDMContainer(allDogs, "Sophie", props);   			
			}

			String[] dogStatus = new String[]{"Barking", "Watching", "Sleeping", "Running", "Howling"};
			for (SystemDMContainer dog : allDogs.getChildren()) {
				dog.getProperties().put("STATUS", dogStatus[new Random().nextInt(dogStatus.length - 1)]);
			}

			allToys = new SystemDMContainer();
			{
				Map<String, Object> props = new HashMap<String, Object>();

				new SystemDMContainer(allToys, "Big Bone", props);
				new SystemDMContainer(allToys, "Kong", props);
				new SystemDMContainer(allToys, "Chewy Postman", props);
				new SystemDMContainer(allToys, "Frisbee", props);
				new SystemDMContainer(allToys, "Rope Toy", props);
			}
			
			for (SystemDMContainer toy : allToys.getChildren()) {
				int remainingValue = new Random().nextInt(100);
				toy.getProperties().put("REMAINING_VALUE", remainingValue);
				toy.getProperties().put("REMAINING", remainingValue + "%");
			}

		}

		public void setAllDogs(SystemDMContainer allDogs) {
			this.allDogs = allDogs;
		}

		public SystemDMContainer getAllDogs() {
			return allDogs;
		}

		public void setAllToys(SystemDMContainer allToys) {
			this.allToys = allToys;
		}

		public SystemDMContainer getAllToys() {
			return allToys;
		}
		
	}

	public class K9ViewModel extends SystemViewModel {

		private List<SystemVMContainer> rootVMContainers = new ArrayList<SystemVMContainer>();

		@Override
		public void buildViewModel() {
			rootVMContainers = new ArrayList<SystemVMContainer>();
			rootVMContainers.add(getOverviewVMContainer());
			rootVMContainers.add(getBreedsVMContainer());
			rootVMContainers.add(getDogsVMContainer());
			rootVMContainers.add(getToysVMContainer());
		}

		@Override
		public List<SystemVMContainer> getRootContainers() {
			return rootVMContainers;
		}

		public SystemVMContainer getOverviewVMContainer() {
			SystemVMContainer root = new SystemVMContainer(null, "Overview");
			root.getProperties().put(SystemVMContainer.PROP_ID, getPresentationContext().getId() + "_overview");
			SystemVMContainer breeds = new SystemVMContainer(root, "Breeds");
			SystemVMContainer dogs = new SystemVMContainer(root, "Dogs");

			Map<String, SystemVMContainer> breedVMContainers = new HashMap<String, SystemVMContainer>();

			StringMatcher  matcher = getFilterMatcher();
			for (SystemDMContainer dog : getAllDogs().getChildren()) {
				if (matcher.match(dog.getName()))
				{
					String breed = (String) dog.getProperties().get("breed");
					SystemVMContainer breedContainer = breedVMContainers.get(breed);
					if (breedContainer == null)
					{
						breedContainer = new SystemVMContainer(breeds, breed);
						breedVMContainers.put(breed, breedContainer);
					}
					new SystemVMContainer(breedContainer, dog);
					new SystemVMContainer(dogs, dog);
				}
			}		
			
			SystemVMContainer toys = new SystemVMContainer(root, "Toys");
			for (SystemDMContainer toy : getAllToys().getChildren()) {
				if (matcher.match(toy.getName()))
					new SystemVMContainer(toys, toy);
			}
			
			return root;
		}

		public SystemVMContainer getBreedsVMContainer() {
			SystemVMContainer root = new SystemVMContainer(null, "Breeds");

			root.getProperties().put(SystemVMContainer.PROP_ID, getPresentationContext().getId() + "_breeds");
			String[] column_keys = new String[] { SystemVMContainer.PROP_NAME, "COUNT" };
			Map<String, String> column_names = new HashMap<String, String>();
			column_names.put(SystemVMContainer.PROP_NAME, "Description");
			column_names.put("COUNT", "Count");
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_KEYS, column_keys);
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_NAMES, column_names);

			
			Map<String, SystemVMContainer> breedVMContainers = new HashMap<String, SystemVMContainer>();

			StringMatcher  matcher = getFilterMatcher();

			for (SystemDMContainer dog : getAllDogs().getChildren()) {
				if (matcher.match(dog.getName()))
				{
					String breed = (String) dog.getProperties().get("breed");
					SystemVMContainer breedContainer = breedVMContainers.get(breed);
					if (breedContainer == null)
					{
						breedContainer = new SystemVMContainer(root, breed);
						breedVMContainers.put(breed, breedContainer);
					}
					new SystemVMContainer(breedContainer, dog);
				}
			}
			
			for (SystemVMContainer breedContainer : breedVMContainers.values())
			{
				breedContainer.getProperties().put("COUNT", Integer.toString(breedContainer.getChildren().size()));
			}

			return root;
		}

		public SystemVMContainer getDogsVMContainer() {
			SystemVMContainer root = new SystemVMContainer(null, "Dogs");
			root.getProperties().put(SystemVMContainer.PROP_ID, getPresentationContext().getId() + "_dogs");

			String[] column_keys = new String[] { SystemVMContainer.PROP_NAME, "STATUS" };
			Map<String, String> column_names = new HashMap<String, String>();
			column_names.put(SystemVMContainer.PROP_NAME, "Description");
			column_names.put("STATUS", "Status");
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_KEYS, column_keys);
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_NAMES, column_names);

			
			StringMatcher  matcher = getFilterMatcher();

			for (SystemDMContainer dog : getAllDogs().getChildren()) {
				if (matcher.match(dog.getName()))
					new SystemVMContainer(root, dog);
			}		

			return root;
		}

		public SystemVMContainer getToysVMContainer() {
			SystemVMContainer root = new SystemVMContainer(null, "Toys");
			root.getProperties().put(SystemVMContainer.PROP_ID, getPresentationContext().getId() + "_toys");

			String[] column_keys = new String[] { SystemVMContainer.PROP_NAME, "REMAINING" };
			Map<String, String> column_names = new HashMap<String, String>();
			column_names.put(SystemVMContainer.PROP_NAME, "Description");
			column_names.put("REMAINING", "Remaining");
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_KEYS, column_keys);
			root.getProperties().put(SystemVMContainer.PROP_COLUMN_NAMES, column_names);
		
			StringMatcher  matcher = getFilterMatcher();

			for (SystemDMContainer toy : getAllToys().getChildren()) {
				if (matcher.match(toy.getName()))
				new SystemVMContainer(root, toy);
			}

			return root;
		}

	}
	
	@Override
	public void createPartControl(Composite parent) {
		setPresentationContext(new PresentationContext(VIEW_ID));
		setDataModel(new K9DataModel());
		setViewModel(new K9ViewModel());
		getViewModel().buildViewModel();
		createRootComosite(parent);
		setRefreshInterval(5000);		
		createRefreshAction();
		contributeToActionBars();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public SystemDMContainer getAllDogs() {
		return ((K9DataModel)getDataModel()).getAllDogs();
	}

	public SystemDMContainer getAllToys() {
		return ((K9DataModel)getDataModel()).getAllToys();
	}

}
