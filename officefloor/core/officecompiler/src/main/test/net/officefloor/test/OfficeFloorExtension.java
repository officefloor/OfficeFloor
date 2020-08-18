package net.officefloor.test;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Extension} for running {@link OfficeFloor} around tests.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExtension
		implements OfficeFloorJUnit, BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * {@link Namespace} for {@link OfficeFloorExtension}.
	 */
	private static final Namespace NAMESPACE = Namespace.create(OfficeFloorExtension.class);

	/**
	 * {@link SingletonOfficeFloorJUnit} for testing.
	 */
	private SingletonOfficeFloorJUnit singleton;

	/**
	 * Obtains the delegate {@link SingletonOfficeFloorJUnit}.
	 * 
	 * @param context {@link ExtensionContext}. May be <code>null</code>.
	 * @return {@link SingletonOfficeFloorJUnit}.
	 */
	private SingletonOfficeFloorJUnit getSingleton(ExtensionContext context) {

		// Obtain the existing extension
		Store store = null;
		Class<?> testClass = null;
		SingletonOfficeFloorJUnit existing = null;
		if (context != null) {
			store = context.getStore(NAMESPACE);
			testClass = context.getRequiredTestClass();
			existing = store.get(testClass, SingletonOfficeFloorJUnit.class);
		}

		// Determine if have cached singleton
		if (this.singleton != null) {
			// Determine if have existing
			if (existing != null) {
				// Ensure same (otherwise indeterminant behaviour)
				assertSame(existing, this.singleton,
						"INVALID TEST STATE: cached extension does not match context extension");
			}

		} else {
			// Determine if have existing
			if (existing != null) {
				// Use the existing
				this.singleton = existing;

			} else {
				// Use this
				this.singleton = new SingletonOfficeFloorJUnit();
			}
		}

		// Ensure register existing if not yet registered
		if ((existing == null) && (store != null)) {
			store.put(testClass, this.singleton);
		}

		// Undertake action
		return this.singleton;
	}

	/*
	 * ==================== OfficeFloorJUnit ====================
	 */

	@Override
	public OfficeFloor getOfficeFloor() {
		return this.getSingleton(null).officeFloor;
	}

	@Override
	public void invokeProcess(String functionName, Object parameter) {
		this.getSingleton(null).invokeProcess(functionName, parameter);
	}

	@Override
	public void invokeProcess(String functionName, Object parameter, long waitTime) {
		this.getSingleton(null).invokeProcess(functionName, parameter, waitTime);
	}

	@Override
	public void invokeProcess(String officeName, String functionName, Object parameter, long waitTime) {
		this.getSingleton(null).invokeProcess(officeName, functionName, parameter, waitTime);
	}

	/*
	 * ==================== Extension ==========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.getSingleton(context).beforeAll();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.getSingleton(context).beforeEach();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.getSingleton(context).afterEach();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		this.getSingleton(context).afterAll();
	}

	/**
	 * Singleton {@link OfficeFloorJUnit}.
	 */
	private static class SingletonOfficeFloorJUnit extends AbstractOfficeFloorJUnit {

		/**
		 * Indicates if {@link OfficeFloor} for each test.
		 */
		private boolean isEach = false;

		/**
		 * Undertakes the before all logic.
		 * 
		 * @throws Exception If fails.
		 */
		private void beforeAll() throws Exception {
			this.openOfficeFloor();
		}

		/**
		 * Undertakes the before each logic.
		 * 
		 * @throws Exception If fails.
		 */
		private void beforeEach() throws Exception {

			// Determine if for each test
			this.isEach = (this.officeFloor == null);

			// Open OfficeFloor if for each test
			if (this.isEach) {
				this.openOfficeFloor();
			}
		}

		/**
		 * Undertakes the after each logic.
		 * 
		 * @throws Exception If fails.
		 */
		private void afterEach() throws Exception {

			// Close OfficeFloor if for each test
			if (this.isEach) {
				this.closeOfficeFloor();
			}
		}

		/**
		 * Undertakes the after all logic.
		 * 
		 * @throws Exception If fails.
		 */
		private void afterAll() throws Exception {
			this.closeOfficeFloor();
		}

		/**
		 * Opens the {@link OfficeFloor}.
		 * 
		 * @throws Exception If fails to open the {@link OfficeFloor}.
		 */
		private void openOfficeFloor() throws Exception {

			// Open the OfficeFloor
			OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			this.officeFloor = compiler.compile("OfficeFloor");
			try {
				this.officeFloor.openOfficeFloor();
			} catch (Exception ex) {
				// Ensure close and clear the OfficeFloor
				try {
					this.officeFloor.closeOfficeFloor();
				} catch (Throwable ignore) {
					// Ignore failure to close as doing best attempt to clean up
				} finally {
					this.officeFloor = null;
				}

				// Propagate the failure
				throw ex;
			}
		}

		/**
		 * Closes the {@link OfficeFloor}.
		 * 
		 * @throws Exception If fails to close the {@link OfficeFloor}.
		 */
		private void closeOfficeFloor() throws Exception {

			// Close the OfficeFloor and ensure released
			try {
				if (this.officeFloor != null) {
					this.officeFloor.closeOfficeFloor();
				}
			} finally {
				this.officeFloor = null;
			}
		}
	}

}