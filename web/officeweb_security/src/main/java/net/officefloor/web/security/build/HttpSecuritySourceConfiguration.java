package net.officefloor.web.security.build;

import lombok.Data;

import java.util.Map;

/**
 * Configuration for a {@link net.officefloor.web.spi.security.HttpSecuritySource} within a
 * {@link HttpSecurityConfiguration}.
 */
@Data
public class HttpSecuritySourceConfiguration {

	/**
	 * {@link net.officefloor.web.spi.security.HttpSecuritySource} class name.
	 */
	private String source;

	/**
	 * Properties for the {@link net.officefloor.web.spi.security.HttpSecuritySource}.
	 */
	private Map<String, String> properties;

	/**
	 * Maps flow enum names to composition names within the yml file.
	 */
	private Map<String, String> flows;

}
