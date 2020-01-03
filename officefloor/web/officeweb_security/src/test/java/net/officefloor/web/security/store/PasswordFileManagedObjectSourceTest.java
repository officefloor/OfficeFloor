package net.officefloor.web.security.store;

import java.util.Set;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.security.store.CredentialEntry;
import net.officefloor.web.security.store.CredentialStore;
import net.officefloor.web.security.store.PasswordFileManagedObjectSource;

/**
 * Tests the {@link PasswordFileManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordFileManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(PasswordFileManagedObjectSource.class,
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH, "Password File Path");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Exception {

		// Obtain path to password file
		String passwordFilePath = this.findFile(this.getClass(), "password-file.txt").getAbsolutePath();

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(CredentialStore.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, PasswordFileManagedObjectSource.class,
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH, passwordFilePath);
	}

	/**
	 * Ensure can obtain the {@link CredentialStore}.
	 */
	public void testCredentialStore() throws Throwable {

		// Obtain path to password file
		String passwordFilePath = this.findFile(this.getClass(), "password-file.txt").getAbsolutePath();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH, passwordFilePath);
		PasswordFileManagedObjectSource source = loader.loadManagedObjectSource(PasswordFileManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Ensure appropriate credential store
		Object object = managedObject.getObject();
		assertTrue("Object should be " + CredentialStore.class.getSimpleName(), object instanceof CredentialStore);
		CredentialStore store = (CredentialStore) object;
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		Set<String> roles = entry.retrieveRoles();
		assertTrue("Incorrect role", roles.contains("founder"));
	}

}