package net.officefloor.web.resource.spi;

import java.util.ServiceLoader;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceLoader} interface for providing a
 * {@link ResourceTransformerFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformerService extends ServiceFactory<ResourceTransformerFactory> {
}