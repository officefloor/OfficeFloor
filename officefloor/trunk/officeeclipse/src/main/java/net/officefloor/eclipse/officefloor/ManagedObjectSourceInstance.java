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
package net.officefloor.eclipse.officefloor;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.managedobjectsource.ManagedObjectSourceLoader;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.PropertyModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * {@link ManagedObjectSource} instance.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceInstance {

	/**
	 * Fully qualified class name of the {@link ManagedObjectSource}.
	 */
	private final String className;

	/**
	 * {@link ManagedObjectSourceExtension}. May be <code>null</code> if not
	 * obtained via extension.
	 */
	private final ManagedObjectSourceExtension extension;

	/**
	 * {@link ClassLoader} to potentially load the {@link ManagedObjectSource}
	 * class.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiated {@link ManagedObjectSource} from the last call to
	 * {@link #createManagedObjectSourceModel(List)}.
	 */
	private ManagedObjectSource<?, ?> initiatedManagedObjectSource;

	/**
	 * Initiate.
	 * 
	 * @param className
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @param extension
	 *            {@link ManagedObjectSourceExtension}. May be <code>null</code>
	 *            if not obtained via extension.
	 * @param classLoader
	 *            {@link ClassLoader} to potentially load the
	 *            {@link ManagedObjectSource} class.
	 */
	public ManagedObjectSourceInstance(String className,
			ManagedObjectSourceExtension extension, ClassLoader classLoader) {
		this.className = className;
		this.extension = extension;
		this.classLoader = classLoader;
	}

	/**
	 * Obtains fully qualified class name of the {@link ManagedObjectSource}.
	 * 
	 * @return Fully qualified class name of the {@link ManagedObjectSource}.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Flag indicating if the {@link ManagedObjectSource} is usable. This allows
	 * mock/test {@link ManagedObjectSource} instances to not be included.
	 * 
	 * @return Flag indicating if the {@link ManagedObjectSource} is usable.
	 */
	public boolean isUsable() {
		return (this.extension == null ? true : this.extension.isUsable());
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
	 * Obtains the suggested name of the {@link ManagedObjectSourceModel}.
	 * 
	 * @param properties
	 *            Populated {@link InitiateProperty} instances.
	 * @return Suggested name of the {@link ManagedObjectSourceModel}.
	 */
	public String getSuggestedManagedObjectName(
			List<InitiateProperty> properties) {
		// Ensure have extension
		if (this.extension == null) {
			return ""; // no suggestion
		}

		// Return the suggested name
		return this.extension.getSuggestedManagedObjectSourceName(properties);
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link InitiateProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @return Initial {@link PropertyModel} instances. May return
	 *         <code>null</code>.
	 */
	public List<PropertyModel> createControls(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Attempt to obtain the managed object source
		ManagedObjectSource<?, ?> managedObjectSource;
		try {
			managedObjectSource = this.createManagedObjectSource();
		} catch (Exception ex) {
			// Failed to obtain managed object source, indicate failure
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Failed obtaining "
					+ ManagedObjectSource.class.getSimpleName() + " "
					+ this.className + ": " + ex.getMessage());

			// Failed to managed object source so no properties
			return null;
		}

		// Obtain the managed object source specification
		ManagedObjectSourceSpecification managedObjectSourceSpecification = null;
		try {
			managedObjectSourceSpecification = managedObjectSource
					.getSpecification();
		} catch (Throwable ex) {
			// Failed to obtain manage object source specification
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Failed obtaining "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " from " + this.className + ": " + ex.getMessage());

			// Failed to obtain specification so no properties
			return null;
		}

		// Create the property models from specification
		List<PropertyModel> propertyModels = new LinkedList<PropertyModel>();
		if (managedObjectSourceSpecification != null) {
			for (ManagedObjectSourceProperty managedObjectSourceProperty : managedObjectSourceSpecification
					.getProperties()) {
				propertyModels.add(new PropertyModel(
						managedObjectSourceProperty.getName(), null));
			}
		}

		// Determine if have extension
		if (this.extension != null) {
			// Load controls from extension
			List<InitiateProperty> initialProperties = this.extension
					.createControl(page, context);

			// Return the initial properties
			if ((initialProperties != null) && (initialProperties.size() > 0)) {
				// Have extension properties, so override specification
				return OfficeFloorUtil
						.translateForManagedObjectSource(initialProperties);
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
						context.notifyPropertiesChanged(OfficeFloorUtil
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
	 * Creates the {@link ManagedObjectSourceModel}.
	 * 
	 * @param propertyModels
	 *            {@link PropertyModel} instances to create the
	 *            {@link ManagedObjectSourceModel}.
	 * @return {@link ManagedObjectSourceModel}.
	 * @throws Exception
	 *             If fails to create the {@link ManagedObjectSourceModel}.
	 */
	public ManagedObjectSourceModel createManagedObjectSourceModel(
			List<PropertyModel> propertyModels) throws Throwable {

		// Clear the initiated managed object source
		this.initiatedManagedObjectSource = null;

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

		// Obtain the managed object source
		ManagedObjectSource<?, ?> managedObjectSource = this
				.createManagedObjectSource();

		// Provide dummy values to load
		String managedObjectSourceName = null;
		long defaultTimeout = 0;

		// Load the managed object source model
		ManagedObjectSourceModel managedObjectSourceModel = ManagedObjectSourceLoader
				.loadManagedObjectSource(managedObjectSourceName,
						managedObjectSource, properties, defaultTimeout,
						this.classLoader);

		// Managed object source initiated in loading
		this.initiatedManagedObjectSource = managedObjectSource;

		// Return the managed object source model
		return managedObjectSourceModel;
	}

	/**
	 * May be called after {@link #createManagedObjectSourceModel(List)} to
	 * obtain the initiated {@link ManagedObjectSource}.
	 * 
	 * @return Initiated {@link ManagedObjectSource}.
	 */
	public ManagedObjectSource<?, ?> getInitiatedManagedObjectSource() {
		return this.initiatedManagedObjectSource;
	}

	/**
	 * Creates the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to create the {@link ManagedObjectSource}.
	 */
	private ManagedObjectSource<?, ?> createManagedObjectSource()
			throws Exception {

		// Obtain the managed object source class
		Class<?> managedObjectSourceClass;
		if (this.extension != null) {
			// Obtain from extension
			managedObjectSourceClass = this.extension
					.getManagedObjectSourceClass();
		} else {
			// Obtain from class name
			managedObjectSourceClass = this.classLoader
					.loadClass(this.className);
		}

		// Instantiate the managed object source
		ManagedObjectSource<?, ?> managedObjectSource = (ManagedObjectSource<?, ?>) managedObjectSourceClass
				.newInstance();

		// Return the managed object source
		return managedObjectSource;
	}

}
