/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;

/**
 * {@link HttpServlet} {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers;

	/**
	 * Instantiate.
	 * 
	 * @param asyncRequest {@link HttpServletRequest}.
	 */
	public HttpServletNonMaterialisedHttpHeaders(HttpServletRequest asyncRequest) {

		// Create listing of headers
		this.headers = new ArrayList<>();
		Enumeration<String> headerNames = asyncRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();

			// Add header
			Enumeration<String> headerValues = asyncRequest.getHeaders(name);
			while (headerValues.hasMoreElements()) {
				String value = headerValues.nextElement();
				HttpHeader header = new SerialisableHttpHeader(name, value);
				headers.add(new NonMaterialisedHttpHeader() {

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
		}
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
