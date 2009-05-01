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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Office} node.
 * 
 * @author Daniel
 */
public interface OfficeNode extends OfficeType, OfficeArchitect,
		DeployedOffice, LinkOfficeNode {

	/**
	 * Adds the context of the {@link OfficeFloor} containing this
	 * {@link DeployedOffice}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	void addOfficeFloorContext(String officeFloorLocation);

	/**
	 * Loads this {@link Office} into this {@link OfficeNode}.
	 * 
	 * @return <code>true</code> if successfully loaded.
	 */
	boolean loadOffice(OfficeSource officeSource, PropertyList properties);

	/**
	 * Loads this {@link Office} ready for it to be built.
	 */
	void loadOffice();

	/**
	 * Builds the {@link Office} for this {@link OfficeNode}.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 */
	void buildOffice(OfficeFloorBuilder builder);

}