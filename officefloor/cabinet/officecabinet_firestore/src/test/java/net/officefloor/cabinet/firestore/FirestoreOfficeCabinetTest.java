package net.officefloor.cabinet.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesEntity;
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
	protected OfficeCabinetArchive<AttributeTypesEntity> getAttributeTypesOfficeCabinetArchive() throws Exception {
		return new FirestoreOfficeCabinetArchive<>(AttributeTypesEntity.class, firestore.getFirestore());
	}

}
