/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;

/**
 * Raw meta-data for the bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawAdministrationMetaData {

	/**
	 * {@link RawBoundManagedObjectMetaData} instances for the
	 * {@link Administration}.
	 */
	private final RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<?, ?, ?> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param rawBoundManagedObjectMetaData
	 *            {@link RawBoundManagedObjectMetaData} instances for the
	 *            {@link Administration}.
	 * @param administrationMetaData
	 *            {@link AdministrationMetaData}.
	 */
	public RawAdministrationMetaData(RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData,
			AdministrationMetaData<?, ?, ?> administrationMetaData) {
		this.rawBoundManagedObjectMetaData = rawBoundManagedObjectMetaData;
		this.administrationMetaData = administrationMetaData;
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} of the
	 * {@link ManagedObject} instances involved in the {@link Administration}.
	 * 
	 * @return {@link RawBoundManagedObjectMetaData} of the
	 *         {@link ManagedObject} instances involved in the
	 *         {@link Administration}.
	 */
	public RawBoundManagedObjectMetaData[] getRawBoundManagedObjectMetaData() {
		return this.rawBoundManagedObjectMetaData;
	}

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	public AdministrationMetaData<?, ?, ?> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}
