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
package net.officefloor.eclipse.dialog.input;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * {@link Input} to obtain a WoOF resource.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofFileInput extends ClasspathFileInput {

	/**
	 * Initiate.
	 * 
	 * @param editor
	 *            {@link IEditorPart}.
	 */
	public WoofFileInput(IEditorPart editor) {
		super(editor);
	}

	/**
	 * Initiate.
	 * 
	 * @param container
	 *            {@link IContainer} to search for a file.
	 * @param shell
	 *            {@link Shell}.
	 */
	public WoofFileInput(IContainer container, Shell shell) {
		super(container, shell);
	}

	/*
	 * ===================== ClasspathFileInput ======================
	 */

	@Override
	protected String transformToPath(IFile file) {

		// Determine if webapp resource
		String path = file.getFullPath().toString();
		String webappPath = "/" + file.getProject().getName()
				+ "/src/main/webapp/";
		if (path.startsWith(webappPath)) {
			// Return path within webapp directory
			path = path.substring(webappPath.length());
			return path;
		}

		// Return from class path
		return super.transformToPath(file);
	}

}