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

package net.officefloor.web.compile;

import net.officefloor.frame.api.manage.Office;

/**
 * Extension to compile the web application into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWebExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context
	 *            {@link CompileWebContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(CompileWebContext context) throws Exception;

}
