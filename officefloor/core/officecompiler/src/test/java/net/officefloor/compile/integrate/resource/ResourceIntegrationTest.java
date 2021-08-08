/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests making the {@link ResourceSource} available to the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Resource.
	 */
	private static InputStream namespaceResource;

	/**
	 * Ensure the {@link SourceContext} of {@link OfficeFrame} contains the
	 * {@link ClassLoader} and {@link ResourceSource} instances.
	 */
	public void testIntegrateSourceContext() throws Exception {

		final ResourceSource resourceSource = this.createMock(ResourceSource.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Reset test
		namespaceResource = null;

		// Record (multiple times as loading managed object type)
		for (int i = 0; i < 3; i++) {
			this.recordReturn(resourceSource, resourceSource.sourceResource("REQUIRED RESOURCE"), resource);
		}

		// Test
		this.replayMockObjects();

		// Configure OfficeFloor
		CompileOffice compile = new CompileOffice();
		compile.getOfficeFloorCompiler().addResources(resourceSource);
		OfficeFloor officeFloor = compile.compileAndOpenOffice((architect, context) -> {
			architect.enableAutoWireObjects();
			architect.addOfficeManagedObjectSource("CLASS_LOADER", ClassLoaderManagedObjectSource.class.getName())
					.addOfficeManagedObject("CLASS_LOADER", ManagedObjectScope.THREAD);
			architect.addOfficeManagedObjectSource("RESOURCE", ResourceManagedObjectSource.class.getName())
					.addOfficeManagedObject("RESOURCE", ManagedObjectScope.PROCESS);
			architect.addOfficeSection("SECTION", ClassSectionSource.class.getName(), ResourceClass.class.getName());
		});

		// Invoke the function
		officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function").invokeProcess(null, null);

		// Ensure correct resources
		assertSame("Incorrect resource", resource, namespaceResource);

		// Close OfficeFloor
		officeFloor.closeOfficeFloor();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Resource {@link Class}.
	 */
	public static class ResourceClass {
		public void function(ClassLoader classLoader, InputStream resource) {
			namespaceResource = resource;
		}
	}

	/**
	 * {@link ManagedObjectSource} to obtain the {@link ClassLoader}.
	 */
	public static class ClassLoaderManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private ClassLoader classLoader;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();
			context.setObjectClass(ClassLoader.class);
			this.classLoader = mosContext.getClassLoader();
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.classLoader;
		}
	}

	/**
	 * {@link ManagedObjectSource} to obtain the resource.
	 */
	public static class ResourceManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private InputStream resource;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();
			context.setObjectClass(InputStream.class);
			this.resource = mosContext.getResource("REQUIRED RESOURCE");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.resource;
		}
	}

}
