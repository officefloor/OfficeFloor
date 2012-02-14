/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Optional interface to allow configuring the {@link SSLEngine} for use.
 *
 * @author Daniel Sagenschneider
 */
public interface SslEngineConfigurator {

	/**
	 * Invoked to have this {@link SslEngineConfigurator} initialise itself.
	 *
	 * @param context
	 *            {@link SSLContext} that will be used to create the
	 *            {@link SSLEngine} instances.
	 * @throws Exception
	 *             If fails to initialise (possibly because a protocol or cipher
	 *             is not supported).
	 */
	void init(SSLContext context) throws Exception;

	/**
	 * <p>
	 * Configures the {@link SSLEngine}.
	 * <p>
	 * It is anticipated that only a single instance will be create and
	 * therefore this method will be thread safe.
	 *
	 * @param engine
	 *            {@link SSLEngine} to configure.
	 */
	void configureSslEngine(SSLEngine engine);

}