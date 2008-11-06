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

import java.util.Iterator;
import java.util.List;

import net.officefloor.desk.WorkToDeskWorkSynchroniser;
import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.desk.DeskUtil;
import net.officefloor.eclipse.desk.WorkLoaderInstance;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;

import org.eclipse.core.resources.IProject;
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

		// Obtains the project
		IProject project = FileConfigurationItem.getProject(deskEditPart);

		// Obtain the work loader instances
		this.workLoaders = DeskUtil.createWorkLoaderInstances(project);

		// Obtain the listing of work loader names
		String[] workLoaderNames = new String[this.workLoaders.length];
		for (int i = 0; i < workLoaderNames.length; i++) {
			workLoaderNames[i] = this.workLoaders[i].getDisplayName();
		}

		// Create the pages
		this.listingPage = new WorkLoaderListingWizardPage(workLoaderNames);
		this.propertiesPages = new WorkLoaderPropertiesWizardPage[this.workLoaders.length];
		for (int i = 0; i < this.propertiesPages.length; i++) {
			this.propertiesPages[i] = new WorkLoaderPropertiesWizardPage(this,
					this.workLoaders[i], project);
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
			this.tasksPage.loadWorkModel(this.currentPropertiesPage
					.getWorkModel(), this.currentPropertiesPage
					.getSuggestedWorkName());
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

		// Obtain the work and the loader
		WorkModel<?> workModel = this.currentPropertiesPage.getWorkModel();
		String loader = this.currentPropertiesPage.getWorkLoaderInstance()
				.getClassName();

		// Obtain the tasks to provide
		List<TaskModel<?, ?>> selectedTasks = this.tasksPage
				.getChosenTaskModels();

		// Create and load the desk work
		DeskWorkModel deskWorkModel = new DeskWorkModel();
		WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(workModel,
				deskWorkModel);

		// Specify the loader and properties on the work
		deskWorkModel.setLoader(loader);
		for (PropertyModel property : this.currentPropertiesPage
				.getPropertyModels()) {
			deskWorkModel.addProperty(property);
		}

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

}
