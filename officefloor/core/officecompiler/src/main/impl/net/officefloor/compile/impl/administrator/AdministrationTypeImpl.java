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
	 * Initiate.
	 * 
	 * @param extensionInterface
	 *            Extension interface.
	 */
	public AdministrationTypeImpl(Class<E> extensionInterface) {
		this.extensionInterface = extensionInterface;
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
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
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
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public AdministrationGovernanceType<G>[] getGovernanceTypes() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

}