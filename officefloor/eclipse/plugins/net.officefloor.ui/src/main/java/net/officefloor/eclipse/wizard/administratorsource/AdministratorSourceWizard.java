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
package net.officefloor.eclipse.wizard.administratorsource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.administratorsource.AdministratorSourceExtension;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link Administrator} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorSourceWizard extends Wizard implements
		AdministratorSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link AdministratorInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link AdministratorSourceWizard}.
	 * @param administratorInstance
	 *            {@link AdministratorInstance} to based decisions.
	 *            <code>null</code> if creating new
	 *            {@link AdministratorInstance}.
	 * @return {@link AdministratorInstance} or <code>null</code> if cancelled.
	 */
	public static AdministratorInstance getAdministratorInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			AdministratorInstance administratorInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		AdministratorSourceWizard wizard = new AdministratorSourceWizard(
				project, administratorInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the administrator instance
			return wizard.getAdministratorInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link AdministratorSource} class name to its
	 * {@link AdministratorSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link AdministratorSourceInstanceContext}.
	 * @return Mapping of {@link AdministratorSource} class name to its
	 *         {@link AdministratorSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, AdministratorSourceInstance> createAdministratorSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			AdministratorSourceInstanceContext context) {

		// Obtain the administrator source instances (unique set)
		Map<String, AdministratorSourceInstance> administratorSourceInstances = new HashMap<String, AdministratorSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					AdministratorSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				administratorSourceInstances.put(className,
						new AdministratorSourceInstance(className, null,
								classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (AdministratorSourceExtension administratorSourceExtension : ExtensionUtil
				.createAdministratorSourceExtensionList()) {
			try {
				Class<?> administratorSourceClass = administratorSourceExtension
						.getAdministratorSourceClass();
				String administratorSourceClassName = administratorSourceClass
						.getName();
				administratorSourceInstances.put(administratorSourceClassName,
						new AdministratorSourceInstance(
								administratorSourceClassName,
								administratorSourceExtension, classLoader,
								project, context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ administratorSourceExtension.getClass().getName(), ex);
			}
		}

		// Return administrator source instances by the class name
		return administratorSourceInstances;
	}

	/**
	 * {@link AdministratorSourceListingWizardPage}.
	 */
	private final AdministratorSourceListingWizardPage listingPage;

	/**
	 * {@link AdministratorSourcePropertiesWizardPage} pages by their
	 * {@link AdministratorSourceInstance}.
	 */
	private final Map<AdministratorSourceInstance, AdministratorSourcePropertiesWizardPage> propertiesPages = new HashMap<AdministratorSourceInstance, AdministratorSourcePropertiesWizardPage>();

	/**
	 * Selected {@link AdministratorSourceInstance}.
	 */
	private AdministratorSourceInstance selectedAdministratorSourceInstance = null;

	/**
	 * Current {@link AdministratorSourcePropertiesWizardPage}.
	 */
	private AdministratorSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link AdministratorInstance}.
	 */
	private AdministratorInstance administratorInstance = null;

	/**
	 * Initiate to create a new {@link AdministratorInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public AdministratorSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param administratorInstance
	 *            {@link AdministratorInstance} to be edited, or
	 *            <code>null</code> to create a new
	 *            {@link AdministratorInstance}.
	 */
	public AdministratorSourceWizard(IProject project,
			AdministratorInstance administratorInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of administrator source instances
		Map<String, AdministratorSourceInstance> administratorSourceInstanceMap = createAdministratorSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of administrator source instances (in order)
		AdministratorSourceInstance[] administratorSourceInstanceListing = administratorSourceInstanceMap
				.values().toArray(new AdministratorSourceInstance[0]);
		Arrays.sort(administratorSourceInstanceListing,
				new Comparator<AdministratorSourceInstance>() {
					@Override
					public int compare(AdministratorSourceInstance a,
							AdministratorSourceInstance b) {
						return a.getAdministratorSourceClassName().compareTo(
								b.getAdministratorSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new AdministratorSourceListingWizardPage(
				administratorSourceInstanceListing);
		for (AdministratorSourceInstance administratorSourceInstance : administratorSourceInstanceListing) {
			this.propertiesPages.put(administratorSourceInstance,
					new AdministratorSourcePropertiesWizardPage(this,
							administratorSourceInstance));
		}
	}

	/**
	 * Obtains the {@link AdministratorInstance}.
	 * 
	 * @return {@link AdministratorInstance}.
	 */
	public AdministratorInstance getAdministratorInstance() {
		return this.administratorInstance;
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
			this.selectedAdministratorSourceInstance = this.listingPage
					.getSelectedAdministratorSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedAdministratorSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load administrator type to set state and return as next page
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

		// Obtain the details of the administrator instance
		String administratorName = this.selectedAdministratorSourceInstance
				.getAdministratorName();
		String administratorSourceClassName = this.selectedAdministratorSourceInstance
				.getAdministratorSourceClassName();
		PropertyList propertyList = this.selectedAdministratorSourceInstance
				.getPropertyList();
		AdministratorScope administratorScope = this.selectedAdministratorSourceInstance
				.getAdministratorScope();
		AdministratorType<?, ?> administratorType = this.selectedAdministratorSourceInstance
				.getAdministratorType();

		// Normalise the properties
		propertyList.normalise();

		// Specify the administrator instance
		this.administratorInstance = new AdministratorInstance(
				administratorName, administratorSourceClassName, propertyList,
				administratorScope, administratorType);

		// Finished
		return true;
	}

	/*
	 * =============== AdministratorSourceInstanceContext =================
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
	public void setAdministratorTypeLoaded(boolean isAdministratorTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage
					.setPageComplete(isAdministratorTypeLoaded);
		}
	}

}