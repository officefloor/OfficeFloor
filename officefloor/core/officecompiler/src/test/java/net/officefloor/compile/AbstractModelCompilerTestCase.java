package net.officefloor.compile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.configuration.impl.configuration.XmlFileConfigurationContext;
import net.officefloor.extension.AutoWireOfficeExtensionService;
import net.officefloor.extension.AutoWireOfficeFloorExtensionService;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Provides abstract functionality for testing integration of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractModelCompilerTestCase extends OfficeFrameTestCase {

	/**
	 * {@link XmlFileConfigurationContext} for testing.
	 */
	private XmlFileConfigurationContext configurationContext = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset the managed object pool source
		TestManagedObjectPoolSource.reset();
	}

	@Override
	protected void tearDown() throws Exception {

		// Reset the extension services
		AutoWireOfficeExtensionService.reset();
		AutoWireOfficeFloorExtensionService.reset();

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Obtains the {@link ResourceSource} for test being run.
	 * 
	 * @return {@link ResourceSource} for test being run.
	 */
	protected ResourceSource getResourceSource() {

		// Determine if already available
		if (this.configurationContext != null) {
			return this.configurationContext;
		}

		// Move the 'Test' to start of test case name
		String testCaseName = this.getClass().getSimpleName();
		testCaseName = "Test" + testCaseName.substring(0, (testCaseName.length() - "Test".length()));

		// Remove the 'test' from the start of the test name
		String testName = this.getName();
		testName = testName.substring("test".length());

		// Create the configuration context
		String configFileName = testCaseName + "/" + testName + ".xml";
		try {
			this.configurationContext = new XmlFileConfigurationContext(this, configFileName);

			// Add the tag replacements
			this.configurationContext.addProperty("testcase", this.getClass().getName());
			this.configurationContext.addProperty("POOL", TestManagedObjectPoolSource.class.getName());

		} catch (Exception ex) {
			// Wrap failure to not require tests to have to handle
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			fail("Failed to obtain configuration: " + configFileName + "\n" + stackTrace.toString());
			return null; // fail should propagate exception
		}

		// Return the configuration context
		return this.configurationContext;
	}

	@TestSource
	public static class TestManagedObjectPoolSource extends AbstractManagedObjectPoolSource
			implements ManagedObjectPoolFactory, ThreadCompletionListenerFactory {

		public static final String PROPERTY_POOL_ID = "id";

		private static Map<String, TestManagedObjectPoolSource> instances = new HashMap<>();

		private static void reset() {
			instances.clear();
		}

		public static TestManagedObjectPoolSource getManagedObjectPoolSource(String managedObjectPoolId) {
			TestManagedObjectPoolSource instance = instances.get(managedObjectPoolId);
			if (instance == null) {
				instance = new TestManagedObjectPoolSource();
				instances.put(managedObjectPoolId, instance);
			}
			return instance;
		}

		/*
		 * ================= ManagedObjectPoolSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_POOL_ID);
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {

			// Register the source
			String poolId = context.getManagedObjectPoolSourceContext().getProperty(PROPERTY_POOL_ID);
			TestManagedObjectPoolSource pool = getManagedObjectPoolSource(poolId);

			// Obtain the pooled object type
			String pooledObjectTypeName = context.getManagedObjectPoolSourceContext().getProperty("pooled.object.type",
					null);
			Class<?> pooledObjectType = Object.class;
			if (pooledObjectTypeName != null) {
				pooledObjectType = context.getManagedObjectPoolSourceContext().loadClass(pooledObjectTypeName);
			}

			// Configure the source
			context.setPooledObjectType(pooledObjectType);
			context.setManagedObjectPoolFactory(pool);
			context.addThreadCompleteListener(pool);
		}

		@Override
		public ThreadCompletionListener createThreadCompletionListener(ManagedObjectPool pool) {
			fail("Should not create ManagedObjectPool in compiling");
			return null;
		}

		@Override
		public ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext context) {
			fail("Should not create ThreadCompletionListener in compiling");
			return null;
		}
	}

}