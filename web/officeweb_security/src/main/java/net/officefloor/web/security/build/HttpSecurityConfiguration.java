package net.officefloor.web.security.build;

import lombok.Data;
import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * yml configuration for a {@link net.officefloor.web.spi.security.HttpSecurity}.
 */
@Data
public class HttpSecurityConfiguration extends ComposeConfiguration {

	private HttpSecuritySourceConfiguration security;

}
