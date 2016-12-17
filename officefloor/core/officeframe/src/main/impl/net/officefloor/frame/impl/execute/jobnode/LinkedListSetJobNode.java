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
package net.officefloor.frame.impl.execute.jobnode;

import java.util.function.Function;

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link JobNode} to execute a {@link JobNode} for each
 * {@link LinkedListSetItem} of a list.
 *
 * @author Daniel Sagenschneider
 */
public class LinkedListSetJobNode<E> implements JobNode {

	/**
	 * Previous {@link JobNode} chain to complete before executing the next
	 * {@link LinkedListSetItem} in the list.
	 */
	private final JobNode previousJobNode;

	/**
	 * Head {@link LinkedListSetItem} of the listing.
	 */
	private final LinkedListSetItem<E> head;

	/**
	 * Head {@link JobNode}.
	 */
	private final JobNode headJobNode;

	/**
	 * {@link Function} to transform the {@link LinkedListSetItem} to a
	 * {@link JobNode}.
	 */
	private final Function<LinkedListSetItem<E>, JobNode> jobNodeFactory;

	/**
	 * Under takes all {@link JobNode} instances within the list.
	 * 
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing.
	 * @param jobNodeFactory
	 *            {@link Function} to transform the {@link LinkedListSetItem} to
	 *            a {@link JobNode}.
	 */
	public LinkedListSetJobNode(LinkedListSetItem<E> head, Function<LinkedListSetItem<E>, JobNode> jobNodeFactory) {
		this(null, head, jobNodeFactory.apply(head), jobNodeFactory);
	}

	/**
	 * Used internally to enable continuing the execution of the list of
	 * {@link JobNode} instances.
	 * 
	 * @param previousJobNode
	 *            Previous {@link JobNode} chain to complete before executing
	 *            the next {@link LinkedListSetItem} in the list.
	 * @param head
	 *            Head {@link LinkedListSetItem} of the listing of
	 *            {@link JobNode} instances to execute.
	 * @param jobNodeFactory
	 *            {@link Function} to transform the {@link LinkedListSetItem} to
	 *            a {@link JobNode}.
	 */
	private LinkedListSetJobNode(JobNode previousJobNode, LinkedListSetItem<E> head, JobNode headJobNode,
			Function<LinkedListSetItem<E>, JobNode> jobNodeFactory) {
		this.previousJobNode = null;
		this.head = head;
		this.headJobNode = headJobNode;
		this.jobNodeFactory = jobNodeFactory;
	}

	/*
	 * ==================== JobNode ==============================
	 */

	@Override
	public JobNode doJob(JobContext context) {

		// Determine if previous chain of job nodes to complete
		if (this.previousJobNode != null) {
			JobNode nextJobNode = this.previousJobNode.doJob(context);
			if (nextJobNode != null) {
				// Need to complete previous job nodes before next item in list
				return new LinkedListSetJobNode<E>(nextJobNode, this.head, this.headJobNode, this.jobNodeFactory);
			}
		}

		// Undertake the head job
		JobNode headJobNode = this.jobNodeFactory.apply(this.head);
		JobNode nextPreviousJobNode = headJobNode.doJob(context);
		LinkedListSetItem<E> nextHead = this.head.getNext();
		if (nextHead == null) {
			// Nothing further in list, so complete last chain of jobs
			return nextPreviousJobNode;
		} else {
			// Continue on for next item in the list
			JobNode nextHeadJobNode = this.jobNodeFactory.apply(nextHead);
			return new LinkedListSetJobNode<>(nextPreviousJobNode, nextHead, nextHeadJobNode, this.jobNodeFactory);
		}
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return (this.previousJobNode != null) ? this.previousJobNode.getResponsibleTeam()
				: this.headJobNode.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return (this.previousJobNode != null) ? this.previousJobNode.getThreadState()
				: this.headJobNode.getThreadState();
	}

}