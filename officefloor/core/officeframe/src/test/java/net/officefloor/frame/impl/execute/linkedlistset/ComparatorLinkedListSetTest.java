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
