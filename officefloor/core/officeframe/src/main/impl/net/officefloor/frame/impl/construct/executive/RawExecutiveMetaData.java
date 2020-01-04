/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	 * {@link TeamOversight} instances by their names.
	 */
	private final Map<String, TeamOversight> teamOversights;

	/**
	 * Instantiate.
	 * 
	 * @param executive           {@link Exception}.
	 * @param executionStrategies {@link Map} of {@link ExecutionStrategy} name to
	 *                            its {@link ThreadFactory} instances.
	 * @param teamOversights      {@link TeamOversight} instances by their names.
	 */
	public RawExecutiveMetaData(Executive executive, Map<String, ThreadFactory[]> executionStrategies,
			Map<String, TeamOversight> teamOversights) {
		this.executive = executive;
		this.executionStrategies = executionStrategies;
		this.teamOversights = teamOversights;
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
	 * Obtains the {@link TeamOversight} instances by their name.
	 * 
	 * @return {@link TeamOversight} instances by their name.
	 */
	public Map<String, TeamOversight> getTeamOversights() {
		return this.teamOversights;
	}

}
