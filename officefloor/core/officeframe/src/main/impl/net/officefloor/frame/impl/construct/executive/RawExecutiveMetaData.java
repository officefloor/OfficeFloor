/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.executive;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * Raw {@link Executive} meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaData {

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * {@link Map} of {@link ExecutionStrategy} name to its {@link ThreadFactory}
	 * instances.
	 */
	private final Map<String, ThreadFactory[]> executionStrategies;

	/**
	 * {@link TeamOversight}.
	 */
	private final TeamOversight teamOversight;

	/**
	 * Instantiate.
	 * 
	 * @param executive           {@link Exception}.
	 * @param executionStrategies {@link Map} of {@link ExecutionStrategy} name to
	 *                            its {@link ThreadFactory} instances.
	 * @param teamOversight       {@link TeamOversight} .
	 */
	public RawExecutiveMetaData(Executive executive, Map<String, ThreadFactory[]> executionStrategies,
			TeamOversight teamOversight) {
		this.executive = executive;
		this.executionStrategies = executionStrategies;
		this.teamOversight = teamOversight;
	}

	/**
	 * Obtains the {@link Executive}.
	 * 
	 * @return {@link Executive}.
	 */
	public Executive getExecutive() {
		return this.executive;
	}

	/**
	 * Obtains the {@link ExecutionStrategy} instances by their names.
	 * 
	 * @return {@link ExecutionStrategy} instances by their names.
	 */
	public Map<String, ThreadFactory[]> getExecutionStrategies() {
		return this.executionStrategies;
	}

	/**
	 * Obtains the {@link TeamOversight}.
	 * 
	 * @return {@link TeamOversight}.
	 */
	public TeamOversight getTeamOversight() {
		return this.teamOversight;
	}

}
