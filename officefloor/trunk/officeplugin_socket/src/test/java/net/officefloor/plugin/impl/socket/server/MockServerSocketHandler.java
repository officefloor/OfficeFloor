/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * Test {@link ServerSocketHandler}.
 * 
 * @author Daniel
 */
public class MockServerSocketHandler implements ServerSocketHandler<Indexed> {

	/**
	 * {@link ConnectionHandler}.
	 */
	private final ConnectionHandler connectionHandler;

	/**
	 * Initiate.
	 * 
	 * @param connectionHandler
	 *            {@link ConnectionHandler}.
	 */
	public MockServerSocketHandler(ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	/*
	 * ============================================================================
	 * ServerSocketHandler
	 * ============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ServerSocketHandler#createConnectionHandler(net.officefloor.plugin.socket.server.spi.Connection)
	 */
	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return this.connectionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ServerSocketHandler#createServer()
	 */
	@Override
	public Server createServer() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
	 */
	@Override
	public void setHandlerContext(HandlerContext<Indexed> context)
			throws Exception {
		TestCase.fail("Should not be invoked");
	}

}
