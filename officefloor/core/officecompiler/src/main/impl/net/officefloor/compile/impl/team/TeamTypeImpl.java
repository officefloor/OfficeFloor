/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.team;

import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link TeamType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamTypeImpl implements TeamType {

	/**
	 * Indicates if require {@link Team} size.
	 */
	private final boolean isRequireTeamSize;

	/**
	 * Instantiate.
	 * 
	 * @param isRequireTeamSize Indicates if require {@link Team} size.
	 */
	public TeamTypeImpl(boolean isRequireTeamSize) {
		this.isRequireTeamSize = isRequireTeamSize;
	}

	/*
	 * ============== TeamType ====================
	 */

	@Override
	public boolean isRequireTeamSize() {
		return this.isRequireTeamSize;
	}

}
