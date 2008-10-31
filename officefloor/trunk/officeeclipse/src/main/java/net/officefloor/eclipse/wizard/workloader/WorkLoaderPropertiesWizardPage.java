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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.desk.WorkLoaderContextImpl;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.wizard.workloader.WorkLoaderWizard.WorkLoaderInstance;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkProperty;
import net.officefloor.work.WorkSpecification;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link WorkSpecification} for the {@link Work} of the {@link WorkLoader}.
	 */
	private final WorkSpecification workSpecification;

	/**
	 * {@link WorkLoader}.
	 */
	private final WorkLoader workLoader;

	/**
	 * {@link Input} for the {@link PropertyModel} instances.
	 */
	private BeanListInput<PropertyModel> propertiesInput;

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
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws Exception
	 *             If fails to create.
	 */
	public WorkLoaderPropertiesWizardPage(WorkLoaderWizard workLoaderWizard,
			WorkLoaderInstance workLoaderInstance, ClassLoader classLoader)
			throws Exception {
		super("WorkLoader properties");
		this.workLoaderWizard = workLoaderWizard;
		this.workLoaderInstance = workLoaderInstance;
		this.classLoader = classLoader;

		// Specify wizard and initially not complete
		this.setWizard(this.workLoaderWizard);
		this.setPageComplete(false);

		// Obtain the work loader class
		Class<?> workLoaderClass;
		if (this.workLoaderInstance.extension != null) {
			// Obtain from extension
			workLoaderClass = this.workLoaderInstance.extension
					.getWorkLoaderClass();
		} else {
			// Obtain from class name
			workLoaderClass = classLoader
					.loadClass(this.workLoaderInstance.className);
		}

		// Instantiate the work loader
		this.workLoader = (WorkLoader) workLoaderClass.newInstance();

		// Obtain the work specification
		this.workSpecification = this.workLoader.getSpecification();
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
	 * Handles change to the properties.
	 * 
	 * @param propertyModels
	 *            {@link PropertyModel} instances.
	 */
	private void handlePropertiesChanged(List<PropertyModel> propertyModels) {

		// Clear the work model and error message
		this.propertyModels = propertyModels;
		this.workModel = null;
		this.setErrorMessage(null);

		// Page not complete until able to load work
		this.setPageComplete(false);

		// Create the properties
		Properties properties = new Properties();
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

			// Load the property
			properties.setProperty(name, value);
		}

		try {
			// Attempt to load the work
			this.workModel = this.workLoader
					.loadWork(new WorkLoaderContextImpl(properties,
							this.classLoader));

			// Determine if work loaded
			if (this.workModel == null) {
				// Work model must be created
				this.setErrorMessage(this.workLoaderInstance.className
						+ " failed to provide a " + WorkModel.class.getName());
			} else {
				// Successful, may move to next page
				this.setPageComplete(true);
			}

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

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);

		// Create the property models from specification
		List<PropertyModel> propertyModels = new LinkedList<PropertyModel>();
		for (WorkProperty workProperty : this.workSpecification.getProperties()) {
			propertyModels.add(new PropertyModel(workProperty.getName(), null));
		}

		// Create controls based on whether extension provided
		if (this.workLoaderInstance.extension == null) {
			// No an extension so provide properties table to fill out
			page.setLayout(new GridLayout());
			this.setTitle("Specify properties");
			this.propertiesInput = new BeanListInput<PropertyModel>(
					PropertyModel.class);
			this.propertiesInput.addProperty("name", 1);
			this.propertiesInput.addProperty("value", 2);
			new InputHandler<List<PropertyModel>>(page, this.propertiesInput,
					new InputAdapter() {
						@Override
						@SuppressWarnings("unchecked")
						public void notifyValueChanged(Object value) {
							List<PropertyModel> properties = (List<PropertyModel>) value;
							WorkLoaderPropertiesWizardPage.this
									.handlePropertiesChanged(properties);
						}
					});

			// Initiate the properties from the specification
			for (PropertyModel propertyModel : propertyModels) {
				this.propertiesInput.addBean(propertyModel);
			}

		} else {
			// Extension provided, so allow it to populate the page

			// TODO remove
			page.setLayout(new RowLayout(SWT.VERTICAL));
			new Label(page, SWT.HORIZONTAL)
					.setText("Provide page population by WorkLoader extension "
							+ this.workLoaderInstance.getDisplayName());
		}

		// Indicate initial state (no properties yet)
		this.handlePropertiesChanged(propertyModels);

		// Specify control
		this.setControl(page);
	}
}
