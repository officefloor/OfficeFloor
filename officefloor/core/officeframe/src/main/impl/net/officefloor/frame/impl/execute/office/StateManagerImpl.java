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

package net.officefloor.frame.impl.execute.office;

import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.office.LoadManagedObjectFunctionFactory.LoadManagedObjectParameter;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link StateManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StateManagerImpl implements StateManager {

	/**
	 * Load object {@link ManagedFunctionMetaData} instances by the loading object
	 * bound name.
	 */
	private final Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas;

	/**
	 * {@link ThreadState} for context of the {@link ManagedObject} instances.
	 */
	private final ThreadState threadState;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * Executes the {@link FunctionState} instances.
	 */
	private final Consumer<FunctionState> functionExecutor;

	/**
	 * Cleans up state on closing {@link StateManager}.
	 */
	private final Runnable cleanUpState;

	/**
	 * Instantiate.
	 * 
	 * @param loadObjectMetaDatas Load object {@link ManagedFunctionMetaData}
	 *                            instances by the loading object bound name.
	 * @param threadState         {@link ThreadState} for context of the
	 *                            {@link ManagedObject} instances.
	 * @param monitorClock        {@link MonitorClock}.
	 * @param functionExecutor    Executes the {@link FunctionState} instances.
	 * @param cleanUpState        Cleans up state on closing {@link StateManager}.
	 */
	public StateManagerImpl(Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas, ThreadState threadState,
			MonitorClock monitorClock, Consumer<FunctionState> functionExecutor, Runnable cleanUpState) {
		this.loadObjectMetaDatas = loadObjectMetaDatas;
		this.threadState = threadState;
		this.monitorClock = monitorClock;
		this.functionExecutor = functionExecutor;
		this.cleanUpState = cleanUpState;
	}

	/*
	 * ===================== StateManager =========================
	 */

	@Override
	public <O> void load(String boundObjectName, ObjectUser<O> user) throws UnknownObjectException {

		// Obtain the function meta-data to load the bound object
		ManagedFunctionMetaData<?, ?> loadMetaData = this.loadObjectMetaDatas.get(boundObjectName);
		if (loadMetaData == null) {
			throw new UnknownObjectException(boundObjectName);
		}

		// Create the load completion to capture object (or possible failure)
		LoadObjectCompletion<?> loadCompletion = new LoadObjectCompletion<>(user);

		// Create flow to load object
		Flow flow = this.threadState.createFlow(loadCompletion, null);

		// Create the managed object to load the object
		FunctionState loader = flow.createManagedFunction(loadCompletion, loadMetaData, true, null);

		// Undertake function to load the object
		this.functionExecutor.accept(loader);
	}

	@Override
	public <O> O getObject(String boundObjectName, long timeoutInMilliseconds) throws Throwable {

		// Obtain the object
		ObjectUserImpl<O> user = new ObjectUserImpl<O>(boundObjectName, this.monitorClock);
		this.load(boundObjectName, user);

		// Return the loaded object (or fail)
		return user.getObject(timeoutInMilliseconds);
	}

	@Override
	public void close() throws Exception {
		this.cleanUpState.run();
	}

	/**
	 * Captures the result of loading an object.
	 */
	private static class LoadObjectCompletion<O>
			extends AbstractLinkedListSetEntry<FlowCompletion, ManagedFunctionContainer>
			implements LoadManagedObjectParameter, FlowCompletion {

		/**
		 * {@link ObjectUser}.
		 */
		private final ObjectUser<O> objectUser;

		/**
		 * Instantiate.
		 * 
		 * @param objectUser {@link ObjectUser}.
		 */
		private LoadObjectCompletion(ObjectUser<O> objectUser) {
			this.objectUser = objectUser;
		}

		/*
		 * =================== LoadManagedObjectParameter ======================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void load(Object object) {
			this.objectUser.use((O) object, null);
		}

		/*
		 * ==================== FlowCompletion =================================
		 */

		@Override
		public ManagedFunctionContainer getLinkedListSetOwner() {
			throw new IllegalStateException("Should not require " + ManagedFunctionContainer.class.getSimpleName()
					+ " for " + StateManager.class.getSimpleName());
		}

		@Override
		public FunctionState flowComplete(Throwable escalation) {

			// Handle the escalation
			if (escalation != null) {
				this.objectUser.use(null, escalation);
			}

			// Nothing further
			return null;
		}
	}

}
