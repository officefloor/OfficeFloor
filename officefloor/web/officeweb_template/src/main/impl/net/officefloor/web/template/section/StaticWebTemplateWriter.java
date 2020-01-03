package net.officefloor.web.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.parse.StaticParsedTemplateSectionContent;

/**
 * {@link WebTemplateWriter} to write static content.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticWebTemplateWriter implements WebTemplateWriter {

	/**
	 * Static content as text.
	 */
	private final String textContent;

	/**
	 * Encoded content to write to the {@link ServerWriter}.
	 */
	private final byte[] encodedContent;

	/**
	 * Initiate.
	 * 
	 * @param staticContent
	 *            {@link StaticParsedTemplateSectionContent} to write.
	 * @param charset
	 *            {@link Charset} for the template.
	 * @throws IOException
	 *             If fails to prepare the static content.
	 */
	public StaticWebTemplateWriter(StaticParsedTemplateSectionContent staticContent, Charset charset)
			throws IOException {
		this.textContent = staticContent.getStaticContent();

		// Pre-encode the static content for faster I/O
		this.encodedContent = this.textContent.getBytes(charset);
	}

	/*
	 * ================ HttpTemplateWriter ===================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException {

		try {
			// Use pre-encoded content if using default charset
			if (isDefaultCharset) {
				// Provide pre-encoded content
				writer.write(this.encodedContent);

			} else {
				// Provide the content (with appropriate charset)
				writer.write(this.textContent);
			}

		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}