package net.officefloor.cabinet.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class FirestoreOfficeCabinetTest extends AbstractOfficeCabinetTestCase {

	public static @RegisterExtension final FirestoreExtension firestore = new FirestoreExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeStore getOfficeStore() {
		return new FirestoreOfficeStore(firestore.getFirestore());
	}

}
