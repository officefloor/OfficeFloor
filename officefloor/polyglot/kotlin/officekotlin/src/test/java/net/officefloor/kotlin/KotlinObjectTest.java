/*-
 * #%L
 * Kotlin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.kotlin;

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
