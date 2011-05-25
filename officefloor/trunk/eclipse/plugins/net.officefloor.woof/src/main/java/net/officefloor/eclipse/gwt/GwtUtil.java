/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.eclipse.gwt;

import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.plugin.gwt.module.GwtModuleRepository;
import net.officefloor.plugin.gwt.module.GwtModuleRepositoryImpl;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * GWT Utilities.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtUtil {

	/**
	 * Resources source folder within the {@link IProject}.
	 */
	private static final String RESOURCES_SOURCE_FOLDER = "src/main/resources";

	public static void createGwtModule(String templateUri,
			String entryPointClassName, IProject project,
			IProgressMonitor monitor) throws Exception {

		// Determine the module path
		int index = entryPointClassName.lastIndexOf('.');
		if (index >= 0) {
			index = entryPointClassName
					.lastIndexOf('.', (index - ".".length()));
		}
		String modulePath = entryPointClassName.substring(0, index).replace(
				'.', '/');

		// Ensure the folder exists to create the GWT Module
		IFolder folder = project.getFolder(RESOURCES_SOURCE_FOLDER + "/"
				+ modulePath);

		// Create the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo(templateUri);
		module.setEntryPointClassName(entryPointClassName);
		GwtModuleRepository repository = new GwtModuleRepositoryImpl(
				new ModelRepositoryImpl());
		repository.createGwtModule(module, new ProjectConfigurationContext(
				project).getConfigurationItem(templateUri + ".gwt.xml"));
	}

}