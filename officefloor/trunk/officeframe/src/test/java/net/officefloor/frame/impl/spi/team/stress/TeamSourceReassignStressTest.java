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
package net.officefloor.frame.impl.spi.team.stress;

import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Stress tests the {@link TeamSource} by reassigning a {@link Job} over and
 * over.
 * 
 * @author Daniel
 */
public class TeamSourceReassignStressTest extends OfficeFrameTestCase {

	/**
	 * {@link Team} to test.
	 */
	private Team team;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the team to test
		this.team = this.getTeamToTest();
	}

	/**
	 * Obtains the {@link Team} to test.
	 * 
	 * @return {@link Team} to test.
	 */
	protected Team getTeamToTest() throws Exception {
		// By default use the one person team
		return new TeamSourceStandAlone().loadTeam(OnePersonTeamSource.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// Ensure team is stopped
		this.team.stopWorking();
	}

	/**
	 * Stress tests the {@link TeamSource}.
	 */
	@StressTest
	public void testStressReassignSingleJobTeamSource() throws Exception {

		final int REASSIGN_COUNT = 1000000;

		// Create the reassign job
		ReassignJob job = new ReassignJob(team, REASSIGN_COUNT);

		// Run the reassignment stress test
		this.team.startWorking();
		this.team.assignJob(job);

		// Wait until complete
		long startTime = System.currentTimeMillis();
		while (ReassignJob.getReassignCount() < REASSIGN_COUNT) {
			this.sleep(1);
			this.printMessage("Reassigned " + ReassignJob.getReassignCount()
					+ " times");
			this.printHeapMemoryDiagnostics();
			this.timeout(startTime);
		}
		this.printMessage("Reassigned " + ReassignJob.getReassignCount()
				+ " times");
	}

	/**
	 * Reassign {@link Job}.
	 */
	private static class ReassignJob implements Job {

		/**
		 * Resets for another test run.
		 */
		public synchronized static void reset() {
			reassignCount = 0;
		}

		/**
		 * Obtains the number of reassigns made.
		 * 
		 * @return Number of reassigns made.
		 */
		public static synchronized int getReassignCount() {
			return reassignCount;
		}

		/**
		 * Number of reassigns made.
		 */
		private static int reassignCount = 0;

		/**
		 * {@link Team} to reassign this {@link Job} when executed.
		 */
		private final Team reassingTeam;

		/**
		 * Maximum number of reassigns before stop reassigning.
		 */
		private final int maxReassigns;

		/**
		 * Initialise.
		 * 
		 * @param reassingTeam
		 *            {@link Team} to reassign this {@link Job} when executed.
		 * @param maxReassigns
		 *            Maximum number of reassigns before stop reassigning.
		 */
		public ReassignJob(Team reassingTeam, int maxReassigns) {
			this.reassingTeam = reassingTeam;
			this.maxReassigns = maxReassigns;
		}

		/*
		 * ===================== Job =====================================
		 */

		@Override
		public boolean doJob(JobContext executionContext) {

			// Determine if max number of reassigns
			synchronized (ReassignJob.class) {
				reassignCount++;
				if (reassignCount >= this.maxReassigns) {
					// Max reassigns
					return true;
				}
			}

			// Assign again (as new job)
			this.reassingTeam.assignJob(new ReassignJob(this.reassingTeam,
					this.maxReassigns));
			return true;
		}

		/**
		 * Next {@link Job}.
		 */
		private Job nextJob = null;

		@Override
		public Job getNextJob() {
			return this.nextJob;
		}

		@Override
		public void setNextJob(Job job) {
			this.nextJob = job;
		}

	}

}