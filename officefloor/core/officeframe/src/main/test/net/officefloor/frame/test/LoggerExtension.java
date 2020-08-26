package net.officefloor.frame.test;

import java.util.logging.Logger;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} to assert {@link Logger} events.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerExtension extends AbstractLoggerJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if for each test.
	 */
	private boolean isEach = true;

	/*
	 * ====================== Extension ==========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Setup capture
		this.setupLogCapture();

		// Flag for all
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Setup capture if for each
		if (this.isEach) {
			this.setupLogCapture();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Tear down capture if for each
		if (this.isEach) {
			this.teardownLogCapture();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Tear down capture if all
		if (!this.isEach) {
			this.teardownLogCapture();
		}

		// Reset for all
		this.isEach = true;
	}

}