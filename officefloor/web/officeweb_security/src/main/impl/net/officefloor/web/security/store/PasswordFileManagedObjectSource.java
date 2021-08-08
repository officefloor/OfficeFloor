/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
