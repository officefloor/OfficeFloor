package net.officefloor.cabinet.override;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.MStoreNone;
import net.officefloor.cabinet.source.CabinetManagerManagedObjectSource;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests overriding the {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public abstract class AbstractOfficeStoreOverrideTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mock = new MockTestSupport();

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure can override {@link OfficeStore}.
	 */
	@Test
	@MStoreNone
	public void overrideOfficeStore() throws Throwable {

		// Create OfficeStore
		OfficeStore mockOfficeStore = this.mock.createMock(OfficeStore.class);
		CabinetManager mockCabinetManager = this.mock.createMock(CabinetManager.class);

		// Record creating CabinetManager
		this.mock.recordReturn(mockOfficeStore, mockOfficeStore.createCabinetManager(), mockCabinetManager);
		this.mock.replayMockObjects();

		// Override with mock
		Closure<CabinetManager> cabinetManager = new Closure<>();
		CabinetManagerManagedObjectSource.overrideOfficeStore(mockOfficeStore, () -> {

			// Load the managed object source
			CabinetManagerManagedObjectSource managedObjectSource = new ManagedObjectSourceStandAlone()
					.loadManagedObjectSource(CabinetManagerManagedObjectSource.class);

			// Obtain the CabinetManager
			try {
				ManagedObject managedObject = new ManagedObjectUserStandAlone()
						.sourceManagedObject(managedObjectSource);
				Object object = managedObject.getObject();
				assertTrue(object instanceof CabinetManager, "Should be CabinetManager");
				cabinetManager.value = (CabinetManager) object;
			} catch (Throwable ex) {
				fail(ex);
			}
		});

		// Ensure using mock
		assertSame(mockCabinetManager, cabinetManager.value, "Should be mock CabinetManager");
		this.mock.verifyMockObjects();
	}

}