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
 * Describes a {@link JobSequence} required by the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityFlowMetaData<F extends Enum<F>> {

	/**
	 * Obtains the {@link Enum} key identifying the application
	 * {@link JobSequence} to instigate.
	 * 
	 * @return {@link Enum} key identifying the application {@link JobSequence}
	 *         to instigate.
	 */
	F getKey();

	/**
	 * <p>
	 * Obtains the {@link Class} of the argument that is passed to the
	 * {@link JobSequence}.
	 * <p>
	 * This may be <code>null</code> to indicate no argument is passed.
	 * 
	 * @return Type of the argument that is passed to the {@link JobSequence}.
	 */
	Class<?> getArgumentType();

	/**
	 * Provides a descriptive name for this {@link JobSequence}. This is useful
	 * to better describe the {@link JobSequence}.
	 * 
	 * @return Descriptive name for this {@link JobSequence}.
	 */
	String getLabel();

}