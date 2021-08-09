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

package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpChallengeContext;

/**
 * {@link ManagedObjectSource} for the {@link HttpChallenge}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpChallengeContextManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * <code>WWW-Authenticate</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName CHALLENGE_NAME = new HttpHeaderName("WWW-Authenticate");

	/**
	 * Loads the {@link HttpChallenge} to the {@link HttpResponse}.
	 * 
	 * @param httpChallengeContext {@link HttpChallengeContext}.
	 * @param response             {@link HttpResponse}.
	 */
	public static void loadHttpChallenge(HttpChallengeContext httpChallengeContext, HttpResponse response) {

		// Obtain the challenge
		HttpChallengeContextManagedObject managedObject = (HttpChallengeContextManagedObject) httpChallengeContext;

		// Determine if challenge and if so send challenge
		managedObject.managedObjectContext.run(() -> {
			if (managedObject.challenge.length() > 0) {
				response.setStatus(HttpStatus.UNAUTHORIZED);
				response.getHeaders().addHeader(CHALLENGE_NAME, managedObject.challenge.toString());
			}
			return null;
		});
	}

	/*
	 * =================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpChallengeContext.class);
		context.setManagedObjectClass(HttpChallengeContextManagedObject.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpChallengeContextManagedObject();
	}

	/**
	 * {@link HttpChallenge} {@link ManagedObject}.
	 */
	private class HttpChallengeContextManagedObject
			implements ContextAwareManagedObject, HttpChallengeContext, HttpChallenge {

		/**
		 * {@link ManagedObjectContext}.
		 */
		private ManagedObjectContext managedObjectContext;

		/**
		 * {@link StringBuilder} to load the {@link HttpChallenge}.
		 */
		private final StringBuilder challenge = new StringBuilder(128);

		/*
		 * ================ ManagedObject ===========================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.managedObjectContext = context;
		}

		@Override
		public Object getObject() {
			return this;
		}

		/*
		 * =============== ChallengeContext =========================
		 */

		@Override
		public HttpChallenge setChallenge(String authenticationScheme, String realm) {
			return this.managedObjectContext.run(() -> {
				if (this.challenge.length() > 0) {
					this.challenge.append(", ");
				}
				this.challenge.append(authenticationScheme);
				this.challenge.append(" realm=\"");
				this.challenge.append(realm);
				this.challenge.append("\"");
				return this;
			});
		}

		/*
		 * ================ HttpChallenge ============================
		 */

		@Override
		public void addParameter(String name, String value) {
			this.managedObjectContext.run(() -> {
				this.challenge.append(", ");
				this.challenge.append(name);
				this.challenge.append("=");
				this.challenge.append(value);
				return null;
			});
		}
	}

}
