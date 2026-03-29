package net.officefloor.server.google.function.wrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.tokenise.HttpRequestTokeniser;

/**
 * {@link HttpFunction} {@link com.google.cloud.functions.HttpRequest}.
 */
public class HttpFunctionHttpRequest implements com.google.cloud.functions.HttpRequest {

	/**
	 * {@link net.officefloor.server.http.HttpRequest}.
	 */
	private net.officefloor.server.http.HttpRequest request;

	/**
	 * Query parameters.
	 */
	private final Map<String, List<String>> queryParameters = new HashMap<>();

	/**
	 * Headers.
	 */
	private final Map<String, List<String>> headers = new HashMap<>();

	/**
	 * {@link URI} of request.
	 */
	private URI uri;

	/**
	 * Indicates if tokenised.
	 */
	private boolean isTokenised = false;

	/**
	 * {@link BufferedReader}.
	 */
	private BufferedReader reader = null;

	/**
	 * Instantiate.
	 * 
	 * @param request {@link net.officefloor.server.http.HttpRequest}.
	 */
	public HttpFunctionHttpRequest(net.officefloor.server.http.HttpRequest request) {
		this.request = request;
	}

	/**
	 * Ensures the request is tokenised.
	 */
	private void ensureTokenised() {
		if (!this.isTokenised) {

			// Obtain the URI
			try {
				this.uri = new URI(this.request.getUri());
			} catch (URISyntaxException ex) {
				throw new IllegalStateException("Should always be valid URI", ex);
			}

			// Tokenise the request
			HttpRequestTokeniser.tokeniseHttpRequest(this.request, null, (name, value, type) -> {
				switch (type) {
				case HEADER:
					loadValue(name, value, HttpFunctionHttpRequest.this.headers);
					break;

				case QUERY:
					loadValue(name, value, HttpFunctionHttpRequest.this.queryParameters);
					break;

				default:
					// ignore
				}
			});

			// Now tokenised
			this.isTokenised = true;
		}
	}

	/**
	 * Loads the value.
	 * 
	 * @param name       Name.
	 * @param value      Value.
	 * @param nameValues Name/values to load with name/value.
	 */
	private static void loadValue(String name, String value, Map<String, List<String>> nameValues) {
		List<String> values = nameValues.get(name);
		if (values == null) {
			values = new LinkedList<>();
			nameValues.put(name, values);
		}
		values.add(value);
	}
	
	/*
	 * ===================== HttpRequest ======================
	 */

	@Override
	public String getUri() {
		return this.request.getUri();
	}

	@Override
	public String getMethod() {
		return this.request.getMethod().getName();
	}

	@Override
	public String getPath() {
		this.ensureTokenised();
		return this.uri.getPath();
	}

	@Override
	public Optional<String> getQuery() {
		this.ensureTokenised();
		return Optional.ofNullable(this.uri.getQuery());
	}

	@Override
	public Map<String, List<String>> getQueryParameters() {
		this.ensureTokenised();
		return this.queryParameters;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		this.ensureTokenised();
		return this.headers;
	}

	@Override
	public Optional<String> getContentType() {
		HttpHeader header = this.request.getHeaders().getHeader("Content-Type");
		return header != null ? Optional.of(header.getValue()) : Optional.empty();
	}

	@Override
	public Optional<String> getCharacterEncoding() {
		Optional<String> contentType = this.getContentType();
		if (contentType.isEmpty()) {
			return Optional.empty();
		} else {
			String contentTypeValue = contentType.get();
			String[] contentTypeParts = contentTypeValue.split(";");
			for (int i = 1; i < contentTypeParts.length; i++) {
				String[] parameterParts = contentTypeParts[i].split("=");
				String parameterName = parameterParts[0].trim();
				if ("charset".equalsIgnoreCase(parameterName)) {
					String[] valueParts = Arrays.copyOfRange(parameterParts, 1, parameterParts.length);
					return Optional.of(String.join("", valueParts));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public long getContentLength() {
		HttpHeader header = this.request.getHeaders().getHeader("Content-Length");
		return header != null ? Long.parseLong(header.getValue()) : -1;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.request.getEntity();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (this.reader == null) {
			InputStream entity = this.request.getEntity();
			this.reader = new BufferedReader(
					new InputStreamReader(entity, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		}
		return this.reader;
	}

	@Override
	public Map<String, HttpPart> getParts() {
		throw new UnsupportedOperationException(
				"Parts not supported by " + MockGoogleHttpFunctionExtension.class.getSimpleName());
	}

}