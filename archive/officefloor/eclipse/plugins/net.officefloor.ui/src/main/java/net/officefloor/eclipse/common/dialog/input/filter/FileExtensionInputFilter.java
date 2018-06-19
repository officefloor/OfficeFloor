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
package net.officefloor.eclipse.common.dialog.input.filter;

import org.eclipse.core.resources.IFile;

import net.officefloor.eclipse.common.dialog.input.InputFilter;

/**
 * {@link InputFilter} that filters {@link IFile} instances based on extension.
 * 
 * @author Daniel Sagenschneider
 */
public class FileExtensionInputFilter implements InputFilter<IFile> {

	/**
	 * Listing of extensions to include.
	 */
	private final String[] extensions;

	/**
	 * Initiate.
	 * 
	 * @param extensions
	 *            Extensions of {@link IFile} instances to include.
	 */
	public FileExtensionInputFilter(String... extensions) {
		this.extensions = extensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputFilter#isFilter(java.lang.Object)
	 */
	@Override
	public boolean isFilter(IFile item) {

		// Determine if filter file
		boolean isFilter = true;
		if (this.extensions.length == 0) {
			// No filtering
			isFilter = false;
		} else {
			// Filter based on extension
			String fileExtension = item.getFileExtension();
			for (String extension : this.extensions) {
				if ((extension != null) && (extension.equals(fileExtension))) {
					isFilter = false;
				}
			}
		}

		// Return whether to filter
		return isFilter;
	}

}
