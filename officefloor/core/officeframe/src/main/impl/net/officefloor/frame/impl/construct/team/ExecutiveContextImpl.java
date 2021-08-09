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

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.TeamOversight;
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
	 * Indicates if requested no {@link TeamOversight}.
	 */
	private final boolean isRequestNoTeamOversight;

	/**
	 * {@link Team} size.
	 */
	private final int teamSize;

	/**
	 * Indicates if the {@link Team} size is required.
	 */
	private boolean isRequireTeamSize = false;

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
	 * @param teamSize                  {@link Team} size. Value of 0 or below
	 *                                  indicates no {@link Team} size configured.
	 * @param teamSource                {@link TeamSource}.
	 * @param executive                 {@link Executive}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 * @param properties                {@link SourceProperties} to initialise the
	 *                                  {@link TeamSource}.
	 * @param sourceContext             {@link SourceContext}.
	 */
	public ExecutiveContextImpl(boolean isLoadingType, String teamName, boolean isRequestNoTeamOversight, int teamSize,
			TeamSource teamSource, Executive executive, ThreadFactoryManufacturer threadFactoryManufacturer,
			SourceProperties properties, SourceContext sourceContext) {
		super(teamName, isLoadingType, new String[0], sourceContext, properties);
		this.teamName = teamName;
		this.isRequestNoTeamOversight = isRequestNoTeamOversight;
		this.teamSize = teamSize;
		this.teamSource = teamSource;
		this.executive = executive;
		this.threadFactoryManufacturer = threadFactoryManufacturer;
	}

	/**
	 * Indicates if required {@link Team} size.
	 * 
	 * @return <code>true</code> if the {@link Team} size is required.
	 */
	public boolean isRequireTeamSize() {
		return this.isRequireTeamSize;
	}

	/*
	 * ===================== TeamSourceContext =========================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public boolean isRequestNoTeamOversight() {
		return this.isRequestNoTeamOversight;
	}

	@Override
	public int getTeamSize() {

		// Flag that requires team size
		this.isRequireTeamSize = true;

		// Return the team size
		return this.teamSize;
	}

	@Override
	public int getTeamSize(int defaultSize) {
		return (this.teamSize > 0) ? this.teamSize : defaultSize;
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
