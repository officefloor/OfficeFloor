package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.http.mock.MockHttpServer;

/**
 * {@link Extension} for Google {@link HttpFunction} execution.
 */
public class GoogleHttpFunctionExtension extends AbstractGoogleHttpFunctionJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates whether to start/stop {@link MockHttpServer} for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public GoogleHttpFunctionExtension(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/*
	 * ================= Extension ====================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start only once for all tests
		this.isEach = false;

		// Start the server
		this.openMockHttpServer();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Start the server if for each
		if (this.isEach) {
			this.openMockHttpServer();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop the server if for each
		if (this.isEach) {
			this.close();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop server if all
		if (!this.isEach) {
			this.close();
		}

		// Reset to all
		this.isEach = false;
	}

}
