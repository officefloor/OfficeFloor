/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

/**
 * {@link Team} implementation of many {@link Thread} instances that follow the
 * leader follower pattern.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeam extends ThreadGroup implements Team {

	/**
	 * {@link TeamMember} instances.
	 */
	protected final TeamMember[] teamMembers;

	/**
	 * {@link TeamMemberStack}.
	 */
	private final TeamMemberStack teamMemberStack = new TeamMemberStack();

	/**
	 * {@link TaskQueue}.
	 */
	private final TaskQueue taskQueue = new TaskQueue();

	/**
	 * Flag indicating to continue to work.
	 */
	private volatile boolean continueWorking = true;

	/**
	 * Initiate with the name of team.
	 * 
	 * @param teamName
	 *            Name of team.
	 * @param teamMemberCount
	 *            Number of {@link TeamMember} instances within this
	 *            {@link LeaderFollowerTeam}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public LeaderFollowerTeam(String teamName, int teamMemberCount,
			long waitTime) {
		super(teamName);

		// Create the listing of Team Members
		this.teamMembers = new TeamMember[teamMemberCount];
		for (int i = 0; i < this.teamMembers.length; i++) {
			// Create the Team Member
			this.teamMembers[i] = new TeamMember(this.teamMemberStack,
					this.taskQueue, waitTime);
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
			new Thread(this, this.teamMembers[i]).start();
		}
	}

	@Override
	public void assignJob(Job task) {
		this.taskQueue.enqueue(task);
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
		System.out.println(t.getName() + "[" + t.getId() + "]: "
				+ e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
		e.printStackTrace();
	}

	/**
	 * Team member of the {@link LeaderFollowerTeam}.
	 */
	protected class TeamMember implements Runnable, JobContext {

		/**
		 * Indicates no time.
		 */
		private static final int NO_TIME = 0;

		/**
		 * Stack of the {@link TeamMember} instances.
		 */
		private final TeamMemberStack teamMemberStack;

		/**
		 * Queue of {@link Job} instances to execute.
		 */
		private final TaskQueue taskQueue;

		/**
		 * Time to wait in milliseconds for a {@link Job}.
		 */
		private final long waitTime;

		/**
		 * Used by {@link TeamMemberStack} to flag if leader.
		 */
		protected boolean isLeader = false;

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
		 *            {@link TaskQueue} of {@link Job} instances.
		 * @param waitTime
		 *            Time to wait in milliseconds for a {@link Job}.
		 */
		public TeamMember(TeamMemberStack teamMemberStack, TaskQueue taskQueue,
				long waitTime) {
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
					Job job = this.taskQueue.dequeue(this, this.waitTime);
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
						this.time = NO_TIME;

						// Run the job
						if (!job.doJob(this)) {
							// Job needs to be re-run
							this.taskQueue.enqueue(job);
						}
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
			if (this.time == NO_TIME) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
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
		 * Flag indicating if there is a current leader.
		 */
		private boolean isLeader = false;

		/**
		 * Top of the Stack of followers.
		 */
		private TeamMember top = null;

		/**
		 * Blocks the {@link TeamMember} until promoted to leader.
		 * 
		 * @param teamMember
		 *            {@link TeamMember} waiting to be promoted to leader.
		 */
		public void waitToBeLeader(TeamMember teamMember) {
			synchronized (teamMember) {

				// Check to see if leader
				if (!teamMember.isLeader) {

					// Not leader, so determine if must wait
					boolean isWait;
					synchronized (this) {
						// Only load as follower if no leader
						if (!this.isLeader) {
							// Promoted to leader as no other leader
							this.isLeader = true;
							isWait = false;

						} else {
							// Follower and must wait
							isWait = true;
							if (this.top != null) {
								teamMember.previous = this.top;
							}
							this.top = teamMember;
						}
					}
					if (isWait) {
						// Wait to be promoted to leader
						try {
							teamMember.wait();
						} catch (InterruptedException ex) {
							// Ignore
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

				// No longer leader as another leader promoted
				teamMember.isLeader = false;

				// Obtain the next follower to promote to leader
				synchronized (this) {
					if (this.top == null) {
						// No followers, so flag no leader promoted
						this.isLeader = false;
						promotedLeader = null;

					} else {
						// Obtain the new leader
						promotedLeader = this.top;
						this.top = promotedLeader.previous;
						promotedLeader.previous = null;

						// Has new leader
						this.isLeader = true;
					}
				}
			}

			// Promote the next leader (if there is one)
			if (promotedLeader != null) {
				synchronized (promotedLeader) {
					// Flag as leader and notify to start running
					promotedLeader.isLeader = true;
					promotedLeader.notify();
				}
			}
		}

		/**
		 * Obtains the number of {@link TeamMember} instances on this
		 * {@link TeamMemberStack}.
		 * 
		 * @return Number of {@link TeamMember} instances on this
		 *         {@link TeamMemberStack}.
		 */
		protected int size() {
			synchronized (this) {
				if (this.top == null) {
					return 0;
				} else {
					return this.size(this.top, 1);
				}
			}
		}

		/**
		 * Recursive count to obtain the size.
		 * 
		 * @param node
		 *            Current node in recursion.
		 * @param previousCount
		 *            Count of previous recursed nodes.
		 * @return Number of {@link TeamMember} instances on this
		 *         {@link TeamMemberStack}.
		 */
		private int size(TeamMember node, int previousCount) {
			if (node.previous != null) {
				return size(node.previous, ++previousCount);
			} else {
				return previousCount;
			}
		}
	}

}