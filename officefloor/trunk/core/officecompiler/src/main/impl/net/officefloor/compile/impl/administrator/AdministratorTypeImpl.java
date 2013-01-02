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

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;

/**
 * {@link AdministratorType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorTypeImpl<I, A extends Enum<A>> implements
		AdministratorType<I, A> {

	/**
	 * Extension interface.
	 */
	private final Class<I> extensionInterface;

	/**
	 * {@link DutyType} instances.
	 */
	private final DutyType<A, ?>[] dutyTypes;

	/**
	 * Initiate.
	 * 
	 * @param extensionInterface
	 *            Extension interface.
	 * @param dutyTypes
	 *            {@link DutyType} instances.
	 */
	public AdministratorTypeImpl(Class<I> extensionInterface,
			DutyType<A, ?>[] dutyTypes) {
		this.extensionInterface = extensionInterface;
		this.dutyTypes = dutyTypes;
	}

	/*
	 * ==================== AdministratorType ================================
	 */

	@Override
	public Class<I> getExtensionInterface() {
		return this.extensionInterface;
	}

	@Override
	public DutyType<A, ?>[] getDutyTypes() {
		return this.dutyTypes;
	}

}