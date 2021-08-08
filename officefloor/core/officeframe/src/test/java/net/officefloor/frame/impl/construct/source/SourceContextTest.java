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

package net.officefloor.frame.impl.construct.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.source.LoadServiceError;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.source.UnknownServiceError;
import net.officefloor.frame.test.MockClockFactory;
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

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// Ensure correct indication of loading type
		assertTrue("Should be loading type",
				new SourceContextImpl("TEST", true, null, classLoader, new MockClockFactory()).isLoadingType());
		assertFalse("Should be loading live configuration",
				new SourceContextImpl("TEST", false, null, classLoader, new MockClockFactory()).isLoadingType());
	}

	/**
	 * Ensure able to obtain {@link Clock}.
	 */
	public void testClock() {

		long currentTimeSeconds = System.currentTimeMillis() / 1000;

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory(currentTimeSeconds));

		// Ensure able to obtain clock
		assertEquals("Incorrect clock", Long.valueOf(currentTimeSeconds), context.getClock((time) -> time).getTime());
	}

	/**
	 * Ensure able to indicate name.
	 */
	public void testName() {

		// Create the context
		final String name = "TEST";
		SourceContext context = new SourceContextImpl(name, false, null, Thread.currentThread().getContextClassLoader(),
				new MockClockFactory());

		// Ensure correct name
		assertEquals("Incorrect name", name, context.getName());
	}

	/**
	 * Ensure able to load {@link Logger}.
	 */
	public void testLogger() {

		// Create the context
		final String name = "TEST";
		SourceContext context = new SourceContextImpl(name, false, null, Thread.currentThread().getContextClassLoader(),
				new MockClockFactory());

		// Ensure have logger by correct name
		Logger logger = context.getLogger();
		assertEquals("Incorrect logger", name, logger.getName());
	}

	/**
	 * Ensure appropriate class loader.
	 */
	public void testClassLoader() {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null, classLoader, new MockClockFactory());

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader, context.getClassLoader());
	}

	/**
	 * Ensure able to load optional {@link Class} instances.
	 */
	public void testLoadOptionalClass() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load class
		assertEquals("Should load Object class", Object.class, context.loadOptionalClass(Object.class.getName()));

		// Ensure not load unknown class
		assertNull("Should not load unknown class", context.loadOptionalClass("UNKNOWN CLASS"));
	}

	/**
	 * Ensure appropriately loads {@link Class} instances and reports on unavailable
	 * {@link Class} instances.
	 */
	public void testLoadClass() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load class
		assertEquals("Should load Object class", Object.class, context.loadClass(Object.class.getName()));

		// Ensure error if not able to load class
		try {
			context.loadClass("UNKNOWN CLASS");
			fail("Should not be successful");
		} catch (UnknownClassError ex) {
			assertEquals("Incorrect error", "UNKNOWN CLASS", ex.getUnknownClassName());
		}
	}

	/**
	 * Ensure appropriately loads {@link Array} {@link Class} and reports on
	 * unavailable {@link Class}.
	 */
	public void testLoadArrayClass() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load array class
		assertEquals("Should load array class", Object[].class, context.loadClass(Object[].class.getName()));

		// Ensure error if not able to load class
		try {
			context.loadClass("[LUNKNOWN CLASS;");
			fail("Should not be successful");
		} catch (UnknownClassError ex) {
			assertEquals("Incorrect error", "[LUNKNOWN CLASS;", ex.getUnknownClassName());
		}
	}

	/**
	 * Ensure can appropriate loads primitives.
	 */
	public void testLoadPrimitives() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Load primitives
		for (Class<?> primitive : new Class[] { boolean.class, byte.class, short.class, char.class, int.class,
				long.class, float.class, double.class }) {
			assertEquals(primitive.getSimpleName(), primitive, context.loadClass(primitive.getName()));
		}
	}

	/**
	 * Ensure can appropriate load primitive {@link Array}.
	 */
	public void testLoadPrimitiveArrays() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Load primitives
		for (Class<?> primitive : new Class[] { boolean[].class, byte[].class, short[].class, char[].class, int[].class,
				long[].class, float[].class, double[].class }) {
			assertEquals(primitive.getSimpleName(), primitive, context.loadClass(primitive.getName()));
		}
	}

	/**
	 * Ensure able to obtain optional resources.
	 */
	public void testGetOptionalResource() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load Object resource
		assertNotNull("Ensure able to load available resource",
				context.getOptionalResource(Object.class.getName().replace('.', '/') + ".class"));

		// Ensure not obtain unknown resource
		assertNull("Ensure not object unknown resource", context.getOptionalResource("UNKNOWN RESOURCE"));
	}

	/**
	 * Ensure able to obtain resource.
	 */
	public void testGetResource() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load Object resource
		assertNotNull("Ensure able to load available resource",
				context.getResource(Object.class.getName().replace('.', '/') + ".class"));

		// Ensure not obtain unknown resource
		try {
			context.getResource("UNKNOWN RESOURCE");
			fail("Should not be successful");
		} catch (UnknownResourceError ex) {
			assertEquals("Incorrect error", "UNKNOWN RESOURCE", ex.getUnknownResourceLocation());
		}
	}

	/**
	 * Ensure able to use {@link ResourceSource} to obtain resource.
	 */
	public void testResourceSource() {

		final String OBJECT_RESOURCE_LOCATION = Object.class.getName().replace('.', '/') + ".class";

		final ResourceSource source = this.createMock(ResourceSource.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record each attempt
		this.recordReturn(source, source.sourceResource("SOURCE"), resource);
		this.recordReturn(source, source.sourceResource("SOURCE"), resource);
		this.recordReturn(source, source.sourceResource(OBJECT_RESOURCE_LOCATION), null);
		this.recordReturn(source, source.sourceResource(OBJECT_RESOURCE_LOCATION), null);
		this.recordReturn(source, source.sourceResource("UNKNOWN RESOURCE"), null);
		this.recordReturn(source, source.sourceResource("UNKNOWN RESOURCE"), null);

		// Test
		this.replayMockObjects();

		// Create context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory(), source);

		// Obtain from resource source
		assertSame("Ensure obtain via resource source", resource, context.getOptionalResource("SOURCE"));
		assertSame("Ensure required obtain via resource source", resource, context.getResource("SOURCE"));

		// Fall through to Class Loader
		assertNotNull("Ensure obtain from class loader", context.getOptionalResource(OBJECT_RESOURCE_LOCATION));
		assertNotNull("Ensure required obtain from class loader", context.getResource(OBJECT_RESOURCE_LOCATION));

		// Not find resource
		assertNull("Ensure not find optional resource", context.getOptionalResource("UNKNOWN RESOURCE"));
		try {
			context.getResource("UNKNOWN RESOURCE");
			fail("Should not be successful");
		} catch (UnknownResourceError ex) {
			assertEquals("Incorrect error", "UNKNOWN RESOURCE", ex.getUnknownResourceLocation());
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load service.
	 */
	public void testLoadService() {

		// Ensure test valid
		assertFalse("No configured service",
				ServiceLoader.load(NotConfiguredServiceFactory.class).iterator().hasNext());
		Iterator<SingleServiceFactory> singleIterator = ServiceLoader.load(SingleServiceFactory.class).iterator();
		assertTrue("Single Service", singleIterator.hasNext());
		singleIterator.next();
		assertFalse("Should only be the single service", singleIterator.hasNext());
		Iterator<MultipleServiceFactory> multipleIterator = ServiceLoader.load(MultipleServiceFactory.class).iterator();
		assertTrue("One of two multiple services", multipleIterator.hasNext());
		multipleIterator.next();
		assertTrue("Two of two multiple services", multipleIterator.hasNext());
		multipleIterator.next();
		assertFalse("Should be no more multiple services", multipleIterator.hasNext());
		Iterator<PropertyServiceFactory> propertyIterator = ServiceLoader.load(PropertyServiceFactory.class).iterator();
		assertTrue("Should be proeprty service", propertyIterator.hasNext());
		propertyIterator.next();
		assertFalse("Should be no more property services", propertyIterator.hasNext());

		// Create context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Ensure able to load provided service
		Class<SingleServiceFactory> loadedService = context.loadService(new SingleServiceFactory());
		assertEquals("Should load provided service", SingleServiceFactory.class, loadedService);

		// Ensure able to load optional service
		Class<SingleServiceFactory> optionalService = context.loadOptionalService(SingleServiceFactory.class);
		assertEquals("Should load the single optional service", SingleServiceFactory.class, optionalService);

		// Ensure able to not load optional service
		Class<NotConfiguredServiceFactory> notConfiguredOptionalService = context
				.loadOptionalService(NotConfiguredServiceFactory.class);
		assertNull("Not configured so should not optionally load", notConfiguredOptionalService);

		// Ensure load failure if multiple configured
		try {
			context.loadOptionalService(MultipleServiceFactory.class);
			fail("Should not be successful");
		} catch (LoadServiceError ex) {
			assertEquals("Incorrect service", MultipleServiceFactory.class.getName(), ex.getServiceFactoryClassName());
			assertEquals("Indicate multiple services configured",
					"Multiple services configured for single required service "
							+ MultipleServiceFactory.class.getName(),
					ex.getCause().getMessage());
		}

		// Ensure load service
		Class<SingleServiceFactory> service = context.loadService(SingleServiceFactory.class, null);
		assertEquals("Should load single service", SingleServiceFactory.class, service);

		// Ensure load service from default
		Class<NotConfiguredServiceFactory> defaultService = context.loadService(NotConfiguredServiceFactory.class,
				new DefaultServiceFactory());
		assertEquals("Should load default single service", NotConfiguredServiceFactory.class, defaultService);

		// Assert unknown service
		Consumer<Runnable> assertUnknown = (loadService) -> {
			try {
				loadService.run();
				fail("Should not be successful");
			} catch (UnknownServiceError ex) {
				assertEquals("Incorrect unknown service", NotConfiguredServiceFactory.class,
						ex.getUnknownServiceFactoryType());
			}
		};

		// Ensure provide unknown service error
		assertUnknown.accept(() -> context.loadService(NotConfiguredServiceFactory.class, null));

		// Assert loading multiple
		Consumer<Supplier<Iterable<? extends Class<? extends MultipleServiceFactory>>>> assertMultiple = (
				loadServices) -> {
			this.assertServices("Multiple", loadServices, MultipleServiceFactoryOne.class,
					MultipleServiceFactoryTwo.class);
		};

		// Ensure able to load optional multiple services
		assertMultiple.accept(() -> context.loadOptionalServices(MultipleServiceFactory.class));
		this.assertServices("Optional single", () -> context.loadOptionalServices(SingleServiceFactory.class),
				SingleServiceFactory.class);
		this.assertServices("Optional not configured",
				() -> context.loadOptionalServices(NotConfiguredServiceFactory.class));

		// Ensure able to load multiple services
		assertMultiple.accept(() -> context.loadServices(MultipleServiceFactory.class, null));
		assertMultiple
				.accept(() -> context.loadServices(MultipleServiceFactory.class, new MultipleServiceFactoryDefault()));
		this.assertServices("Single", () -> context.loadServices(SingleServiceFactory.class, null),
				SingleServiceFactory.class);
		this.assertServices("Default single",
				() -> context.loadServices(NotConfiguredServiceFactory.class, new DefaultServiceFactory()),
				NotConfiguredServiceFactory.class);
		assertUnknown.accept(() -> context.loadServices(NotConfiguredServiceFactory.class, null));

		// Ensure able to load properties
		final String PROPERTY_VALUE = "VALUE";
		SourceContext propertiesContext = new SourceContextImpl("property", false, null, context,
				new SourcePropertiesImpl(PropertyServiceFactory.PROPERTY_NAME, PROPERTY_VALUE));
		assertEquals("Incorrect service instance property", PROPERTY_VALUE,
				propertiesContext.loadService(new PropertyServiceFactory()));
		assertEquals("Incorrect optional service property", PROPERTY_VALUE,
				propertiesContext.loadOptionalService(PropertyServiceFactory.class));
		assertEquals("Incorrect optional services property", PROPERTY_VALUE,
				propertiesContext.loadOptionalServices(PropertyServiceFactory.class).iterator().next());
		assertEquals("Incorrect service property", PROPERTY_VALUE,
				propertiesContext.loadService(PropertyServiceFactory.class, null));
		assertEquals("Incorrect services property", PROPERTY_VALUE,
				propertiesContext.loadServices(PropertyServiceFactory.class, null).iterator().next());
	}

	/**
	 * Asserts the services loaded.
	 * 
	 * @param messagePrefix    Prefix to assert messages.
	 * @param loadServices     Loads the services.
	 * @param expectedServices Expected services.
	 */
	private void assertServices(String messagePrefix, Supplier<? extends Iterable<?>> loadServices,
			Object... expectedServices) {
		List<Object> multiples = new LinkedList<>();
		for (Object serviceImplementation : loadServices.get()) {
			multiples.add(serviceImplementation);
		}
		for (Object expectedService : expectedServices) {
			assertTrue(messagePrefix + " : should have service " + expectedService, multiples.remove(expectedService));
		}
		assertEquals(messagePrefix + " : should be no further configured services", 0, multiples.size());
	}

	/**
	 * Ensure appropriate report failing to load service.
	 */
	public void testLoadFailingServices() {

		// Ensure test valid
		final Exception failure = new Exception("TEST");
		try {
			FailServiceFactory.reset();
			FailServiceFactory.instantiateFailure = failure;
			ServiceLoader.load(FailServiceFactory.class).iterator().next();
			fail("Should not be successful");
		} catch (ServiceConfigurationError ex) {
			assertSame("Incorrect instantiation failure", failure, ex.getCause());
		}

		// Create context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		// Assert load error
		final Throwable instantiateFailure = new Exception("TEST");
		FailServiceFactory.reset();
		FailServiceFactory.instantiateFailure = instantiateFailure;
		this.assertLoadServiceError(FailServiceFactory.class, instantiateFailure,
				() -> context.loadOptionalService(FailServiceFactory.class));
		this.assertLoadServiceError(FailServiceFactory.class, instantiateFailure,
				() -> context.loadService(FailServiceFactory.class, null));
		this.assertLoadServiceError(FailServiceFactory.class, instantiateFailure,
				() -> context.loadOptionalServices(FailServiceFactory.class).iterator().next());
		this.assertLoadServiceError(FailServiceFactory.class, instantiateFailure,
				() -> context.loadServices(FailServiceFactory.class, null).iterator().next());

		// Assert create error
		final Throwable createFailure = new RuntimeException("TEST");
		FailServiceFactory.reset();
		FailServiceFactory.createServiceFailure = createFailure;
		this.assertLoadServiceError(FailServiceFactory.class, createFailure,
				() -> context.loadOptionalService(FailServiceFactory.class));
		this.assertLoadServiceError(FailServiceFactory.class, createFailure,
				() -> context.loadService(FailServiceFactory.class, null));
		this.assertLoadServiceError(FailServiceFactory.class, createFailure,
				() -> context.loadOptionalServices(FailServiceFactory.class).iterator().next());
		this.assertLoadServiceError(FailServiceFactory.class, createFailure,
				() -> context.loadServices(FailServiceFactory.class, null).iterator().next());
	}

	/**
	 * Missing property for the service.
	 */
	public void testMissingPropertyForService() {

		// Create the context
		SourceContext context = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());

		NotConfiguredServiceFactory serviceFactory = (serviceContext) -> {
			serviceContext.getProperty("missing");
			fail("Should not be successful");
			return null;
		};
		try {
			context.loadService(NotConfiguredServiceFactory.class, serviceFactory);
		} catch (UnknownPropertyError ex) {
			assertEquals("Incorrect missing property", "missing", ex.getUnknownPropertyName());
			assertSame("Incorrect service factory", serviceFactory, ex.getServiceFactory());
			assertEquals("Incorrect message",
					"Must specify property 'missing' for service factory " + serviceFactory.getClass().getName(),
					ex.getMessage());
		}
	}

	/**
	 * Asserts {@link LoadServiceError}.
	 * 
	 * @param expectedServiceFactory {@link ServiceFactory} {@link Class} triggering
	 *                               the failure.
	 * @param cause                  Expected cause.
	 * @param runnable               {@link Runnable} to trigger loading service.
	 */
	private void assertLoadServiceError(Class<?> expectedServiceFactory, Throwable cause, Runnable runnable) {
		try {
			runnable.run();
			fail("Should not be successful");
		} catch (LoadServiceError ex) {
			assertEquals("Incorrect service factory", expectedServiceFactory.getName(),
					ex.getServiceFactoryClassName());
			assertSame("Incorrect cause", cause, ex.getCause());
		}
	}

	/**
	 * Ensure profiles.
	 */
	public void testProfiles() {

		// Ensure no properties
		final SourceContext emptyContext = new SourceContextImpl("TEST", false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());
		assertProfiles(emptyContext.getProfiles());

		// Ensure can have properties
		final SourceContext profileContext = new SourceContextImpl("TEST", false,
				new String[] { "one", "two", "three" }, Thread.currentThread().getContextClassLoader(),
				new MockClockFactory());
		assertProfiles(profileContext.getProfiles(), "one", "two", "three");

		// Ensure can have no additional profiles on delegation
		assertProfiles(new SourceContextImpl("NO_PROFILES", false, null, emptyContext, null).getProfiles());
		assertProfiles(new SourceContextImpl("EXISTING_PROFILES", false, null, profileContext, null).getProfiles(),
				"one", "two", "three");

		// Ensure can add additional profiles
		assertProfiles(new SourceContextImpl("ADD_PROFILES", false, new String[] { "a", "b" }, emptyContext, null)
				.getProfiles(), "a", "b");
		assertProfiles(
				new SourceContextImpl("ADDITIONAL_PROFILES", false, new String[] { "a", "b" }, profileContext, null)
						.getProfiles(),
				"a", "b", "one", "two", "three");

		// Ensure additional profiles take priority
		assertProfiles(new SourceContextImpl("PRIORITY_PROFILES", false, new String[] { "three", "two", "one" },
				profileContext, null).getProfiles(), "three", "two", "one");

	}

	/**
	 * Asserts the profiles.
	 * 
	 * @param profiles         Actual profiles.
	 * @param expectedProfiles Expected profiles.
	 */
	private static void assertProfiles(List<String> profiles, String... expectedProfiles) {
		assertEquals("Incorrect number of profiles", expectedProfiles.length, profiles.size());
		for (int i = 0; i < expectedProfiles.length; i++) {
			assertEquals("Incorrect profile " + i, expectedProfiles[i], profiles.get(i));
		}
	}

	/**
	 * Ensure properties and delegation occur.
	 */
	public void testDelegationAndProperties() {

		final SourceContext delegate = this.createMock(SourceContext.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final List<String> profiles = Arrays.asList("profile");

		// Record obtain context details
		this.recordReturn(delegate, delegate.getProfiles(), profiles);
		this.recordReturn(delegate, delegate.getClassLoader(), classLoader);
		this.recordReturn(delegate, delegate.loadOptionalClass("CLASS"), SourceContextTest.class);
		this.recordReturn(delegate, delegate.loadClass("CLASS"), SourceContextTest.class);
		this.recordReturn(delegate, delegate.getOptionalResource("RESOURCE"), resource);
		this.recordReturn(delegate, delegate.getResource("RESOURCE"), resource);

		// Test
		this.replayMockObjects();

		// Create the properties
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties.addProperty("NAME", "VALUE");

		// Create context with the properties
		final String NAME = "TEST";
		SourceContext context = new SourceContextImpl(NAME, true, null, delegate, properties);

		// Ensure correct name
		assertEquals("Incorrect name", NAME, context.getName());

		// Ensure correct profiles
		assertEquals("Incorrect profile", "profile", context.getProfiles().get(0));
		assertEquals("Incorrect number of profiles", 1, context.getProfiles().size());

		// Ensure correct logger name
		assertEquals("Incorrect logger name", NAME, context.getLogger().getName());

		// Ensure the property is available
		assertEquals("Property should be available", "VALUE", context.getProperty("NAME"));

		// Ensure indicate differently of loading type
		assertTrue("Should now be loading type", context.isLoadingType());

		// Ensure correct class loader
		assertSame("Incorrect class loader", classLoader, context.getClassLoader());

		// Ensure obtain details from delegate
		assertEquals("Incorrect optional class", SourceContextTest.class, context.loadOptionalClass("CLASS"));
		assertEquals("Incorrect class", SourceContextTest.class, context.loadClass("CLASS"));
		assertSame("Incorrect optional resource", resource, context.getOptionalResource("RESOURCE"));
		assertSame("Incorrect resource", resource, context.getResource("RESOURCE"));

		// Verify
		this.verifyMockObjects();
	}

}
