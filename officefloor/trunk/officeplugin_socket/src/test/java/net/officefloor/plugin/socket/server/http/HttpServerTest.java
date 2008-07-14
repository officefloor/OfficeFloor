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
package net.officefloor.plugin.socket.server.http;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.impl.socket.server.ServerSocketHandlerEnum;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel
 */
public class HttpServerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Starting port number.
	 */
	public static int portStart = 12643;

	/**
	 * Port number to use for testing.
	 */
	public static int PORT;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Specify the port
		PORT = portStart;
		portStart++; // increment for next test

		// Register the Server Socket Managed Object
		ManagedObjectBuilder<?> serverSocketBuilder = this
				.constructManagedObject("MO",
						HttpServerSocketManagedObjectSource.class, "OFFICE");
		serverSocketBuilder.addProperty("port", String.valueOf(PORT));
		serverSocketBuilder.addProperty("buffer_size", "1024");
		serverSocketBuilder.addProperty("message_size", "3");
		serverSocketBuilder.setDefaultTimeout(3000);

		// Register the necessary teams for socket listening
		this.constructTeam("ACCEPTER_TEAM", new OnePersonTeam(100));
		this.constructTeam("LISTENER_TEAM", new WorkerPerTaskTeam("Listener"));
		this.constructTeam("CLEANUP_TEAM", new PassiveTeam());
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		officeBuilder.registerTeam("of-MO.serversocket." + PORT
				+ ".Accepter.TEAM", "of-ACCEPTER_TEAM");
		officeBuilder.registerTeam("of-MO.serversocket." + PORT
				+ ".Listener.TEAM", "of-LISTENER_TEAM");

		// Provide the process managed object to the office
		officeBuilder.addProcessManagedObject("MO", "MO");

		// Register team to do the work
		this.constructTeam("WORKER", new OnePersonTeam(100));

		// Register the work to process messages
		ReflectiveWorkBuilder workBuilder = this.constructWork(new HttpWork(),
				"servicer", "service");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("service",
				"WORKER");
		taskBuilder.buildObject("P-MO", "MO");

		// Link handler to task
		officeBuilder.addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context)
					throws BuildException {
				// Obtain the managed object handler builder
				ManagedObjectHandlerBuilder<ServerSocketHandlerEnum> handlerBuilder = context
						.getManagedObjectHandlerBuilder("of-MO",
								ServerSocketHandlerEnum.class);

				// Link in the handler task
				handlerBuilder.registerHandler(
						ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER)
						.linkProcess(0, "servicer", "service");
			}
		});

		// Create the Office Floor
		this.officeFloor = this.constructOfficeFloor("OFFICE");

		// Open the Office Floor
		this.officeFloor.openOfficeFloor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

		// Close the office
		this.officeFloor.closeOfficeFloor();

		// Clean up
		super.tearDown();
	}

	/**
	 * Ensures can handle a GET request.
	 */
	public void testGetRequest() throws Exception {

		// Create the request
		HttpUriRequest request = new HttpGet("http://localhost:" + PORT
				+ "/path");

		// Obtain the response
		HttpResponse response = this.doRequest(request);

		// Obtain the response body
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect response body", "Hello World", responseBody);

		// Output the response entity
		System.out.println("GET response body: " + responseBody);
	}

	/**
	 * Ensure can handle a POST request.
	 */
	public void testPostRequest() throws Exception {

		// Create the request
		HttpUriRequest request = new HttpPost("http://localhost:" + PORT
				+ "/path");

		// Obtain the response
		HttpResponse response = this.doRequest(request);

		// Obtain the response body
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect response body", "Hello World", responseBody);

		// Output the response entity
		System.out.println("POST response body: " + responseBody);
	}

	/**
	 * Does the {@link HttpUriRequest}.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @return Resulting {@link HttpResponse}.
	 */
	private HttpResponse doRequest(HttpUriRequest request) throws Exception {

		// Create and initiate the client
		DefaultHttpClient client = new DefaultHttpClient();
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0,
				false));

		// Do the request and obtain the response
		HttpResponse response = client.execute(request);
		assertNotNull(response);

		// Return the response
		return response;
	}

}
