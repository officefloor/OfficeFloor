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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.work.RawWorkMetaData;
import net.officefloor.frame.internal.configuration.TaskConfiguration;

/**
 * Factory to construct {@link RawTaskMetaData}.
 * 
 * @author Daniel
 */
public interface RawTaskMetaDataFactory {

	/**
	 * Constructs the {@link RawTaskMetaData}.
	 * 
	 * @param configuration
	 *            {@link TaskConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param rawWorkMetaData
	 *            {@link RawWorkMetaData} of the {@link Work} containing this
	 *            {@link Task}.
	 * @return {@link RawTaskMetaData}.
	 */
	<P, W extends Work, M extends Enum<M>, F extends Enum<F>> RawTaskMetaData<P, W, M, F> constructRawTaskMetaData(
			TaskConfiguration<P, W, M, F> configuration,
			OfficeFloorIssues issues, RawWorkMetaData<W> rawWorkMetaData);

}