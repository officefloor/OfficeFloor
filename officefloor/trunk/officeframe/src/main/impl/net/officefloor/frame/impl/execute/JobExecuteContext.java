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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.spi.team.Job;

/**
 * Context for {@link JobContainer} to execute the {@link Job}.
 * 
 * @author Daniel
 */
public interface JobExecuteContext {

	/**
	 * Specifies whether the job is complete.
	 * 
	 * @param isComplete
	 *            <code>true<code> if complete.
	 */
	void setJobComplete(boolean isComplete);

	/**
	 * Joins on the {@link Flow} for the {@link FlowFuture}.
	 * 
	 * @param flowFuture
	 *            {@link FlowFuture} of the {@link Flow} to join.
	 */
	void joinFlow(FlowFuture flowFuture);

	/**
	 * Invokes the {@link Flow} for the input {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the {@link Flow}.
	 * @return {@link FlowFuture} of the {@link Flow}.
	 */
	FlowFuture doFlow(FlowMetaData<?> flowMetaData, Object parameter);

}