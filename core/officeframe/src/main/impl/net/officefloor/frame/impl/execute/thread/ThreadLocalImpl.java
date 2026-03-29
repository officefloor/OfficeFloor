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

package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.configuration.ThreadLocalConfiguration;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadStateContext;

/**
 * {@link OptionalThreadLocal} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalImpl<T> implements ThreadLocalConfiguration {

	/**
	 * Fallback {@link ThreadState}.
	 */
	private static final ThreadState fallbackThreadState = new ThreadStateImpl(
			new ThreadMetaDataImpl(new ManagedObjectMetaData[0], new GovernanceMetaData[0], Integer.MAX_VALUE,
					new ThreadSynchroniserFactory[0], null, null),
			null, null, null, null);

	/**
	 * {@link OptionalThreadLocal}.
	 */
	private final OptionalThreadLocalImpl<T> optionalThreadLocal = new OptionalThreadLocalImpl<>();

	/**
	 * Obtains the {@link OptionalThreadLocal}.
	 * 
	 * @return {@link OptionalThreadLocal}.
	 */
	public OptionalThreadLocal<T> getOptionalThreadLocal() {
		return this.optionalThreadLocal;
	}

	/*
	 * ================= OptionalThreadLocal ========================
	 */

	@Override
	public void setManagedObjectIndex(ManagedObjectIndex index) {
		this.optionalThreadLocal.managedObjectIndex = index;
	}

	/**
	 * {@link OptionalThreadLocal} implementation.
	 */
	public static class OptionalThreadLocalImpl<T> implements OptionalThreadLocal<T> {

		/**
		 * {@link ManagedObjectIndex}.
		 */
		private ManagedObjectIndex managedObjectIndex;

		/*
		 * ================= OptionalThreadLocal ========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public T get() {

			// Obtain the thread state context
			ThreadStateContext context = ThreadStateImpl.currentThreadContext(ThreadLocalImpl.fallbackThreadState);

			// Obtain the managed object container
			ManagedObjectContainer container = context.getManagedObject(this.managedObjectIndex);

			// Obtain the object (if available)
			Object object = null;
			if (container != null) {
				object = container.getOptionalObject();
			}

			// Return the possible object
			return (T) object;
		}
	}

}
