package net.officefloor.cabinet.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.OfficeCabinetArchive;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class FirestoreOfficeCabinetTest extends AbstractOfficeCabinetTest {

	public @RegisterExtension static final FirestoreExtension firestore = new FirestoreExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeCabinetArchive<AttributeTypesDocument> getAttributeTypesOfficeCabinetArchive() throws Exception {
		return new FirestoreOfficeCabinetArchive<>(AttributeTypesDocument.class, firestore.getFirestore());
	}

}
