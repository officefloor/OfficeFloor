/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.wizard.teamsource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamType;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceWizard extends Wizard implements
		TeamSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link TeamInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link TeamSourceWizard}.
	 * @param teamInstance
	 *            {@link TeamInstance} to based decisions. <code>null</code> if
	 *            creating new {@link TeamInstance}.
	 * @return {@link TeamInstance} or <code>null</code> if cancelled.
	 */
	public static TeamInstance getTeamInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			TeamInstance teamInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		TeamSourceWizard wizard = new TeamSourceWizard(project, teamInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the team instance
			return wizard.getTeamInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link TeamSource} class name to its
	 * {@link TeamSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link TeamSourceInstanceContext}.
	 * @return Mapping of {@link TeamSource} class name to its
	 *         {@link TeamSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, TeamSourceInstance> createTeamSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			TeamSourceInstanceContext context) {

		// Obtain the team source instances (by class name to get unique set)
		Map<String, TeamSourceInstance> teamSourceInstances = new HashMap<String, TeamSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					TeamSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				teamSourceInstances.put(className, new TeamSourceInstance(
						className, null, classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (TeamSourceExtension teamSourceExtension : ExtensionUtil
				.createTeamSourceExtensionList()) {
			try {
				Class<?> teamSourceClass = teamSourceExtension
						.getTeamSourceClass();
				String teamSourceClassName = teamSourceClass.getName();
				teamSourceInstances.put(teamSourceClassName,
						new TeamSourceInstance(teamSourceClassName,
								teamSourceExtension, classLoader, project,
								context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ teamSourceExtension.getClass().getName(), ex);
			}
		}

		// Return team source instances by the team source class name
		return teamSourceInstances;
	}

	/**
	 * {@link TeamSourceListingWizardPage}.
	 */
	private final TeamSourceListingWizardPage listingPage;

	/**
	 * {@link TeamSourcePropertiesWizardPage} pages by their
	 * {@link TeamSourceInstance}.
	 */
	private final Map<TeamSourceInstance, TeamSourcePropertiesWizardPage> propertiesPages = new HashMap<TeamSourceInstance, TeamSourcePropertiesWizardPage>();

	/**
	 * Selected {@link TeamSourceInstance}.
	 */
	private TeamSourceInstance selectedTeamSourceInstance = null;

	/**
	 * Current {@link TeamSourcePropertiesWizardPage}.
	 */
	private TeamSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link TeamInstance}.
	 */
	private TeamInstance teamInstance = null;

	/**
	 * Initiate to create a new {@link TeamInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public TeamSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param teamInstance
	 *            {@link TeamInstance} to be edited, or <code>null</code> to
	 *            create a new {@link TeamInstance}.
	 */
	public TeamSourceWizard(IProject project, TeamInstance teamInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of team source instances
		Map<String, TeamSourceInstance> teamSourceInstanceMap = createTeamSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of team source instances (in order)
		TeamSourceInstance[] teamSourceInstanceListing = teamSourceInstanceMap
				.values().toArray(new TeamSourceInstance[0]);
		Arrays.sort(teamSourceInstanceListing,
				new Comparator<TeamSourceInstance>() {
					@Override
					public int compare(TeamSourceInstance a,
							TeamSourceInstance b) {
						return a.getTeamSourceClassName().compareTo(
								b.getTeamSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new TeamSourceListingWizardPage(
				teamSourceInstanceListing);
		for (TeamSourceInstance teamSourceInstance : teamSourceInstanceListing) {
			this.propertiesPages
					.put(teamSourceInstance,
							new TeamSourcePropertiesWizardPage(this,
									teamSourceInstance));
		}
	}

	/**
	 * Obtains the {@link TeamInstance}.
	 * 
	 * @return {@link TeamInstance}.
	 */
	public TeamInstance getTeamInstance() {
		return this.teamInstance;
	}

	/*
	 * ====================== Wizard ==================================
	 */

	@Override
	public void addPages() {
		this.addPage(this.listingPage);
		if (this.propertiesPages.size() > 0) {
			// Load the first properties page
			this.addPage(this.propertiesPages.values().toArray(
					new IWizardPage[0])[0]);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedTeamSourceInstance = this.listingPage
					.getSelectedTeamSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedTeamSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load team type to set state and return as next page
			return this.currentPropertiesPage;

		} else {
			// Tasks selected, nothing further
			return null;
		}
	}

	@Override
	public boolean canFinish() {

		// Ensure listing page complete
		if (!this.listingPage.isPageComplete()) {
			return false;
		}

		// Ensure have current properties page and is complete
		if (this.currentPropertiesPage == null) {
			return false;
		}
		if (!this.currentPropertiesPage.isPageComplete()) {
			return false;
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the team instance
		String teamName = this.selectedTeamSourceInstance.getTeamName();
		String teamSourceClassName = this.selectedTeamSourceInstance
				.getTeamSourceClassName();
		PropertyList propertyList = this.selectedTeamSourceInstance
				.getPropertyList();
		TeamType teamType = this.selectedTeamSourceInstance.getTeamType();

		// Normalise the properties
		propertyList.normalise();

		// Specify the team instance
		this.teamInstance = new TeamInstance(teamName, teamSourceClassName,
				propertyList, teamType);

		// Finished
		return true;
	}

	/*
	 * ================== TeamSourceInstanceContext ======================
	 */

	@Override
	public void setTitle(String title) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setTitle(title);
		}
	}

	@Override
	public void setErrorMessage(String message) {
		this.listingPage.setErrorMessage(message);
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setErrorMessage(message);
		}
	}

	@Override
	public void setTeamTypeLoaded(boolean isTeamTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isTeamTypeLoaded);
		}
	}

}