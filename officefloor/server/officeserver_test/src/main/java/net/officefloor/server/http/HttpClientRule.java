/*-
 * #%L
 * Testing of HTTP Server
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

package net.officefloor.server.http;

import org.apache.http.client.HttpClient;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for a {@link HttpClient} to {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientRule extends AbstractHttpClientJUnit<HttpClientRule> implements TestRule {

	/**
	 * Instantiate for non-secure (HTTP) connection to {@link HttpServer}.
	 */
	public HttpClientRule() {
	}

	/**
	 * Instantiate with flagging if secure (HTTPS) connection to {@link HttpServer}.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 */
	public HttpClientRule(boolean isSecure) {
		super(isSecure);
	}

	/**
	 * Instantiate indicating the server port.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 * @param port     Server port to connect.
	 */
	public HttpClientRule(boolean isSecure, int port) {
		super(isSecure, port);
	}

	/*
	 * =============== TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access to rule
				HttpClientRule rule = HttpClientRule.this;

				// Undertake test
				try {
					rule.openHttpClient();

					// Undertake test
					base.evaluate();

				} finally {
					rule.closeHttpClient();
				}
			}
		};
	}

}
