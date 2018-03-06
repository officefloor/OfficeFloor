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
package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceTypeImpl<I, F extends Enum<F>> implements
		GovernanceType<I, F> {

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? extends I, F> governanceFactory;

	/**
	 * Extension interface.
	 */
	private final Class<I> extensionInterface;

	/**
	 * {@link GovernanceFlowType} instances.
	 */
	private final GovernanceFlowType<F>[] flowTypes;

	/**
	 * {@link GovernanceEscalationType} instances.
	 */
	private final GovernanceEscalationType[] escalationTypes;

	/**
	 * Initiate.
	 * 
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param flowTypes
	 *            {@link GovernanceFlowType} instances.
	 * @param escalationTypes
	 *            {@link GovernanceEscalationType} instances.
	 */
	public GovernanceTypeImpl(
			GovernanceFactory<? extends I, F> governanceFactory,
			Class<I> extensionInterface, GovernanceFlowType<F>[] flowTypes,
			GovernanceEscalationType[] escalationTypes) {
		this.governanceFactory = governanceFactory;
		this.extensionInterface = extensionInterface;
		this.flowTypes = flowTypes;
		this.escalationTypes = escalationTypes;
	}

	/*
	 * ======================== GovernanceType ======================
	 */

	@Override
	public GovernanceFactory<? extends I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<I> getExtensionType() {
		return this.extensionInterface;
	}

	@Override
	public GovernanceFlowType<F>[] getFlowTypes() {
		return this.flowTypes;
	}

	@Override
	public GovernanceEscalationType[] getEscalationTypes() {
		return this.escalationTypes;
	}

}