/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to override {@link ClassLoader} and add {@link ResourceSource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceIntegrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure obtain default class loader.
	 */
	public void testDefaultClassLoader() throws Exception {

		// Construct the managed object
		this.constructManagedObject("MO", ClassLoaderManagedObjectSource.class, this.getOfficeName());

		// Construct work to obtain the Class Loader
		ClassLoaderWork classLoaderWork = new ClassLoaderWork();
		ReflectiveFunctionBuilder task = this.constructFunction(classLoaderWork, "validateClassLoader");
		task.buildObject("MO", ManagedObjectScope.PROCESS);

		// Invoke function and ensure correct class loader
		this.invokeFunction("validateClassLoader", null);

		// Ensure correct default class loader
		assertSame("Incorrect class loader", Thread.currentThread().getContextClassLoader(),
				classLoaderWork.classLoader);
	}

	/**
	 * Ensure override class loader.
	 */
	public void testOverrideClassLoader() throws Exception {

		final ClassLoader classLoader = new URLClassLoader(new URL[0]);

		// Override the Class Loader
		this.getOfficeFloorBuilder().setClassLoader(classLoader);

		// Construct the managed object
		this.constructManagedObject("MO", ClassLoaderManagedObjectSource.class, this.getOfficeName());

		// Construct work to obtain the Class Loader
		ClassLoaderWork classLoaderWork = new ClassLoaderWork();
		ReflectiveFunctionBuilder task = this.constructFunction(classLoaderWork, "validateClassLoader");
		task.buildObject("MO", ManagedObjectScope.PROCESS);

		// Invoke function and ensure correct class loader
		this.invokeFunction("validateClassLoader", null);

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader, classLoaderWork.classLoader);
	}

	/**
	 * {@link ClassLoader} functionality.
	 */
	public static class ClassLoaderWork {

		/**
		 * Actual {@link ClassLoader}.
		 */
		public ClassLoader classLoader = null;

		/**
		 * Validates the {@link ClassLoader}.
		 * 
		 * @param actual
		 *            {@link ClassLoader}.
		 */
		public void validateClassLoader(ClassLoader actual) {
			this.classLoader = actual;
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
	 * Ensure able to locate a resource through a {@link ResourceSource}.
	 */
	public void testAdditionalResourceSource() throws Exception {

		final InputStream RESOURCE = new ByteArrayInputStream(new byte[0]);
		final ResourceSource resourceSource = this.createMock(ResourceSource.class);

		// Record obtaining the resource
		this.recordReturn(resourceSource, resourceSource.sourceResource("REQUIRED RESOURCE"), RESOURCE);

		// Test
		this.replayMockObjects();

		// Add the resource source
		this.getOfficeFloorBuilder().addResources(resourceSource);

		// Construct the managed object
		this.constructManagedObject("MO", ResourceManagedObjectSource.class, this.getOfficeName());

		// Construct work to obtain the Resource
		ResourceWork resourceWork = new ResourceWork();
		ReflectiveFunctionBuilder task = this.constructFunction(resourceWork, "validateResource");
		task.buildObject("MO", ManagedObjectScope.PROCESS);

		// Invoke function and ensure correct resource
		this.invokeFunction("validateResource", null);

		// Ensure correct resource
		assertSame("Incorrect resource", RESOURCE, resourceWork.resource);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Resource functionality.
	 */
	public static class ResourceWork {

		/**
		 * Actual resource.
		 */
		public InputStream resource = null;

		/**
		 * Validates the resource.
		 * 
		 * @param actual
		 *            Resource.
		 */
		public void validateResource(InputStream actual) {
			this.resource = actual;
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
