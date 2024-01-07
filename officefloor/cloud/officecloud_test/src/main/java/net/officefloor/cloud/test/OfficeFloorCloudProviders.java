package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import net.officefloor.cabinet.source.CabinetManagerManagedObjectSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.test.ObjectTestDependencyService;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * {@link TestTemplateInvocationContextProvider} for the {@link OfficeFloor}
 * cloud providers of a {@link CloudTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCloudProviders implements TestTemplateInvocationContextProvider {

	/**
	 * {@link CloudTestInstance} instances.
	 */
	private static final List<CloudTestInstance> cloudTestInstances = new LinkedList<>();

	/**
	 * Instance and state to manage a {@link CloudTestService}.
	 */
	private static class CloudTestInstance {

		/**
		 * {@link CloudTestService}.
		 */
		private CloudTestService cloudTestService;

		/**
		 * {@link CloudTestCabinet}.
		 */
		private CloudTestCabinet cloudTestCabinet = null;

		/**
		 * Instantiate.
		 * 
		 * @param cloudTestService {@link CloudTestService}.
		 */
		private CloudTestInstance(CloudTestService cloudTestService) {
			this.cloudTestService = cloudTestService;
		}

		/**
		 * Ensures the data store is available.
		 */
		private void ensureDataStoreAvailable() {

			// Determine if already available
			if (this.cloudTestCabinet != null) {
				return; // already available
			}

			// Start the data store
			this.cloudTestCabinet = this.cloudTestService.getCloudTestCabinet();
			this.cloudTestCabinet.startDataStore();

			// Ensure shutdown data store on close
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				this.cloudTestCabinet.stopDataStore();
			}));
		}
	}

	static {
		// Load the cloud test services
		SourceContext context = new SourceContextImpl(OfficeFloorCloudProviders.class.getSimpleName(), false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());
		for (CloudTestService service : context.loadOptionalServices(CloudTestServiceFactory.class)) {
			cloudTestInstances.add(new CloudTestInstance(service));
		}

		// Ensure have at least one cloud service
		assertTrue(cloudTestInstances.size() > 0,
				"No " + CloudTestService.class.getSimpleName() + " instances found on class path");
	}

	/*
	 * =============== TestTemplateInvocationContextProvider ================
	 */

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		Optional<Method> testMethod = context.getTestMethod();
		return testMethod.isPresent() ? testMethod.get().isAnnotationPresent(CloudTest.class) : false;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {

		// Create the stream
		List<TestTemplateInvocationContext> contexts = new LinkedList<>();

		// Provide context for each cloud test service
		for (CloudTestInstance cloudTestInstance : cloudTestInstances) {

			// Add context for the cloud test provider
			contexts.add(new TestTemplateInvocationContext() {

				@Override
				public String getDisplayName(int invocationIndex) {
					return cloudTestInstance.cloudTestService.getCloudServiceName();
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return Arrays.asList(new CloudTestExtension(cloudTestInstance));
				}
			});
		}
		return contexts.stream();
	}

	/**
	 * {@link Extension} to run {@link CloudTest}.
	 */
	private static class CloudTestExtension
			implements TestInstancePostProcessor, BeforeEachCallback, ParameterResolver, AfterEachCallback {

		/**
		 * {@link CloudTestInstance}.
		 */
		private final CloudTestInstance cloudTestInstance;

		/**
		 * Allow determining appropriate instance.
		 */
		private MockWoofServerExtension server = null;

		/**
		 * Instantiate.
		 * 
		 * @param cloudTestInstance {@link CloudTestInstance}.
		 */
		private CloudTestExtension(CloudTestInstance cloudTestInstance) {
			this.cloudTestInstance = cloudTestInstance;
		}

		/*
		 * ===================== Extension ========================
		 */

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

			// Capture the server
			MockWoofServerExtension testServer = null;

			// Search instance for possible extension
			Class<?> clazz = testInstance.getClass();
			while ((testServer == null) && (clazz != null)) {

				// Search fields for server extension
				FOUND_EXTENSION: for (Field field : clazz.getDeclaredFields()) {
					if (field.getType().equals(MockWoofServerExtension.class)
							&& (field.isAnnotationPresent(RegisterExtension.class))) {

						// Ensure have value
						field.setAccessible(true);
						Object fieldValue = field.get(testInstance);
						if (fieldValue != null) {

							// Test declares custom server to use
							testServer = (MockWoofServerExtension) fieldValue;
							break FOUND_EXTENSION;
						}
					}
				}

				// Search super class
				clazz = clazz.getSuperclass();
			}

			// Not on test, so create an instance
			if (testServer == null) {
				testServer = new MockWoofServerExtension();

				// Allow setup for extension
				this.server = testServer;
			}

			// Ensure the data store is available
			this.cloudTestInstance.ensureDataStoreAvailable();

			// Allow overriding the OfficeStore
			testServer.wrap((wrapped) -> {

				// Override the OfficeStore
				CabinetManagerManagedObjectSource
						.overrideOfficeStore(this.cloudTestInstance.cloudTestCabinet.getOfficeStore(), () -> {

							// Undertake compile and open
							wrapped.compileAndOpen();
						});
			});

			// Configure the server
			testServer.testDependencyService(new ObjectTestDependencyService<>(testServer));
			testServer.testDependencyService(
					new ObjectTestDependencyService<>(CloudTestService.class, this.cloudTestInstance.cloudTestService));
		}

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			if (this.server != null) {
				this.server.beforeEach(context);
			}
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return (this.server != null) ? this.server.supportsParameter(parameterContext, extensionContext) : false;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return (this.server != null) ? this.server.resolveParameter(parameterContext, extensionContext) : null;
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			if (this.server != null) {
				this.server.afterEach(context);
			}
		}
	}

}