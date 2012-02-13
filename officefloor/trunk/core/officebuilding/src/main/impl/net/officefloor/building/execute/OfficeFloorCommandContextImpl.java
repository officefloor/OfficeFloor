/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.building.execute;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;

/**
 * {@link OfficeFloorCommandContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandContextImpl implements OfficeFloorCommandContext {

	/**
	 * {@link ClassPathFactory}.
	 */
	private final ClassPathFactory classPathFactory;

	/**
	 * {@link OfficeFloorDecorator} instances.
	 */
	private final OfficeFloorDecorator[] decorators;

	/**
	 * Listing of class path entries in order for the realised class path.
	 */
	private final List<String> classPathEntries = new LinkedList<String>();

	/**
	 * Environment.
	 */
	private final Properties environment = new Properties();

	/**
	 * {@link OfficeFloorCommandParameter} options.
	 */
	private final Map<String, List<String>> options = new HashMap<String, List<String>>();

	/**
	 * Warnings regarding building the class path.
	 */
	private final List<String> classPathWarnings = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param classPathFactory
	 *            {@link ClassPathFactory}.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 */
	public OfficeFloorCommandContextImpl(ClassPathFactory classPathFactory,
			OfficeFloorDecorator[] decorators) {
		this.classPathFactory = classPathFactory;
		this.decorators = decorators;
	}

	/**
	 * Obtains the {@link OfficeFloorCommand} class path.
	 * 
	 * @return {@link OfficeFloorCommand} class path.
	 */
	public String getCommandClassPath() {

		// Build the class path
		StringBuilder path = new StringBuilder();
		boolean isFirst = true;
		for (String entry : this.classPathEntries) {

			// Provide separator between entries
			if (!isFirst) {
				path.append(File.pathSeparator);
			}
			isFirst = false; // for next iteration

			// Add the class path entry
			path.append(entry);
		}

		// Return the class path
		return path.toString();
	}

	/**
	 * Obtains the {@link OfficeFloorCommand} environment.
	 * 
	 * @return {@link OfficeFloorCommand} environment.
	 */
	public Properties getCommandEnvironment() {
		return this.environment;
	}

	/**
	 * Obtains the {@link OfficeFloorCommandParameter} values.
	 * 
	 * @return {@link OfficeFloorCommandParameter} values.
	 */
	public Map<String, List<String>> getCommandOptions() {
		return this.options;
	}

	/**
	 * Obtains the warnings.
	 * 
	 * @return Warnings.
	 */
	public String[] getWarnings() {
		return this.classPathWarnings.toArray(new String[this.classPathWarnings
				.size()]);
	}

	/**
	 * Adds a class path warning.
	 * 
	 * @param description
	 *            Description of the warning.
	 * @param cause
	 *            Cause of warning.
	 */
	private void addClassPathWarning(String description, Throwable cause) {
		String message = cause.getMessage();
		String causeMessage = ((message == null)
				|| (message.trim().length() == 0) ? cause.getClass().getName()
				: message + " [" + cause.getClass().getSimpleName() + "]");
		this.classPathWarnings.add(description + " (" + causeMessage + ")");
	}

	/*
	 * ======================= OfficeFloorCommandContext ======================
	 */

	@Override
	public void includeClassPathEntry(String classPathEntry) {

		// Create the decorator context
		DecoratorContext context = new DecoratorContext(classPathEntry);

		// Decorate for the class path entry
		for (OfficeFloorDecorator decorator : this.decorators) {
			try {
				decorator.decorate(context);
			} catch (Exception ex) {
				this.addClassPathWarning("Failed decoration by "
						+ decorator.getClass().getName()
						+ " for class path entry " + classPathEntry, ex);
			}
		}

		// Determine if class path entry overridden
		if (context.resolvedClassPathEntries.size() > 0) {
			// Include the overridden class path entries
			for (String resolvedClassPathEntry : context.resolvedClassPathEntries) {
				this.classPathEntries.add(resolvedClassPathEntry);
			}
		} else {
			// Not overridden so include class path entry
			this.classPathEntries.add(classPathEntry);
		}
	}

	@Override
	public void includeClassPathArtifact(String artifactLocation) {
		try {
			// Obtain the class path entries
			String[] classPathEntries = this.classPathFactory
					.createArtifactClassPath(artifactLocation);

			// Include the class path entries
			for (String classPathEntry : classPathEntries) {
				this.includeClassPathEntry(classPathEntry);
			}

		} catch (Exception ex) {
			// Propagate failure back to execution unit
			throw new ClassPathError(ex);
		}
	}

	@Override
	public void includeClassPathArtifact(String groupId, String artifactId,
			String version, String type, String classifier) {
		try {

			// Obtain the class path entries
			String[] classPathEntries = this.classPathFactory
					.createArtifactClassPath(groupId, artifactId, version,
							type, classifier);

			// Include the class path entries
			for (String classPathEntry : classPathEntries) {
				this.includeClassPathEntry(classPathEntry);
			}

		} catch (Exception ex) {
			// Propagate failure back to execution unit
			throw new ClassPathError(ex);
		}
	}

	/**
	 * {@link OfficeFloorDecoratorContext}.
	 */
	private class DecoratorContext implements OfficeFloorDecoratorContext {

		/**
		 * Raw class path entry.
		 */
		private final String rawClassPathEntry;

		/**
		 * Resolved class path entries.
		 */
		public final List<String> resolvedClassPathEntries = new LinkedList<String>();

		/**
		 * Initiate.
		 * 
		 * @param rawClassPathEntry
		 *            Raw class path entry.
		 */
		public DecoratorContext(String rawClassPathEntry) {
			this.rawClassPathEntry = rawClassPathEntry;
		}

		/*
		 * ================== OfficeFloorDecoratorContext ==================
		 */

		@Override
		public String getRawClassPathEntry() {
			return this.rawClassPathEntry;
		}

		@Override
		public void includeResolvedClassPathEntry(String classpathEntry) {
			this.resolvedClassPathEntries.add(classpathEntry);
		}

		@Override
		public void setEnvironmentProperty(String name, String value) {
			// Load property if not overriding existing environment property
			if (!OfficeFloorCommandContextImpl.this.environment
					.containsKey(name)) {
				OfficeFloorCommandContextImpl.this.environment.setProperty(
						name, value);
			}
		}

		@Override
		public void addCommandOption(String parameterName, String value) {

			// Lazy obtain the values for the parameter
			List<String> values = OfficeFloorCommandContextImpl.this.options
					.get(parameterName);
			if (values == null) {
				values = new LinkedList<String>();
				OfficeFloorCommandContextImpl.this.options.put(parameterName,
						values);
			}

			// Add the value
			values.add(value);
		}
	}

}