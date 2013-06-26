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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;

/**
 * <p>
 * Builder to build a {@link Duty}.
 * <p>
 * All linked {@link JobSequence} instances will be instigated in parallel.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyBuilder {

	/**
	 * Links in a {@link JobSequence} by specifying the first {@link Task} of the
	 * {@link JobSequence}.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link JobSequence} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on the {@link Work}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link JobSequence}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	<F extends Enum<F>> void linkFlow(F key, String workName, String taskName,
			Class<?> argumentType);

	/**
	 * Links in a {@link JobSequence} by specifying the first {@link Task} of the
	 * {@link JobSequence}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link JobSequence}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link JobSequence} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on the {@link Work}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link JobSequence}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	void linkFlow(int flowIndex, String workName, String taskName,
			Class<?> argumentType);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param key
	 *            Key for the {@link Duty} to identify the {@link Governance}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	<G extends Enum<G>> void linkGovernance(G key, String governanceName);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param governanceIndex
	 *            Index for the {@link Duty} to identify the {@link Governance}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	void linkGovernance(int governanceIndex, String governanceName);

}