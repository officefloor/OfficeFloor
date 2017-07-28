/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.impl.construct.function.AbstractFunctionBuilder;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<E, F extends Enum<F>> extends AbstractFunctionBuilder<F>
		implements GovernanceBuilder<F>, GovernanceConfiguration<E, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super E, F> governanceFactory;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 */
	public GovernanceBuilderImpl(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory) {
		this.governanceName = governanceName;
		this.extensionInterface = extensionInterface;
		this.governanceFactory = governanceFactory;
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super E, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<E> getExtensionInterface() {
		return this.extensionInterface;
	}

}