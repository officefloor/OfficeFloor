/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.dialog.input.filter;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;

import net.officefloor.eclipse.common.dialog.input.InputFilter;
import net.officefloor.eclipse.util.EclipseUtil;

/**
 * {@link InputFilter} that filters by name of {@link IFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileNameInputFilter implements InputFilter<IFile> {

	/**
	 * {@link Pattern} by which to include the file names.
	 */
	private Pattern includePattern = null;

	/**
	 * Specifies the regular expression on which to include the file names.
	 * 
	 * @param expression
	 *            Regular expression on which to include the file names.
	 */
	public void setFileNameRegularExpression(String expression) {

		// Ensure have expresssion
		if (EclipseUtil.isBlank(expression)) {
			this.includePattern = null;
		}

		// Attempt to parse the expression
		try {
			this.includePattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
		} catch (Throwable ex) {
			// Provide not pattern if fails to compile
			this.includePattern = null;
		}
	}

	/*
	 * ================== InputFilter ==============================
	 */

	@Override
	public boolean isFilter(IFile item) {

		// If no include pattern, then always include (not filter)
		if (this.includePattern == null) {
			return false;
		}

		// Obtain the file name
		String fileName = item.getName();

		// Determine if match (expression found in file name)
		boolean isMatch = this.includePattern.matcher(fileName).find();

		// Do not filter if match
		return !isMatch;
	}

}