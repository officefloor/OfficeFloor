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

package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} implementation of many {@link Thread} instances that follow the
 * leader follower pattern.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeam implements Team {

	/**
	 * {@link ThreadFactory}.
	 */
	private final ThreadFactory threadFactory;

	/**
	 * {@link TeamMember} instances.
	 */
	protected final TeamMember[] teamMembers;

	/**
	 * {@link TeamMemberStack}.
	 */
	private final TeamMemberStack teamMemberStack;

	/**
	 * Time to wait in milliseconds for a {@link Job}.
	 */
	private final long waitTime;

	/**
	 * {@link JobQueue}.
	 */
	private final JobQueue jobQueue = new JobQueue();

	/**
	 * Flag indicating to continue to work.
	 */
	private volatile boolean continueWorking = true;

	/**
	 * Initiate with the name of team.
	 * 
	 * @param teamMemberCount
	 *            Number of {@link TeamMember} instances within this
	 *            {@link LeaderFollowerTeam}.
	 * @param threadFactory
	 *            {@link ThreadFactory}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public LeaderFollowerTeam(int teamMemberCount, ThreadFactory threadFactory, long waitTime) {
		this.threadFactory = threadFactory;
		this.waitTime = waitTime;

		// Create the Team Member stack (indicating number of Team Members)
		this.teamMemberStack = new TeamMemberStack(teamMemberCount);

		// Create the listing of Team Members
		this.teamMembers = new TeamMember[teamMemberCount];
		for (int i = 0; i < this.teamMembers.length; i++) {
			// Create the Team Member
			this.teamMembers[i] = new TeamMember();
		}
	}

	/*
	 * ====================== Team ==========================================
	 */

	@Override
	public void startWorking() {

		// Ensure indicate to continue working
		this.continueWorking = true;

		// Start the team members working
		for (int i = 0; i < this.teamMembers.length; i++) {
			Thread thread = this.threadFactory.newThread(this.teamMembers[i]);
			thread.start();
		}
	}

	@Override
	public void assignJob(Job job) {
		this.jobQueue.enqueue(job);
	}

	@Override
	public void stopWorking() {

		// Flag team members to stop working
		this.continueWorking = false;

		// Stop the team workings
		for (TeamMember teamMember : this.teamMembers) {

			// Wait until team member is finished
			while (!teamMember.finished) {

				// Wake up team member
				synchronized (teamMember) {
					teamMember.notify();
				}

				// Allow team member to finish
				Thread.yield();
			}
		}
	}

	/**
	 * Team member of the {@link LeaderFollowerTeam}.
	 */
	private class TeamMember implements Runnable {

		/**
		 * Previous {@link TeamMember} for {@link TeamMemberStack} to use.
		 */
		private TeamMember previous = null;

		/**
		 * Flag to indicate finished.
		 */
		private volatile boolean finished = false;

		/*
		 * ================== Runnable ======================================
		 */

		@Override
		public void run() {
			
			// Easy access to team
			LeaderFollowerTeam team = LeaderFollowerTeam.this;
			
			try {
				for (;;) {
					// Obtain the next job to run
					Job job = team.jobQueue.dequeue(team.waitTime);
					if (job == null) {
						// No job, so check if continue working
						if (!LeaderFollowerTeam.this.continueWorking) {
							// Stop working
							return;
						}

						// Wait to be leader to try for another job
						team.teamMemberStack.waitToBeLeader(this);

					} else {
						// Have job so promote leader for possible next job
						team.teamMemberStack.promoteLeader(this);

						// Run the job
						job.run();
					}
				}
			} finally {
				// Flag finished
				this.finished = true;
			}
		}
	}

	/**
	 * Stack of {@link TeamMember} instances.
	 */
	private static class TeamMemberStack {

		/**
		 * Number of {@link TeamMember} instances.
		 */
		private final int teamMemberWaitCount;

		/**
		 * Number of {@link TeamMember} instances waiting on this
		 * {@link TeamMemberStack}.
		 */
		private int waitingTeamMembers = 0;

		/**
		 * Top of the Stack of followers.
		 */
		private TeamMember top = null;

		/**
		 * Initiate.
		 * 
		 * @param teamMemberCount
		 *            Number of {@link TeamMember} instances.
		 */
		public TeamMemberStack(int teamMemberCount) {
			this.teamMemberWaitCount = teamMemberCount - 1;
		}

		/**
		 * Blocks the {@link TeamMember} until promoted to leader.
		 * 
		 * @param teamMember
		 *            {@link TeamMember} waiting to be promoted to leader.
		 */
		public void waitToBeLeader(TeamMember teamMember) {
			synchronized (teamMember) {

				// Determine if allowed to wait for leader
				boolean isWait;
				synchronized (this) {
					if (this.waitingTeamMembers == this.teamMemberWaitCount) {
						// Last team member must not wait
						isWait = false;
					} else {
						// Flag for team member to wait to be promoted
						isWait = true;

						// Add to stack
						this.waitingTeamMembers++;
						teamMember.previous = this.top;
						this.top = teamMember;
					}
				}

				// Determine if must wait
				if (isWait) {
					// Wait to be promoted to leader
					try {
						teamMember.wait();
					} catch (InterruptedException ex) {
						// Ignore
					} finally {
						// No longer waiting
						synchronized (this) {
							this.waitingTeamMembers--;
						}
					}
				}
			}
		}

		/**
		 * Promotes the next leader.
		 * 
		 * @param teamMember
		 *            {@link TeamMember} that is current leader.
		 */
		public void promoteLeader(TeamMember teamMember) {

			// Obtain the promoted leader
			TeamMember promotedLeader;
			synchronized (teamMember) {

				// Obtain the next follower to promote to leader
				synchronized (this) {
					if (this.top == null) {
						// No followers, so flag no leader promoted
						promotedLeader = null;

					} else {
						// Obtain the new leader
						promotedLeader = this.top;
						this.top = promotedLeader.previous;
						promotedLeader.previous = null;
					}
				}
			}

			// Promote the next leader (if there is one)
			if (promotedLeader != null) {
				synchronized (promotedLeader) {
					// Flag as leader and notify to start running
					promotedLeader.notify();
				}
			}
		}
	}

}
