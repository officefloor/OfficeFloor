/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureProperty;

/**
 * {@link ProcedureProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedurePropertyImpl implements ProcedureProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name  Name of property.
	 * @param label Label of property.
	 */
	public ProcedurePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * ================= ProcedureProperty ======================
	 */

	@Override
	public String getName() {
		// TODO implement ProcedureProperty.getName
		throw new UnsupportedOperationException("TODO implement ProcedureProperty.getName");
	}

	@Override
	public String getLabel() {
		// TODO implement ProcedureProperty.getLabel
		throw new UnsupportedOperationException("TODO implement ProcedureProperty.getLabel");
	}

}