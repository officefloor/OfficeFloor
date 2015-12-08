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
package net.officefloor.frame.spi.governance;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Context for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceContext<F> {

	/**
	 * Instigates a {@link JobSequence} to be run in parallel to
	 * {@link Governance}.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the
	 *            {@link JobSequence}.
	 */
	void doFlow(F key, Object parameter);

	/**
	 * <p>
	 * Similar to {@link #doFlow(Object, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * flows available.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the
	 *            {@link JobSequence}.
	 */
	void doFlow(int flowIndex, Object parameter);

}