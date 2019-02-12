package net.officefloor.web.jwt.jwks;

import java.security.Key;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * <p>
 * {@link ServiceFactory} to plug in various {@link JwksKeyParser}
 * implementations.
 * <p>
 * This allows extending the {@link Key} instances handled.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyParserServiceFactory extends ServiceFactory<JwksKeyParser> {
}