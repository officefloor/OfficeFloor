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
package net.officefloor.frame.impl.execute.linkedlist;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.impl.execute.AbstractLinkedList;
import net.officefloor.frame.impl.execute.AbstractLinkedListEntry;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.LinkedListEntry;
import net.officefloor.frame.internal.structure.LinkedListItem;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AbstractLinkedList} and the {@link AbstractLinkedListEntry}.
 * 
 * @author Daniel
 */
public class AbstractLinkedListTest extends OfficeFrameTestCase {

	/**
	 * {@link AbstractLinkedList} to test.
	 */
	protected final LinkedList<TestLinkedListEntry, Object> linkedList = new AbstractLinkedList<TestLinkedListEntry, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			AbstractLinkedListTest.this.isLastEntryRemoved = true;
			if (removeParameter != null) {
				AbstractLinkedListTest.this.removeParameters
						.add(removeParameter);
			}
		}
	};

	/**
	 * Indicates if the last {@link LinkedListEntry} was removed.
	 */
	protected boolean isLastEntryRemoved = false;

	/**
	 * Remove parameters on removing the last entry.
	 */
	protected List<Object> removeParameters = new ArrayList<Object>(1);

	/**
	 * Flag indicating if expecting the last entry to be removed.
	 */
	protected boolean expectLastEntryRemoved = true;

	/**
	 * Current unique Id for a {@link TestLinkedListEntry}.
	 */
	protected int currentEntryId = 1;

	/**
	 * {@link AbstractLinkedListEntry} for testing.
	 */
	protected class TestLinkedListEntry extends
			AbstractLinkedListEntry<TestLinkedListEntry, Object> {

		/**
		 * Unique Id for this entry.
		 */
		protected final int id;

		// Inherit constructor
		public TestLinkedListEntry(
				LinkedList<TestLinkedListEntry, Object> linkedList) {
			super(linkedList);

			// Assign Id
			this.id = currentEntryId++;

			// Automatically add entry to list
			linkedList.addLinkedListEntry(this);
		}

		/**
		 * Obtains the Id for this entry.
		 * 
		 * @return Id for this entry.
		 */
		public int getId() {
			return this.id;
		}
	}

	/**
	 * Validates the {@link LinkedList}.
	 * 
	 * @param entries
	 *            Entries, in order, expected in the {@link LinkedList}.
	 */
	protected void validateList(TestLinkedListEntry... entries) {

		// Determine if expecting empty list
		if (entries.length == 0) {
			// Validate empty list
			assertNull("No head for empty list", this.linkedList.getHead());
			assertNull("No tail for empty list", this.linkedList.getTail());

			// Validate last removed invoked
			assertEquals("lastLinkedListEntryRemoved not invoked",
					this.expectLastEntryRemoved, this.isLastEntryRemoved);

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
		for (TestLinkedListEntry entry : entries) {
			nextExpected.append(entry.getId() + " ");
		}

		// Create next actual listing
		StringBuilder nextActual = new StringBuilder();
		TestLinkedListEntry current = this.linkedList.getHead();
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
	 * Validates the remove parameters.
	 * 
	 * @param expectedRemoveParameters
	 *            Expected remove parameters.
	 */
	protected void validateRemoveParameters(Object... expectedRemoveParameters) {
		// Ensure correct number
		assertEquals("Incorrect number of remove parameters",
				expectedRemoveParameters.length, this.removeParameters.size());
		for (int i = 0; i < expectedRemoveParameters.length; i++) {
			assertEquals("Incorrect remove parameter " + i,
					expectedRemoveParameters[i], this.removeParameters.get(i));
		}
	}

	/**
	 * Ensure correctly adds an entry.
	 */
	public void testAddEntry() {
		TestLinkedListEntry entry = new TestLinkedListEntry(this.linkedList);
		this.validateList(entry);
	}

	/**
	 * Ensure empty list after adding then removing entry.
	 */
	public void testAddRemoveEntry() {
		TestLinkedListEntry entry = new TestLinkedListEntry(this.linkedList);
		entry.removeFromLinkedList("remove");
		this.validateList();
		this.validateRemoveParameters("remove");
	}

	/**
	 * Ensure remove first entry of many entries.
	 */
	public void testRemoveFirst() {
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry middle = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry last = new TestLinkedListEntry(this.linkedList);
		first.removeFromLinkedList("not last");
		this.validateList(middle, last);
		this.validateRemoveParameters();
	}

	/**
	 * Ensure remove middle entry of many entries.
	 */
	public void testRemoveMiddle() {
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry middle = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry last = new TestLinkedListEntry(this.linkedList);
		middle.removeFromLinkedList("not last");
		this.validateList(first, last);
		this.validateRemoveParameters();
	}

	/**
	 * Ensure remove last entry of many entries.
	 */
	public void testRemoveLast() {
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry middle = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry last = new TestLinkedListEntry(this.linkedList);
		last.removeFromLinkedList("not last");
		this.validateList(first, middle);
		this.validateRemoveParameters();
	}

	/**
	 * Ensure empty list after adding then removing multiple entries.
	 */
	public void testAddRemoveMultipleEntry() {
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry middle = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry last = new TestLinkedListEntry(this.linkedList);
		this.validateList(first, middle, last);
		last.removeFromLinkedList("not last");
		this.validateList(first, middle);
		this.validateRemoveParameters();
		TestLinkedListEntry extra = new TestLinkedListEntry(this.linkedList);
		this.validateList(first, middle, extra);
		middle.removeFromLinkedList("not last");
		this.validateList(first, extra);
		this.validateRemoveParameters();
		extra.removeFromLinkedList("not last");
		this.validateList(first);
		this.validateRemoveParameters();
		first.removeFromLinkedList("last");
		this.validateList();
		this.validateRemoveParameters("last");
	}

	/**
	 * Ensure able to reuse.
	 */
	public void testAddRemoveAddRemove() {
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		this.validateList(first);
		first.removeFromLinkedList("one");
		this.validateList();
		this.validateRemoveParameters("one");
		TestLinkedListEntry second = new TestLinkedListEntry(this.linkedList);
		this.validateList(second);
		// Ensure last entry removed called again
		this.isLastEntryRemoved = false;
		second.removeFromLinkedList("two");
		this.validateList();
		this.validateRemoveParameters("one", "two");
	}

	/**
	 * Validate purge empty list.
	 */
	public void testPurgeEmptyList() {
		// Purge the empty list expecting not return
		assertNull("Should not return head of empty list", this.linkedList
				.purgeLinkedList("not removed"));

		// Last entry removed should not be invoked
		this.expectLastEntryRemoved = false;
		this.validateList();
		this.validateRemoveParameters();
	}

	/**
	 * Validate purge empty list.
	 */
	public void testPurgeList() {
		// Load up the list
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry second = new TestLinkedListEntry(this.linkedList);
		this.validateList(first, second);

		// Purge the list
		TestLinkedListEntry head = this.linkedList.purgeLinkedList("removed");
		this.validateList();
		this.validateRemoveParameters("removed");

		// Validate the returned list
		assertEquals("Incorrect head of returned list", head, first);
		assertEquals("Incorrect returned second list entry", head.getNext(),
				second);
	}

	/**
	 * Validate copying empty list.
	 */
	public void testCopyEmptyList() {
		// Copy the list (not expected last removed)
		LinkedListItem<TestLinkedListEntry> head = this.linkedList
				.copyLinkedList();
		this.expectLastEntryRemoved = false;
		this.validateList();

		// Validate no items were returned
		assertNull("No items should be returned on copying an empty list", head);
	}

	/**
	 * Validates copying the list.
	 */
	public void testCopyList() {
		// Load up the list
		TestLinkedListEntry first = new TestLinkedListEntry(this.linkedList);
		TestLinkedListEntry second = new TestLinkedListEntry(this.linkedList);
		this.validateList(first, second);

		// Copy the list
		LinkedListItem<TestLinkedListEntry> head = this.linkedList
				.copyLinkedList();
		this.validateList(first, second);

		// Validate the returned list
		assertEquals("Head incorrect entry", first, head.getEntry());
		assertEquals("Second list item incorrect", second, head.getNext()
				.getEntry());
		assertNull("Should be no third item", head.getNext().getNext());
	}

}
