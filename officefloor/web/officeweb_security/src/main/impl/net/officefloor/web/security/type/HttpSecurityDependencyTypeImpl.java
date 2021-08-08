/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;

/**
 * {@link HttpSecurityDependencyType} adapted from the
 * {@link ManagedObjectDependencyType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityDependencyTypeImpl<O extends Enum<O>> implements HttpSecurityDependencyType<O> {

	/**
	 * {@link ManagedObjectDependencyType}.
	 */
	private final ManagedObjectDependencyType<O> dependency;

	/**
	 * Initiate.
	 * 
	 * @param dependency {@link ManagedObjectDependencyType}.
	 */
	public HttpSecurityDependencyTypeImpl(ManagedObjectDependencyType<O> dependency) {
		this.dependency = dependency;
	}

	/*
	 * ============= HttpSecurityDependencyType =========================
	 */

	@Override
	public String getDependencyName() {
		return this.dependency.getDependencyName();
	}

	@Override
	public int getIndex() {
		return this.dependency.getIndex();
	}

	@Override
	public Class<?> getDependencyType() {
		return this.dependency.getDependencyType();
	}

	@Override
	public String getTypeQualifier() {
		return this.dependency.getTypeQualifier();
	}

	@Override
	public O getKey() {
		return this.dependency.getKey();
	}
}
