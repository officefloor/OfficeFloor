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
package net.officefloor.plugin.jndi.context;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * {@link ObjectFactory} for creating an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorObjectFactory implements ObjectFactory {

	/**
	 * File extension for an {@link OfficeFloor} configuration file.
	 */
	public static final String OFFICE_FLOOR_CONFIGURATION_FILE_EXTENSION = ".officefloor";

	/**
	 * File extension for a properties file containing the properties to run an
	 * {@link OfficeFloor}.
	 */
	public static final String OFFICE_FLOOR_PROPERTIES_FILE_EXTENSION = ".properties";

	/**
	 * Property identifying the {@link OfficeFloorSource} implementation to use.
	 */
	public static final String PROPERTY_OFFICE_FLOOR_SOURCE = "officefloor.source";

	/**
	 * Property identifying the resource path to the {@link OfficeFloor}
	 * configuration file within the {@link ClassLoader}.
	 */
	public static final String PROPERTY_OFFICE_FLOOR_LOCATION = "officefloor.location";

	/**
	 * Registry of the open {@link OpenOfficeFloorEntry} instances.
	 */
	private static final Map<String, OpenOfficeFloorEntry> openOfficeFloorRegistry = new HashMap<String, OpenOfficeFloorEntry>();

	/*
	 * =================== ObjectFactory ==============================
	 */

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {

		// Transform name to resource name
		StringBuilder resourceNameBuffer = new StringBuilder();
		boolean isFirst = true;
		for (int i = 0; i < name.size(); i++) {
			if (!isFirst) {
				resourceNameBuffer.append('/');
			}
			resourceNameBuffer.append(name.get(i));
			isFirst = false;
		}
		String resourceName = resourceNameBuffer.toString();
		resourceName = resourceName.replace('.', '/');

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// First attempt for direct OfficeFloor configuration
		Class<?> officeFloorSourceClass = null;
		String officeFloorLocation = resourceName + OFFICE_FLOOR_CONFIGURATION_FILE_EXTENSION;
		URL resourceUrl = classLoader.getResource(officeFloorLocation);
		if (resourceUrl == null) {
			// Next attempt for properties to run OfficeFloor
			String officeFloorPropertiesPath = resourceName + OFFICE_FLOOR_PROPERTIES_FILE_EXTENSION;
			resourceUrl = classLoader.getResource(officeFloorPropertiesPath);
			if (resourceUrl != null) {
				// Load properties for OfficeFloor
				Properties properties = new Properties();
				InputStream propertyContent = resourceUrl.openStream();
				properties.load(propertyContent);
				propertyContent.close();

				// Obtain the OfficeFloor location
				officeFloorLocation = properties.getProperty(PROPERTY_OFFICE_FLOOR_LOCATION);

				// Obtain the OfficeFloor source
				String officeFloorSourceClassName = properties.getProperty(PROPERTY_OFFICE_FLOOR_SOURCE, null);
				if (officeFloorSourceClassName != null) {
					officeFloorSourceClass = classLoader.loadClass(officeFloorSourceClassName);
				}

			} else {
				// Unknown resource
				throw new NamingException("Unknown " + OfficeFloor.class.getSimpleName() + " resource '" + name + "'");
			}
		}

		// Lazy obtain the entry for the open OfficeFloor.
		// This is synchronised separate to creation to increase performance.
		OpenOfficeFloorEntry entry;
		synchronized (openOfficeFloorRegistry) {
			entry = openOfficeFloorRegistry.get(officeFloorLocation);
			if (entry == null) {
				// No entry as yet, so create one to open OfficeFloor against
				entry = new OpenOfficeFloorEntry();
				openOfficeFloorRegistry.put(officeFloorLocation, entry);
			}
		}

		// Synchronise obtaining the OfficeFloor to ensure singleton.
		synchronized (entry) {

			// Determine if OfficeFloor already open
			OfficeFloor officeFloor = entry.officeFloor;
			if (officeFloor != null) {
				// Already open, so return it
				return officeFloor;
			}

			// Create the OfficeFloor compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
			if (officeFloorSourceClass != null) {
				compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
			}
			compiler.setOfficeFloorLocation(officeFloorLocation);
			final List<DefaultCompilerIssue> issues = new LinkedList<>();
			compiler.setCompilerIssues(new AbstractCompilerIssues() {
				@Override
				protected void handleDefaultIssue(DefaultCompilerIssue issue) {
					issues.add(issue);
				}
			});

			// Compile the OfficeFloor
			officeFloor = compiler.compile(resourceName);
			if ((officeFloor == null) || (issues.size() > 0)) {
				// Failed to compile OfficeFloor
				StringWriter reason = new StringWriter();
				reason.write("Failed to load " + OfficeFloor.class.getSimpleName() + ": '" + officeFloorLocation
						+ "' for name '" + name + "'\n\n");
				for (DefaultCompilerIssue issue : issues) {
					CompileException.printIssue(issue, new PrintWriter(reason));
				}
				throw new NamingException(reason.toString());
			}

			// Open the OfficeFloor
			officeFloor.openOfficeFloor();

			// Register the open OfficeFloor against entry
			entry.officeFloor = officeFloor;

			// Return the OfficeFloor
			return officeFloor;
		}
	}

	/**
	 * Open {@link OfficeFloor} entry within the registry.
	 */
	private class OpenOfficeFloorEntry {

		/**
		 * Open {@link OfficeFloor} for this entry.
		 */
		public OfficeFloor officeFloor;

	}

}