package net.officefloor.vertx;

import io.vertx.core.Vertx;

/**
 * Wrapper {@link Exception} for {@link Throwable} from {@link Vertx}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorVertxException extends Exception {

	/**
	 * Serialisaion verstion.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public OfficeFloorVertxException(Throwable cause) {
		super(cause);
	}

}