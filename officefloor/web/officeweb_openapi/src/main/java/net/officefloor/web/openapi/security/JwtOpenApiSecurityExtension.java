package net.officefloor.web.openapi.security;

import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link OpenApiSecurityExtension} for <code>basic</code> authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtOpenApiSecurityExtension implements OpenApiSecurityExtension, OpenApiSecurityExtensionServiceFactory {

	/**
	 * {@link Class} of the <code>JWT</code> {@link HttpSecuritySource}.
	 */
	public static final String JWT_HTTP_SECURITY_SOURCE_CLASS_NAME = "net.officefloor.web.jwt.JwtHttpSecuritySource";

	/*
	 * ======================== OpenApiSecurityExtension =======================
	 */

	@Override
	public OpenApiSecurityExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extend(OpenApiSecurityExtensionContext context) throws Exception {
		if (context.getHttpSecurity().getHttpSecuritySource().getClass().getName()
				.equals(JWT_HTTP_SECURITY_SOURCE_CLASS_NAME)) {
			SecurityScheme scheme = new SecurityScheme();
			String securityName = context.getHttpSecurity().getHttpSecurityName();
			scheme.setType(Type.HTTP);
			scheme.setScheme("bearer");
			context.addSecurityScheme(securityName, scheme);
		}
	}

}