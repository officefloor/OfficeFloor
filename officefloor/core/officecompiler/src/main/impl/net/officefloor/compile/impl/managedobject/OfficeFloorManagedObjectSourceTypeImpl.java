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

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorManagedObjectSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceTypeImpl implements
		OfficeFloorManagedObjectSourceType {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String name;

	/**
	 * {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	private final OfficeFloorManagedObjectSourcePropertyType[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	public OfficeFloorManagedObjectSourceTypeImpl(String name,
			OfficeFloorManagedObjectSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ================= OfficeFloorManagedObjectSourceType =================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorManagedObjectSourcePropertyType[] getOfficeFloorManagedObjectSourcePropertyTypes() {
		return this.properties;
	}

}
