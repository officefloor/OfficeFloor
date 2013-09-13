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
package net.officefloor.plugin.bayeux.transport;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.ServerMessage;

/**
 * Transport {@link Message}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TransportMessage extends ServerMessage {

	/**
	 * Name of &quot;authSuccessful&quot; field.
	 */
	static String AUTH_SUCCESSFUL_FIELD = "authSuccessful";

	/**
	 * Obtains the {@link Message#VERSION_FIELD}.
	 * 
	 * @return Version.
	 */
	String getVersion();

	/**
	 * Obtains the {@link Message#SUPPORTED_CONNECTION_TYPES_FIELD}.
	 * 
	 * @return Supported connection types.
	 */
	String[] getSupportedConnectionTypes();

	/**
	 * Obtains the {@link #AUTH_SUCCESSFUL_FIELD}.
	 * 
	 * @return Indicates if authentication successful.
	 */
	Boolean isAuthSuccessful();

	/**
	 * Obtains the {@link Message#ERROR_FIELD}.
	 * 
	 * @return Error.
	 */
	String getError();

	/**
	 * Mutable {@link TransportMessage}.
	 */
	public interface TransportMutable extends TransportMessage, Mutable {

		/**
		 * Specifies the {@link Message#VERSION_FIELD}.
		 * 
		 * @param version
		 *            Version.
		 */
		void setVersion(String version);

		/**
		 * Specifies the {@link Message#SUPPORTED_CONNECTION_TYPES_FIELD}.
		 * 
		 * @param supportedConnectionTypes
		 *            Supported connection types.
		 */
		void setSupportedConnectionTypes(String... supportedConnectionTypes);
	}

}