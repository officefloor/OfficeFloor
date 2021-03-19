/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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
import net.officefloor.frame.test.TestSupport;

/**
 * Provides functionality for testing integration of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelCompilerTestSupport implements TestSupport, BeforeEachCallback, AfterEachCallback {

	/**
	 * Test instance.
	 */
	private Class<?> testClass;

	/**
	 * Provides the name of the test.
	 */
	private Supplier<String> testName;

	/**
	 * {@link XmlFileConfigurationContext} for testing.
	 */
	private XmlFileConfigurationContext configurationContext = null;

	/**
	 * Indicates whether to re-use the same {@link XmlFileConfigurationContext}
	 * across all tests of the class.
	 */
	private boolean isSameConfigurationAcrossTests = false;

	/**
	 * Initiate as {@link TestSupport}.
	 */
	public ModelCompilerTestSupport() {
		// Used for extension
	}

	/**
	 * Compatibility for non-JUnit5 tests.
	 * 
	 * @param testClass Test {@link Class}.
	 * @param testName  Name of test.
	 */
	public ModelCompilerTestSupport(Class<?> testClass, Supplier<String> testName) {
		this.testClass = testClass;
		this.testName = testName;
	}

	/**
	 * Flags to use same {@link XmlFileConfigurationContext} across all tests of the
	 * class.
	 * 
	 * @param isSame <code>true</code> to re-use same configuration file for all
	 *               tests of the class.
	 * @return <code>this</code>.
	 */
	public ModelCompilerTestSupport setSameConfigurationForAllTests(boolean isSame) {
		this.isSameConfigurationAcrossTests = isSame;
		return this;
	}

	/**
	 * Obtains the {@link ResourceSource} for test being run.
	 * 
	 * @return {@link ResourceSource} for test being run.
	 */
	public ResourceSource getResourceSource() {

		// Determine if already available
		if (this.configurationContext != null) {
			return this.configurationContext;
		}

		// Move the 'Test' to start of test case name
		String testCaseName = this.testClass.getSimpleName();
		testCaseName = "Test" + testCaseName.substring(0, (testCaseName.length() - "Test".length()));

		// Remove the 'test' from the start of the test name
		String testName;
		if (this.isSameConfigurationAcrossTests) {
			// Same configuration file across all tests
			testName = this.testClass.getSimpleName();
		} else {
			// Different configuration file per test
			testName = this.testName.get();
			if (testName.startsWith("test")) {
				testName = testName.substring("test".length());
			}

			// Ensure initial letter is capital
			testName = testName.substring(0, 1).toUpperCase() + testName.substring(1);
		}

		// Create the configuration context
		String configFileName = testCaseName + "/" + testName + ".xml";
		try {
			this.configurationContext = new XmlFileConfigurationContext(this.testClass, configFileName);

			// Add the tag replacements
			this.configurationContext.addProperty("testcase", this.testClass.getName());
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

	/*
	 * ======================= TestSupport ============================
	 */

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.testClass = context.getRequiredTestClass();
	}

	/*
	 * ======================== Extensions =============================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Provide test name (if test support)
		if (context != null) {
			this.testName = context.getRequiredTestMethod()::getName;
		}

		// Reset the managed object pool source
		TestManagedObjectPoolSource.reset();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Reset the extension services
		AutoWireOfficeExtensionService.reset();
		AutoWireOfficeFloorExtensionService.reset();
	}

}
