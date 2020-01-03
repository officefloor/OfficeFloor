package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Transforms resource instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformer {

	/**
	 * <p>
	 * Transforms the resource at {@link Path} to another resource at the return
	 * {@link Path}.
	 * <p>
	 * Typically this is to compress the files, however available for other
	 * transforms to files (such as altering contents of files).
	 * 
	 * @param context
	 *            {@link ResourceTransformerContext}.
	 * @throws IOException
	 *             If fails to transform the resource.
	 */
	void transform(ResourceTransformerContext context) throws IOException;

}