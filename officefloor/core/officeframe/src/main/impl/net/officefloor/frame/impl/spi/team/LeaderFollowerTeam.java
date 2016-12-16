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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link Team} implementation of many {@link Thread} instances that follow the
 * leader follower pattern.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeam extends ThreadGroup implements Team {

	/**
	 * {@link TeamIdentifier} of this {@link Team}.
	 */
	private final TeamIdentifier teamIdentifier;

	/**
	 * {@link TeamMember} instances.
	 */
	protected final TeamMember[] teamMembers;

	/**
	 * {@link TeamMemberStack}.
	 */
	private final TeamMemberStack teamMemberStack;

	/**
	 * {@link JobQueue}.
	 */
	private final JobQueue taskQueue = new JobQueue();

	/**
	 * Flag indicating to continue to work.
	 */
	private volatile boolean continueWorking = true;

	/**
	 * Initiate with the name of team.
	 * 
	 * @param teamName
	 *            Name of team.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} of this {@link Team}.
	 * @param teamMemberCount
	 *            Number of {@link TeamMember} instances within this
	 *            {@link LeaderFollowerTeam}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public LeaderFollowerTeam(String teamName, TeamIdentifier teamIdentifier, int teamMemberCount, long waitTime) {
		super(teamName);
		this.teamIdentifier = teamIdentifier;

		// Create the Team Member stack (indicating number of Team Members)
		this.teamMemberStack = new TeamMemberStack(teamMemberCount);

		// Create the listing of Team Members
		this.teamMembers = new TeamMember[teamMemberCount];
		for (int i = 0; i < this.teamMembers.length; i++) {
			// Create the Team Member
			this.teamMembers[i] = new TeamMember(this.teamMemberStack, this.taskQueue, waitTime);
		}
	}

	/*
	 * ====================== Team ==========================================
	 */

	@Override
	public synchronized void startWorking() {

		// Ensure indicate to continue working
		this.continueWorking = true;

		// Start the team members working
		for (int i = 0; i < this.teamMembers.length; i++) {
			String threadName = this.getClass().getSimpleName() + "_" + this.getName() + "_" + String.valueOf(i);
			Thread thread = new Thread(this, this.teamMembers[i], threadName);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void assignJob(Job job, TeamIdentifier assignerTeam) {
		this.taskQueue.enqueue(job);
	}

	@Override
	public synchronized void stopWorking() {

		// Flag team members to stop working
		this.continueWorking = false;

		// Stop the team workings
		for (TeamMember teamMember : this.teamMembers) {

			// Wait until team member is finished
			while (!teamMember.finished) {
				// Flag to wake up team member
				synchronized (teamMember) {
					teamMember.notify();
				}

				// Allow team member to finish
				Thread.yield();
			}
		}
	}

	/*
	 * ================== ThreadGroup =================================
	 */

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// Indicate failure
		System.out.println(
				t.getName() + "[" + t.getId() + "]: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
		e.printStackTrace();
	}

	/**
	 * Team member of the {@link LeaderFollowerTeam}.
	 */
	protected class TeamMember implements Runnable, JobContext {

		/**
		 * Indicates unknown time.
		 */
		private static final int UNKNOWN_TIME = 0;

		/**
		 * Stack of the {@link TeamMember} instances.
		 */
		private final TeamMemberStack teamMemberStack;

		/**
		 * Queue of {@link Job} instances to execute.
		 */
		private final JobQueue taskQueue;

		/**
		 * Time to wait in milliseconds for a {@link Job}.
		 */
		private final long waitTime;

		/**
		 * Previous {@link TeamMember} for {@link TeamMemberStack} to use.
		 */
		protected TeamMember previous = null;

		/**
		 * Flag to indicate finished.
		 */
		protected volatile boolean finished = false;

		/**
		 * Time for the {@link Job}.
		 */
		private long time;

		/**
		 * Initiate the {@link TeamMember}.
		 * 
		 * @param teamMemberStack
		 *            {@link TeamMemberStack} for the {@link TeamMember}
		 *            instances of this {@link Team}.
		 * @param taskQueue
		 *            {@link JobQueue} of {@link Job} instances.
		 * @param waitTime
		 *            Time to wait in milliseconds for a {@link Job}.
		 */
		public TeamMember(TeamMemberStack teamMemberStack, JobQueue taskQueue, long waitTime) {
			this.teamMemberStack = teamMemberStack;
			this.taskQueue = taskQueue;
			this.waitTime = waitTime;
		}

		/*
		 * ================== Runnable ======================================
		 */

		@Override
		public void run() {
			try {
				for (;;) {
					// Obtain the next job to run
					Job job = this.taskQueue.dequeue(this.waitTime);
					if (job == null) {
						// No job, so check if continue working
						if (!LeaderFollowerTeam.this.continueWorking) {
							// Stop working
							return;
						}

						// Wait to be leader to try for another job
						this.teamMemberStack.waitToBeLeader(this);

					} else {
						// Have job so promote leader for possible next job
						this.teamMemberStack.promoteLeader(this);

						// Reset for running the job
						this.time = UNKNOWN_TIME;

						// Run the job
						job.doJob(this);
					}
				}
			} finally {
				// Flag finished
				this.finished = true;
			}
		}

		/*
		 * =================== ExecutionContext ===============================
		 */

		@Override
		public long getTime() {

			// Ensure have the time
			if (this.time == UNKNOWN_TIME) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public TeamIdentifier getCurrentTeam() {
			return LeaderFollowerTeam.this.teamIdentifier;
		}

		@Override
		public boolean continueExecution() {
			return LeaderFollowerTeam.this.continueWorking;
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