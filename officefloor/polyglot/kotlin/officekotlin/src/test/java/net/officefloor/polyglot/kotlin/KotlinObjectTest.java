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
package net.officefloor.polyglot.kotlin;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.polyglot.test.AbstractPolyglotObjectTest;
import net.officefloor.polyglot.test.ObjectInterface;

/**
 * Tests adapting Kotlin {@link Object} for {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class KotlinObjectTest extends AbstractPolyglotObjectTest {

	@Override
	protected ObjectInterface create() {
		return new KotlinObject();
	}

	@Override
	protected void object(CompileOfficeContext context) {
		context.addManagedObject("OBJECT", KotlinObject.class, ManagedObjectScope.THREAD);
	}

}