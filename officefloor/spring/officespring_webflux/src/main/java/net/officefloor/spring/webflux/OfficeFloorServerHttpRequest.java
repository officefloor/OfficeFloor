package net.officefloor.spring.webflux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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