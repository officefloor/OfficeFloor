/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.script;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link ScriptFlow} {@link MethodParameterFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptFlowParameterFactory implements MethodParameterFactory {

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
	 * ====================== ManagedFunctionParameterFactory ======================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) throws Exception {
		return new ScriptFlowImpl(context);
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