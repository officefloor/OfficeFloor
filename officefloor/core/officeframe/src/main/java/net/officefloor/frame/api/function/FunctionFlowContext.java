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

package net.officefloor.frame.api.function;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;

/**
 * Context for the {@link Flow} instances from the {@link ManagedFunctionLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowContext<F extends Enum<F>> {

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	Logger getLogger();

	/**
	 * Instigates a {@link Flow} to be run from the {@link ManagedFunctionLogic}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter for the first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Similar to {@link #doFlow(Enum, Object, FlowCallback)} except that allows
	 * dynamic instigation of {@link Flow} instances.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * {@link Flow} instances available.
	 * 
	 * @param flowIndex Index identifying the {@link Flow} to instigate.
	 * @param parameter Parameter for the first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

	/**
	 * Creates an {@link AsynchronousFlow} that must be completed before any further
	 * {@link Flow} is executed.
	 * 
	 * @return {@link AsynchronousFlow} that must be completed.
	 */
	AsynchronousFlow createAsynchronousFlow();

	/**
	 * <p>
	 * Obtains the {@link Executor} to run on another {@link Thread}.
	 * <p>
	 * {@link Runnable} instances provided to this {@link Executor} will always be
	 * executed on another {@link Thread}. This allows breaking thread stack
	 * execution.
	 * <p>
	 * Note that the returned {@link Executor} is a singleton per
	 * {@link OfficeFloor}. Therefore, be careful to not tie up all its
	 * {@link Thread} instances with blocking / long running {@link Runnable}
	 * instances. Preference should be to use {@link ManagedFunction} instances
	 * where appropriate {@link Thread} injection can manage execution.
	 * 
	 * @return {@link Executor} to run {@link Runnable} on another {@link Thread}.
	 */
	Executor getExecutor();

}
