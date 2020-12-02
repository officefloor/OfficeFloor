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
