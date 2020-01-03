package net.officefloor.server.http;

/**
 * Clock for the <code>Date</code> {@link HttpHeaderValue}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DateHttpHeaderClock {

	/**
	 * Obtains the <code>Date</code> {@link HttpHeaderValue}.
	 * 
	 * @return <code>Date</code> {@link HttpHeaderValue}.
	 */
	HttpHeaderValue getDateHttpHeaderValue();

}