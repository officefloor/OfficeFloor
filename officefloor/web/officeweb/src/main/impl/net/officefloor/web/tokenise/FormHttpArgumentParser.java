/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
