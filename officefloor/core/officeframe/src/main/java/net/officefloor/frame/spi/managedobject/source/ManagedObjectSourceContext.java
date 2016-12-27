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

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
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
public interface ManagedObjectSourceContext<F extends Enum<F>> extends SourceContext {

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction}
	 * of the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param key
	 *            Key identifying {@link Flow} being invoked by the
	 *            {@link ManagedObjectSource}.
	 * @param functionName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkProcess(F key, String functionName);

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction}
	 * of the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param functionName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkProcess(int flowIndex, String functionName);

	/**
	 * <p>
	 * Invoking this method during the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} will create
	 * recycle functionality for the {@link ManagedObject} to be returned to a
	 * {@link ManagedObjectPool}.
	 * <p>
	 * The initial {@link ManagedFunction} will be used as the recycle starting
	 * point for this {@link ManagedObject}.
	 *
	 * @param managedFunctionFactory
	 *            {@link ManagedFunctionFactory} to create the recycle
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedObjectFunctionBuilder} to recycle this
	 *         {@link ManagedObject}.
	 */
	<O extends Enum<O>, f extends Enum<f>> ManagedObjectFunctionBuilder<O, f> getRecycleFunction(
			ManagedFunctionFactory<O, F> managedFunctionFactory);

	/**
	 * Creates the {@link ManagedObjectFunctionBuilder} to build a
	 * {@link ManagedFunction}.
	 * 
	 * @param <o>
	 *            Dependency key type.
	 * @param <f>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param managedFunctionFactory
	 *            {@link ManagedFunctionFactory} to create the
	 *            {@link ManagedFunction}.
	 * @return Specific {@link ManagedObjectFunctionBuilder}.
	 */
	<o extends Enum<o>, f extends Enum<f>> ManagedObjectFunctionBuilder<o, f> addManagedFunction(String functionName,
			ManagedFunctionFactory<o, f> managedFunctionFactory);

	/**
	 * <p>
	 * Adds a {@link ManagedFunction} to invoke on start up of the
	 * {@link Office}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction} registered by this
	 *            {@link ManagedObjectSource}.
	 */
	void addStartupFunction(String functionName);

}