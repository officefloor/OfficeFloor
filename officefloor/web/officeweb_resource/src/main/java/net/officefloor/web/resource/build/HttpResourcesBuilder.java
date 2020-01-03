package net.officefloor.web.resource.build;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerFactory;
import net.officefloor.web.security.build.HttpSecurableBuilder;

/**
 * Builds the external {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourcesBuilder {

	/**
	 * <p>
	 * Specifies the context path within the application to serve the
	 * {@link HttpResource} instances.
	 * <p>
	 * Should a context path not be specified, the {@link HttpResource}
	 * instances will be served from the root of the application.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	void setContextPath(String contextPath);

	/**
	 * Adds a {@link TypeQualification} for the {@link HttpResourceStore}
	 * {@link ManagedObject} backing this {@link HttpResourcesBuilder}.
	 * 
	 * @param qualifier
	 *            {@link TypeQualification} qualifier.
	 */
	void addTypeQualifier(String qualifier);

	/**
	 * Registers a {@link ResourceTransformer} to transform the resources.
	 * 
	 * @param transformer
	 *            {@link ResourceTransformer}.
	 */
	void addResourceTransformer(ResourceTransformer transformer);

	/**
	 * Registers a {@link ResourceTransformer} from a
	 * {@link ResourceTransformerFactory} to transform the resources.
	 * 
	 * @param name
	 *            Name of the {@link ResourceTransformerFactory} to create the
	 *            {@link ResourceTransformer}.
	 * 
	 * @see ResourceTransformerFactory
	 */
	void addResourceTransformer(String name);

	/**
	 * Specifies the default file names within a directory.
	 * 
	 * @param defaultResourceNames
	 *            Default file names within directory.
	 */
	void setDirectoryDefaultResourceNames(String... defaultResourceNames);

	/**
	 * <p>
	 * Obtains the {@link HttpSecurableBuilder} to configure access controls to
	 * the {@link HttpResource} instances.
	 * <p>
	 * Calling this method without providing configuration requires only
	 * authentication to access {@link HttpResource} instances.
	 * 
	 * @return {@link HttpSecurableBuilder}.
	 */
	HttpSecurableBuilder getHttpSecurer();

}