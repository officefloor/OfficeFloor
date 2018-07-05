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
package net.officefloor.model.impl.change;

import net.officefloor.model.change.Conflict;

/**
 * {@link Conflict} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ConflictImpl implements Conflict {

	/**
	 * Description of the {@link Conflict}.
	 */
	private final String conflictDescription;

	/**
	 * Cuase of the {@link Conflict}. May be <code>null</code>.
	 */
	private final Throwable cause;

	/**
	 * Initiate.
	 * 
	 * @param conflictDescription
	 *            Description of the {@link Conflict}.
	 * @param cause
	 *            Cause of the {@link Conflict}. May be <code>null</code>.
	 */
	public ConflictImpl(String conflictDescription, Throwable cause) {
		this.conflictDescription = conflictDescription;
		this.cause = cause;
	}

	/*
	 * ====================== Conflict ==================================
	 */

	@Override
	public String getConflictDescription() {
		return this.conflictDescription;
	}

	@Override
	public Throwable getConflictCause() {
		return this.cause;
	}

}