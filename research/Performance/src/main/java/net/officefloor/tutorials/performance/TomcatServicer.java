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
package net.officefloor.tutorials.performance;

import net.officefloor.tutorials.performance.logic.HttpServletServicer;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;

/**
 * Tomcat {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServicer implements Servicer {

	private Tomcat server;

	@Override
	public int getPort() {
		return 8888;
	}

	@Override
	public int getMaximumConnectionCount() {
		return 10000;
	}

	@Override
	public void start() throws Exception {
		this.server = new Tomcat();
		this.server.setBaseDir(System.getProperty("java.io.tmpdir"));
		Connector connector = this.server.getConnector();
		connector.setPort(this.getPort());
		((AbstractProtocol) this.server.getConnector().getProtocolHandler())
				.setBacklog(25000);
		this.server.setSilent(true);
		this.server.addContext("/", System.getProperty("java.io.tmpdir"));
		this.server.addServlet("/", "test", new HttpServletServicer())
				.addMapping("/*");
		this.server.start();
	}

	@Override
	public void stop() throws Exception {
		this.server.stop();
	}

}