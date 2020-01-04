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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.build.FlowBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Builds the {@link ManagedFunction} necessary for the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionBuilder<O extends Enum<O>, F extends Enum<F>> extends FlowBuilder<F> {

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param key           Key identifying the object.
	 * @param parameterType Type of the parameter.
	 */
	void linkParameter(O key, Class<?> parameterType);

	/**
	 * Links in the parameter for this {@link ManagedFunction}.
	 * 
	 * @param index         Index identifying the object.
	 * @param parameterType Type of the parameter.
	 */
	void linkParameter(int index, Class<?> parameterType);

	/**
	 * Links in the {@link ManagedObjectFunctionDependency} to this
	 * {@link ManagedFunction}.
	 * 
	 * @param key        Key identifying the object.
	 * @param dependency {@link ManagedObjectFunctionDependency}.
	 */
	void linkObject(O key, ManagedObjectFunctionDependency dependency);

	/**
	 * Links in the {@link ManagedObjectFunctionDependency} to this
	 * {@link ManagedFunction}.
	 * 
	 * @param index      Index identifying the object.
	 * @param dependency {@link ManagedObjectFunctionDependency}.
	 */
	void linkObject(int index, ManagedObjectFunctionDependency dependency);

	/**
	 * Links the {@link ManagedObject} input by the {@link ManagedObjectSource}.
	 * 
	 * @param key Key identifying the object.
	 */
	void linkManagedObject(O key);

	/**
	 * Links the {@link ManagedObject} input by the {@link ManagedObjectSource}.
	 * 
	 * @param index Index identifying the object.
	 */
	void linkManagedObject(int index);

	/**
	 * Links in a {@link Flow} to the {@link ManagedObjectSourceFlow}.
	 * 
	 * @param key                Key identifying the {@link Flow}.
	 * @param flow               {@link ManagedObjectSourceFlow}.
	 * @param argumentType       Type of argument passed to the instigated
	 *                           {@link Flow}. May be <code>null</code> to indicate
	 *                           no argument.
	 * @param isSpawnThreadState <code>true</code> to instigate the {@link Flow} in
	 *                           a spawned {@link ThreadState}.
	 */
	void linkFlow(F key, ManagedObjectSourceFlow flow, Class<?> argumentType, boolean isSpawnThreadState);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param flowIndex          Index identifying the {@link Flow}.
	 * @param flow               {@link ManagedObjectSourceFlow}.
	 * @param argumentType       Type of argument passed to the instigated
	 *                           {@link Flow}. May be <code>null</code> to indicate
	 *                           no argument.
	 * @param isSpawnThreadState <code>true</code> to instigate the {@link Flow} in
	 *                           a spawned {@link ThreadState}.
	 */
	void linkFlow(int flowIndex, ManagedObjectSourceFlow flow, Class<?> argumentType, boolean isSpawnThreadState);

}
