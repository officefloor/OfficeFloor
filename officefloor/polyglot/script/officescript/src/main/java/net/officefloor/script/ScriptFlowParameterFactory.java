/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link ScriptFlow} {@link ClassDependencyFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptFlowParameterFactory implements ClassDependencyFactory {

	/**
	 * Index of the {@link Flow}.
	 */
	private final int flowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param flowIndex Index of the {@link Flow}.
	 */
	public ScriptFlowParameterFactory(int flowIndex) {
		this.flowIndex = flowIndex;
	}

	/*
	 * ====================== ClassDependencyFactory ======================
	 */

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return new ScriptFlowImpl(context);
	}

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {
		throw new IllegalStateException(
				"Should not use " + ScriptFlow.class.getSimpleName() + " for " + ManagedObject.class.getSimpleName());
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		throw new IllegalStateException(
				"Should not use " + ScriptFlow.class.getSimpleName() + " for " + Administration.class.getSimpleName());
	}

	/**
	 * {@link ScriptFlow} implementation.
	 */
	private class ScriptFlowImpl implements ScriptFlow {

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<?, ?> context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ManagedFunctionContext}.
		 */
		private ScriptFlowImpl(ManagedFunctionContext<?, ?> context) {
			this.context = context;
		}

		/*
		 * ====================== ScriptFlow ================================
		 */

		@Override
		public void doFlow(Object parameter, FlowCallback callback) {
			this.context.doFlow(ScriptFlowParameterFactory.this.flowIndex, parameter, callback);
		}
	}

}
