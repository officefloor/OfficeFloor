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
package net.officefloor.plugin.web.http.security;

import net.officefloor.frame.internal.structure.JobSequence;

/**
 * {@link HttpSecurityFlowMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityFlowMetaDataImpl<F extends Enum<F>> implements
		HttpSecurityFlowMetaData<F> {

	/**
	 * Key identifying the {@link JobSequence}.
	 */
	private final F key;

	/**
	 * Type of argument passed to the {@link JobSequence}.
	 */
	private final Class<?> argumentType;

	/**
	 * Optional label to describe the {@link JobSequence}.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence}.
	 * @param argumentType
	 *            Type of argument passed to the {@link JobSequence}.
	 */
	public HttpSecurityFlowMetaDataImpl(F key, Class<?> argumentType) {
		this.key = key;
		this.argumentType = argumentType;
	}

	/**
	 * Specifies a label to describe the {@link JobSequence}.
	 * 
	 * @param label
	 *            Label to describe the {@link JobSequence}.
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