/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.impl.execute.AbstractJobContainer;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.team.Job;

/**
 * Tests the {@link AbstractJobContainer} invoking asynchronous {@link Flow}
 * instances.
 * 
 * @author Daniel
 */
public class AsynchronousJobContainerTest extends AbstractJobContainerTest {
	/**
	 * Ensures execution of a {@link Job} with an asynchronous {@link Flow}
	 * invoked.
	 */
	public void testExecuteJobWithAsynchronousFlow() {

		// Create a job invoking a asynchronous flow
		final Object asynchronousFlowParameter = "Asynchronous Flow Parameter";
		FunctionalityJob job = this.createJob(false, new JobFunctionality() {
			@Override
			public Object executeFunctionality(JobFunctionalityContext context)
					throws Throwable {
				context.doFlow(0, FlowInstigationStrategyEnum.ASYNCHRONOUS,
						asynchronousFlowParameter);
				return null;
			}
		});

		// Record invoking the asynchronous job.
		this.record_JobContainer_initialSteps(null);
		this.record_doAsynchronousFlow(job, asynchronousFlowParameter);
		this.record_JobMetaData_getNextTaskInFlow(false);
		this.record_completeJob(job);
		this.record_JobActivatableSet_activateJobs();

		// Replay mocks
		this.replayMockObjects();

		// Execute to invoke the parallel job
		this.doJob(job, true);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure job run
		assertJobExecuted(job);
	}

}
