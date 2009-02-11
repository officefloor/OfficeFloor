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
package net.officefloor.frame.impl.construct.administrator;

import java.util.Map;

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.team.Team;

/**
 * Factory to create {@link RawBoundAdministratorMetaData}.
 * 
 * @author Daniel
 */
public interface RawBoundAdministratorMetaDataFactory {

	/**
	 * Creates the {@link RawBoundAdministratorMetaData} instances.
	 * 
	 * @param configuration
	 *            {@link AdministratorSourceConfiguration} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param administratorScope
	 *            {@link AdministratorScope} that the {@link Administrator}
	 *            instances are being bound.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administrator}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administrator}
	 *            instances.
	 * @param officeTeams
	 *            {@link Team} instances by their {@link Office} registered
	 *            names.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @return Constructed {@link RawBoundAdministratorMetaData} instances.
	 */
	RawBoundAdministratorMetaData<?, ?>[] constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<?, ?>[] configuration,
			OfficeFloorIssues issues, AdministratorScope administratorScope,
			AssetType assetType, String assetName,
			Map<String, Team> officeTeams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo);

}