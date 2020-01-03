package net.officefloor.web.security.store;

import java.io.File;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.security.store.CredentialStore;

/**
 * {@link ManagedObjectSource} for a {@link PasswordFile}
 * {@link CredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordFileManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Name of property identifying the path to the {@link PasswordFile}.
	 */
	public static final String PROPERTY_PASSWORD_FILE_PATH = "password.file.path";

	/**
	 * {@link CredentialStore}.
	 */
	private CredentialStore credentialStore;

	/*
	 * ==================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PASSWORD_FILE_PATH, "Password File Path");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the password file path
		String path = mosContext.getProperty(PROPERTY_PASSWORD_FILE_PATH);

		// Create the password file credential store
		PasswordFile file = PasswordFileCredentialStore.loadPasswordFile(new File(path));
		this.credentialStore = new PasswordFileCredentialStore(file);

		// Specify meta-data
		context.setObjectClass(CredentialStore.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================= ManagedObject ===========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.credentialStore;
	}

}