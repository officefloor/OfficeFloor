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
package net.officefloor.server.ssl;

import javax.net.ssl.SSLContext;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Source for {@link SSLContext} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SslContextSource {

	/**
	 * Creates a new {@link SSLContext}.
	 * 
	 * @param context
	 *            {@link SourceContext} to configure the {@link SSLContext}.
	 * @return New {@link SSLContext} ready for use.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext} (possibly because a
	 *             protocol or cipher is not supported).
	 */
	SSLContext createSslContext(SourceContext context) throws Exception;

}