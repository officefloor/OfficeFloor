/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceContext<F extends Enum<F>> extends SourceContext {

	/**
	 * <p>
	 * Specifies the default {@link ManagedObjectPool}.
	 * <p>
	 * This may be overridden by {@link ManagedObjectBuilder} configuration, however
	 * allows for a default {@link ManagedObjectPool} should one not be configured.
	 * 
	 * @param poolFactory {@link ManagedObjectPoolFactory} for the default
	 *                    {@link ManagedObjectPool}.
	 * @return {@link ManagedObjectPoolBuilder}.
	 */
	ManagedObjectPoolBuilder setDefaultManagedObjectPool(ManagedObjectPoolFactory poolFactory);

	/**
	 * Obtains the {@link ManagedObjectSourceFlow}.
	 * 
	 * @param key Key identifying {@link Flow} being invoked by the
	 *            {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectSourceFlow} for the key.
	 */
	ManagedObjectSourceFlow getFlow(F key);

	/**
	 * Obtains the {@link ManagedObjectSourceFlow}.
	 * 
	 * @param flowIndex Index identifying the {@link Flow}.
	 * @return {@link ManagedObjectSourceFlow} for the index.
	 */
	ManagedObjectSourceFlow getFlow(int flowIndex);

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
	 * @param <O>                    Dependency key type.
	 * @param <f>                    Flow key type.
	 * @param managedFunctionFactory {@link ManagedFunctionFactory} to create the
	 *                               recycle {@link ManagedFunction}.
	 * @return {@link ManagedObjectFunctionBuilder} to recycle this
	 *         {@link ManagedObject}.
	 */
	<O extends Enum<O>, f extends Enum<f>> ManagedObjectFunctionBuilder<O, f> getRecycleFunction(
			ManagedFunctionFactory<O, f> managedFunctionFactory);

	/**
	 * Creates the {@link ManagedObjectFunctionBuilder} to build a
	 * {@link ManagedFunction}.
	 * 
	 * @param <O>                    Dependency key type.
	 * @param <f>                    Flow key type.
	 * @param functionName           Name of the {@link ManagedFunction}.
	 * @param managedFunctionFactory {@link ManagedFunctionFactory} to create the
	 *                               {@link ManagedFunction}.
	 * @return Specific {@link ManagedObjectFunctionBuilder}.
	 */
	<O extends Enum<O>, f extends Enum<f>> ManagedObjectFunctionBuilder<O, f> addManagedFunction(String functionName,
			ManagedFunctionFactory<O, f> managedFunctionFactory);

	/**
	 * Adds a {@link ManagedObjectFunctionDependency}.
	 * 
	 * @param name       Name of the {@link ManagedObjectFunctionDependency}.
	 * @param objectType Object type.
	 * @return {@link ManagedObjectFunctionDependency}.
	 */
	ManagedObjectFunctionDependency addFunctionDependency(String name, Class<?> objectType);

	/**
	 * Creates a {@link ManagedObjectStartupCompletion}.
	 * 
	 * @return New {@link ManagedObjectStartupCompletion} that must be completed
	 *         before {@link OfficeFloor} servicing.
	 */
	ManagedObjectStartupCompletion createStartupCompletion();

	/**
	 * <p>
	 * Adds a {@link ManagedFunction} to invoke on start up of the {@link Office}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param functionName Name of {@link ManagedFunction} registered by this
	 *                     {@link ManagedObjectSource}.
	 * @param parameter    Parameter for the {@link ManagedFunction}. Typically the
	 *                     parameter will contain any created
	 *                     {@link ManagedObjectStartupCompletion} to enable
	 *                     indicating when startup is complete.
	 */
	void addStartupFunction(String functionName, Object parameter);

}
