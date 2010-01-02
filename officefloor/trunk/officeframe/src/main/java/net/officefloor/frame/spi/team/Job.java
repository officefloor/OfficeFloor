/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.spi.team;

/**
 * {@link Job} executed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Job {

	/**
	 * <p>
	 * Executes the {@link Job}.
	 * <p>
	 * The return indicates if the {@link Job} has been completed and may be
	 * released. Returning <code>false</code> indicates this method must be
	 * executed again (and possibly again and again) until it returns
	 * <code>true</code>.
	 * 
	 * @param executionContext
	 *            Context for execution.
	 * @return <code>true</code> if the {@link Job} has completed.
	 */
	boolean doJob(JobContext executionContext);

	/**
	 * <p>
	 * Specifies the next {@link Job}. This provides ability to create a linked
	 * list of {@link Job} instances.
	 * <p>
	 * Note there is no thread-safety guaranteed on this method.
	 * 
	 * @param job
	 *            {@link Job} that is next in the list to this {@link Job}.
	 * @see #getNextJob()
	 */
	void setNextJob(Job job);

	/**
	 * <p>
	 * Obtains the next {@link Job}. This provides ability to create a linked
	 * list of {@link Job} instances.
	 * <p>
	 * Note there is no thread-safety guaranteed on this method.
	 * 
	 * @return Next {@link Job} after this in the list.
	 * @see #setNextJob(Job)
	 */
	Job getNextJob();

}