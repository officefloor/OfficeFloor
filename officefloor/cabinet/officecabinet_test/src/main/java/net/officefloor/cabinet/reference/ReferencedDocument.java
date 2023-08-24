package net.officefloor.cabinet.reference;

import lombok.Data;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

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
	
	private String change;

	public ReferencedDocument() {
	}

	public ReferencedDocument(int identifier, String testName) {
		this.identifier = identifier;
		this.testName = testName;
	}

}
