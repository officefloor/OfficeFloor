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
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link StrictLinkedListSet} and the
 * {@link AbstractLinkedListSetEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public class StrictLinkedListSetTest extends OfficeFrameTestCase {

	/**
	 * {@link StrictLinkedListSet} to test.
	 */
	protected final LinkedListSet<Entry, StrictLinkedListSetTest> linkedList = this
			.createLinkedListSet(this);

	/**
	 * Allow for testing {@link ComparatorLinkedListSet} for strictness.
	 * 
	 * @param owner
	 *            Owner.
	 * @return {@link LinkedListSet} to validate for strictness.
	 */
	protected LinkedListSet<Entry, StrictLinkedListSetTest> createLinkedListSet(
			final StrictLinkedListSetTest owner) {
		return new StrictLinkedListSet<Entry, StrictLinkedListSetTest>() {
			@Override
			protected StrictLinkedListSetTest getOwner() {
				return owner;
			}
		};
	}

	/**
	 * Ensure correctly adds an entry.
	 */
	public void testAddEntry() {
		Entry entry = new Entry();
		this.validateList(entry);
	}

	/**
	 * Ensure failure if entry added with wrong owner.
	 */
	public void testAddEntryWithWrongOwner() {
		StrictLinkedListSetTest anotherOwner = new StrictLinkedListSetTest();
		Entry notOwnedEntry = new Entry(anotherOwner);
		try {
			this.linkedList.addEntry(notOwnedEntry);
			fail("Should fail to add a non-owned entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Invalid LinkedListSet owner (entry owner=" + anotherOwner
							+ ", list owner=" + this + ", entry="
							+ notOwnedEntry + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if only entry added again.
	 */
	public void testFailIfAddOnlyEntryAgain() {
		Entry entry = new Entry(); // added first time
		try {
			this.linkedList.addEntry(entry);
			fail("Should not be able to re-add an entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry already added (entry=" + entry + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if first entry of list added again.
	 */
	public void testFailIfAddFirstEntryAgain() {
		Entry first = new Entry(); // added first time
		new Entry();
		new Entry();
		try {
			this.linkedList.addEntry(first);
			fail("Should not be able to re-add an entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry already added (entry=" + first + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if entry in middle of list added again.
	 */
	public void testFailIfAddMiddleEntryAgain() {
		new Entry();
		Entry middle = new Entry(); // added first time
		new Entry();
		try {
			this.linkedList.addEntry(middle);
			fail("Should not be able to re-add an entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry already added (entry=" + middle + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if last entry of list added again.
	 */
	public void testFailIfAddLastEntryAgain() {
		new Entry();
		new Entry();
		Entry last = new Entry(); // added first time
		try {
			this.linkedList.addEntry(last);
			fail("Should not be able to re-add an entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry already added (entry=" + last + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if entry removed with wrong owner.
	 */
	public void testRemoveEntryWithWrongOwner() {
		StrictLinkedListSetTest anotherOwner = new StrictLinkedListSetTest();
		Entry notOwnedEntry = new Entry(anotherOwner);
		try {
			this.linkedList.removeEntry(notOwnedEntry);
			fail("Should fail to remove a non-owned entry");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Invalid LinkedListSet owner (entry owner=" + anotherOwner
							+ ", list owner=" + this + ", entry="
							+ notOwnedEntry + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if attempting remove entry from empty list.
	 */
	public void testFailIfRemoveFromEmptyList() {
		Entry entry = new Entry(this); // (not automatically add)
		try {
			this.linkedList.removeEntry(entry);
			fail("Should fail if attempt to remove entry not in list");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry not in list (entry=" + entry + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure failure if attempting remove entry from empty list.
	 */
	public void testFailIfRemoveWhenNotInPopulatedList() {

		// Populate the list
		new Entry();
		new Entry();
		new Entry();

		Entry notInList = new Entry(this); // (not automatically add)
		try {
			this.linkedList.removeEntry(notInList);
			fail("Should fail if attempt to remove entry not in list");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect failure details",
					"Entry not in list (entry=" + notInList + ", list="
							+ this.linkedList + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure empty list after adding then removing entry.
	 */
	public void testAddRemoveEntry() {
		Entry entry = new Entry();
		this.assertLast(this.linkedList.removeEntry(entry));
		this.validateList();

		// Ensure state of entry is reset
		assertNull("Entry next should be cleared", entry.getNext());
		assertNull("Entry prev should be cleared", entry.getPrev());
	}

	/**
	 * Ensure that can add the last entry, remove it and add it again.
	 */
	public void testLastEntryAddRemoveAndAddAgain() {
		new Entry();
		new Entry();
		Entry last = new Entry();
		this.assertNotLast(this.linkedList.removeEntry(last));
		this.linkedList.addEntry(last); // add again
	}

	/**
	 * Ensure remove first entry of many entries.
	 */
	public void testRemoveFirst() {
		Entry first = new Entry();
		Entry middle = new Entry();
		Entry last = new Entry();
		this.assertNotLast(this.linkedList.removeEntry(first));
		this.validateList(middle, last);

		// Ensure state of entry is reset
		assertNull("Entry next should be cleared", first.getNext());
		assertNull("Entry prev should be cleared", first.getPrev());
	}

	/**
	 * Ensure remove middle entry of many entries.
	 */
	public void testRemoveMiddle() {
		Entry first = new Entry();
		Entry middle = new Entry();
		Entry last = new Entry();
		this.assertNotLast(this.linkedList.removeEntry(middle));
		this.validateList(first, last);

		// Ensure state of entry is reset
		assertNull("Entry next should be cleared", middle.getNext());
		assertNull("Entry prev should be cleared", middle.getPrev());
	}

	/**
	 * Ensure remove last entry of many entries.
	 */
	public void testRemoveLast() {
		Entry first = new Entry();
		Entry middle = new Entry();
		Entry last = new Entry();
		this.assertNotLast(this.linkedList.removeEntry(last));
		this.validateList(first, middle);

		// Ensure state of entry is reset
		assertNull("Entry next should be cleared", last.getNext());
		assertNull("Entry prev should be cleared", last.getPrev());
	}

	/**
	 * Ensure empty list after adding then removing multiple entries.
	 */
	public void testAddRemoveMultipleEntries() {
		Entry first = new Entry();
		Entry middle = new Entry();
		Entry last = new Entry();
		this.validateList(first, middle, last);
		this.assertNotLast(this.linkedList.removeEntry(last));
		this.validateList(first, middle);
		Entry extra = new Entry();
		this.validateList(first, middle, extra);
		this.assertNotLast(this.linkedList.removeEntry(middle));
		this.validateList(first, extra);
		this.assertNotLast(this.linkedList.removeEntry(extra));
		this.validateList(first);
		this.assertLast(this.linkedList.removeEntry(first));
		this.validateList();
	}

	/**
	 * Ensure able to reuse.
	 */
	public void testAddRemoveAddRemove() {
		Entry first = new Entry();
		this.validateList(first);
		this.assertLast(this.linkedList.removeEntry(first));
		this.validateList();
		Entry second = new Entry();
		this.validateList(second);
		this.assertLast(this.linkedList.removeEntry(second));
		this.validateList();
	}

	/**
	 * Validate purge empty list.
	 */
	public void testPurgeEmptyList() {
		// Purge the empty list expecting not return
		assertNull("Should not return head of empty list", this.linkedList
				.purgeEntries());
		this.validateList();
	}

	/**
	 * Validate purge empty list.
	 */
	public void testPurgeList() {
		// Load up the list
		Entry first = new Entry();
		Entry second = new Entry();
		this.validateList(first, second);

		// Purge the list
		Entry head = this.linkedList.purgeEntries();
		this.validateList();

		// Validate the returned list
		assertEquals("Incorrect head of returned list", head, first);
		assertEquals("Incorrect returned second list entry", head.getNext(),
				second);
		assertNull("Should be no third entry", head.getNext().getNext());
	}

	/**
	 * Validate copying empty list.
	 */
	public void testCopyEmptyList() {
		// Copy the empty list
		LinkedListSetItem<Entry> head = this.linkedList.copyEntries();
		this.validateList();

		// Validate no items were returned
		assertNull("No items should be returned on copying an empty list", head);
	}

	/**
	 * Validates copying the list.
	 */
	public void testCopyList() {
		// Load up the list
		Entry first = new Entry();
		Entry second = new Entry();
		this.validateList(first, second);

		// Copy the list
		LinkedListSetItem<Entry> head = this.linkedList.copyEntries();
		this.validateList(first, second);

		// Validate the returned list
		assertEquals("Head incorrect entry", first, head.getEntry());
		assertEquals("Second list item incorrect", second, head.getNext()
				.getEntry());
		assertNull("Should be no third item", head.getNext().getNext());
	}

	/**
	 * Current unique Id for a {@link Entry}.
	 */
	private int currentEntryId = 1;

	/**
	 * {@link AbstractLinkedListSetEntry} for testing.
	 */
	protected class Entry extends
			AbstractLinkedListSetEntry<Entry, StrictLinkedListSetTest> {

		/**
		 * Unique Id for this entry.
		 */
		protected final int id;

		/**
		 * Owner.
		 */
		private StrictLinkedListSetTest owner;

		/**
		 * Initiate.
		 * 
		 * @param owner
		 *            Owner.
		 */
		public Entry(StrictLinkedListSetTest owner) {
			this.owner = owner;
			this.id = currentEntryId++; // unique id
		}

		/**
		 * Initiate with default owner and automatically add as entry.
		 */
		public Entry() {
			this(StrictLinkedListSetTest.this);

			// Automatically add entry
			StrictLinkedListSetTest.this.linkedList.addEntry(this);
		}

		/**
		 * Obtains the Id for this entry.
		 * 
		 * @return Id for this entry.
		 */
		public int getId() {
			return this.id;
		}

		/*
		 * ================ LinkedListSetEntry ==============================
		 */

		@Override
		public StrictLinkedListSetTest getLinkedListSetOwner() {
			return this.owner;
		}
	}

	/**
	 * Validates the {@link LinkedListSet}.
	 * 
	 * @param entries
	 *            Entries, in order, expected in the {@link LinkedListSet}.
	 */
	protected void validateList(Entry... entries) {

		// Determine if expecting empty list
		if (entries.length == 0) {
			// Validate empty list
			assertNull("No head for empty list", this.linkedList.getHead());
			assertNull("No tail for empty list", this.linkedList.getTail());

			// Validated
			return;
		}

		// Validate head/tail of linked list
		assertSame("Incorrect head", entries[0], this.linkedList.getHead());
		assertNull("Head must not have prev", this.linkedList.getHead()
				.getPrev());
		assertSame("Incorrect tail", entries[(entries.length - 1)],
				this.linkedList.getTail());
		assertNull("Tail must not have next", this.linkedList.getTail()
				.getNext());

		// Create next expected listing
		StringBuilder nextExpected = new StringBuilder();
		for (Entry entry : entries) {
			nextExpected.append(entry.getId() + " ");
		}

		// Create next actual listing
		StringBuilder nextActual = new StringBuilder();
		Entry current = this.linkedList.getHead();
		while (current != null) {
			nextActual.append(current.getId() + " ");
			current = current.getNext();
		}

		// Validate next listing
		assertEquals("Incorrect next entries [expected "
				+ nextExpected.toString() + " actual " + nextActual.toString()
				+ "]", nextExpected.toString(), nextActual.toString());

		// Create prev expected listing
		StringBuilder prevExpected = new StringBuilder();
		for (int i = (entries.length - 1); i >= 0; i--) {
			prevExpected.append(entries[i].getId() + " ");
		}

		// Create prev actual listing
		StringBuilder prevActual = new StringBuilder();
		current = this.linkedList.getTail();
		while (current != null) {
			prevActual.append(current.getId() + " ");
			current = current.getPrev();
		}

		// Validate prev listing
		assertEquals("Incorrect prev entries [expected "
				+ prevExpected.toString() + " actual " + prevActual.toString()
				+ "]", prevExpected.toString(), prevActual.toString());
	}

	/**
	 * Ensures the return of remove indicates last {@link LinkedListSetEntry}
	 * was removed.
	 * 
	 * @param isLast
	 *            Return from remove.
	 */
	protected void assertLast(boolean isLast) {
		assertTrue("Expected to have removed last entry", isLast);
	}

	/**
	 * Ensures the return of remove indicates the {@link LinkedListSetEntry}
	 * removed was not the last.
	 * 
	 * @param isLast
	 *            Return from remove.
	 */
	protected void assertNotLast(boolean isLast) {
		assertFalse("Expected not to be last entry removed", isLast);
	}

}
