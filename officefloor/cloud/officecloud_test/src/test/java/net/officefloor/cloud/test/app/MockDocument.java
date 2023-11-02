package net.officefloor.cloud.test.app;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Mock {@link Document}.
 */
@Document
public class MockDocument {

	private @Key String key;

	private String message;

	public MockDocument() {
	}

	public MockDocument(String message) {
		this.message = message;
	}

	public String getKey() {
		return this.key;
	}

	public String getMessage() {
		return this.message;
	}
}
