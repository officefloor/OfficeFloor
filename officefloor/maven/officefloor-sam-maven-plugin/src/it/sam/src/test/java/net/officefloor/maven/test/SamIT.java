/*-
 * #%L
 * OfficeFloor SAM Maven Plugin
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.maven.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import net.officefloor.nosql.dynamodb.test.DynamoDbConnectExtension;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.UsesAwsTest;
import net.officefloor.test.UsesDockerTest;

/**
 * Integration tests the SAM application started with maven plugin.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
@UsesAwsTest
public class SamIT extends AbstractSamTestCase {

	public final @RegisterExtension HttpClientExtension client = new HttpClientExtension(false, 8181).timeout(30_000);

	public final @RegisterExtension DynamoDbConnectExtension connect = new DynamoDbConnectExtension();

	/*
	 * ===================== AbstractSamTestCase =====================
	 */

	@Override
	protected DynamoDBMapper getDynamoDbMapper() {
		return this.connect.getDynamoDbMapper();
	}

	@Override
	protected Response doRequest(HttpMethod method, String path, String entity, String... headerNameValues) {
		try {

			// Create the request
			String requestUri = this.client.url(path);
			HttpUriRequest request;
			switch (method.getEnum()) {

			case GET:
				request = new HttpGet(requestUri);
				break;

			case POST:
				HttpPost post = new HttpPost(requestUri);
				if (entity != null) {
					post.setEntity(new StringEntity(entity));
				}
				request = post;
				break;

			default:
				return fail("Unsupported HTTP method " + method);
			}

			// Provide the headers
			for (int i = 0; i < headerNameValues.length; i += 2) {
				String name = headerNameValues[i];
				String value = headerNameValues[i + 1];
				request.setHeader(name, value);
			}

			// Execute and wrap with response check
			return new ClientResponse(this.client.execute(request));

		} catch (IOException ex) {
			return fail(ex);
		}
	}

	/**
	 * {@link Response} for {@link HttpClientExtension}.
	 */
	private static class ClientResponse extends Response {

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse response;

		/**
		 * Instantiate.
		 * 
		 * @param response {@link HttpResponse}.
		 */
		private ClientResponse(HttpResponse response) {
			this.response = response;
		}

		/*
		 * ===================== Response ============================
		 */

		@Override
		public void assertResponse(int statusCode, String entity, String... headerNameValues) {
			try {
				HttpEntity httpEntity = this.response.getEntity();
				String actualEntity = httpEntity != null ? EntityUtils.toString(httpEntity) : "";
				assertEquals(statusCode, this.response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
				assertEquals(entity == null ? "" : entity, actualEntity, "Incorrect entity");
				for (int i = 0; i < headerNameValues.length; i += 2) {
					String name = headerNameValues[i];
					String value = headerNameValues[i + 1];
					Header header = this.response.getFirstHeader(name);
					assertNotNull(header, "No header '" + name + "'");
					assertEquals(value, header.getValue(), "Incorrect header value for " + name);
				}
			} catch (IOException ex) {
				fail(ex);
			}
		}
	}

}
