/*-
 * #%L
 * Objectify Persistence
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

package net.officefloor.nosql.objectify;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Deque;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * {@link ThreadSynchroniserFactory} that auto-registers itself with the
 * {@link OfficeArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyThreadSynchroniserFactory implements ThreadSynchroniserFactory {

	/**
	 * Obtains the {@link Objectify} stack from the {@link ObjectifyFactory}.
	 * 
	 * @param objectifyFactory {@link ObjectifyFactory}.
	 * @return {@link Objectify} stack.
	 */
	@SuppressWarnings("unchecked")
	public static Deque<Objectify> getStack(ObjectifyFactory objectifyFactory) {
		try {
			return ((ThreadLocal<Deque<Objectify>>) stacks.get(objectifyFactory)).get();
		} catch (Exception ex) {
			// Should not occur, as reflection on field
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Stacks {@link Field} in the {@link ObjectifyFactory} holding
	 * {@link ThreadLocal} state of transactions.
	 */
	private static final Field stacks;

	static {
		try {
			stacks = ObjectifyFactory.class.getDeclaredField("stacks");
			stacks.setAccessible(true);
		} catch (Exception ex) {
			// Should not occur
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * {@link ObjectifyFactory}.
	 */
	private final ObjectifyFactory objectifyFactory;

	/**
	 * Instantiate.
	 * 
	 * @param objectifyFactory {@link ObjectifyFactory}.
	 * @throws Exception If fails to access {@link ObjectifyFactory} for
	 *                   {@link ThreadLocal} details.
	 */
	public ObjectifyThreadSynchroniserFactory(ObjectifyFactory objectifyFactory) {
		this.objectifyFactory = objectifyFactory;
	}

	/*
	 * ================= ThreadSynchroniserFactory ==================
	 */

	@Override
	public ThreadSynchroniser createThreadSynchroniser() {
		return new ObjectifyThreadSynchroniser();
	}

	/**
	 * {@link Objectify} {@link ThreadSynchroniser}.
	 */
	private class ObjectifyThreadSynchroniser implements ThreadSynchroniser {

		/**
		 * {@link Objectify} suspended stack.
		 */
		private Objectify[] stack;

		/*
		 * =================== ThreadSynchroniser ====================
		 */

		@Override
		public void suspendThread() {

			// Obtain the stacks
			Deque<Objectify> stacks = getStack(ObjectifyThreadSynchroniserFactory.this.objectifyFactory);

			// Copy out the stack
			this.stack = stacks.toArray(new Objectify[stacks.size()]);

			// Clear current thread stack
			stacks.clear();
		}

		@Override
		public void resumeThread() {

			// Obtain the stacks
			Deque<Objectify> stacks = getStack(ObjectifyThreadSynchroniserFactory.this.objectifyFactory);

			// Load in the stacks
			stacks.addAll(Arrays.asList(this.stack));
		}
	}

}
