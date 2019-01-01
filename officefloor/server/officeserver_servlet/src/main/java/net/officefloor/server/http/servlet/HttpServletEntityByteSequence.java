package net.officefloor.server.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ByteSequence} for the {@link HttpServletRequest} entity.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletEntityByteSequence implements ByteSequence {

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request;

	/**
	 * Bytes.
	 */
	private volatile byte[] bytes;

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpServletRequest}.
	 */
	public HttpServletEntityByteSequence(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Ensures the bytes are loaded.
	 */
	private void ensureBytesLoaded() {
		try {
			if (this.bytes == null) {
				synchronized (this) {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					InputStream requestEntity = this.request.getInputStream();
					for (int value = requestEntity.read(); value != -1; value = requestEntity.read()) {
						buffer.write(value);
					}
					this.bytes = buffer.toByteArray();
				}
			}
		} catch (IOException ex) {
			// Failed to service (as must obtain entity)
			throw new HttpException(ex);
		}
	}

	/*
	 * ================== ByteSequence =======================
	 */

	@Override
	public byte byteAt(int index) {
		this.ensureBytesLoaded();
		return this.bytes[index];
	}

	@Override
	public int length() {
		this.ensureBytesLoaded();
		return this.bytes.length;
	}

}