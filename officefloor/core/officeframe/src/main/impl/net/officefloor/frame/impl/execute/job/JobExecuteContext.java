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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.team.Job;

/**
 * Context for {@link AbstractJobContainer} to execute the {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobExecuteContext {

	/**
	 * Specifies whether the job is complete.
	 * 
	 * @param isComplete
	 *            <code>true</code> if complete.
	 */
	void setJobComplete(boolean isComplete);

	/**
	 * Joins on the {@link JobSequence} for the {@link FlowFuture}.
	 * 
	 * @param flowFuture
	 *            {@link FlowFuture} of the {@link JobSequence} to join.
	 * @param timeout
	 *            Timeout in milliseconds for the {@link JobSequence} join.
	 * @param token
	 *            {@link JobSequence} join token.
	 */
	void joinFlow(FlowFuture flowFuture, long timeout, Object token);

	/**
	 * Invokes the {@link JobSequence} for the input {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the {@link JobSequence}.
	 * @return {@link FlowFuture} of the {@link JobSequence}.
	 */
	FlowFuture doFlow(FlowMetaData<?> flowMetaData, Object parameter);

}