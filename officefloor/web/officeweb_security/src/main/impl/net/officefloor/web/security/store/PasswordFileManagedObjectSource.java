/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.store;

import java.io.File;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

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
