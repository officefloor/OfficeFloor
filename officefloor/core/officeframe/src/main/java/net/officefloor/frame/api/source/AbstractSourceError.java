/*-
 * #%L
 * OfficeFrame
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
