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
package net.officefloor.frame.integrate.jobnode.execute;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.integrate.jobnode.AbstractTaskNodeTestCase;
import net.officefloor.frame.integrate.jobnode.ExecutionNode;
import net.officefloor.frame.integrate.jobnode.ExecutionTeam;
import net.officefloor.frame.integrate.jobnode.ManagedObjectProcesser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

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
	 * Ensure next node executed by the responsible {@link Team}.
	 */
	public void testNextJobByResponsibleTeam() {
		ExecutionTeam responsibleTeam = this.createExecutionTeam();
		this.doNextTest(responsibleTeam, responsibleTeam);
	}

	/**
	 * Ensure can continue {@link Team} to execute the next node.
	 */
	public void testNextJobByContinueTeam() {
		this.doNextTest(this.getInitialTeam(), this.getContinueTeam());
	}

	/**
	 * Ensure next node will executed.
	 * 
	 * @param responsibleTeam
	 *            {@link Team} responsible for next {@link Job}.
	 * @param expectedExecutionTeam
	 *            {@link Team} expected to execute the next {@link Job}.
	 */
	private void doNextTest(TeamManagement responsibleTeam,
			ExecutionTeam expectedExecutionTeam) {

		// Add next node to execute
		ExecutionNode<?> nextNode = this.bindNextNode(this.getInitialNode(),
				responsibleTeam, expectedExecutionTeam);

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), nextNode);
	}

	/**
	 * Ensure sequential node executed by the responsible {@link Team}.
	 */
	public void testSequentialJobByResponsibleTeam() {
		ExecutionTeam responsibleTeam = this.createExecutionTeam();
		this.doSequentialTest(responsibleTeam, responsibleTeam);
	}

	/**
	 * Ensure can continue {@link Team} to execute the sequential node.
	 */
	public void testSequentialJobByContinueTeam() {
		this.doSequentialTest(this.getInitialTeam(), this.getContinueTeam());
	}

	/**
	 * Ensure node will be executed sequentially.
	 * 
	 * @param responsibleTeam
	 *            {@link Team} responsible for sequential {@link Job}.
	 * @param expectedExecutionTeam
	 *            {@link Team} expected to execute the sequential {@link Job}.
	 */
	private void doSequentialTest(TeamManagement responsibleTeam,
			ExecutionTeam expectedExecutionTeam) {

		// Add sequential node to execute
		ExecutionNode<?> sequentialNode = this.bindSequentialNode(
				this.getInitialNode(), responsibleTeam, expectedExecutionTeam);

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), sequentialNode);
	}

	/**
	 * Ensure parallel node executed by the responsible {@link Team}.
	 */
	public void testParallelJobByResponsibleTeam() {
		ExecutionTeam responsibleTeam = this.createExecutionTeam();
		this.doParallelTest(responsibleTeam, responsibleTeam);
	}

	/**
	 * Ensure can continue {@link Team} to execute the parallel node.
	 */
	public void testParallelJobByContinueTeam() {
		this.doParallelTest(this.getInitialTeam(), this.getContinueTeam());
	}

	/**
	 * Ensure node will be executed in parallel.
	 * 
	 * @param responsibleTeam
	 *            {@link Team} responsible for parallel {@link Job}.
	 * @param expectedExecutionTeam
	 *            {@link Team} expected to execute the parallel {@link Job}.
	 */
	private void doParallelTest(TeamManagement responsibleTeam,
			ExecutionTeam expectedExecutionTeam) {

		// Add parallel node to execute
		ExecutionNode<?> parallelNode = this.bindParallelNode(
				this.getInitialNode(), responsibleTeam, expectedExecutionTeam);

		// Execute the nodes
		this.execute();

		this.validateExecutionOrder(this.getInitialNode(), parallelNode,
				this.getInitialNode());
	}

	/**
	 * Ensure asynchronous node executed by the responsible {@link Team}.
	 */
	public void testAsynchronousJobByResponsibleTeam() {
		ExecutionTeam responsibleTeam = this.createExecutionTeam();
		this.doAsynchronousTest(responsibleTeam);
	}

	/**
	 * Ensure for asynchronous {@link Job} that does not continue with same
	 * {@link Team} {@link Thread}.
	 */
	public void testAsynchronousJobByContinueTeam() {
		this.doAsynchronousTest(this.getInitialTeam());
	}

	/**
	 * Ensure node will be executed asynchronously.
	 * 
	 * @param responsibleTeam
	 *            {@link Team} responsible for asynchronous {@link Job}.
	 */
	private void doAsynchronousTest(ExecutionTeam responsibleTeam) {

		// Add asynchronous node to execute
		ExecutionNode<?> asyncNode = this.bindAsynchronousNode(
				this.getInitialNode(), responsibleTeam);

		// Add next node to ensure order
		ExecutionNode<?> nextNode = this.bindNextNode(this.getInitialNode(),
				this.getInitialTeam(), this.getContinueTeam());

		// Execute the nodes
		this.execute();

		// As all teams are passive, order is followed
		this.validateExecutionOrder(this.getInitialNode(), asyncNode, nextNode);
	}

}