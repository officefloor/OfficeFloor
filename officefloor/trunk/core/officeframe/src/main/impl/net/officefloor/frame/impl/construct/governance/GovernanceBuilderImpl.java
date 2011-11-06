/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<I, F extends Enum<F>> implements
		GovernanceBuilder, GovernanceConfiguration<I, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super I, F> governanceFactory;

	/**
	 * Extension interface.
	 */
	private final Class<I> extensionInterface;

	/**
	 * {@link Team} name responsible to undertake the {@link Governance}
	 * {@link Task} instances.
	 */
	private String teamName;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param extensionInterface
	 *            Extension interface.
	 */
	public GovernanceBuilderImpl(String governanceName,
			GovernanceFactory<? super I, F> governanceFactory,
			Class<I> extensionInterface) {
		this.governanceName = governanceName;
		this.governanceFactory = governanceFactory;
		this.extensionInterface = extensionInterface;
	}

	/*
	 * ================= GovernanceBuilder =======================
	 */

	@Override
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<I> getExtensionInterface() {
		return this.extensionInterface;
	}

	@Override
	public String getTeamName() {
		return this.teamName;
	}

}