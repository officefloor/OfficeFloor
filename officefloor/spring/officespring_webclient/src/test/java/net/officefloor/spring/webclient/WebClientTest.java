/*-
 * #%L
 * Spring WebClient
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

package net.officefloor.spring.webclient;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.reactive.ReactiveWoof;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;

/**
 * Tests integrating {@link WebClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebClientTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Test using the {@link WebClient}.
	 */
	public void testWebClient() throws Exception {
		this.doWebClientTest(false, "http://localhost:7878/path", false, 200, "\"TEST\"");
	}

	/**
	 * Test using the {@link WebClient}.
	 */
	public void testCustomWebClient() throws Exception {
		this.doWebClientTest(true, "/path", false, 200, "\"TEST\"");
	}

	/**
	 * Ensure able to send raw error.
	 */
	public void testNotPropagateHttpError() throws Exception {
		this.doWebClientTest(false, "http://localhost:7878/not-found", false, 500,
				"{\"error\":\"404 Not Found from GET http://localhost:7878/not-found\"}");
	}

	/**
	 * Ensure able to propagate {@link WebClient} error.
	 */
	public void testPropagateHttpError() throws Exception {
		this.doWebClientTest(false, "http://localhost:7878/not-found", true, 404,
				"{\"error\":\"" + WebClientResponseException.create(404,
						"Not Found from GET http://localhost:7878/not-found", null, null, null) + "\"}");
	}

	/**
	 * Undertakes the {@link WebClient} test.
	 * 
	 * @param isCustomWebClient    Indicates whether to use custom
	 *                             {@link WebClient}.
	 * @param url                  URL to make {@link WebClient} call from within
	 *                             {@link OfficeFloor}.
	 * @param isPropagateHttpError Indicates whether to propagate
	 *                             {@link HttpException}.
	 * @param expectedStatus       Expected status.
	 * @param expectedBody         Expected body.
	 */
	private void doWebClientTest(boolean isCustomWebClient, String url, boolean isPropagateHttpError,
			int expectedStatus, String expectedBody) throws Exception {

		// Create the server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.officeFloor((context) -> {
			new HttpServer(
					context.getDeployedOffice().getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME,
							WebArchitect.HANDLER_INPUT_NAME),
					context.getOfficeFloorDeployer(), context.getOfficeFloorSourceContext());
		});
		compiler.web((context) -> {

			// Add the web client
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("WEB_CLIENT",
					WebClientManagedObjectSource.class.getName());
			if (isCustomWebClient) {
				mos.addProperty(WebClientManagedObjectSource.PROPERTY_WEB_CLIENT_BUILDER_FACTORY,
						MockWebClientBuilderFactory.class.getName());
			}
			mos.addOfficeManagedObject("WEB_CLIENT", ManagedObjectScope.THREAD);

			// Configure servicing
			context.link(false, "/webclient", WebClientLogic.class);
			context.link(false, "/path", ResponseLogic.class);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Ensure can obtain data with web client
		WebClient client = WebClient.create();
		ThreadSafeClosure<Integer> statusCode = new ThreadSafeClosure<>();
		String responseBody = client.get()
				.uri("http://localhost:7878/webclient?url=" + url + "&propagate="
						+ Boolean.toString(isPropagateHttpError))
				.accept(MediaType.APPLICATION_JSON).exchangeToMono(clientResponse -> {
					statusCode.set(clientResponse.statusCode().value());
					return clientResponse.bodyToMono(String.class);
				}).block();
		assertEquals("Incorrect status", expectedStatus, statusCode.get().intValue());
		assertEquals("Should obtain result", expectedBody, responseBody);
	}

	public static class WebClientLogic {
		public void service(@HttpQueryParameter("url") String url, @HttpQueryParameter("propagate") String isPropagate,
				WebClient client, AsynchronousFlow flow, ObjectResponse<String> response) {
			client.get().uri(url).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).subscribe(
					ReactiveWoof.send(flow, response),
					Boolean.valueOf(isPropagate) ? ReactiveWoof.propagateHttpError(flow)
							: ReactiveWoof.flowError(flow));
		}
	}

	public static class ResponseLogic {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	public static class MockWebClientBuilderFactory implements WebClientBuilderFactory {

		@Override
		public Builder createWebClientBuilder(SourceContext context) throws Exception {
			return WebClient.builder().baseUrl("http://localhost:7878");
		}
	}

}
