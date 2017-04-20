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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link AdministrationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationTypeImpl<E, F extends Enum<F>, G extends Enum<G>> implements AdministrationType<E, F, G> {

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link Flow} key {@link Enum}.
	 */
	private final Class<F> flowKeyClass;

	/**
	 * {@link Governance} key {@link Enum}.
	 */
	private final Class<G> governanceKeyClass;

	/**
	 * Initiate.
	 * 
	 * @param extensionInterface
	 *            Extension interface.
	 * @param flowKeyClass
	 *            {@link Flow} key {@link Enum}.
	 */
	public AdministrationTypeImpl(Class<E> extensionInterface, Class<F> flowKeyClass, Class<G> governanceKeyClass) {
		this.extensionInterface = extensionInterface;
		this.flowKeyClass = flowKeyClass;
		this.governanceKeyClass = governanceKeyClass;
	}

	/*
	 * ==================== AdministrationType ================================
	 */

	@Override
	public Class<E> getExtensionInterface() {
		return this.extensionInterface;
	}

	@Override
	public AdministrationFactory<E, F, G> getAdministrationFactory() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Class<F> getFlowKeyClass() {
		return this.flowKeyClass;
	}

	@Override
	public AdministrationFlowType<F>[] getFlowTypes() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public AdministrationEscalationType[] getEscalationTypes() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Class<G> getGovernanceKeyClass() {
		return this.governanceKeyClass;
	}

	@Override
	public AdministrationGovernanceType<G>[] getGovernanceTypes() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

}