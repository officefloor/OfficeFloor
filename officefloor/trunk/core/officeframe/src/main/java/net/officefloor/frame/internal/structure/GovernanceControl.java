/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Provides control over the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceControl<I, F extends Enum<F>> {

	/**
	 * Activates the {@link Governance}.
	 * 
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails to activate the {@link Governance}.
	 */
	void activateGovernance(TaskContext<?, ?, F> taskContext) throws Throwable;

	/**
	 * Initiates {@link Governance} over the {@link ManagedObject}.
	 * 
	 * @param extension
	 *            Extension of the {@link ManagedObject} to provide
	 *            {@link Governance} over it.
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails to initiate {@link Governance} over the
	 *             {@link ManagedObject}.
	 */
	void governManagedObject(I extension, TaskContext<?, ?, F> taskContext)
			throws Throwable;

	/**
	 * Enforce the {@link Governance}.
	 * 
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails to enforce the {@link Governance}.
	 */
	void enforceGovernance(TaskContext<?, ?, F> taskContext) throws Throwable;

	/**
	 * Disregards the {@link Governance}.
	 * 
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails to disregard the {@link Governance}.
	 */
	void disregardGovernance(TaskContext<?, ?, F> taskContext) throws Throwable;

}