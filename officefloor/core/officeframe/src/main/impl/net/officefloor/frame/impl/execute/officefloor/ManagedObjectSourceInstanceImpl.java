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
package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.internal.structure.ManagedObjectExecuteContextFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSourceInstance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstanceImpl<F extends Enum<F>> implements ManagedObjectSourceInstance<F> {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, F> managedObjectSource;

	/**
	 * {@link ManagedObjectExecuteContextFactory} for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectExecuteContextFactory<F> managedObjectExecuteContextFactory;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * {@link TeamManagement} responsible for this
	 * {@link ManagedObjectSourceInstance}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectExecuteContextFactory
	 *            {@link ManagedObjectExecuteContextFactory} for the
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} responsible for this
	 *            {@link ManagedObjectSourceInstance}.
	 */
	public ManagedObjectSourceInstanceImpl(ManagedObjectSource<?, F> managedObjectSource,
			ManagedObjectExecuteContextFactory<F> managedObjectExecuteContextFactory,
			ManagedObjectPool managedObjectPool, TeamManagement responsibleTeam) {
		this.managedObjectSource = managedObjectSource;
		this.managedObjectExecuteContextFactory = managedObjectExecuteContextFactory;
		this.managedObjectPool = managedObjectPool;
		this.responsibleTeam = responsibleTeam;
	}

	/*
	 * ==================== ManagedObjectSourceInstance ==================
	 */

	@Override
	public ManagedObjectSource<?, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectExecuteContextFactory<F> getManagedObjectExecuteContextFactory() {
		return this.managedObjectExecuteContextFactory;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

}