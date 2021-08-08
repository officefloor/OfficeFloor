/*-
 * #%L
 * Spring Web Flux Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.webflux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpRequestState;
import reactor.core.publisher.Flux;

/**
 * {@link OfficeFloor} {@link ServerHttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServerHttpRequest extends AbstractServerHttpRequest implements SslInfo {

	/**
	 * Obtains the {@link URI} from the {@link HttpRequest}.
	 * 
	 * @param request {@link HttpRequest}.
	 * @return {@link URI}.
	 * @throws URISyntaxException If fails to create {@link URI}.
	 */
	private static URI uri(HttpRequest request) throws URISyntaxException {
		return new URI(request.getUri());
	}

	/**
	 * Obtains the {@link HttpHeaders} from the {@link HttpRequest}.
	 * 
	 * @param request {@link HttpRequest}.
	 * @return {@link HttpHeaders}.
	 */
	private static HttpHeaders httpHeaders(HttpRequest request) {
		HttpHeaders headers = new HttpHeaders();
		for (HttpHeader header : request.getHeaders()) {
			headers.add(header.getName(), header.getValue());
		}
		return headers;
	}

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest httpRequest;

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState;

	/**
	 * {@link DataBufferFactory}.
	 */
	private final DataBufferFactory dataBufferFactory;

	/**
	 * Instantiate.
	 * 
	 * @param httpRequest       {@link HttpRequest}.
	 * @param requestState      {@link HttpRequestState}.
	 * @param contextPath       Context path.
	 * @param dataBufferFactory {@link DataBufferFactory}.
	 * @throws URISyntaxException If fails to create {@link URI}.
	 */
	public OfficeFloorServerHttpRequest(HttpRequest httpRequest, HttpRequestState requestState, String contextPath,
			DataBufferFactory dataBufferFactory) throws URISyntaxException {
		super(uri(httpRequest), contextPath, httpHeaders(httpRequest));
		this.httpRequest = httpRequest;
		this.requestState = requestState;
		this.dataBufferFactory = dataBufferFactory;
	}

	/**
	 * Loads the path parameters.
	 * 
	 * @param exchange {@link ServerWebExchange}.
	 */
	public void loadPathParameters(ServerWebExchange exchange) {

		// Load the path parameters
		Map<String, String> pathParameters = new HashMap<>();
		this.requestState.loadValues((name, value, location) -> {
			if (location == HttpValueLocation.PATH) {
				pathParameters.put(name, value);
			}
		});

		// Load the path parameters
		exchange.getAttributes().put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathParameters);
	}

	/*
	 * ======================= ServerHttpRequest ========================
	 */

	@Override
	protected SslInfo initSslInfo() {
		return this;
	}

	@Override
	public String getMethodValue() {
		return this.httpRequest.getMethod().getName();
	}

	@Override
	protected MultiValueMap<String, HttpCookie> initCookies() {
		MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
		this.requestState.loadValues((name, value, location) -> {
			if (HttpValueLocation.COOKIE.equals(location)) {
				cookies.add(name, new HttpCookie(name, value));
			}
		});
		return cookies;
	}

	@Override
	public Flux<DataBuffer> getBody() {

		// Obtain the entity
		InputStream entity = this.httpRequest.getEntity().createBrowseInputStream();

		// Obtain size of body
		byte[] data;
		try {
			int size = entity.available();

			// Obtain the entity data
			data = new byte[size];
			entity.read(data);

		} catch (IOException ex) {
			// No body
			return Flux.just();
		}

		// Return body
		return Flux.just(this.dataBufferFactory.wrap(data));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getNativeRequest() {
		return (T) this.httpRequest;
	}

	/*
	 * ======================== SslInfo ===================================
	 */

	@Override
	public String getSessionId() {
		return null;
	}

	@Override
	public X509Certificate[] getPeerCertificates() {
		return null;
	}

}
