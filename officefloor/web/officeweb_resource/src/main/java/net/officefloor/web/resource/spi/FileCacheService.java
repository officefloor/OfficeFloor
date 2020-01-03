package net.officefloor.web.resource.spi;

import java.util.ServiceLoader;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceLoader} interface for providing a {@link FileCacheFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCacheService extends ServiceFactory<FileCacheFactory> {
}