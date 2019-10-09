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
package net.officefloor.activity.procedure.section;

import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcedureManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(ProcedureManagedFunctionSource.class,
				ProcedureManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, "Class",
				ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME, "Service",
				ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME, "Procedure");
	}

}