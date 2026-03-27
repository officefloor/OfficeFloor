package net.officefloor.server.google.function.wrap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpStatus;

/**
 * {@link HttpFunction} {@link com.google.cloud.functions.HttpResponse}.
 */
public class HttpFunctionHttpResponse implements com.google.cloud.functions.HttpResponse {

	/**
	 * {@link net.officefloor.server.http.HttpResponse}.
	 */
	private final net.officefloor.server.http.HttpResponse response;

	/**
	 * Indicates if the <code>Content-Type</code> is specified.
	 */
	private boolean isContentTypeSpecified = false;

	/**
	 * Cache the {@link BufferedWriter}.
	 */
	private BufferedWriter writer = null;

	/**
	 * Instantiate.
	 * 
	 * @param response {@link net.officefloor.server.http.HttpResponse}.
	 */
	public HttpFunctionHttpResponse(net.officefloor.server.http.HttpResponse response) {
		this.response = response;
	}

	/**
	 * Ensures to flush and send response.
	 * 
	 * @throws Exception If fails to send.
	 */
	public void flushEntity() throws Exception {
		if (this.writer != null) {
			this.writer.flush();
		}
	}

	/*
	 * ================== HttpResponse =======================
	 */

	@Override
	public void setStatusCode(int code) {
		this.response.setStatus(HttpStatus.getHttpStatus(code));
	}

	@Override
	public void setStatusCode(int code, String message) {
		this.response.setStatus(new HttpStatus(code, message));
	}

	@Override
	public void setContentType(String contentType) {
		try {
			this.response.setContentType(contentType, null);
			this.isContentTypeSpecified = (contentType != null);
		} catch (IOException ex) {
			throw new IllegalStateException("Should always be able to set Content-Type", ex);
		}
	}

	@Override
	public Optional<String> getContentType() {
		return this.isContentTypeSpecified ? Optional.of(this.response.getContentType()) : Optional.empty();
	}

	@Override
	public void appendHeader(String header, String value) {
		this.response.getHeaders().addHeader(header, value);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		Map<String, List<String>> headers = new HashMap<>();
		for (HttpHeader header : this.response.getHeaders()) {
			String name = header.getName();
			List<String> values = headers.get(name);
			if (values == null) {
				values = new LinkedList<>();
				headers.put(name, values);
			}
			values.add(header.getValue());
		}
		return headers;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.response.getEntity();
	}

	@Override
	public BufferedWriter getWriter() throws IOException {
		if (this.writer == null) {
			this.writer = new BufferedWriter(this.response.getEntityWriter());
		}
		return this.writer;
	}
}
