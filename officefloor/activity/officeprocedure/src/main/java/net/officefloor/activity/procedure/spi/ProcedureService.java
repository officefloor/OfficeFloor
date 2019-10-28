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

/**
 * Service providing {@link Procedure} adaptation.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureService {

	/**
	 * <p>
	 * Name of this service.
	 * <p>
	 * Note that this is the name used in configuration to identify this
	 * {@link ProcedureService}. Therefore, it can not change without causing
	 * configurations to be updated.
	 * <p>
	 * The reasons for using this logical name over {@link Class} names is:
	 * <ul>
	 * <li>Class names can be quite long</li>
	 * <li>Class names are not easily readable</li>
	 * <li>Enables swapping plugins for same logical service name</li>
	 * </ul>
	 * 
	 * @return Name of this service.
	 */
	String getServiceName();

	/**
	 * Provides the available {@link Procedure} instances for the resource.
	 * 
	 * @param context {@link ProcedureListContext}.
	 * @throws Exception If fails to list {@link Procedure} instances.
	 */
	void listProcedures(ProcedureListContext context) throws Exception;

	/**
	 * Loads the {@link Method} for the {@link Procedure}.
	 * 
	 * @param context {@link ProcedureMethodContext}.
	 * @return {@link Method} for the {@link ProcedureService}.
	 * @throws Exception If fails to load the method.
	 */
	Method loadMethod(ProcedureMethodContext context) throws Exception;

}