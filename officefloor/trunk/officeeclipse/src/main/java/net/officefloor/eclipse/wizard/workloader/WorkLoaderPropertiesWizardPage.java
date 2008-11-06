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

import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.desk.DeskUtil;
import net.officefloor.eclipse.desk.WorkLoaderInstance;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} providing the properties of the {@link WorkLoader}.
 * 
 * @author Daniel
 */
public class WorkLoaderPropertiesWizardPage extends WizardPage {

	/**
	 * {@link WorkLoaderWizard}.
	 */
	private final WorkLoaderWizard workLoaderWizard;

	/**
	 * {@link WorkLoaderInstance} instance.
	 */
	private final WorkLoaderInstance workLoaderInstance;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link PropertyModel} instances.
	 */
	private List<PropertyModel> propertyModels = null;

	/**
	 * {@link WorkModel}.
	 */
	private WorkModel<?> workModel = null;

	/**
	 * Initiate.
	 * 
	 * @param workLoaderWizard
	 *            Owning {@link WorkLoaderWizard}.
	 * @param workLoaderInstance
	 *            {@link WorkLoaderInstance} instances.
	 * @param project
	 *            {@link IProject}.
	 * @throws Exception
	 *             If fails to create.
	 */
	public WorkLoaderPropertiesWizardPage(WorkLoaderWizard workLoaderWizard,
			WorkLoaderInstance workLoaderInstance, IProject project)
			throws Exception {
		super("WorkLoader properties");
		this.workLoaderWizard = workLoaderWizard;
		this.workLoaderInstance = workLoaderInstance;
		this.project = project;

		// Specify wizard and initially not complete
		this.setWizard(this.workLoaderWizard);
		this.setPageComplete(false);
	}

	/**
	 * Obtains the {@link WorkLoaderInstance}.
	 * 
	 * @return {@link WorkLoaderInstance}.
	 */
	public WorkLoaderInstance getWorkLoaderInstance() {
		return this.workLoaderInstance;
	}

	/**
	 * Obtains the {@link PropertyModel} instances.
	 * 
	 * @return {@link PropertyModel} instances.
	 */
	public List<PropertyModel> getPropertyModels() {
		return this.propertyModels;
	}

	/**
	 * Obtains the {@link WorkModel}.
	 * 
	 * @return {@link WorkModel}.
	 */
	public WorkModel<?> getWorkModel() {
		return this.workModel;
	}

	/**
	 * Obtains the suggested name of the {@link WorkModel}.
	 * 
	 * @return Suggested name of the {@link WorkModel}.
	 */
	public String getSuggestedWorkName() {

		// Ensure have property models
		List<PropertyModel> properties = this.getPropertyModels();
		if (properties == null) {
			return ""; // no suggestion
		}

		// Obtain the suggested name
		String suggestedWorkName = this.workLoaderInstance
				.getSuggestedWorkName(DeskUtil
						.translateForExtension(properties));

		// Return the suggested name
		return (suggestedWorkName == null ? "" : suggestedWorkName);
	}

	/**
	 * Handles change to the properties.
	 * 
	 * @param propertyModels
	 *            {@link PropertyModel} instances.
	 */
	private void handlePropertiesChanged(List<PropertyModel> propertyModels) {

		// Ensure have the properties
		if (propertyModels == null) {
			propertyModels = new ArrayList<PropertyModel>(0);
		}

		// Clear the work model and error message
		this.propertyModels = propertyModels;
		this.workModel = null;
		this.setErrorMessage(null);

		// Page not complete until able to load work
		this.setPageComplete(false);

		// Ensure properties populated
		for (PropertyModel propertyModel : propertyModels) {

			// Ensure have property name
			String name = propertyModel.getName();
			if ((name == null) || (name.trim().length() == 0)) {
				this.setErrorMessage("Name not provided for property");
				return;
			}

			// Ensure have property value
			String value = propertyModel.getValue();
			if ((value == null) || (value.trim().length() == 0)) {
				this.setErrorMessage("Value not provided for property '" + name
						+ "'");
				return;
			}
		}

		try {
			// Attempt to load the work
			this.workModel = this.workLoaderInstance
					.createWorkModel(propertyModels);

			// Determine if work loaded
			if (this.workModel == null) {
				// Work model must be created
				this.setErrorMessage(this.workLoaderInstance.getClassName()
						+ " failed to provide a " + WorkModel.class.getName());
				return;
			}

			// If here successful, may move to next page
			this.setPageComplete(true);

		} catch (Throwable ex) {
			// Indicate failure to create the work model
			String message = ex.getMessage();
			if ((message == null) || (message.trim().length() == 0)) {
				message = ex.getClass().getName();
			}
			this.setErrorMessage("Failed creating work: " + message);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		// Specify default title
		this.setTitle("Specify properties");

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		List<PropertyModel> initialProperties = this.workLoaderInstance
				.createControls(page, new WorkLoaderExtensionContext() {

					@Override
					public void setTitle(String title) {
						WorkLoaderPropertiesWizardPage.this.setTitle(title);
					}

					@Override
					public void notifyPropertiesChanged(
							List<WorkLoaderProperty> properties) {
						// Notify of property changes
						WorkLoaderPropertiesWizardPage.this
								.handlePropertiesChanged(DeskUtil
										.translateForWorkLoader(properties));
					}

					@Override
					public void setErrorMessage(String message) {
						WorkLoaderPropertiesWizardPage.this
								.setErrorMessage(message);
					}

					@Override
					public IProject getProject() {
						return WorkLoaderPropertiesWizardPage.this.project;
					}

				});

		// Indicate initial state
		this.handlePropertiesChanged(initialProperties);

		// Specify control
		this.setControl(page);
	}

}
