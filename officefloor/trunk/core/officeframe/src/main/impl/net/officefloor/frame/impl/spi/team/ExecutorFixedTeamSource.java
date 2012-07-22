/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;

/**
 * {@link TeamSource} utilising a fixed {@link ExecutorService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutorFixedTeamSource extends AbstractExecutorTeamSource {

	/**
	 * Indicates maximum number of {@link Thread} instances.
	 */
	public static final String PROPERTY_TEAM_SIZE = "team.size";

	/*
	 * ===================== AbstractExecutorTeamSource =====================
	 */

	@Override
	protected ExecutorServiceFactory createExecutorServiceFactory(
			TeamSourceContext context) throws Exception {

		// Obtain the team size
		final int teamSize = Integer.valueOf(context
				.getProperty(PROPERTY_TEAM_SIZE));

		// Create and return the factory
		return new ExecutorServiceFactory() {
			@Override
			public ExecutorService createExecutorService() {
				return Executors.newFixedThreadPool(teamSize);
			}
		};
	}

}