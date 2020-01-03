/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.session;

import java.io.Serializable;
import java.time.Instant;
import java.util.Iterator;

import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.web.session.spi.HttpSessionStore;

/**
 * HTTP session.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSession {

	/**
	 * Obtains the session Id.
	 * 
	 * @return Session Id.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	String getSessionId() throws InvalidatedSessionHttpException;

	/**
	 * <p>
	 * Obtains the token name.
	 * <p>
	 * This is the name of the {@link HttpRequestCookie} that contains the
	 * session Id.
	 * 
	 * @return Token name.
	 */
	String getTokenName();

	/**
	 * Indicates if this is a new {@link HttpSession}.
	 * 
	 * @return <code>true</code> if this is a new {@link HttpSession}.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	boolean isNew() throws InvalidatedSessionHttpException;

	/**
	 * Obtains the time this {@link HttpSession} was created.
	 * 
	 * @return Time this {@link HttpSession} was created.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	Instant getCreationTime() throws InvalidatedSessionHttpException;

	/**
	 * Obtains the time this {@link HttpSession} will be expired should it be
	 * idle.
	 * 
	 * @return Time this {@link HttpSession} will be expired.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	Instant getExpireTime() throws InvalidatedSessionHttpException;

	/**
	 * <p>
	 * Specifies the time this {@link HttpSession} will expire if idle.
	 * <p>
	 * The {@link HttpSessionStore} may increment this time on further requests
	 * to keep the {@link HttpSession} active over a long conversation.
	 * 
	 * @param expireTime
	 *            Time to expire this {@link HttpSession}.
	 * @throws StoringSessionHttpException
	 *             Indicating the {@link HttpSession} is currently being stored
	 *             and can not be altered.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	void setExpireTime(Instant expireTime) throws StoringSessionHttpException, InvalidatedSessionHttpException;

	/**
	 * Obtains the {@link Object} that is bound to the name for this
	 * {@link HttpSession}.
	 * 
	 * @param name
	 *            Name.
	 * @return {@link Object} bound to the name or <code>null</code> if no
	 *         {@link Object} bound by the name.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	Serializable getAttribute(String name) throws InvalidatedSessionHttpException;

	/**
	 * Obtains an {@link Iterator} to the names of the bound {@link Object}
	 * instances.
	 * 
	 * @return {@link Iterator} to the names of the bound {@link Object}
	 *         instances.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	Iterator<String> getAttributeNames() throws InvalidatedSessionHttpException;

	/**
	 * Binds the {@link Object} to the name within this {@link HttpSession}.
	 * 
	 * @param name
	 *            Name.
	 * @param object
	 *            {@link Object}. Must be {@link Serializable} as the attributes
	 *            of this {@link HttpSession} may be serialised to larger,
	 *            cheaper memory stores or for clustered {@link HttpSession}
	 *            management sent over the network.
	 * @throws StoringSessionHttpException
	 *             Indicating the {@link HttpSession} is currently being stored
	 *             and can not be altered.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	void setAttribute(String name, Serializable object)
			throws StoringSessionHttpException, InvalidatedSessionHttpException;

	/**
	 * Removes the bound {@link Object} by the name from this
	 * {@link HttpSession}.
	 * 
	 * @param name
	 *            Name of bound {@link Object} to remove.
	 * @throws StoringSessionHttpException
	 *             Indicating the {@link HttpSession} is currently being stored
	 *             and can not be altered.
	 * @throws InvalidatedSessionHttpException
	 *             Indicating the {@link HttpSession} is invalidated.
	 */
	void removeAttribute(String name) throws StoringSessionHttpException, InvalidatedSessionHttpException;

	/**
	 * Obtains the {@link HttpSessionAdministration} to administer this
	 * {@link HttpSession}.
	 * 
	 * @return {@link HttpSessionAdministration} to administer this
	 *         {@link HttpSession}.
	 */
	HttpSessionAdministration getHttpSessionAdministration();

}
