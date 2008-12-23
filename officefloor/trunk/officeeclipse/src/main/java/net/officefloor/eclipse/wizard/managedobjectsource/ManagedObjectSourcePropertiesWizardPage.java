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

import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.officefloor.ManagedObjectSourceInstance;
import net.officefloor.eclipse.officefloor.OfficeFloorUtil;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.PropertyModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link IWizardPage} providing the properties of the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourcePropertiesWizardPage extends WizardPage {

	/**
	 * {@link ManagedObjectSourceWizard}.
	 */
	private final ManagedObjectSourceWizard managedObjectSourceWizard;

	/**
	 * {@link ManagedObjectSourceInstance} instance.
	 */
	private final ManagedObjectSourceInstance managedObjectSourceInstance;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link PropertyModel} instances.
	 */
	private List<PropertyModel> propertyModels = null;

	/**
	 * Initiated {@link ManagedObjectSource}.
	 */
	private ManagedObjectSource<?, ?> initiatedManagedObjectSource = null;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	private ManagedObjectSourceModel managedObjectSourceModel = null;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceWizard
	 *            Owning {@link ManagedObjectSourceWizard}.
	 * @param managedObjectSourceInstance
	 *            {@link ManagedObjectSourceInstance} instances.
	 * @param project
	 *            {@link IProject}.
	 * @throws Exception
	 *             If fails to create.
	 */
	public ManagedObjectSourcePropertiesWizardPage(
			ManagedObjectSourceWizard managedObjectSourceWizard,
			ManagedObjectSourceInstance managedObjectSourceInstance,
			IProject project) throws Exception {
		super("ManagedObjectSource properties");
		this.managedObjectSourceWizard = managedObjectSourceWizard;
		this.managedObjectSourceInstance = managedObjectSourceInstance;
		this.project = project;

		// Specify wizard and initially not complete
		this.setWizard(this.managedObjectSourceWizard);
		this.setPageComplete(false);
	}

	/**
	 * Obtains the {@link ManagedObjectSourceInstance}.
	 * 
	 * @return {@link ManagedObjectSourceInstance}.
	 */
	public ManagedObjectSourceInstance getManagedObjectSourceInstance() {
		return this.managedObjectSourceInstance;
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
	 * Obtains the initiated {@link ManagedObjectSource} so that the
	 * {@link ManagedObjectSourceMetaData} may be retrieved from it.
	 * 
	 * @return Initiated {@link ManagedObjectSource}.
	 */
	public ManagedObjectSource<?, ?> getInitiatedManagedObjectSource() {
		return this.initiatedManagedObjectSource;
	}

	/**
	 * Obtains the {@link ManagedObjectSourceModel}.
	 * 
	 * @return {@link ManagedObjectSourceModel}.
	 */
	public ManagedObjectSourceModel getManagedObjectSourceModel() {
		return this.managedObjectSourceModel;
	}

	/**
	 * Obtains the suggested name of the {@link ManagedObjectSourceModel}.
	 * 
	 * @return Suggested name of the {@link ManagedObjectSourceModel}.
	 */
	public String getSuggestedManagedObjectSourceName() {

		// Ensure have property models
		List<PropertyModel> properties = this.getPropertyModels();
		if (properties == null) {
			return ""; // no suggestion
		}

		// Obtain the suggested name
		String suggestedWorkName = this.managedObjectSourceInstance
				.getSuggestedManagedObjectName(OfficeFloorUtil
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

		// Clear the managed object source model and error message
		this.propertyModels = propertyModels;
		this.initiatedManagedObjectSource = null;
		this.managedObjectSourceModel = null;
		this.setErrorMessage(null);

		// Page not complete until able to load managed object source
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
			// Attempt to load the managed object source
			this.managedObjectSourceModel = this.managedObjectSourceInstance
					.createManagedObjectSourceModel(propertyModels);

			// Determine if managed object source loaded
			if (this.managedObjectSourceModel == null) {
				// Work model must be created
				this.setErrorMessage(this.managedObjectSourceInstance
						.getClassName()
						+ " failed to provide a "
						+ ManagedObjectSourceModel.class.getName());
				return;
			}

			// Specify the initiated managed object source
			this.initiatedManagedObjectSource = this.managedObjectSourceInstance
					.getInitiatedManagedObjectSource();

			// If here successful, may move to next page
			this.setPageComplete(true);

		} catch (Throwable ex) {
			// Indicate failure to create the managed object source model
			String message = ex.getMessage();
			if ((message == null) || (message.trim().length() == 0)) {
				message = ex.getClass().getName();
			}
			this.setErrorMessage("Failed creating managed object source: "
					+ message);
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

		// Create the page for the managed object source loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create controls to populate the properties
		List<PropertyModel> initialProperties = this.managedObjectSourceInstance
				.createControls(page,
						new ManagedObjectSourceExtensionContext() {

							@Override
							public void setTitle(String title) {
								ManagedObjectSourcePropertiesWizardPage.this
										.setTitle(title);
							}

							@Override
							public void notifyPropertiesChanged(
									List<InitiateProperty> properties) {
								// Notify of property changes
								ManagedObjectSourcePropertiesWizardPage.this
										.handlePropertiesChanged(OfficeFloorUtil
												.translateForManagedObjectSource(properties));
							}

							@Override
							public void setErrorMessage(String message) {
								ManagedObjectSourcePropertiesWizardPage.this
										.setErrorMessage(message);
							}

							@Override
							public IProject getProject() {
								return ManagedObjectSourcePropertiesWizardPage.this.project;
							}

							@Override
							public Shell getShell() {
								return ManagedObjectSourcePropertiesWizardPage.this
										.getShell();
							}
						});

		// Indicate initial state
		this.handlePropertiesChanged(initialProperties);

		// Specify control
		this.setControl(page);
	}

}
