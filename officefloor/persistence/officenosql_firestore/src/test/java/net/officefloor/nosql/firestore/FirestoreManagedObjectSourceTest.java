package net.officefloor.nosql.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.Firestore;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreManagedObjectSourceTest {

	public static final @RegisterExtension FirestoreExtension firestore = new FirestoreExtension().waitForFirestore();

	/**
	 * Ensure correct specification.
	 */
	@UsesDockerTest
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(FirestoreManagedObjectSource.class);
	}

	/**
	 * Ensure correct meta-data.
	 */
	@UsesDockerTest
	public void metaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Firestore.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, FirestoreManagedObjectSource.class);
	}

	/**
	 * Ensure {@link FirestoreManagedObjectSource} working.
	 */
	@UsesDockerTest
	public void firestore() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Register Firestore
			office.addOfficeManagedObjectSource("FIRESTORE", FirestoreManagedObjectSource.class.getName())
					.addOfficeManagedObject("FIRESTORE", ManagedObjectScope.THREAD);

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Invoke functionality
			final String documentId = "1";
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", documentId);

			// Ensure able to obtain data
			TestSection.validate(firestore.getFirestore(), documentId);
		}
	}

}
