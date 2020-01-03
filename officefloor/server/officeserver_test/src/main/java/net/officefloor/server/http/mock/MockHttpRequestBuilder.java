package net.officefloor.server.http.mock;

import java.io.OutputStream;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;

/**
 * Builder for a mock {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpRequestBuilder {

	/**
	 * Flags with the {@link HttpRequest} is secure.
	 * 
	 * @param isSecure
	 *            <code>true</code> if secure {@link HttpRequest}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder secure(boolean isSecure);

	/**
	 * Specifies the {@link HttpMethod}.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder method(HttpMethod method);

	/**
	 * Specifies the request URI.
	 * 
	 * @param requestUri
	 *            Request URI.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder uri(String requestUri);

	/**
	 * Specifies the {@link HttpVersion}.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder version(HttpVersion version);

	/**
	 * Adds a {@link HttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder header(String name, String value);

	/**
	 * Adds a {@link HttpRequestCookie}.
	 * 
	 * @param name
	 *            {@link HttpRequestCookie} name.
	 * @param value
	 *            {@link HttpRequestCookie} value.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder cookie(String name, String value);

	/**
	 * <p>
	 * Adds all the appropriate {@link WritableHttpCookie} instances from the
	 * {@link MockHttpResponse}.
	 * <p>
	 * This is a convenience method to enable sending back
	 * {@link HttpRequestCookie} instances received on a previous
	 * {@link MockHttpResponse}.
	 * 
	 * @param response
	 *            {@link MockHttpResponse}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder cookies(MockHttpResponse response);

	/**
	 * Sets the HTTP entity.
	 * 
	 * @param entity
	 *            Entity content.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder entity(String entity);

	/**
	 * Obtains the {@link OutputStream} to write the HTTP entity.
	 * 
	 * @return {@link OutputStream} to write the HTTP entity.
	 */
	OutputStream getHttpEntity();

	/**
	 * Flags to turn off checks for {@link HttpRequest} and provide efficient
	 * processing.
	 * 
	 * @param isStress
	 *            <code>true</code> to turn off checks and process more
	 *            efficiently.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder setEfficientForStressTests(boolean isStress);

	/**
	 * <p>
	 * Builds a mock {@link HttpRequest} from this
	 * {@link MockHttpRequestBuilder} configuration.
	 * <p>
	 * This is useful for testing to create a mock {@link HttpRequest}.
	 * 
	 * @return Mock {@link HttpRequest}.
	 */
	HttpRequest build();

}