package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.test.TestDependencyService;
import net.officefloor.test.TestDependencyServiceContext;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * {@link TestTemplateInvocationContextProvider} for the {@link OfficeFloor}
 * cloud providers of a {@link CloudTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCloudProviders implements TestTemplateInvocationContextProvider {

	/**
	 * {@link CloudTestService} instances.
	 */
	private static final List<CloudTestService> cloudTestServices = new LinkedList<>();

	static {
		// Load the cloud test services
		SourceContext context = new SourceContextImpl(OfficeFloorCloudProviders.class.getSimpleName(), false, null,
				Thread.currentThread().getContextClassLoader(), new MockClockFactory());
		for (CloudTestService service : context.loadOptionalServices(CloudTestServiceFactory.class)) {
			cloudTestServices.add(service);
		}

		// Ensure have at least one cloud service
		assertTrue(cloudTestServices.size() > 0,
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
		for (CloudTestService cloudTestService : cloudTestServices) {

			// Add context for the cloud test provider
			contexts.add(new TestTemplateInvocationContext() {

				@Override
				public String getDisplayName(int invocationIndex) {
					return cloudTestService.getCloudServiceName();
				}

				@Override
				public List<Extension> getAdditionalExtensions() {

					// Create the Mock Woof Server
					MockWoofServerExtension server = new MockWoofServerExtension();
					server.testDependencyService(new TestDependencyService() {

						@Override
						public boolean isObjectAvailable(TestDependencyServiceContext context) {
							return context.getObjectType().isAssignableFrom(server.getClass());
						}

						@Override
						public Object getObject(TestDependencyServiceContext context)
								throws UnknownObjectException, Throwable {
							return server;
						}
					});

					// Return list of extensions
					return Arrays.asList(server);
				}
			});
		}
		return contexts.stream();
	}

}