/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.worksource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.java.JavaUtil;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.api.execute.Work;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link Work} instances.
 * 
 * @author Daniel
 */
public class WorkSourceWizard extends Wizard implements
		WorkSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link WorkInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link WorkSourceWizard}.
	 * @param workInstance
	 *            {@link WorkInstance} to based decisions. <code>null</code> if
	 *            creating new {@link WorkInstance}.
	 * @return {@link WorkInstance} or <code>null</code> if cancelled.
	 */
	public static WorkInstance getWorkInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			WorkInstance workInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		WorkSourceWizard wizard = new WorkSourceWizard(project);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the work instance
			return wizard.getWorkInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link WorkSource} class name to its
	 * {@link WorkSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link WorkSourceInstanceContext}.
	 * @return Mapping of {@link WorkSource} class name to its
	 *         {@link WorkSourceInstance}.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, WorkSourceInstance> createWorkSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			WorkSourceInstanceContext context) {

		// Obtain the work source instances (by class name to get unique set)
		Map<String, WorkSourceInstance> workSourceInstances = new HashMap<String, WorkSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, WorkSource.class
					.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				workSourceInstances.put(className, new WorkSourceInstance(
						className, null, classLoader, project, context));
			}
		} catch (JavaModelException ex) {
			// Do not add the types
		}

		// Obtain via extension point second to override
		try {
			List<WorkSourceExtension> workSourceExtensions = ExtensionUtil
					.createExecutableExtensions(
							WorkSourceExtension.EXTENSION_ID,
							WorkSourceExtension.class);
			for (WorkSourceExtension workSourceExtension : workSourceExtensions) {
				Class<?> workSourceClass = workSourceExtension
						.getWorkSourceClass();
				String workSourceClassName = workSourceClass.getName();
				workSourceInstances.put(workSourceClassName,
						new WorkSourceInstance(workSourceClassName,
								workSourceExtension, classLoader, project,
								context));
			}
		} catch (Exception ex) {
			// Do not add the types
		}

		// Return work source instances by the work source class name
		return workSourceInstances;
	}

	/**
	 * {@link WorkSourceListingWizardPage}.
	 */
	private final WorkSourceListingWizardPage listingPage;

	/**
	 * {@link WorkSourcePropertiesWizardPage} pages by their
	 * {@link WorkSourceInstance}.
	 */
	private final Map<WorkSourceInstance, WorkSourcePropertiesWizardPage> propertiesPages = new HashMap<WorkSourceInstance, WorkSourcePropertiesWizardPage>();

	/**
	 * {@link WorkSourceTasksWizardPage}.
	 */
	private final WorkSourceTasksWizardPage tasksPage;

	/**
	 * Selected {@link WorkSourceInstance}.
	 */
	private WorkSourceInstance selectedWorkSourceInstance = null;

	/**
	 * Current {@link WorkSourcePropertiesWizardPage}.
	 */
	private WorkSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link WorkInstance}.
	 */
	private WorkInstance workInstance = null;

	/**
	 * Initiate to create a new {@link WorkInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	public WorkSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param workInstance
	 *            {@link WorkInstance} to be edited, or <code>null</code> to
	 *            create a new {@link WorkInstance}.
	 */
	public WorkSourceWizard(IProject project, WorkInstance workInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of work source instances
		Map<String, WorkSourceInstance> workSourceInstanceMap = createWorkSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of work source instances (in order)
		WorkSourceInstance[] workSourceInstanceListing = workSourceInstanceMap
				.values().toArray(new WorkSourceInstance[0]);
		Arrays.sort(workSourceInstanceListing,
				new Comparator<WorkSourceInstance>() {
					@Override
					public int compare(WorkSourceInstance a,
							WorkSourceInstance b) {
						return a.getWorkSourceClassName().compareTo(
								b.getWorkSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new WorkSourceListingWizardPage(
				workSourceInstanceListing);
		for (WorkSourceInstance workSourceInstance : workSourceInstanceListing) {
			this.propertiesPages
					.put(workSourceInstance,
							new WorkSourcePropertiesWizardPage(this,
									workSourceInstance));
		}
		this.tasksPage = new WorkSourceTasksWizardPage();
	}

	/**
	 * Obtains the {@link WorkInstance}.
	 * 
	 * @return {@link WorkInstance}.
	 */
	public WorkInstance getWorkInstance() {
		return this.workInstance;
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
		this.addPage(this.tasksPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedWorkSourceInstance = this.listingPage
					.getSelectedWorkSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedWorkSourceInstance);

			// Load work type to set state and return as next page
			return this.currentPropertiesPage;

		} else if (page instanceof WorkSourcePropertiesWizardPage) {
			// Properties specified, so now select tasks
			this.tasksPage
					.loadWorkSourceInstance(this.selectedWorkSourceInstance);
			return this.tasksPage;

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
		if (!this.tasksPage.isPageComplete()) {
			return false;
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the work instance
		String workName = this.tasksPage.getWorkName();
		String workSourceClassName = this.selectedWorkSourceInstance
				.getWorkSourceClassName();
		PropertyList propertyList = this.selectedWorkSourceInstance
				.getPropertyList();
		WorkType<?> workType = this.selectedWorkSourceInstance.getWorkType();
		TaskType<?, ?, ?>[] taskTypes = this.tasksPage.getSelectedTaskTypes()
				.toArray(new TaskType[0]);

		// Specify the work instance
		this.workInstance = new WorkInstance(workName, workSourceClassName,
				propertyList, workType, taskTypes);

		// Finished
		return true;
	}

	/*
	 * ================== WorkSourceInstanceContext ==========================
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
	public void setWorkTypeLoaded(boolean isWorkTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isWorkTypeLoaded);
		}
	}

}