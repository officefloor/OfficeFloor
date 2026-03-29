/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.aws.sam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;

/**
 * AWS SAM {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers;

	/**
	 * Instantiate.
	 * 
	 * @param samHeaders SAM headers.
	 */
	public SamNonMaterialisedHttpHeaders(Map<String, List<String>> samHeaders) {

		// Create listing of headers
		this.headers = new ArrayList<>();
		samHeaders.forEach((name, values) -> {
			for (String value : values) {
				HttpHeader header = new SerialisableHttpHeader(name, value);
				this.headers.add(new NonMaterialisedHttpHeader() {

					@Override
					public CharSequence getName() {
						return header.getName();
					}

					@Override
					public HttpHeader materialiseHttpHeader() {
						return header;
					}
				});
			}
		});
	}

	/*
	 * =================== NonMaterialisedHttpHeaders ====================
	 */

	@Override
	public Iterator<NonMaterialisedHttpHeader> iterator() {
		return this.headers.iterator();
	}

	@Override
	public int length() {
		return this.headers.size();
	}

}
