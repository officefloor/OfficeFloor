package net.officefloor.cloud.test.google;

import static org.junit.jupiter.api.Assertions.fail;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.firestore.FirestoreOfficeStore;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.cloud.test.CloudTestCabinet;
import net.officefloor.cloud.test.CloudTestService;
import net.officefloor.cloud.test.CloudTestServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.nosql.firestore.test.AbstractFirestoreJunit;

/**
 * {@link CloudTestService} for Google.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleCloudTestService implements CloudTestService, CloudTestServiceFactory {

	/*
	 * ================ CloudTestServiceFactory ================
	 */

	@Override
	public CloudTestService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== CloudTestService ====================
	 */

	@Override
	public String getCloudServiceName() {
		return "Google";
	}

	@Override
	public CloudTestCabinet getCloudTestCabinet() {
		return new GoogleCloudTestCabinet();
	}

	/**
	 * {@link CloudTestCabinet} for Google.
	 */
	private static class GoogleCloudTestCabinet extends AbstractFirestoreJunit<GoogleCloudTestCabinet>
			implements CloudTestCabinet {

		/*
		 * ============== CloudTestCabinet ===================
		 */

		@Override
		public void startDataStore() {
			try {
				this.startFirestore();
			} catch (Exception ex) {
				fail(ex);
			}
		}

		@Override
		public OfficeStore getOfficeStore() {
			Firestore firestore = this.getFirestore();
			return new FirestoreOfficeStore(firestore);
		}

		@Override
		public void stopDataStore() {
			try {
				this.stopFirestore();
			} catch (Exception ex) {
				fail(ex);
			}
		}
	}

}