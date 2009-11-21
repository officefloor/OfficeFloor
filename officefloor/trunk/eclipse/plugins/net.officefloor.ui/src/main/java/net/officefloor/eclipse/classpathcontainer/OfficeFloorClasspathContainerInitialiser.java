/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.classpathcontainer;

import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * {@link ClasspathContainerInitializer} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClasspathContainerInitialiser extends
		ClasspathContainerInitializer {

	/*
	 * ============= ClasspathContainerInitializer ==================
	 */

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {

		// Create the office floor container
		OfficeFloorClasspathContainer container;
		try {
			container = new OfficeFloorClasspathContainer(containerPath,
					project);
		} catch (Exception ex) {
			throw EclipseUtil.createCoreException(ex);
		}

		// Register the container
		JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { container }, null);
	}

	@Override
	public IStatus getSourceAttachmentStatus(IPath containerPath,
			IJavaProject project) {
		// Allow adding sources
		return Status.OK_STATUS;
	}

}