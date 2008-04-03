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
package net.officefloor.eclipse.common.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.desk.DeskLoader;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.dialog.input.impl.SubTypeSelectionInput;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkProperty;
import net.officefloor.work.WorkSpecification;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Dialog} to create a {@link DeskWorkModel}.
 * 
 * @author Daniel
 */
public class DeskWorkCreateDialog extends Dialog {

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link DeskWorkModel} to create.
	 */
	private DeskWorkModel deskWorkModel = null;

	/**
	 * {@link Text} to get the {@link DeskWorkModel} name.
	 */
	private Text workName;

	/**
	 * List to obtain the {@link WorkLoader} class name.
	 */
	private InputHandler<String> workLoaderList;

	/**
	 * {@link Input} for the properties to create the {@link WorkModel}.
	 */
	private BeanListInput<PropertyModel> propertiesInput;

	/**
	 * {@link InputHandler} for properties to create the {@link WorkModel}.
	 */
	private InputHandler<List<PropertyModel>> propertiesHandler;

	/**
	 * Reports errors.
	 */
	private Label errorText;

	/**
	 * Initiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param project
	 *            {@link IProject}.
	 */
	public DeskWorkCreateDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * Creates the {@link WorkModel}.
	 * 
	 * @return {@link WorkModel}.
	 */
	public DeskWorkModel createDeskWork() throws Exception {

		// Block to open
		this.setBlockOnOpen(true);
		this.open();

		// Return the created work
		return this.deskWorkModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// Create parent composite
		Composite composite = (Composite) super.createDialogArea(parent);

		// Enter work name
		new Label(composite, SWT.WRAP).setText("Name");
		this.workName = new Text(composite, SWT.SINGLE | SWT.BORDER);

		// Enter the work loader
		new Label(composite, SWT.WRAP).setText("Work Loader");
		this.workLoaderList = new InputHandler<String>(composite,
				new SubTypeSelectionInput(this.project, WorkLoader.class
						.getName()), new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						DeskWorkCreateDialog.this.populateProperties();
					}
				});

		// Enter the properties
		new Label(composite, SWT.WRAP).setText("Properties");
		this.propertiesInput = new BeanListInput<PropertyModel>(
				PropertyModel.class);
		this.propertiesInput.addProperty("name", 1);
		this.propertiesInput.addProperty("value", 2);
		this.propertiesHandler = new InputHandler<List<PropertyModel>>(
				composite, this.propertiesInput);

		// Error text
		this.errorText = new Label(composite, SWT.WRAP);
		this.errorText.setText("");
		GridData errorGridData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL);
		errorGridData.heightHint = 60;
		this.errorText.setLayoutData(errorGridData);
		this.errorText.setBackground(errorText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		this.errorText.setForeground(ColorConstants.red);

		// Return the composite
		return composite;
	}

	/**
	 * Populates the {@link Properties}.
	 */
	protected void populateProperties() {

		// Attempt to create an instance of the Work Loader
		WorkLoader workLoader = this.createWorkLoaderInstance();
		if (workLoader == null) {
			return;
		}

		// Obtain the list of existing property names
		List<PropertyModel> existingPropertyList = this.propertiesHandler
				.getTrySafeValue();
		Map<String, PropertyModel> existingProperties = new HashMap<String, PropertyModel>(
				existingPropertyList.size());
		for (PropertyModel existingProperty : existingPropertyList) {
			existingProperties
					.put(existingProperty.getName(), existingProperty);
		}

		// Synchronise the properties
		WorkSpecification specification = workLoader.getSpecification();
		WorkProperty[] workProperties = (specification == null ? new WorkProperty[0]
				: specification.getProperties());
		for (WorkProperty workProperty : workProperties) {
			String propertyName = workProperty.getName();
			if (existingProperties.containsKey(propertyName)) {
				// Remove (so not removed later)
				existingProperties.remove(propertyName);
			} else {
				// Add property
				PropertyModel property = new PropertyModel(propertyName, "");
				this.propertiesInput.addBean(property);
			}
		}

		// Remove no longer existing properties
		for (PropertyModel oldProperty : existingProperties.values()) {
			this.propertiesInput.removeBean(oldProperty);
		}

		// No errors if at this point
		this.errorText.setText("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		try {

			// Ensure work name provided
			String workName = this.workName.getText();
			if ((workName == null) || (workName.trim().length() == 0)) {
				this.errorText.setText("Enter work name");
				return;
			}

			// Attempt to create the Work Loader
			WorkLoader workLoaderInstance = this.createWorkLoaderInstance();
			if (workLoaderInstance == null) {
				return;
			}

			// Create the list of properties
			List<PropertyModel> propertyModels = this.propertiesHandler
					.getTrySafeValue();
			Properties properties = new Properties();
			for (PropertyModel propertyModel : propertyModels) {
				String name = propertyModel.getName();
				String value = propertyModel.getValue();
				properties.setProperty(name, value);
			}

			// Obtain the class loader for the project
			ProjectClassLoader classLoader = ProjectClassLoader
					.create(this.project);

			// Load the desk work model
			DeskWorkModel work = new DeskWorkModel(workName, workLoaderInstance
					.getClass().getName(), null, null, null, propertyModels
					.toArray(new PropertyModel[0]));
			new DeskLoader(classLoader).loadWork(work, classLoader
					.getConfigurationContext());

			// Specify the desk work model for return
			this.deskWorkModel = work;

		} catch (NoClassDefFoundError ex) {
			// Indicated class missing on class path
			this.errorText.setText(ex.getClass().getSimpleName() + ":\n"
					+ ex.getMessage() + "\nPlease ensure on class path");
			return;

		} catch (Throwable ex) {
			// Failed, report error and do not close dialog
			this.errorText.setText(ex.getClass().getSimpleName() + ":\n"
					+ ex.getMessage());
			return;
		}

		// Successful
		super.okPressed();
	}

	/**
	 * Creates the {@link WorkLoader} instance.
	 * 
	 * @return {@link WorkLoader} instance.
	 */
	private WorkLoader createWorkLoaderInstance() {

		// Ensure the work loader selected
		String workLoaderClassName = this.workLoaderList.getTrySafeValue();
		if ((workLoaderClassName == null)
				|| (workLoaderClassName.trim().length() == 0)) {
			this.errorText.setText("Select a Work Loader");
			return null;
		}

		// Attempt to create an instance of the Work Loader
		try {
			ProjectClassLoader classLoader = ProjectClassLoader
					.create(this.project);
			Class<?> workLoaderClass = classLoader
					.loadClass(workLoaderClassName);
			Object instance = workLoaderClass.newInstance();
			if (!(instance instanceof WorkLoader)) {
				throw new Exception(workLoaderClassName
						+ " must be an instance of "
						+ WorkLoader.class.getName());
			}
			WorkLoader workLoader = (WorkLoader) instance;

			// Return the work loader
			return workLoader;

		} catch (Throwable ex) {
			this.errorText.setText(ex.getMessage() + " ["
					+ ex.getClass().getSimpleName() + "]");
			return null;
		}
	}

}
