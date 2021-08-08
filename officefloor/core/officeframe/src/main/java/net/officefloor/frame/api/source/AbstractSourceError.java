/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.source;

/**
 * Abstract source error.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSourceError extends Error {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Potential {@link ServiceFactory} requiring the property.
	 */
	private final ServiceFactory<?> serviceFactory;

	/**
	 * Instantiate with message.
	 * 
	 * @param message Message.
	 */
	public AbstractSourceError(String message) {
		super(message);
		this.serviceFactory = null;
	}

	/**
	 * Instantiate with message and cause.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public AbstractSourceError(String message, Throwable cause) {
		super(message, cause);
		this.serviceFactory = null;
	}

	/**
	 * Instantiate to propagate {@link ServiceFactory} source error.
	 * 
	 * @param error          {@link AbstractSourceError} from the
	 *                       {@link ServiceFactory}.
	 * @param serviceFactory {@link ServiceFactory}.
	 */
	public AbstractSourceError(AbstractSourceError error, ServiceFactory<?> serviceFactory) {
		super(error.getMessage() + " for service factory " + serviceFactory.getClass().getName());
		this.serviceFactory = serviceFactory;
	}

	/**
	 * Obtains the potential {@link ServiceFactory} requiring the property.
	 * 
	 * @return {@link ServiceFactory} requiring the property. May be
	 *         <code>null</code> if not {@link ServiceFactory} requiring the
	 *         property.
	 */
	public ServiceFactory<?> getServiceFactory() {
		return this.serviceFactory;
	}

	/**
	 * Adds the issue to the {@link IssueTarget}.
	 * 
	 * @param issueTarget {@link IssueTarget}.
	 */
	public void addIssue(IssueTarget issueTarget) {
		Throwable cause = this.getCause();
		if (cause != null) {
			issueTarget.addIssue(this.getMessage(), cause);
		} else {
			issueTarget.addIssue(this.getMessage());
		}
	}

}
