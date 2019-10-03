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

import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.frame.api.build.Indexed;

/**
 * Loader for {@link ProcedureManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureLoader {

	/**
	 * Lists the available {@link Procedure} instances from the {@link Class}.
	 * 
	 * @param clazz {@link Class}.
	 * @return Listing of available {@link Procedure} instances or <code>null</code>
	 *         with issues reported to {@link CompilerIssues}.
	 */
	Procedure[] listProcedures(Class<?> clazz);

	/**
	 * Loads the {@link ManagedFunctionType} for the {@link Procedure} of the
	 * {@link Class}.
	 * 
	 * @param clazz         {@link Class}.
	 * @param procedureName {@link Procedure} name.
	 * @param serviceName   {@link ProcedureService} name.
	 * @return {@link ManagedFunctionType} for the {@link Procedure} or
	 *         <code>null</code> with issues reported to {@link CompilerIssues}.
	 */
	ManagedFunctionType<Indexed, Indexed> loadProcedureType(Class<?> clazz, String procedureName, String serviceName);

}