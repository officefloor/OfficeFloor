/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.jdbc;

import java.util.Comparator;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.common.dialog.input.impl.SubTypeSelectionInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;
import net.officefloor.plugin.jdbc.util.ReflectionUtil;
import net.officefloor.plugin.jdbc.util.Setter;

/**
 * JDBC {@link ManagedObjectSourceExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class JdbcManagedObjectSourceExtension
		implements ManagedObjectSourceExtension<None, None, JdbcManagedObjectSource> {

	/**
	 * Sort the properties.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	private static void sortProperties(PropertyList properties) {
		// Sort the properties by their weighting
		properties.sort(new Comparator<Property>() {
			@Override
			public int compare(Property a, Property b) {
				// Determine wait comparison
				int weightComparison = calculateWeighting(b) - calculateWeighting(a);
				if (weightComparison != 0) {
					// Comparison determined by weighting
					return weightComparison;
				}

				// Weighting the same, so make alphabetical
				return a.getName().compareTo(b.getName());
			}
		});
	}

	/**
	 * Calculates the weighting of the {@link Property} for comparison.
	 * 
	 * @param property
	 *            {@link Property}.
	 * @return Weighting.
	 */
	private static int calculateWeighting(Property property) {
		int weighting = 0;

		// Add to waiting based on contents of name
		String name = property.getName().toUpperCase();
		if (name.startsWith("URL")) {
			weighting += 6;
		}
		if (name.startsWith("SERVER") || name.startsWith("SOURCE")) {
			weighting += 5;
		}
		if (name.startsWith("PORT")) {
			weighting += 4;
		}
		if (name.startsWith("DATABASE") || name.startsWith("CATALOG") || name.startsWith("SCHEMA")) {
			weighting += 3;
		}
		if (name.startsWith("USER")) {
			weighting += 2;
		}
		if (name.startsWith("PASSWORD")) {
			weighting += 1;
		}

		// Return the weighting
		return weighting;
	}

	/*
	 * ================= ManagedObjectSourceExtension ========================
	 */

	@Override
	public Class<JdbcManagedObjectSource> getManagedObjectSourceClass() {
		return JdbcManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "JDBC";
	}

	@Override
	public void createControl(Composite page, final ManagedObjectSourceExtensionContext context) {

		// Obtain the properties
		PropertyList properties = context.getPropertyList();

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Input for the properties
		final PropertyListInput propertiesInput = new PropertyListInput(properties);
		propertiesInput.hideProperty(JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY);

		// Obtain the Data Source
		new InputHandler<String>(page,
				new SubTypeSelectionInput(context.getProject(), ConnectionPoolDataSource.class.getName()),
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {

						// Clear the configuration properties
						PropertyList properties = context.getPropertyList();
						properties.clear();

						// Add the data source property
						String dataSourceClassName = (String) value;
						properties.addProperty(JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY)
								.setValue((String) value);

						// Load new property set for the data source
						if (dataSourceClassName != null) {
							try {
								// Obtain the properties from data source class
								Class<?> dataSourceClass = ClasspathUtil.loadProjectClass(context.getProject(),
										dataSourceClassName);

								// Obtain the properties to potential set
								Setter<?>[] setters = ReflectionUtil.getSetters(dataSourceClass);
								for (Setter<?> setter : setters) {
									// Create the property for the setter
									String propertyName = setter.getPropertyName();
									properties.addProperty(propertyName);
								}

								// Sort the properties
								JdbcManagedObjectSourceExtension.sortProperties(properties);

							} catch (Exception ex) {
								// Indicate failure to obtain properties
								context.setErrorMessage(ex.getMessage());
							}
						}

						// Refresh the properties
						propertiesInput.refreshProperties();

						// Notify data source changed
						context.notifyPropertiesChanged();
					}
				});

		// Load properties input to page
		new InputHandler<PropertyList>(page, propertiesInput, new InputAdapter() {
			@Override
			public void notifyValueChanged(Object value) {
				context.notifyPropertiesChanged();
			}
		});

		// Provide button to validate connection
		Button button = new Button(page, SWT.PUSH);
		button.setText("Test connection");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Ensure have the data source class name
				PropertyList propertyList = context.getPropertyList();
				Property dataSourceProperty = propertyList
						.getProperty(JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY);
				if ((dataSourceProperty == null) || (dataSourceProperty.getValue() == null)) {
					// Indicate must select data source
					MessageDialog.openError(null, "Test connection", "Must select a data source");
					return;
				}
				String dataSourceClassName = dataSourceProperty.getValue();

				// Obtain the properties for the connection
				Properties properties = propertyList.getProperties();

				// Test the connection
				try {
					// Create the connection pool data source
					ClassLoader classLoader = context.getClassLoader();
					ConnectionPoolDataSource dataSource = ReflectionUtil.createInitialisedBean(dataSourceClassName,
							classLoader, ConnectionPoolDataSource.class, properties);

					// Obtain connection from pool to validate
					PooledConnection connection = dataSource.getPooledConnection();
					connection.close();

					// Indicate connection successful
					MessageDialog.openInformation(null, "Test connection", "Connection successful");

				} catch (Throwable ex) {
					// Indicate failure to connect
					MessageDialog.openError(null, "Test connection",
							"Connection failed\n\n" + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		});
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "JDBC";
	}

}