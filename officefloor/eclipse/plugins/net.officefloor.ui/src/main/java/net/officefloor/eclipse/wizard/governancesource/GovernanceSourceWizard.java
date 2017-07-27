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
package net.officefloor.eclipse.wizard.governancesource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.spi.governance.Governance;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link Governance} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourceWizard extends Wizard implements
		GovernanceSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link GovernanceInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link GovernanceSourceWizard}.
	 * @param governanceInstance
	 *            {@link GovernanceInstance} to base decisions.
	 *            <code>null</code> if creating new {@link GovernanceInstance}.
	 * @return {@link GovernanceInstance} or <code>null</code> if cancelled.
	 */
	public static GovernanceInstance getGovernanceInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			GovernanceInstance governanceInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		GovernanceSourceWizard wizard = new GovernanceSourceWizard(project,
				governanceInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the governance instance
			return wizard.getGovernanceInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link GovernanceSource} class name to its
	 * {@link GovernanceSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link GovernanceSourceInstanceContext}.
	 * @return Mapping of {@link GovernanceSource} class name to its
	 *         {@link GovernanceSourceInstance}.
	 */
	public static Map<String, GovernanceSourceInstance> createGovernanceSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			GovernanceSourceInstanceContext context) {

		// Obtain the governance source instances (by class name for unique set)
		Map<String, GovernanceSourceInstance> governanceSourceInstances = new HashMap<String, GovernanceSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					GovernanceSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				governanceSourceInstances.put(className,
						new GovernanceSourceInstance(className, null,
								classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (GovernanceSourceExtension<?, ?, ?> governanceSourceExtension : ExtensionUtil
				.createGovernanceSourceExtensionList()) {
			try {
				Class<?> governanceSourceClass = governanceSourceExtension
						.getGovernanceSourceClass();
				String governanceSourceClassName = governanceSourceClass
						.getName();
				governanceSourceInstances.put(governanceSourceClassName,
						new GovernanceSourceInstance(governanceSourceClassName,
								governanceSourceExtension, classLoader,
								project, context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ governanceSourceExtension.getClass().getName(), ex);
			}
		}

		// Return governance source instances by governance source class name
		return governanceSourceInstances;
	}

	/**
	 * {@link GovernanceSourceListingWizardPage}.
	 */
	private final GovernanceSourceListingWizardPage listingPage;

	/**
	 * {@link GovernanceSourcePropertiesWizardPage} pages by their
	 * {@link GovernanceSourceInstance}.
	 */
	private final Map<GovernanceSourceInstance, GovernanceSourcePropertiesWizardPage> propertiesPages = new HashMap<GovernanceSourceInstance, GovernanceSourcePropertiesWizardPage>();

	/**
	 * Selected {@link GovernanceSourceInstance}.
	 */
	private GovernanceSourceInstance selectedGovernanceSourceInstance = null;

	/**
	 * Current {@link GovernanceSourcePropertiesWizardPage}.
	 */
	private GovernanceSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link GovernanceInstance}.
	 */
	private GovernanceInstance governanceInstance = null;

	/**
	 * Initiate to create a new {@link GovernanceInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public GovernanceSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param governanceInstance
	 *            {@link GovernanceInstance} to be edited, or <code>null</code>
	 *            to create a new {@link GovernanceInstance}.
	 */
	public GovernanceSourceWizard(IProject project,
			GovernanceInstance governanceInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of governance source instances
		Map<String, GovernanceSourceInstance> governanceSourceInstanceMap = createGovernanceSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of governance source instances (in order)
		GovernanceSourceInstance[] governanceSourceInstanceListing = governanceSourceInstanceMap
				.values().toArray(new GovernanceSourceInstance[0]);
		Arrays.sort(governanceSourceInstanceListing,
				new Comparator<GovernanceSourceInstance>() {
					@Override
					public int compare(GovernanceSourceInstance a,
							GovernanceSourceInstance b) {
						return a.getGovernanceSourceClassName().compareTo(
								b.getGovernanceSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new GovernanceSourceListingWizardPage(
				governanceSourceInstanceListing, governanceInstance);
		for (GovernanceSourceInstance governanceSourceInstance : governanceSourceInstanceListing) {
			this.propertiesPages.put(governanceSourceInstance,
					new GovernanceSourcePropertiesWizardPage(this,
							governanceSourceInstance));
		}

		// Add refactor governance
		if (governanceInstance != null) {

			// Load governance instance for matching governance source instance
			String governanceSourceClassName = governanceInstance
					.getGovernanceSourceClassName();
			for (GovernanceSourceInstance governanceSourceInstance : governanceSourceInstanceListing) {
				if (governanceSourceClassName.equals(governanceSourceInstance
						.getGovernanceSourceClassName())) {
					governanceSourceInstance
							.loadGovernanceInstance(governanceInstance);
				}
			}
		}
	}

	/**
	 * Obtains the {@link GovernanceInstance}.
	 * 
	 * @return {@link GovernanceInstance}.
	 */
	public GovernanceInstance getGovernanceInstance() {
		return this.governanceInstance;
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
			this.selectedGovernanceSourceInstance = this.listingPage
					.getSelectedGovernanceSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedGovernanceSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load governance type to set state and return as next page
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

		// Obtain the details of the governance instance
		String governanceName = this.selectedGovernanceSourceInstance
				.getGovernanceName();
		String governanceSourceClassName = this.selectedGovernanceSourceInstance
				.getGovernanceSourceClassName();
		PropertyList propertyList = this.selectedGovernanceSourceInstance
				.getPropertyList();
		GovernanceType<?, ?> governanceType = this.selectedGovernanceSourceInstance
				.getGovernanceType();

		// Normalise the properties
		propertyList.normalise();

		// Specify the governance instance
		this.governanceInstance = new GovernanceInstance(governanceName,
				governanceSourceClassName, propertyList, governanceType);

		// Finished
		return true;
	}

	/*
	 * ================== GovernanceSourceInstanceContext ======================
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
	public void setGovernanceTypeLoaded(boolean isGovernanceTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isGovernanceTypeLoaded);
		}
	}

}