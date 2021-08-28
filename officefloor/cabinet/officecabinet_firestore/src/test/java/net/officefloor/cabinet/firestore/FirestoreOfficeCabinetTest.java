package net.officefloor.cabinet.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
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

	private static final FirestoreOfficeCabinetAdapter ADAPTER = new FirestoreOfficeCabinetAdapter();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType) throws Exception {
		return new FirestoreOfficeCabinetArchive<>(ADAPTER, documentType, firestore.getFirestore());
	}

}
