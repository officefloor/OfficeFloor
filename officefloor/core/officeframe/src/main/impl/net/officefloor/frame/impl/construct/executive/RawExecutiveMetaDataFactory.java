/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.executive;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Factory for the construction of the {@link RawExecutiveMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaDataFactory {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 */
	public RawExecutiveMetaDataFactory(SourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}

	/**
	 * Creates the {@link RawExecutiveMetaData}.
	 *
	 * @param                 <XS> {@link ExecutiveSource} type.
	 * @param configuration   {@link ExecutiveConfiguration}.
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @param issues          {@link OfficeFloorIssues}.
	 * @return {@link RawExecutiveMetaData} or <code>null</code> if fails to
	 *         construct.
	 */
	public <XS extends ExecutiveSource> RawExecutiveMetaData constructRawExecutiveMetaData(
			ExecutiveConfiguration<XS> configuration, String officeFloorName, OfficeFloorIssues issues) {
		// TODO Auto-generated method stub
		return null;
	}

}