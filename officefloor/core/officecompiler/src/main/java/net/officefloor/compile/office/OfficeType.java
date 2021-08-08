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

import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeType {

	/**
	 * Obtains the {@link OfficeInput} <code>type definitions</code> required by
	 * this {@link OfficeType}.
	 * 
	 * @return {@link OfficeInput} <code>type definitions</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeInputType[] getOfficeInputTypes();

	/**
	 * Obtains the {@link OfficeOutput} <code>type definitions</code> required
	 * by this {@link OfficeType}.
	 * 
	 * @return {@link OfficeOutput} <code>type definitions</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeOutputType[] getOfficeOutputTypes();

	/**
	 * Obtains the {@link Team} <code>type definitions</code> required by this
	 * {@link OfficeType}.
	 * 
	 * @return {@link Team} <code>type definitions</code> required by this
	 *         {@link OfficeType}.
	 */
	OfficeTeamType[] getOfficeTeamTypes();

	/**
	 * Obtains the {@link ManagedObject} <code>type definition</code> required
	 * by this {@link OfficeType}.
	 * 
	 * @return {@link ManagedObject} <code>type definition</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeManagedObjectType[] getOfficeManagedObjectTypes();

	/**
	 * Obtains the {@link OfficeSectionInput} <code>type definition</code>
	 * available for this {@link OfficeType}.
	 * 
	 * @return {@link OfficeSectionInput} <code>type definition</code> available
	 *         for this {@link OfficeType}.
	 */
	OfficeAvailableSectionInputType[] getOfficeSectionInputTypes();

}
