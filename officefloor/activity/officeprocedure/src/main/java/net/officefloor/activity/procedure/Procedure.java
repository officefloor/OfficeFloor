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
package net.officefloor.activity.procedure;

import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * Procedure.
 * 
 * @author Daniel Sagenschneider
 */
public interface Procedure {

	/**
	 * Obtains the name of the {@link ProcedureSource}.
	 * 
	 * @return Name of the {@link ProcedureSource}.
	 */
	String getServiceName();

	/**
	 * Obtains the name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}. May be <code>null</code> to indicate
	 *         manually selected.
	 */
	String getProcedureName();

	/**
	 * Obtains the specification of properties for the {@link Procedure}.
	 * 
	 * @return Property specification.
	 */
	ProcedureProperty[] getProperties();

}