/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.integrate.jobnode.execute;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.integrate.jobnode.AbstractTaskNodeTestCase;
import net.officefloor.frame.integrate.jobnode.ExecutionNode;
import net.officefloor.frame.integrate.jobnode.ManagedObjectProcesser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Validates simple execution trees.
 * 
 * @author Daniel Sagenschneider
 */
public class SimpleExecutionTest extends AbstractTaskNodeTestCase<Work> {

	/**
	 * Ensure a single node may be executed.
	 */
	public void testSingleNode() {

		// Execute the single node
		this.execute();

		assertTrue("Ensure the single node was executed", this.getInitialNode()
				.isExecuted());
	}

	/**
	 * Ensure able to access the {@link ProcessState} {@link ManagedObject}.
	 */
	public void testAccessProcessManagedObject() {

		final Object OBJECT = new Object();
		final Object[] processObject = new Object[1];

		// Flag use of the process managed object
		this.getInitialNode().processManagedObject(ManagedObjectScope.PROCESS,
				OBJECT, new ManagedObjectProcesser<Object>() {
					public void process(Object object) {
						processObject[0] = object;
					}
				});

		// Execute the single node
		this.execute();

		assertTrue("Ensure the single node was executed", this.getInitialNode()
				.isExecuted());
		assertEquals("Ensure object matches", OBJECT, processObject[0]);
	}

	/**
	 * Ensure able to access the {@link ThreadState} {@link ManagedObject}.
	 */
	public void testAccessThreadManagedObject() {

		final Object OBJECT = new Object();
		final Object[] processObject = new Object[1];

		// Flag use of the thread managed object
		this.getInitialNode().processManagedObject(ManagedObjectScope.THREAD,
				OBJECT, new ManagedObjectProcesser<Object>() {
					public void process(Object object) {
						processObject[0] = object;
					}
				});

		// Execute the single node
		this.execute();

		assertTrue("Ensure the single node was executed", this.getInitialNode()
				.isExecuted());
		assertEquals("Ensure object matches", OBJECT, processObject[0]);
	}

	/**
	 * Ensure able to access the {@link Work} {@link ManagedObject}.
	 */
	public void testAccessWorkManagedObject() {

		final Object OBJECT = new Object();
		final Object[] processObject = new Object[1];

		// Flag use of the process managed object
		this.getInitialNode().processManagedObject(ManagedObjectScope.WORK,
				OBJECT, new ManagedObjectProcesser<Object>() {
					public void process(Object object) {
						processObject[0] = object;
					}
				});

		// Execute the single node
		this.execute();

		assertTrue("Ensure the single node was executed", this.getInitialNode()
				.isExecuted());
		assertEquals("Ensure object matches", OBJECT, processObject[0]);
	}

	/**
	 * Ensure next node will executed.
	 */
	public void testNext() {

		// Add next node to execute
		ExecutionNode<?> nextNode = this.bindNextNode(this.getInitialNode());

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), nextNode);
	}

	/**
	 * Ensure node will be executed sequentially.
	 */
	public void testSequential() {

		// Add sequential node to execute
		ExecutionNode<?> sequentialNode = this.bindSequentialNode(this
				.getInitialNode());

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), sequentialNode);
	}

	/**
	 * Ensure node will be executed in parallel.
	 */
	public void testParallel() {

		// Add parallel node to execute
		ExecutionNode<?> parallelNode = this.bindParallelNode(this
				.getInitialNode());

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), parallelNode, this
				.getInitialNode());
	}

	/**
	 * Ensure node will be executed asynchronously.
	 */
	public void testAsynchronous() {

		// Add asynchronous node to execute
		ExecutionNode<?> asyncNode = this.bindAsynchronousNode(this
				.getInitialNode());

		// Add next node to ensure order
		ExecutionNode<?> nextNode = this.bindNextNode(this.getInitialNode());

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), asyncNode, nextNode);
	}

}