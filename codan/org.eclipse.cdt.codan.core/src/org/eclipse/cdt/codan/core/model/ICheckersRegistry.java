/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;

/**
 * This interface an API to add/remove checker and problems programmatically,
 * get problem profiles and change problem default settings
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 */
public interface ICheckersRegistry extends Iterable<IChecker> {
	/**
	 * Iterator for registered checkers
	 * @return
	 */
	public abstract Iterator<IChecker> iterator();

	/**
	 * Add another checker
	 * @param checker
	 */
	public abstract void addChecker(IChecker checker);

	/**
	 * Add problem p with default category by category id into default profile, category must exists in default profile
	 * @param p - problem
	 * @param categoryId - category id
	 */
	public abstract void addProblem(IProblem p, String categoryId);

	/**
	 * Add subcategory with id categoryId into parent category, 
	 * if parent does not exist in default, profile - if not will be added to the root
	 * @param category - new category
	 * @param parentCategoryId - parent category id
	 */
	public abstract void addCategory(IProblemCategory category, String parentCategoryId);

	/**
	 * Add problem reference to a checker, i.e. claim that checker can produce this problem.
	 * If checker does not claim any problems it cannot be enabled.
	 * @param c - checker
	 * @param p - problem
	 */
	public abstract void addRefProblem(IChecker c, IProblem p);

	/**
	 * Get default profile, default profile is kind of "Installation Default". Always the same, comes from default in checker extensions
	 * @return
	 */
	public abstract IProblemProfile getDefaultProfile();

	/**
	 * Get workspace profile. User can change setting for workspace profile.
	 * @return profile
	 */
	public abstract IProblemProfile getWorkspaceProfile();

	/**
	 * Get resource profile. For example given directory can have different profile
	 * than parent project.
	 * 
	 * @param element - resource
	 * @return profile
	 */
	public abstract IProblemProfile getResourceProfile(IResource element);

	/**
	 * Returns profile working copy for given resource element. (If profile is not
	 * specified for given element it will search for parent resource and so on).
	 * @param element
	 * @return
	 */
	public abstract IProblemProfile getResourceProfileWorkingCopy(IResource element);

	/**
	 * Set profile for resource. 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param resource
	 *            - resource
	 * @param profile
	 *            - problems profile
	 */
	public abstract void updateProfile(IResource resource, IProblemProfile profile);
}