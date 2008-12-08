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
package net.officefloor.eclipse.desk;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.desk.WorkLoaderContextImpl;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtensionContext;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkProperty;
import net.officefloor.work.WorkSpecification;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * {@link WorkLoader} instance.
 * 
 * @author Daniel
 */
public class WorkLoaderInstance {

	/**
	 * Fully qualified class name of the {@link WorkLoader}.
	 */
	private final String className;

	/**
	 * {@link WorkLoaderExtension}. May be <code>null</code> if not obtained via
	 * extension point.
	 */
	private final WorkLoaderExtension extension;

	/**
	 * {@link ClassLoader} to potentially load the {@link WorkLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link WorkLoader}.
	 */
	private WorkLoader workLoader;

	/**
	 * Initiate.
	 * 
	 * @param className
	 *            Name of the {@link WorkLoader} class name.
	 * @param extension
	 *            {@link WorkLoaderExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public WorkLoaderInstance(String className, WorkLoaderExtension extension,
			ClassLoader classLoader) {
		this.className = className;
		this.extension = extension;
		this.classLoader = classLoader;
	}

	/**
	 * Obtains the fully qualified class name of the {@link WorkLoader}.
	 * 
	 * @return {@link WorkLoader} class name.
	 */
	public String getClassName() {
		return this.className;
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

	/**
	 * Obtains the suggested name of the {@link DeskWorkModel}.
	 * 
	 * @param properties
	 *            Populated {@link WorkLoaderProperty} instances.
	 * @return Suggested name of the {@link DeskWorkModel}.
	 */
	public String getSuggestedWorkName(List<WorkLoaderProperty> properties) {

		// Ensure have extension
		if (this.extension == null) {
			return ""; // no suggestion
		}

		// Return the suggested name
		return this.extension.getSuggestedWorkName(properties);
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link WorkLoaderProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 * @param context
	 *            {@link WorkLoaderExtensionContext}.
	 * @return Initial {@link PropertyModel} instances. May return
	 *         <code>null</code>.
	 */
	public List<PropertyModel> createControls(Composite page,
			final WorkLoaderExtensionContext context) {

		// Attempt to obtain the work loader
		WorkLoader workLoader;
		try {
			workLoader = this.getWorkLoader();
		} catch (Throwable ex) {
			// Failed to obtain work loader
			this.loadFailureControl(page, ex);
			context.setErrorMessage(ex.getMessage());
			return null;
		}

		// Obtain the work specification
		WorkSpecification workSpecification = workLoader.getSpecification();

		// Create the property models from specification
		List<PropertyModel> propertyModels = new LinkedList<PropertyModel>();
		if (workSpecification != null) {
			for (WorkProperty workProperty : workSpecification.getProperties()) {
				propertyModels.add(new PropertyModel(workProperty.getName(),
						null));
			}
		}

		// Determine if have extension
		if (this.extension != null) {

			// Load controls from extension
			List<WorkLoaderProperty> initialProperties;
			try {
				initialProperties = this.extension.createControl(page, context);
			} catch (Throwable ex) {
				// Failed to create controls
				this.loadFailureControl(page, ex);
				context.setErrorMessage(ex.getMessage());
				return null;
			}

			// Return the initial properties
			if ((initialProperties != null) && (initialProperties.size() > 0)) {
				// Have extension properties, so override specification
				return DeskUtil.translateForWorkLoader(initialProperties);
			} else {
				// No initial properties, so provide from specification
				return propertyModels;
			}
		}

		// No an extension so provide properties table to fill out
		page.setLayout(new GridLayout());
		BeanListInput<PropertyModel> propertiesInput = new BeanListInput<PropertyModel>(
				PropertyModel.class);
		propertiesInput.addProperty("name", 1);
		propertiesInput.addProperty("value", 2);
		new InputHandler<List<PropertyModel>>(page, propertiesInput,
				new InputAdapter() {
					@Override
					@SuppressWarnings("unchecked")
					public void notifyValueChanged(Object value) {
						List<PropertyModel> properties = (List<PropertyModel>) value;
						context.notifyPropertiesChanged(DeskUtil
								.translateForExtension(properties));
					}
				});

		// Initiate the properties from the specification
		for (PropertyModel propertyModel : propertyModels) {
			propertiesInput.addBean(propertyModel);
		}

		// Return initial properties from the specification
		return propertyModels;
	}

	/**
	 * Loads failure details.
	 * 
	 * @param page
	 *            Page {@link Composite}.
	 * @param failure
	 *            Failure.
	 */
	private void loadFailureControl(Composite page, Throwable failure) {
		page.setLayout(new GridLayout());
		Label label = new Label(page, SWT.NONE);
		label.setForeground(ColorConstants.red);
		label.setText("Failed obtaining " + WorkLoader.class.getSimpleName()
				+ " " + this.className + ": " + failure.getMessage());
	}

	/**
	 * Creates the {@link WorkModel}.
	 * 
	 * @param propertyModels
	 *            {@link PropertyModel} instances to create the
	 *            {@link WorkModel}.
	 * @return {@link WorkModel}.
	 * @throws Exception
	 *             If fails to create the {@link WorkModel}.
	 */
	public WorkModel<?> createWorkModel(List<PropertyModel> propertyModels)
			throws Exception {

		// Create the properties
		String[] propertyNames = new String[propertyModels.size()];
		int index = 0;
		Properties properties = new Properties();
		for (PropertyModel propertyModel : propertyModels) {

			// Obtain details of property
			String name = propertyModel.getName();
			String value = propertyModel.getValue();

			// Load the property and specify name in its order
			propertyNames[index++] = name;
			properties.setProperty(name, value);
		}

		// Attempt to load the work
		WorkModel<?> workModel = this.getWorkLoader().loadWork(
				new WorkLoaderContextImpl(propertyNames, properties,
						this.classLoader));

		// Return the work model
		return workModel;
	}

	/**
	 * Creates the {@link WorkLoader}.
	 * 
	 * @return {@link WorkLoader}.
	 * @throws Exception
	 *             If fails to create the {@link WorkLoader}.
	 */
	private WorkLoader getWorkLoader() throws Exception {

		// Lazy load the work loader
		if (this.workLoader != null) {
			return this.workLoader;
		}

		// Obtain the work loader class
		Class<?> workLoaderClass;
		if (this.extension != null) {
			// Obtain from extension
			workLoaderClass = this.extension.getWorkLoaderClass();
		} else {
			// Obtain from class name
			workLoaderClass = this.classLoader.loadClass(this.className);
		}

		// Instantiate the work loader
		this.workLoader = (WorkLoader) workLoaderClass.newInstance();

		// Return the work loader
		return this.workLoader;
	}

}
