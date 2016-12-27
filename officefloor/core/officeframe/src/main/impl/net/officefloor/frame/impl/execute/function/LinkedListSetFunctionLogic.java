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

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link FunctionLogic} to execute a {@link FunctionLogic} for each
 * {@link LinkedListSetItem} of a list.
 *
 * @author Daniel Sagenschneider
 */
public class LinkedListSetFunctionLogic<E> implements FunctionLogic {

	/**
	 * Convenience constructor to create a {@link LinkedListSetFunctionLogic}
	 * that casts the {@link LinkedListSetItem} to a {@link Function}.
	 * 
	 * @param head
	 *            {@link LinkedListSetItem} head of list.
	 * @return {@link LinkedListSetFunctionLogic}.
	 */
	@SuppressWarnings("unchecked")
	public static <J extends FunctionLogic> LinkedListSetFunctionLogic<J> createCastFunction(
			LinkedListSetItem<J> head) {
		return new LinkedListSetFunctionLogic<>(null, head, head.getEntry(), CAST_JOB_NODE_FACTORY);
	}

	/**
	 * {@link Function} to cast {@link LinkedListSetItem} to a {@link Function}.
	 */
	@SuppressWarnings({ "rawtypes" })
	private static final Function CAST_JOB_NODE_FACTORY = new Function() {
		@Override
		public Object apply(Object item) {
			return ((LinkedListSetItem) item).getEntry();
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
	 * Head {@link FunctionLogic}.
	 */
	private final FunctionLogic headFunction;

	/**
	 * {@link Function} to transform the {@link LinkedListSetItem} to a
	 * {@link FunctionLogic}.
	 */
	private final Function<LinkedListSetItem<E>, FunctionLogic> functionFactory;

	/**
	 * Under takes all {@link FunctionState} instances within the list.
	 * 
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing.
	 * @param jobNodeFactory
	 *            {@link Function} to transform the {@link LinkedListSetItem} to
	 *            a {@link FunctionLogic}.
	 */
	public LinkedListSetFunctionLogic(LinkedListSetItem<E> head,
			Function<LinkedListSetItem<E>, FunctionLogic> functionFactory) {
		this(null, head, functionFactory.apply(head), functionFactory);
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
	 * @param functionFactory
	 *            {@link Function} to transform the {@link LinkedListSetItem} to
	 *            a {@link FunctionLogic}.
	 */
	private LinkedListSetFunctionLogic(FunctionState previousFunction, LinkedListSetItem<E> head,
			FunctionLogic headFunction, Function<LinkedListSetItem<E>, FunctionLogic> functionFactory) {
		this.previousFunction = previousFunction;
		this.head = head;
		this.headFunction = headFunction;
		this.functionFactory = functionFactory;
	}

	/*
	 * ==================== FunctionState ==============================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return (this.previousFunction != null) ? this.previousFunction.getResponsibleTeam()
				: this.headFunction.getResponsibleTeam();
	}

	@Override
	public FunctionState execute(Flow flow) throws Throwable {

		// Determine if previous chain of functions to complete
		if (this.previousFunction != null) {
			FunctionState nextFunction = this.previousFunction.execute();
			if (nextFunction != null) {
				// Need to complete previous functions before next item in list
				return flow.createFunction(new LinkedListSetFunctionLogic<E>(nextFunction, this.head, this.headFunction,
						this.functionFactory));
			}
		}

		// Undertake the head function
		FunctionState nextPreviousFunction = this.headFunction.execute(flow);
		LinkedListSetItem<E> nextHead = this.head.getNext();
		if (nextHead == null) {
			// Nothing further in list, so complete last chain of functions
			return nextPreviousFunction;
		} else {
			// Continue on for next item in the list
			FunctionLogic nextHeadFunction = this.functionFactory.apply(nextHead);
			return flow.createFunction(new LinkedListSetFunctionLogic<>(nextPreviousFunction, nextHead,
					nextHeadFunction, this.functionFactory));
		}
	}

}