/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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