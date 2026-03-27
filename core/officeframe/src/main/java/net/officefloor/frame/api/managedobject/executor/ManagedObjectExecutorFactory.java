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

package net.officefloor.frame.api.managedobject.executor;

import java.util.concurrent.Executor;
import java.util.function.Function;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceFlow;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.Labeller;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Convenience class to wrap the {@link ManagedObjectServiceContext} as an
 * {@link Executor}.
 * <p>
 * This allows the {@link ManagedObjectSource} to integrate with libraries
 * requiring an {@link Executor}. An example being SSL requiring to run
 * {@link Runnable} tasks.
 * 
 * @param <F> Flow {@link Enum}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecutorFactory<F extends Enum<F>> {

	/**
	 * Dependency keys for {@link ManagedFunction}.
	 */
	private static enum Dependencies {
		RUNNABLE
	}

	/**
	 * {@link ManagedFunctionFactory} to execute the {@link Runnable}.
	 */
	private static ManagedFunctionFactory<Dependencies, None> executorManagedFunctionFactory = () -> (context) -> {
		Runnable runnable = (Runnable) context.getObject(Dependencies.RUNNABLE);
		runnable.run();
	};

	/**
	 * Links the {@link ProcessState} {@link Flow} to execute the {@link Runnable}.
	 * 
	 * @param mosContext  {@link ManagedObjectSourceContext}.
	 * @param teamName    Name of the {@link Team}.
	 * @param flowFactory {@link Function} to create the
	 *                    {@link ManagedObjectSourceFlow} from the
	 *                    {@link ManagedObjectSourceContext}.
	 */
	private static <F extends Enum<F>> void linkRunnableProcess(ManagedObjectSourceContext<F> mosContext,
			String teamName, Function<ManagedObjectSourceContext<F>, ManagedObjectSourceFlow> flowFactory) {

		// Link in the managed function
		ManagedObjectFunctionBuilder<Dependencies, None> managedFunction = mosContext.addManagedFunction("Executor",
				executorManagedFunctionFactory);
		managedFunction.linkParameter(Dependencies.RUNNABLE, Runnable.class);
		managedFunction.setResponsibleTeam(teamName);

		// Configure flow to point to managed function
		ManagedObjectSourceFlow flow = flowFactory.apply(mosContext);
		flow.linkFunction("Executor");
	}

	/**
	 * {@link TriggerProcess}.
	 */
	private final TriggerProcess<F> trigger;

	/**
	 * Instantiate for invoking by {@link Flow} key.
	 * 
	 * @param context  {@link MetaDataContext} for the {@link ManagedObjectSource}.
	 * @param flowKey  {@link Flow} key to register the {@link Flow}.
	 * @param teamName Name of the {@link Team}.
	 */
	public ManagedObjectExecutorFactory(MetaDataContext<?, F> context, F flowKey, String teamName) {

		// Link in handling of flow
		context.addFlow(flowKey, Runnable.class);
		linkRunnableProcess(context.getManagedObjectSourceContext(), teamName,
				(mosContext) -> mosContext.getFlow(flowKey));

		// Create the trigger
		this.trigger = (executeContext, managedObject, runnable) -> executeContext.invokeProcess(flowKey, runnable,
				managedObject, 0, null);
	}

	/**
	 * Instantiate for invoking by next index.
	 * 
	 * @param context  {@link MetaDataContext} for the {@link ManagedObjectSource}.
	 * @param teamName Name of the {@link Team}.
	 */
	public ManagedObjectExecutorFactory(MetaDataContext<?, Indexed> context, String teamName) {

		// Add the flow (capturing process index)
		Labeller<Indexed> flow = context.addFlow(Runnable.class);
		flow.setLabel(Executor.class.getSimpleName());
		int processIndex = flow.getIndex();
		linkRunnableProcess(context.getManagedObjectSourceContext(), teamName,
				(mosContext) -> mosContext.getFlow(processIndex));

		// Create the trigger
		this.trigger = (executeContext, managedObject, runnable) -> executeContext.invokeProcess(processIndex, runnable,
				managedObject, 0, null);
	}

	/**
	 * Creates the {@link Executor}.
	 * 
	 * @param context       {@link ManagedObjectServiceContext} for the
	 *                      {@link ManagedObjectSource}.
	 * @param managedObject {@link ManagedObject} used for all {@link Runnable}
	 *                      executions.
	 * @return {@link Executor} that delegates to the
	 *         {@link ManagedObjectExecuteContext} to execute the {@link Runnable}
	 *         instances.
	 */
	public Executor createExecutor(ManagedObjectServiceContext<F> context, ManagedObject managedObject) {
		return (runnable) -> this.trigger.trigger(context, managedObject, runnable);
	}

	/**
	 * {@link FunctionalInterface} to trigger the {@link ProcessState}.
	 */
	@FunctionalInterface
	private static interface TriggerProcess<F extends Enum<F>> {

		/**
		 * Triggers for the {@link Runnable} to be executed.
		 * 
		 * @param executeContext {@link ManagedObjectServiceContext}.
		 * @param managedObject  {@link ManagedObject} for all the invoked
		 *                       {@link ProcessState}.
		 * @param runnable       {@link Runnable} to be executed.
		 */
		void trigger(ManagedObjectServiceContext<F> executeContext, ManagedObject managedObject, Runnable runnable);
	}

}
