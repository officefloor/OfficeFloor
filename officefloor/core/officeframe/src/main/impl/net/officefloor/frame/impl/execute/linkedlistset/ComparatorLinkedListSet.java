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
package net.officefloor.frame.impl.execute.linkedlistset;

import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;

/**
 * <p>
 * {@link LinkedListSet} that compares {@link LinkedListSetEntry} instances
 * before adding as {@link LinkedListSetEntry} may hold content that should not
 * be re-added to the {@link LinkedListSet}.
 * <p>
 * Otherwise it ensure strictness of adding/removing {@link LinkedListSetEntry}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ComparatorLinkedListSet<E extends LinkedListSetEntry<E, O>, O>
		extends StrictLinkedListSet<E, O> {

	/**
	 * Invoked to determine if the two entries are equal.
	 * 
	 * @param entryA
	 *            First entry.
	 * @param entryB
	 *            Second entry.
	 * @return <code>true</code> if <code>entryA</code> is equal to
	 *         <code>entryB</code>, otherwise <code>false</code> to indicate not
	 *         equal.
	 */
	protected abstract boolean isEqual(E entryA, E entryB);

	/*
	 * ==================== LinkedListSet ================================
	 */

	@Override
	public void addEntry(E entry) {

		// Determine if the entry already exists in the list
		E current = this.getHead();
		while (current != null) {
			if (this.isEqual(current, entry)) {
				// Already in list
				return;
			}
			current = current.getNext();
		}

		// Not in list, so add the entry
		super.addEntry(entry);
	}

}