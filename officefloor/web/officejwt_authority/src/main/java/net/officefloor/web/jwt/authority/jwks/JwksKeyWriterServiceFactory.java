package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * <p>
 * {@link ServiceFactory} to plug in various {@link JwksKeyWriter}
 * implementations.
 * <p>
 * This allows extending the {@link Key} instances handled.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyWriterServiceFactory extends ServiceFactory<JwksKeyWriter<? extends Key>> {
}