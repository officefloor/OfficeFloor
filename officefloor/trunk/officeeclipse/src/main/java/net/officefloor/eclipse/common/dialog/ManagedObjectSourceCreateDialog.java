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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.dialog.input.impl.SubTypeSelectionInput;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.managedobjectsource.ManagedObjectSourceLoader;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.PropertyModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Dialog} to create a {@link ManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceCreateDialog extends Dialog {

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	private ManagedObjectSourceModel managedObjectSource = null;

	/**
	 * Name of the {@link ManagedObjectSourceModel}.
	 */
	private Text managedObjectSourceName;

	/**
	 * List of {@link ManagedObjectSource} instances.
	 */
	private InputHandler<String> managedObjectSourceList;

	/**
	 * {@link Input} for the properties to create the
	 * {@link ManagedObjectSource}.
	 */
	private BeanListInput<PropertyModel> propertiesInput;

	/**
	 * {@link InputHandler} for the properties to create the
	 * {@link ManagedObjectSource}.
	 */
	private InputHandler<List<PropertyModel>> propertiesHandler;

	/**
	 * Default timeout for the {@link ManagedObjectSourceModel}.
	 */
	private Text defaultTimeout;

	/**
	 * Reports errors.
	 */
	private Label errorText;

	/**
	 * {@link Button} to provide the error detail.
	 */
	private Button errorDetailButton;

	/**
	 * Detail of the error.
	 */
	private String errorDetail = null;

	/**
	 * Initiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param project
	 *            {@link IProject}.
	 */
	public ManagedObjectSourceCreateDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * Creates the {@link ManagedObjectSourceModel}.
	 * 
	 * @return {@link ManagedObjectSourceModel} or <code>null</code> if not
	 *         created.
	 */
	public ManagedObjectSourceModel createManagedObjectSource() {

		// Block to open
		this.setBlockOnOpen(true);
		this.open();

		// Return the created managed object source
		return this.managedObjectSource;
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

		// Enter managed object source name
		new Label(composite, SWT.WRAP).setText("Name");
		this.managedObjectSourceName = new Text(composite, SWT.SINGLE
				| SWT.BORDER);

		// Enter the managed object source
		new Label(composite, SWT.WRAP).setText("Managed Object Source");
		this.managedObjectSourceList = new InputHandler<String>(composite,
				new SubTypeSelectionInput(this.project,
						ManagedObjectSource.class.getName()),
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						// Populate the properties
						ManagedObjectSourceCreateDialog.this
								.populateProperties();
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

		// Ensure default timeout
		new Label(composite, SWT.WRAP).setText("Default timeout");
		this.defaultTimeout = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.defaultTimeout.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.defaultTimeout.setText(String.valueOf(0));

		// Error text
		this.errorText = new Label(composite, SWT.WRAP);
		this.errorText.setText("");
		this.errorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.errorText.setBackground(errorText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		this.errorText.setForeground(ColorConstants.red);

		// Error detail
		this.errorDetailButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		this.errorDetailButton.setText("Error Detail");
		this.errorDetailButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtain the error detail
				String msg = ManagedObjectSourceCreateDialog.this.errorDetail;
				if (msg == null) {
					msg = "No error detail available";
				}
				MessageDialog.openError(ManagedObjectSourceCreateDialog.this
						.getShell(), "Error Detail", msg);
			}
		});

		// Return the composite
		return composite;
	}

	/**
	 * Populates the {@link Properties}.
	 */
	protected void populateProperties() {

		// Attempt to create an instance of the Managed Object Source
		ManagedObjectSource<?, ?> managedObjectSource = this
				.createManagedObjectSourceInstance();
		if (managedObjectSource == null) {
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
		ManagedObjectSourceSpecification specification = managedObjectSource
				.getSpecification();
		ManagedObjectSourceProperty[] mosProperties = (specification == null ? new ManagedObjectSourceProperty[0]
				: specification.getProperties());
		for (ManagedObjectSourceProperty mosProperty : mosProperties) {
			String propertyName = mosProperty.getName();
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
		this.clearError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		try {

			// Ensure managed object source name provided
			String managedObjectSourceName = this.managedObjectSourceName
					.getText();
			if ((managedObjectSourceName == null)
					|| (managedObjectSourceName.trim().length() == 0)) {
				this.setError("Enter managed object source name", null);
				return;
			}

			// Attempt to create the Managed Object Source
			ManagedObjectSource<?, ?> managedObjectSourceInstance = this
					.createManagedObjectSourceInstance();
			if (managedObjectSourceInstance == null) {
				return;
			}

			// Obtain the default timeout
			long defaultTimeoutValue;
			try {
				defaultTimeoutValue = Long.parseLong(this.defaultTimeout
						.getText());
			} catch (NumberFormatException ex) {
				this
						.setError("Default timeout is not numeric", ex
								.getMessage());
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
			ClassLoader projectClassLoader = ProjectClassLoader
					.create(this.project);

			// Load and specify the managed object source model
			ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
			ManagedObjectSourceModel managedObjectSourceModel = loader
					.loadManagedObjectSource(managedObjectSourceName,
							managedObjectSourceInstance, properties,
							defaultTimeoutValue, projectClassLoader);

			// Ensure default timeout if asynchronous managed object
			Class<?> managedObjectClass = managedObjectSourceInstance
					.getMetaData().getManagedObjectClass();
			if (AsynchronousManagedObject.class
					.isAssignableFrom(managedObjectClass)) {
				if (defaultTimeoutValue <= 0) {
					this.setError("Must provide default timeout as "
							+ ManagedObject.class.getSimpleName()
							+ " is asynchronous", null);
					return;
				}
			}

			// Specify the model for return
			this.managedObjectSource = managedObjectSourceModel;

		} catch (Throwable ex) {
			// Failed, report error and do not close dialog
			this.setError(ex);
			return;
		}

		// Successful
		super.okPressed();
	}

	/**
	 * Creates an instance of {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource} or <code>null</code> if not
	 *         created.
	 */
	private ManagedObjectSource<?, ?> createManagedObjectSourceInstance() {

		// Ensure managed object source provided
		String managedObjectSourceClassName = this.managedObjectSourceList
				.getTrySafeValue();
		if ((managedObjectSourceClassName == null)
				|| (managedObjectSourceClassName.trim().length() == 0)) {
			this.setError("Select a managed object source", null);
			return null;
		}

		// Attempt to create an instance of the Managed Object Source
		try {
			ProjectClassLoader classLoader = ProjectClassLoader
					.create(this.project);
			Class<?> managedObjectSourceClass = classLoader
					.loadClass(managedObjectSourceClassName);
			Object instance = managedObjectSourceClass.newInstance();
			if (!(instance instanceof ManagedObjectSource)) {
				throw new Exception(managedObjectSourceClassName
						+ " must be an instance of "
						+ ManagedObjectSource.class.getName());
			}
			ManagedObjectSource<?, ?> managedObjectSource = (ManagedObjectSource<?, ?>) instance;

			// Return the managed object source
			return managedObjectSource;

		} catch (Throwable ex) {
			this.setError(ex);
			return null;
		}
	}

	/**
	 * Clears the error.
	 */
	private void clearError() {
		this.setError(null, null);
	}

	/**
	 * Specifies the error from a {@link Throwable}.
	 * 
	 * @param error
	 *            {@link Throwable} error.
	 */
	private void setError(Throwable error) {

		// Obtain the message as the error
		String text = error.getClass().getSimpleName() + ": "
				+ error.getMessage();

		// Obtain the stack trace as detail
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));

		// Specify the error
		this.setError(text, stackTrace.toString());
	}

	/**
	 * Specifies the error.
	 * 
	 * @param text
	 *            Text of the error.
	 * @param detail
	 *            Detail of the error.
	 */
	private void setError(String text, String detail) {
		if ((text == null) || (text.trim().length() == 0)) {
			// No error
			this.errorText.setText("");
			this.errorDetail = null;
			this.errorDetailButton.setVisible(false);
		} else {
			// Specify the error
			this.errorText.setText(text);
			this.errorDetail = detail;
			this.errorDetailButton.setVisible(this.errorDetail != null);
		}
	}
}
