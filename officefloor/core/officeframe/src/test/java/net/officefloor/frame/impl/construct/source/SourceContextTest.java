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
package net.officefloor.frame.impl.construct.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure appropriately indicate if loading type.
	 */
	public void testLoadingType() {

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Ensure correct indication of loading type
		assertTrue("Should be loading type", new SourceContextImpl(true,
				classLoader).isLoadingType());
		assertFalse("Should be loading live configuration",
				new SourceContextImpl(false, classLoader).isLoadingType());
	}

	/**
	 * Ensure appropriate class loader.
	 */
	public void testClassLoader() {

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Create the context
		SourceContext context = new SourceContextImpl(false, classLoader);

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader,
				context.getClassLoader());
	}

	/**
	 * Ensure able to load optional {@link Class} instances.
	 */
	public void testLoadOptionalClass() {

		// Create the context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Ensure able to load class
		assertEquals("Should load Object class", Object.class,
				context.loadOptionalClass(Object.class.getName()));

		// Ensure not load unknown class
		assertNull("Should not load unknown class",
				context.loadOptionalClass("UNKNOWN CLASS"));
	}

	/**
	 * Ensure appropriately loads {@link Class} instances and reports on
	 * unavailable {@link Class} instances.
	 */
	public void testLoadClass() {

		// Create the context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Ensure able to load class
		assertEquals("Should load Object class", Object.class,
				context.loadClass(Object.class.getName()));

		// Ensure error if not able to load class
		try {
			context.loadClass("UNKNOWN CLASS");
			fail("Should not be successful");
		} catch (UnknownClassError ex) {
			assertEquals("Incorrect error", "UNKNOWN CLASS",
					ex.getUnknownClassName());
		}
	}

	/**
	 * Ensure able to obtain optional resources.
	 */
	public void testGetOptionalResource() {

		// Create the context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Ensure able to load Object resource
		assertNotNull(
				"Ensure able to load available resource",
				context.getOptionalResource(Object.class.getName().replace('.',
						'/')
						+ ".class"));

		// Ensure not obtain unknown resource
		assertNull("Ensure not object unknown resource",
				context.getOptionalResource("UNKNOWN RESOURCE"));
	}

	/**
	 * Ensure able to obtain resource.
	 */
	public void testGetResource() {

		// Create the context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Ensure able to load Object resource
		assertNotNull(
				"Ensure able to load available resource",
				context.getResource(Object.class.getName().replace('.', '/')
						+ ".class"));

		// Ensure not obtain unknown resource
		try {
			context.getResource("UNKNOWN RESOURCE");
			fail("Should not be successful");
		} catch (UnknownResourceError ex) {
			assertEquals("Incorrect error", "UNKNOWN RESOURCE",
					ex.getUnknownResourceLocation());
		}
	}

	/**
	 * Ensure able to use {@link ResourceSource} to obtain resource.
	 */
	public void testResourceSource() {

		final String OBJECT_RESOURCE_LOCATION = Object.class.getName().replace(
				'.', '/')
				+ ".class";

		final ResourceSource source = this.createMock(ResourceSource.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record each attempt
		this.recordReturn(source, source.sourceResource("SOURCE"), resource);
		this.recordReturn(source, source.sourceResource("SOURCE"), resource);
		this.recordReturn(source,
				source.sourceResource(OBJECT_RESOURCE_LOCATION), null);
		this.recordReturn(source,
				source.sourceResource(OBJECT_RESOURCE_LOCATION), null);
		this.recordReturn(source, source.sourceResource("UNKNOWN RESOURCE"),
				null);
		this.recordReturn(source, source.sourceResource("UNKNOWN RESOURCE"),
				null);

		// Test
		this.replayMockObjects();

		// Create context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader(), source);

		// Obtain from resource source
		assertSame("Ensure obtain via resource source", resource,
				context.getOptionalResource("SOURCE"));
		assertSame("Ensure required obtain via resource source", resource,
				context.getResource("SOURCE"));

		// Fall through to Class Loader
		assertNotNull("Ensure obtain from class loader",
				context.getOptionalResource(OBJECT_RESOURCE_LOCATION));
		assertNotNull("Ensure required obtain from class loader",
				context.getResource(OBJECT_RESOURCE_LOCATION));

		// Not find resource
		assertNull("Ensure not find optional resource",
				context.getOptionalResource("UNKNOWN RESOURCE"));
		try {
			context.getResource("UNKNOWN RESOURCE");
			fail("Should not be successful");
		} catch (UnknownResourceError ex) {
			assertEquals("Incorrect error", "UNKNOWN RESOURCE",
					ex.getUnknownResourceLocation());
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure properties and delegation occur.
	 */
	public void testDelegationAndProperties() {

		final SourceContext delegate = this.createMock(SourceContext.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);
		final ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Record obtain context details
		this.recordReturn(delegate, delegate.getClassLoader(), classLoader);
		this.recordReturn(delegate, delegate.loadOptionalClass("CLASS"),
				SourceContextTest.class);
		this.recordReturn(delegate, delegate.loadClass("CLASS"),
				SourceContextTest.class);
		this.recordReturn(delegate, delegate.getOptionalResource("RESOURCE"),
				resource);
		this.recordReturn(delegate, delegate.getResource("RESOURCE"), resource);

		// Test
		this.replayMockObjects();

		// Create the properties
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties.addProperty("NAME", "VALUE");

		// Create context with the properties
		SourceContext context = new SourceContextImpl(true, delegate,
				properties);

		// Ensure the property is available
		assertEquals("Property should be available", "VALUE",
				context.getProperty("NAME"));

		// Ensure indicate differently of loading type
		assertTrue("Should now be loading type", context.isLoadingType());

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader,
				context.getClassLoader());

		// Ensure obtain details from delegate
		assertEquals("Incorrect optional class", SourceContextTest.class,
				context.loadOptionalClass("CLASS"));
		assertEquals("Incorrect class", SourceContextTest.class,
				context.loadClass("CLASS"));
		assertSame("Incorrect optional resource", resource,
				context.getOptionalResource("RESOURCE"));
		assertSame("Incorrect resource", resource,
				context.getResource("RESOURCE"));

		// Verify
		this.verifyMockObjects();
	}

}