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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link net.officefloor.frame.spi.team.Team} implementation of many
 * {@link java.lang.Thread} instances that follow the leader follower pattern.
 * 
 * @author Daniel
 */
public class LeaderFollowerTeam extends ThreadGroup implements Team {

	/**
	 * {@link TeamMember} instances.
	 */
	protected final TeamMember[] teamMembers;

	/**
	 * {@link TeamMemberStack}.
	 */
	protected final TeamMemberStack teamMemberStack = new TeamMemberStack();

	/**
	 * {@link TaskQueue}.
	 */
	protected final TaskQueue taskQueue = new TaskQueue();

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
	 * ======================================================================
	 * Team
	 * ======================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#startWorking()
	 */
	public synchronized void startWorking() {
		// Start the team working
		for (int i = 0; i < this.teamMembers.length; i++) {
			// Start the Team Member
			new Thread(this, this.teamMembers[i]).start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void assignJob(Job task) {
		this.taskQueue.enqueue(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#stopWorking()
	 */
	public synchronized void stopWorking() {
		// Stop the team workings
		for (TeamMember teamMember : this.teamMembers) {
			// Flag the team member to stop work
			teamMember.continueWorking = false;

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
	 * ======================================================================
	 * ThreadGroup
	 * ======================================================================
	 */

	public void uncaughtException(Thread t, Throwable e) {
		// Indicate failure
		System.out.println(t.getName() + "[" + t.getId() + "]: "
				+ e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
		e.printStackTrace();
	}

}

/**
 * Team member of the {@link LeaderFollowerTeam}.
 * 
 * @author Daniel
 */
class TeamMember implements Runnable, JobContext {

	/**
	 * Stack of the {@link TeamMember} instances.
	 */
	protected final TeamMemberStack teamMemberStack;

	/**
	 * Queue of {@link Job} instances to execute.
	 */
	protected final TaskQueue taskQueue;

	/**
	 * Time to wait in milliseconds for a {@link Job}.
	 */
	protected final long waitTime;

	/**
	 * Previous {@link TeamMember}.
	 */
	protected TeamMember previous;

	/**
	 * Flag indicating to continue to work.
	 */
	protected volatile boolean continueWorking = true;

	/**
	 * Flag to indicate finished.
	 */
	protected volatile boolean finished = false;

	/**
	 * Time.
	 */
	protected long time;

	/**
	 * Initiate the {@link TeamMember}.
	 * 
	 * @param teamMemberStack
	 *            {@link TeamMemberStack} for the {@link TeamMember} instances
	 *            of this {@link Team}.
	 * @param taskQueue
	 *            {@link TaskQueue} of {@link Job} instances.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public TeamMember(TeamMemberStack teamMemberStack, TaskQueue taskQueue,
			long waitTime) {
		// Store state
		this.teamMemberStack = teamMemberStack;
		this.taskQueue = taskQueue;
		this.waitTime = waitTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			boolean isWork = true;
			while (isWork) {

				// Become a follower
				this.teamMemberStack.waitToBeLeader(this);

				// Promoted to leader, check if working
				if (!this.continueWorking) {
					// Stop working
					isWork = false;

				} else {

					// Promote another leader
					this.teamMemberStack.promoteLeader();

					// Obtain the execution time
					this.time = System.currentTimeMillis();

					// Obtain the next Task
					Job task = this.taskQueue.dequeue(this,
							this.waitTime);
					if (task != null) {
						// Have task therefore execute it
						if (!task.doJob(this)) {
							// Task needs to be re-executed
							this.taskQueue.enqueue(task);
						}
					}
				}
			}
		} finally {
			// Flag finished
			this.finished = true;
		}
	}

	/*
	 * ======================================================================
	 * ExecutionContext
	 * ======================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#getTime()
	 */
	public long getTime() {
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#continueExecution()
	 */
	public boolean continueExecution() {
		return this.continueWorking;
	}

}

/**
 * Stack of {@link TeamMember} instances.
 */
class TeamMemberStack {

	/**
	 * Leader.
	 */
	protected TeamMember leader = null;

	/**
	 * Top of the Stack of followers.
	 */
	protected TeamMember top = null;

	/**
	 * Completion flag indicating the next leader is about to wait and can not
	 * yet be notified to become the leader.
	 */
	protected volatile boolean isAboutToWait = false;

	/**
	 * <p>
	 * Promotes the most recently added {@link TeamMember} to become leader.
	 * </p>
	 * <p>
	 * Note the most recently added {@link TeamMember} is activated as likely to
	 * be in memory.
	 * </p>
	 * 
	 * @return {@link TeamMember}.
	 */
	public void promoteLeader() {

		synchronized (this) {
			// Notify the next follower to become leader
			if (this.top == null) {
				// No follower waiting to become leader
				this.leader = null;

			} else {
				// Obtain the new leader
				this.leader = this.top;
				this.top = this.leader.previous;
				this.leader.previous = null;

				// Promote the new leader
				boolean isNotified = false;
				while (!isNotified) {
					if (this.isAboutToWait) {
						// Do not notify new leader until waiting
						Thread.yield();
					} else {
						// New leader waiting
						synchronized (this.leader) {
							this.leader.notify();
						}
						isNotified = true;
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Allow a {@link TeamMember} to be a follower until promoted to leader.
	 * </p>
	 */
	public void waitToBeLeader(TeamMember follower) {

		/*
		 * Do not proceed until not about to wait. This is done outside
		 * synchronize as promotion should be selected for execution over
		 * becoming follower to gain greater throughput.
		 */
		while (this.isAboutToWait) {
			Thread.yield();
		}

		// Followers may queue at this point
		synchronized (this) {

			// Do not proceed until not waiting
			while (this.isAboutToWait) {
				Thread.yield();
			}

			// Check if promoted immediately to leader
			if (this.leader == null) {
				// Promote to leader immediately
				this.leader = follower;
				return;
			}

			// Flag about to wait
			this.isAboutToWait = true;

			// Become a follower
			if (this.top != null) {
				follower.previous = this.top;
			}
			this.top = follower;
		}

		synchronized (follower) {
			// Flag not waiting
			this.isAboutToWait = false;

			// Wait to become leader
			try {
				follower.wait();
			} catch (InterruptedException ex) {
				// Ignore
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
