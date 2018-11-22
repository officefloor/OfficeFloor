/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.tutorial.transactionhttpserver;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Transaction HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionHttpServerTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private static final String POST_CONTENT = "Interesting post article";

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void createPost() throws Exception {

		// Ensure content not in database
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/posts"));
		response.assertResponse(200, mapper.writeValueAsString(Arrays.asList()), "Content-Type", "application/json");

		// Create the post entry
		response = this.server.send(
				MockHttpServer.mockRequest("/posts").method(HttpMethod.POST).header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new Post(null, POST_CONTENT))));
		response.assertResponse(200, mapper.writeValueAsString(new Post(1, POST_CONTENT)), "Content-Type",
				"application/json");

		// Ensure post persisted
		response = this.server.send(MockHttpServer.mockRequest("/posts"));
		response.assertResponse(200, mapper.writeValueAsString(Arrays.asList(new Post(1, POST_CONTENT))),
				"Content-Type", "application/json");
	}

	// START SNIPPET: rollback
	@Test
	public void rollback() throws Exception {

		// Attempt to create (but should roll back)
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/rollback").method(HttpMethod.POST)
				.header("Content-Type", "application/json")
				.entity(mapper.writeValueAsString(new Post(null, POST_CONTENT))));
		response.assertResponse(500, "rolled back");

		// Ensure not persisted to database
		response = this.server.send(MockHttpServer.mockRequest("/posts"));
		response.assertResponse(200, mapper.writeValueAsString(Arrays.asList()), "Content-Type", "application/json");
	}
	// END SNIPPET: rollback

	// START SNIPPET: commit
	@Test
	public void commit() throws Exception {

		// Create, will handle exception and commit
		MockHttpResponse response = this.server.send(
				MockHttpServer.mockRequest("/commit").method(HttpMethod.POST).header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new Post(null, POST_CONTENT))));
		response.assertResponse(201, "committed");

		// Ensure persisted to database
		response = this.server.send(MockHttpServer.mockRequest("/posts"));
		response.assertResponse(200,
				mapper.writeValueAsString(Arrays.asList(new Post(1, POST_CONTENT), new Post(2, "Additional"))),
				"Content-Type", "application/json");
	}
	// END SNIPPET: commit

}