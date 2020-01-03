package net.officefloor.woof.mock;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.ObjectResponse;

/**
 * Mock {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectResponse<T> implements ObjectResponse<T> {

	/**
	 * Object.
	 */
	private T object;

	/**
	 * Obtains the sent object.
	 * 
	 * @return Sent object.
	 */
	public T getObject() {
		return this.object;
	}

	/*
	 * =============== ObjectResponse ==================
	 */

	@Override
	public void send(T object) throws HttpException {
		this.object = object;
	}

}