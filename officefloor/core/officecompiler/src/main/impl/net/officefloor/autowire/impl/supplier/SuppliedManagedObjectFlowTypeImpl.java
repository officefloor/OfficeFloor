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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link SuppliedManagedObjectFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectFlowTypeImpl implements
		SuppliedManagedObjectFlowType {

	/**
	 * Name of flow.
	 */
	private final String flowName;

	/**
	 * Name of {@link OfficeSection}.
	 */
	private final String sectionName;

	/**
	 * Name of {@link OfficeSectionInput}.
	 */
	private final String sectionInputName;

	/**
	 * Argument type.
	 */
	private final Class<?> argumentType;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of flow.
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param sectionInputName
	 *            Name of {@link OfficeSectionInput}.
	 * @param argumentType
	 *            Argument type.
	 */
	public SuppliedManagedObjectFlowTypeImpl(String flowName,
			String sectionName, String sectionInputName, Class<?> argumentType) {
		this.flowName = flowName;
		this.sectionName = sectionName;
		this.sectionInputName = sectionInputName;
		this.argumentType = argumentType;
	}

	/*
	 * ==================== SuppliedManagedObjectFlowType ====================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public String getSectionName() {
		return this.sectionName;
	}

	@Override
	public String getSectionInputName() {
		return this.sectionInputName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}