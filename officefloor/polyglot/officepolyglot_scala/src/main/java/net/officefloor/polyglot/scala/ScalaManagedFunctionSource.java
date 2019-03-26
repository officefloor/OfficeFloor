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
package net.officefloor.polyglot.scala;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;

/**
 * Scala {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaManagedFunctionSource extends AbstractManagedFunctionSource {

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