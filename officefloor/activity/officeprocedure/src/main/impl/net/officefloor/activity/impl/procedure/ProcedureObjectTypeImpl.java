/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureObjectType;

/**
 * {@link ProcedureObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureObjectTypeImpl implements ProcedureObjectType {

	/**
	 * Name of {@link Object}.
	 */
	private final String objectName;

	/**
	 * {@link Object} type.
	 */
	private final Class<?> objectType;

	/**
	 * Qualifier for the {@link Object}.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param objectName    Name of {@link Object}.
	 * @param objectType    {@link Object} type.
	 * @param typeQualifier Qualifier for the {@link Object}.
	 */
	public ProcedureObjectTypeImpl(String objectName, Class<?> objectType, String typeQualifier) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ================ ProcedureObjectType ====================
	 */

	@Override
	public String getObjectName() {
		// TODO implement ProcedureObjectType.getObjectName
		throw new UnsupportedOperationException("TODO implement ProcedureObjectType.getObjectName");
	}

	@Override
	public Class<?> getObjectType() {
		// TODO implement ProcedureObjectType.getObjectType
		throw new UnsupportedOperationException("TODO implement ProcedureObjectType.getObjectType");
	}

	@Override
	public String getTypeQualifier() {
		// TODO implement ProcedureObjectType.getTypeQualifier
		throw new UnsupportedOperationException("TODO implement ProcedureObjectType.getTypeQualifier");
	}

}