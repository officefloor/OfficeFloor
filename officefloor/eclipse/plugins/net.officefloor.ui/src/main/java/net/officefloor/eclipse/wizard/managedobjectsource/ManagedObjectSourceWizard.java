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
package net.officefloor.eclipse.wizard.managedobjectsource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceWizard extends Wizard implements
		ManagedObjectSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link ManagedObjectInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link ManagedObjectSourceWizard}.
	 * @param managedObjectInstance
	 *            {@link ManagedObjectInstance} to based decisions.
	 *            <code>null</code> if creating new
	 *            {@link ManagedObjectInstance}.
	 * @return {@link ManagedObjectInstance} or <code>null</code> if cancelled.
	 */
	public static ManagedObjectInstance getManagedObjectInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			ManagedObjectInstance managedObjectInstance) {

		// Create and run the wizard
		ManagedObjectSourceWizard wizard = new ManagedObjectSourceWizard(
				editPart);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the managed object instance
			return wizard.getManagedObjectInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link ManagedObjectSource} class name to its
	 * {@link ManagedObjectSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link ManagedObjectSourceInstanceContext}.
	 * @return Mapping of {@link ManagedObjectSource} class name to its
	 *         {@link ManagedObjectSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ManagedObjectSourceInstance> createManagedObjectSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			ManagedObjectSourceInstanceContext context) {

		// Obtain the managed object source instances (unique set)
		Map<String, ManagedObjectSourceInstance> managedObjectSourceInstances = new HashMap<String, ManagedObjectSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					ManagedObjectSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				managedObjectSourceInstances.put(className,
						new ManagedObjectSourceInstance(className, null,
								classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (ManagedObjectSourceExtension managedObjectSourceExtension : ExtensionUtil
				.createManagedObjectSourceExtensionList()) {
			try {
				Class<?> managedObjectSourceClass = managedObjectSourceExtension
						.getManagedObjectSourceClass();
				String managedObjectSourceClassName = managedObjectSourceClass
						.getName();
				managedObjectSourceInstances.put(managedObjectSourceClassName,
						new ManagedObjectSourceInstance(
								managedObjectSourceClassName,
								managedObjectSourceExtension, classLoader,
								project, context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ managedObjectSourceExtension.getClass().getName(), ex);
			}
		}

		// Return managed object source instances by their class name
		return managedObjectSourceInstances;
	}

	/**
	 * {@link ManagedObjectSourceListingWizardPage}.
	 */
	private final ManagedObjectSourceListingWizardPage listingPage;

	/**
	 * {@link ManagedObjectSourcePropertiesWizardPage} pages by their
	 * {@link ManagedObjectSourceInstance}.
	 */
	private final Map<ManagedObjectSourceInstance, ManagedObjectSourcePropertiesWizardPage> propertiesPages = new HashMap<ManagedObjectSourceInstance, ManagedObjectSourcePropertiesWizardPage>();

	/**
	 * {@link ManagedObjectSourceDetailsWizardPage}.
	 */
	private final ManagedObjectSourceDetailsWizardPage detailsPage;

	/**
	 * Selected {@link ManagedObjectSourceInstance}.
	 */
	private ManagedObjectSourceInstance selectedManagedObjectSourceInstance = null;

	/**
	 * Current {@link ManagedObjectSourcePropertiesWizardPage}.
	 */
	private ManagedObjectSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link ManagedObjectInstance}.
	 */
	private ManagedObjectInstance managedObjectInstance = null;

	/**
	 * Initiate to create a new {@link ManagedObjectInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 */
	public ManagedObjectSourceWizard(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		this(editPart, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param managedObjectInstance
	 *            {@link ManagedObjectInstance} to be edited, or
	 *            <code>null</code> to create a new
	 *            {@link ManagedObjectInstance}.
	 */
	public ManagedObjectSourceWizard(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			ManagedObjectInstance managedObjectInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of managed object source instances
		Map<String, ManagedObjectSourceInstance> managedObjectSourceInstanceMap = createManagedObjectSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of managed object source instances (in order)
		ManagedObjectSourceInstance[] managedObjectSourceInstanceListing = managedObjectSourceInstanceMap
				.values().toArray(new ManagedObjectSourceInstance[0]);
		Arrays.sort(managedObjectSourceInstanceListing,
				new Comparator<ManagedObjectSourceInstance>() {
					@Override
					public int compare(ManagedObjectSourceInstance a,
							ManagedObjectSourceInstance b) {
						return a.getManagedObjectSourceClassName().compareTo(
								b.getManagedObjectSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new ManagedObjectSourceListingWizardPage(
				managedObjectSourceInstanceListing);
		for (ManagedObjectSourceInstance managedObjectSourceInstance : managedObjectSourceInstanceListing) {
			this.propertiesPages.put(managedObjectSourceInstance,
					new ManagedObjectSourcePropertiesWizardPage(this,
							managedObjectSourceInstance));
		}
		this.detailsPage = new ManagedObjectSourceDetailsWizardPage();
	}

	/**
	 * Obtains the {@link ManagedObjectInstance}.
	 * 
	 * @return {@link ManagedObjectInstance}.
	 */
	public ManagedObjectInstance getManagedObjectInstance() {
		return this.managedObjectInstance;
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
		this.addPage(this.detailsPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedManagedObjectSourceInstance = this.listingPage
					.getSelectedManagedObjectSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedManagedObjectSourceInstance);

			// Return as next page to set properties
			return this.currentPropertiesPage;

		} else if (page instanceof ManagedObjectSourcePropertiesWizardPage) {
			// Properties specified, so now select tasks
			this.detailsPage
					.loadManagedObjectSourceInstance(this.selectedManagedObjectSourceInstance);
			return this.detailsPage;

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

		// Ensure the tasks page complete
		if (!this.detailsPage.isPageComplete()) {
			return false;
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the managed object instance
		String managedObjectName = this.detailsPage.getManagedObjectName();
		long defaultTimetout = this.detailsPage.getDefaultTimeout();
		String managedObjectSourceClassName = this.selectedManagedObjectSourceInstance
				.getManagedObjectSourceClassName();
		PropertyList propertyList = this.selectedManagedObjectSourceInstance
				.getPropertyList();
		ManagedObjectType<?> managedObjectType = this.selectedManagedObjectSourceInstance
				.getManagedObjectType();

		// Normalise the properties
		propertyList.normalise();

		// Specify the managed object instance
		this.managedObjectInstance = new ManagedObjectInstance(
				managedObjectName, managedObjectSourceClassName, propertyList,
				managedObjectType, defaultTimetout);

		// Finished
		return true;
	}

	/*
	 * ============== ManagedObjectSourceInstanceContext =======================
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
	public void setManagedObjectTypeLoaded(boolean isManagedObjectTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage
					.setPageComplete(isManagedObjectTypeLoaded);
		}
	}

}