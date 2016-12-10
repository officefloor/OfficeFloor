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
package net.officefloor.plugin.gwt.service;

import net.officefloor.plugin.socket.server.http.HttpRequest;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * <p>
 * Server connection for a GWT RPC client.
 * <p>
 * To send the response use the appropriate {@link AsyncCallback} method. This
 * is to enable the injection of an {@link AsyncCallback} as a dependency.
 * <p>
 * Should there be a failure sending a response the {@link AsyncCallback}
 * methods will propagate an {@link ServerGwtRpcConnectionException}.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ServerGwtRpcConnectionException
 */
public interface ServerGwtRpcConnection<T> extends AsyncCallback<T> {

	/**
	 * <p>
	 * Specifies the required return type.
	 * <p>
	 * Once specified the return type can only be made more specific - e.g. may
	 * be specified as {@link CharSequence} then as {@link String} but not
	 * subsequently as a different type of {@link Integer}.
	 * 
	 * @param returnType
	 *            Required return type.
	 */
	void setReturnType(Class<?> returnType);

	/**
	 * Obtains the {@link RPCRequest}.
	 * 
	 * @return {@link RPCRequest}.
	 */
	RPCRequest getRpcRequest();

	/**
	 * <p>
	 * Obtains the underlying {@link HttpRequest}.
	 * <p>
	 * The body of the {@link HttpRequest} will have already been consumed.
	 * 
	 * @return Underlying {@link HttpRequest}.
	 */
	HttpRequest getHttpRequest();

}