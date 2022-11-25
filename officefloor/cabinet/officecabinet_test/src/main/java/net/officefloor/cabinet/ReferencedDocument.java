package net.officefloor.cabinet;

import lombok.Data;

/**
 * {@link Document} referenced by the {@link ReferencingDocument}.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Document
public class ReferencedDocument {

	@Key
	private String key;

	private String testName;

	private int identifier;

	public ReferencedDocument() {
	}

	public ReferencedDocument(int identifier, String testName) {
		this.identifier = identifier;
		this.testName = testName;
	}

}
