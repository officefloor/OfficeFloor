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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Meta-data for a {@link Duty}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyMetaData {

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData<?> getFlow(int flowIndex);

	/**
	 * Translates the {@link Duty} {@link Governance} index to the
	 * {@link ThreadState} {@link Governance} index.
	 * 
	 * @param governanceIndex
	 *            {@link Duty} {@link Governance} index.
	 * @return {@link ThreadState} {@link Governance} index.
	 */
	int translateGovernanceIndexToThreadIndex(int governanceIndex);

}