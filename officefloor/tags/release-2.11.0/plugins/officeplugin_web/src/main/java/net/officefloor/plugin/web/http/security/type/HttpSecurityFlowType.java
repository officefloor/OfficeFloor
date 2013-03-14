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
package net.officefloor.plugin.web.http.security.type;

import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * <code>Type definition</code> of a {@link JobSequence} instigated by the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link JobSequence}.
	 * 
	 * @return Name of the {@link JobSequence}.
	 */
	String getFlowName();

	/**
	 * Obtains the key identifying the {@link JobSequence}.
	 * 
	 * @return Key identifying the {@link JobSequence}.
	 */
	F getKey();

	/**
	 * Obtains the index identifying the {@link JobSequence}.
	 * 
	 * @return Index identifying the {@link JobSequence}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed to the {@link JobSequence}.
	 * 
	 * @return Type of argument passed to the {@link JobSequence}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

}