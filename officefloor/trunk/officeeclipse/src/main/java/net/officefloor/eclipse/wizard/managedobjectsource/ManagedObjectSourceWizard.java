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
package net.officefloor.eclipse.wizard.managedobjectsource;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.officefloor.ManagedObjectSourceInstance;
import net.officefloor.eclipse.officefloor.OfficeFloorUtil;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link ManagedObjectSourceModel} instances.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceWizard extends Wizard {

	/**
	 * {@link ManagedObjectSourceInstance} instances.
	 */
	private final ManagedObjectSourceInstance[] managedObjectSourceInstances;

	/**
	 * {@link ManagedObjectSourceListingWizardPage}.
	 */
	private final ManagedObjectSourceListingWizardPage listingPage;

	/**
	 * {@link ManagedObjectSourcePropertiesWizardPage} pages one each for the
	 * {@link ManagedObjectSourceInstance}.
	 */
	private final ManagedObjectSourcePropertiesWizardPage[] propertiesPages;

	/**
	 * {@link ManagedObjectSourceNameWizardPage}.
	 */
	private final ManagedObjectSourceNameWizardPage namePage;

	/**
	 * Current {@link ManagedObjectSourcePropertiesWizardPage}.
	 */
	private ManagedObjectSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	private ManagedObjectSourceModel managedObjectSource = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorEditPart
	 *            {@link OfficeFloorEditPart}.
	 * @throws Exception
	 *             If fails to create.
	 */
	public ManagedObjectSourceWizard(OfficeFloorEditPart officeFloorEditPart)
			throws Exception {

		// Obtains the project
		IProject project = FileConfigurationItem
				.getProject(officeFloorEditPart);

		// Obtain the managed object source instances
		ManagedObjectSourceInstance[] instances = OfficeFloorUtil
				.createManagedObjectSourceInstances(project);

		// Filter out managed object sources that are not usable
		List<ManagedObjectSourceInstance> instanceList = new ArrayList<ManagedObjectSourceInstance>(
				instances.length);
		for (ManagedObjectSourceInstance instance : instances) {
			if (instance.isUsable()) {
				instanceList.add(instance);
			}
		}
		this.managedObjectSourceInstances = instanceList
				.toArray(new ManagedObjectSourceInstance[0]);

		// Obtain the listing of managed object source names
		String[] managedObjectSourceNames = new String[this.managedObjectSourceInstances.length];
		for (int i = 0; i < managedObjectSourceNames.length; i++) {
			managedObjectSourceNames[i] = this.managedObjectSourceInstances[i]
					.getDisplayName();
		}

		// Create the pages
		this.listingPage = new ManagedObjectSourceListingWizardPage(
				managedObjectSourceNames);
		this.propertiesPages = new ManagedObjectSourcePropertiesWizardPage[this.managedObjectSourceInstances.length];
		for (int i = 0; i < this.propertiesPages.length; i++) {
			this.propertiesPages[i] = new ManagedObjectSourcePropertiesWizardPage(
					this, this.managedObjectSourceInstances[i], project);
		}
		this.namePage = new ManagedObjectSourceNameWizardPage();
	}

	/**
	 * Obtains the {@link ManagedObjectSourceModel}.
	 * 
	 * @return {@link ManagedObjectSourceModel} or <code>null</code> if not
	 *         created.
	 */
	public ManagedObjectSourceModel getManagedObjectSourceModel() {
		return this.managedObjectSource;
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
		this.addPage(this.namePage);
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

		} else if (page instanceof ManagedObjectSourcePropertiesWizardPage) {
			// Properties specified, so now specify name
			this.namePage.loadManagedObjectSourceModel(
					this.currentPropertiesPage
							.getInitiatedManagedObjectSource(),
					this.currentPropertiesPage.getManagedObjectSourceModel(),
					this.currentPropertiesPage
							.getSuggestedManagedObjectSourceName());
			return this.namePage;

		} else {
			// Name specified, nothing further
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

		// Ensure the name page complete
		if (!this.namePage.isPageComplete()) {
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

		// Obtain the managed object source
		ManagedObjectSourceModel managedObjectSourceModel = this.currentPropertiesPage
				.getManagedObjectSourceModel();

		// Specify the name of the managed object source
		managedObjectSourceModel.setId(this.namePage
				.getManagedObjectSourceName());

		// Specify the default timeout of the managed object source
		managedObjectSourceModel.setDefaultTimeout(String.valueOf(this.namePage
				.getDefaultTimeoutValue()));

		// Specify the managed object source
		this.managedObjectSource = managedObjectSourceModel;

		// Finished
		return true;
	}

}
