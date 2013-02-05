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
package net.officefloor.plugin.web.http.application;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireSectionImpl;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * Allows wiring the flows of the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityAutoWireSectionImpl extends AutoWireSectionImpl
		implements HttpSecurityAutoWireSection {

	/**
	 * {@link Class} of the {@link HttpSecuritySource}.
	 */
	private final Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass;

	/**
	 * By default allow 10 seconds before timing out.
	 */
	private long securityTimeout = 10 * 1000;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param httpSecuritySourceClass
	 *            {@link HttpSecuritySource} class.
	 */
	public HttpSecurityAutoWireSectionImpl(
			OfficeFloorCompiler compiler,
			AutoWireSection section,
			Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass) {
		super(compiler, section);
		this.httpSecuritySourceClass = httpSecuritySourceClass;
	}

	/*
	 * ====================== HttpSecurityAutoWireSection =====================
	 */

	@Override
	public Class<? extends HttpSecuritySource<?, ?, ?, ?>> getHttpSecuritySourceClass() {
		return this.httpSecuritySourceClass;
	}

	@Override
	public long getSecurityTimeout() {
		return this.securityTimeout;
	}

	@Override
	public void setSecurityTimeout(long timeout) {
		this.securityTimeout = timeout;
	}

}