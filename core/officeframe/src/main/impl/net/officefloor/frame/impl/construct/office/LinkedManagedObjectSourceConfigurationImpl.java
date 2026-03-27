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

package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;

/**
 * {@link LinkedManagedObjectSourceConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkedManagedObjectSourceConfigurationImpl implements
		LinkedManagedObjectSourceConfiguration {

	/**
	 * {@link Office} name of the {@link ManagedObject}.
	 */
	private final String officeManagedObjectName;

	/**
	 * {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	private final String officeFloorManagedObjectSourceName;

	/**
	 * Initiate.
	 * 
	 * @param officeManagedObjectName
	 *            {@link Office} name of the {@link ManagedObject}.
	 * @param officeFloorManagedObjectSourceName
	 *            {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	public LinkedManagedObjectSourceConfigurationImpl(String officeManagedObjectName,
			String officeFloorManagedObjectSourceName) {
		this.officeManagedObjectName = officeManagedObjectName;
		this.officeFloorManagedObjectSourceName = officeFloorManagedObjectSourceName;
	}

	/*
	 * ================ LinkedManagedObjectSourceConfiguration ================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.officeManagedObjectName;
	}

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.officeFloorManagedObjectSourceName;
	}

}
