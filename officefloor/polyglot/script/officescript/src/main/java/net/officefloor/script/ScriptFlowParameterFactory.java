/*-
 * #%L
 * PolyglotScript
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
