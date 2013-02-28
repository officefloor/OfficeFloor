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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Configuration of a {@link JobSequence} instigated by a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name to identify this {@link JobSequence}.
	 * 
	 * @return Name identifying this {@link JobSequence}.
	 */
	String getFlowName();

	/**
	 * Obtains the key for this {@link JobSequence}.
	 * 
	 * @return Key for this flow. May be <code>null</code> if {@link JobSequence}
	 *         instances are {@link Indexed}.
	 */
	F getFlowKey();

	/**
	 * Obtains the {@link TaskNodeReference} for this {@link JobSequence}.
	 * 
	 * @return {@link TaskNodeReference} to the {@link JobSequence}.
	 */
	TaskNodeReference getTaskNodeReference();

}