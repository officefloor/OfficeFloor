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

import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.web.HttpInputPath;

/**
 * HTTP input.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInput {

	/**
	 * Obtains the {@link OfficeFlowSourceNode} to link for handling the
	 * {@link HttpInput}.
	 * 
	 * @return {@link OfficeFlowSourceNode} to link for handling the
	 *         {@link HttpInput}.
	 */
	OfficeFlowSourceNode getInput();

	/**
	 * Obtains the {@link HttpInputPath} for this {@link HttpInput}.
	 * 
	 * @return {@link HttpInputPath} for this {@link HttpInput}.
	 */
	HttpInputPath getPath();

	/**
	 * Specifies documentation to describe this {@link HttpInput}.
	 * 
	 * @param documentation Documentation to describe this {@link HttpInput}.
	 */
	void setDocumentation(String documentation);
}
