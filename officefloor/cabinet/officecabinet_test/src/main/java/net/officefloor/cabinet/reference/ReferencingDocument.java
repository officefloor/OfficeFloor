package net.officefloor.cabinet.reference;

import lombok.Data;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OneToOne;

/**
 * {@link Document} that references the {@link ReferencedDocument}.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Document
public class ReferencingDocument {

	@Key
	private String key;

	private String testName;

	private int identifier;

	private final OneToOne<ReferencedDocument> oneToOne = new OneToOne<>(ReferencedDocument.class);

	public ReferencingDocument() {
	}

	public ReferencingDocument(int identifier, String testName) {
		this.identifier = identifier;
		this.testName = testName;
	}

}
