/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.issues;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link CompilerIssue} {@link Error} to enable propagating to
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileError extends Error {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param issueDescription Description of {@link CompilerIssue}.
	 */
	public CompileError(String issueDescription) {
		super(issueDescription);
	}

}
