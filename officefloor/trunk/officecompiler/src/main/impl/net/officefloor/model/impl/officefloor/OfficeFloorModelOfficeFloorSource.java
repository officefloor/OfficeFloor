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
package net.officefloor.model.impl.officefloor;

import java.io.FileNotFoundException;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeFloorModel} {@link OfficeFloorSource}.
 * 
 * @author Daniel
 */
public class OfficeFloorModelOfficeFloorSource extends
		AbstractOfficeFloorSource {

	/*
	 * =================== AbstractOfficeFloorSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void specifyConfigurationProperties(
			RequiredProperties requiredProperties,
			OfficeFloorSourceContext context) throws Exception {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorSource.specifyConfigurationProperties");
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Obtain the configuration to the section
		ConfigurationItem configuration = context.getConfiguration(context
				.getOfficeFloorLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office floor '"
					+ context.getOfficeFloorLocation() + "'");
		}

		// Retrieve the office floor model
		OfficeFloorModel officeFloor = new OfficeFloorRepositoryImpl(
				new ModelRepositoryImpl()).retrieveOfficeFloor(configuration);

		// Load the office floor teams
		for (OfficeFloorTeamModel teamModel : officeFloor.getOfficeFloorTeams()) {
			OfficeFloorTeam team = deployer.addTeam(teamModel
					.getOfficeFloorTeamName(), teamModel
					.getTeamSourceClassName());
			for (PropertyModel property : teamModel.getProperties()) {
				team.addProperty(property.getName(), property.getValue());
			}
		}
	}

}