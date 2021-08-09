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

package net.officefloor.woof.model.teams;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link WoofTeamsRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsRepositoryImpl implements WoofTeamsRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public WoofTeamsRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== WoofTeamsRepository ====================
	 */

	@Override
	public void retrieveWoofTeams(WoofTeamsModel teams, ConfigurationItem configuration) throws Exception {
		this.modelRepository.retrieve(teams, configuration);
	}

	@Override
	public void storeWoofTeams(WoofTeamsModel teams, WritableConfigurationItem configuration) throws Exception {
		this.modelRepository.store(teams, configuration);
	}

}
