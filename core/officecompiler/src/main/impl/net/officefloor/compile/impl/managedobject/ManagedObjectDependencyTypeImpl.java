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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;

/**
 * {@link ManagedObjectDependencyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyTypeImpl<D extends Enum<D>> implements ManagedObjectDependencyType<D> {

	/**
	 * Name describing this dependency.
	 */
	private final String name;

	/**
	 * Index identifying this dependency.
	 */
	private final int index;

	/**
	 * Type required of the dependency.
	 */
	private final Class<?> type;

	/**
	 * Type qualifier.
	 */
	private final String typeQualifier;

	/**
	 * Annotations for this dependency.
	 */
	private final Object[] annotations;

	/**
	 * Key identifying this dependency.
	 */
	private final D key;

	/**
	 * Initialise.
	 * 
	 * @param index         Index identifying this dependency.
	 * @param type          Type required of the dependency.
	 * @param typeQualifier Type qualifier.
	 * @param annotations   Annotations describing the dependency.
	 * @param key           Key identifying this dependency. May be
	 *                      <code>null</code>.
	 * @param label         Label describing the dependency. May be
	 *                      <code>null</code>.
	 */
	public ManagedObjectDependencyTypeImpl(int index, Class<?> type, String typeQualifier, Object[] annotations, D key,
			String label) {
		this.index = index;
		this.type = type;
		this.typeQualifier = typeQualifier;
		this.annotations = annotations;
		this.key = key;

		// Determine the name
		if (!CompileUtil.isBlank(label)) {
			this.name = label;
		} else if (this.key != null) {
			this.name = this.key.toString();
		} else {
			this.name = this.type.getSimpleName();
		}
	}

	/*
	 * ==================== ManagedObjectDependencyType =======================
	 */

	@Override
	public String getDependencyName() {
		return this.name;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getDependencyType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

	@Override
	public D getKey() {
		return this.key;
	}

}
