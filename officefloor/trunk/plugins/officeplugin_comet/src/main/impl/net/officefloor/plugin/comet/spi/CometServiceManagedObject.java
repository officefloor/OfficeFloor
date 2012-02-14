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

package net.officefloor.plugin.comet.spi;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link ManagedObject} for the {@link CometService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceManagedObject implements AsynchronousManagedObject,
		CoordinatingManagedObject<CometServiceManagedObject.Dependencies>,
		CometService {

	/**
	 * {@link CometPublisherManagedObject} dependency keys.
	 */
	public static enum Dependencies {
		SERVER_GWT_RPC_CONNECTION
	}

	/**
	 * {@link CometServiceManagedObjectSource}.
	 */
	private final CometServiceManagedObjectSource source;

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener async;

	/**
	 * {@link ServerGwtRpcConnection}.
	 */
	private ServerGwtRpcConnection<CometResponse> connection;

	/**
	 * Initiate.
	 * 
	 * @param source
	 *            {@link CometServiceManagedObjectSource}.
	 */
	public CometServiceManagedObject(CometServiceManagedObjectSource source) {
		this.source = source;
	}

	/*
	 * ====================== CometService ==========================
	 */

	@Override
	public void service() {

		// Obtain the Comet Request
		RPCRequest rpcRequest = this.connection.getRpcRequest();
		CometRequest cometRequest = (CometRequest) rpcRequest.getParameters()[0];

		// Retrieve or wait on events for Comet Request
		this.source.receiveOrWaitOnEvents(cometRequest.getInterests(),
				this.connection, this.async,
				cometRequest.getLastSequenceNumber());
	}

	@Override
	public long publishEvent(String listenerType, Object event, Object matchKey) {
		return this.source.publishEvent(
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, listenerType,
				event, matchKey);
	}

	@Override
	public long publishEvent(long sequenceNumber, String listenerType,
			Object event, Object matchKey) {
		return this.source.publishEvent(sequenceNumber, listenerType, event,
				matchKey);
	}

	@Override
	public void expire() {
		this.source.expire();
	}

	/*
	 * ===================== ManagedObject ============================
	 */

	@Override
	public void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		this.async = listener;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadObjects(ObjectRegistry<Dependencies> registry) {

		// Obtain the Server GWT RPC connection
		this.connection = (ServerGwtRpcConnection<CometResponse>) registry
				.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION);
	}

	@Override
	public Object getObject() {
		return this;
	}

}