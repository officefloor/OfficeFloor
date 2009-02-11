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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * {@link OfficeMetaData} implementation.
 * 
 * @author Daniel
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * TODO have this replace with details of office and actual do the creation
	 * within this meta-data.
	 */
	private final OfficeImpl office;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param office
	 *            TODO replace with details of office.
	 */
	public OfficeMetaDataImpl(String officeName, OfficeImpl office) {
		this.officeName = officeName;
		this.office = office;
	}

	/*
	 * ==================== OfficeMetaData ==============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.OfficeMetaData#getOfficeName()
	 */
	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.OfficeMetaData#createOffice()
	 */
	@Override
	public Office createOffice() {
		// Open the office
		this.office.openOffice();

		// Return the office
		return this.office;
	}

}
