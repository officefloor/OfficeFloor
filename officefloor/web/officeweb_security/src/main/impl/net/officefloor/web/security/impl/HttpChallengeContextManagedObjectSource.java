/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
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
	 * @param httpChallengeContext
	 *            {@link HttpChallengeContext}.
	 * @param response
	 *            {@link HttpResponse}.
	 */
	public static void loadHttpChallenge(HttpChallengeContext httpChallengeContext, HttpResponse response) {

		// Obtain the challenge
		HttpChallengeContextManagedObject managedObject = (HttpChallengeContextManagedObject) httpChallengeContext;

		// Determine if challenge and if so send challenge
		managedObject.processAwareContext.run(() -> {
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
			implements ProcessAwareManagedObject, HttpChallengeContext, HttpChallenge {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private ProcessAwareContext processAwareContext;

		/**
		 * {@link StringBuilder} to load the {@link HttpChallenge}.
		 */
		private final StringBuilder challenge = new StringBuilder(128);

		/*
		 * ================ ManagedObject ===========================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			this.processAwareContext = context;
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
			return this.processAwareContext.run(() -> {
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
			this.processAwareContext.run(() -> {
				this.challenge.append(", ");
				this.challenge.append(name);
				this.challenge.append("=");
				this.challenge.append(value);
				return null;
			});
		}
	}

}