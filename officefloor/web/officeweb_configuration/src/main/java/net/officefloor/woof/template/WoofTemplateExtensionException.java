package net.officefloor.woof.template;

import net.officefloor.web.template.extension.WebTemplateExtension;

/**
 * Flags that the {@link WebTemplateExtension} is unknown or unable to be
 * obtained.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param message Reason.
	 * @param cause   Cause.
	 */
	public WoofTemplateExtensionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Initiate.
	 * 
	 * @param message Reason.
	 */
	public WoofTemplateExtensionException(String message) {
		super(message);
	}
}