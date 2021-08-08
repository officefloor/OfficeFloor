/*-
 * #%L
 * Procedure
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
		return this.objectName;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

}
