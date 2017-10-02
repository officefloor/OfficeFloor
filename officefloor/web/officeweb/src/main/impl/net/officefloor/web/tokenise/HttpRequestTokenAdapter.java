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
package net.officefloor.web.tokenise;

/**
 * Adapter for the {@link HttpRequestTokenHandler} so that need not implement
 * every method.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTokenAdapter implements HttpRequestTokenHandler {

	/*
	 * ===================== HttpRequestTokenHandler ==========================
	 */

	@Override
	public void handlePath(String path) throws HttpRequestTokeniseException {
		// Do nothing by default
	}

	@Override
	public void handleHttpParameter(String name, String value)
			throws HttpRequestTokeniseException {
		// Do nothing by default
	}

	@Override
	public void handleQueryString(String queryString)
			throws HttpRequestTokeniseException {
		// Do nothing by default
	}

	@Override
	public void handleFragment(String fragment)
			throws HttpRequestTokeniseException {
		// Do nothing by default
	}

}