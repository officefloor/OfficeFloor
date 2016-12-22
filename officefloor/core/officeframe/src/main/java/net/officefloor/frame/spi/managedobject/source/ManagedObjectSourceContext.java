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
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * Context for a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceContext<F extends Enum<F>> extends
		SourceContext {

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this {@link ManagedObjectSource}.
	 * 
	 * @param key
	 *            Key identifying {@link Flow} being invoked by the
	 *            {@link ManagedObjectSource}.
	 * @param workName
	 *            Name of the {@link Work} that the {@link ManagedFunction} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkProcess(F key, String workName, String taskName);

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this {@link ManagedObjectSource}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the {@link ManagedFunction} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkProcess(int flowIndex, String workName, String taskName);

	/**
	 * <p>
	 * Invoking this method during the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} will create
	 * recycle functionality for the {@link ManagedObject} to be returned to a
	 * {@link ManagedObjectPool}.
	 * <p>
	 * The initial {@link ManagedFunction} will be used as the recycle starting point for
	 * this {@link ManagedObject}.
	 *
	 * @param <W>
	 *            {@link Work} type.
	 * @param workFactory
	 *            {@link WorkFactory} to create the recycle {@link Work}.
	 * @return {@link WorkBuilder} to recycle this {@link ManagedObject}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> getRecycleWork(
			WorkFactory<W> workFactory);

	/**
	 * Adds {@link ManagedObjectWorkBuilder} for {@link Work} of the
	 * {@link ManagedObjectSource}.
	 *
	 * @param <W>
	 *            {@link Work} type.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory} to create the {@link Work}.
	 * @return {@link ManagedObjectWorkBuilder}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> addWork(String workName,
			WorkFactory<W> workFactory);

	/**
	 * <p>
	 * Adds a {@link ManagedFunction} to invoke on start up of the {@link Office}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this {@link ManagedObjectSource}.
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link ManagedFunction}.
	 * @param taskName
	 *            Name of {@link ManagedFunction} on the {@link Work}.
	 */
	void addStartupTask(String workName, String taskName);

}