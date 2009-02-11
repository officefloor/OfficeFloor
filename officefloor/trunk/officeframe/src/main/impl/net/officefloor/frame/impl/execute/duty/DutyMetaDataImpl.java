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
package net.officefloor.frame.impl.execute.duty;

import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;

/**
 * Implementation of {@link DutyMetaData}.
 * 
 * @author Daniel
 */
public class DutyMetaDataImpl implements DutyMetaData {

	/**
	 * Listing of {@link FlowMetaData}.
	 */
	private final FlowMetaData<?>[] flows;

	/**
	 * Initiate.
	 * 
	 * @param flows
	 *            Listing of {@link FlowMetaData}.
	 */
	public DutyMetaDataImpl(FlowMetaData<?>[] flows) {
		this.flows = flows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.DutyMetaData#getFlow(int)
	 */
	public FlowMetaData<?> getFlow(int flowIndex) {
		return this.flows[flowIndex];
	}

}
