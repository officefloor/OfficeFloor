package net.officefloor.web.resource.spi;

import java.util.ServiceLoader;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceLoader} interface for providing a
 * {@link ResourceSystemFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystemService extends ServiceFactory<ResourceSystemFactory> {
}