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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.TaskConfiguration;

/**
 * Factory to construct {@link RawTaskMetaData}.
 * 
 * @author Daniel Sagenschneider
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
	<W extends Work, D extends Enum<D>, F extends Enum<F>> RawTaskMetaData<W, D, F> constructRawTaskMetaData(
			TaskConfiguration<W, D, F> configuration,
			OfficeFloorIssues issues, RawWorkMetaData<W> rawWorkMetaData);

}