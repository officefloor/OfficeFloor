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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Nginx {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class NginxServicer implements Servicer {

	@Override
	public int getPort() {
		return 81;
	}

	@Override
	public int getMaximumConnectionCount() {
		return 10000;
	}

	@Override
	public void start() throws Exception {

		// Ensure Nginx running
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(new HttpGet(
					"http://localhost:" + this.getPort() + "/test.php"));
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("Nginx seems to not be running");
			}
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	@Override
	public void stop() throws Exception {
		// Just leave running
	}

}