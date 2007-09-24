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

import net.officefloor.frame.impl.spi.team.TaskQueue;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link net.officefloor.frame.impl.spi.team.TaskQueue}.
 * 
 * @author Daniel
 */
public class TaskQueueTest extends OfficeFrameTestCase implements
		ExecutionContext {

	/**
	 * {@link TaskQueue} to test.
	 */
	protected TaskQueue taskQueue = new TaskQueue();

	/**
	 * Time waiting.
	 */
	protected volatile long time;

	/**
	 * Flag to continue working (testing).
	 */
	protected volatile boolean continueWorking = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#getTime()
	 */
	public long getTime() {
		return System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#continueExecution()
	 */
	public boolean continueExecution() {
		return this.continueWorking;
	}

	/**
	 * Ensure able to dequeue <code>null</code> from empty queue.
	 */
	public void testEmptyDequeue() {
		TaskContainer returnedTask = this.taskQueue.dequeue(this);

		// Validate
		assertNull("Incorrect task returned", returnedTask);
	}

	/**
	 * Ensure able to enqueue and dequeue a task.
	 */
	public void testSingleEnqueueDequeue() {
		TaskContainer task = new MockTaskContainer();
		this.taskQueue.enqueue(task);
		TaskContainer returnedTask = this.taskQueue.dequeue(this);

		// Validate
		assertSame("Incorrect task returned", task, returnedTask);
	}

	/**
	 * Ensure able to dequeue the head of queue if many tasks.
	 */
	public void testEnqueueDequeueHead() {
		TaskContainer taskOne = new MockTaskContainer();
		TaskContainer taskTwo = new MockTaskContainer();
		this.taskQueue.enqueue(taskOne);
		this.taskQueue.enqueue(taskTwo);

		// Validate state
		assertSame("Incorrect head", this.taskQueue.head, taskOne);
		assertSame("Incorrect tail", this.taskQueue.tail, taskTwo);
		assertSame("Incorrect link", taskTwo, taskOne.getNextTask());
		assertNull("Incorrect end", taskTwo.getNextTask());

		TaskContainer returnedTask = this.taskQueue.dequeue(this);

		// Validate state
		assertSame("Incorrect return", taskOne, returnedTask);
		assertNull("Return not cleaned", taskOne.getNextTask());
		assertSame("Incorrect head", this.taskQueue.head, taskTwo);
		assertSame("Incorrect tail", this.taskQueue.tail, taskTwo);
		assertNull("Incorrect end", taskTwo.getNextTask());
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
					taskQueue.waitForTask(WAIT_TIME);

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
		TaskContainer task = new MockTaskContainer();

		// Add the task
		this.taskQueue.enqueue(task);

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

		final ExecutionContext context = this;
		final long WAIT_TIME = 10000;

		// Lock
		final Object lock = new Object();

		// Dequeued Task
		final TaskContainer[] dequeuedTask = new TaskContainer[1];

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
					TaskContainer task = taskQueue.dequeue(context, WAIT_TIME);

					// Specify time waited
					time = System.currentTimeMillis() - startTime;

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
		TaskContainer task = new MockTaskContainer();

		// Add the task
		this.taskQueue.enqueue(task);

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
