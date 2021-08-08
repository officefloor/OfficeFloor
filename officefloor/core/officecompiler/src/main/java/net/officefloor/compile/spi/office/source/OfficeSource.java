/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.office.source;

import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.Office;

/**
 * Sources the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSource {

	/**
	 * <p>
	 * Obtains the {@link OfficeSourceSpecification} for this
	 * {@link OfficeSource}.
	 * <p>
	 * This enables the {@link OfficeSourceContext} to be populated with the
	 * necessary details as per this {@link OfficeSourceSpecification} in
	 * loading the {@link OfficeType}.
	 * 
	 * @return {@link OfficeSourceSpecification}.
	 */
	OfficeSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeType} by constructing it via the input
	 * {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect} to structure the {@link Office}.
	 * @param context
	 *            {@link OfficeSourceContext} to source details to structure the
	 *            {@link Office}.
	 * @throws Exception
	 *             If fails to construct the {@link Office}.
	 */
	void sourceOffice(OfficeArchitect officeArchitect,
			OfficeSourceContext context) throws Exception;

}
