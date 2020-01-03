package net.officefloor.web.template.section;

import java.nio.charset.Charset;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Interface to write the template content to {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateWriter {

	/**
	 * Writes the template content to the {@link ServerWriter}.
	 * 
	 * @param writer
	 *            {@link ServerWriter} to receive the template content.
	 * @param isDefaultCharset
	 *            Indicates if the default {@link Charset} for outputting the
	 *            template is being used. While the {@link Charset} may be
	 *            programmatically changed, it is expected in the majority of
	 *            cases to be using the default {@link Charset} configured for
	 *            the template. This flag allows static content to be cached in
	 *            bytes (using default {@link Charset}) for improved
	 *            performance.
	 * @param bean
	 *            Bean to potentially obtain data. May be <code>null</code> if
	 *            template contents does not require a bean.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param templatePath
	 *            Current path for the {@link WebTemplate}. As the path may be
	 *            dynamic (contain path parameters), this is the path to be used
	 *            by links in rending of the {@link WebTemplate}.
	 * @throws HttpException
	 *             If fails to write content.
	 */
	void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException;

}