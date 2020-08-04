package net.officefloor.frame.test;

import org.easymock.Capture;
import org.easymock.EasyMock;

/**
 * Captures a parameter value.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterCapture<T> {

	/**
	 * {@link Capture}.
	 */
	private final Capture<T> capture = Capture.newInstance();

	/**
	 * Captures the value.
	 * 
	 * @return Parameter input to capture the value.
	 */
	public T capture() {
		return EasyMock.capture(this.capture);
	}

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	public T getValue() {
		return this.capture.getValue();
	}

}