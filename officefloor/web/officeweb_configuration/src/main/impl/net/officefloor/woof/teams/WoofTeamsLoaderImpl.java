/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.teams;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.woof.model.teams.PropertyFileModel;
import net.officefloor.woof.model.teams.PropertyModel;
import net.officefloor.woof.model.teams.PropertySourceModel;
import net.officefloor.woof.model.teams.TypeQualificationModel;
import net.officefloor.woof.model.teams.WoofTeamModel;
import net.officefloor.woof.model.teams.WoofTeamsModel;
import net.officefloor.woof.model.teams.WoofTeamsRepository;

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
	 * @param repository {@link WoofTeamsRepository}.
	 */
	public WoofTeamsLoaderImpl(WoofTeamsRepository repository) {
		this.repository = repository;
	}

	/**
	 * {@link FunctionalInterface} to load a {@link WoofTeamModel}.
	 */
	@FunctionalInterface
	private static interface TeamLoader {

		/**
		 * Loads the {@link WoofTeamModel}.
		 * 
		 * @param teamName            Name of the {@link Team}.
		 * @param teamSize            Size of the {@link Team}.
		 * @param teamSourceClassName Name of the {@link TeamSource} {@link Class}.
		 * @param typeQualifications  {@link AutoWire} type qualifications for the
		 *                            {@link Team}.
		 * @param teamModel           {@link WoofTeamModel}.
		 * @throws Exception If fails to load the {@link Team}.
		 */
		void loadTeam(String teamName, int teamSize, String teamSourceClassName, List<AutoWire> typeQualifications,
				WoofTeamModel teamModel) throws Exception;
	}

	/**
	 * Generic method to load the {@link WoofTeamModel} instances.
	 * 
	 * @param getConfiguration    {@link Supplier} to obtain the
	 *                            {@link WoofTeamsModel} {@link ConfigurationItem}.
	 * @param enableAutoWireTeams {@link Runnable} to enable auto-wire of the
	 *                            {@link Team} instances.
	 * @param loader              {@link TeamLoader} to load the individual
	 *                            {@link Team} instances.
	 * @throws Exception If fails to load the {@link Team}.
	 */
	private void loadWoofTeams(Supplier<ConfigurationItem> getConfiguration, Runnable enableAutoWireTeams,
			TeamLoader loader) throws Exception {

		// Load the teams model
		WoofTeamsModel teams = new WoofTeamsModel();
		this.repository.retrieveWoofTeams(teams, getConfiguration.get());

		// Obtain the team models
		List<WoofTeamModel> teamModels = teams.getWoofTeams();

		// If teams, enable auto-wire teams
		if (teamModels.size() > 0) {
			enableAutoWireTeams.run();
		}

		// Configure the teams
		for (WoofTeamModel teamModel : teamModels) {

			// Obtain the team details
			int teamSize = teamModel.getTeamSize();
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

			// Load the team
			loader.loadTeam(teamName, teamSize, teamSourceClassName, typeQualifications, teamModel);
		}
	}

	/*
	 * ======================= WoofTeamsLoader ===========================
	 */

	@Override
	public void loadWoofTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception {

		// Obtain the deployer and extension context
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorExtensionContext extensionContext = context.getOfficeFloorExtensionContext();
		DeployedOffice office = context.getDeployedOffice();

		// Load the teams
		this.loadWoofTeams(() -> context.getConfiguration(), () -> deployer.enableAutoWireTeams(),
				(teamName, teamSize, teamSourceClassName, typeQualifications, teamModel) -> {

					// Add the team
					OfficeFloorTeam team = deployer.addTeam(teamName, teamSourceClassName);
					if (teamSize > 0) {
						team.setTeamSize(teamSize);
					}

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
							InputStream propertyConfiguration = extensionContext.getResource(propertyFile.getPath());
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

					// Direct link to office team
					OfficeTeam officeTeam = office.getDeployedOfficeTeam(teamName);
					deployer.link(officeTeam, team);
				});
	}

	@Override
	public void loadWoofTeamsUsage(WoofTeamsUsageContext context) throws Exception {

		// Obtain the architect and extension context
		OfficeArchitect architect = context.getOfficeArchitect();

		// Load the teams
		this.loadWoofTeams(() -> context.getConfiguration(), () -> architect.enableAutoWireTeams(),
				(teamName, teamSize, teamSourceClassName, typeQualifications, teamModel) -> {

					// Add the team
					OfficeTeam team = architect.addOfficeTeam(teamName);

					// Load the type qualification
					for (AutoWire autoWire : typeQualifications) {
						team.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
					}
				});
	}

}
