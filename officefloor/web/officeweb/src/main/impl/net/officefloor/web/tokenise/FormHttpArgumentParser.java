package net.officefloor.web.tokenise;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Form {@link HttpArgumentParser}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormHttpArgumentParser implements HttpArgumentParser {

	/*
	 * =================== HttpArgumentParser ====================
	 */

	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public void parse(HttpRequest request, ValueLoader valueLoader) throws HttpException {
		HttpRequestTokeniser.tokeniseFormEntity(request, valueLoader);
	}

}