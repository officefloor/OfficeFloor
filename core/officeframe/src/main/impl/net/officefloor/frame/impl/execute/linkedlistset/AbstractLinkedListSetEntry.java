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

import net.officefloor.frame.internal.structure.LinkedListSetEntry;

/**
 * Abstract {@link LinkedListSetEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractLinkedListSetEntry<E extends LinkedListSetEntry<E, O>, O>
		implements LinkedListSetEntry<E, O> {

	/**
	 * Previous entry in the linked list.
	 */
	private E prev = null;

	/**
	 * Next entry in the linked list.
	 */
	private E next = null;

	/*
	 * =================== LinkedListEntry =================================
	 * 
	 * Methods are final to stop accidental overriding by other linking
	 * structures.
	 */

	@Override
	public final E getPrev() {
		return this.prev;
	}

	@Override
	public final void setPrev(E entry) {
		this.prev = entry;
	}

	@Override
	public final E getNext() {
		return this.next;
	}

	@Override
	public final void setNext(E entry) {
		this.next = entry;
	}

}
