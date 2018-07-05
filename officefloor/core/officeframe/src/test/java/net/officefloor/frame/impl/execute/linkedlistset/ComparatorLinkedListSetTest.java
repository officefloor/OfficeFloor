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

/**
 * Tests the {@link ComparatorLinkedListSet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ComparatorLinkedListSetTest extends StrictLinkedListSetTest {

	/**
	 * First entry.
	 */
	private Entry first = null;

	/**
	 * Second entry.
	 */
	private Entry second = null;

	/*
	 * ==================== StrictLinkedListSetTest ==========================
	 */

	@Override
	protected LinkedListSet<Entry, StrictLinkedListSetTest> createLinkedListSet(
			final StrictLinkedListSetTest owner) {
		return new ComparatorLinkedListSet<Entry, StrictLinkedListSetTest>() {
			@Override
			protected StrictLinkedListSetTest getOwner() {
				return owner;
			}

			@Override
			protected boolean isEqual(Entry entryA, Entry entryB) {
				// Equal if match specified entries
				return ((entryA == ComparatorLinkedListSetTest.this.first) && (entryB == ComparatorLinkedListSetTest.this.second));
			}
		};
	}

	/**
	 * Ensure not add second entry if found to be equal.
	 */
	public void testAddEqualEntry() {

		// Create two entries (not yet added)
		this.first = new Entry(this);
		this.second = new Entry(this);

		// Add the entries (ensuring no failure)
		this.linkedList.addEntry(this.first);
		this.linkedList.addEntry(this.second);

		// Ensure only the first in the list
		this.validateList(this.first);

		// Ensure second is not linked in
		assertNull("Second next", this.second.getNext());
		assertNull("Second prev", this.second.getPrev());
	}

}