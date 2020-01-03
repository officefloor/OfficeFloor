/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;

/**
 * HTTP URL continuation.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpUrlContinuation extends HttpInput {

	/**
	 * Obtains the {@link OfficeFlowSinkNode} to link to this
	 * {@link HttpUrlContinuation}.
	 * 
	 * @param parameterTypeName
	 *            Name of the {@link Class} providing the possible parameters
	 *            for the {@link HttpUrlContinuation} path. May be
	 *            <code>null</code> if {@link HttpUrlContinuation} path contains
	 *            no parameters.
	 * @return {@link OfficeFlowSinkNode}.
	 * @throws CompileError
	 *             If fails to create {@link OfficeFlowSinkNode} with the
	 *             parameter type.
	 */
	OfficeFlowSinkNode getRedirect(String parameterTypeName) throws CompileError;

}
