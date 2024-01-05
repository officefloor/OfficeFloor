package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import net.officefloor.cabinet.source.OfficeStoreServiceFactory;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link Firestore} {@link FirestoreOfficeStoreServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeStoreServiceFactory implements OfficeStoreServiceFactory {

	/*
	 * ================== OfficeStoreServiceFactory ==================
	 */

	@Override
	public OfficeStore createService(ServiceContext context) throws Throwable {

		// Build the Firestore connection
		// TODO configure connection
		FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setProjectId("officefloor-test")
				.setEmulatorHost("localhost:" + 8002).build();
		Firestore firestore = firestoreOptions.getService();

		// Return the OfficeStore
		return new FirestoreOfficeStore(firestore);
	}

}
