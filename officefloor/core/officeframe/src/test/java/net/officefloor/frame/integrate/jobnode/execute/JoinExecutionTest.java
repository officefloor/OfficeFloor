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

/**
 * Validates joining.
 * 
 * @author Daniel Sagenschneider
 */
public class JoinExecutionTest extends AbstractTaskNodeTestCase<Work> {

	/**
	 * Ensure join asynchronous.
	 */
	public void testJoinAsynchronous() {

		// Execute the asynchronous node before re-execution
		ExecutionNode<Work> asyncNode = this.bindAsynchronousNode(
				this.getInitialNode(), this.createExecutionTeam());
		this.joinNode(this.getInitialNode(), asyncNode);

		// Execute job
		this.execute();

		// Validate the execution order
		this.validateExecutionOrder(this.getInitialNode(), asyncNode,
				this.getInitialNode());
	}

}