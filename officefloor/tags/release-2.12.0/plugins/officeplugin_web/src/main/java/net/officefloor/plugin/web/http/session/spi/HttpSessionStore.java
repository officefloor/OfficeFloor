/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.session.spi;

import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * <p>
 * Provides a store (or cache) of {@link HttpSession} instances.
 * <p>
 * This interfaces allows customising the storage of {@link HttpSession}
 * instances. Different storage strategies can be used to suit the deployment of
 * the application. For example a different strategy may be chosen if the
 * application is hosted on a single server or across a cluster.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionStore {

	/**
	 * Triggers creating new details for a {@link HttpSession}.
	 *
	 * @param operation
	 *            Operation to receive results of creation.
	 */
	void createHttpSession(CreateHttpSessionOperation operation);

	/**
	 * Triggers retrieving the {@link HttpSession} details.
	 *
	 * @param operation
	 *            Operation to receive results of the retrieval.
	 */
	void retrieveHttpSession(RetrieveHttpSessionOperation operation);

	/**
	 * Triggers storing the {@link HttpSession} details.
	 *
	 * @param operation
	 *            Operations to receive the results of the storage.
	 */
	void storeHttpSession(StoreHttpSessionOperation operation);

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param operation
	 *            Operation to invalidate the {@link HttpSession}.
	 */
	void invalidateHttpSession(InvalidateHttpSessionOperation operation);

}