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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;

/**
 * {@link ManagedFunctionSource} for first-class procedure.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Property} name providing the service to create the procedure.
	 */
	public static final String SERVICE_NAME_PROPERTY_NAME = "service.name";

	/**
	 * {@link Property} name identifying the procedure name.
	 */
	public static final String PROCEDURE_PROPERTY_NAME = "procedure";

	/*
	 * ================= ManagedFunctionSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement AbstractManagedFunctionSource.loadSpecification
		throw new UnsupportedOperationException("TODO implement AbstractManagedFunctionSource.loadSpecification");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {
		// TODO implement ManagedFunctionSource.sourceManagedFunctions
		throw new UnsupportedOperationException("TODO implement ManagedFunctionSource.sourceManagedFunctions");
	}

}