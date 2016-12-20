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

import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionState} to execute a {@link FunctionState} for each
 * {@link LinkedListSetItem} of a list.
 *
 * @author Daniel Sagenschneider
 */
public class LinkedListSetJobNode<E> implements FunctionState {

	/**
	 * Convenience constructor to create a {@link LinkedListSetJobNode} that
	 * casts the {@link LinkedListSetItem} to a {@link Function}.
	 * 
	 * @param head
	 *            {@link LinkedListSetItem} head of list.
	 * @return {@link LinkedListSetJobNode}.
	 */
	@SuppressWarnings("unchecked")
	public static <J extends FunctionState> LinkedListSetJobNode<J> createCastJobNode(LinkedListSetItem<J> head) {
		return new LinkedListSetJobNode<>(null, head, head.getEntry(), CAST_JOB_NODE_FACTORY);
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
	 * Head {@link Function}.
	 */
	private final FunctionState headFunction;

	/**
	 * {@link Function} to transform the {@link LinkedListSetItem} to a
	 * {@link FunctionState}.
	 */
	private final Function<LinkedListSetItem<E>, FunctionState> functionFactory;

	/**
	 * Under takes all {@link FunctionState} instances within the list.
	 * 
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing.
	 * @param jobNodeFactory
	 *            {@link Function} to transform the {@link LinkedListSetItem} to
	 *            a {@link FunctionState}.
	 */
	public LinkedListSetJobNode(LinkedListSetItem<E> head,
			Function<LinkedListSetItem<E>, FunctionState> jobNodeFactory) {
		this(null, head, jobNodeFactory.apply(head), jobNodeFactory);
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
	 *            a {@link FunctionState}.
	 */
	private LinkedListSetJobNode(FunctionState previousFunction, LinkedListSetItem<E> head, FunctionState headFunction,
			Function<LinkedListSetItem<E>, FunctionState> functionFactory) {
		this.previousFunction = null;
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
	public ThreadState getThreadState() {
		return (this.previousFunction != null) ? this.previousFunction.getThreadState()
				: this.headFunction.getThreadState();
	}

	@Override
	public FunctionState execute() {

		// Determine if previous chain of functions to complete
		if (this.previousFunction != null) {
			FunctionState nextFunction = this.previousFunction.execute();
			if (nextFunction != null) {
				// Need to complete previous functions before next item in list
				return new LinkedListSetJobNode<E>(nextFunction, this.head, this.headFunction, this.functionFactory);
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
			FunctionState nextHeadFunction = this.functionFactory.apply(nextHead);
			return new LinkedListSetJobNode<>(nextPreviousFunction, nextHead, nextHeadFunction, this.functionFactory);
		}
	}

}