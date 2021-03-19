/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.type;

import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;

/**
 * Tests loading the {@link SuppliedManagedObjectSourceType} from the
 * {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectSourceTypeContextTest
		extends AbstractTestTypeContext<SuppliedManagedObjectSourceNode, SuppliedManagedObjectSourceType> {

	/**
	 * Instantiate.
	 */
	public SuppliedManagedObjectSourceTypeContextTest() {
		super(SuppliedManagedObjectSourceNode.class, SuppliedManagedObjectSourceType.class,
				(context, node) -> node.loadSuppliedManagedObjectSourceType(context),
				(context, node) -> context.getOrLoadSuppliedManagedObjectSourceType(node));
	}

}
