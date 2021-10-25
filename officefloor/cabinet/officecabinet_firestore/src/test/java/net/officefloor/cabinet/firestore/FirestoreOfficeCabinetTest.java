package net.officefloor.cabinet.firestore;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.spi.Index;
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

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {
		FirestoreDocumentAdapter adapter = new FirestoreDocumentAdapter(firestore.getFirestore());
		return new FirestoreOfficeCabinetArchive<>(adapter, documentType, indexes);
	}

	@Override
	protected DomainCabinetManufacturer getDomainSpecificCabinetManufacturer() {
		Assumptions.assumeFalse(true, "TODO implement");
		return null;
	}

}
