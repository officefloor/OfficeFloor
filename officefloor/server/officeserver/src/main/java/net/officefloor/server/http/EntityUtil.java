package net.officefloor.server.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * {@link HttpRequest} entity utilities.
 * 
 * @author Daniel Sagenschneider
 */
public class EntityUtil {

	/**
	 * Obtains the HTTP entity content from the {@link HttpRequest}.
	 * 
	 * @param request {@link HttpRequest}.
	 * @param charset {@link Charset}. May be <code>null</code> to use default
	 *                {@link Charset}.
	 * @return HTTP entity content.
	 * @throws HttpException If fails to obtain entity content.
	 */
	public static String toString(HttpRequest request, Charset charset) throws HttpException {

		// Ensure have charset
		if (charset == null) {
			charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		}

		// Obtain the content
		StringWriter content = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(request.getEntity().createBrowseInputStream(), charset);
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
		} catch (IOException ex) {
			throw new HttpException(ex);
		}

		// Return the content
		return content.toString();
	}

}