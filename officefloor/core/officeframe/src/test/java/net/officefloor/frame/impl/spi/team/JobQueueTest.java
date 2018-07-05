/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link net.officefloor.frame.impl.spi.team.JobQueue}.
 * 
 * @author Daniel Sagenschneider
 */
public class JobQueueTest extends OfficeFrameTestCase {

	/**
	 * {@link JobQueue} to test.
	 */
	private JobQueue jobQueue = new JobQueue();

	/**
	 * Time.
	 */
	private long time;

	/**
	 * Ensure able to dequeue <code>null</code> from empty queue.
	 */
	public void testEmptyDequeue() {
		Job returnedTask = this.jobQueue.dequeue();

		// Validate
		assertNull("Incorrect task returned", returnedTask);
	}

	/**
	 * Ensure able to enqueue and dequeue a task.
	 */
	public void testSingleEnqueueDequeue() {
		Job task = new MockJob();
		this.jobQueue.enqueue(task);
		Job returnedTask = this.jobQueue.dequeue();

		// Validate
		assertSame("Incorrect task returned", task, returnedTask);
	}

	/**
	 * Ensure able to dequeue the head of queue if many tasks.
	 */
	public void testEnqueueDequeueHead() {
		Job taskOne = new MockJob();
		Job taskTwo = new MockJob();
		this.jobQueue.enqueue(taskOne);
		this.jobQueue.enqueue(taskTwo);

		// Validate state
		assertSame("Incorrect first object dequeued", taskOne, this.jobQueue.dequeue());
		assertSame("Incorrect second object dequeued", taskTwo, this.jobQueue.dequeue());
	}

	/**
	 * Ensure wait on task.
	 */
	public void testWaitForTask() {

		final long WAIT_TIME = 10000;

		// Lock
		final Object lock = new Object();

		// Flag to indicate returned from wait
		final boolean flag[] = new boolean[1];
		flag[0] = false;

		// Wait until new thread is running
		synchronized (lock) {

			// Wait on Task
			new Thread(new Runnable() {
				public void run() {

					// Obtain start time
					long startTime = System.currentTimeMillis();

					// Notify started
					synchronized (lock) {
						lock.notify();
					}

					// Wait on task to be added
					jobQueue.waitForTask(WAIT_TIME);

					// Specify time waited
					time = System.currentTimeMillis() - startTime;

					// Flag returned
					synchronized (lock) {
						flag[0] = true;
					}
				}
			}).start();

			// Wait until started
			try {
				lock.wait();
			} catch (InterruptedException ex) {
				fail("Interrupted: " + ex.getMessage());
			}
		}

		// Give a little time to ensure probability of waiting for task
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted: " + ex.getMessage());
		}

		// Ensure waiting on task
		synchronized (lock) {
			assertFalse("Should be waiting on task", flag[0]);
		}

		// Create the Task
		Job task = new MockJob();

		// Add the task
		this.jobQueue.enqueue(task);

		// Wait on return
		boolean isComplete = false;
		while (!isComplete) {
			synchronized (lock) {
				isComplete = flag[0];
			}
			Thread.yield();
		}

		// Ensure released below half the wait time
		assertTrue("Not returned in time", (this.time < (WAIT_TIME / 2)));
	}

	/**
	 * Ensure wait on dequeue.
	 */
	public void testWaitForDequeue() {

		final long WAIT_TIME = 10000;

		// Lock
		final Object lock = new Object();

		// Dequeued Task
		final Job[] dequeuedTask = new Job[1];

		// Wait until new thread is running
		synchronized (lock) {

			// Wait on Task
			new Thread(new Runnable() {
				public void run() {

					// Obtain start time
					long startTime = System.currentTimeMillis();

					// Notify started
					synchronized (lock) {
						lock.notify();
					}

					// Wait on task to be added
					Job task = JobQueueTest.this.jobQueue.dequeue(WAIT_TIME);

					// Specify time waited
					JobQueueTest.this.time = System.currentTimeMillis() - startTime;

					// Flag returned
					synchronized (lock) {
						dequeuedTask[0] = task;
					}
				}
			}).start();

			// Wait until started
			try {
				lock.wait();
			} catch (InterruptedException ex) {
				fail("Interrupted: " + ex.getMessage());
			}
		}

		// Give a little time to ensure probability of waiting for task
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted: " + ex.getMessage());
		}

		// Ensure waiting on task
		synchronized (lock) {
			assertNull("Should be waiting on dequeue", dequeuedTask[0]);
		}

		// Create the Task
		Job task = new MockJob();

		// Add the task
		this.jobQueue.enqueue(task);

		// Wait on return
		boolean isComplete = false;
		while (!isComplete) {
			synchronized (lock) {
				isComplete = (dequeuedTask[0] != null);
			}
			Thread.yield();
		}

		// Ensure correct task
		assertEquals("Incorrect task", task, dequeuedTask[0]);

		// Ensure released below half the wait time
		assertTrue("Not returned in time", (this.time < (WAIT_TIME / 2)));
	}

}