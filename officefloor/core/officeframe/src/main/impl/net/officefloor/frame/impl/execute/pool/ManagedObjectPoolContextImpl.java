/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;

/**
 * {@link ManagedObjectPoolContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolContextImpl implements ManagedObjectPoolContext {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectPoolContextImpl(ManagedObjectSource<?, ?> managedObjectSource) {
		this.managedObjectSource = managedObjectSource;
	}

	/*
	 * ===================== ManagedObjectPoolContext ========================
	 */

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public boolean isCurrentThreadManaged() {
		return ManagedExecutionFactoryImpl.isCurrentThreadManaged();
	}

}
