/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.spi.security.impl;

import net.officefloor.web.spi.security.HttpSecurityDependencyMetaData;

/**
 * Implementation of the {@link HttpSecurityDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityDependencyMetaDataImpl<D extends Enum<D>> implements
		HttpSecurityDependencyMetaData<D> {

	/**
	 * Key identifying the dependency.
	 */
	private final D key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Optional qualifier for the type.
	 */
	private String qualifier = null;

	/**
	 * Optional label to describe the dependency.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @param type
	 *            Type of dependency.
	 */
	public HttpSecurityDependencyMetaDataImpl(D key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Specifies a label to describe the dependency.
	 * 
	 * @param label
	 *            Label to describe the dependency.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	public void setTypeQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/*
	 * ================= HttpSecurityDependencyMetaData =================
	 */

	@Override
	public D getKey() {
		return this.key;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.qualifier;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}