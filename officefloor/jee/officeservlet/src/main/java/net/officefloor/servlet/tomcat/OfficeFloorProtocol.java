/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet.tomcat;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.Constants;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ProtocolHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorProtocol extends AbstractProtocol<Void> {

	/**
	 * {@link Log}.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Instantiate.
	 */
	public OfficeFloorProtocol() {
		super(new OfficeFloorEndPoint());
		setConnectionTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
		ConnectionHandler<Void> cHandler = new ConnectionHandler<>(this);
		setHandler(cHandler);
		getEndpoint().setHandler(cHandler);
	}

	/**
	 * Obtains the {@link OfficeFloorEndPoint}.
	 * 
	 * @return {@link OfficeFloorEndPoint}.
	 */
	public OfficeFloorEndPoint getOfficeFloorEndPoint() {
		return (OfficeFloorEndPoint) this.getEndpoint();
	}

	/*
	 * =================== ProtocolHandler =====================
	 */

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected String getNamePrefix() {
		return "OfficeFloor";
	}

	@Override
	protected String getProtocolName() {
		return "OfficeFloor";
	}

	/*
	 * =============== ProtocolHandler (unused) =================
	 */

	@Override
	public void addSslHostConfig(SSLHostConfig sslHostConfig) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	public SSLHostConfig[] findSslHostConfigs() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	public UpgradeProtocol[] findUpgradeProtocols() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected UpgradeProtocol getNegotiatedProtocol(String name) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected UpgradeProtocol getUpgradeProtocol(String name) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected Processor createProcessor() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected Processor createUpgradeProcessor(SocketWrapperBase<?> socket, UpgradeToken upgradeToken) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

}
