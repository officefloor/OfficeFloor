/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.spring.webclient;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.reactive.OfficeFloorReactiveUtil;
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
		this.doWebClientTest(false);
	}

	/**
	 * Test using the {@link WebClient}.
	 */
	public void testCustomWebClient() throws Exception {
		this.doWebClientTest(false);
	}

	/**
	 * Undertakes the {@link WebClient} test.
	 * 
	 * @param isCustomWebClient Indicates whether to use custom {@link WebClient}.
	 */
	private void doWebClientTest(boolean isCustomWebClient) throws Exception {

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
		String result = client.get()
				.uri("http://localhost:7878/webclient?url=" + (isCustomWebClient ? "/" : "http://localhost:7878/"))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).block();
		assertEquals("Should obtain result", "\"TEST\"", result);
	}

	public static class WebClientLogic {
		public void service(@HttpQueryParameter("url") String url, WebClient client, AsynchronousFlow flow,
				ObjectResponse<String> response) {
			client.get().uri(url + "path").accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)
					.doOnError(OfficeFloorReactiveUtil.onError(flow))
					.subscribe(OfficeFloorReactiveUtil.send(flow, response));
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