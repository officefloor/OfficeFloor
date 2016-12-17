/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

/**
 * Linked List maintaining a unique set of {@link LinkedListSetEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedListSet<E extends LinkedListSetEntry<E, O>, O> {

	/**
	 * Obtains the head of this {@link LinkedListSet}.
	 * 
	 * @return Head {@link LinkedListSetEntry} of this {@link LinkedListSet}.
	 */
	E getHead();

	/**
	 * Obtains the tail of this {@link LinkedListSet}.
	 * 
	 * @return Tail of this {@link LinkedListSet}.
	 */
	E getTail();

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
	void addEntry(E entry) throws IllegalStateException;

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
	boolean removeEntry(E entry) throws IllegalStateException;

	/**
	 * Purges all {@link LinkedListSetEntry} instances within this
	 * {@link LinkedListSet}.
	 * 
	 * @return Head of the {@link LinkedListSet} (before the purge) so that may
	 *         action the {@link LinkedListSetEntry} instances.
	 */
	E purgeEntries();

	/**
	 * <p>
	 * Creates a copy of this {@link LinkedListSet} returning the head of the
	 * copy.
	 * <p>
	 * The returned copy is {@link Thread} safe to iterate.
	 * 
	 * @return Head {@link LinkedListSetItem} of the copy.
	 */
	LinkedListSetItem<E> copyEntries();

}