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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.frame.api.manage.OfficeFloor;

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
	 * Workspace for the {@link OfficeFloor}.
	 */
	private final File workspace;

	/**
	 * Listing of class path entries in order for the realised class path.
	 */
	private final List<String> classPathEntries = new LinkedList<String>();

	/**
	 * Warnings regarding building the class path.
	 */
	private final List<String> classPathWarnings = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param classPathFactory
	 *            {@link ClassPathFactory}.
	 * @param workspace
	 *            Workspace for the {@link OfficeFloor}.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 */
	public OfficeFloorCommandContextImpl(ClassPathFactory classPathFactory,
			File workspace, OfficeFloorDecorator[] decorators) {
		this.classPathFactory = classPathFactory;
		this.workspace = workspace;
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
		public File createWorkspaceFile(String identifier, String extension) {
			try {
				// Create and return the temporary file
				return File.createTempFile(identifier, "." + extension,
						OfficeFloorCommandContextImpl.this.workspace);

			} catch (IOException ex) {
				// Propagate failure
				throw new RuntimeException(
						"Failed to create workspace file for decoration (identifier="
								+ identifier + ", extension=" + extension + ")",
						ex);
			}
		}

		@Override
		public void includeResolvedClassPathEntry(String classpathEntry) {
			this.resolvedClassPathEntries.add(classpathEntry);
		}
	}

}