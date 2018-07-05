/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.store;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * <p>
 * Provides mocking of a {@link CredentialStore} in a similar way
 * {@link MockChallengeHttpSecuritySource} works.
 * <p>
 * Using this store allows the application to be built with its appropriate
 * {@link HttpSecuritySource} authentication scheme. This mocks the backing
 * {@link CredentialStore} for development and tests environments to avoid user
 * management. This therefore avoids the need to change the application
 * behaviour between environments (same {@link HttpSecuritySource} but differing
 * configured {@link CredentialStore}).
 * 
 * @author Daniel Sagenschneider
 */
public class MockCredentialStoreManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, CredentialStore {

	/**
	 * Name of the {@link Property} for the algorithm.
	 */
	public static final String PROPERTY_ALGORITHM = "mock.credential.store.algorithm";

	/**
	 * Algorithm.
	 */
	private String algorithm;

	/*
	 * ===================== ManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the algorithm
		this.algorithm = mosContext.getProperty(PROPERTY_ALGORITHM, null);

		// Provide meta-data
		context.setObjectClass(CredentialStore.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ============================ ManagedObject ============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ========================== CredentialStore ============================
	 */

	@Override
	public String getAlgorithm() {
		return this.algorithm;
	}

	@Override
	public CredentialEntry retrieveCredentialEntry(String userId, String realm) {

		// Create the password
		byte[] password = userId.getBytes(AbstractHttpSecuritySource.UTF_8);

		// Encrypt password (if required)
		MessageDigest digest = CredentialStoreUtil.createDigest(this.algorithm);
		if (digest != null) {
			password = digest.digest(password);
		}

		// Split the user Id for the potential multiple roles
		String[] roles = userId.split(",");
		for (int i = 0; i < roles.length; i++) {
			roles[i] = roles[i].trim();
		}

		// Create and return the credential entry
		return new MockCredentialEntry(password, new HashSet<String>(Arrays.asList(roles)));
	}

	/**
	 * Mock {@link CredentialEntry}.
	 */
	private static class MockCredentialEntry implements CredentialEntry {

		/**
		 * Credentials.
		 */
		private final byte[] credentials;

		/**
		 * Roles.
		 */
		private final Set<String> roles;

		/**
		 * Initiate.
		 * 
		 * @param credentials
		 *            Credentials.
		 * @param roles
		 *            Roles.
		 */
		public MockCredentialEntry(byte[] credentials, Set<String> roles) {
			this.credentials = credentials;
			this.roles = roles;
		}

		/*
		 * ================ CredentialEntry ======================
		 */

		@Override
		public byte[] retrieveCredentials() {
			return this.credentials;
		}

		@Override
		public Set<String> retrieveRoles() {
			return this.roles;
		}
	}

}