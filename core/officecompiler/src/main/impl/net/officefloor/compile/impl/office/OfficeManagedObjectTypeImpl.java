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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.spi.office.OfficeManagedObject;

/**
 * {@link OfficeManagedObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectTypeImpl implements OfficeManagedObjectType {

	/**
	 * Name of the {@link OfficeManagedObject}.
	 */
	private final String objectName;

	/**
	 * Type of the {@link OfficeManagedObject}.
	 */
	private final String objectType;

	/**
	 * Type qualifier for the {@link OfficeManagedObject}.
	 */
	private final String typeQualifier;

	/**
	 * Extension interfaces supported by the {@link OfficeManagedObject}.
	 */
	private final String[] extensionInterfaces;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param objectType
	 *            Type of the {@link OfficeManagedObject}.
	 * @param typeQualifier
	 *            Type qualifier for the {@link OfficeManagedObject}.
	 * @param extensionInterfaces
	 *            Extension interfaces supported by the
	 *            {@link OfficeManagedObject}.
	 */
	public OfficeManagedObjectTypeImpl(String objectName, String objectType,
			String typeQualifier, String[] extensionInterfaces) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
		this.extensionInterfaces = extensionInterfaces;
	}

	/**
	 * ======================== OfficeManagedObjectType ========================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public String[] getExtensionInterfaces() {
		return this.extensionInterfaces;
	}

}
