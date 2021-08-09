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

package net.officefloor.frame.api.managedobject.recycle;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Parameter to the recycle {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RecycleManagedObjectParameter<MO extends ManagedObject> {

	/**
	 * Convenience method to obtain the {@link RecycleManagedObjectParameter} from
	 * the {@link ManagedFunctionContext}.
	 * 
	 * @param <M>
	 *            {@link ManagedObject} type.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @return {@link RecycleManagedObjectParameter}.
	 */
	@SuppressWarnings("unchecked")
	static <M extends ManagedObject> RecycleManagedObjectParameter<M> getRecycleManagedObjectParameter(
			ManagedFunctionContext<?, ?> context) {
		return (RecycleManagedObjectParameter<M>) context.getObject(0);
	}

	/**
	 * Obtains the {@link ManagedObject} being recycled.
	 * 
	 * @return {@link ManagedObject} being recycled.
	 */
	MO getManagedObject();

	/**
	 * <p>
	 * Invoked at the end of recycling to re-use the {@link ManagedObject}.
	 * </p>
	 * Should this method not be invoked, the {@link ManagedObject} will be
	 * destroyed.
	 */
	void reuseManagedObject();

	/**
	 * Obtains possible {@link CleanupEscalation} instances that occurred in
	 * cleaning up previous {@link ManagedObject} instances.
	 * 
	 * @return Possible {@link CleanupEscalation} instances.
	 */
	CleanupEscalation[] getCleanupEscalations();

}
