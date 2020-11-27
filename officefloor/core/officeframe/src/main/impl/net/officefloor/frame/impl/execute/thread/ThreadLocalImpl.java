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
