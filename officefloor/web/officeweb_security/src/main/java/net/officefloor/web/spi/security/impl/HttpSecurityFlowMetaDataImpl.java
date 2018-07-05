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
package net.officefloor.web.spi.security.impl;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecurityFlowMetaData;

/**
 * {@link HttpSecurityFlowMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityFlowMetaDataImpl<F extends Enum<F>> implements
		HttpSecurityFlowMetaData<F> {

	/**
	 * Key identifying the {@link Flow}.
	 */
	private final F key;

	/**
	 * Type of argument passed to the {@link Flow}.
	 */
	private final Class<?> argumentType;

	/**
	 * Optional label to describe the {@link Flow}.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the {@link Flow}.
	 */
	public HttpSecurityFlowMetaDataImpl(F key, Class<?> argumentType) {
		this.key = key;
		this.argumentType = argumentType;
	}

	/**
	 * Specifies a label to describe the {@link Flow}.
	 * 
	 * @param label
	 *            Label to describe the {@link Flow}.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * ==================== HttpSecurityFlowMetaData ======================
	 */

	@Override
	public F getKey() {
		return this.key;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}