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

import com.google.cloud.firestore.Firestore;

import net.officefloor.nosql.firestore.test.AbstractFirestoreConnectJunit.Configuration;
import net.officefloor.nosql.firestore.test.FirestoreConnectExtension;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.UsesDockerTest;

/**
 * Integration tests the SAM application started with maven plugin.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class GoogleFunctionIT extends AbstractGoogleFunctionTestCase {

	public final @RegisterExtension HttpClientExtension client = new HttpClientExtension(false, 8181).timeout(30_000);

	public final @RegisterExtension FirestoreConnectExtension firestore = new FirestoreConnectExtension(
			new Configuration().port(8185));

	/*
	 * ===================== AbstractGoogleFunctionTestCase =====================
	 */

	@Override
	protected Firestore getFirestore() {
		return this.firestore.getFirestore();
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
