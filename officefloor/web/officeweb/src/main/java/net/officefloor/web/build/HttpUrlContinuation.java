/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
