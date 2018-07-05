/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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