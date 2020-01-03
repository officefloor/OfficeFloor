package net.officefloor.web.resource.impl;

import java.io.Closeable;

import net.officefloor.web.resource.HttpResource;

/**
 * Abstract {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResource implements HttpResource, Closeable {

	/**
	 * Path.
	 */
	protected String path;

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            Resource path.
	 */
	public AbstractHttpResource(String path) {
		this.path = path;
	}

	/*
	 * ======================= HttpResource ==========================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public boolean equals(Object obj) {

		// Check if same object
		if (this == obj) {
			return true;
		}

		// Ensure same type
		if (!(obj instanceof AbstractHttpResource)) {
			return false;
		}
		AbstractHttpResource that = (AbstractHttpResource) obj;

		// Return whether same resource by path
		return this.path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}