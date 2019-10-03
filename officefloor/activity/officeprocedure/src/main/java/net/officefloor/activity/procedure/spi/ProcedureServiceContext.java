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
package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceFactory;

/**
 * Context for the {@link ProcedureService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureServiceContext {

	/**
	 * Obtains the {@link Class} configured to provide the {@link Procedure}.
	 * 
	 * @return {@link Class} configured to provide the {@link Procedure}.
	 */
	Class<?> getInstanceClass();

	/**
	 * Name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * <p>
	 * Overrides the default {@link MethodObjectInstanceFactory}.
	 * <p>
	 * Specifying <code>null</code> indicates a static {@link Method}.
	 * 
	 * @param factory {@link MethodObjectInstanceFactory}.
	 */
	void setMethodObjectInstanceFactory(MethodObjectInstanceFactory factory);

}