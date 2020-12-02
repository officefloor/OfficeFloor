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

package net.officefloor.frame.impl.execute.managedfunction;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedFunction} implementation of a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLogicImpl<O extends Enum<O>, F extends Enum<F>> implements ManagedFunctionLogic {

	/**
	 * <p>
	 * Scope index of the {@link ManagedObjectIndex} indicating the parameter rather
	 * than the object of the {@link ManagedObject}.
	 * <p>
	 * As {@link ManagedObjectIndex} are indexes into arrays, the negative value is
	 * safe to use as a parameter index.
	 */
	public static final int PARAMETER_INDEX = -1;

	/**
	 * {@link ManagedObjectIndex} for the parameter.
	 */
	public static final ManagedObjectIndex PARAMETER_MANAGED_OBJECT_INDEX = new ManagedObjectIndexImpl(
			ManagedObjectScope.FUNCTION, PARAMETER_INDEX);

	/**
	 * {@link ManagedFunctionMetaData} for the {@link ManagedFunction}.
	 */
	private final ManagedFunctionMetaData<O, F> functionMetaData;

	/**
	 * Parameter for the {@link ManagedFunction}.
	 */
	private final Object parameter;

	/**
	 * Initiate.
	 * 
	 * @param functionMetaData {@link ManagedFunctionMetaData}.
	 * @param parameter        Parameter for the {@link ManagedFunction}.
	 */
	public ManagedFunctionLogicImpl(ManagedFunctionMetaData<O, F> functionMetaData, Object parameter) {
		this.parameter = parameter;
		this.functionMetaData = functionMetaData;
	}

	/*
	 * ====================== ManagedFunctionLogic ==========================
	 */

	@Override
	public void execute(ManagedFunctionLogicContext context, ThreadState threadState) throws Throwable {

		// Create the manage function
		ManagedFunction<O, F> function = functionMetaData.getManagedFunctionFactory().createManagedFunction();

		// Execute the managed function
		ManagedFunctionContextToken token = new ManagedFunctionContextToken(context, threadState);
		function.execute(token);
	}

	/**
	 * <p>
	 * Token class given to the {@link ManagedFunction}.
	 * <p>
	 * As application code will be provided a {@link ManagedFunctionContext} this
	 * exposes just the necessary functionality and prevents access to internals of
	 * the framework.
	 */
	private final class ManagedFunctionContextToken implements ManagedFunctionContext<O, F> {

		/**
		 * {@link ManagedFunctionLogicContext}.
		 */
		private final ManagedFunctionLogicContext context;

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * Instantiate.
		 * 
		 * @param context     {@link ManagedFunctionLogicContext}.
		 * @param threadState {@link ThreadState}.
		 */
		private ManagedFunctionContextToken(ManagedFunctionLogicContext context, ThreadState threadState) {
			this.context = context;
			this.threadState = threadState;
		}

		/*
		 * ====================== ManagedFunctionContext ======================
		 */

		@Override
		public Logger getLogger() {
			return ManagedFunctionLogicImpl.this.functionMetaData.getLogger();
		}

		@Override
		public Object getObject(O key) {
			return this.getObject(key.ordinal());
		}

		@Override
		public Object getObject(int managedObjectIndex) {

			// Obtain the managed object index
			ManagedObjectIndex index = ManagedFunctionLogicImpl.this.functionMetaData
					.getManagedObject(managedObjectIndex);

			// Determine if a parameter
			if (index.getIndexOfManagedObjectWithinScope() == PARAMETER_INDEX) {
				return ManagedFunctionLogicImpl.this.parameter;
			}

			// Return the Object
			return this.context.getObject(index);
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			// Obtain the Flow meta-data and do the flow
			FlowMetaData flowMetaData = ManagedFunctionLogicImpl.this.functionMetaData.getFlow(flowIndex);
			this.context.doFlow(flowMetaData, parameter, callback);
		}

		@Override
		public void doFlow(String functionName, Object parameter, FlowCallback callback)
				throws UnknownFunctionException, InvalidParameterTypeException {

			// Obtain the function meta-data
			ManagedFunctionLocator functionLocator = ManagedFunctionLogicImpl.this.functionMetaData.getOfficeMetaData()
					.getManagedFunctionLocator();
			final ManagedFunctionMetaData<?, ?> functionMetaData = functionLocator
					.getManagedFunctionMetaData(functionName);
			if (functionMetaData == null) {
				throw new UnknownFunctionException(functionName);
			}

			// Create dynamic flow meta-data
			FlowMetaData dynamicFlowMetaData = new FlowMetaData() {
				@Override
				public ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData() {
					return functionMetaData;
				}

				@Override
				public boolean isSpawnThreadState() {
					// Dynamic flows are not spawned in thread states
					return false;
				}
			};

			// Invoke the Flow
			this.context.doFlow(dynamicFlowMetaData, parameter, callback);
		}

		@Override
		public AsynchronousFlow createAsynchronousFlow() {
			return this.context.createAsynchronousFlow();
		}

		@Override
		public Executor getExecutor() {
			return this.threadState.getProcessState().getExecutor();
		}

		@Override
		public void setNextFunctionArgument(Object argument) {
			this.context.setNextFunctionArgument(argument);
		}
	}

}
