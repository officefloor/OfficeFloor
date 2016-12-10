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
package net.officefloor.eclipse.wizard.access;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.WoofExtensionUtil;
import net.officefloor.eclipse.extension.access.HttpSecuritySourceExtension;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link HttpSecuritySource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceWizard extends Wizard implements
		HttpSecuritySourceInstanceContext {

	/**
	 * Facade method to obtain the {@link AccessInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link HttpSecuritySourceWizard}.
	 * @param accessInstance
	 *            {@link AccessInstance} to base decisions. <code>null</code> if
	 *            creating new {@link AccessInstance}.
	 * @return {@link AccessInstance} or <code>null</code> if cancelled.
	 */
	public static AccessInstance getAccessInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			AccessInstance accessInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		HttpSecuritySourceWizard wizard = new HttpSecuritySourceWizard(project,
				accessInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the access instance
			return wizard.getAccessInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link HttpSecuritySource} class name to its
	 * {@link HttpSecuritySourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link HttpSecuritySourceInstanceContext}.
	 * @return Mapping of {@link HttpSecuritySource} class name to its
	 *         {@link HttpSecuritySourceInstance}.
	 */
	public static Map<String, HttpSecuritySourceInstance> createHttpSecuritySourceInstanceMap(
			ClassLoader classLoader, IProject project,
			HttpSecuritySourceInstanceContext context) {

		// Obtain HTTP Security source instances (by class name for unique set)
		Map<String, HttpSecuritySourceInstance> httpSecuritySourceInstances = new HashMap<String, HttpSecuritySourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					HttpSecuritySource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				httpSecuritySourceInstances.put(className,
						new HttpSecuritySourceInstance(className, null,
								classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (HttpSecuritySourceExtension<?> httpSecuritySourceExtension : WoofExtensionUtil
				.createHttpSecuritySourceExtensionList()) {
			try {
				Class<?> httpSecuritySourceClass = httpSecuritySourceExtension
						.getHttpSecuritySourceClass();
				String httpSecuritySourceClassName = httpSecuritySourceClass
						.getName();
				httpSecuritySourceInstances.put(httpSecuritySourceClassName,
						new HttpSecuritySourceInstance(
								httpSecuritySourceClassName,
								httpSecuritySourceExtension, classLoader,
								project, context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ httpSecuritySourceExtension.getClass().getName(), ex);
			}
		}

		// Return HTTP Security instances by HTTP Security class name
		return httpSecuritySourceInstances;
	}

	/**
	 * {@link HttpSecuritySourceListingWizardPage}.
	 */
	private final HttpSecuritySourceListingWizardPage listingPage;

	/**
	 * {@link HttpSecuritySourcePropertiesWizardPage} pages by their
	 * {@link HttpSecuritySourceInstance}.
	 */
	private final Map<HttpSecuritySourceInstance, HttpSecuritySourcePropertiesWizardPage> propertiesPages = new HashMap<HttpSecuritySourceInstance, HttpSecuritySourcePropertiesWizardPage>();

	/**
	 * {@link HttpSecuritySourceAlignWizardPage}.
	 */
	private final AccessAlignWizardPage accessAlignPage;

	/**
	 * Selected {@link HttpSecuritySourceInstance}.
	 */
	private HttpSecuritySourceInstance selectedHttpSecuritySourceInstance = null;

	/**
	 * Current {@link HttpSecuritySourcePropertiesWizardPage}.
	 */
	private HttpSecuritySourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link AccessInstance}.
	 */
	private AccessInstance accessInstance = null;

	/**
	 * Initiate to create a new {@link AccessInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public HttpSecuritySourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param accessInstance
	 *            {@link AccessInstance} to be edited, or <code>null</code> to
	 *            create a new {@link AccessInstance}.
	 */
	public HttpSecuritySourceWizard(IProject project,
			AccessInstance accessInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of HTTP Security source instances
		Map<String, HttpSecuritySourceInstance> httpSecuritySourceInstanceMap = createHttpSecuritySourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of HTTP Security source instances (in order)
		HttpSecuritySourceInstance[] httpSecuritySourceInstanceListing = httpSecuritySourceInstanceMap
				.values().toArray(new HttpSecuritySourceInstance[0]);
		Arrays.sort(httpSecuritySourceInstanceListing,
				new Comparator<HttpSecuritySourceInstance>() {
					@Override
					public int compare(HttpSecuritySourceInstance a,
							HttpSecuritySourceInstance b) {
						return a.getHttpSecuritySourceClassName().compareTo(
								b.getHttpSecuritySourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new HttpSecuritySourceListingWizardPage(
				httpSecuritySourceInstanceListing, accessInstance);
		for (HttpSecuritySourceInstance httpSecuritySourceInstance : httpSecuritySourceInstanceListing) {
			this.propertiesPages.put(httpSecuritySourceInstance,
					new HttpSecuritySourcePropertiesWizardPage(this,
							httpSecuritySourceInstance));
		}

		// Determine if require creating refactor pages
		if (accessInstance != null) {
			// Refactoring access
			this.accessAlignPage = new AccessAlignWizardPage(accessInstance);

			// Load access instance for matching HTTP Security source instance
			String httpSecuritySourceClassName = accessInstance
					.getHttpSecuritySourceClassName();
			for (HttpSecuritySourceInstance httpSecuritySourceInstance : httpSecuritySourceInstanceListing) {
				if (httpSecuritySourceClassName
						.equals(httpSecuritySourceInstance
								.getHttpSecuritySourceClassName())) {
					httpSecuritySourceInstance
							.loadAccessInstance(accessInstance);
				}
			}

		} else {
			// Creating new section
			this.accessAlignPage = null;
		}
	}

	/**
	 * Obtains the {@link AccessInstance}.
	 * 
	 * @return {@link AccessInstance}.
	 */
	public AccessInstance getAccessInstance() {
		return this.accessInstance;
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

		// Add refactor pages
		if (this.accessAlignPage != null) {
			this.addPage(this.accessAlignPage);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedHttpSecuritySourceInstance = this.listingPage
					.getSelectedHttpSecuritySourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedHttpSecuritySourceInstance);

			// Obtain properties
			this.currentPropertiesPage.activatePage();
			return this.currentPropertiesPage;
		}

		if (page == this.currentPropertiesPage) {
			// Determine if refactoring
			if (this.accessAlignPage != null) {
				// Refactoring access
				this.accessAlignPage
						.loadHttpSecuritySourceInstance(this.selectedHttpSecuritySourceInstance);
				return this.accessAlignPage;
			}

			// Not refactoring, nothing further
			return null;

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

		// Obtain the details of the access instance
		String httpSecuritySourceClassName = this.selectedHttpSecuritySourceInstance
				.getHttpSecuritySourceClassName();
		long authenticationTimeout = this.selectedHttpSecuritySourceInstance
				.getAuthenticationTimeout();
		PropertyList propertyList = this.selectedHttpSecuritySourceInstance
				.getPropertyList();
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = this.selectedHttpSecuritySourceInstance
				.getHttpSecurityType();

		// Obtain the mappings
		Map<String, String> inputNameMapping = null;
		Map<String, String> outputNameMapping = null;
		if (this.accessAlignPage != null) {
			// Obtain mappings for access
			inputNameMapping = this.accessAlignPage.getInputNameMapping();
			outputNameMapping = this.accessAlignPage.getOutputNameMapping();
		}

		// Normalise the properties
		propertyList.normalise();

		// Specify the access instance
		this.accessInstance = new AccessInstance(httpSecuritySourceClassName,
				authenticationTimeout, propertyList, httpSecurityType,
				inputNameMapping, outputNameMapping);

		// Finished
		return true;
	}

	/*
	 * =============== HttpSecuritySourceInstanceContext ===================
	 */

	@Override
	public void setTitle(String title) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setTitle(title);
		}
	}

	@Override
	public void setErrorMessage(String message) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setErrorMessage(message);
		}
	}

	@Override
	public void setHttpSecurityTypeLoaded(boolean isHttpSecurityTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage
					.setPageComplete(isHttpSecurityTypeLoaded);
		}
	}

}