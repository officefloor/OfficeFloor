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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Raw meta-data for a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataImpl implements RawGovernanceMetaDataFactory,
		RawGovernanceMetaData {

	/**
	 * Obtains the {@link RawGovernanceMetaDataFactory}.
	 * 
	 * @return {@link RawGovernanceMetaDataFactory}.
	 */
	public static RawGovernanceMetaDataFactory getFactory() {
		return new RawGovernanceMetaDataImpl();
	}

	/*
	 * ==================== RawGovernanceMetaDataFactory ==================
	 */

	@Override
	public RawGovernanceMetaData createRawGovernanceMetaData() {
		// TODO create raw governance meta-data
		return new RawGovernanceMetaDataImpl();
	}

	/*
	 * =================== RawGovernanceMetaDataMetaData ==================
	 */

	@Override
	public Object getGovernanceName() {
		// TODO implement RawGovernanceMetaData.getGovernanceName
		throw new UnsupportedOperationException(
				"TODO implement RawGovernanceMetaData.getGovernanceName");
	}

}