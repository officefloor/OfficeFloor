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
package net.officefloor.eclipse.common.editpolicies.open;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.Model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Context for the {@link OpenHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenHandlerContext<M extends Model> {

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @return {@link Model}.
	 */
	M getModel();

	/**
	 * Obtains the {@link AbstractOfficeFloorEditPart} for the {@link Model}.
	 * 
	 * @return {@link AbstractOfficeFloorEditPart} for the {@link Model}.
	 */
	AbstractOfficeFloorEditPart<M, ?, ?> getEditPart();

	/**
	 * Opens the {@link IFile} on the class path for the {@link IProject}
	 * containing the {@link Model}.
	 * 
	 * @param filePath
	 *            Path to the file on the class path.
	 */
	void openClasspathFile(String filePath);

}