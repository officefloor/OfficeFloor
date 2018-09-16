/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;

/**
 * {@link ExecutiveContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveContextImpl extends SourceContextImpl implements ExecutiveContext {

	/**
	 * Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	private final String teamName;

	/**
	 * {@link Team} size.
	 */
	private final int teamSize;

	/**
	 * {@link TeamSource}.
	 */
	private final TeamSource teamSource;

	/**
	 * {@link ThreadFactoryManufacturer}.
	 */
	private final ThreadFactoryManufacturer threadFactoryManufacturer;

	/**
	 * {@link Executive}.
	 */
	private Executive executive;

	/**
	 * {@link ThreadFactory}.
	 */
	private ThreadFactory threadFactory = null;

	/**
	 * Initialise.
	 * 
	 * @param isLoadingType             Indicates if loading type.
	 * @param teamName                  Name of the {@link Team} to be created from
	 *                                  the {@link TeamSource}.
	 * @param teamSize                  {@link Team} size.
	 * @param teamSource                {@link TeamSource}.
	 * @param executive                 {@link Executive}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 * @param properties                {@link SourceProperties} to initialise the
	 *                                  {@link TeamSource}.
	 * @param sourceContext             {@link SourceContext}.
	 */
	public ExecutiveContextImpl(boolean isLoadingType, String teamName, int teamSize, TeamSource teamSource,
			Executive executive, ThreadFactoryManufacturer threadFactoryManufacturer, SourceProperties properties,
			SourceContext sourceContext) {
		super(isLoadingType, sourceContext, properties);
		this.teamName = teamName;
		this.teamSize = teamSize;
		this.teamSource = teamSource;
		this.executive = executive;
		this.threadFactoryManufacturer = threadFactoryManufacturer;
	}

	/*
	 * ===================== TeamSourceContext =========================
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
	public ThreadFactory getThreadFactory() {
		if (this.threadFactory == null) {
			this.threadFactory = this.threadFactoryManufacturer.manufactureThreadFactory(this.teamName, this.executive);
		}
		return this.threadFactory;
	}

	@Override
	public TeamSource getTeamSource() {
		return this.teamSource;
	}

	@Override
	public ThreadFactory createThreadFactory(String teamName) {
		return this.threadFactoryManufacturer.manufactureThreadFactory(teamName, this.executive);
	}

}