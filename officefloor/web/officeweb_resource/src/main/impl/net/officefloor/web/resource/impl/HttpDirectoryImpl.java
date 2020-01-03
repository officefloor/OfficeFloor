package net.officefloor.web.resource.impl;

import java.io.IOException;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link HttpDirectory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpDirectoryImpl extends AbstractHttpResource implements HttpDirectory {

	/**
	 * {@link HttpResourceStore}.
	 */
	private final HttpResourceStore store;

	/**
	 * Instantiate.
	 * 
	 * @param path  Path to the {@link HttpDirectory}.
	 * @param store {@link HttpResourceStore}.
	 */
	public HttpDirectoryImpl(String path, HttpResourceStore store) {
		super(path);
		this.store = store;
	}

	/*
	 * ================= HttpDirectory =====================
	 */

	@Override
	public boolean isExist() {
		return true;
	}

	@Override
	public HttpFile getDefaultHttpFile() throws IOException {
		return this.store.getDefaultHttpFile(this);
	}

	/*
	 * ===================== Closeable ==========================
	 */

	@Override
	public void close() throws IOException {
	}

}