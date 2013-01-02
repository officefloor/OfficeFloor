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
package net.officefloor.plugin.teams;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.AutoWireModel;
import net.officefloor.model.teams.AutoWireTeamModel;
import net.officefloor.model.teams.AutoWireTeamsModel;
import net.officefloor.model.teams.AutoWireTeamsRepository;
import net.officefloor.model.teams.PropertyFileModel;
import net.officefloor.model.teams.PropertyModel;
import net.officefloor.model.teams.PropertySourceModel;

/**
 * {@link AutoWireTeamsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeamsLoaderImpl implements AutoWireTeamsLoader {

	/**
	 * {@link AutoWireTeamsRepository}.
	 */
	private final AutoWireTeamsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link AutoWireTeamsRepository}.
	 */
	public AutoWireTeamsLoaderImpl(AutoWireTeamsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= AutoWireTeamsLoader ===========================
	 */

	@Override
	public void loadAutoWireTeamsConfiguration(
			ConfigurationItem teamsConfiguration,
			AutoWireApplication application) throws Exception {

		// Load the teams model
		AutoWireTeamsModel teams = this.repository
				.retrieveAutoWireTeams(teamsConfiguration);

		// Configure the teams
		for (AutoWireTeamModel teamModel : teams.getAutoWireTeams()) {

			// Obtain the team details
			String teamSourceClassName = teamModel.getTeamSourceClassName();

			// Obtain the auto-wiring
			List<AutoWire> autoWiring = new LinkedList<AutoWire>();
			String qualifier = teamModel.getQualifier();
			String type = teamModel.getType();
			if (!(CompileUtil.isBlank(type))) {
				// Short-cut auto-wire provided
				autoWiring.add(new AutoWire(qualifier, type));
			}
			for (AutoWireModel autoWire : teamModel.getAutoWiring()) {
				autoWiring.add(new AutoWire(autoWire.getQualifier(), autoWire
						.getType()));
			}

			// Assign the team
			AutoWireTeam team = application.assignTeam(teamSourceClassName,
					autoWiring.toArray(new AutoWire[autoWiring.size()]));

			// Load the properties
			for (PropertySourceModel propertySource : teamModel
					.getPropertySources()) {

				// Load based on property source type
				if (propertySource instanceof PropertyModel) {
					// Load the property
					PropertyModel property = (PropertyModel) propertySource;
					team.addProperty(property.getName(), property.getValue());

				} else if (propertySource instanceof PropertyFileModel) {
					// Load properties from file
					PropertyFileModel propertyFile = (PropertyFileModel) propertySource;
					team.loadProperties(propertyFile.getPath());

				} else {
					// Unknown property source
					throw new IllegalStateException(
							"Unknown property source type "
									+ propertySource.getClass().getName());
				}
			}
		}
	}

}