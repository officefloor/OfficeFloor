/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
