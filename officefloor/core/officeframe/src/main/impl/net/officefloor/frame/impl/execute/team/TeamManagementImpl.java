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

package net.officefloor.frame.impl.execute.team;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link TeamManagement} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamManagementImpl implements TeamManagement {

	/**
	 * Identifier for the {@link Team} under this {@link TeamManagement}.
	 */
	private final Object teamIdentifier = new Object();

	/**
	 * {@link Team} under this {@link TeamManagement}.
	 */
	private final Team team;

	/**
	 * Initiate.
	 * 
	 * @param team
	 *            {@link Team} under this {@link TeamManagement}.
	 */
	public TeamManagementImpl(Team team) {
		this.team = team;
	}

	/*
	 * ====================== TeamManagement ================================
	 */

	@Override
	public Object getIdentifier() {
		return this.teamIdentifier;
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

}
