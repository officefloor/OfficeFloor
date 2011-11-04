/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Identifies active {@link Governance} of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveGovernance {

	/**
	 * Indicates if the {@link Governance} is still active.
	 * 
	 * @return <code>true</code> if the {@link Governance} is still active.
	 */
	boolean isActive();

	/**
	 * Obtains the {@link FlowMetaData} for the {@link Task} to setup the
	 * {@link ActiveGovernance}.
	 * 
	 * @return {@link FlowMetaData} for the {@link Task} to setup the
	 *         {@link ActiveGovernance}.
	 */
	FlowMetaData<?> getFlowMetaData();

}