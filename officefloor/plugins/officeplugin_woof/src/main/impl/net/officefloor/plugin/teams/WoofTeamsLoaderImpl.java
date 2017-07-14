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

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.model.teams.PropertyFileModel;
import net.officefloor.model.teams.PropertyModel;
import net.officefloor.model.teams.PropertySourceModel;
import net.officefloor.model.teams.TypeQualificationModel;
import net.officefloor.model.teams.WoofTeamModel;
import net.officefloor.model.teams.WoofTeamsModel;
import net.officefloor.model.teams.WoofTeamsRepository;

/**
 * {@link WoofTeamsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsLoaderImpl implements WoofTeamsLoader {

	/**
	 * {@link WoofTeamsRepository}.
	 */
	private final WoofTeamsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link WoofTeamsRepository}.
	 */
	public WoofTeamsLoaderImpl(WoofTeamsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= WoofTeamsLoader ===========================
	 */

	@Override
	public void loadAutoWireTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception {

		// Load the teams model
		WoofTeamsModel teams = this.repository.retrieveAutoWireTeams(context.getConfiguration());

		// Configure the teams
		for (WoofTeamModel teamModel : teams.getWoofTeams()) {

			// Obtain the team details
			String teamSourceClassName = teamModel.getTeamSourceClassName();

			// Obtain the type qualification
			List<AutoWire> typeQualifications = new LinkedList<AutoWire>();
			String qualifier = teamModel.getQualifier();
			String type = teamModel.getType();
			if (!(CompileUtil.isBlank(type))) {
				// Short-cut type qualification provided
				typeQualifications.add(new AutoWire(qualifier, type));
			}
			for (TypeQualificationModel autoWire : teamModel.getTypeQualifications()) {
				typeQualifications.add(new AutoWire(autoWire.getQualifier(), autoWire.getType()));
			}

			// Obtain the team name
			String teamName = (typeQualifications.size() > 0 ? typeQualifications.get(0).toString()
					: teamSourceClassName);

			// Add the team
			OfficeFloorTeam team = context.getOfficeFloorDeployer().addTeam(teamName, teamSourceClassName);

			// Load the type qualification
			for (AutoWire autoWire : typeQualifications) {
				team.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
			}

			// Load the properties
			for (PropertySourceModel propertySource : teamModel.getPropertySources()) {

				// Load based on property source type
				if (propertySource instanceof PropertyModel) {
					// Load the property
					PropertyModel property = (PropertyModel) propertySource;
					team.addProperty(property.getName(), property.getValue());

				} else if (propertySource instanceof PropertyFileModel) {
					// Load properties from file
					PropertyFileModel propertyFile = (PropertyFileModel) propertySource;
					InputStream propertyConfiguration = context.getOfficeFloorExtensionContext()
							.getResource(propertyFile.getPath());
					Properties properties = new Properties();
					properties.load(propertyConfiguration);
					for (String propertyName : properties.stringPropertyNames()) {
						String propertyValue = properties.getProperty(propertyName);
						team.addProperty(propertyName, propertyValue);
					}

				} else {
					// Unknown property source
					throw new IllegalStateException(
							"Unknown property source type " + propertySource.getClass().getName());
				}
			}
		}
	}

}