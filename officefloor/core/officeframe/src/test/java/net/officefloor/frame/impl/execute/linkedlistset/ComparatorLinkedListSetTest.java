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