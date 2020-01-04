/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureProperty;
import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * {@link Procedure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureImpl implements Procedure {

	/**
	 * Procedure name.
	 */
	private final String procedureName;

	/**
	 * {@link ProcedureSource} name.
	 */
	private final String serviceName;

	/**
	 * {@link ProcedureProperty} instances.
	 */
	private final ProcedureProperty[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param procedureName Procedure name.
	 * @param serviceName   {@link ProcedureSource} name.
	 * @param properties    {@link ProcedureProperty} instances.
	 */
	public ProcedureImpl(String procedureName, String serviceName, ProcedureProperty[] properties) {
		this.procedureName = procedureName;
		this.serviceName = serviceName;
		this.properties = properties;
	}

	/*
	 * =================== Procedure ======================
	 */

	@Override
	public String getProcedureName() {
		return this.procedureName;
	}

	@Override
	public String getServiceName() {
		return this.serviceName;
	}

	@Override
	public ProcedureProperty[] getProperties() {
		return this.properties;
	}

}
