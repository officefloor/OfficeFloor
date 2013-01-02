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
package net.officefloor.plugin.socket.server.ssl;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * <p>
 * Anonymous {@link SslEngineConfigurator}.
 * <p>
 * <b>This should NOT be used within production.</b> The purpose is to allow
 * testing of HTTPS communication without needing {@link KeyStore} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousSslEngineConfigurator implements SslEngineConfigurator {

	@Override
	public void init(SSLContext context) throws Exception {
		// Nothing to initialise for context
	}

	@Override
	public void configureSslEngine(SSLEngine engine) {
		// Allow anonymous connection
		engine.setEnabledCipherSuites(engine.getSupportedCipherSuites());
	}

}