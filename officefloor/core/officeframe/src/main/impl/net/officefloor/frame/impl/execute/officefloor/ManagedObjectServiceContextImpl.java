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

package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ManagedObjectServiceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectServiceContextImpl<F extends Enum<F>> implements ManagedObjectServiceContext<F> {

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * Index of the {@link ManagedObject} within the {@link ProcessState}.
	 */
	private final int processMoIndex;

	/**
	 * {@link FlowMetaData} in index order for the {@link ManagedObjectSource}.
	 */
	private final FlowMetaData[] processLinks;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} of the
	 *                              {@link ManagedObject}.
	 * @param processMoIndex        Index of the {@link ManagedObject} within the
	 *                              {@link ProcessState}.
	 * @param processLinks          {@link FlowMetaData} in index order for the
	 *                              {@link ManagedObjectSource}.
	 * @param officeMetaData        {@link OfficeMetaData} to create
	 *                              {@link ProcessState} instances.
	 */
	public ManagedObjectServiceContextImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, OfficeMetaData officeMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.officeMetaData = officeMetaData;
	}

	/**
	 * Obtains the {@link FlowMetaData}.
	 * 
	 * @param flowIndex Index of the {@link FlowMetaData}.
	 * @return {@link FlowMetaData}.
	 */
	public FlowMetaData getFlowMetaData(int flowIndex) {

		// Ensure valid flow meta-data
		if ((flowIndex < 0) || (flowIndex >= this.processLinks.length)) {
			String validIndexes = (this.processLinks.length == 0 ? " [no processes linked]"
					: " [valid only 0 to " + (this.processLinks.length - 1) + "]");
			throw new IllegalArgumentException("Invalid process index " + flowIndex + validIndexes);
		}

		// Return the flow meta-data
		return this.processLinks[flowIndex];
	}

	/**
	 * Invokes the {@link ProcessState} for the {@link FlowMetaData}.
	 * 
	 * @param flowMetaData  {@link FlowMetaData}.
	 * @param parameter     Parameter.
	 * @param managedObject {@link ManagedObject}.
	 * @param delay         Possible delay.
	 * @param callback      {@link FlowCallback}.
	 * @return {@link ProcessManager}.
	 */
	public ProcessManager invokeProcess(FlowMetaData flowMetaData, Object parameter, ManagedObject managedObject,
			long delay, FlowCallback callback) {

		// Ensure execution is managed
		Execution<RuntimeException> execution = () -> {
			try {

				// Invoke the process
				return this.officeMetaData.invokeProcess(flowMetaData, parameter, delay, callback, null, managedObject,
						this.managedObjectMetaData, this.processMoIndex);
			} catch (InvalidParameterTypeException ex) {
				// Propagate (unlikely so no need for checked exception)
				throw new IllegalArgumentException(ex);
			}
		};
		return this.officeMetaData.getManagedExecutionFactory()
				.createManagedExecution(this.officeMetaData.getExecutive(), execution).managedExecute();
	}

	/*
	 * ====================== ManagedObjectServiceContext ======================
	 */

	@Override
	public ProcessManager invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) {
		return this.invokeProcess(key.ordinal(), parameter, managedObject, delay, callback);
	}

	@Override
	public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) {

		// Obtain the flow meta-data
		FlowMetaData flowMetaData = this.getFlowMetaData(flowIndex);

		// Invoke the process
		return this.invokeProcess(flowMetaData, parameter, managedObject, delay, callback);
	}

}
