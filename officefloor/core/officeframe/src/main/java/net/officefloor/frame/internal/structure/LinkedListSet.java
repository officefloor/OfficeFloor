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

package net.officefloor.frame.internal.structure;

/**
 * Linked List maintaining a unique set of {@link LinkedListSetEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSet<I extends LinkedListSetEntry<I, O>, O> {

	/**
	 * Obtains the head of this {@link LinkedListSet}.
	 * 
	 * @return Head {@link LinkedListSetEntry} of this {@link LinkedListSet}.
	 */
	I getHead();

	/**
	 * Obtains the tail of this {@link LinkedListSet}.
	 * 
	 * @return Tail of this {@link LinkedListSet}.
	 */
	I getTail();

	/**
	 * Adds a {@link LinkedListSetEntry} to this {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            {@link LinkedListSetEntry} to be added to this
	 *            {@link LinkedListSet}.
	 * @throws IllegalStateException
	 *             If {@link LinkedListSetEntry} is not valid for this
	 *             {@link LinkedListSet}.
	 */
	void addEntry(I entry) throws IllegalStateException;

	/**
	 * Removes this {@link LinkedListSetEntry} from the {@link LinkedListSet}.
	 * 
	 * @param entry
	 *            {@link LinkedListSetEntry} to be removed from this
	 *            {@link LinkedListSet}.
	 * @return <code>true</code> if the {@link LinkedListSetEntry} just removed
	 *         was the last {@link LinkedListSetEntry} in the
	 *         {@link LinkedListSet}.
	 * @throws IllegalStateException
	 *             If {@link LinkedListSetEntry} is not valid for this
	 *             {@link LinkedListSet}.
	 */
	boolean removeEntry(I entry) throws IllegalStateException;

	/**
	 * Purges all {@link LinkedListSetEntry} instances within this
	 * {@link LinkedListSet}.
	 * 
	 * @return Head of the {@link LinkedListSet} (before the purge) so that may
	 *         action the {@link LinkedListSetEntry} instances.
	 */
	I purgeEntries();

	/**
	 * <p>
	 * Creates a copy of this {@link LinkedListSet} returning the head of the
	 * copy.
	 * <p>
	 * The returned copy is {@link Thread} safe to iterate.
	 * 
	 * @return Head {@link LinkedListSetItem} of the copy.
	 */
	LinkedListSetItem<I> copyEntries();

}
