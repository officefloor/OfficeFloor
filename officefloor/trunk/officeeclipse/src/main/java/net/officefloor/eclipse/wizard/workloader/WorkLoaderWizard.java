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
package net.officefloor.eclipse.wizard.workloader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.desk.WorkToDeskWorkSynchroniser;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.java.JavaUtil;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and managed {@link WorkModel} instances.
 * 
 * @author Daniel
 */
public class WorkLoaderWizard extends Wizard {

	/**
	 * {@link DeskEditPart}.
	 */
	private final DeskEditPart deskEditPart;

	/**
	 * {@link WorkLoaderInstance} instances.
	 */
	private final WorkLoaderInstance[] workLoaders;

	/**
	 * {@link WorkLoaderListingWizardPage}.
	 */
	private final WorkLoaderListingWizardPage listingPage;

	/**
	 * {@link WorkLoaderPropertiesWizardPage} pages one each for the
	 * {@link WorkLoaderInstance}.
	 */
	private final WorkLoaderPropertiesWizardPage[] propertiesPages;

	/**
	 * {@link WorkLoaderTasksWizardPage}.
	 */
	private final WorkLoaderTasksWizardPage tasksPage;

	/**
	 * Current {@link WorkLoaderPropertiesWizardPage}.
	 */
	private WorkLoaderPropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link DeskWorkModel}.
	 */
	private DeskWorkModel deskWork = null;

	/**
	 * Initiate.
	 * 
	 * @param deskEditPart
	 *            {@link DeskEditPart}.
	 * @throws Exception
	 *             If fails to create.
	 */
	public WorkLoaderWizard(DeskEditPart deskEditPart) throws Exception {
		this.deskEditPart = deskEditPart;

		// Obtain the work loader instances
		Map<String, WorkLoaderInstance> workLoaderInstances = new HashMap<String, WorkLoaderInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(this.deskEditPart,
					WorkLoader.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				workLoaderInstances.put(className, new WorkLoaderInstance(
						className, null));
			}
		} catch (JavaModelException ex) {
			// Do not add the types
		}

		// Obtain via extension point
		try {
			List<WorkLoaderExtension> workLoaderExtensions = ExtensionUtil
					.createExecutableExtensions(
							WorkLoaderExtension.EXTENSION_ID,
							WorkLoaderExtension.class);
			for (WorkLoaderExtension workLoaderExtension : workLoaderExtensions) {
				Class<?> workLoaderClass = workLoaderExtension
						.getWorkLoaderClass();
				String className = workLoaderClass.getName();
				workLoaderInstances.put(className, new WorkLoaderInstance(
						className, workLoaderExtension));
			}
		} catch (Exception ex) {
			// Do not add the types
		}

		// Obtain the listing of work loader instances (in order)
		this.workLoaders = workLoaderInstances.values().toArray(
				new WorkLoaderInstance[0]);
		Arrays.sort(this.workLoaders, new Comparator<WorkLoaderInstance>() {
			@Override
			public int compare(WorkLoaderInstance a, WorkLoaderInstance b) {
				return a.className.compareTo(b.className);
			}
		});

		// Obtain the listing of work loader names
		String[] workLoaderNames = new String[this.workLoaders.length];
		for (int i = 0; i < workLoaderNames.length; i++) {
			workLoaderNames[i] = this.workLoaders[i].getDisplayName();
		}

		// Obtain the class loader
		ProjectClassLoader classLoader = ProjectClassLoader.create(deskEditPart
				.getEditor());

		// Create the pages
		this.listingPage = new WorkLoaderListingWizardPage(workLoaderNames);
		this.propertiesPages = new WorkLoaderPropertiesWizardPage[this.workLoaders.length];
		for (int i = 0; i < this.propertiesPages.length; i++) {
			this.propertiesPages[i] = new WorkLoaderPropertiesWizardPage(this,
					this.workLoaders[i], classLoader);
		}
		this.tasksPage = new WorkLoaderTasksWizardPage();
	}

	/**
	 * Obtains the {@link DeskWorkModel}.
	 * 
	 * @return {@link DeskWorkModel} or <code>null</code> if not created.
	 */
	public DeskWorkModel getDeskWorkModel() {
		return this.deskWork;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		this.addPage(this.listingPage);
		if (this.propertiesPages.length > 0) {
			this.addPage(this.propertiesPages[0]);
		}
		this.addPage(this.tasksPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.
	 * IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			int selection = this.listingPage.getSelectionIndex();
			this.currentPropertiesPage = this.propertiesPages[selection];
			return this.currentPropertiesPage;

		} else if (page instanceof WorkLoaderPropertiesWizardPage) {
			// Properties specified, so now select tasks
			this.tasksPage.setWorkModel(this.currentPropertiesPage
					.getWorkModel());
			return this.tasksPage;

		} else {
			// Tasks being selected, nothing further
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		// Obtain the work
		WorkModel<?> workModel = this.currentPropertiesPage.getWorkModel();

		// Obtain the tasks to provide
		List<TaskModel<?, ?>> selectedTasks = this.tasksPage
				.getSelectedTaskModels();

		// Create and load the desk work
		DeskWorkModel deskWorkModel = new DeskWorkModel();
		WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(workModel,
				deskWorkModel);

		// Iterate over removing the unselected desk tasks
		// TODO: include this as part of synchroniseWorkOntoDeskWork
		for (Iterator<DeskTaskModel> iterator = deskWorkModel.getTasks()
				.iterator(); iterator.hasNext();) {
			DeskTaskModel deskTask = iterator.next();

			// Remove if task not contained in selected
			if (!selectedTasks.contains(deskTask.getTask())) {
				iterator.remove();
			}
		}

		// Specify the name of the work
		deskWorkModel.setId(this.tasksPage.getWorkName());

		// Specify the desk work
		this.deskWork = deskWorkModel;

		// Finished
		return true;
	}

	/**
	 * Instance of a {@link WorkLoader}.
	 */
	protected static class WorkLoaderInstance {

		/**
		 * Name of the {@link WorkLoader} class name.
		 */
		public final String className;

		/**
		 * {@link WorkLoaderExtension}. May be <code>null</code> if not obtained
		 * via extension point.
		 */
		public final WorkLoaderExtension extension;

		/**
		 * Initiate.
		 * 
		 * @param className
		 *            Name of the {@link WorkLoader} class name.
		 * @param extension
		 *            {@link WorkLoaderExtension}. May be <code>null</code>.
		 */
		public WorkLoaderInstance(String className,
				WorkLoaderExtension extension) {
			this.className = className;
			this.extension = extension;
		}

		/**
		 * Obtains the display name.
		 * 
		 * @return Display name.
		 */
		public String getDisplayName() {
			if (this.extension == null) {
				// No extension so use class name
				return this.className;
			} else {
				// Attempt to obtain from extension
				String name = this.extension.getDisplayName();
				if ((name == null) || (name.trim().length() == 0)) {
					// No name so use class name
					name = this.className;
				}
				return name;
			}
		}
	}
}
