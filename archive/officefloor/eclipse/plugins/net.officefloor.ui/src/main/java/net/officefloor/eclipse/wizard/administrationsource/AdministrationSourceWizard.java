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
package net.officefloor.eclipse.wizard.administrationsource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.administrationsource.AdministrationSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.api.administration.Administration;

/**
 * {@link IWizard} to add and manage {@link Administration} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationSourceWizard extends Wizard implements AdministrationSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link AdministrationInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link AdministrationSourceWizard}.
	 * @param administrationInstance
	 *            {@link AdministrationInstance} to based decisions.
	 *            <code>null</code> if creating new
	 *            {@link AdministrationInstance}.
	 * @return {@link AdministrationInstance} or <code>null</code> if cancelled.
	 */
	public static AdministrationInstance getAdministrationInstance(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			AdministrationInstance administrationInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart.getEditor().getEditorInput());

		// Create and run the wizard
		AdministrationSourceWizard wizard = new AdministrationSourceWizard(project, administrationInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the administration instance
			return wizard.getAdministrationInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link AdministrationSource} class name to its
	 * {@link AdministrationSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link AdministrationSourceInstanceContext}.
	 * @return Mapping of {@link AdministrationSource} class name to its
	 *         {@link AdministrationSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, AdministrationSourceInstance> createAdministrationSourceInstanceMap(
			ClassLoader classLoader, IProject project, AdministrationSourceInstanceContext context) {

		// Obtain the administration source instances (unique set)
		Map<String, AdministrationSourceInstance> administrationSourceInstances = new HashMap<String, AdministrationSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, AdministrationSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				administrationSourceInstances.put(className,
						new AdministrationSourceInstance(className, null, classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError("Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (AdministrationSourceExtension administrationSourceExtension : ExtensionUtil
				.createAdministrationSourceExtensionList()) {
			try {
				Class<?> administrationSourceClass = administrationSourceExtension.getAdministrationSourceClass();
				String administrationSourceClassName = administrationSourceClass.getName();
				administrationSourceInstances.put(administrationSourceClassName, new AdministrationSourceInstance(
						administrationSourceClassName, administrationSourceExtension, classLoader, project, context));
			} catch (Throwable ex) {
				LogUtil.logError(
						"Failed to create source instance for " + administrationSourceExtension.getClass().getName(),
						ex);
			}
		}

		// Return administration source instances by the class name
		return administrationSourceInstances;
	}

	/**
	 * {@link AdministrationSourceListingWizardPage}.
	 */
	private final AdministrationSourceListingWizardPage listingPage;

	/**
	 * {@link AdministrationSourcePropertiesWizardPage} pages by their
	 * {@link AdministrationSourceInstance}.
	 */
	private final Map<AdministrationSourceInstance, AdministrationSourcePropertiesWizardPage> propertiesPages = new HashMap<AdministrationSourceInstance, AdministrationSourcePropertiesWizardPage>();

	/**
	 * Selected {@link AdministrationSourceInstance}.
	 */
	private AdministrationSourceInstance selectedAdministrationSourceInstance = null;

	/**
	 * Current {@link AdministrationSourcePropertiesWizardPage}.
	 */
	private AdministrationSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link AdministrationInstance}.
	 */
	private AdministrationInstance administrationInstance = null;

	/**
	 * Initiate to create a new {@link AdministrationInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public AdministrationSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param administrationInstance
	 *            {@link AdministrationInstance} to be edited, or
	 *            <code>null</code> to create a new
	 *            {@link AdministrationInstance}.
	 */
	public AdministrationSourceWizard(IProject project, AdministrationInstance administrationInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of administration source instances
		Map<String, AdministrationSourceInstance> administrationSourceInstanceMap = createAdministrationSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of administration source instances (in order)
		AdministrationSourceInstance[] administrationSourceInstanceListing = administrationSourceInstanceMap.values()
				.toArray(new AdministrationSourceInstance[0]);
		Arrays.sort(administrationSourceInstanceListing, new Comparator<AdministrationSourceInstance>() {
			@Override
			public int compare(AdministrationSourceInstance a, AdministrationSourceInstance b) {
				return a.getAdministrationSourceClassName().compareTo(b.getAdministrationSourceClassName());
			}
		});

		// Create the pages
		this.listingPage = new AdministrationSourceListingWizardPage(administrationSourceInstanceListing);
		for (AdministrationSourceInstance administrationSourceInstance : administrationSourceInstanceListing) {
			this.propertiesPages.put(administrationSourceInstance,
					new AdministrationSourcePropertiesWizardPage(this, administrationSourceInstance));
		}
	}

	/**
	 * Obtains the {@link AdministrationInstance}.
	 * 
	 * @return {@link AdministrationInstance}.
	 */
	public AdministrationInstance getAdministrationInstance() {
		return this.administrationInstance;
	}

	/*
	 * ====================== Wizard ==================================
	 */

	@Override
	public void addPages() {
		this.addPage(this.listingPage);
		if (this.propertiesPages.size() > 0) {
			// Load the first properties page
			this.addPage(this.propertiesPages.values().toArray(new IWizardPage[0])[0]);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedAdministrationSourceInstance = this.listingPage.getSelectedAdministrationSourceInstance();
			this.currentPropertiesPage = this.propertiesPages.get(this.selectedAdministrationSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load administration type to set state and return as next page
			return this.currentPropertiesPage;

		} else {
			// Nothing further
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

		// Obtain the details of the administration instance
		String administrationName = this.selectedAdministrationSourceInstance.getAdministratorName();
		String administrationSourceClassName = this.selectedAdministrationSourceInstance
				.getAdministrationSourceClassName();
		PropertyList propertyList = this.selectedAdministrationSourceInstance.getPropertyList();
		AdministrationType<?, ?, ?> administrationType = this.selectedAdministrationSourceInstance
				.getAdministrationType();

		// Normalise the properties
		propertyList.normalise();

		// Specify the administration instance
		this.administrationInstance = new AdministrationInstance(administrationName, administrationSourceClassName,
				propertyList, administrationType);

		// Finished
		return true;
	}

	/*
	 * =============== AdministrationSourceInstanceContext =================
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
	public void setAdministrationTypeLoaded(boolean isAdministratorTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isAdministratorTypeLoaded);
		}
	}

}