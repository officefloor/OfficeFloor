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
