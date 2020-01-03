/*-
 * #%L
 * Objectify Persistence
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
