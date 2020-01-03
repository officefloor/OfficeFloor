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