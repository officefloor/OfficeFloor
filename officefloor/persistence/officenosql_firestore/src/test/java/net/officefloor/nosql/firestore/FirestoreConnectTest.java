package net.officefloor.nosql.firestore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.test.system.AbstractEnvironmentOverride;

/**
 * Ensures able to connect to {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class FirestoreConnectTest {

	public static @RegisterExtension FirestoreExtension firestore = new FirestoreExtension().waitForFirestore();

	/**
	 * Must clear {@link FirestoreFactory} to allow manual connection.
	 */
	@BeforeEach
	public void clearConnectionFactory() throws Exception {
		FirestoreConnect.setFirestoreFactory(null);
	}

	/**
	 * Ensure able to connect via configured {@link Property} values.
	 */
	@Test
	public void connectViaProperties() throws Throwable {
		this.doTest(FirestoreConnect.FIRESTORE_EMULATOR_HOST, firestore.getEmulatorHost());
	}

	/**
	 * Ensure able to connect via environment variables (typically from Google
	 * configuration).
	 */
	@Test
	public void connectViaEnvironment() throws Throwable {
		ConnectEnvironment env = new ConnectEnvironment();
		Runnable reset = env.property(FirestoreConnect.FIRESTORE_EMULATOR_HOST, firestore.getEmulatorHost()).setup();
		try {
			this.doTest();
		} finally {
			reset.run();
		}
	}

	/**
	 * Enable overriding the environment.
	 */
	private static class ConnectEnvironment extends AbstractEnvironmentOverride<ConnectEnvironment> {

		public Runnable setup() {
			OverrideReset reset = this.override();
			return () -> reset.resetOverrides();
		}
	}

	/**
	 * Undertakes the tests.
	 * 
	 * @param propertyNameValues {@link Property} name/value pairs for
	 *                           {@link CosmosClientManagedObjectSource}.
	 */
	private void doTest(String... propertyNameValues) throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Register Firestore
			OfficeManagedObjectSource clientMos = office.addOfficeManagedObjectSource("FIRESTORE",
					FirestoreManagedObjectSource.class.getName());
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String propertyName = propertyNameValues[i];
				String propertyValue = propertyNameValues[i + 1];
				clientMos.addProperty(propertyName, propertyValue);
			}
			clientMos.addOfficeManagedObject("FIRESTORE", ManagedObjectScope.THREAD);

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Invoke functionality
			final String documentId = "1";
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", documentId);

			// Ensure able to obtain entity
			DocumentSnapshot snapshot = TestSection.firestore.collection("test").document(documentId).get().get();
			assertEquals("Daniel", snapshot.getString("first"), "Incorrect entity");
		}
	}

	public static class TestSection {

		private static Firestore firestore;

		public void service(Firestore firestore, @Parameter String documentId) throws Exception {
			TestSection.firestore = firestore;

			// Create the item
			DocumentReference docRef = firestore.collection("test").document(documentId);
			Map<String, Object> data = new HashMap<>();
			data.put("first", "Daniel");
			data.put("last", "Sagenschneider");
			data.put("born", 1978);
			docRef.set(data).get();

			// Retrieve the item
			DocumentSnapshot snapshot = firestore.collection("test").document(documentId).get().get();
			assertEquals("Daniel", snapshot.getString("first"), "Incorrect first name");
			assertEquals("Sagenschneider", snapshot.getString("last"), "Incorrect last name");
			assertEquals(1978, snapshot.getLong("born"), "Incorrect born");
		}
	}

}
