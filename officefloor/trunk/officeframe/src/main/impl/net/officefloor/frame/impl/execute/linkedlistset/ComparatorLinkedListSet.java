/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
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
 * @author Daniel
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