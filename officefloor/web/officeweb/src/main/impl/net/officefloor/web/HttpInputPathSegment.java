package net.officefloor.web;

/**
 * Segment of the {@link HttpInputPath}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpInputPathSegment {

	/**
	 * Types of {@link HttpInputPathSegment}.
	 */
	public static enum HttpInputPathSegmentEnum {
		STATIC, PARAMETER
	}

	/**
	 * {@link HttpInputPathSegmentEnum}.
	 */
	public final HttpInputPathSegmentEnum type;

	/**
	 * Static path or parameter name.
	 */
	public final String value;

	/**
	 * Next {@link HttpInputPathSegment}.
	 */
	public HttpInputPathSegment next = null;

	/**
	 * Instantiate.
	 * 
	 * @param type
	 *            {@link HttpInputPathSegmentEnum}.
	 * @param value
	 *            Static path or parameter name.
	 */
	public HttpInputPathSegment(HttpInputPathSegmentEnum type, String value) {
		this.type = type;
		this.value = value;
	}
}