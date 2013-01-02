/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.integrate.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

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
		this.constructManagedObject("MO", ClassLoaderManagedObjectSource.class,
				this.getOfficeName());

		// Construct work to obtain the Class Loader
		ClassLoaderWork classLoaderWork = new ClassLoaderWork();
		ReflectiveWorkBuilder work = this.constructWork(classLoaderWork,
				"WORK", "validateClassLoader");
		ReflectiveTaskBuilder task = work.buildTask("validateClassLoader",
				"TEAM");
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		this.constructTeam("TEAM", new PassiveTeam());

		// Invoke work and ensure correct class loader
		this.invokeWork("WORK", null);

		// Ensure correct default class loader
		assertSame("Incorrect class loader", Thread.currentThread()
				.getContextClassLoader(), classLoaderWork.classLoader);
	}

	/**
	 * Ensure override class loader.
	 */
	public void testOverrideClassLoader() throws Exception {

		final ClassLoader classLoader = new URLClassLoader(new URL[0]);

		// Override the Class Loader
		this.getOfficeFloorBuilder().setClassLoader(classLoader);

		// Construct the managed object
		this.constructManagedObject("MO", ClassLoaderManagedObjectSource.class,
				this.getOfficeName());

		// Construct work to obtain the Class Loader
		ClassLoaderWork classLoaderWork = new ClassLoaderWork();
		ReflectiveWorkBuilder work = this.constructWork(classLoaderWork,
				"WORK", "validateClassLoader");
		ReflectiveTaskBuilder task = work.buildTask("validateClassLoader",
				"TEAM");
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		this.constructTeam("TEAM", new PassiveTeam());

		// Invoke work and ensure correct class loader
		this.invokeWork("WORK", null);

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader,
				classLoaderWork.classLoader);
	}

	/**
	 * {@link ClassLoader} {@link Work}.
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
	public static class ClassLoaderManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject {

		private ClassLoader classLoader;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			ManagedObjectSourceContext<None> mosContext = context
					.getManagedObjectSourceContext();
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
		final ResourceSource resourceSource = this
				.createMock(ResourceSource.class);

		// Record obtaining the resource
		this.recordReturn(resourceSource,
				resourceSource.sourceResource("REQUIRED RESOURCE"), RESOURCE);

		// Test
		this.replayMockObjects();

		// Add the resource source
		this.getOfficeFloorBuilder().addResources(resourceSource);

		// Construct the managed object
		this.constructManagedObject("MO", ResourceManagedObjectSource.class,
				this.getOfficeName());

		// Construct work to obtain the Resource
		ResourceWork resourceWork = new ResourceWork();
		ReflectiveWorkBuilder work = this.constructWork(resourceWork, "WORK",
				"validateResource");
		ReflectiveTaskBuilder task = work.buildTask("validateResource", "TEAM");
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		this.constructTeam("TEAM", new PassiveTeam());

		// Invoke work and ensure correct resource
		this.invokeWork("WORK", null);

		// Ensure correct resource
		assertSame("Incorrect resource", RESOURCE, resourceWork.resource);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Resource {@link Work}.
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
	public static class ResourceManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject {

		private InputStream resource;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			ManagedObjectSourceContext<None> mosContext = context
					.getManagedObjectSourceContext();
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