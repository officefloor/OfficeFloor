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

package net.officefloor.compile.office;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> of an input into the {@link Office} that may be
 * used for a {@link ManagedObjectFlow} added to the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeAvailableSectionInputType {

	/**
	 * Obtains the name of the {@link OfficeSection} containing the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @return Name of the {@link OfficeSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the name of {@link DeployedOfficeInput} on the
	 * {@link OfficeSection}.
	 * 
	 * @return Name of this {@link DeployedOfficeInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Obtains the fully qualified class name of the parameter type to this
	 * {@link OfficeAvailableSectionInputType}.
	 * 
	 * @return Parameter type to this {@link OfficeAvailableSectionInputType}.
	 */
	String getParameterType();

}
