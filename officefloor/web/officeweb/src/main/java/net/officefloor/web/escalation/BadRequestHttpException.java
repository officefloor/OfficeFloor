package net.officefloor.web.escalation;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.WritableHttpHeader;

/**
 * Bad request {@link HttpException}.
 * 
 * @author Daniel Sagenschneider
 */
public class BadRequestHttpException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param headers {@link WritableHttpHeader} instances.
	 * @param entity  {@link HttpResponse} entity.
	 */
	public BadRequestHttpException(WritableHttpHeader[] headers, String entity) {
		super(HttpStatus.BAD_REQUEST, headers, entity);
	}

}