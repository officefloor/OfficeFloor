package net.officefloor.server.google.function;

/**
 * Mock request.
 */
public class MockDataTransferObject {

	private String text;

	public MockDataTransferObject() {
	}

	public MockDataTransferObject(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
