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

package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.TeamConfiguration;

/**
 * Implements the {@link TeamBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamBuilderImpl<TS extends TeamSource> implements TeamBuilder<TS>, TeamConfiguration<TS> {

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link Team} size.
	 */
	private int teamSize = 0;

	/**
	 * Indicates if requesting no {@link TeamOversight}.
	 */
	private boolean isRequestNoTeamOversight = false;

	/**
	 * {@link TeamSource}.
	 */
	private final TS teamSource;

	/**
	 * {@link Class} of the {@link TeamSource}.
	 */
	private final Class<TS> teamSourceClass;

	/**
	 * {@link SourceProperties} for initialising the {@link TeamSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Initiate.
	 * 
	 * @param teamName   Name of the {@link Team}.
	 * @param teamSource {@link TeamSource}.
	 */
	public TeamBuilderImpl(String teamName, TS teamSource) {
		this.teamName = teamName;
		this.teamSource = teamSource;
		this.teamSourceClass = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param teamName        Name of the {@link Team}.
	 * @param teamSourceClass {@link Class} of the {@link TeamSource}.
	 */
	public TeamBuilderImpl(String teamName, Class<TS> teamSourceClass) {
		this.teamName = teamName;
		this.teamSource = null;
		this.teamSourceClass = teamSourceClass;
	}

	/*
	 * ====================== TeamBuilder ================================
	 */

	@Override
	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	@Override
	public void requestNoTeamOversight() {
		this.isRequestNoTeamOversight = true;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/*
	 * ====================== TeamConfiguration =============================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public int getTeamSize() {
		return this.teamSize;
	}

	@Override
	public boolean isRequestNoTeamOversight() {
		return this.isRequestNoTeamOversight;
	}

	@Override
	public TS getTeamSource() {
		return this.teamSource;
	}

	@Override
	public Class<TS> getTeamSourceClass() {
		return this.teamSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

}
