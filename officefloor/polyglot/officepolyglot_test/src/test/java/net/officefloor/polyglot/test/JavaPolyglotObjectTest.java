/*-
 * #%L
 * Polyglot Test
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

package net.officefloor.polyglot.test;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Confirms the tests with {@link ClassManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaPolyglotObjectTest extends AbstractPolyglotObjectTest {

	@Override
	protected ObjectInterface create() {
		return new ObjectInterfaceImpl();
	}

	@Override
	protected void object(CompileOfficeContext context) {
		context.addManagedObject("OBJECT", ObjectInterfaceImpl.class, ManagedObjectScope.THREAD);
	}

	public static class ObjectInterfaceImpl implements ObjectInterface {

		@Dependency
		private JavaObject dependency;

		@Override
		public String getValue() {
			return "test";
		}

		@Override
		public JavaObject getDependency() {
			return this.dependency;
		}
	}

}
