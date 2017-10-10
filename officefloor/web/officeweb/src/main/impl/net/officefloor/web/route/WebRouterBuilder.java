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
package net.officefloor.web.route;

/**
 * Builds the {@link WebRouter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouterBuilder {

	/**
	 * Adds a path.
	 * 
	 * @param path
	 *            Path. Use <code>{param}</code> to signify path parameters.
	 * @param handler
	 *            {@link WebRouteHandler} for the path.
	 * @return <code>this</code> for builder pattern.
	 */
	public WebRouterBuilder addPath(String path, WebRouteHandler handler) {
		return this;
	}

	/**
	 * Builds the {@link WebRouter}.
	 * 
	 * @return {@link WebRouter}.
	 */
	public WebRouter build() {
		return null;
	}

}