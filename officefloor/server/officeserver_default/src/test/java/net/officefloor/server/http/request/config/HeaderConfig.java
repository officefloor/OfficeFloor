package net.officefloor.server.http.request.config;

/**
 * HTTP header parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class HeaderConfig {

	/**
	 * Header parameter name.
	 */
	public String name;

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Header parameter value.
	 */
	public String value;

	public void setValue(String value) {
		this.value = value;
	}
}
