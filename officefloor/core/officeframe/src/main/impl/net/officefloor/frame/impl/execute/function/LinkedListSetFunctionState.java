/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.function;

import java.util.function.Function;

import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSetItem;

/**
 * {@link FunctionLogic} to execute a {@link FunctionLogic} for each
 * {@link LinkedListSetItem} of a list.
 *
 * @author Daniel Sagenschneider
 */
public class LinkedListSetFunctionState<E> extends AbstractDelegateFunctionState {

	/**
	 * Translates the {@link LinkedListSetItem} to a {@link FunctionState}.
	 */
	public static interface Translate<E> {

		/**
		 * Translates the {@link LinkedListSetItem} to a {@link FunctionState}.
		 * 
		 * @param item
		 *            {@link LinkedListSetItem}.
		 * @return {@link FunctionState}.
		 */
		FunctionState translate(LinkedListSetItem<E> item);
	}

	/**
	 * Convenience constructor to create a {@link LinkedListSetFunctionState}
	 * that casts the {@link LinkedListSetItem} to a {@link Function}.
	 * 
	 * @param head
	 *            {@link LinkedListSetItem} head of list.
	 * @return {@link LinkedListSetFunctionState}.
	 */
	@SuppressWarnings("unchecked")
	public static <J extends FunctionLogic> LinkedListSetFunctionState<J> createCastFunction(
			LinkedListSetItem<J> head) {
		return new LinkedListSetFunctionState<>(head, CAST_TRANSLATE);
	}

	/**
	 * {@link Function} to cast {@link LinkedListSetItem} to a {@link Function}.
	 */
	private static final Translate CAST_TRANSLATE = new Translate() {
		@Override
		public FunctionState translate(LinkedListSetItem item) {
			return (FunctionState) item.getEntry();
		}
	};

	/**
	 * Previous {@link FunctionState} chain to complete before executing the
	 * next {@link LinkedListSetItem} in the list.
	 */
	private final FunctionState previousFunction;

	/**
	 * Head {@link LinkedListSetItem} of the listing.
	 */
	private final LinkedListSetItem<E> head;

	/**
	 * Head {@link FunctionState}.
	 */
	private final FunctionState headFunction;

	/**
	 * {@link Translate}.
	 */
	private final Translate<E> translate;

	/**
	 * Under takes all {@link FunctionState} instances within the list.
	 * 
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing.
	 * @param translate
	 *            {@link Translate}.
	 */
	public LinkedListSetFunctionState(LinkedListSetItem<E> head, Translate<E> translate) {
		this(null, head, translate.translate(head), translate);
	}

	/**
	 * Used internally to enable continuing the execution of the list of
	 * {@link Function} instances.
	 * 
	 * @param previousFunction
	 *            Previous {@link FunctionState} chain to complete before
	 *            executing the next {@link LinkedListSetItem} in the list.
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing of
	 *            {@link FunctionState} instances to execute.
	 * @param headFunction
	 *            {@link FunctionState} for head.
	 * @param translate
	 *            {@link Translate}.
	 */
	private LinkedListSetFunctionState(FunctionState previousFunction, LinkedListSetItem<E> head,
			FunctionState headFunction, Translate<E> translate) {
		super(previousFunction != null ? previousFunction : headFunction);
		this.previousFunction = previousFunction;
		this.head = head;
		this.headFunction = headFunction;
		this.translate = translate;
	}

	/*
	 * ==================== FunctionState ==============================
	 */

	@Override
	public FunctionState execute() throws Throwable {

		// Determine if previous chain of functions to complete
		if (this.previousFunction != null) {
			FunctionState nextFunction = this.previousFunction.execute();
			if (nextFunction != null) {
				// Need to complete previous functions before next item in list
				return new LinkedListSetFunctionState<E>(nextFunction, this.head, this.headFunction, this.translate);
			}
		}

		// Undertake the head function
		FunctionState nextPreviousFunction = this.headFunction.execute();
		LinkedListSetItem<E> nextHead = this.head.getNext();
		if (nextHead == null) {
			// Nothing further in list, so complete last chain of functions
			return nextPreviousFunction;
		} else {
			// Continue on for next item in the list
			FunctionState nextHeadFunction = this.translate.translate(nextHead);
			return new LinkedListSetFunctionState<>(nextPreviousFunction, nextHead, nextHeadFunction, this.translate);
		}
	}

}