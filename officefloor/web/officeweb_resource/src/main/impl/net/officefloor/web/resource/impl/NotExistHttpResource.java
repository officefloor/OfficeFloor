package net.officefloor.web.resource.impl;

import java.io.IOException;

import net.officefloor.web.resource.HttpResource;

/**
 * Not existing {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class NotExistHttpResource extends AbstractHttpResource {

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            Path.
	 */
	public NotExistHttpResource(String path) {
		super(path);
	}

	/*
	 * ====================== HttpResource ======================
	 */

	@Override
	public boolean isExist() {
		return false;
	}

	/*
	 * ===================== Closeable ==========================
	 */

	@Override
	public void close() throws IOException {
	}

}