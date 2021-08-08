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

package net.officefloor.frame.impl.execute.function;

import java.util.function.Function;

import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.LinkedListSetItem;

/**
 * {@link FunctionLogic} to execute a {@link FunctionLogic} for each
 * {@link LinkedListSetEntry} from a {@link LinkedListSet}.
 *
 * @author Daniel Sagenschneider
 */
public class LinkedListSetPromise<I, E> extends AbstractDelegateFunctionState {

	/**
	 * Translates the entry to a {@link FunctionState}.
	 */
	public static interface Translate<E> {

		/**
		 * Translates the entry to a {@link FunctionState}.
		 * 
		 * @param entry Entry of {@link LinkedListSet}.
		 * @return {@link FunctionState}.
		 */
		FunctionState translate(E entry);
	}

	/**
	 * Purges the {@link LinkedListSet} and executes a {@link FunctionState} for
	 * each item.
	 * 
	 * @param               <E> {@link LinkedListSetEntry} type.
	 * @param               <O> Owner type of {@link LinkedListSetEntry}.
	 * @param linkedListSet {@link LinkedListSet}.
	 * @param translate     {@link Translate}.
	 * @return {@link FunctionState} to purse the {@link LinkedListSet}.
	 */
	public static <E extends LinkedListSetEntry<E, O>, O> FunctionState purge(LinkedListSet<E, O> linkedListSet,
			Translate<E> translate) {

		// Ensure items in the linked list
		if (linkedListSet.getHead() == null) {
			return null; // No items
		}

		// Create purge of items in linked list
		return promise(linkedListSet.purgeEntries(), (entry) -> {
			return entry;
		}, (entry) -> {
			return entry.getNext();
		}, translate);
	}

	/**
	 * Executes a {@link FunctionState} for all entries of the
	 * {@link LinkedListSet}. This does not change the {@link LinkedListSet} (as
	 * takes copy).
	 *
	 * @param               <E> {@link LinkedListSetEntry} type.
	 * @param               <O> Owner type of {@link LinkedListSetEntry}.
	 * @param linkedListSet {@link LinkedListSet}.
	 * @param translate     {@link Translate}.
	 * @return {@link FunctionState} to purse the {@link LinkedListSet}.
	 */
	public static <E extends LinkedListSetEntry<E, O>, O> FunctionState all(LinkedListSet<E, O> linkedListSet,
			Translate<E> translate) {

		// Ensure items in the linked list
		if (linkedListSet.getHead() == null) {
			return null; // No items
		}

		// Create copy of items in linked list
		return promise(linkedListSet.copyEntries(), (item) -> {
			return item.getEntry();
		}, (entry) -> {
			return entry.getNext();
		}, translate);
	}

	/**
	 * <p>
	 * Creates the {@link FunctionState} for items from head.
	 * <p>
	 * Note: should no head {@link FunctionState} be found for an item in the list,
	 * then may return <code>null</code>.
	 * 
	 * @param head      Head {@link LinkedListSetItem} of the listing.
	 * @param getEntry  Obtains the entry from the item.
	 * @param getNext   Obtains the next item from the {@link LinkedListSet}.
	 * @param translate {@link Translate}.
	 * @return {@link FunctionState} to undertake the list. May be <code>null</code>
	 *         if nothing in the list.
	 */
	private static <I, E> FunctionState promise(I head, Function<I, E> getEntry, Function<I, I> getNext,
			Translate<E> translate) {

		// Obtain the head function
		FunctionState headFunction = translate.translate(getEntry.apply(head));
		while (headFunction == null) {

			// Move to next item in the list
			head = getNext.apply(head);
			if (head == null) {
				return null; // nothing in list to handle
			}

			// Attempt to obtain the head function
			translate.translate(getEntry.apply(head));
		}

		// Return the promise start at head function
		return new LinkedListSetPromise<>(head, headFunction, getEntry, getNext, translate);
	}

	/**
	 * Previous {@link FunctionState} chain to complete before executing the next
	 * {@link LinkedListSetItem} in the list.
	 */
	private final FunctionState previousFunction;

	/**
	 * Head of the listing.
	 */
	private final I head;

	/**
	 * Head {@link FunctionState}.
	 */
	private final FunctionState headFunction;

	/**
	 * Obtains the entry from the item.
	 */
	private final Function<I, E> getEntry;

	/**
	 * Obtains the next item from the {@link LinkedListSet}.
	 */
	private final Function<I, I> getNext;

	/**
	 * {@link Translate}.
	 */
	private final Translate<E> translate;

	/**
	 * Under takes all {@link FunctionState} instances within the list.
	 * 
	 * @param head         Head {@link LinkedListSetItem} of the listing.
	 * @param headFunction {@link FunctionState} for head.
	 * @param getEntry     Obtains the entry from the item.
	 * @param getNext      Obtains the next item from the {@link LinkedListSet}.
	 * @param translate    {@link Translate}.
	 */
	private LinkedListSetPromise(I head, FunctionState headFunction, Function<I, E> getEntry, Function<I, I> getNext,
			Translate<E> translate) {
		this(null, head, headFunction, getEntry, getNext, translate);
	}

	/**
	 * Used internally to enable continuing the execution of the list of
	 * {@link Function} instances.
	 * 
	 * @param previousFunction Previous {@link FunctionState} chain to complete
	 *                         before executing the next {@link LinkedListSetItem}
	 *                         in the list.
	 * @param head             Head {@link LinkedListSetItem} of the listing of
	 *                         {@link FunctionState} instances to execute.
	 * @param headFunction     {@link FunctionState} for head.
	 * @param getEntry         Obtains the entry from the item.
	 * @param getNext          Obtains the next item from the {@link LinkedListSet}.
	 * @param translate        {@link Translate}.
	 */
	private LinkedListSetPromise(FunctionState previousFunction, I head, FunctionState headFunction,
			Function<I, E> getEntry, Function<I, I> getNext, Translate<E> translate) {
		super(previousFunction != null ? previousFunction : headFunction);
		this.previousFunction = previousFunction;
		this.head = head;
		this.headFunction = headFunction;
		this.getEntry = getEntry;
		this.getNext = getNext;
		this.translate = translate;
	}

	/*
	 * ==================== FunctionState ==============================
	 */

	@Override
	public FunctionState execute(FunctionStateContext context) throws Throwable {

		// Determine if previous chain of functions to complete
		if (this.previousFunction != null) {
			FunctionState nextFunction = this.previousFunction.execute(context);
			if (nextFunction != null) {
				// Need to complete previous functions before next item in list
				return new LinkedListSetPromise<I, E>(nextFunction, this.head, this.headFunction, this.getEntry,
						this.getNext, this.translate);
			}
		}

		// Undertake the head function
		FunctionState nextPreviousFunction = this.headFunction.execute(context);
		I nextHead = this.getNext.apply(this.head);
		if (nextHead == null) {
			// Nothing further in list, so complete last chain of functions
			return nextPreviousFunction;
		} else {
			// Continue on for next item in the list
			E nextHeadEntry = this.getEntry.apply(nextHead);
			FunctionState nextHeadFunction = this.translate.translate(nextHeadEntry);

			// Ensure have function
			while (nextHeadFunction == null) {
				nextHead = this.getNext.apply(nextHead);
				if (nextHead == null) {
					return null; // no further items
				}
				nextHeadEntry = this.getEntry.apply(nextHead);
				nextHeadFunction = this.translate.translate(nextHeadEntry);
			}

			// Return promise for the function
			return new LinkedListSetPromise<>(nextPreviousFunction, nextHead, nextHeadFunction, this.getEntry,
					this.getNext, this.translate);
		}
	}

}
