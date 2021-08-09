/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configures {@link HttpSecurity} around configuration for the
 * {@link OfficeArchitect} and {@link SectionDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurer {

	/**
	 * Registers {@link HttpOfficeSecurer}.
	 * 
	 * @param securer
	 *            {@link HttpOfficeSecurer}.
	 */
	void secure(HttpOfficeSecurer securer);

	/**
	 * Creates the {@link HttpFlowSecurer}.
	 * 
	 * @return {@link HttpFlowSecurer}.
	 */
	HttpFlowSecurer createFlowSecurer();

}
