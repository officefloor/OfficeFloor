/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link Context}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorContext implements Context {

	/**
	 * Schema for {@link OfficeFloor} JNDI context.
	 */
	public static final String SCHEMA = "officefloor";

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
	 * Property identifying the resource path to the {@link OfficeFloor}
	 * configuration file within the {@link ClassLoader}.
	 */
	public static final String PROPERTY_OFFICE_FLOOR_CONFIGURATION_PATH = "officefloor.configuration.path";

	/*
	 * ======================= Context ============================
	 */

	@Override
	public Object lookup(Name name) throws NamingException {
		// TODO implement Context.lookup
		throw new UnsupportedOperationException("TODO implement Context.lookup");
	}

	@Override
	public Object lookup(String name) throws NamingException {
		try {
			// Ensure have name
			if (name == null) {
				return null; // no object if no name
			}

			// Strip off the schema
			int schemaEndIndex = name.indexOf(':');
			if (schemaEndIndex >= 0) {
				name = name.substring(schemaEndIndex + ".".length());
			}

			// Ensure non-blank name provided
			if (name.trim().length() == 0) {
				return null; // no object if no name
			}

			// Transform name to resource name
			String resourceName = name.replace('.', '/');

			// Obtain the class loader
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();

			// First attempt for direct OfficeFloor configuration
			String officeFloorConfigurationPath = resourceName
					+ OFFICE_FLOOR_CONFIGURATION_FILE_EXTENSION;
			URL resourceUrl = classLoader
					.getResource(officeFloorConfigurationPath);
			if (resourceUrl == null) {
				// Next attempt for properties to run OfficeFloor
				String officeFloorPropertiesPath = resourceName
						+ OFFICE_FLOOR_PROPERTIES_FILE_EXTENSION;
				resourceUrl = classLoader
						.getResource(officeFloorPropertiesPath);
				if (resourceUrl != null) {
					// Load properties for OfficeFloor
					Properties properties = new Properties();
					InputStream propertyContent = resourceUrl.openStream();
					properties.load(propertyContent);
					propertyContent.close();

					// Obtain the OfficeFloor configuration path
					officeFloorConfigurationPath = properties
							.getProperty(PROPERTY_OFFICE_FLOOR_CONFIGURATION_PATH);

				} else {
					// Unknown resource
					throw new NamingException("Unknown "
							+ OfficeFloor.class.getSimpleName() + " resource '"
							+ name + "'");
				}
			}

			// Create the OfficeFloor compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler
					.newOfficeFloorCompiler();
			final StringBuilder issues = new StringBuilder();
			compiler.setCompilerIssues(new CompilerIssues() {
				@Override
				public void addIssue(LocationType locationType,
						String location, AssetType assetType, String assetName,
						String issueDescription) {
					this.addIssue(locationType, location, assetType, assetName,
							issueDescription, null);
				}

				@Override
				public void addIssue(LocationType locationType,
						String location, AssetType assetType, String assetName,
						String issueDescription, Throwable cause) {
					issues.append("\n\t");
					issues.append(issueDescription);
					issues.append(" [");
					if (locationType != null) {
						issues.append(locationType + ":" + location);
					}
					if (assetType != null) {
						issues.append(", " + assetType + ":" + assetName);
					}
					issues.append("]");
					if (cause != null) {
						issues.append(" - " + cause.getMessage() + " ["
								+ cause.getClass().getSimpleName() + "]");
					}
				}
			});

			// Obtain issue text
			String issuesText = issues.toString();

			// Compile the OfficeFloor
			OfficeFloor officeFloor = compiler
					.compile(officeFloorConfigurationPath);
			if (officeFloor == null) {
				// Failed to compile OfficeFloor
				throw new NamingException("Failed to load "
						+ OfficeFloor.class.getSimpleName() + ": '"
						+ officeFloorConfigurationPath + "' for name '" + name
						+ "'" + issuesText);
			} else if ((issuesText != null) && (issuesText.trim().length() > 0)) {
				// Issues in compiling OfficeFloor
				throw new NamingException("Issue in loading "
						+ OfficeFloor.class.getSimpleName() + ": '"
						+ officeFloorConfigurationPath + "' for name '" + name
						+ "'" + issuesText);
			}

			// Open the OfficeFloor
			officeFloor.openOfficeFloor();

			// Return the OfficeFloor
			return officeFloor;

		} catch (Exception ex) {
			// Propagate as naming exception
			if (ex instanceof NamingException) {
				throw (NamingException) ex;
			} else {
				NamingException exception = new NamingException(ex.getMessage());
				exception.setRootCause(ex);
				throw exception;
			}
		}
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		// TODO implement Context.addToEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.addToEnvironment");
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		// TODO implement Context.getEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.getEnvironment");
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		// TODO implement Context.removeFromEnvironment
		throw new UnsupportedOperationException(
				"TODO implement Context.removeFromEnvironment");
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		// TODO implement Context.bind
		throw new UnsupportedOperationException("TODO implement Context.bind");
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		// TODO implement Context.bind
		throw new UnsupportedOperationException("TODO implement Context.bind");
	}

	@Override
	public void close() throws NamingException {
		// TODO implement Context.close
		throw new UnsupportedOperationException("TODO implement Context.close");
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		// TODO implement Context.composeName
		throw new UnsupportedOperationException(
				"TODO implement Context.composeName");
	}

	@Override
	public String composeName(String name, String prefix)
			throws NamingException {
		// TODO implement Context.composeName
		throw new UnsupportedOperationException(
				"TODO implement Context.composeName");
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		// TODO implement Context.createSubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.createSubcontext");
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		// TODO implement Context.createSubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.createSubcontext");
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		// TODO implement Context.destroySubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.destroySubcontext");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		// TODO implement Context.destroySubcontext
		throw new UnsupportedOperationException(
				"TODO implement Context.destroySubcontext");
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		// TODO implement Context.getNameInNamespace
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameInNamespace");
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		// TODO implement Context.getNameParser
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameParser");
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		// TODO implement Context.getNameParser
		throw new UnsupportedOperationException(
				"TODO implement Context.getNameParser");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		// TODO implement Context.list
		throw new UnsupportedOperationException("TODO implement Context.list");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		// TODO implement Context.list
		throw new UnsupportedOperationException("TODO implement Context.list");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		// TODO implement Context.listBindings
		throw new UnsupportedOperationException(
				"TODO implement Context.listBindings");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		// TODO implement Context.listBindings
		throw new UnsupportedOperationException(
				"TODO implement Context.listBindings");
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		// TODO implement Context.lookupLink
		throw new UnsupportedOperationException(
				"TODO implement Context.lookupLink");
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		// TODO implement Context.lookupLink
		throw new UnsupportedOperationException(
				"TODO implement Context.lookupLink");
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		// TODO implement Context.rebind
		throw new UnsupportedOperationException("TODO implement Context.rebind");
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		// TODO implement Context.rebind
		throw new UnsupportedOperationException("TODO implement Context.rebind");
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		// TODO implement Context.rename
		throw new UnsupportedOperationException("TODO implement Context.rename");
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		// TODO implement Context.rename
		throw new UnsupportedOperationException("TODO implement Context.rename");
	}

	@Override
	public void unbind(Name name) throws NamingException {
		// TODO implement Context.unbind
		throw new UnsupportedOperationException("TODO implement Context.unbind");
	}

	@Override
	public void unbind(String name) throws NamingException {
		// TODO implement Context.unbind
		throw new UnsupportedOperationException("TODO implement Context.unbind");
	}

}