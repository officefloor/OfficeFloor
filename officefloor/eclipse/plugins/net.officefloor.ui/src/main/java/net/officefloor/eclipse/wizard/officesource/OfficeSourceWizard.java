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
package net.officefloor.eclipse.wizard.officesource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.officesource.OfficeSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.api.manage.Office;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link Office} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceWizard extends Wizard implements
		OfficeSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link OfficeInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link OfficeSourceWizard}.
	 * @param officeInstance
	 *            {@link OfficeInstance} to based decisions. <code>null</code>
	 *            if creating new {@link OfficeInstance}.
	 * @return {@link OfficeInstance} or <code>null</code> if canceled.
	 */
	public static OfficeInstance getOfficeInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			OfficeInstance officeInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		OfficeSourceWizard wizard = new OfficeSourceWizard(project,
				officeInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the office instance
			return wizard.getOfficeInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link OfficeSource} class name to its
	 * {@link OfficeSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link OfficeSourceInstanceContext}.
	 * @return Mapping of {@link OfficeSource} class name to its
	 *         {@link OfficeSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, OfficeSourceInstance> createOfficeSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			OfficeSourceInstanceContext context) {

		// Obtain the office source instances (by class name to get unique set)
		Map<String, OfficeSourceInstance> officeSourceInstances = new HashMap<String, OfficeSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					OfficeSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				officeSourceInstances.put(className, new OfficeSourceInstance(
						className, null, classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (OfficeSourceExtension officeSourceExtension : ExtensionUtil
				.createOfficeSourceExtensionList()) {
			try {
				Class<?> officeSourceClass = officeSourceExtension
						.getOfficeSourceClass();
				String officeSourceClassName = officeSourceClass.getName();
				officeSourceInstances.put(officeSourceClassName,
						new OfficeSourceInstance(officeSourceClassName,
								officeSourceExtension, classLoader, project,
								context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ officeSourceExtension.getClass().getName(), ex);
			}
		}

		// Return office source instances by the office source class name
		return officeSourceInstances;
	}

	/**
	 * {@link OfficeSourceListingWizardPage}.
	 */
	private final OfficeSourceListingWizardPage listingPage;

	/**
	 * {@link OfficeSourcePropertiesWizardPage} pages by their
	 * {@link OfficeSourceInstance}.
	 */
	private final Map<OfficeSourceInstance, OfficeSourcePropertiesWizardPage> propertiesPages = new HashMap<OfficeSourceInstance, OfficeSourcePropertiesWizardPage>();

	/**
	 * {@link OfficeSourceAlignDeployedOfficeWizardPage}.
	 */
	private final OfficeSourceAlignDeployedOfficeWizardPage officeAlignPage;

	/**
	 * Selected {@link OfficeSourceInstance}.
	 */
	private OfficeSourceInstance selectedOfficeSourceInstance = null;

	/**
	 * Current {@link OfficeSourcePropertiesWizardPage}.
	 */
	private OfficeSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link OfficeInstance}.
	 */
	private OfficeInstance officeInstance = null;

	/**
	 * Initiate to create a new {@link OfficeInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public OfficeSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param officeInstance
	 *            {@link OfficeInstance} to be edited, or <code>null</code> to
	 *            create a new {@link OfficeInstance}.
	 */
	public OfficeSourceWizard(IProject project, OfficeInstance officeInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of office source instances
		Map<String, OfficeSourceInstance> officeSourceInstanceMap = createOfficeSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of office source instances (in order)
		OfficeSourceInstance[] officeSourceInstanceListing = officeSourceInstanceMap
				.values().toArray(new OfficeSourceInstance[0]);
		Arrays.sort(officeSourceInstanceListing,
				new Comparator<OfficeSourceInstance>() {
					@Override
					public int compare(OfficeSourceInstance a,
							OfficeSourceInstance b) {
						return a.getOfficeSourceClassName().compareTo(
								b.getOfficeSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new OfficeSourceListingWizardPage(
				officeSourceInstanceListing, project, officeInstance);
		for (OfficeSourceInstance officeSourceInstance : officeSourceInstanceListing) {
			this.propertiesPages.put(officeSourceInstance,
					new OfficeSourcePropertiesWizardPage(this,
							officeSourceInstance));
		}

		// Determine if require creating refactor pages
		if (officeInstance != null) {
			// Refactoring office
			this.officeAlignPage = new OfficeSourceAlignDeployedOfficeWizardPage(
					officeInstance);
		} else {
			// Create new office
			this.officeAlignPage = null;
		}
	}

	/**
	 * Obtains the {@link OfficeInstance}.
	 * 
	 * @return {@link OfficeInstance}.
	 */
	public OfficeInstance getOfficeInstance() {
		return this.officeInstance;
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

		// Add the refactor pages
		if (this.officeAlignPage != null) {
			this.addPage(this.officeAlignPage);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedOfficeSourceInstance = this.listingPage
					.getSelectedOfficeSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedOfficeSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load office type to set state and return as next page
			return this.currentPropertiesPage;

		} else if (page == this.currentPropertiesPage) {
			// Determine if require refactoring
			if (this.officeAlignPage != null) {
				// Refactoring office
				this.officeAlignPage
						.loadOfficeSourceInstance(this.selectedOfficeSourceInstance);
				return this.officeAlignPage;
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

		// Obtain the details of the office instance
		String officeName = this.selectedOfficeSourceInstance.getOfficeName();
		String officeSourceClassName = this.selectedOfficeSourceInstance
				.getOfficeSourceClassName();
		String officeLocation = this.selectedOfficeSourceInstance
				.getOfficeLocation();
		PropertyList propertyList = this.selectedOfficeSourceInstance
				.getPropertyList();
		OfficeType officeType = this.selectedOfficeSourceInstance
				.getOfficeType();

		// Obtain the mappings
		Map<String, String> objectNameMapping = null;
		Map<String, String> inputNameMapping = null;
		Map<String, String> teamNameMapping = null;
		if (this.officeAlignPage != null) {
			// Obtain mappings for office
			objectNameMapping = this.officeAlignPage.getObjectNameMapping();
			inputNameMapping = this.officeAlignPage.getInputNameMapping();
			teamNameMapping = this.officeAlignPage.getTeamNameMapping();
		}

		// Normalise the properties
		propertyList.normalise();

		// Specify the office instance
		this.officeInstance = new OfficeInstance(officeName,
				officeSourceClassName, officeLocation, propertyList,
				officeType, objectNameMapping, inputNameMapping,
				teamNameMapping);

		// Finished
		return true;
	}

	/*
	 * ================== SectionSourceInstanceContext ======================
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
	public void setOfficeLoaded(boolean isOfficeTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isOfficeTypeLoaded);
		}
	}

}