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
package net.officefloor.eclipse.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.dialog.input.impl.SubTypeSelectionInput;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.jdbc.JdbcManagedObjectSource;
import net.officefloor.plugin.jdbc.util.ReflectionUtil;
import net.officefloor.plugin.jdbc.util.Setter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * JDBC {@link ManagedObjectSourceExtension}.
 * 
 * @author Daniel
 */
public class JdbcManagedObjectSourceExtension implements
		ManagedObjectSourceExtension, ExtensionClasspathProvider {

	/**
	 * Sort the properties.
	 * 
	 * @param properties
	 *            Properties.
	 */
	private static void sortProperties(List<InitiateProperty> properties) {
		// Sort the properties by their weighting
		Collections.sort(properties, new Comparator<InitiateProperty>() {
			@Override
			public int compare(InitiateProperty a, InitiateProperty b) {
				// Determine wait comparison
				int weightComparison = calculateWeighting(b)
						- calculateWeighting(a);
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
	 * Calculates the weighting of the {@link InitiateProperty} for comparison.
	 * 
	 * @param property
	 *            {@link InitiateProperty}.
	 * @return Weighting.
	 */
	private static int calculateWeighting(InitiateProperty property) {
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
		if (name.startsWith("DATABASE") || name.startsWith("CATALOG")
				|| name.startsWith("SCHEMA")) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getManagedObjectSourceClass()
	 */
	@Override
	public Class<? extends ManagedObjectSource<?, ?>> getManagedObjectSourceClass() {
		return JdbcManagedObjectSource.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#isUsable()
	 */
	@Override
	public boolean isUsable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "JDBC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * net.officefloor.eclipse.
	 * extension.managedobjectsource.ManagedObjectSourceExtensionContext)
	 */
	@Override
	public List<InitiateProperty> createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Create the properties
		final InitiateProperty dataSourceProperty = new InitiateProperty(
				JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY);
		final List<InitiateProperty> configureProperties = new ArrayList<InitiateProperty>();

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Input to specify the configure properties
		final BeanListInput<InitiateProperty> propertiesInput = new BeanListInput<InitiateProperty>(
				InitiateProperty.class);
		propertiesInput.addProperty("name", 1);
		propertiesInput.addProperty("value", 2);

		// Create the input handler for changes of configuration properties
		final InputAdapter configurationPropertiesAdapter = new InputAdapter() {
			public void notifyValueChanged(Object value) {
				// Create the listing of properties
				List<InitiateProperty> properties = new LinkedList<InitiateProperty>();
				properties.add(dataSourceProperty);

				// Load only the properties that have a value
				for (InitiateProperty property : configureProperties) {
					String propertyValue = property.getValue();
					if ((propertyValue != null)
							&& (propertyValue.trim().length() > 0)) {
						properties.add(property);
					}
				}

				// Notify the properties have changed
				context.notifyPropertiesChanged(properties);
			}
		};

		// Obtain the Data Source
		new InputHandler<String>(page, new SubTypeSelectionInput(context
				.getProject(), ConnectionPoolDataSource.class.getName()),
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						// Changed data source
						String dataSourceClassName = (String) value;
						dataSourceProperty.setValue(dataSourceClassName);

						// Clear the configuration properties
						for (InitiateProperty property : configureProperties) {
							propertiesInput.removeBean(property);
						}
						configureProperties.clear();

						// Load new property set for the data source
						if (dataSourceClassName != null) {
							try {
								// Obtain the properties from data source class
								Class<?> dataSourceClass = ClasspathUtil
										.loadProjectClass(context.getProject(),
												dataSourceClassName);

								// Obtain the properties to potential set
								Setter<?>[] setters = ReflectionUtil
										.getSetters(dataSourceClass);
								for (Setter<?> setter : setters) {
									// Create the property for the setter
									String propertyName = setter
											.getPropertyName();
									InitiateProperty property = new InitiateProperty(
											propertyName);

									// Add the property
									configureProperties.add(property);
								}

								// Sort the properties
								JdbcManagedObjectSourceExtension
										.sortProperties(configureProperties);

								// Add the properties
								for (InitiateProperty property : configureProperties) {
									propertiesInput.addBean(property);
								}

							} catch (Exception ex) {
								// Indicate failure to obtain properties
								context.setErrorMessage(ex.getMessage());
								return;
							}
						}

						// Notify data source changed
						configurationPropertiesAdapter.notifyValueChanged(null);
					}
				});

		// Load properties input to page
		new InputHandler<List<InitiateProperty>>(page, propertiesInput,
				configurationPropertiesAdapter);

		// Provide button to validate connection
		Button button = new Button(page, SWT.PUSH);
		button.setText("Test connection");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Obtain the properties for the connection
				String dataSourceClassName = dataSourceProperty.getValue();
				Properties properties = new Properties();
				for (InitiateProperty property : configureProperties) {
					properties.setProperty(property.getName(), property
							.getValue());
				}

				// Test the connection
				try {
					// Create the connection pool data source
					ProjectClassLoader classLoader = ProjectClassLoader
							.create(context.getProject());
					ConnectionPoolDataSource dataSource = JdbcManagedObjectSource
							.createConnectionPoolDataSource(
									dataSourceClassName, classLoader,
									properties);

					// Obtain connection from pool to validate ok
					PooledConnection connection = dataSource
							.getPooledConnection();
					connection.close();

					// Indicate connection ok
					MessageDialog.openInformation(context.getShell(),
							"Test connection", "Connection OK");

				} catch (Throwable ex) {
					// Indicate failure to connect
					MessageDialog.openError(context.getShell(),
							"Test connection", "Connection failed: "
									+ ex.getMessage());
				}
			}
		});

		// Initially only the data source property
		return Arrays.asList(dataSourceProperty);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.extension.managedobjectsource.
	 * ManagedObjectSourceExtension
	 * #getSuggestedManagedObjectSourceName(java.util.List)
	 */
	@Override
	public String getSuggestedManagedObjectSourceName(
			List<InitiateProperty> properties) {
		return "JDBC";
	}

	/*
	 * ================ ExtensionClasspathProvider ====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider
	 * #getClasspathProvisions()
	 */
	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				JdbcManagedObjectSource.class) };
	}

}
