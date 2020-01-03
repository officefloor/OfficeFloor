package net.officefloor.plugin.xml;

import java.io.IOException;

/**
 * Indicates failure to marshall/unmarshall XML.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMarshallException extends IOException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Enforce reason.
	 * 
	 * @param reason Reason.
	 */
	public XmlMarshallException(String reason) {
		super(reason);
	}

	/**
	 * Enforce reason and allow cause.
	 * 
	 * @param reason Reason.
	 * @param cause  Cause.
	 */
	public XmlMarshallException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
